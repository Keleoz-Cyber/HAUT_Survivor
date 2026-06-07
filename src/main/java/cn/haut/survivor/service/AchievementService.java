package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.UserAchievement;

import java.util.List;

/**
 * 成就系统：根据条件类型检查并解锁成就，奖励称号。
 */
public interface AchievementService {

    /** 获取所有活跃成就 */
    List<Achievement> listActiveAchievements();

    /** 获取玩家已解锁的成就列表（包含 achievement 关联字段） */
    List<UserAchievement> listUserAchievements(Long userId);

    /** 获取玩家最近解锁的成就（按解锁时间倒序，取前 limit 条） */
    List<UserAchievement> listRecentUnlocked(Long userId, int limit);

    /** 检查并解锁：遍历指定 conditionType 的成就，满足条件则解锁。返回本次新解锁列表。 */
    List<Achievement> unlockIfEligible(Long userId, String conditionType, int currentValue);

    /** 按 achievementKey 解锁单个成就。返回是否新解锁。 */
    boolean unlockAchievement(Long userId, String achievementKey);

    /** 检查玩家是否已解锁指定成就 */
    boolean hasUnlocked(Long userId, String achievementKey);

    /** 全量检查：根据玩家当前状态检查所有条件类型 */
    void checkAllAchievements(Long userId);
}
