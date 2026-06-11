package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 周主题系统：消费 16 周学期阶段，不再在各业务服务中散落周次判断。
 * 所有玩法数值从 SemesterStage 读取，不再各自硬编码。
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

    /** Organization activity bonus from stage data (e.g. rhythm stage +1). */
    public int organizationActivityBonus(Integer currentWeek) {
        return semesterCalendarService.stageForWeek(currentWeek).organizationActivityBonus();
    }

    /** Opening stage lowers the social threshold for joining organizations. */
    public int organizationJoinSocialRequirementReduction(Integer currentWeek) {
        return semesterCalendarService.stageForWeek(currentWeek).organizationJoinSocialReduction();
    }

    /** Opening stage makes first contacts slightly easier, but does not stack with weekly buddy familiarity bonus. */
    public int npcOpeningWeekFamiliarityBonus(Integer currentWeek, boolean weeklyBuddy) {
        if (weeklyBuddy) {
            return 0;
        }
        return semesterCalendarService.stageForWeek(currentWeek).npcFamiliarityBonus();
    }

    /** Short suffix shown on NPC interaction result text when the opening stage bonus applies. */
    public String openingWeekNpcInteractionSuffix(Integer currentWeek, boolean weeklyBuddy) {
        return npcOpeningWeekFamiliarityBonus(currentWeek, weeklyBuddy) > 0
                ? " 开学适应阶段：新学期大家都在重新认识彼此，本次熟悉度额外 +1。"
                : "";
    }

    /** Extra pressure applied to dungeon settlements from stage data (e.g. project stage +1). */
    public int dungeonPressureBonus(Integer currentWeek) {
        return semesterCalendarService.stageForWeek(currentWeek).dungeonPressureBonus();
    }

    /** Final stage makes library review and playground physical-test routes more productive.
     *  Uses stage.primaryLocationIds to determine valid locations instead of hardcoded IDs. */
    public AttributeChange finalWeekExplorationAttributeChange(Integer currentWeek, Long locationId) {
        SemesterCalendarService.SemesterStage stage = semesterCalendarService.stageForWeek(currentWeek);
        if (locationId == null || !"final".equals(stage.stageKey())
                || !stage.primaryLocationIds().contains(locationId)) {
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

    /** Dungeon pressure relief from stage data (e.g. final stage -1 for physical type). */
    public int finalWeekDungeonPressureRelief(Integer currentWeek, String dungeonType) {
        int relief = semesterCalendarService.stageForWeek(currentWeek).dungeonPressureRelief();
        if (relief == 0) {
            return 0;
        }
        return "physical".equalsIgnoreCase(dungeonType) ? relief : 0;
    }

    /** Short suffix shown on physical dungeon result text when stage relief applies. */
    public String finalWeekDungeonResultSuffix(Integer currentWeek, String dungeonType) {
        return finalWeekDungeonPressureRelief(currentWeek, dungeonType) < 0
                ? " 期末与体测阶段：你提前适应了节奏，本阶段压力额外 -1。"
                : "";
    }

    /** Returns the stage summary hint for the given week. */
    public String stageSummaryHint(Integer currentWeek) {
        return semesterCalendarService.stageForWeek(currentWeek).stageSummaryHint();
    }

    /** 获取所有阶段主题 */
    public List<WeekTheme> allThemes() {
        return semesterCalendarService.allStages().stream()
                .map(stage -> new WeekTheme(stage.weekStart(), stage.name(), stage.description(), stage.hint(), stage.icon()))
                .toList();
    }
}
