package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
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
class EndingScoreServiceTests {

    @Autowired
    private EndingScoreService endingScoreService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerAttributeMapper playerAttributeMapper;

    // ==================== 基础测试 ====================

    @Test
    void buildScoreReportReturnsNonNullForFreshProfile() {
        playerService.createProfile(2L, "score fresh test", "大二", "计算机类", "就业路线");

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        assertThat(report).isNotNull();
        assertThat(report.routeTendencyName()).isNotBlank();
        assertThat(report.routeTendencyDesc()).isNotBlank();
        assertThat(report.scores()).hasSize(5);
        assertThat(report.evidence()).isNotEmpty();
        assertThat(report.semesterSummaryText()).isNotBlank();
    }

    @Test
    void allFiveDimensionsPresent() {
        playerService.createProfile(2L, "score dims test", "大二", "计算机类", "考研路线");

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        assertThat(report.scores()).extracting("dimensionKey")
                .containsExactly("academic", "skill", "social", "survival", "balanced");
    }

    @Test
    void scoresAreClampedTo0Through100() {
        playerService.createProfile(2L, "score clamp test", "大二", "计算机类", "六边形路线");

        // 极端属性值
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setAcademic(100);
        attr.setHealth(100);
        attr.setSocial(100);
        attr.setSkill(100);
        attr.setPressure(0);
        attr.setDiscipline(100);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        for (EndingScoreService.RouteDimensionScore score : report.scores()) {
            assertThat(score.score()).isBetween(0, 100);
        }
    }

    @Test
    void scoreLabelsReflectValues() {
        playerService.createProfile(2L, "score label test", "大二", "计算机类", "摆烂求生路线");

        // 高压力低健康 → survival 分数应该低
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setAcademic(20);
        attr.setHealth(20);
        attr.setSocial(20);
        attr.setSkill(20);
        attr.setPressure(90);
        attr.setDiscipline(20);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        EndingScoreService.RouteDimensionScore survival = report.scores().stream()
                .filter(s -> "survival".equals(s.dimensionKey()))
                .findFirst()
                .orElseThrow();
        assertThat(survival.label()).isEqualTo("不足");

        EndingScoreService.RouteDimensionScore academic = report.scores().stream()
                .filter(s -> "academic".equals(s.dimensionKey()))
                .findFirst()
                .orElseThrow();
        assertThat(academic.label()).isEqualTo("不足");
    }

    // ==================== 路线倾向测试 ====================

    @Test
    void academicRouteProfileShowsHighAcademicScore() {
        playerService.createProfile(2L, "score academic route", "大二", "计算机类", "考研路线");

        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setAcademic(80);
        attr.setDiscipline(70);
        attr.setPressure(30);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        assertThat(report.routeTendencyName()).isEqualTo("学业路线");

        EndingScoreService.RouteDimensionScore academic = report.scores().stream()
                .filter(s -> "academic".equals(s.dimensionKey()))
                .findFirst()
                .orElseThrow();
        assertThat(academic.score()).isGreaterThanOrEqualTo(60);
        assertThat(academic.label()).isIn("良好", "优秀");
    }

    @Test
    void socialRouteProfileShowsHighSocialScore() {
        playerService.createProfile(2L, "score social route", "大二", "计算机类", "六边形路线");

        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSocial(80);
        attr.setHealth(70);
        attr.setPressure(25);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        EndingScoreService.RouteDimensionScore social = report.scores().stream()
                .filter(s -> "social".equals(s.dimensionKey()))
                .findFirst()
                .orElseThrow();
        assertThat(social.score()).isGreaterThanOrEqualTo(30);
    }

    // ==================== 证据生成测试 ====================

    @Test
    void evidenceGeneratedForHighPerformingDimensions() {
        playerService.createProfile(2L, "score evidence test", "大二", "计算机类", "就业路线");

        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setAcademic(70);
        attr.setDiscipline(65);
        attr.setHealth(80);
        attr.setPressure(20);
        attr.setSkill(60);
        attr.setSocial(50);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        assertThat(report.evidence()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(report.evidence()).hasSizeLessThanOrEqualTo(4);
    }

    @Test
    void lowScoresProduceFallbackEvidence() {
        playerService.createProfile(2L, "score fallback test", "大二", "计算机类", "摆烂求生路线");

        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setAcademic(10);
        attr.setHealth(10);
        attr.setSocial(10);
        attr.setSkill(10);
        attr.setPressure(95);
        attr.setDiscipline(10);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        // 极端低分时应该有 fallback 证据
        assertThat(report.evidence()).isNotEmpty();
    }

    // ==================== 学期总结文案测试 ====================

    @Test
    void summaryTextContainsRouteName() {
        playerService.createProfile(2L, "score summary test", "大二", "计算机类", "考研路线");

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        // 总结文案应包含推导的路线倾向名称（可能和 chosenRoute 不同）
        assertThat(report.semesterSummaryText()).contains(report.routeTendencyName());
    }

    @Test
    void summaryTextVariesByPerformance() {
        playerService.createProfile(2L, "score summary vary", "大二", "计算机类", "就业路线");

        // 高性能属性
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setAcademic(75);
        attr.setHealth(80);
        attr.setSocial(75);
        attr.setSkill(80);
        attr.setPressure(20);
        attr.setDiscipline(75);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport highReport = endingScoreService.buildScoreReport(2L);

        // 低性能属性
        attr.setAcademic(25);
        attr.setHealth(25);
        attr.setSocial(25);
        attr.setSkill(25);
        attr.setPressure(85);
        attr.setDiscipline(25);
        playerAttributeMapper.updateById(attr);

        EndingScoreService.EndingScoreReport lowReport = endingScoreService.buildScoreReport(2L);

        // 两份文案应该不同
        assertThat(highReport.semesterSummaryText()).isNotEqualTo(lowReport.semesterSummaryText());
    }

    // ==================== 数据缺失 fallback 测试 ====================

    @Test
    void noWeeklyGoalsStillProducesReport() {
        playerService.createProfile(2L, "score no goals", "大二", "计算机类", "竞赛路线");

        // 不创建任何周目标，直接出报告
        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        assertThat(report).isNotNull();
        assertThat(report.scores()).hasSize(5);
    }

    @Test
    void noNpcRelationsStillProducesReport() {
        playerService.createProfile(2L, "score no npc", "大二", "计算机类", "摆烂求生路线");

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        assertThat(report).isNotNull();
        EndingScoreService.RouteDimensionScore social = report.scores().stream()
                .filter(s -> "social".equals(s.dimensionKey()))
                .findFirst()
                .orElseThrow();
        // 没有 NPC 关系，社交分数应该较低但仍可计算
        assertThat(social.score()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void noDungeonRecordsStillProducesReport() {
        playerService.createProfile(2L, "score no dungeon", "大二", "计算机类", "就业路线");

        EndingScoreService.EndingScoreReport report = endingScoreService.buildScoreReport(2L);

        assertThat(report).isNotNull();
        EndingScoreService.RouteDimensionScore skill = report.scores().stream()
                .filter(s -> "skill".equals(s.dimensionKey()))
                .findFirst()
                .orElseThrow();
        assertThat(skill.score()).isGreaterThanOrEqualTo(0);
    }
}
