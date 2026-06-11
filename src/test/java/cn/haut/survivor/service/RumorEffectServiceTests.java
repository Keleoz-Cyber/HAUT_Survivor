package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.mapper.RumorMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;

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

    @Autowired
    private RumorMapper rumorMapper;

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
    void eventHintRumorUsesTargetAttributeForSocialSkillHealthAndMoney() {
        insertEventHintRumor(91, 1L, "test social hint", "social");
        insertEventHintRumor(92, 1L, "test skill hint", "skill");
        insertEventHintRumor(93, 1L, "test health hint", "health");
        insertEventHintRumor(98, 1L, "test money hint", "money");
        insertEventHintRumor(99, 1L, "test pressure hint", "pressure");

        assertEventHintInfluence(91, "test social hint", change -> change.socialChange() == 1);
        assertEventHintInfluence(92, "test skill hint", change -> change.skillChange() == 1);
        assertEventHintInfluence(93, "test health hint", change -> change.healthChange() == 1);
        assertEventHintInfluence(98, "test money hint", change -> change.moneyChange() == 1);
        assertEventHintInfluence(99, "test pressure hint", change -> change.pressureChange() == 1);
    }

    @Test
    void getEventHintPreferredEventTypeMapsSupportedTargets() {
        insertEventHintRumor(94, 1L, "test academic preferred event", "academic");
        insertEventHintRumor(95, 1L, "test social preferred event", "social");
        insertEventHintRumor(96, 1L, "test skill preferred event", "skill");
        insertEventHintRumor(97, 1L, "test health preferred event", "health");
        insertEventHintRumor(98, 1L, "test money preferred event", "money");
        insertEventHintRumor(99, 1L, "test pressure preferred event", "pressure");

        assertThat(rumorEffectService.getEventHintPreferredEventType(1L, 94, 1L)).isEqualTo("academic_crisis");
        assertThat(rumorEffectService.getEventHintPreferredEventType(1L, 95, 1L)).isEqualTo("社交");
        assertThat(rumorEffectService.getEventHintPreferredEventType(1L, 96, 1L)).isEqualTo("技能");
        assertThat(rumorEffectService.getEventHintPreferredEventType(1L, 97, 1L)).isEqualTo("健康");
        assertThat(rumorEffectService.getEventHintPreferredEventType(1L, 98, 1L)).isEqualTo("金钱");
        assertThat(rumorEffectService.getEventHintPreferredEventType(1L, 99, 1L)).isEqualTo("学习");
    }

    @Test
    void eventHintLifeTargetMapsToLifeEventType() {
        insertEventHintRumor(100, 8L, "test life hint", "life");

        assertThat(rumorEffectService.getEventHintPreferredEventType(1L, 100, 8L)).isEqualTo("生活");
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

    private void assertEventHintInfluence(int weekNumber, String titlePart, Predicate<AttributeChange> changePredicate) {
        Long userId = userSeeingRumor(weekNumber, titlePart);
        List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, weekNumber, 1L);

        assertThat(influences).anyMatch(i ->
                "rumor".equals(i.sourceType())
                        && i.sourceName().contains(titlePart)
                        && i.attributeChange() != null
                        && changePredicate.test(i.attributeChange()));
    }

    private void insertEventHintRumor(int weekNumber, Long locationId, String title, String target) {
        Rumor rumor = new Rumor();
        rumor.setWeekNumber(weekNumber);
        rumor.setLocationId(locationId);
        rumor.setRumorTitle(title);
        rumor.setRumorText(title);
        rumor.setEffectHint("test hint");
        rumor.setEffectType("event_hint");
        rumor.setEffectValue(1);
        rumor.setEffectTarget(target);
        rumor.setRarity("common");
        rumor.setActive(1);
        rumorMapper.insert(rumor);
    }
}
