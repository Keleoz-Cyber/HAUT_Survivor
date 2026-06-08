package cn.haut.survivor.service;

import cn.haut.survivor.mapper.ExplorationStoryProgressMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ExplorationStoryServiceTests {

    @Autowired
    private ExplorationStoryService explorationStoryService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ExplorationStoryProgressMapper progressMapper;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "story test", "大二", "计算机类", "就业路线");
    }

    @Test
    void triggerSpecificChainStepCreatesProgress() {
        var result = explorationStoryService.triggerSpecificStep(2L, "library_seat", 1, 1);

        assertThat(result).isPresent();
        assertThat(result.get().chain().getChainKey()).isEqualTo("library_seat");
        assertThat(result.get().completed()).isFalse();
        assertThat(progressMapper.selectById(result.get().progress().getId()).getCurrentStep()).isEqualTo(2);
    }

    @Test
    void finalStepMarksChainCompleted() {
        explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 1, 1);
        var result = explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 2, 1);

        assertThat(result).isPresent();
        assertThat(result.get().completed()).isTrue();
        assertThat(result.get().progress().getCompleted()).isEqualTo(1);
    }

    @Test
    void completedChainDoesNotTriggerAgain() {
        explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 1, 1);
        explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 2, 1);

        var result = explorationStoryService.maybeTrigger(2L, 4L, 1, 0);

        assertThat(result).isEmpty();
    }
}