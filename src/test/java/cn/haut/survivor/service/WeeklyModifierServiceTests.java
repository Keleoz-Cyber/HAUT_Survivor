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
}