package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.SemesterEnding;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserSemesterEnding;
import cn.haut.survivor.mapper.SemesterEndingMapper;
import cn.haut.survivor.mapper.UserDungeonRecordMapper;
import cn.haut.survivor.mapper.UserSemesterEndingMapper;
import cn.haut.survivor.service.DungeonService;
import cn.haut.survivor.service.ExplorationService;
import cn.haut.survivor.service.OrganizationService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.SemesterEndingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class SemesterEndingServiceImpl implements SemesterEndingService {

    private final SemesterEndingMapper semesterEndingMapper;
    private final UserSemesterEndingMapper userSemesterEndingMapper;
    private final PlayerService playerService;
    private final ExplorationService explorationService;
    private final OrganizationService organizationService;
    private final DungeonService dungeonService;
    private final UserDungeonRecordMapper userDungeonRecordMapper;

    public SemesterEndingServiceImpl(
            SemesterEndingMapper semesterEndingMapper,
            UserSemesterEndingMapper userSemesterEndingMapper,
            PlayerService playerService,
            ExplorationService explorationService,
            OrganizationService organizationService,
            DungeonService dungeonService,
            UserDungeonRecordMapper userDungeonRecordMapper) {
        this.semesterEndingMapper = semesterEndingMapper;
        this.userSemesterEndingMapper = userSemesterEndingMapper;
        this.playerService = playerService;
        this.explorationService = explorationService;
        this.organizationService = organizationService;
        this.dungeonService = dungeonService;
        this.userDungeonRecordMapper = userDungeonRecordMapper;
    }

    @Override
    public List<SemesterEnding> listAllEndings() {
        return semesterEndingMapper.selectList(new LambdaQueryWrapper<SemesterEnding>()
                .orderByDesc(SemesterEnding::getPriority));
    }

    @Override
    @Transactional
    public SemesterEnding settleSemester(Long userId) {
        if (!playerService.isSemesterOver(userId)) {
            throw new IllegalArgumentException("学期尚未结束");
        }
        if (hasSettled(userId)) {
            throw new IllegalArgumentException("已经完成学期结算");
        }

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);

        // 计算当前学期号（历史结局数 + 1）
        int semesterNumber = listUserEndingHistory(userId).size() + 1;

        // 构建属性变量表
        Map<String, Integer> vars = Map.of(
                "academic", attribute.getAcademic(),
                "health", attribute.getHealth(),
                "money", attribute.getMoney(),
                "social", attribute.getSocial(),
                "skill", attribute.getSkill(),
                "pressure", attribute.getPressure(),
                "discipline", attribute.getDiscipline()
        );

        // 按优先级从高到低匹配结局（先尝试路线结局）
        SettlementContext ctx = buildSettlementContext(userId);
        String routeEndingName = matchRouteEnding(ctx, vars);

        List<SemesterEnding> allEndings = listAllEndings();
        SemesterEnding matched;

        if (routeEndingName != null) {
            // 路线结局优先匹配
            matched = allEndings.stream()
                    .filter(ending -> ending.getEndingName().equals(routeEndingName))
                    .findFirst()
                    .orElseGet(() -> findFallbackEnding());
        } else {
            // 没有路线结局时，按属性条件匹配
            matched = allEndings.stream()
                    .filter(ending -> evaluateCondition(ending.getConditionRule(), vars))
                    .max(Comparator.comparingInt(SemesterEnding::getPriority))
                    .orElseGet(() -> findFallbackEnding());
        }

        // 保存结算记录
        UserSemesterEnding record = new UserSemesterEnding();
        record.setUserId(userId);
        record.setEndingId(matched.getId());
        record.setGrowthRoute(profile.getGrowthRoute());
        record.setSemesterNumber(semesterNumber);
        record.setAcademic(attribute.getAcademic());
        record.setHealth(attribute.getHealth());
        record.setSocial(attribute.getSocial());
        record.setSkill(attribute.getSkill());
        record.setPressure(attribute.getPressure());
        record.setDiscipline(attribute.getDiscipline());
        record.setCreateTime(LocalDateTime.now());
        userSemesterEndingMapper.insert(record);

        return matched;
    }

    @Override
    public UserSemesterEnding findUserEnding(Long userId) {
        return userSemesterEndingMapper.selectOne(new LambdaQueryWrapper<UserSemesterEnding>()
                .eq(UserSemesterEnding::getUserId, userId)
                .orderByDesc(UserSemesterEnding::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public List<UserSemesterEnding> listUserEndingHistory(Long userId) {
        return userSemesterEndingMapper.selectList(new LambdaQueryWrapper<UserSemesterEnding>()
                .eq(UserSemesterEnding::getUserId, userId)
                .orderByDesc(UserSemesterEnding::getCreateTime));
    }

    @Override
    public boolean hasSettled(Long userId) {
        // 查找当前学期的结算记录
        UserSemesterEnding latest = findUserEnding(userId);
        if (latest == null) return false;
        // 如果 player_profile.semesterNumber 和最新结局的 semesterNumber 一致，说明当前学期已结算
        cn.haut.survivor.domain.entity.PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (profile == null) return false;
        return latest.getSemesterNumber() != null && latest.getSemesterNumber().equals(profile.getSemesterNumber());
    }

    /**
     * 简单的条件表达式求值器。
     * 支持 AND 连接的比较表达式：attr>=value, attr<value, attr<=value, attr>value
     * 示例: "skill>=70 AND academic>=60 AND discipline>=50"
     */
    public boolean evaluateCondition(String conditionRule, Map<String, Integer> vars) {
        if (conditionRule == null || conditionRule.isBlank()) {
            return true;
        }

        String[] clauses = conditionRule.split("\\s+AND\\s+");
        for (String clause : clauses) {
            clause = clause.trim();
            if (!evaluateClause(clause, vars)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateClause(String clause, Map<String, Integer> vars) {
        // >=
        if (clause.contains(">=")) {
            String[] parts = clause.split(">=");
            return compare(parts[0].trim(), parts[1].trim(), vars, Comparison.GTE);
        }
        // <=
        if (clause.contains("<=")) {
            String[] parts = clause.split("<=");
            return compare(parts[0].trim(), parts[1].trim(), vars, Comparison.LTE);
        }
        // >
        if (clause.contains(">")) {
            String[] parts = clause.split(">");
            return compare(parts[0].trim(), parts[1].trim(), vars, Comparison.GT);
        }
        // <
        if (clause.contains("<")) {
            String[] parts = clause.split("<");
            return compare(parts[0].trim(), parts[1].trim(), vars, Comparison.LT);
        }
        return true;
    }

    private boolean compare(String attrName, String valueStr, Map<String, Integer> vars, Comparison op) {
        Integer attrValue = vars.get(attrName);
        if (attrValue == null) return false;
        int threshold;
        try {
            threshold = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return false;
        }
        return switch (op) {
            case GTE -> attrValue >= threshold;
            case LTE -> attrValue <= threshold;
            case GT -> attrValue > threshold;
            case LT -> attrValue < threshold;
        };
    }

    private SemesterEnding findFallbackEnding() {
        // 默认结局：六边形工大学子
        SemesterEnding fallback = semesterEndingMapper.selectOne(new LambdaQueryWrapper<SemesterEnding>()
                .eq(SemesterEnding::getEndingName, "六边形工大学子")
                .last("LIMIT 1"));
        if (fallback != null) return fallback;

        // 如果连六边形都没有，返回第一个
        List<SemesterEnding> all = listAllEndings();
        return all.isEmpty() ? createDefaultEnding() : all.get(all.size() - 1);
    }

    private SemesterEnding createDefaultEnding() {
        SemesterEnding ending = new SemesterEnding();
        ending.setId(0L);
        ending.setEndingName("工大过客");
        ending.setEndingType("默认");
        ending.setDescription("你度过了这个学期，没有什么特别突出的地方，但也没有什么大的遗憾。");
        ending.setThemeColor("#6b7280");
        ending.setIcon("🎓");
        return ending;
    }

    private enum Comparison {
        GTE, LTE, GT, LT
    }

    @Override
    public SettlementContext buildSettlementContext(Long userId) {
        // 探索度：实验室=6, 图书馆=2, 操场=4
        int labExploreLevel = explorationService.getExploreLevel(userId, 6L);
        int libraryExploreLevel = explorationService.getExploreLevel(userId, 2L);
        int playgroundExploreLevel = explorationService.getExploreLevel(userId, 4L);

        // 组织贡献总和
        int orgContribution = organizationService.listUserOrganizations(userId).stream()
                .mapToInt(r -> r.getContribution() == null ? 0 : r.getContribution())
                .sum();

        // 副本完成情况
        boolean dungeon1Completed = false;
        boolean dungeon2Completed = false;
        String dungeon1Evaluation = null;
        String dungeon2Evaluation = null;
        List<UserDungeonRecord> dungeonRecords = userDungeonRecordMapper.selectList(
                new LambdaQueryWrapper<UserDungeonRecord>()
                        .eq(UserDungeonRecord::getUserId, userId)
                        .eq(UserDungeonRecord::getStatus, "COMPLETED"));
        for (UserDungeonRecord dr : dungeonRecords) {
            if (dr.getDungeonId() != null && dr.getDungeonId() == 1L) {
                dungeon1Completed = true;
                dungeon1Evaluation = dr.getFinalEvaluation();
            }
            if (dr.getDungeonId() != null && dr.getDungeonId() == 2L) {
                dungeon2Completed = true;
                dungeon2Evaluation = dr.getFinalEvaluation();
            }
        }

        return new SettlementContext(labExploreLevel, libraryExploreLevel, playgroundExploreLevel,
                orgContribution, dungeon1Completed, dungeon2Completed,
                dungeon1Evaluation, dungeon2Evaluation);
    }

    /**
     * 基于探索/组织/副本上下文的路线结局判断。
     * 使用固定优先级列表，优先匹配更稀缺/更具体的条件。
     * 返回匹配的结局名称，无匹配返回 null。
     */
    private String matchRouteEnding(SettlementContext ctx, Map<String, Integer> vars) {
        // 优先级 1：课设战神 — 需要副本完成+高评价（最稀缺条件）
        if ("课设战神".equals(ctx.dungeon1Evaluation())) {
            return "课设战神";
        }
        // 优先级 2：实验室编外研究员 — 需要实验室探索高+技能高
        if (ctx.labExploreLevel() >= 40 && vars.getOrDefault("skill", 0) >= 55) {
            return "实验室编外研究员";
        }
        // 优先级 3：社团风云人物 — 需要组织贡献高+社交高
        if (ctx.orgContribution() >= 6 && vars.getOrDefault("social", 0) >= 65) {
            return "社团风云人物";
        }
        // 优先级 4：图书馆常驻民 — 需要图书馆探索高+学业高
        if (ctx.libraryExploreLevel() >= 40 && vars.getOrDefault("academic", 0) >= 65) {
            return "图书馆常驻民";
        }
        // 优先级 5：体测幸存者 — 体测副本完成或健康极高
        if (ctx.dungeon2Completed()) {
            return "体测幸存者";
        }
        return null;
    }
}
