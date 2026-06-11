package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 周主题系统：消费 16 周学期阶段，不再在各业务服务中散落周次判断。
 */
@Service
public class WeeklyThemeService {

    public record WeekTheme(
            int week,
            String name,
            String description,
            String hint,
            String icon
    ) {}

    private final SemesterCalendarService semesterCalendarService;

    public WeeklyThemeService(SemesterCalendarService semesterCalendarService) {
        this.semesterCalendarService = semesterCalendarService;
    }

    /** 获取指定周的主题，基于学期阶段映射 */
    public WeekTheme getTheme(int currentWeek) {
        SemesterCalendarService.SemesterStage stage = semesterCalendarService.stageForWeek(currentWeek);
        return new WeekTheme(currentWeek, stage.name(), stage.description(), stage.hint(), stage.icon());
    }

    /** Returns the event type boosted by the current weekly theme. */
    public String preferredEventType(Integer currentWeek) {
        return semesterCalendarService.preferredEventType(currentWeek);
    }

    /** Organization activity bonus during rhythm stage (社团招新峰值). */
    public int organizationActivityBonus(Integer currentWeek) {
        String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
        return "rhythm".equals(stageKey) ? 1 : 0;
    }

    /** Opening stage lowers the social threshold for joining organizations. */
    public int organizationJoinSocialRequirementReduction(Integer currentWeek) {
        String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
        return "opening".equals(stageKey) ? 5 : 0;
    }

    /** Opening stage makes first contacts slightly easier, but does not stack with weekly buddy familiarity bonus. */
    public int npcOpeningWeekFamiliarityBonus(Integer currentWeek, boolean weeklyBuddy) {
        if (weeklyBuddy) {
            return 0;
        }
        String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
        return "opening".equals(stageKey) ? 1 : 0;
    }

    /** Short suffix shown on NPC interaction result text when the opening stage bonus applies. */
    public String openingWeekNpcInteractionSuffix(Integer currentWeek, boolean weeklyBuddy) {
        return npcOpeningWeekFamiliarityBonus(currentWeek, weeklyBuddy) > 0
                ? " 开学适应阶段：新学期大家都在重新认识彼此，本次熟悉度额外 +1。"
                : "";
    }

    /** Extra pressure applied to dungeon settlements during project stage (DDL 高压). */
    public int dungeonPressureBonus(Integer currentWeek) {
        String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
        return "project".equals(stageKey) ? 1 : 0;
    }

    /** Final stage makes library review and playground physical-test routes more productive. */
    public AttributeChange finalWeekExplorationAttributeChange(Integer currentWeek, Long locationId) {
        String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
        if (!"final".equals(stageKey) || locationId == null) {
            return AttributeChange.EMPTY;
        }
        if (locationId == 2L) {
            return new AttributeChange(1, 0, 0, 0, 1, -1, 1, 0);
        }
        if (locationId == 5L) {
            return new AttributeChange(0, 2, 0, 0, 0, -1, 1, 0);
        }
        return AttributeChange.EMPTY;
    }

    /** Final stage slightly buffers pressure in physical-test dungeons. */
    public int finalWeekDungeonPressureRelief(Integer currentWeek, String dungeonType) {
        String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
        if (!"final".equals(stageKey)) {
            return 0;
        }
        return "physical".equalsIgnoreCase(dungeonType) ? -1 : 0;
    }

    /** Short suffix shown on physical dungeon result text when final-stage relief applies. */
    public String finalWeekDungeonResultSuffix(Integer currentWeek, String dungeonType) {
        return finalWeekDungeonPressureRelief(currentWeek, dungeonType) < 0
                ? " 期末与体测阶段：你提前适应了节奏，本阶段压力额外 -1。"
                : "";
    }

    /** 获取所有阶段主题 */
    public List<WeekTheme> allThemes() {
        return semesterCalendarService.allStages().stream()
                .map(stage -> new WeekTheme(stage.weekStart(), stage.name(), stage.description(), stage.hint(), stage.icon()))
                .toList();
    }
}
