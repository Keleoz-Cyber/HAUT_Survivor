package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.EventRecord;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.mapper.CampusLocationMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.EventRecordMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.service.EventService;
import cn.haut.survivor.service.ExplorationService;
import cn.haut.survivor.service.PlayerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EventServiceImpl implements EventService {

    private final CampusLocationMapper campusLocationMapper;
    private final EventMapper eventMapper;
    private final EventOptionMapper eventOptionMapper;
    private final EventRecordMapper eventRecordMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;
    private final ExplorationService explorationService;

    public EventServiceImpl(
            CampusLocationMapper campusLocationMapper,
            EventMapper eventMapper,
            EventOptionMapper eventOptionMapper,
            EventRecordMapper eventRecordMapper,
            PlayerAttributeMapper playerAttributeMapper,
            PlayerProfileMapper playerProfileMapper,
            PlayerService playerService,
            ExplorationService explorationService
    ) {
        this.campusLocationMapper = campusLocationMapper;
        this.eventMapper = eventMapper;
        this.eventOptionMapper = eventOptionMapper;
        this.eventRecordMapper = eventRecordMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerService = playerService;
        this.explorationService = explorationService;
    }

    @Override
    public List<CampusLocation> listEnabledLocations() {
        return campusLocationMapper.selectList(new LambdaQueryWrapper<CampusLocation>()
                .eq(CampusLocation::getStatus, 1)
                .orderByAsc(CampusLocation::getId));
    }

    @Override
    public List<Event> listEnabledEventsForLocation(Long locationId, Integer currentWeek) {
        return listEnabledEventsForLocation(locationId, currentWeek, 0);
    }

    @Override
    public List<Event> listEnabledEventsForLocation(Long locationId, Integer currentWeek, Integer exploreLevel) {
        int week = currentWeek == null ? 1 : currentWeek;
        int level = exploreLevel == null ? 0 : exploreLevel;
        return eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .eq(Event::getStatus, 1)
                .eq(Event::getLocationId, locationId)
                .le(Event::getMinWeek, week)
                .ge(Event::getMaxWeek, week)
                .le(Event::getMinExploreLevel, level)
                .orderByDesc(Event::getProbability)
                .orderByAsc(Event::getId));
    }

    @Override
    public Event triggerRandomEvent(Long userId, Long locationId) {
        PlayerProfile profile = requireProfile(userId);
        int exploreLevel = explorationService.getExploreLevel(userId, locationId);
        List<Event> events = listEnabledEventsForLocation(locationId, profile.getCurrentWeek(), exploreLevel);
        if (events.isEmpty()) {
            return null;
        }
        int totalWeight = events.stream()
                .map(Event::getProbability)
                .mapToInt(probability -> Math.max(probability == null ? 1 : probability, 1))
                .sum();
        int ticket = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (Event event : events) {
            cursor += Math.max(event.getProbability() == null ? 1 : event.getProbability(), 1);
            if (ticket < cursor) {
                return event;
            }
        }
        return events.get(0);
    }

    @Override
    public List<EventOption> listOptions(Long eventId) {
        return eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                .eq(EventOption::getEventId, eventId)
                .orderByAsc(EventOption::getId));
    }

    @Override
    @Transactional
    public EventRecord chooseOption(Long userId, Long eventId, Long optionId) {
        PlayerAttribute attribute = requireAttribute(userId);
        PlayerProfile profile = requireProfile(userId);
        EventOption option = requireOption(eventId, optionId);

        // 记录旧值用于计算实际变化
        int oldAcademic = attribute.getAcademic(), oldHealth = attribute.getHealth();
        int oldMoney = attribute.getMoney(), oldSocial = attribute.getSocial();
        int oldSkill = attribute.getSkill(), oldPressure = attribute.getPressure();
        int oldDiscipline = attribute.getDiscipline();
        int oldExp = profile.getExp();

        attribute.setAcademic(clamp(attribute.getAcademic() + option.getAcademicChange()));
        attribute.setHealth(clamp(attribute.getHealth() + option.getHealthChange()));
        attribute.setMoney(clamp(attribute.getMoney() + option.getMoneyChange()));
        attribute.setSocial(clamp(attribute.getSocial() + option.getSocialChange()));
        attribute.setSkill(clamp(attribute.getSkill() + option.getSkillChange()));
        attribute.setPressure(clamp(attribute.getPressure() + option.getPressureChange()));
        attribute.setDiscipline(clamp(attribute.getDiscipline() + option.getDisciplineChange()));
        attribute.setUpdateTime(LocalDateTime.now());
        playerAttributeMapper.updateById(attribute);

        profile.setExp(profile.getExp() + option.getExpChange());
        playerProfileMapper.updateById(profile);

        EventRecord record = new EventRecord();
        record.setUserId(userId);
        record.setEventId(eventId);
        record.setOptionId(optionId);
        record.setResultText(option.getResultText());
        record.setCreateTime(LocalDateTime.now());
        // 记录实际属性变化（考虑 clamp 边界）
        record.setAttributeChange(new AttributeChange(
                attribute.getAcademic() - oldAcademic,
                attribute.getHealth() - oldHealth,
                attribute.getMoney() - oldMoney,
                attribute.getSocial() - oldSocial,
                attribute.getSkill() - oldSkill,
                attribute.getPressure() - oldPressure,
                attribute.getDiscipline() - oldDiscipline,
                profile.getExp() - oldExp
        ));
        eventRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<Event> listAllEvents() {
        return eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .orderByAsc(Event::getLocationId)
                .orderByAsc(Event::getId));
    }

    @Override
    public Event findEventById(Long eventId) {
        if (eventId == null) {
            return null;
        }
        return eventMapper.selectById(eventId);
    }

    @Override
    @Transactional
    public Event createEvent(String eventName, String eventType, Long locationId, String description,
                             Integer probability, Integer minWeek, Integer maxWeek) {
        Event event = new Event();
        fillEvent(event, eventName, eventType, locationId, description, probability, minWeek, maxWeek);
        event.setStatus(1);
        eventMapper.insert(event);
        return event;
    }

    @Override
    @Transactional
    public Event updateEvent(Long eventId, String eventName, String eventType, Long locationId, String description,
                             Integer probability, Integer minWeek, Integer maxWeek) {
        Event event = findEventById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        fillEvent(event, eventName, eventType, locationId, description, probability, minWeek, maxWeek);
        eventMapper.updateById(event);
        return event;
    }

    @Override
    @Transactional
    public void disableEvent(Long eventId) {
        Event event = findEventById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        event.setStatus(0);
        eventMapper.updateById(event);
    }

    private PlayerProfile requireProfile(Long userId) {
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("请先创建角色");
        }
        return profile;
    }

    private PlayerAttribute requireAttribute(Long userId) {
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        if (attribute == null) {
            throw new IllegalArgumentException("角色属性不存在");
        }
        return attribute;
    }

    private EventOption requireOption(Long eventId, Long optionId) {
        return listOptions(eventId).stream()
                .filter(option -> option.getId().equals(optionId))
                .min(Comparator.comparing(EventOption::getId))
                .orElseThrow(() -> new IllegalArgumentException("事件选项不存在"));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private void fillEvent(Event event, String eventName, String eventType, Long locationId, String description,
                           Integer probability, Integer minWeek, Integer maxWeek) {
        if (locationId == null) {
            throw new IllegalArgumentException("地点不能为空");
        }
        event.setEventName(requireText(eventName, "事件名称不能为空"));
        event.setEventType(requireText(eventType, "事件类型不能为空"));
        event.setLocationId(locationId);
        event.setDescription(requireText(description, "事件描述不能为空"));
        event.setProbability(clampProbability(probability));
        event.setMinWeek(minWeek == null ? 1 : minWeek);
        event.setMaxWeek(maxWeek == null ? 20 : maxWeek);
    }

    private int clampProbability(Integer probability) {
        int value = probability == null ? 50 : probability;
        return Math.max(1, Math.min(100, value));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
