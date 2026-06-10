package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Npc;
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
class NpcStoryServiceTests {

    @Autowired
    private NpcStoryService npcStoryService;

    @Autowired
    private InfluenceLogService influenceLogService;

    @Autowired
    private NpcService npcService;

    @Autowired
    private PlayerService playerService;

    @Test
    void interactionTriggerCreatesStoryProgressAndInfluenceLog() {
        playerService.createProfile(2L, "npc story test", "大二", "计算机类", "就业路线");
        Npc npc = npcService.listActiveNpcs().stream()
                .filter(candidate -> candidate.getId().equals(2L))
                .findFirst()
                .orElseThrow();

        Optional<NpcStoryService.NpcStoryResult> result =
                npcStoryService.advanceOnInteraction(2L, npc, 1);

        assertThat(result).isPresent();
        assertThat(result.get().storyKey()).isEqualTo("linran_review_notes");
        assertThat(result.get().stage()).isEqualTo(1);
        assertThat(result.get().title()).isNotBlank();
        assertThat(result.get().description()).isNotBlank();
        assertThat(result.get().completed()).isFalse();

        List<InfluenceLogService.InfluenceLogEntry> entries = influenceLogService.listWeekInfluences(2L, 1);
        assertThat(entries).extracting(InfluenceLogService.InfluenceLogEntry::sourceType)
                .contains("npc_story");
        assertThat(entries).extracting(InfluenceLogService.InfluenceLogEntry::sourceName)
                .contains(result.get().title());
    }

    @Test
    void completedStoryDoesNotAdvanceRepeatedly() {
        playerService.createProfile(2L, "npc story repeat test", "大二", "计算机类", "就业路线");
        Npc npc = npcService.listActiveNpcs().stream()
                .filter(candidate -> candidate.getId().equals(2L))
                .findFirst()
                .orElseThrow();

        Optional<NpcStoryService.NpcStoryResult> first = npcStoryService.advanceOnInteraction(2L, npc, 1);
        Optional<NpcStoryService.NpcStoryResult> second = npcStoryService.advanceOnInteraction(2L, npc, 2);
        Optional<NpcStoryService.NpcStoryResult> third = npcStoryService.advanceOnInteraction(2L, npc, 3);

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(second.get().completed()).isTrue();
        assertThat(third).isEmpty();
    }
}
