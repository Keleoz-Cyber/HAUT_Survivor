package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.mapper.UserWeeklyGoalMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.RouteTendencyService;
import cn.haut.survivor.service.SemesterCalendarService;
import cn.haut.survivor.service.WeeklyGoalService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class WeeklyGoalServiceImpl implements WeeklyGoalService {

    private final WeeklyGoalMapper weeklyGoalMapper;
    private final UserWeeklyGoalMapper userWeeklyGoalMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final AchievementService achievementService;
    private final SemesterCalendarService semesterCalendarService;
    private final RouteTendencyService routeTendencyService;

    public WeeklyGoalServiceImpl(
            WeeklyGoalMapper weeklyGoalMapper,
            UserWeeklyGoalMapper userWeeklyGoalMapper,
            PlayerProfileMapper playerProfileMapper,
            PlayerAttributeMapper playerAttributeMapper,
            AchievementService achievementService,
            SemesterCalendarService semesterCalendarService,
            RouteTendencyService routeTendencyService) {
        this.weeklyGoalMapper = weeklyGoalMapper;
        this.userWeeklyGoalMapper = userWeeklyGoalMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.achievementService = achievementService;
        this.semesterCalendarService = semesterCalendarService;
        this.routeTendencyService = routeTendencyService;
    }

    @Override
    public List<WeeklyGoal> listActiveGoals() {
        return weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .eq(WeeklyGoal::getActive, 1));
    }

    @Override
    public List<WeeklyGoal> pickCandidateGoals(Long userId, int currentWeek) {
        List<WeeklyGoal> activeGoals = listActiveGoals();
        if (activeGoals.isEmpty()) {
            return Collections.emptyList();
        }
        if (activeGoals.size() <= 3) {
            return activeGoals;
        }

        // 阶段和路线加权选择
        String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
        String routeKey = deriveRouteKey(userId);

        // 为每个目标计算权重
        long seed = userId * 31L + currentWeek;
        java.util.Random rng = new java.util.Random(seed);

        List<WeightedGoal> weighted = activeGoals.stream()
                .map(goal -> new WeightedGoal(goal, goalWeight(goal, stageKey, routeKey, rng)))
                .sorted(Comparator.comparingInt(WeightedGoal::weight).reversed())
                .toList();

        // 取权重最高的 3 个
        return weighted.subList(0, Math.min(3, weighted.size())).stream()
                .map(WeightedGoal::goal)
                .toList();
    }

    /** 从玩家属性和选择的路线推导路线 key */
    private String deriveRouteKey(Long userId) {
        PlayerProfile profile = playerProfileMapper.selectOne(new LambdaQueryWrapper<PlayerProfile>()
                .eq(PlayerProfile::getUserId, userId)
                .last("LIMIT 1"));
        String chosenRoute = profile != null ? profile.getGrowthRoute() : null;

        PlayerAttribute attr = playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
                .eq(PlayerAttribute::getUserId, userId)
                .last("LIMIT 1"));

        return routeTendencyService.deriveTendency(attr, chosenRoute).routeKey();
    }

    /** 计算目标在当前阶段和路线下的权重 */
    private int goalWeight(WeeklyGoal goal, String stageKey, String routeKey, java.util.Random rng) {
        int weight = 1; // 基础权重，保证所有目标都可能被选中
        String goalType = goal.getGoalType();

        // 阶段加权：按阶段偏好给目标类型加分
        weight += stageWeight(goalType, stageKey);

        // 路线加权：按路线倾向给目标类型加分
        weight += routeWeight(goalType, routeKey);

        // 随机扰动（±1），避免完全确定性导致每周候选固定
        weight += rng.nextInt(3) - 1;

        return Math.max(1, weight);
    }

    /** 阶段权重：按当前阶段给匹配的目标类型加分 */
    private int stageWeight(String goalType, String stageKey) {
        return switch (stageKey) {
            case "opening" -> matchAny(goalType, Set.of("explore_count", "npc_meet", "buddy_selected", "exploration")) ? 3 : 0;
            case "rhythm" -> matchAny(goalType, Set.of("org_activity", "npc_interaction", "familiarity_gain")) ? 3 : 0;
            case "midterm" -> matchAny(goalType, Set.of("academic_event", "pressure_keep", "exploration_story_step")) ? 3 : 0;
            case "route" -> matchAny(goalType, Set.of("org_activity", "npc_interaction", "dungeon_stage")) ? 3 : 0;
            case "project" -> matchAny(goalType, Set.of("dungeon_stage", "buddy_assist", "weekly_modifier_used")) ? 3 : 0;
            case "final" -> matchAny(goalType, Set.of("pressure_keep", "exploration")) ? 3 : 0;
            default -> 0;
        };
    }

    /** 路线权重：按当前路线倾向给匹配的目标类型加分 */
    private int routeWeight(String goalType, String routeKey) {
        return switch (routeKey) {
            case "academic" -> matchAny(goalType, Set.of("academic_event", "exploration_story_step")) ? 2 : 0;
            case "social" -> matchAny(goalType, Set.of("npc_meet", "npc_interaction", "buddy_selected", "familiarity_gain")) ? 2 : 0;
            case "skill" -> matchAny(goalType, Set.of("dungeon_stage", "org_activity")) ? 2 : 0;
            case "survival" -> matchAny(goalType, Set.of("pressure_keep", "health")) ? 2 : 0;
            case "balanced" -> 1; // 均衡路线所有目标 +1
            default -> 0;
        };
    }

    private boolean matchAny(String value, Set<String> candidates) {
        return candidates.contains(value);
    }

    private record WeightedGoal(WeeklyGoal goal, int weight) {}

    @Override
    @Transactional
    public UserWeeklyGoal chooseGoal(Long userId, int currentWeek, Long goalId) {
        // 检查是否已有本周目标
        UserWeeklyGoal existing = getCurrentGoal(userId, currentWeek);
        if (existing != null) {
            throw new IllegalArgumentException("本周已选择目标");
        }

        WeeklyGoal goal = weeklyGoalMapper.selectById(goalId);
        if (goal == null || goal.getActive() != 1) {
            throw new IllegalArgumentException("目标不存在或已失效");
        }

        // 获取 start_value
        int startValue = getStartValueForGoalType(userId, goal.getGoalType());

        UserWeeklyGoal userGoal = new UserWeeklyGoal();
        userGoal.setUserId(userId);
        userGoal.setWeekNumber(currentWeek);
        userGoal.setGoalId(goalId);
        userGoal.setStartValue(startValue);
        userGoal.setCurrentValue(startValue);
        userGoal.setCompleted(0);
        userGoal.setClaimed(0);
        userGoal.setCreatedAt(LocalDateTime.now());

        userWeeklyGoalMapper.insert(userGoal);
        return userGoal;
    }

    @Override
    public UserWeeklyGoal getCurrentGoal(Long userId, int currentWeek) {
        return userWeeklyGoalMapper.selectOne(new LambdaQueryWrapper<UserWeeklyGoal>()
                .eq(UserWeeklyGoal::getUserId, userId)
                .eq(UserWeeklyGoal::getWeekNumber, currentWeek)
                .last("LIMIT 1"));
    }

    @Override
    public WeeklyGoal getCurrentGoalDefinition(Long userId, int currentWeek) {
        UserWeeklyGoal userGoal = getCurrentGoal(userId, currentWeek);
        if (userGoal == null) {
            return null;
        }
        return weeklyGoalMapper.selectById(userGoal.getGoalId());
    }

    @Override
    @Transactional
    public UserWeeklyGoal updateProgress(Long userId, int currentWeek, String goalType, int delta) {
        UserWeeklyGoal userGoal = getCurrentGoal(userId, currentWeek);
        if (userGoal == null) {
            return null;
        }

        WeeklyGoal goal = weeklyGoalMapper.selectById(userGoal.getGoalId());
        if (goal == null || !goal.getGoalType().equals(goalType)) {
            // goalType 不匹配，不更新
            return userGoal;
        }

        userGoal.setCurrentValue(userGoal.getCurrentValue() + delta);
        userWeeklyGoalMapper.updateById(userGoal);

        // 自动检查完成
        checkCompletion(userId, currentWeek);

        return userGoal;
    }

    @Override
    @Transactional
    public boolean checkCompletion(Long userId, int currentWeek) {
        UserWeeklyGoal userGoal = getCurrentGoal(userId, currentWeek);
        if (userGoal == null || userGoal.getCompleted() == 1) {
            return userGoal != null && userGoal.getCompleted() == 1;
        }

        WeeklyGoal goal = weeklyGoalMapper.selectById(userGoal.getGoalId());
        if (goal == null) {
            return false;
        }

        if (userGoal.getCurrentValue() >= goal.getTargetValue()) {
            userGoal.setCompleted(1);
            userWeeklyGoalMapper.updateById(userGoal);

            // 成就：完成本周目标
            achievementService.unlockAchievement(userId, "goal_finisher");
            // 成就：压力保持目标完成
            if ("pressure_keep".equals(goal.getGoalType())) {
                achievementService.unlockIfEligible(userId, "pressure_keep", 1);
            }

            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void claimReward(Long userId, int currentWeek) {
        UserWeeklyGoal userGoal = getCurrentGoal(userId, currentWeek);
        if (userGoal == null) {
            throw new IllegalArgumentException("本周没有目标");
        }
        if (userGoal.getCompleted() == 0) {
            throw new IllegalArgumentException("目标尚未完成");
        }
        if (userGoal.getClaimed() == 1) {
            throw new IllegalArgumentException("奖励已领取");
        }

        WeeklyGoal goal = weeklyGoalMapper.selectById(userGoal.getGoalId());
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }

        // 发放奖励
        PlayerProfile profile = playerProfileMapper.selectOne(new LambdaQueryWrapper<PlayerProfile>()
                .eq(PlayerProfile::getUserId, userId)
                .last("LIMIT 1"));

        if (profile != null && goal.getRewardExp() != null && goal.getRewardExp() > 0) {
            profile.setExp(profile.getExp() + goal.getRewardExp());
            playerProfileMapper.updateById(profile);
        }

        // 属性奖励
        if (goal.getRewardAttribute() != null && goal.getRewardAmount() != null && goal.getRewardAmount() > 0) {
            PlayerAttribute attribute = playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
                    .eq(PlayerAttribute::getUserId, userId)
                    .last("LIMIT 1"));

            if (attribute != null) {
                applyAttributeReward(attribute, goal.getRewardAttribute(), goal.getRewardAmount());
                attribute.setUpdateTime(LocalDateTime.now());
                playerAttributeMapper.updateById(attribute);
            }
        }

        userGoal.setClaimed(1);
        userWeeklyGoalMapper.updateById(userGoal);
    }

    @Override
    @Transactional
    public boolean checkPressureKeepGoal(Long userId, int currentWeek) {
        UserWeeklyGoal userGoal = getCurrentGoal(userId, currentWeek);
        if (userGoal == null || userGoal.getCompleted() == 1) {
            return userGoal != null && userGoal.getCompleted() == 1;
        }

        WeeklyGoal goal = weeklyGoalMapper.selectById(userGoal.getGoalId());
        if (goal == null || !"pressure_keep".equals(goal.getGoalType())) {
            return false;
        }

        // 检查当前压力值
        PlayerAttribute attribute = playerAttributeMapper.selectOne(
                new LambdaQueryWrapper<PlayerAttribute>()
                        .eq(PlayerAttribute::getUserId, userId)
                        .last("LIMIT 1"));

        if (attribute == null) {
            return false;
        }

        // 更新 current_value 为当前压力值
        userGoal.setCurrentValue(attribute.getPressure());
        userWeeklyGoalMapper.updateById(userGoal);

        // 压力 <= 目标值（target_value 代表压力上限）则完成
        if (attribute.getPressure() <= goal.getTargetValue()) {
            userGoal.setCompleted(1);
            userWeeklyGoalMapper.updateById(userGoal);

            // 成就：完成本周目标 + 压力保持目标
            achievementService.unlockAchievement(userId, "goal_finisher");
            achievementService.unlockIfEligible(userId, "pressure_keep", 1);

            return true;
        }

        return false;
    }

    private int getStartValueForGoalType(Long userId, String goalType) {
        if (goalType == null) {
            return 0;
        }

        // For count-based goals, start from 0 (count actions this week)
        switch (goalType) {
            case "explore_count" -> {
                return 0; // 探索次数从 0 开始计数
            }
            case "npc_meet" -> {
                return 0; // NPC 遇见次数从 0 开始计数
            }
            case "org_activity" -> {
                return 0; // 组织活动次数从 0 开始计数
            }
            case "dungeon_stage" -> {
                return 0; // 副本阶段完成次数从 0 开始计数
            }
            case "academic_event" -> {
                return 0; // 学业危机事件次数从 0 开始计数
            }
            case "npc_interaction", "buddy_selected", "familiarity_gain",
                 "rumor_effect_used", "exploration_story_step", "weekly_modifier_used", "buddy_assist" -> {
                return 0;
            }
            case "pressure_keep" -> {
                // 压力保持目标：记录当前压力值
                PlayerAttribute attribute = playerAttributeMapper.selectOne(
                        new LambdaQueryWrapper<PlayerAttribute>()
                                .eq(PlayerAttribute::getUserId, userId)
                                .last("LIMIT 1"));
                return attribute != null ? attribute.getPressure() : 0;
            }
            default -> {
                // 兼容旧的属性类目标
                PlayerAttribute attribute = playerAttributeMapper.selectOne(
                        new LambdaQueryWrapper<PlayerAttribute>()
                                .eq(PlayerAttribute::getUserId, userId)
                                .last("LIMIT 1"));
                if (attribute == null) {
                    return 0;
                }
                return switch (goalType) {
                    case "academic" -> attribute.getAcademic();
                    case "health" -> attribute.getHealth();
                    case "social" -> attribute.getSocial();
                    case "skill" -> attribute.getSkill();
                    case "discipline" -> attribute.getDiscipline();
                    default -> 0;
                };
            }
        }
    }

    private void applyAttributeReward(PlayerAttribute attribute, String rewardAttribute, int amount) {
        switch (rewardAttribute) {
            case "academic" -> attribute.setAcademic(clamp(attribute.getAcademic() + amount));
            case "health" -> attribute.setHealth(clamp(attribute.getHealth() + amount));
            case "money" -> attribute.setMoney(clamp(attribute.getMoney() + amount));
            case "social" -> attribute.setSocial(clamp(attribute.getSocial() + amount));
            case "skill" -> attribute.setSkill(clamp(attribute.getSkill() + amount));
            case "pressure" -> attribute.setPressure(clamp(attribute.getPressure() - amount)); // 减压
            case "discipline" -> attribute.setDiscipline(clamp(attribute.getDiscipline() + amount));
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
