package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;

public interface PlayerService {

    PlayerProfile createProfile(Long userId, String playerName, String grade, String majorType, String growthRoute);

    PlayerProfile findProfileByUserId(Long userId);

    PlayerAttribute findAttributeByUserId(Long userId);

    boolean hasProfile(Long userId);

    /** 扣减行动点，返回更新后的 profile。如果没有剩余行动点则抛异常。 */
    PlayerProfile consumeActionPoint(Long userId);

    /** 结束当前周，推进到下一周，恢复行动点，执行周结算。 */
    PlayerProfile advanceWeek(Long userId);

    /** 检查学期是否结束。 */
    boolean isSemesterOver(Long userId);

    /** 获取学期阶段描述。 */
    String getWeekPhaseLabel(PlayerProfile profile);

    /** 重置学期：恢复周次和属性，清理探索度和组织关系，保留结局历史。 */
    void resetSemester(Long userId);
}
