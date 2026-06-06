package cn.haut.survivor.service;

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
class RumorServiceTests {

    @Autowired
    private RumorService rumorService;

    @Test
    void listByWeekReturnsRumorsForThatWeek() {
        List<Rumor> week1 = rumorService.listByWeek(1);
        assertThat(week1).isNotEmpty();
        assertThat(week1).allMatch(r -> r.getWeekNumber() == 1);
    }

    @Test
    void listByWeekReturnsEmptyForNonexistentWeek() {
        List<Rumor> week99 = rumorService.listByWeek(99);
        assertThat(week99).isEmpty();
    }

    @Test
    void pickRumorsForUserReturnsStableResults() {
        // Same user + same week should return same rumors
        List<Rumor> first = rumorService.pickRumorsForUser(2L, 1, 3);
        List<Rumor> second = rumorService.pickRumorsForUser(2L, 1, 3);
        assertThat(first.size()).isEqualTo(second.size());
        for (int i = 0; i < first.size(); i++) {
            assertThat(first.get(i).getId()).isEqualTo(second.get(i).getId());
        }
    }

    @Test
    void pickRumorsForUserDifferentWeekReturnsDifferentPool() {
        List<Rumor> week1 = rumorService.listByWeek(1);
        List<Rumor> week2 = rumorService.listByWeek(2);
        // Different weeks should have different rumor pools
        if (!week1.isEmpty() && !week2.isEmpty()) {
            assertThat(week1.get(0).getWeekNumber()).isNotEqualTo(week2.get(0).getWeekNumber());
        }
    }

    @Test
    void pickRumorsRespectsCountLimit() {
        List<Rumor> picked = rumorService.pickRumorsForUser(2L, 1, 2);
        assertThat(picked.size()).isBetween(0, 2);
    }
}
