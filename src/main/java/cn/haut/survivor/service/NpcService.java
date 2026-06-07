package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import java.util.List;
import java.util.Optional;

/**
 * NPC/搭子系统 MVP：探索地点后可能遇到 NPC。
 */
public interface NpcService {

    /** 获取所有活跃 NPC */
    List<Npc> listActiveNpcs();

    /** 获取玩家已认识的 NPC 关系列表 */
    List<UserNpcRelation> listKnownNpcs(Long userId);

    /** 探索某地点后，有概率遇到对应 NPC。currentWeek 用于记录遇见周次。返回 empty 表示没遇到 */
    Optional<NpcEncounter> maybeMeetNpc(Long userId, Long locationId, int currentWeek);

    /** 增加熟悉度 */
    void increaseFamiliarity(Long userId, Long npcId, int amount);

    List<NpcInteraction> listAvailableInteractions(Long userId, Long npcId, int weekNumber);

    NpcInteractionResult interact(Long userId, Long npcId, Long interactionId, int weekNumber);

    Optional<UserNpcWeeklyAction> getCurrentBuddy(Long userId, int weekNumber);

    void chooseWeeklyBuddy(Long userId, Long npcId, int weekNumber);

    String getRelationStage(Integer familiarity);

    /** NPC 遇见结果 */
    record NpcEncounter(
            Npc npc,
            UserNpcRelation relation,
            int familiarityGain,
            String encounterText,
            String tendencyHint
    ) {}

    record NpcInteractionResult(
            Npc npc,
            NpcInteraction interaction,
            UserNpcRelation relation,
            AttributeChange attributeChange,
            int familiarityGain,
            String resultText,
            String relationStage
    ) {}
}
