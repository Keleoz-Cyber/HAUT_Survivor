package cn.haut.survivor.service.impl;

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
import cn.haut.survivor.service.PlayerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public EventServiceImpl(
            CampusLocationMapper campusLocationMapper,
            EventMapper eventMapper,
            EventOptionMapper eventOptionMapper,
            EventRecordMapper eventRecordMapper,
            PlayerAttributeMapper playerAttributeMapper,
            PlayerProfileMapper playerProfileMapper,
            PlayerService playerService
    ) {
        this.campusLocationMapper = campusLocationMapper;
        this.eventMapper = eventMapper;
        this.eventOptionMapper = eventOptionMapper;
        this.eventRecordMapper = eventRecordMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerService = playerService;
    }

    @Override
    public List<CampusLocation> listEnabledLocations() {
        return campusLocationMapper.selectList(new LambdaQueryWrapper<CampusLocation>()
                .eq(CampusLocation::getStatus, 1)
                .orderByAsc(CampusLocation::getId));
    }

    @Override
    public List<Event> listEnabledEventsForLocation(Long locationId, Integer currentWeek) {
        int week = currentWeek == null ? 1 : currentWeek;
        return eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .eq(Event::getStatus, 1)
                .eq(Event::getLocationId, locationId)
                .le(Event::getMinWeek, week)
                .ge(Event::getMaxWeek, week)
                .orderByDesc(Event::getProbability)
                .orderByAsc(Event::getId));
    }

    @Override
    public Event triggerRandomEvent(Long userId, Long locationId) {
        PlayerProfile profile = requireProfile(userId);
        List<Event> events = listEnabledEventsForLocation(locationId, profile.getCurrentWeek());
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
        eventRecordMapper.insert(record);
        return record;
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
}
