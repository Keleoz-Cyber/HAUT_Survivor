package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 路线倾向推导服务：基于玩家当前属性和选择的成长路线，
 * 推导当前最可能的成长方向。纯 Java 层计算，不新增表。
 */
@Service
public class RouteTendencyService {

    public record RouteTendency(
            String routeKey,
            String routeName,
            String description,
            int score
    ) {}

    private static final List<RouteDefinition> ROUTE_DEFINITIONS = List.of(
            new RouteDefinition("academic", "学业路线", "你的学业和自律正在稳步推进"),
            new RouteDefinition("social", "社交路线", "你的人际关系和社交网络正在扩展"),
            new RouteDefinition("skill", "技能路线", "你的技能和实践能力正在快速提升"),
            new RouteDefinition("survival", "稳定生活路线", "你在压力管理和健康维护上做得不错"),
            new RouteDefinition("balanced", "均衡路线", "你在多个维度上保持均衡发展")
    );

    /** 推导当前最可能的路线倾向 */
    public RouteTendency deriveTendency(PlayerAttribute attribute, String chosenRoute) {
        List<RouteTendency> all = deriveAllTendencies(attribute, chosenRoute);
        String chosenKey = mapChosenRouteToKey(chosenRoute);
        return all.stream()
                .max(Comparator.comparingInt(RouteTendency::score)
                        .thenComparing(t -> t.routeKey().equals(chosenKey) ? 1 : 0))
                .orElseGet(() -> new RouteTendency("balanced", "均衡路线", "均衡发展中", 0));
    }

    /** 获取所有路线倾向分数（用于展示对比） */
    public List<RouteTendency> deriveAllTendencies(PlayerAttribute attribute, String chosenRoute) {
        if (attribute == null) {
            return ROUTE_DEFINITIONS.stream()
                    .map(d -> new RouteTendency(d.routeKey, d.routeName, d.description, 0))
                    .toList();
        }

        List<RouteTendency> tendencies = new ArrayList<>();

        int academicScore = calcAcademicScore(attribute);
        int socialScore = calcSocialScore(attribute);
        int skillScore = calcSkillScore(attribute);
        int survivalScore = calcSurvivalScore(attribute);
        int balancedScore = calcBalancedScore(attribute);

        // chosenRoute 一致性 bonus：如果推导方向与玩家选择一致，+10
        String chosenKey = mapChosenRouteToKey(chosenRoute);
        if ("academic".equals(chosenKey)) academicScore += 10;
        if ("social".equals(chosenKey)) socialScore += 10;
        if ("skill".equals(chosenKey)) skillScore += 10;
        if ("survival".equals(chosenKey)) survivalScore += 10;
        if ("balanced".equals(chosenKey)) balancedScore += 10;

        tendencies.add(new RouteTendency("academic", "学业路线", "你的学业和自律正在稳步推进", clampScore(academicScore)));
        tendencies.add(new RouteTendency("social", "社交路线", "你的人际关系和社交网络正在扩展", clampScore(socialScore)));
        tendencies.add(new RouteTendency("skill", "技能路线", "你的技能和实践能力正在快速提升", clampScore(skillScore)));
        tendencies.add(new RouteTendency("survival", "稳定生活路线", "你在压力管理和健康维护上做得不错", clampScore(survivalScore)));
        tendencies.add(new RouteTendency("balanced", "均衡路线", "你在多个维度上保持均衡发展", clampScore(balancedScore)));

        return tendencies;
    }

    /** 将玩家选择的成长路线映射为内部 key */
    public String mapChosenRouteToKey(String chosenRoute) {
        if (chosenRoute == null) return "balanced";
        return switch (chosenRoute) {
            case "考研路线" -> "academic";
            case "就业路线" -> "skill";
            case "竞赛路线" -> "skill";
            case "六边形路线" -> "balanced";
            case "摆烂求生路线" -> "survival";
            default -> "balanced";
        };
    }

    // ==================== 评分规则 ====================

    /** 学业路线：学业 + 自律高，压力可控 */
    private int calcAcademicScore(PlayerAttribute a) {
        int score = 0;
        if (a.getAcademic() >= 60) score += a.getAcademic();
        else score += a.getAcademic() / 2;
        if (a.getDiscipline() >= 50) score += a.getDiscipline() / 2;
        if (a.getPressure() <= 70) score += 10;
        if (a.getPressure() > 80) score -= 15;
        return score;
    }

    /** 社交路线：社交高，健康好 */
    private int calcSocialScore(PlayerAttribute a) {
        int score = 0;
        if (a.getSocial() >= 50) score += a.getSocial();
        else score += a.getSocial() / 2;
        score += a.getHealth() / 3;
        if (a.getPressure() <= 60) score += 5;
        return score;
    }

    /** 技能路线：技能高，有实践能力 */
    private int calcSkillScore(PlayerAttribute a) {
        int score = 0;
        if (a.getSkill() >= 50) score += a.getSkill();
        else score += a.getSkill() / 2;
        if (a.getAcademic() >= 50) score += a.getAcademic() / 4;
        return score;
    }

    /** 稳定生活路线：健康高，压力低 */
    private int calcSurvivalScore(PlayerAttribute a) {
        int score = 0;
        if (a.getHealth() >= 60) score += a.getHealth();
        else score += a.getHealth() / 2;
        score -= a.getPressure();
        if (a.getPressure() <= 40) score += 15;
        if (a.getPressure() <= 30) score += 10;
        return score;
    }

    /** 均衡路线：各属性差距小 */
    private int calcBalancedScore(PlayerAttribute a) {
        int[] values = {a.getAcademic(), a.getHealth(), a.getSocial(), a.getSkill(), a.getDiscipline()};
        int min = 100, max = 0;
        for (int v : values) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        int spread = max - min;
        // spread 越小越均衡
        return Math.max(0, 100 - spread * 2);
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private record RouteDefinition(String routeKey, String routeName, String description) {}
}
