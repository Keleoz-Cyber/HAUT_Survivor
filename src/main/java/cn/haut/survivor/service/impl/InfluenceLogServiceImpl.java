package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.UserInfluenceLog;
import cn.haut.survivor.mapper.UserInfluenceLogMapper;
import cn.haut.survivor.service.InfluenceLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfluenceLogServiceImpl implements InfluenceLogService {

    private final UserInfluenceLogMapper userInfluenceLogMapper;

    public InfluenceLogServiceImpl(UserInfluenceLogMapper userInfluenceLogMapper) {
        this.userInfluenceLogMapper = userInfluenceLogMapper;
    }

    @Override
    @Transactional
    public void recordExplorationInfluences(Long userId, int weekNumber, Long locationId, List<ExplorationInfluence> influences) {
        if (userId == null || influences == null || influences.isEmpty()) {
            return;
        }

        for (ExplorationInfluence influence : influences) {
            if (influence == null || !influence.hasEffect()) {
                continue;
            }
            recordInfluence(userId, weekNumber, locationId, influence.sourceType(), influence.sourceName(),
                    influence.description(), influence.attributeChange(), influence.exploreBonus());
        }
    }

    @Override
    @Transactional
    public void recordInfluence(Long userId, int weekNumber, Long locationId, String sourceType,
                                String sourceName, String description, AttributeChange change, int exploreBonus) {
        if (userId == null) {
            return;
        }
        AttributeChange safeChange = change != null ? change : AttributeChange.EMPTY;

        UserInfluenceLog log = new UserInfluenceLog();
        log.setUserId(userId);
        log.setWeekNumber(weekNumber);
        log.setLocationId(locationId);
        log.setSourceType(sourceType);
        log.setSourceName(sourceName);
        log.setDescription(description);
        log.setAcademicChange(safeChange.academicChange());
        log.setHealthChange(safeChange.healthChange());
        log.setMoneyChange(safeChange.moneyChange());
        log.setSocialChange(safeChange.socialChange());
        log.setSkillChange(safeChange.skillChange());
        log.setPressureChange(safeChange.pressureChange());
        log.setDisciplineChange(safeChange.disciplineChange());
        log.setExpChange(safeChange.expChange());
        log.setExploreBonus(exploreBonus);
        log.setCreatedAt(LocalDateTime.now());
        userInfluenceLogMapper.insert(log);
    }

    @Override
    public List<InfluenceLogEntry> listWeekInfluences(Long userId, int weekNumber) {
        if (userId == null) {
            return List.of();
        }

        return userInfluenceLogMapper.selectList(new LambdaQueryWrapper<UserInfluenceLog>()
                        .eq(UserInfluenceLog::getUserId, userId)
                        .eq(UserInfluenceLog::getWeekNumber, weekNumber)
                        .orderByAsc(UserInfluenceLog::getId))
                .stream()
                .map(this::toEntry)
                .toList();
    }

    @Override
    public List<WeekInfluenceRecap> listSemesterInfluenceRecaps(Long userId) {
        if (userId == null) {
            return List.of();
        }

        Map<Integer, List<InfluenceLogEntry>> entriesByWeek = new LinkedHashMap<>();
        userInfluenceLogMapper.selectList(new LambdaQueryWrapper<UserInfluenceLog>()
                        .eq(UserInfluenceLog::getUserId, userId)
                        .orderByDesc(UserInfluenceLog::getWeekNumber)
                        .orderByAsc(UserInfluenceLog::getId))
                .stream()
                .map(this::toEntryWithWeek)
                .forEach(entry -> entriesByWeek
                        .computeIfAbsent(entry.weekNumber(), ignored -> new ArrayList<>())
                        .add(entry.entry()));

        return entriesByWeek.entrySet().stream()
                .map(entry -> new WeekInfluenceRecap(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();
    }

    private EntryWithWeek toEntryWithWeek(UserInfluenceLog log) {
        return new EntryWithWeek(value(log.getWeekNumber()), toEntry(log));
    }

    private InfluenceLogEntry toEntry(UserInfluenceLog log) {
        int academic = value(log.getAcademicChange());
        int health = value(log.getHealthChange());
        int money = value(log.getMoneyChange());
        int social = value(log.getSocialChange());
        int skill = value(log.getSkillChange());
        int pressure = value(log.getPressureChange());
        int discipline = value(log.getDisciplineChange());
        int exp = value(log.getExpChange());
        int explore = value(log.getExploreBonus());

        return new InfluenceLogEntry(
                log.getId(),
                log.getSourceType(),
                sourceLabel(log.getSourceType()),
                log.getSourceName(),
                log.getDescription(),
                academic,
                health,
                money,
                social,
                skill,
                pressure,
                discipline,
                exp,
                explore,
                buildChangeText(academic, health, money, social, skill, pressure, discipline, exp, explore)
        );
    }

    private String sourceLabel(String sourceType) {
        return switch (sourceType != null ? sourceType : "") {
            case "rumor" -> "校园传闻";
            case "weekly_theme" -> "周主题";
            case "buddy" -> "本周搭子";
            case "buddy_rescue" -> "搭子救场";
            case "npc_story" -> "关系推进";
            case "story" -> "校园奇遇";
            default -> "校园影响";
        };
    }

    private String buildChangeText(int academic, int health, int money, int social, int skill,
                                   int pressure, int discipline, int exp, int explore) {
        List<String> parts = new ArrayList<>();
        addPart(parts, "学习", academic);
        addPart(parts, "健康", health);
        addPart(parts, "金钱", money);
        addPart(parts, "社交", social);
        addPart(parts, "技能", skill);
        addPart(parts, "压力", pressure);
        addPart(parts, "自律", discipline);
        addPart(parts, "经验", exp);
        addPart(parts, "探索", explore);
        return parts.isEmpty() ? "效果已触发" : String.join("，", parts);
    }

    private void addPart(List<String> parts, String label, int value) {
        if (value == 0) {
            return;
        }
        parts.add(label + " " + (value > 0 ? "+" : "") + value);
    }

    private int value(Integer value) {
        return value != null ? value : 0;
    }

    private record EntryWithWeek(int weekNumber, InfluenceLogEntry entry) {}
}
