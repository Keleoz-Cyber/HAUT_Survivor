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
        String stageKey = semesterCalendarService.stageForWeek(weekNumber).stageKey();

        if ("opening".equals(stageKey) && weekNumber == 1) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "开学适应",
                    "开学适应阶段：你对校园还新鲜，探索度额外 +1。",
                    AttributeChange.EMPTY,
                    1);
        }
        if ("rhythm".equals(stageKey) && (locationId == 4L || locationId == 7L || locationId == 8L)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "节奏建立",
                    "节奏建立阶段：社团招新活跃，社交收益 +1。",
                    new AttributeChange(0, 0, 0, 1, 0, 0, 0, 0),
                    0);
        }
        if ("project".equals(stageKey) && (locationId == 2L || locationId == 6L || locationId == 1L)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "项目与 DDL",
                    "项目与 DDL 阶段：学习和技能收益 +1，但压力也更容易上升。",
                    new AttributeChange(0, 0, 0, 0, 1, 1, 0, 0),
                    0);
        }
        if ("final".equals(stageKey) && (locationId == 2L || locationId == 5L)) {
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
