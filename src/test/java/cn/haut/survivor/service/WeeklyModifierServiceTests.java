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
        assertThat(influence.description()).contains("开学适应周");
    }

    @Test
    void weekThreeLabAddsSkillAndPressure() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(3, 6L);

        assertThat(influence.attributeChange().skillChange()).isEqualTo(1);
        assertThat(influence.attributeChange().pressureChange()).isEqualTo(1);
        assertThat(influence.description()).contains("DDL");
    }

    @Test
    void unknownWeekReturnsEmptyInfluence() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(9, 2L);

        assertThat(influence.hasEffect()).isFalse();
        assertThat(influence.description()).isBlank();
    }

    @Test
    void weekFourLibraryAddsReviewInfluence() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(4, 2L);

        assertThat(influence.sourceType()).isEqualTo("weekly_theme");
        assertThat(influence.sourceName()).isEqualTo("期末与体测周");
        assertThat(influence.exploreBonus()).isEqualTo(1);
        assertThat(influence.attributeChange().academicChange()).isEqualTo(1);
        assertThat(influence.attributeChange().skillChange()).isEqualTo(1);
        assertThat(influence.attributeChange().pressureChange()).isEqualTo(-1);
    }

    @Test
    void weekFourPlaygroundAddsPhysicalTestInfluenceButServiceWindowDoesNot() {
        ExplorationInfluence playground = weeklyModifierService.getExplorationInfluence(4, 5L);
        ExplorationInfluence serviceWindow = weeklyModifierService.getExplorationInfluence(4, 8L);

        assertThat(playground.sourceName()).isEqualTo("期末与体测周");
        assertThat(playground.exploreBonus()).isEqualTo(1);
        assertThat(playground.attributeChange().healthChange()).isEqualTo(2);
        assertThat(playground.attributeChange().pressureChange()).isEqualTo(-1);

        assertThat(serviceWindow.hasEffect()).isFalse();
    }
}