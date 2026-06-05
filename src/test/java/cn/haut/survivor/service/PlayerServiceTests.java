package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class PlayerServiceTests {

    @Autowired
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void creatingProfileInitializesDefaultAttributes() {
        PlayerProfile profile = playerService.findProfileByUserId(2L);
        PlayerAttribute attribute = playerService.findAttributeByUserId(2L);

        assertThat(profile.getPlayerName()).isEqualTo("测试玩家");
        assertThat(profile.getLevel()).isEqualTo(1);
        assertThat(profile.getExp()).isZero();
        assertThat(profile.getCurrentWeek()).isEqualTo(1);
        assertThat(profile.getActionPoints()).isEqualTo(4);
        assertThat(profile.getMaxActionPoints()).isEqualTo(4);
        assertThat(profile.getSemesterPhase()).isEqualTo("early");
        assertThat(attribute.getSkill()).isEqualTo(50);
        assertThat(attribute.getSocial()).isEqualTo(55);
    }

    @Test
    void growthRouteAdjustsAttributes() {
        PlayerAttribute attribute = playerService.findAttributeByUserId(2L);

        assertThat(attribute.getAcademic()).isEqualTo(60);
        assertThat(attribute.getSkill()).isEqualTo(50);
        assertThat(attribute.getSocial()).isEqualTo(55);
    }

    @Test
    void userWithProfileCanBeDetected() {
        assertThat(playerService.hasProfile(2L)).isTrue();
    }

    // ==================== 行动点测试 ====================

    @Test
    void consumeActionPointDecrementsPoints() {
        playerService.consumeActionPoint(2L);
        PlayerProfile profile = playerService.findProfileByUserId(2L);

        assertThat(profile.getActionPoints()).isEqualTo(3);
    }

    @Test
    void consumeAllActionPointsReachesZero() {
        for (int i = 0; i < 4; i++) {
            playerService.consumeActionPoint(2L);
        }
        PlayerProfile profile = playerService.findProfileByUserId(2L);

        assertThat(profile.getActionPoints()).isEqualTo(0);
    }

    @Test
    void consumeActionPointWhenEmptyThrows() {
        for (int i = 0; i < 4; i++) {
            playerService.consumeActionPoint(2L);
        }
        assertThatThrownBy(() -> playerService.consumeActionPoint(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("行动点已用完");
    }

    // ==================== 周推进测试 ====================

    @Test
    void advanceWeekResetsActionPoints() {
        playerService.consumeActionPoint(2L);
        playerService.consumeActionPoint(2L);
        playerService.advanceWeek(2L);

        PlayerProfile profile = playerService.findProfileByUserId(2L);
        assertThat(profile.getCurrentWeek()).isEqualTo(2);
        assertThat(profile.getActionPoints()).isEqualTo(4);
        assertThat(profile.getSemesterPhase()).isEqualTo("early");
    }

    @Test
    void advanceWeekUpdatesSemesterPhase() {
        playerService.advanceWeek(2L); // week 2 → early
        playerService.advanceWeek(2L); // week 3 → mid

        PlayerProfile profile = playerService.findProfileByUserId(2L);
        assertThat(profile.getCurrentWeek()).isEqualTo(3);
        assertThat(profile.getSemesterPhase()).isEqualTo("mid");
    }

    @Test
    void advanceToFinalPhase() {
        playerService.advanceWeek(2L); // week 2
        playerService.advanceWeek(2L); // week 3
        playerService.advanceWeek(2L); // week 4

        PlayerProfile profile = playerService.findProfileByUserId(2L);
        assertThat(profile.getCurrentWeek()).isEqualTo(4);
        assertThat(profile.getSemesterPhase()).isEqualTo("final");
    }

    @Test
    void semesterEndsAfterFourWeeks() {
        playerService.advanceWeek(2L); // week 2
        playerService.advanceWeek(2L); // week 3
        playerService.advanceWeek(2L); // week 4
        playerService.advanceWeek(2L); // week 5 → over

        assertThat(playerService.isSemesterOver(2L)).isTrue();
        assertThat(playerService.getWeekPhaseLabel(playerService.findProfileByUserId(2L))).contains("学期结束");
    }

    @Test
    void cannotAdvanceAfterSemesterEnds() {
        for (int i = 0; i < 4; i++) {
            playerService.advanceWeek(2L);
        }
        assertThatThrownBy(() -> playerService.advanceWeek(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学期已结束");
    }

    @Test
    void cannotActAfterSemesterEnds() {
        for (int i = 0; i < 4; i++) {
            playerService.advanceWeek(2L);
        }
        assertThatThrownBy(() -> playerService.consumeActionPoint(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学期已结束");
    }

    @Test
    void advanceWeekReducesPressureNaturally() {
        PlayerAttribute before = playerService.findAttributeByUserId(2L);
        int pressureBefore = before.getPressure();

        playerService.advanceWeek(2L);

        PlayerAttribute after = playerService.findAttributeByUserId(2L);
        assertThat(after.getPressure()).isEqualTo(Math.max(0, pressureBefore - 5));
    }

    @Test
    void weekPhaseLabelShowsCorrectPhase() {
        PlayerProfile profile = playerService.findProfileByUserId(2L);
        assertThat(playerService.getWeekPhaseLabel(profile)).contains("第 1 周").contains("开学适应期");
    }
}
