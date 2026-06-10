package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.NpcInteraction;

import java.util.List;
import java.util.Optional;

public interface NpcStoryService {

    Optional<NpcStoryResult> advanceOnInteraction(Long userId, Npc npc, int weekNumber);

    List<NpcInteraction> listUnlockedBranchInteractions(Long userId, Long npcId);

    Optional<NpcStoryResult> recordBranchInteraction(Long userId, Npc npc, NpcInteraction interaction, int weekNumber);

    record NpcStoryResult(
            String storyKey,
            Long npcId,
            int stage,
            String title,
            String description,
            AttributeChange attributeChange,
            boolean completed
    ) {}
}
