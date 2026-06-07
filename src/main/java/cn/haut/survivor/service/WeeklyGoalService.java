package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.domain.entity.WeeklyGoal;

import java.util.List;

/**
 * 周目标系统：每周从活跃目标中抽取候选，玩家选择一个追踪。
 */
public interface WeeklyGoalService {

    /** 获取所有活跃周目标 */
    List<WeeklyGoal> listActiveGoals();

    /** 为玩家抽取 3 个候选目标（基于 userId+week 哈希确定性抽取） */
    List<WeeklyGoal> pickCandidateGoals(Long userId, int currentWeek);

    /** 玩家选择目标，创建 user_weekly_goal 记录，start_value 取自当前属性 */
    UserWeeklyGoal chooseGoal(Long userId, int currentWeek, Long goalId);

    /** 获取玩家当前周的目标 */
    UserWeeklyGoal getCurrentGoal(Long userId, int currentWeek);

    /** 获取玩家当前周的目标定义（WeeklyGoal 实体） */
    WeeklyGoal getCurrentGoalDefinition(Long userId, int currentWeek);

    /** 更新进度：仅当 goal.goalType 匹配时更新 current_value */
    UserWeeklyGoal updateProgress(Long userId, int currentWeek, String goalType, int delta);

    /** 检查是否完成：current_value >= target_value 时设置 completed=1 */
    boolean checkCompletion(Long userId, int currentWeek);

    /** 领取奖励：设置 claimed=1，发放 exp 和属性奖励 */
    void claimReward(Long userId, int currentWeek);

    /** 检查压力保持目标：如果当前压力 <= 目标值，标记完成 */
    boolean checkPressureKeepGoal(Long userId, int currentWeek);
}
