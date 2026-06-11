package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.service.NpcService.NpcEncounter;
import cn.haut.survivor.service.NpcService.NpcInteractionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class NpcServiceTests {

    @Autowired
    private NpcService npcService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private NpcStoryService npcStoryService;

    @Autowired
    private InfluenceLogService influenceLogService;

    @Test
    void listActiveNpcsReturnsSeedData() {
        List<Npc> npcs = npcService.listActiveNpcs();
        assertThat(npcs).isNotEmpty();
        assertThat(npcs).allMatch(n -> n.getActive() == 1);
    }

    @Test
    void listKnownNpcsReturnsRelationsWithNpcFieldPopulated() {
        playerService.createProfile(2L, "NPC字段测试", "大二", "计算机类", "就业路线");
        // Force a meeting
        for (int i = 0; i < 20; i++) {
            npcService.maybeMeetNpc(2L, 1L, 2);
        }
        List<UserNpcRelation> known = npcService.listKnownNpcs(2L);
        if (!known.isEmpty()) {
            UserNpcRelation rel = known.get(0);
            assertThat(rel.getNpc()).isNotNull();
            assertThat(rel.getNpc().getNpcName()).isNotBlank();
            assertThat(rel.getNpc().getAvatarIcon()).isNotBlank();
        }
    }

    @Test
    void maybeMeetNpcCanReturnEncounter() {
        playerService.createProfile(2L, "NPC测试玩家", "大二", "计算机类", "就业路线");
        // Try multiple times since it's probabilistic
        boolean met = false;
        for (int i = 0; i < 20; i++) {
            Optional<NpcEncounter> encounter = npcService.maybeMeetNpc(2L, 1L, 3);
            if (encounter.isPresent()) {
                met = true;
                NpcEncounter e = encounter.get();
                assertThat(e.npc()).isNotNull();
                assertThat(e.relation()).isNotNull();
                assertThat(e.familiarityGain()).isPositive();
                assertThat(e.encounterText()).isNotBlank();
                assertThat(e.tendencyHint()).isNotBlank();
                break;
            }
        }
        assertThat(met).describedAs("Should meet an NPC within 20 attempts").isTrue();
    }

    @Test
    void maybeMeetNpcCreatesRelationOnFirstMeeting() {
        playerService.createProfile(2L, "NPC关系测试", "大二", "计算机类", "就业路线");
        // Force a meeting by trying many times
        for (int i = 0; i < 20; i++) {
            npcService.maybeMeetNpc(2L, 1L, 1);
        }
        List<UserNpcRelation> known = npcService.listKnownNpcs(2L);
        assertThat(known).isNotEmpty();
    }

    @Test
    void maybeMeetNpcSetsLastMetWeekToProvidedValue() {
        playerService.createProfile(2L, "周次测试", "大二", "计算机类", "就业路线");
        // Force a meeting in week 3
        for (int i = 0; i < 20; i++) {
            npcService.maybeMeetNpc(2L, 1L, 3);
        }
        List<UserNpcRelation> known = npcService.listKnownNpcs(2L);
        if (!known.isEmpty()) {
            UserNpcRelation rel = known.get(0);
            assertThat(rel.getLastMetWeek()).isEqualTo(3);
        }
    }

    @Test
    void maybeMeetNpcUpdatesLastMetWeekOnRepeatedMeeting() {
        playerService.createProfile(2L, "周次更新测试", "大二", "计算机类", "就业路线");
        Optional<NpcEncounter> firstEncounter = Optional.empty();
        for (int i = 0; i < 100 && firstEncounter.isEmpty(); i++) {
            firstEncounter = npcService.maybeMeetNpc(2L, 3L, 1);
        }
        assertThat(firstEncounter).isPresent();
        Long npcId = firstEncounter.get().npc().getId();

        Optional<NpcEncounter> secondEncounter = Optional.empty();
        for (int i = 0; i < 100 && secondEncounter.isEmpty(); i++) {
            secondEncounter = npcService.maybeMeetNpc(2L, 3L, 3);
        }
        assertThat(secondEncounter).isPresent();

        UserNpcRelation rel = npcService.listKnownNpcs(2L).stream()
                .filter(r -> r.getNpcId().equals(npcId))
                .findFirst()
                .orElseThrow();
        assertThat(rel.getLastMetWeek()).isEqualTo(3);
    }

    @Test
    void increaseFamiliarityWorks() {
        playerService.createProfile(2L, "熟悉度测试", "大二", "计算机类", "就业路线");
        // First meet to create a relation
        for (int i = 0; i < 20; i++) {
            npcService.maybeMeetNpc(2L, 1L, 1);
        }
        List<UserNpcRelation> known = npcService.listKnownNpcs(2L);
        if (!known.isEmpty()) {
            UserNpcRelation rel = known.get(0);
            int before = rel.getFamiliarity();
            npcService.increaseFamiliarity(2L, rel.getNpcId(), 5);
            List<UserNpcRelation> updated = npcService.listKnownNpcs(2L);
            UserNpcRelation after = updated.stream()
                    .filter(r -> r.getNpcId().equals(rel.getNpcId()))
                    .findFirst().orElse(null);
            assertThat(after).isNotNull();
            assertThat(after.getFamiliarity()).isGreaterThanOrEqualTo(before);
        }
    }

    @Test
    void listAvailableInteractionsRespectsFamiliarity() {
        playerService.createProfile(2L, "NPC interaction test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 25);

        List<NpcInteraction> interactions = npcService.listAvailableInteractions(2L, 2L, 1);

        assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
                .contains("linran_notes", "linran_study")
                .doesNotContain("linran_quiz");
    }

    @Test
    void chooseWeeklyBuddyStoresCurrentBuddy() {
        playerService.createProfile(2L, "buddy selection test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 55);

        npcService.chooseWeeklyBuddy(2L, 2L, 1);

        Optional<UserNpcWeeklyAction> buddy = npcService.getCurrentBuddy(2L, 1);
        assertThat(buddy).isPresent();
        assertThat(buddy.get().getNpcId()).isEqualTo(2L);
        assertThat(buddy.get().getBuddySelected()).isEqualTo(1);
    }

    @Test
    void interactingConsumesActionPointAndAppliesActualAttributeChange() {
        playerService.createProfile(2L, "npc active interaction test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 25);
        int beforeAp = playerService.findProfileByUserId(2L).getActionPoints();
        PlayerAttribute before = playerService.findAttributeByUserId(2L);

        NpcInteractionResult result = npcService.interact(2L, 2L, 3004L, 1);

        PlayerAttribute after = playerService.findAttributeByUserId(2L);
        assertThat(playerService.findProfileByUserId(2L).getActionPoints()).isEqualTo(beforeAp - 1);
        assertThat(after.getAcademic()).isEqualTo(before.getAcademic() + result.attributeChange().academicChange());
        assertThat(after.getSocial()).isEqualTo(before.getSocial() + result.attributeChange().socialChange());
        assertThat(result.familiarityGain()).isPositive();
        assertThat(result.relationStage()).isNotBlank();
    }

    @Test
    void sameNpcCannotInteractTwiceInSameWeek() {
        playerService.createProfile(2L, "npc weekly limit test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 25);

        npcService.interact(2L, 2L, 3004L, 1);

        assertThatThrownBy(() -> npcService.interact(2L, 2L, 3005L, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("本周");
    }

    @Test
    void weeklyBuddyAddsSmallInteractionBonus() {
        playerService.createProfile(2L, "npc buddy bonus test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 55);
        npcService.chooseWeeklyBuddy(2L, 2L, 1);

        NpcInteractionResult result = npcService.interact(2L, 2L, 3004L, 1);

        assertThat(result.attributeChange().academicChange()).isEqualTo(5);
        assertThat(result.familiarityGain()).isEqualTo(5);
        assertThat(result.resultText()).doesNotContain("开学适应周");
    }

    @Test
    void openingWeekAddsSmallNpcInteractionFamiliarityWhenNotWeeklyBuddy() {
        playerService.createProfile(2L, "opening week npc test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 6101L, 25);

        NpcInteractionResult result = npcService.interact(2L, 6101L, 610003L, 1);

        assertThat(result.familiarityGain()).isEqualTo(5);
        assertThat(result.resultText()).contains("开学适应");
    }

    @Test
    void storyProgressUnlocksExclusiveBranchInteraction() {
        playerService.createProfile(2L, "npc branch unlock test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 25);

        assertThat(npcService.listAvailableInteractions(2L, 2L, 1))
                .extracting(NpcInteraction::getInteractionKey)
                .doesNotContain("linran_key_week_review");

        Npc linran = npcService.listActiveNpcs().stream()
                .filter(candidate -> candidate.getId().equals(2L))
                .findFirst()
                .orElseThrow();
        npcStoryService.advanceOnInteraction(2L, linran, 1);

        assertThat(npcService.listAvailableInteractions(2L, 2L, 2))
                .extracting(NpcInteraction::getInteractionKey)
                .contains("linran_key_week_review");
    }

    @Test
    void branchInteractionAppliesChangesAndRecordsInfluenceLog() {
        playerService.createProfile(2L, "npc branch effect test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 25);
        Npc linran = npcService.listActiveNpcs().stream()
                .filter(candidate -> candidate.getId().equals(2L))
                .findFirst()
                .orElseThrow();
        npcStoryService.advanceOnInteraction(2L, linran, 1);
        PlayerAttribute before = playerService.findAttributeByUserId(2L);

        NpcInteractionResult result = npcService.interact(2L, 2L, 900201L, 2);

        PlayerAttribute after = playerService.findAttributeByUserId(2L);
        assertThat(result.interaction().getInteractionKey()).isEqualTo("linran_key_week_review");
        assertThat(result.attributeChange().academicChange()).isEqualTo(3);
        assertThat(after.getAcademic()).isEqualTo(before.getAcademic() + 3);
        assertThat(result.storyResult()).isNotNull();
        assertThat(influenceLogService.listWeekInfluences(2L, 2))
                .extracting(InfluenceLogService.InfluenceLogEntry::sourceType)
                .contains("npc_branch");
    }

    @Test
    void relationshipSummaryUsesCp5StageBoundaries() {
        assertThat(npcService.getRelationSummary(0).stageKey()).isEqualTo("acquaintance");
        assertThat(npcService.getRelationSummary(24).stageKey()).isEqualTo("acquaintance");
        assertThat(npcService.getRelationSummary(25).stageKey()).isEqualTo("familiar");
        assertThat(npcService.getRelationSummary(49).stageKey()).isEqualTo("familiar");
        assertThat(npcService.getRelationSummary(50).stageKey()).isEqualTo("buddy");
        assertThat(npcService.getRelationSummary(79).stageKey()).isEqualTo("buddy");
        assertThat(npcService.getRelationSummary(80).stageKey()).isEqualTo("close");

        NpcService.RelationSummary close = npcService.getRelationSummary(80);
        assertThat(close.label()).isNotBlank();
        assertThat(close.description()).isNotBlank();
        assertThat(close.nextStageHint()).isNotBlank();
        assertThat(close.nextStageAt()).isNull();
        assertThat(close.progressPercent()).isEqualTo(100);
    }
}
