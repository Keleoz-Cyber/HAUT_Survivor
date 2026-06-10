package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.UserAchievement;
import cn.haut.survivor.domain.entity.UserWeekSummary;

import java.util.List;

/**
 * 周总结系统：每周结束时生成总结，包含主题、目标结果、属性变化、NPC 互动。
 */
public interface WeekSummaryService {

    /** 生成周总结（持久化到 user_week_summary） */
    UserWeekSummary generateSummary(Long userId, int weekNumber);

    /** 获取指定周的总结 */
    UserWeekSummary getSummary(Long userId, int weekNumber);

    /** 构建当前周总结视图（即时计算，不持久化） */
    WeekSummaryView buildCurrentWeekSummary(Long userId, int weekNumber);

    /** 周总结视图 */
    record WeekSummaryView(
            int weekNumber,
            String themeName,
            String themeDescription,
            String themeIcon,
            String weeklyGoalName,
            boolean weeklyGoalCompleted,
            boolean weeklyGoalClaimed,
            String weeklyGoalProgressText,
            int knownNpcCount,
            List<String> recentNpcNames,
            List<UserAchievement> recentAchievements,
            int academic,
            int health,
            int money,
            int social,
            int skill,
            int pressure,
            int discipline,
            String summaryText,
            String ratingLabel,
            List<InfluenceLogService.InfluenceLogEntry> impactRecaps
    ) {}
}
