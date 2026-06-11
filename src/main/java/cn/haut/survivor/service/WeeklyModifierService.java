package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import org.springframework.stereotype.Service;

@Service
public class WeeklyModifierService {

    private final WeeklyThemeService weeklyThemeService;
    private final SemesterCalendarService semesterCalendarService;

    public WeeklyModifierService(WeeklyThemeService weeklyThemeService,
                                  SemesterCalendarService semesterCalendarService) {
        this.weeklyThemeService = weeklyThemeService;
        this.semesterCalendarService = semesterCalendarService;
    }

    public ExplorationInfluence getExplorationInfluence(int weekNumber, Long locationId) {
        SemesterCalendarService.SemesterStage stage = semesterCalendarService.stageForWeek(weekNumber);
        String stageKey = stage.stageKey();

        // opening: week 1 explore bonus (fresh campus feel)
        if ("opening".equals(stageKey) && weekNumber == 1) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "开学适应",
                    "开学适应阶段：你对校园还新鲜，探索度额外 +1。",
                    AttributeChange.EMPTY,
                    1);
        }

        // rhythm: social bonus at primary locations (食堂/惟学楼/生活服务点)
        if ("rhythm".equals(stageKey) && stage.primaryLocationIds().contains(locationId)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "节奏建立",
                    "节奏建立阶段：社团活跃，社交收益 +1。",
                    new AttributeChange(0, 0, 0, 1, 0, 0, 0, 0),
                    0);
        }

        // midterm: academic bonus + pressure at primary locations (教学楼/图书馆/实验室)
        if ("midterm".equals(stageKey) && stage.primaryLocationIds().contains(locationId)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "期中波动",
                    "期中波动阶段：考试逼近，学业收益 +1，但压力也更容易上升。",
                    new AttributeChange(1, 0, 0, 0, 0, 1, 0, 0),
                    0);
        }

        // route: skill bonus at primary locations (图书馆/实验室/惟学楼)
        if ("route".equals(stageKey) && stage.primaryLocationIds().contains(locationId)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "路线分化",
                    "路线分化阶段：技能成长进入关键期，技能收益 +1。",
                    new AttributeChange(0, 0, 0, 0, 1, 0, 0, 0),
                    0);
        }

        // project: skill + pressure at primary locations (教学楼/图书馆/实验室)
        if ("project".equals(stageKey) && stage.primaryLocationIds().contains(locationId)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "项目与 DDL",
                    "项目与 DDL 阶段：学习和技能收益 +1，但压力也更容易上升。",
                    new AttributeChange(0, 0, 0, 0, 1, 1, 0, 0),
                    0);
        }

        // final: location-specific exploration bonus (library review / playground physical test)
        if ("final".equals(stageKey) && stage.primaryLocationIds().contains(locationId)) {
            AttributeChange finalWeekChange = weeklyThemeService.finalWeekExplorationAttributeChange(weekNumber, locationId);
            String description = locationId == 2L
                    ? "期末与体测阶段：图书馆复习路线更清楚，学业 +1、技能 +1、压力 -1，探索度额外 +1。"
                    : "期末与体测阶段：操场体测路线更明确，健康 +2、压力 -1，探索度额外 +1。";
            return new ExplorationInfluence(
                    "weekly_theme",
                    "期末与体测",
                    description,
                    finalWeekChange,
                    1);
        }

        return new ExplorationInfluence("weekly_theme", "", "", AttributeChange.EMPTY, 0);
    }
}
