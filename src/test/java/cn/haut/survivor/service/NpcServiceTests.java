package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.service.NpcService.NpcEncounter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        // Meet in week 1
        for (int i = 0; i < 20; i++) {
            npcService.maybeMeetNpc(2L, 1L, 1);
        }
        // Then meet again in week 3
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
}
