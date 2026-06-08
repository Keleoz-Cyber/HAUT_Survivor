package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.Rumor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class RumorEffectServiceTests {

    @Autowired
    private RumorEffectService rumorEffectService;

    @Autowired
    private RumorService rumorService;

    @Test
    void locationRumorProducesInfluence() {
        Long userId = userSeeingRumor(1, "图书馆");
        List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, 1, 2L);

        assertThat(influences).anyMatch(i ->
                "rumor".equals(i.sourceType())
                        && i.sourceName().contains("图书馆")
                        && i.exploreBonus() > 0);
    }

    @Test
    void nonMatchingLocationDoesNotUseLocationRumor() {
        Long userId = userSeeingRumor(1, "图书馆");
        List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, 1, 6L);

        assertThat(influences).noneMatch(i -> i.sourceName().contains("图书馆二楼"));
    }

    @Test
    void attrBonusRumorProducesAttributeChange() {
        // Try multiple weeks and locations to find a attr_bonus rumor influence
        // Week 2 has rumor 4007 (attr_bonus skill, lab, rare) and 4005/4006 (attr_bonus social/health, common)
        boolean found = false;
        for (int week = 1; week <= 4; week++) {
            for (long userId = 1L; userId <= 80L; userId++) {
                List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, week, 4L);
                if (influences.stream().anyMatch(i ->
                        i.attributeChange() != null && i.attributeChange().hasAnyChange())) {
                    assertThat(influences).anyMatch(i ->
                            "rumor".equals(i.sourceType()) && i.attributeChange().hasAnyChange());
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        assertThat(found).isTrue();
    }

    private Long userSeeingRumor(int weekNumber, String titlePart) {
        for (long userId = 1L; userId <= 80L; userId++) {
            List<Rumor> visible = rumorService.pickVisibleRumorsForUser(userId, weekNumber);
            boolean found = visible.stream()
                    .map(Rumor::getRumorTitle)
                    .anyMatch(title -> title != null && title.contains(titlePart));
            if (found) {
                return userId;
            }
        }
        throw new AssertionError("No deterministic user sees rumor title containing: " + titlePart);
    }
}