package cn.haut.survivor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学期日历服务：统一管理学期总周数、阶段映射、阶段文案、事件偏向和核心地点。
 * 所有学期周次判断必须通过本服务，不允许在其他服务中各自写死周数。
 */
@Service
public class SemesterCalendarService {

    public static final int DEFAULT_SEMESTER_WEEKS = 16;
    public static final int DEFAULT_WEEKLY_ACTION_POINTS = 4;

    public record SemesterStage(
            String stageKey,
            int weekStart,
            int weekEnd,
            String name,
            String description,
            String hint,
            String icon,
            String preferredEventType,
            List<Long> primaryLocationIds
    ) {}

    private final int semesterWeeks;
    private final int weeklyActionPoints;

    private static final List<SemesterStage> STAGES = List.of(
            new SemesterStage("opening", 1, 2, "开学适应",
                    "刚开学，一切还没失控。", "趁节奏慢，多探索校园、了解组织。", "🎒", "生活",
                    List.of(3L, 4L, 7L)),
            new SemesterStage("rhythm", 3, 5, "节奏建立",
                    "课程和社团开始进入稳定节奏。", "适合建立周目标、认识组织和推进校园奇遇。", "🎉", "社交",
                    List.of(4L, 7L, 8L)),
            new SemesterStage("midterm", 6, 8, "期中波动",
                    "复习、实验和阶段作业开始制造压力。", "学业和技能事件更重要，注意别让压力失控。", "📚", "学习",
                    List.of(1L, 2L, 6L)),
            new SemesterStage("route", 9, 11, "路线分化",
                    "你的成长路线开始拉开差异。", "围绕成长路线选择行动，关系和组织会影响后续走向。", "🧭", "技能",
                    List.of(2L, 6L, 7L)),
            new SemesterStage("project", 12, 14, "项目与 DDL",
                    "课设、小组作业和副本挑战集中出现。", "副本和技能判定更关键，别忘了压力管理。", "⏰", "学习",
                    List.of(1L, 2L, 6L)),
            new SemesterStage("final", 15, 16, "期末与体测",
                    "复习、体测、结算都来了。", "图书馆和操场是关键，坚持就是胜利。", "🏁", "健康",
                    List.of(2L, 5L))
    );

    public SemesterCalendarService(
            @Value("${gameplay.semester-weeks:16}") int semesterWeeks,
            @Value("${gameplay.weekly-action-points:4}") int weeklyActionPoints
    ) {
        this.semesterWeeks = semesterWeeks > 0 ? semesterWeeks : DEFAULT_SEMESTER_WEEKS;
        this.weeklyActionPoints = weeklyActionPoints > 0 ? weeklyActionPoints : DEFAULT_WEEKLY_ACTION_POINTS;
    }

    public int semesterWeeks() {
        return semesterWeeks;
    }

    public int weeklyActionPoints() {
        return weeklyActionPoints;
    }

    public boolean isSemesterOver(Integer currentWeek) {
        return currentWeek != null && currentWeek > semesterWeeks;
    }

    public SemesterStage stageForWeek(Integer currentWeek) {
        int week = currentWeek == null ? 1 : currentWeek;
        if (week <= 1) {
            return STAGES.get(0);
        }
        if (week >= semesterWeeks) {
            return STAGES.get(STAGES.size() - 1);
        }
        return STAGES.stream()
                .filter(stage -> week >= stage.weekStart() && week <= stage.weekEnd())
                .findFirst()
                .orElse(STAGES.get(STAGES.size() - 1));
    }

    public String legacySemesterPhaseForWeek(Integer currentWeek) {
        int week = currentWeek == null ? 1 : currentWeek;
        if (week <= 5) {
            return "early";
        }
        if (week <= 11) {
            return "mid";
        }
        return "final";
    }

    public String weekPhaseLabel(Integer currentWeek) {
        if (isSemesterOver(currentWeek)) {
            return "学期结束";
        }
        int week = currentWeek == null ? 1 : currentWeek;
        SemesterStage stage = stageForWeek(week);
        return "第 " + week + " 周 · " + stage.name() + "（共 " + semesterWeeks + " 周）";
    }

    public String preferredEventType(Integer currentWeek) {
        return stageForWeek(currentWeek).preferredEventType();
    }

    public List<SemesterStage> allStages() {
        return STAGES;
    }
}
