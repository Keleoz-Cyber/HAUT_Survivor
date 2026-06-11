package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
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
class RouteTendencyServiceTests {

    @Autowired
    private RouteTendencyService routeTendencyService;

    @Test
    void highAcademicDerivesAcademicRoute() {
        PlayerAttribute attr = makeAttr(85, 60, 50, 40, 50, 30, 70);
        RouteTendencyService.RouteTendency tendency = routeTendencyService.deriveTendency(attr, "考研路线");
        assertThat(tendency.routeKey()).isEqualTo("academic");
        assertThat(tendency.routeName()).isEqualTo("学业路线");
        assertThat(tendency.description()).isNotBlank();
        assertThat(tendency.score()).isGreaterThan(0);
    }

    @Test
    void highSocialDerivesSocialRoute() {
        PlayerAttribute attr = makeAttr(50, 70, 50, 85, 40, 30, 50);
        RouteTendencyService.RouteTendency tendency = routeTendencyService.deriveTendency(attr, "考研路线");
        assertThat(tendency.routeKey()).isEqualTo("social");
    }

    @Test
    void highSkillDerivesSkillRoute() {
        PlayerAttribute attr = makeAttr(50, 60, 50, 40, 90, 40, 50);
        RouteTendencyService.RouteTendency tendency = routeTendencyService.deriveTendency(attr, "就业路线");
        assertThat(tendency.routeKey()).isEqualTo("skill");
    }

    @Test
    void highHealthLowPressureDerivesSurvivalRoute() {
        PlayerAttribute attr = makeAttr(40, 90, 50, 40, 40, 20, 30);
        RouteTendencyService.RouteTendency tendency = routeTendencyService.deriveTendency(attr, "摆烂求生路线");
        assertThat(tendency.routeKey()).isEqualTo("survival");
    }

    @Test
    void balancedAttributesDerivesBalancedRoute() {
        PlayerAttribute attr = makeAttr(60, 60, 60, 60, 60, 40, 60);
        RouteTendencyService.RouteTendency tendency = routeTendencyService.deriveTendency(attr, "六边形路线");
        assertThat(tendency.routeKey()).isEqualTo("balanced");
    }

    @Test
    void deriveAllTendenciesReturnsAllFive() {
        PlayerAttribute attr = makeAttr(60, 70, 50, 50, 50, 30, 50);
        List<RouteTendencyService.RouteTendency> all = routeTendencyService.deriveAllTendencies(attr, "考研路线");
        assertThat(all).hasSize(5);
        assertThat(all.stream().map(RouteTendencyService.RouteTendency::routeKey).toList())
                .containsExactly("academic", "social", "skill", "survival", "balanced");
    }

    @Test
    void chosenRouteConsistencyBonus() {
        // 考研路线 -> academic key, should get +10 bonus
        PlayerAttribute attr = makeAttr(60, 60, 50, 40, 50, 30, 50);

        List<RouteTendencyService.RouteTendency> withAcademic = routeTendencyService.deriveAllTendencies(attr, "考研路线");
        RouteTendencyService.RouteTendency academic = withAcademic.stream()
                .filter(t -> "academic".equals(t.routeKey())).findFirst().orElseThrow();

        List<RouteTendencyService.RouteTendency> withoutBonus = routeTendencyService.deriveAllTendencies(attr, "摆烂求生路线");
        RouteTendencyService.RouteTendency academicNoBonus = withoutBonus.stream()
                .filter(t -> "academic".equals(t.routeKey())).findFirst().orElseThrow();

        assertThat(academic.score()).isGreaterThan(academicNoBonus.score());
    }

    @Test
    void nullAttributeReturnsZeroScores() {
        List<RouteTendencyService.RouteTendency> all = routeTendencyService.deriveAllTendencies(null, "考研路线");
        assertThat(all).hasSize(5);
        assertThat(all).allMatch(t -> t.score() == 0);
    }

    @Test
    void mapChosenRouteToKeyMapsAllRoutes() {
        assertThat(routeTendencyService.mapChosenRouteToKey("考研路线")).isEqualTo("academic");
        assertThat(routeTendencyService.mapChosenRouteToKey("就业路线")).isEqualTo("skill");
        assertThat(routeTendencyService.mapChosenRouteToKey("竞赛路线")).isEqualTo("skill");
        assertThat(routeTendencyService.mapChosenRouteToKey("六边形路线")).isEqualTo("balanced");
        assertThat(routeTendencyService.mapChosenRouteToKey("摆烂求生路线")).isEqualTo("survival");
        assertThat(routeTendencyService.mapChosenRouteToKey(null)).isEqualTo("balanced");
        assertThat(routeTendencyService.mapChosenRouteToKey("未知路线")).isEqualTo("balanced");
    }

    @Test
    void scoresAreClampedToZeroToHundred() {
        PlayerAttribute attr = makeAttr(100, 100, 100, 100, 100, 0, 100);
        List<RouteTendencyService.RouteTendency> all = routeTendencyService.deriveAllTendencies(attr, "六边形路线");
        assertThat(all).allMatch(t -> t.score() >= 0 && t.score() <= 100);
    }

    private PlayerAttribute makeAttr(int academic, int health, int money, int social, int skill, int pressure, int discipline) {
        PlayerAttribute attr = new PlayerAttribute();
        attr.setAcademic(academic);
        attr.setHealth(health);
        attr.setMoney(money);
        attr.setSocial(social);
        attr.setSkill(skill);
        attr.setPressure(pressure);
        attr.setDiscipline(discipline);
        return attr;
    }
}
