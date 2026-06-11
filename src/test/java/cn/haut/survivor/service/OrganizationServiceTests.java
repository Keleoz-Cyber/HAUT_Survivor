package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Organization;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.domain.entity.UserOrganization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class OrganizationServiceTests {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerAttributeMapper playerAttributeMapper;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "组织测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void listAllReturnsSeededOrganizations() {
        List<Organization> orgs = organizationService.listAll();

        assertThat(orgs).hasSizeGreaterThanOrEqualTo(2);
        assertThat(orgs).extracting(Organization::getOrgName).contains("学生会", "实验室项目组");
    }

    @Test
    void discoverCreatesRelation() {
        UserOrganization relation = organizationService.discover(2L, 1L);

        assertThat(relation).isNotNull();
        assertThat(relation.getMembershipStatus()).isEqualTo("discovered");
        assertThat(relation.getPositionName()).isEqualTo("非成员");
    }

    @Test
    void discoverIsIdempotent() {
        UserOrganization first = organizationService.discover(2L, 1L);
        UserOrganization second = organizationService.discover(2L, 1L);

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void joinSucceedsWithSufficientSocial() {
        // 就业路线初始 social=55, 满足 >= 40
        organizationService.discover(2L, 1L);
        UserOrganization relation = organizationService.join(2L, 1L);

        assertThat(relation.getMembershipStatus()).isEqualTo("member");
        assertThat(relation.getPositionName()).isEqualTo("干事");
        assertThat(relation.getJoinWeek()).isEqualTo(1);
    }

    @Test
    void joinFailsWithLowSocial() {
        // 手动把社交改低
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSocial(20);
        playerAttributeMapper.updateById(attr);

        organizationService.discover(2L, 1L);

        assertThatThrownBy(() -> organizationService.join(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("社交值不足");
    }

    @Test
    void joinTwiceThrows() {
        organizationService.discover(2L, 1L);
        organizationService.join(2L, 1L);

        assertThatThrownBy(() -> organizationService.join(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已经加入");
    }

    @Test
    void attendActivityIncreasesContributionAndAttributes() {
        organizationService.discover(2L, 1L);
        organizationService.join(2L, 1L);
        UserOrganization relation = organizationService.attendActivity(2L, 1L);

        assertThat(relation.getContribution()).isEqualTo(3);
        assertThat(relation.getReputation()).isEqualTo(2);

        // 学生会：社交+3，自律+2，压力+2
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        assertThat(attr.getSocial()).isEqualTo(58);  // 55 + 3
        assertThat(attr.getDiscipline()).isEqualTo(52); // 50 + 2
        assertThat(attr.getPressure()).isEqualTo(32); // 30 + 2
    }

    @Test
    void recruitmentWeekAddsSmallOrganizationActivityGain() {
        organizationService.discover(2L, 1L);
        organizationService.join(2L, 1L);
        // Advance to week 3: rhythm stage (社团招新峰值)
        playerService.advanceWeek(2L);
        playerService.advanceWeek(2L);

        OrganizationService.OrganizationActivityResult result = organizationService.attendActivityWithChange(2L, 1L);
        UserOrganization relation = result.relation();

        assertThat(relation.getContribution()).isEqualTo(4);
        assertThat(relation.getReputation()).isEqualTo(3);
        assertThat(result.activityResultText()).contains("节奏建立");
    }

    @Test
    void labActivityBoostsSkillAndAcademic() {
        organizationService.discover(2L, 2L);
        organizationService.join(2L, 2L);
        organizationService.attendActivity(2L, 2L);

        // 实验室：技能+3，学业+2，压力+3
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        assertThat(attr.getSkill()).isEqualTo(53);   // 50 + 3
        assertThat(attr.getAcademic()).isEqualTo(62); // 60 + 2
        assertThat(attr.getPressure()).isEqualTo(33);  // 30 + 3
    }

    @Test
    void basketballActivityBoostsHealthAndReducesPressure() {
        organizationService.discover(2L, 3L);
        organizationService.join(2L, 3L);
        organizationService.attendActivity(2L, 3L);

        // 篮球社：健康+3，社交+1，压力-2
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        assertThat(attr.getHealth()).isEqualTo(73);    // 70 + 3
        assertThat(attr.getSocial()).isEqualTo(56);    // 55 + 1
        assertThat(attr.getPressure()).isEqualTo(28); // 30 - 2
    }

    @Test
    void attendActivityPromotesToCoreMember() {
        organizationService.discover(2L, 1L);
        organizationService.join(2L, 1L);

        // 参加 4 次活动：贡献 12 >= 10
        for (int i = 0; i < 3; i++) {
            organizationService.attendActivity(2L, 1L);
        }
        // 第 4 次之前需要恢复行动点
        playerService.advanceWeek(2L);
        UserOrganization relation = organizationService.attendActivity(2L, 1L);

        assertThat(relation.getMembershipStatus()).isEqualTo("core_member");
        assertThat(relation.getPositionName()).isEqualTo("部长");
    }

    @Test
    void attendWithoutJoinThrows() {
        organizationService.discover(2L, 1L);

        assertThatThrownBy(() -> organizationService.attendActivity(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不是");
    }

    @Test
    void findRelationReturnsNullForUnknown() {
        UserOrganization relation = organizationService.findRelation(2L, 1L);

        assertThat(relation).isNull();
    }

    @Test
    void openingWeekLowersOrganizationJoinSocialRequirement() {
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSocial(36);
        playerAttributeMapper.updateById(attr);

        organizationService.discover(2L, 1L);
        UserOrganization relation = organizationService.join(2L, 1L);

        assertThat(relation.getMembershipStatus()).isEqualTo("member");
        assertThat(relation.getJoinWeek()).isEqualTo(1);
    }

    @Test
    void organizationJoinRequirementReturnsToNormalAfterOpeningWeek() {
        // Advance to week 3: rhythm stage, opening bonus no longer applies
        playerService.advanceWeek(2L);
        playerService.advanceWeek(2L);
        PlayerAttribute attr = playerService.findAttributeByUserId(2L);
        attr.setSocial(36);
        playerAttributeMapper.updateById(attr);

        organizationService.discover(2L, 1L);

        assertThatThrownBy(() -> organizationService.join(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("社交值不足")
                .hasMessageContaining("40");
    }
}
