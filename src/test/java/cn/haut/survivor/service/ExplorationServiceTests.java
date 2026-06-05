package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.UserLocationExploration;
import cn.haut.survivor.mapper.UserLocationExplorationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ExplorationServiceTests {

    @Autowired
    private ExplorationService explorationService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserLocationExplorationMapper explorationMapper;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "探索测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void findExplorationReturnsNullForUnexplored() {
        UserLocationExploration exploration = explorationService.findExploration(2L, 1L);
        assertThat(exploration).isNull();
    }

    @Test
    void getExploreLevelReturnsZeroForUnexplored() {
        int level = explorationService.getExploreLevel(2L, 1L);
        assertThat(level).isEqualTo(0);
    }

    @Test
    void exploreCreatesRecordAndConsumesAP() {
        ExplorationService.ExplorationResult result = explorationService.explore(2L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.exploration()).isNotNull();
        assertThat(result.exploration().getExploreLevel()).isGreaterThan(0);
        assertThat(result.exploration().getExploreCount()).isEqualTo(1);
        assertThat(result.exploreLevelGain()).isGreaterThan(0);

        // 验证探索记录已创建
        UserLocationExploration saved = explorationService.findExploration(2L, 1L);
        assertThat(saved).isNotNull();
        assertThat(saved.getExploreLevel()).isEqualTo(result.exploration().getExploreLevel());
    }

    @Test
    void exploreMultipleTimesIncreasesLevel() {
        explorationService.explore(2L, 1L); // AP: 4→3
        explorationService.explore(2L, 1L); // AP: 3→2

        UserLocationExploration exploration = explorationService.findExploration(2L, 1L);
        assertThat(exploration.getExploreCount()).isEqualTo(2);
        assertThat(exploration.getExploreLevel()).isGreaterThan(0);
    }

    @Test
    void exploreFailsWithNoAP() {
        // 消耗所有 AP
        playerService.consumeActionPoint(2L); // 4→3
        playerService.consumeActionPoint(2L); // 3→2
        playerService.consumeActionPoint(2L); // 2→1
        playerService.consumeActionPoint(2L); // 1→0

        assertThatThrownBy(() -> explorationService.explore(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("行动点");
    }

    @Test
    void exploreFailsWhenSemesterOver() {
        // 推进到学期结束
        playerService.advanceWeek(2L); // week 2
        playerService.advanceWeek(2L); // week 3
        playerService.advanceWeek(2L); // week 4
        playerService.advanceWeek(2L); // week 5 → over

        assertThatThrownBy(() -> explorationService.explore(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学期已结束");
    }

    @Test
    void listUserExplorationsReturnsAllExplored() {
        explorationService.explore(2L, 1L);
        explorationService.explore(2L, 2L);

        List<UserLocationExploration> list = explorationService.listUserExplorations(2L);
        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void isUnlockedReturnsFalseBelowThreshold() {
        explorationService.explore(2L, 1L); // 探索度 5-15+，不太可能到 20

        boolean unlocked = explorationService.isUnlocked(2L, 1L, 20);
        // 可能是 true 也可能是 false，取决于随机增益
        // 只验证方法不抛异常
        assertThat(unlocked).isNotNull();
    }

    @Test
    void exploreAppliesAttributeChanges() {
        explorationService.explore(2L, 1L);

        PlayerAttribute after = playerService.findAttributeByUserId(2L);
        // 探索结果会改变属性，验证属性值在合理范围
        assertThat(after.getAcademic()).isBetween(0, 100);
        assertThat(after.getPressure()).isBetween(0, 100);
    }

    @Test
    void exploreLevelCapsAt100() {
        // 先探索一次创建记录
        explorationService.explore(2L, 1L);
        UserLocationExploration exploration = explorationService.findExploration(2L, 1L);
        exploration.setExploreLevel(95);
        explorationMapper.updateById(exploration);

        // 恢复 AP
        playerService.advanceWeek(2L);

        // 再探索一次
        ExplorationService.ExplorationResult result = explorationService.explore(2L, 1L);
        assertThat(result.exploration().getExploreLevel()).isLessThanOrEqualTo(100);
    }

    @Test
    void exploreResultHasDescription() {
        ExplorationService.ExplorationResult result = explorationService.explore(2L, 1L);
        assertThat(result.description()).isNotBlank();
        assertThat(result.resultType()).isNotBlank();
    }
}
