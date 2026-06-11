package cn.haut.survivor.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterCalendarServiceTests {

    private final SemesterCalendarService service = new SemesterCalendarService(16, 4);

    @Test
    void defaultCalendarUsesSixteenWeeks() {
        assertThat(service.semesterWeeks()).isEqualTo(16);
        assertThat(service.weeklyActionPoints()).isEqualTo(4);
        assertThat(service.isSemesterOver(16)).isFalse();
        assertThat(service.isSemesterOver(17)).isTrue();
        assertThat(service.isSemesterOver(null)).isFalse();
    }

    @Test
    void mapsWeeksToSixStages() {
        assertThat(service.stageForWeek(1).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(2).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(3).stageKey()).isEqualTo("rhythm");
        assertThat(service.stageForWeek(5).stageKey()).isEqualTo("rhythm");
        assertThat(service.stageForWeek(6).stageKey()).isEqualTo("midterm");
        assertThat(service.stageForWeek(8).stageKey()).isEqualTo("midterm");
        assertThat(service.stageForWeek(9).stageKey()).isEqualTo("route");
        assertThat(service.stageForWeek(11).stageKey()).isEqualTo("route");
        assertThat(service.stageForWeek(12).stageKey()).isEqualTo("project");
        assertThat(service.stageForWeek(14).stageKey()).isEqualTo("project");
        assertThat(service.stageForWeek(15).stageKey()).isEqualTo("final");
        assertThat(service.stageForWeek(16).stageKey()).isEqualTo("final");
    }

    @Test
    void stageForOutOfRangeWeekClampsToUsefulStage() {
        assertThat(service.stageForWeek(0).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(null).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(99).stageKey()).isEqualTo("final");
    }

    @Test
    void legacySemesterPhaseIsKeptCompatible() {
        assertThat(service.legacySemesterPhaseForWeek(1)).isEqualTo("early");
        assertThat(service.legacySemesterPhaseForWeek(5)).isEqualTo("early");
        assertThat(service.legacySemesterPhaseForWeek(6)).isEqualTo("mid");
        assertThat(service.legacySemesterPhaseForWeek(11)).isEqualTo("mid");
        assertThat(service.legacySemesterPhaseForWeek(12)).isEqualTo("final");
        assertThat(service.legacySemesterPhaseForWeek(16)).isEqualTo("final");
        assertThat(service.legacySemesterPhaseForWeek(17)).isEqualTo("final");
    }

    @Test
    void weekPhaseLabelIncludesTotalWeeksAndStageName() {
        assertThat(service.weekPhaseLabel(1)).isEqualTo("第 1 周 · 开学适应（共 16 周）");
        assertThat(service.weekPhaseLabel(6)).isEqualTo("第 6 周 · 期中波动（共 16 周）");
        assertThat(service.weekPhaseLabel(15)).isEqualTo("第 15 周 · 期末与体测（共 16 周）");
        assertThat(service.weekPhaseLabel(17)).isEqualTo("学期结束");
    }

    @Test
    void preferredEventTypeFollowsSemesterStage() {
        assertThat(service.preferredEventType(1)).isEqualTo("生活");
        assertThat(service.preferredEventType(3)).isEqualTo("社交");
        assertThat(service.preferredEventType(6)).isEqualTo("学习");
        assertThat(service.preferredEventType(9)).isEqualTo("技能");
        assertThat(service.preferredEventType(12)).isEqualTo("学习");
        assertThat(service.preferredEventType(15)).isEqualTo("健康");
    }

    @Test
    void primaryLocationIdsFollowSemesterStage() {
        assertThat(service.stageForWeek(1).primaryLocationIds()).containsExactly(3L, 4L, 7L);
        assertThat(service.stageForWeek(6).primaryLocationIds()).containsExactly(1L, 2L, 6L);
        assertThat(service.stageForWeek(15).primaryLocationIds()).containsExactly(2L, 5L);
    }

    @Test
    void stageGameplayHooksAreCentralized() {
        // opening: NPC familiarity bonus, org join social reduction
        var opening = service.stageForWeek(1);
        assertThat(opening.npcFamiliarityBonus()).isEqualTo(1);
        assertThat(opening.organizationJoinSocialReduction()).isEqualTo(5);
        assertThat(opening.organizationActivityBonus()).isZero();
        assertThat(opening.dungeonPressureBonus()).isZero();
        assertThat(opening.dungeonPressureRelief()).isZero();

        // rhythm: org activity bonus
        var rhythm = service.stageForWeek(3);
        assertThat(rhythm.organizationActivityBonus()).isEqualTo(1);
        assertThat(rhythm.npcFamiliarityBonus()).isZero();
        assertThat(rhythm.dungeonPressureBonus()).isZero();

        // midterm: no special hooks (only event bias)
        var midterm = service.stageForWeek(6);
        assertThat(midterm.organizationActivityBonus()).isZero();
        assertThat(midterm.dungeonPressureBonus()).isZero();
        assertThat(midterm.npcFamiliarityBonus()).isZero();

        // route: no special hooks
        var route = service.stageForWeek(9);
        assertThat(route.organizationActivityBonus()).isZero();
        assertThat(route.dungeonPressureBonus()).isZero();

        // project: dungeon pressure bonus
        var project = service.stageForWeek(12);
        assertThat(project.dungeonPressureBonus()).isEqualTo(1);
        assertThat(project.organizationActivityBonus()).isZero();

        // final: dungeon pressure relief (physical)
        var finalStage = service.stageForWeek(15);
        assertThat(finalStage.dungeonPressureRelief()).isEqualTo(-1);
        assertThat(finalStage.dungeonPressureBonus()).isZero();
    }

    @Test
    void weeksLeftInStageCalculatesCorrectly() {
        assertThat(service.weeksLeftInStage(1)).isEqualTo(2);  // opening: 1-2
        assertThat(service.weeksLeftInStage(2)).isEqualTo(1);
        assertThat(service.weeksLeftInStage(3)).isEqualTo(3);  // rhythm: 3-5
        assertThat(service.weeksLeftInStage(5)).isEqualTo(1);
        assertThat(service.weeksLeftInStage(6)).isEqualTo(3);  // midterm: 6-8
        assertThat(service.weeksLeftInStage(15)).isEqualTo(2); // final: 15-16
        assertThat(service.weeksLeftInStage(16)).isEqualTo(1);
        assertThat(service.weeksLeftInStage(null)).isEqualTo(2);
    }

    @Test
    void nextStageReturnsFollowingStageOrNull() {
        assertThat(service.nextStage(1).stageKey()).isEqualTo("rhythm");
        assertThat(service.nextStage(2).stageKey()).isEqualTo("rhythm");
        assertThat(service.nextStage(5).stageKey()).isEqualTo("midterm");
        assertThat(service.nextStage(11).stageKey()).isEqualTo("project");
        assertThat(service.nextStage(14).stageKey()).isEqualTo("final");
        assertThat(service.nextStage(15)).isNull();
        assertThat(service.nextStage(16)).isNull();
    }

    @Test
    void stageSummaryHintIsNotBlankForAllStages() {
        for (var stage : service.allStages()) {
            assertThat(stage.stageSummaryHint())
                    .as("Stage %s should have a non-blank summary hint", stage.stageKey())
                    .isNotBlank();
        }
    }
}
