package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import org.springframework.stereotype.Service;

@Service
public class WeeklyModifierService {

    private final WeeklyThemeService weeklyThemeService;

    public WeeklyModifierService(WeeklyThemeService weeklyThemeService) {
        this.weeklyThemeService = weeklyThemeService;
    }

    public ExplorationInfluence getExplorationInfluence(int weekNumber, Long locationId) {
        if (weekNumber == 1) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "开学适应周",
                    "开学适应周：你对校园还新鲜，探索度额外 +1。",
                    AttributeChange.EMPTY,
                    1);
        }
        if (weekNumber == 2 && (locationId == 4L || locationId == 7L || locationId == 8L)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "社团招新周",
                    "社团招新周：人多消息多，社交收益 +1。",
                    new AttributeChange(0, 0, 0, 1, 0, 0, 0, 0),
                    0);
        }
        if (weekNumber == 3 && (locationId == 2L || locationId == 6L || locationId == 1L)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "DDL 高压周",
                    "DDL 高压周：学习和技能收益 +1，但压力也更容易上升。",
                    new AttributeChange(0, 0, 0, 0, 1, 1, 0, 0),
                    0);
        }
        if (weekNumber == 4 && (locationId == 2L || locationId == 5L)) {
            AttributeChange finalWeekChange = weeklyThemeService.finalWeekExplorationAttributeChange(weekNumber, locationId);
            String description = locationId == 2L
                    ? "期末与体测周：图书馆复习路线更清楚，学业 +1、技能 +1、压力 -1，探索度额外 +1。"
                    : "期末与体测周：操场体测路线更明确，健康 +2、压力 -1，探索度额外 +1。";
            return new ExplorationInfluence(
                    "weekly_theme",
                    "期末与体测周",
                    description,
                    finalWeekChange,
                    1);
        }
        return new ExplorationInfluence("weekly_theme", "", "", AttributeChange.EMPTY, 0);
    }
}