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
import cn.haut.survivor.service.WeeklyGoalService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class WeeklyGoalServiceImpl implements WeeklyGoalService {

    private final WeeklyGoalMapper weeklyGoalMapper;
    private final UserWeeklyGoalMapper userWeeklyGoalMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final AchievementService achievementService;

    public WeeklyGoalServiceImpl(
            WeeklyGoalMapper weeklyGoalMapper,
            UserWeeklyGoalMapper userWeeklyGoalMapper,
            PlayerProfileMapper playerProfileMapper,
            PlayerAttributeMapper playerAttributeMapper,
            AchievementService achievementService) {
        this.weeklyGoalMapper = weeklyGoalMapper;
        this.userWeeklyGoalMapper = userWeeklyGoalMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.achievementService = achievementService;
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

        // 基于 userId + week 哈希确定性抽取 3 个
        long seed = userId * 31L + currentWeek;
        List<WeeklyGoal> shuffled = new ArrayList<>(activeGoals);
        Collections.shuffle(shuffled, new java.util.Random(seed));

        return shuffled.subList(0, 3);
    }

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
