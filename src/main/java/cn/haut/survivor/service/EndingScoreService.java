package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.mapper.UserDungeonRecordMapper;
import cn.haut.survivor.mapper.UserWeeklyGoalMapper;
import cn.haut.survivor.service.RouteTendencyService.RouteTendency;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 结局评分服务：基于 16 周学期经历计算各维度评分和关键证据。
 * 纯 Java 层计算，不新增表，复用现有数据源。
 */
@Service
public class EndingScoreService {

    private final PlayerService playerService;
    private final RouteTendencyService routeTendencyService;
    private final ExplorationService explorationService;
    private final OrganizationService organizationService;
    private final NpcService npcService;
    private final UserDungeonRecordMapper userDungeonRecordMapper;
    private final UserWeeklyGoalMapper userWeeklyGoalMapper;

    public EndingScoreService(PlayerService playerService,
                              RouteTendencyService routeTendencyService,
                              ExplorationService explorationService,
                              OrganizationService organizationService,
                              NpcService npcService,
                              UserDungeonRecordMapper userDungeonRecordMapper,
                              UserWeeklyGoalMapper userWeeklyGoalMapper) {
        this.playerService = playerService;
        this.routeTendencyService = routeTendencyService;
        this.explorationService = explorationService;
        this.organizationService = organizationService;
        this.npcService = npcService;
        this.userDungeonRecordMapper = userDungeonRecordMapper;
        this.userWeeklyGoalMapper = userWeeklyGoalMapper;
    }

    // ==================== Read Model ====================

    public record RouteDimensionScore(
            String dimensionKey,
            String dimensionName,
            int score,
            String label
    ) {}

    public record EndingScoreReport(
            String routeTendencyName,
            String routeTendencyDesc,
            List<RouteDimensionScore> scores,
            List<String> evidence,
            String semesterSummaryText
    ) {}

    // ==================== 核心入口 ====================

    /** 构建结局评分报告 */
    public EndingScoreReport buildScoreReport(Long userId) {
        cn.haut.survivor.domain.entity.PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);

        // 路线倾向
        String chosenRoute = profile != null ? profile.getGrowthRoute() : null;
        RouteTendency tendency = routeTendencyService.deriveTendency(attribute, chosenRoute);

        // 收集数据
        SemesterData data = collectSemesterData(userId);

        // 计算各维度分数
        int academicScore = calcAcademicScore(attribute, data);
        int skillScore = calcSkillScore(attribute, data);
        int socialScore = calcSocialScore(attribute, data);
        int survivalScore = calcSurvivalScore(attribute, data);
        int balancedScore = calcBalancedScore(attribute, data);

        List<RouteDimensionScore> scores = List.of(
                buildDimension("academic", "学业表现", academicScore),
                buildDimension("skill", "技能成长", skillScore),
                buildDimension("social", "社交影响", socialScore),
                buildDimension("survival", "生存能力", survivalScore),
                buildDimension("balanced", "均衡发展", balancedScore)
        );

        // 生成证据
        List<String> evidence = buildEvidence(academicScore, skillScore, socialScore, survivalScore, balancedScore, data);

        // 学期总结文案
        String summaryText = buildSummaryText(tendency, scores, data);

