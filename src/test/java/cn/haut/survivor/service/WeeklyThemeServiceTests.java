package cn.haut.survivor.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyThemeServiceTests {

    private final WeeklyThemeService service = new WeeklyThemeService();

    @Test
    void week1ReturnsFirstTheme() {
        WeeklyThemeService.WeekTheme theme = service.getTheme(1);
        assertThat(theme.name()).isEqualTo("开学适应周");
        assertThat(theme.icon()).isEqualTo("🎒");
    }

    @Test
    void week2ReturnsSecondTheme() {
        WeeklyThemeService.WeekTheme theme = service.getTheme(2);
        assertThat(theme.name()).isEqualTo("社团招新周");
    }

    @Test
    void week3ReturnsThirdTheme() {
        WeeklyThemeService.WeekTheme theme = service.getTheme(3);
        assertThat(theme.name()).isEqualTo("DDL 高压周");
    }

    @Test
    void week4ReturnsFourthTheme() {
        WeeklyThemeService.WeekTheme theme = service.getTheme(4);
        assertThat(theme.name()).isEqualTo("期末与体测周");
    }

    @Test
    void weekBeyondRangeReturnsLastTheme() {
        WeeklyThemeService.WeekTheme theme = service.getTheme(10);
        assertThat(theme.week()).isEqualTo(4);
    }

    @Test
    void weekZeroOrNegativeReturnsFirstTheme() {
        WeeklyThemeService.WeekTheme theme = service.getTheme(0);
        assertThat(theme.week()).isEqualTo(1);
        assertThat(service.getTheme(-1).week()).isEqualTo(1);
    }

    @Test
    void allThemesReturnsFour() {
        assertThat(service.allThemes()).hasSize(4);
    }

    @Test
    void weeklyGameplayHooksAreCentralizedByThemeWeek() {
        assertThat(service.preferredEventType(1)).isEqualTo("生活");
        assertThat(service.preferredEventType(2)).isEqualTo("社交");
        assertThat(service.preferredEventType(3)).isEqualTo("学习");
        assertThat(service.preferredEventType(4)).isEqualTo("健康");
        assertThat(service.preferredEventType(99)).isEqualTo("健康");

        assertThat(service.organizationActivityBonus(1)).isZero();
        assertThat(service.organizationActivityBonus(2)).isEqualTo(1);
        assertThat(service.organizationActivityBonus(3)).isZero();

        assertThat(service.organizationJoinSocialRequirementReduction(1)).isEqualTo(5);
        assertThat(service.organizationJoinSocialRequirementReduction(2)).isZero();
        assertThat(service.organizationJoinSocialRequirementReduction(null)).isEqualTo(5);

        assertThat(service.npcOpeningWeekFamiliarityBonus(1, false)).isEqualTo(1);
        assertThat(service.npcOpeningWeekFamiliarityBonus(1, true)).isZero();
        assertThat(service.npcOpeningWeekFamiliarityBonus(2, false)).isZero();
        assertThat(service.openingWeekNpcInteractionSuffix(1, false)).contains("开学适应周");
        assertThat(service.openingWeekNpcInteractionSuffix(1, true)).isBlank();

        assertThat(service.dungeonPressureBonus(2)).isZero();
        assertThat(service.dungeonPressureBonus(3)).isEqualTo(1);
        assertThat(service.dungeonPressureBonus(4)).isZero();
    }
}
