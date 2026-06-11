package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyThemeServiceTests {

    private final SemesterCalendarService calendarService = new SemesterCalendarService(16, 4);
    private final WeeklyThemeService service = new WeeklyThemeService(calendarService);

    @Test
    void weekThemeFollowsSixteenWeekSemesterStages() {
        assertThat(service.getTheme(1).name()).isEqualTo("开学适应");
        assertThat(service.getTheme(2).name()).isEqualTo("开学适应");
        assertThat(service.getTheme(3).name()).isEqualTo("节奏建立");
        assertThat(service.getTheme(6).name()).isEqualTo("期中波动");
        assertThat(service.getTheme(9).name()).isEqualTo("路线分化");
        assertThat(service.getTheme(12).name()).isEqualTo("项目与 DDL");
        assertThat(service.getTheme(15).name()).isEqualTo("期末与体测");
        assertThat(service.getTheme(99).name()).isEqualTo("期末与体测");
    }

    @Test
    void allThemesReturnsSixStages() {
        assertThat(service.allThemes()).hasSize(6);
        assertThat(service.allThemes()).extracting(WeeklyThemeService.WeekTheme::name)
                .containsExactly("开学适应", "节奏建立", "期中波动", "路线分化", "项目与 DDL", "期末与体测");
    }

    @Test
    void weeklyGameplayHooksAreMappedToV1Stages() {
        assertThat(service.preferredEventType(1)).isEqualTo("生活");
        assertThat(service.preferredEventType(3)).isEqualTo("社交");
        assertThat(service.preferredEventType(6)).isEqualTo("学习");
        assertThat(service.preferredEventType(9)).isEqualTo("技能");
        assertThat(service.preferredEventType(12)).isEqualTo("学习");
        assertThat(service.preferredEventType(15)).isEqualTo("健康");
        assertThat(service.preferredEventType(99)).isEqualTo("健康");

        // rhythm stage (weeks 3-5): organization activity bonus
        assertThat(service.organizationActivityBonus(2)).isZero();
        assertThat(service.organizationActivityBonus(3)).isEqualTo(1);
        assertThat(service.organizationActivityBonus(5)).isEqualTo(1);
        assertThat(service.organizationActivityBonus(6)).isZero();

        // opening stage (weeks 1-2): organization join social requirement reduction
        assertThat(service.organizationJoinSocialRequirementReduction(1)).isEqualTo(5);
        assertThat(service.organizationJoinSocialRequirementReduction(2)).isEqualTo(5);
        assertThat(service.organizationJoinSocialRequirementReduction(3)).isZero();
        assertThat(service.organizationJoinSocialRequirementReduction(null)).isEqualTo(5);

        // opening stage: NPC familiarity bonus
        assertThat(service.npcOpeningWeekFamiliarityBonus(1, false)).isEqualTo(1);
        assertThat(service.npcOpeningWeekFamiliarityBonus(2, false)).isEqualTo(1);
        assertThat(service.npcOpeningWeekFamiliarityBonus(2, true)).isZero();
        assertThat(service.npcOpeningWeekFamiliarityBonus(3, false)).isZero();
        assertThat(service.openingWeekNpcInteractionSuffix(2, false)).contains("开学适应");
        assertThat(service.openingWeekNpcInteractionSuffix(2, true)).isBlank();

        // project stage (weeks 12-14): dungeon pressure bonus
        assertThat(service.dungeonPressureBonus(11)).isZero();
        assertThat(service.dungeonPressureBonus(12)).isEqualTo(1);
        assertThat(service.dungeonPressureBonus(14)).isEqualTo(1);
        assertThat(service.dungeonPressureBonus(15)).isZero();

        // final stage (weeks 15-16): exploration attribute change
        assertThat(service.finalWeekExplorationAttributeChange(15, 2L).academicChange()).isEqualTo(1);
        assertThat(service.finalWeekExplorationAttributeChange(16, 2L).skillChange()).isEqualTo(1);
        assertThat(service.finalWeekExplorationAttributeChange(15, 5L).healthChange()).isEqualTo(2);
        assertThat(service.finalWeekExplorationAttributeChange(16, 5L).pressureChange()).isEqualTo(-1);
        assertThat(service.finalWeekExplorationAttributeChange(14, 2L).hasAnyChange()).isFalse();
        assertThat(service.finalWeekExplorationAttributeChange(15, 8L).hasAnyChange()).isFalse();

        // final stage: physical dungeon pressure relief
        assertThat(service.finalWeekDungeonPressureRelief(15, "physical")).isEqualTo(-1);
        assertThat(service.finalWeekDungeonPressureRelief(16, "physical")).isEqualTo(-1);
        assertThat(service.finalWeekDungeonPressureRelief(15, "academic")).isZero();
        assertThat(service.finalWeekDungeonPressureRelief(14, "physical")).isZero();
        assertThat(service.finalWeekDungeonResultSuffix(15, "physical")).contains("期末与体测");
        assertThat(service.finalWeekDungeonResultSuffix(14, "physical")).isBlank();
    }
}
