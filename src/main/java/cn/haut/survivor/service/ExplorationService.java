package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.UserLocationExploration;
import cn.haut.survivor.service.ExplorationStoryService.ExplorationStoryResult;

import java.util.List;
import java.util.Optional;

public interface ExplorationService {

    /** 获取用户在某地点的探索记录，不存在则返回 null */
    UserLocationExploration findExploration(Long userId, Long locationId);

    /** 获取用户所有地点的探索记录 */
    List<UserLocationExploration> listUserExplorations(Long userId);

    /** 执行探索行动：消耗 1 AP，随机增加探索度，返回探索结果描述 */
    ExplorationResult explore(Long userId, Long locationId);

    /** 检查某地点是否已解锁指定探索度阈值 */
    boolean isUnlocked(Long userId, Long locationId, int requiredLevel);

    /** 获取某地点的探索度，未探索返回 0 */
    int getExploreLevel(Long userId, Long locationId);

    /** 探索结果 */
    record ExplorationResult(
            UserLocationExploration exploration,
            String resultType,
            String description,
            int exploreLevelGain,
            int academicChange,
            int healthChange,
            int moneyChange,
            int socialChange,
            int skillChange,
            int pressureChange,
            int disciplineChange,
            List<ExplorationInfluence> influences,
            Optional<ExplorationStoryResult> storyResult
    ) {}
}
