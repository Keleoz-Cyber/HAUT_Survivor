package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class InfluenceLogServiceTests {

    @Autowired
    private InfluenceLogService influenceLogService;

    @Test
    void recordExplorationInfluencesPersistsOnlyEffectiveInfluences() {
        influenceLogService.recordExplorationInfluences(2L, 1, 4L, List.of(
                new ExplorationInfluence("rumor", "canteen_hint", "social opening",
                        new AttributeChange(0, 0, 0, 2, 0, 0, 0, 0), 0),
                new ExplorationInfluence("weekly_theme", "quiet_week", "no visible effect",
                        AttributeChange.EMPTY, 0)
        ));

        List<InfluenceLogService.InfluenceLogEntry> entries = influenceLogService.listWeekInfluences(2L, 1);

        assertThat(entries).hasSize(1);
        InfluenceLogService.InfluenceLogEntry entry = entries.get(0);
        assertThat(entry.sourceType()).isEqualTo("rumor");
        assertThat(entry.sourceLabel()).isEqualTo("校园传闻");
        assertThat(entry.sourceName()).isEqualTo("canteen_hint");
        assertThat(entry.description()).isEqualTo("social opening");
        assertThat(entry.socialChange()).isEqualTo(2);
        assertThat(entry.exploreBonus()).isZero();
    }

    @Test
    void listSemesterInfluenceRecapsGroupsLogsByWeekWithRecentWeeksFirst() throws Exception {
        influenceLogService.recordExplorationInfluences(2L, 1, 4L, List.of(
                new ExplorationInfluence("rumor", "week one rumor", "first week",
                        new AttributeChange(0, 0, 0, 1, 0, 0, 0, 0), 0)
        ));
        influenceLogService.recordExplorationInfluences(2L, 2, 5L, List.of(
                new ExplorationInfluence("story", "week two story", "second week",
                        new AttributeChange(0, 0, 0, 0, 1, 0, 0, 0), 1)
        ));
        influenceLogService.recordExplorationInfluences(1L, 2, 5L, List.of(
                new ExplorationInfluence("rumor", "other user", "not visible",
                        new AttributeChange(0, 0, 0, 3, 0, 0, 0, 0), 0)
        ));

        Method method = influenceLogService.getClass().getMethod("listSemesterInfluenceRecaps", Long.class);
        @SuppressWarnings("unchecked")
        List<Object> recaps = (List<Object>) method.invoke(influenceLogService, 2L);

        assertThat(recaps).hasSize(2);
        assertThat(readProperty(recaps.get(0), "weekNumber")).isEqualTo(2);
        assertThat(readProperty(recaps.get(1), "weekNumber")).isEqualTo(1);
        assertThat(readEntries(recaps.get(0))).extracting("sourceName").containsExactly("week two story");
        assertThat(readEntries(recaps.get(1))).extracting("sourceName").containsExactly("week one rumor");
    }

    private Object readProperty(Object target, String propertyName) throws Exception {
        return target.getClass().getMethod(propertyName).invoke(target);
    }

    @SuppressWarnings("unchecked")
    private List<InfluenceLogService.InfluenceLogEntry> readEntries(Object target) throws Exception {
        return (List<InfluenceLogService.InfluenceLogEntry>) readProperty(target, "entries");
    }
}
