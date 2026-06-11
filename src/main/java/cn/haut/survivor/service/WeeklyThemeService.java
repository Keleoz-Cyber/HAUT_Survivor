package cn.haut.survivor.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 周主题系统：4 周 Demo 学期每周有不同主题氛围。
 * 先用静态映射，不建表。
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

    private static final List<WeekTheme> THEMES = List.of(
            new WeekTheme(1, "开学适应周", "刚开学，一切还没失控。", "趁节奏慢，多探索校园、了解组织。", "🎒"),
            new WeekTheme(2, "社团招新周", "校园里到处是摊位和海报。", "社交机会多，加入组织收益更大。", "🎉"),
            new WeekTheme(3, "DDL 高压周", "课设、作业、实验报告开始一起压过来。", "学业和副本事件更频繁，注意减压。", "⏰"),
            new WeekTheme(4, "期末与体测周", "复习、体测、结算都来了。", "图书馆和操场是关键，坚持就是胜利。", "🏁")
    );

    /** 获取指定周的主题，超出范围返回最后一周 */
    public WeekTheme getTheme(int currentWeek) {
        if (currentWeek <= 1) return THEMES.get(0);
        if (currentWeek >= 4) return THEMES.get(3);
        return THEMES.get(currentWeek - 1);
    }

    /** Returns the event type boosted by the current weekly theme. */
    public String preferredEventType(Integer currentWeek) {
        int week = getTheme(currentWeek == null ? 1 : currentWeek).week();
        return switch (week) {
            case 1 -> "生活";
            case 2 -> "社交";
            case 3 -> "学习";
            case 4 -> "健康";
            default -> null;
        };
    }

    /** Small organization activity bonus for recruitment week. */
    public int organizationActivityBonus(Integer currentWeek) {
        return getTheme(currentWeek == null ? 1 : currentWeek).week() == 2 ? 1 : 0;
    }

    /** Opening week lowers the social threshold for joining organizations. */
    public int organizationJoinSocialRequirementReduction(Integer currentWeek) {
        return getTheme(currentWeek == null ? 1 : currentWeek).week() == 1 ? 5 : 0;
    }

    /** Opening week makes first contacts slightly easier, but does not stack with weekly buddy familiarity bonus. */
    public int npcOpeningWeekFamiliarityBonus(Integer currentWeek, boolean weeklyBuddy) {
        if (weeklyBuddy) {
            return 0;
        }
        return getTheme(currentWeek == null ? 1 : currentWeek).week() == 1 ? 1 : 0;
    }

    /** Short suffix shown on NPC interaction result text when the opening week bonus applies. */
    public String openingWeekNpcInteractionSuffix(Integer currentWeek, boolean weeklyBuddy) {
        return npcOpeningWeekFamiliarityBonus(currentWeek, weeklyBuddy) > 0
                ? " 开学适应周：新学期大家都在重新认识彼此，本次熟悉度额外 +1。"
                : "";
    }

    /** Extra pressure applied to dungeon settlements during DDL week. */
    public int dungeonPressureBonus(Integer currentWeek) {
        return getTheme(currentWeek == null ? 1 : currentWeek).week() == 3 ? 1 : 0;
    }

    /** 获取所有周主题 */
    public List<WeekTheme> allThemes() {
        return THEMES;
    }
}
