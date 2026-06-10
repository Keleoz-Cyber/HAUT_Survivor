package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class SemesterArchiveServiceTests {

    @Autowired
    private SemesterArchiveService semesterArchiveService;

    @Autowired
    private InfluenceLogService influenceLogService;

    @Autowired
    private NpcService npcService;

    @Autowired
    private PlayerService playerService;

    @Test
    void buildSummaryHandlesEmptyLogs() {
        playerService.createProfile(2L, "archive empty test", "大二", "计算机类", "就业路线");

        SemesterArchiveService.SemesterArchiveSummary summary = semesterArchiveService.buildSummary(2L);

        assertThat(summary.growthKeyword()).isNotBlank();
        assertThat(summary.growthDescription()).isNotBlank();
        assertThat(summary.mainSourceLabel()).isNotBlank();
        assertThat(summary.keyWeekNumber()).isNull();
        assertThat(summary.positiveCount()).isZero();
        assertThat(summary.negativeCount()).isZero();
        assertThat(summary.neutralCount()).isZero();
        assertThat(summary.highlights()).isNotEmpty();
    }

    @Test
    void buildSummaryPicksKeyWeekSourceAndNpcDeterministically() {
        playerService.createProfile(2L, "archive summary test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 60);
        npcService.increaseFamiliarity(2L, 3L, 35);

        influenceLogService.recordInfluence(2L, 1, 2L, "rumor", "图书馆传闻",
                "有人分享了复习资料。", new AttributeChange(2, 0, 0, 0, 0, 0, 0, 0), 0);
        influenceLogService.recordInfluence(2L, 2, 2L, "npc_story", "复习提纲交换",
                "林然帮你圈出易错点。", new AttributeChange(2, 0, 0, 0, 0, 1, 1, 0), 0);
        influenceLogService.recordInfluence(2L, 2, 4L, "weekly_theme", "DDL 周",
                "这周事情明显变多。", new AttributeChange(0, -1, 0, 0, 0, 2, 0, 0), 0);

        SemesterArchiveService.SemesterArchiveSummary summary = semesterArchiveService.buildSummary(2L);

        assertThat(summary.keyWeekNumber()).isEqualTo(2);
        assertThat(summary.mainSourceLabel()).isNotBlank();
        assertThat(summary.keyNpcName()).contains("林然");
        assertThat(summary.keyNpcStage()).isEqualTo("搭子");
        assertThat(summary.positiveCount()).isEqualTo(1);
        assertThat(summary.negativeCount()).isEqualTo(1);
        assertThat(summary.neutralCount()).isEqualTo(1);
        assertThat(summary.highlights()).hasSizeGreaterThanOrEqualTo(3);
    }
}
