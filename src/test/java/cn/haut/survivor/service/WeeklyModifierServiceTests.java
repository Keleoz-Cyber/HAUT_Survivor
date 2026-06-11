package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class WeeklyModifierServiceTests {

    @Autowired
    private WeeklyModifierService weeklyModifierService;

    @Test
    void weekOneAddsSmallExploreBonus() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(1, 2L);

        assertThat(influence.sourceType()).isEqualTo("weekly_theme");
        assertThat(influence.exploreBonus()).isEqualTo(1);
        assertThat(influence.description()).contains("开学适应");
    }

    @Test
    void weekTwelveLabAddsSkillAndPressure() {
        // week 12-14 = project stage, primary locations: 1(教学楼), 2(图书馆), 6(实验室)
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(12, 6L);

        assertThat(influence.attributeChange().skillChange()).isEqualTo(1);
        assertThat(influence.attributeChange().pressureChange()).isEqualTo(1);
        assertThat(influence.description()).contains("DDL");
    }

    @Test
    void midtermStageProvidesAcademicAndPressureAtPrimaryLocations() {
        // week 6-8 = midterm, primary locations: 1(教学楼), 2(图书馆), 6(实验室)
        ExplorationInfluence library = weeklyModifierService.getExplorationInfluence(7, 2L);
        assertThat(library.hasEffect()).isTrue();
        assertThat(library.sourceName()).isEqualTo("期中波动");
        assertThat(library.attributeChange().academicChange()).isEqualTo(1);
        assertThat(library.attributeChange().pressureChange()).isEqualTo(1);

        ExplorationInfluence classroom = weeklyModifierService.getExplorationInfluence(6, 1L);
        assertThat(classroom.hasEffect()).isTrue();
        assertThat(classroom.attributeChange().academicChange()).isEqualTo(1);
    }

    @Test
    void midtermStageNoEffectAtNonPrimaryLocation() {
        // 操场(5) is not a midterm primary location
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(7, 5L);
        assertThat(influence.hasEffect()).isFalse();
    }

    @Test
    void routeStageProvidesSkillBonusAtPrimaryLocations() {
        // week 9-11 = route, primary locations: 2(图书馆), 6(实验室), 7(惟学楼)
        ExplorationInfluence lab = weeklyModifierService.getExplorationInfluence(10, 6L);
        assertThat(lab.hasEffect()).isTrue();
        assertThat(lab.sourceName()).isEqualTo("路线分化");
        assertThat(lab.attributeChange().skillChange()).isEqualTo(1);

        ExplorationInfluence library = weeklyModifierService.getExplorationInfluence(9, 2L);
        assertThat(library.hasEffect()).isTrue();
        assertThat(library.attributeChange().skillChange()).isEqualTo(1);
    }

    @Test
    void routeStageNoEffectAtNonPrimaryLocation() {
        // 教学楼(1) is not a route primary location
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(9, 1L);
        assertThat(influence.hasEffect()).isFalse();
    }

    @Test
    void weekFifteenLibraryAddsReviewInfluence() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(15, 2L);

        assertThat(influence.sourceType()).isEqualTo("weekly_theme");
        assertThat(influence.sourceName()).isEqualTo("期末与体测");
        assertThat(influence.exploreBonus()).isEqualTo(1);
        assertThat(influence.attributeChange().academicChange()).isEqualTo(1);
        assertThat(influence.attributeChange().skillChange()).isEqualTo(1);
        assertThat(influence.attributeChange().pressureChange()).isEqualTo(-1);
    }

    @Test
    void weekFifteenPlaygroundAddsPhysicalTestInfluenceButServiceWindowDoesNot() {
        ExplorationInfluence playground = weeklyModifierService.getExplorationInfluence(15, 5L);
        ExplorationInfluence serviceWindow = weeklyModifierService.getExplorationInfluence(15, 8L);

        assertThat(playground.sourceName()).isEqualTo("期末与体测");
        assertThat(playground.exploreBonus()).isEqualTo(1);
        assertThat(playground.attributeChange().healthChange()).isEqualTo(2);
        assertThat(playground.attributeChange().pressureChange()).isEqualTo(-1);

        assertThat(serviceWindow.hasEffect()).isFalse();
    }

    @Test
    void nonPrimaryLocationInActiveStageReturnsEmpty() {
        // week 3 = rhythm stage, but 教学楼(1) is not a rhythm primary location
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(3, 1L);
        assertThat(influence.hasEffect()).isFalse();
    }
}