        return new EndingScoreReport(
                tendency.routeName(),
                tendency.description(),
                scores,
                evidence,
                summaryText
        );
    }

    // ==================== 评分规则 ====================

    /** 学业表现：学业 + 自律 + 图书馆探索 */
    private int calcAcademicScore(PlayerAttribute a, SemesterData data) {
        if (a == null) return 0;
        double score = a.getAcademic() * 0.5 + a.getDiscipline() * 0.3 + data.libraryExplore * 0.2;
        return clampScore((int) Math.round(score));
    }

    /** 技能成长：技能 + 实验室探索 + 副本完成 */
    private int calcSkillScore(PlayerAttribute a, SemesterData data) {
        if (a == null) return 0;
        double score = a.getSkill() * 0.5 + data.labExplore * 0.2 + data.completedDungeons * 15;
        return clampScore((int) Math.round(score));
    }

    /** 社交影响：社交 + 组织贡献 + NPC 关系数 */
    private int calcSocialScore(PlayerAttribute a, SemesterData data) {
        if (a == null) return 0;
        double score = a.getSocial() * 0.4 + data.orgContribution * 5 + data.npcRelationCount * 5;
        return clampScore((int) Math.round(score));
    }

    /** 生存能力：健康 + 压力控制 */
    private int calcSurvivalScore(PlayerAttribute a, SemesterData data) {
        if (a == null) return 0;
        double score = a.getHealth() * 0.5 + (100 - a.getPressure()) * 0.5;
        return clampScore((int) Math.round(score));
    }

    /** 均衡发展：各属性差距小 + 周目标完成覆盖度 */
    private int calcBalancedScore(PlayerAttribute a, SemesterData data) {
        if (a == null) return 0;
        int[] values = {a.getAcademic(), a.getHealth(), a.getSocial(), a.getSkill(), a.getDiscipline()};
        int min = 100, max = 0;
        for (int v : values) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        int spreadScore = Math.max(0, 100 - (max - min) * 2);
        int goalBonus = data.completedGoals * 3;
        return clampScore(spreadScore + goalBonus);
    }

    // ==================== 证据生成 ====================

    private List<String> buildEvidence(int academicScore, int skillScore, int socialScore,
                                       int survivalScore, int balancedScore, SemesterData data) {
        List<Map.Entry<String, Integer>> ranked = List.of(
                Map.entry("academic", academicScore),
                Map.entry("skill", skillScore),
                Map.entry("social", socialScore),
                Map.entry("survival", survivalScore),
                Map.entry("balanced", balancedScore)
        ).stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        List<String> evidence = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : ranked) {
            if (evidence.size() >= 4) break;
            String text = evidenceForDimension(entry.getKey(), entry.getValue(), data);
            if (text != null) {
                evidence.add(text);
            }
        }

        if (evidence.isEmpty()) {
            evidence.add("这个学期的记录还比较空白，继续探索校园吧。");
        }

        return evidence;
    }

    private String evidenceForDimension(String key, int score, SemesterData data) {
        return switch (key) {
            case "academic" -> {
                if (score >= 60 && data.libraryExplore >= 40) {
                    yield "📚 图书馆探索度达到 " + data.libraryExplore + "，学业表现优秀。";
                } else if (score >= 60) {
                    yield "📖 学业表现扎实，自律和成绩稳步推进。";
                } else if (score >= 40) {
                    yield "📝 学业表现尚可，但还有提升空间。";
                } else {
                    yield null; // 分数太低不展示
                }
            }
            case "skill" -> {
                if (data.completedDungeons > 0 && score >= 60) {
                    yield "⚔️ 完成了 " + data.completedDungeons + " 个副本，技能成长显著。";
                } else if (score >= 60) {
                    yield "🛠️ 技能成长良好，实践能力稳步提升。";
                } else if (score >= 40) {
                    yield "🔧 技能成长一般，可以多尝试副本和实验室。";
                } else {
                    yield null;
                }
            }
            case "social" -> {
                if (data.npcRelationCount >= 3 && score >= 60) {
                    yield "👥 认识了 " + data.npcRelationCount + " 个熟人，社交网络广泛。";
                } else if (data.orgContribution >= 4 && score >= 50) {
                    yield "🏢 组织贡献达到 " + data.orgContribution + "，社团活动积极。";
                } else if (score >= 50) {
                    yield "🤝 社交影响不错，和校园里的人建立了联系。";
                } else {
                    yield null;
                }
            }
            case "survival" -> {
                if (score >= 70) {
                    yield "💚 压力管理出色，健康状态良好，稳稳地活过了这个学期。";
                } else if (score >= 50) {
                    yield "🩹 压力和健康控制还行，但偶尔会有紧绷的时刻。";
                } else {
                    yield null;
                }
            }
            case "balanced" -> {
                if (score >= 70 && data.completedGoals >= 8) {
                    yield "🌟 完成了 " + data.completedGoals + " 个周目标，全面发展令人印象深刻。";
                } else if (score >= 50) {
                    yield "⚖️ 多个维度保持均衡，没有明显的短板。";
                } else {
                    yield null;
                }
            }
            default -> null;
        };
    }

    // ==================== 学期总结文案 ====================

    private String buildSummaryText(RouteTendency tendency, List<RouteDimensionScore> scores, SemesterData data) {
        // 找最强维度
        RouteDimensionScore top = scores.stream()
                .max(Comparator.comparingInt(RouteDimensionScore::score))
                .orElseGet(() -> new RouteDimensionScore("balanced", "均衡发展", 0, "不足"));

        // 找最弱维度
        RouteDimensionScore bottom = scores.stream()
                .min(Comparator.comparingInt(RouteDimensionScore::score))
                .orElseGet(() -> new RouteDimensionScore("balanced", "均衡发展", 0, "不足"));

        String routeName = tendency.routeName();

        if (top.score() >= 80 && bottom.score() >= 50) {
            return "这一学期，你走出了属于自己的「" + routeName + "」，各方面表现均衡且出色。"
                    + "最强项「" + top.dimensionName() + "」达到" + top.label() + "水平，没有明显短板。";
        }

        if (top.score() >= 60) {
            return "这一学期，你以「" + routeName + "」为方向稳步前进。"
                    + "「" + top.dimensionName() + "」是你的亮点，而「" + bottom.dimensionName()
                    + "」还有提升空间。下学期可以尝试更均衡地分配精力。";
        }

        return "这一学期，你选择了「" + routeName + "」，但各方面的表现还有很大的提升空间。"
                + "不要灰心——每个学期都是一次新的开始。";
    }

    // ==================== 数据收集 ====================

    private SemesterData collectSemesterData(Long userId) {
        // 探索度
        int libraryExplore = explorationService.getExploreLevel(userId, 2L);
        int labExplore = explorationService.getExploreLevel(userId, 6L);

        // 组织贡献
        int orgContribution = organizationService.listUserOrganizations(userId).stream()
                .mapToInt(r -> r.getContribution() == null ? 0 : r.getContribution())
                .sum();

        // NPC 关系数
        int npcRelationCount = npcService.listKnownNpcs(userId).size();

        // 副本完成数
        long completedDungeons = userDungeonRecordMapper.selectCount(
                new LambdaQueryWrapper<UserDungeonRecord>()
                        .eq(UserDungeonRecord::getUserId, userId)
                        .eq(UserDungeonRecord::getStatus, "COMPLETED"));

        // 周目标完成数
        long completedGoals = userWeeklyGoalMapper.selectCount(
                new LambdaQueryWrapper<UserWeeklyGoal>()
                        .eq(UserWeeklyGoal::getUserId, userId)
                        .eq(UserWeeklyGoal::getCompleted, 1));

        return new SemesterData(libraryExplore, labExplore, orgContribution,
                npcRelationCount, (int) completedDungeons, (int) completedGoals);
    }

    private record SemesterData(
            int libraryExplore,
            int labExplore,
            int orgContribution,
            int npcRelationCount,
            int completedDungeons,
            int completedGoals
    ) {}

    // ==================== 工具方法 ====================

    private RouteDimensionScore buildDimension(String key, String name, int score) {
        return new RouteDimensionScore(key, name, score, scoreLabel(score));
    }

    private String scoreLabel(int score) {
        if (score >= 80) return "优秀";
        if (score >= 60) return "良好";
        if (score >= 40) return "一般";
        return "不足";
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
