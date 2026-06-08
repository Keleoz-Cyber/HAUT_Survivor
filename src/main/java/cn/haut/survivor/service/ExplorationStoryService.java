package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.ExplorationStoryProgress;

import java.util.Optional;

public interface ExplorationStoryService {

    Optional<ExplorationStoryResult> maybeTrigger(Long userId, Long locationId, int weekNumber, int exploreLevel);

    Optional<ExplorationStoryResult> triggerSpecificStep(Long userId, String chainKey, int stepNumber, int weekNumber);

    record ExplorationStoryResult(
            ExplorationStoryChain chain,
            ExplorationStoryProgress progress,
            AttributeChange attributeChange,
            String storyText,
            boolean completed
    ) {}
}
