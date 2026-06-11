package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.SemesterEnding;
import cn.haut.survivor.domain.entity.UserSemesterEnding;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.UserLocationExplorationMapper;
import cn.haut.survivor.mapper.UserOrganizationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class SemesterEndingServiceTests {

    @Autowired
    private SemesterEndingService semesterEndingService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerAttributeMapper playerAttributeMapper;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "结局测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void listAllEndingsReturnsSeededData() {
        List<SemesterEnding> endings = semesterEndingService.listAllEndings();
        assertThat(endings).hasSizeGreaterThanOrEqualTo(7);
        assertThat(endings).extracting(SemesterEnding::getEndingName)
                .contains("课设战神", "图书馆常驻民", "社团风云人物", "DDL 幸存者", "快乐摆烂人", "六边形工大学子");
    }

    @Test
    void settleRequiresSemesterOver() {
        assertThatThrownBy(() -> semesterEndingService.settleSemester(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学期尚未结束");
    }

    @Test
    void settleSemesterReturnsMatchedEnding() {
        // 推进到学期结束
        advanceToSemesterEnd();

        // 就业路线初始: skill=50, social=55, academic=60, discipline=50, pressure=30
        // 课设战神需要 skill>=70，当前不满足
        // 社团风云人物需要 social>=70，当前不满足
        // 六边形工大学子需要所有>=50 且 pressure<60 —— 压力30<60, 学业60, 社交55, 技能50, 自律50, 健康70 → 满足
        SemesterEnding ending = semesterEndingService.settleSemester(2L);

        assertThat(ending).isNotNull();
        assertThat(ending.getEndingName()).isNotBlank();
    }

    @Test
    void settleMatchesCourseDesignGodWithHighSkill() {
        advanceToSemesterEnd();

        // 手动拉高技能和学业，满足课设战神条件
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSkill(75);
        attr.setAcademic(65);
        attr.setDiscipline(55);
        playerAttributeMapper.updateById(attr);

        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("课设战神");
    }

    @Test
    void settleMatchesSlackerWithLowStats() {
        advanceToSemesterEnd();

        // 手动降低属性满足快乐摆烂人条件
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setPressure(20);
        attr.setDiscipline(35);
        attr.setAcademic(45);
        playerAttributeMapper.updateById(attr);

        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("快乐摆烂人");
    }

    @Test
    void settleMatchesDDLSurvivorWithHighPressure() {
        advanceToSemesterEnd();

        // 手动设置属性满足 DDL 幸存者条件
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setPressure(70);
        attr.setAcademic(45);
        attr.setHealth(50);
        playerAttributeMapper.updateById(attr);

        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("DDL 幸存者");
    }

    @Test
    void settleOnlyOnce() {
        advanceToSemesterEnd();
        semesterEndingService.settleSemester(2L);

        assertThatThrownBy(() -> semesterEndingService.settleSemester(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已经完成");
    }

    @Test
    void hasSettledReturnsFalseBeforeSettlement() {
        // 学期还没结束
        assertThat(semesterEndingService.hasSettled(2L)).isFalse();
    }

    @Test
    void hasSettledReturnsTrueAfterSettlement() {
        advanceToSemesterEnd();
        semesterEndingService.settleSemester(2L);
        assertThat(semesterEndingService.hasSettled(2L)).isTrue();
    }

    @Test
    void findUserEndingReturnsRecord() {
        advanceToSemesterEnd();
        semesterEndingService.settleSemester(2L);

        UserSemesterEnding record = semesterEndingService.findUserEnding(2L);
        assertThat(record).isNotNull();
        assertThat(record.getGrowthRoute()).isEqualTo("就业路线");
        assertThat(record.getAcademic()).isGreaterThan(0);
    }

    @Test
    void conditionEvaluatorHandlesSimpleGte() {
        Map<String, Integer> vars = Map.of("skill", 70, "academic", 60);
        boolean result = ((cn.haut.survivor.service.impl.SemesterEndingServiceImpl) semesterEndingService)
                .evaluateCondition("skill>=70 AND academic>=60", vars);
        assertThat(result).isTrue();
    }

    @Test
    void conditionEvaluatorFailsWhenConditionNotMet() {
        Map<String, Integer> vars = Map.of("skill", 50, "academic", 60);
        boolean result = ((cn.haut.survivor.service.impl.SemesterEndingServiceImpl) semesterEndingService)
                .evaluateCondition("skill>=70 AND academic>=60", vars);
        assertThat(result).isFalse();
    }

    @Test
    void conditionEvaluatorHandlesLt() {
        Map<String, Integer> vars = Map.of("social", 45, "pressure", 25);
        boolean result = ((cn.haut.survivor.service.impl.SemesterEndingServiceImpl) semesterEndingService)
                .evaluateCondition("social<50 AND pressure<30", vars);
        assertThat(result).isTrue();
    }

    @Test
    void conditionEvaluatorHandlesEmptyRule() {
        Map<String, Integer> vars = Map.of("skill", 50);
        boolean result = ((cn.haut.survivor.service.impl.SemesterEndingServiceImpl) semesterEndingService)
                .evaluateCondition("", vars);
        assertThat(result).isTrue();
    }

    // ==================== 路线结局测试 ====================

    @Test
    void buildSettlementContextReturnsExploreAndOrgData() {
        SemesterEndingService.SettlementContext ctx = semesterEndingService.buildSettlementContext(2L);

        // 初始探索度为 0，组织贡献为 0
        assertThat(ctx.labExploreLevel()).isEqualTo(0);
        assertThat(ctx.libraryExploreLevel()).isEqualTo(0);
        assertThat(ctx.playgroundExploreLevel()).isEqualTo(0);
        assertThat(ctx.orgContribution()).isEqualTo(0);
        assertThat(ctx.dungeon1Completed()).isFalse();
        assertThat(ctx.dungeon2Completed()).isFalse();
    }

    @Test
    void routeEndingMatchesLabExploreWithSkill() {
        advanceToSemesterEnd();

        // 手动拉高实验室探索度和技能
        cn.haut.survivor.domain.entity.UserLocationExploration exploration =
                explorationService.findExploration(2L, 6L);
        if (exploration == null) {
            exploration = new cn.haut.survivor.domain.entity.UserLocationExploration();
            exploration.setUserId(2L);
            exploration.setLocationId(6L);
            exploration.setExploreLevel(45);
            exploration.setExploreCount(1);
            exploration.setLastExploreWeek(1);
            explorationMapper.insert(exploration);
        } else {
            exploration.setExploreLevel(45);
            explorationMapper.updateById(exploration);
        }

        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSkill(60);
        playerAttributeMapper.updateById(attr);

        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("实验室编外研究员");
    }

    @Test
    void routeEndingMatchesOrgContributionWithSocial() {
        advanceToSemesterEnd();

        // 手动加入组织并获得贡献
        cn.haut.survivor.domain.entity.UserOrganization orgRelation = organizationService.findRelation(2L, 1L);
        if (orgRelation == null) {
            orgRelation = organizationService.discover(2L, 1L);
        }
        orgRelation.setMembershipStatus("member");
        orgRelation.setContribution(8);
        orgRelation.setReputation(5);
        orgRelation.setPositionName("干事");
        orgRelation.setJoinWeek(1);
        orgRelationMapper.updateById(orgRelation);

        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSocial(70);
        playerAttributeMapper.updateById(attr);

        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("社团风云人物");
    }

    @Test
    void routeEndingPriorityCourseDesignOverLabExplore() {
        // 同时满足课设战神和实验室编外研究员条件时，课设战神优先
        advanceToSemesterEnd();

        // 拉高实验室探索度和技能（满足实验室编外研究员）
        cn.haut.survivor.domain.entity.UserLocationExploration exploration =
                explorationService.findExploration(2L, 6L);
        if (exploration == null) {
            exploration = new cn.haut.survivor.domain.entity.UserLocationExploration();
            exploration.setUserId(2L);
            exploration.setLocationId(6L);
            exploration.setExploreLevel(45);
            exploration.setExploreCount(1);
            exploration.setLastExploreWeek(1);
            explorationMapper.insert(exploration);
        } else {
            exploration.setExploreLevel(45);
            explorationMapper.updateById(exploration);
        }

        // 同时完成课设副本且评价为课设战神
        cn.haut.survivor.domain.entity.UserDungeonRecord dungeonRecord = new cn.haut.survivor.domain.entity.UserDungeonRecord();
        dungeonRecord.setUserId(2L);
        dungeonRecord.setDungeonId(1L);
        dungeonRecord.setStatus("COMPLETED");
        dungeonRecord.setTotalScore(240);
        dungeonRecord.setFinalEvaluation("课设战神");
        dungeonRecord.setStartTime(java.time.LocalDateTime.now());
        dungeonRecordMapper.insert(dungeonRecord);

        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSkill(60);
        playerAttributeMapper.updateById(attr);

        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("课设战神");
    }

    @Test
    void noRouteEndingFallsBackToAttributeMatch() {
        // 不满足任何路线结局条件时，回退到属性条件匹配
        advanceToSemesterEnd();

        // 默认就业路线属性：skill=50, social=55, academic=60, discipline=50, pressure=30, health=70, money=80
        // 不满足任何路线结局条件
        // 体测幸存者需要 health>=75 AND pressure<=40，当前 health=70 不满足
        // 六边形工大学子：academic>=50 AND health>=50 AND social>=50 AND skill>=50 AND discipline>=50 AND pressure<60 → 满足
        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("六边形工大学子");
    }

    @Test
    void highHealthLowPressureMatchesSurvivorEnding() {
        advanceToSemesterEnd();

        // 手动设置高健康低压力，满足体测幸存者条件
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setHealth(80);
        attr.setPressure(35);
        playerAttributeMapper.updateById(attr);

        SemesterEnding ending = semesterEndingService.settleSemester(2L);
        assertThat(ending.getEndingName()).isEqualTo("体测幸存者");
    }

    @Autowired
    private cn.haut.survivor.service.ExplorationService explorationService;

    @Autowired
    private cn.haut.survivor.service.OrganizationService organizationService;

    @Autowired
    private cn.haut.survivor.mapper.UserOrganizationMapper orgRelationMapper;

    @Autowired
    private cn.haut.survivor.mapper.UserLocationExplorationMapper explorationMapper;

    @Autowired
    private cn.haut.survivor.mapper.UserDungeonRecordMapper dungeonRecordMapper;

    private void advanceToSemesterEnd() {
        for (int i = 0; i < 16; i++) {
            playerService.advanceWeek(2L);
        }
    }
}
