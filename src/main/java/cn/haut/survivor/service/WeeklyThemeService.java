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

    /** 获取所有周主题 */
    public List<WeekTheme> allThemes() {
        return THEMES;
    }
}
