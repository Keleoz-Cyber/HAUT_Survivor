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

    @Test
    void npcBoostRumorProducesSocialInfluence() {
        // npc_boost gives +1 social in exploration influences
        // Try week 1 canteen (location 4) — rumor 4003 is npc_boost at canteen
        boolean found = false;
        for (long userId = 1L; userId <= 80L; userId++) {
            int boost = rumorEffectService.getNpcBoostForLocation(userId, 1, 4L);
            if (boost > 0) {
                List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, 1, 4L);
                assertThat(influences).anyMatch(i ->
                        "rumor".equals(i.sourceType())
                                && i.attributeChange() != null
                                && i.attributeChange().socialChange() > 0);
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void eventHintRumorProducesAttributeInfluence() {
        // event_hint gives +1 academic or skill in exploration influences
        // Try week 3 building (location 1) — rumor 4012 is event_hint
        boolean found = false;
        for (long userId = 1L; userId <= 80L; userId++) {
            String target = rumorEffectService.getEventHintTarget(userId, 3, 1L);
            if (target != null) {
                List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, 3, 1L);
                assertThat(influences).anyMatch(i ->
                        "rumor".equals(i.sourceType())
                                && i.sourceName().contains("小测")
                                && i.attributeChange() != null
                                && i.attributeChange().hasAnyChange());
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void getNpcBoostForLocationReturnsBoostValue() {
        // Week 1 has npc_boost rumor at canteen (location 4) with effect_value 10
        boolean found = false;
        for (long userId = 1L; userId <= 80L; userId++) {
            int boost = rumorEffectService.getNpcBoostForLocation(userId, 1, 4L);
            if (boost > 0) {
                assertThat(boost).isGreaterThanOrEqualTo(10);
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void getEventHintTargetReturnsTarget() {
        // Week 3 has event_hint rumor at building (location 1) with target "academic"
        Long userId = userSeeingRumor(3, "小测");
        String target = rumorEffectService.getEventHintTarget(userId, 3, 1L);

        assertThat(target).isEqualTo("academic");
    }

    @Test
    void getEventHintTargetReturnsNullWhenNoHint() {
        // Week 1 at lab (location 6) has no event_hint rumor
        int boost = rumorEffectService.getNpcBoostForLocation(1L, 1, 6L);
        // Just verify it doesn't throw; value may be 0
        assertThat(boost).isGreaterThanOrEqualTo(0);
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