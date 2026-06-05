package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.SemesterEnding;
import cn.haut.survivor.domain.entity.UserSemesterEnding;
import cn.haut.survivor.mapper.SemesterEndingMapper;
import cn.haut.survivor.mapper.UserSemesterEndingMapper;
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

    public SemesterEndingServiceImpl(
            SemesterEndingMapper semesterEndingMapper,
            UserSemesterEndingMapper userSemesterEndingMapper,
            PlayerService playerService) {
        this.semesterEndingMapper = semesterEndingMapper;
        this.userSemesterEndingMapper = userSemesterEndingMapper;
        this.playerService = playerService;
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

        // 按优先级从高到低匹配结局
        List<SemesterEnding> allEndings = listAllEndings();
        SemesterEnding matched = allEndings.stream()
                .filter(ending -> evaluateCondition(ending.getConditionRule(), vars))
                .max(Comparator.comparingInt(SemesterEnding::getPriority))
                .orElseGet(() -> findFallbackEnding());

        // 保存结算记录
        UserSemesterEnding record = new UserSemesterEnding();
        record.setUserId(userId);
        record.setEndingId(matched.getId());
        record.setGrowthRoute(profile.getGrowthRoute());
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
        return findUserEnding(userId) != null;
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
}
