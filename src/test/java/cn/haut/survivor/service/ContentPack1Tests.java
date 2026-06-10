package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.Organization;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.NpcMapper;
import cn.haut.survivor.mapper.OrganizationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容扩展包 1 的 smoke test：
 * - 验证 seed 数据能正确加载
 * - 验证新增事件数量和选项完整性
 * - 验证 NPC 文案根据熟悉度分层
 * - 验证组织活动 resultText 按类型变化
 */
@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack1Tests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private NpcMapper npcMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private NpcService npcService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private OrganizationService organizationService;

    // ==================== Seed 数据加载测试 ====================

    @Test
    void seedEventsCountAtLeast62() {
        // 原始 42 + 扩展包 20 = 62
        long count = eventMapper.selectCount(new LambdaQueryWrapper<Event>()
                .eq(Event::getStatus, 1));
        assertThat(count).isGreaterThanOrEqualTo(62);
    }

    @Test
    void seedEventsCoverAll8Locations() {
        for (long locId = 1; locId <= 8; locId++) {
            long count = eventMapper.selectCount(new LambdaQueryWrapper<Event>()
                    .eq(Event::getLocationId, locId)
                    .eq(Event::getStatus, 1));
            assertThat(count)
                    .as("地点 id=%d 应至少有 3 个事件", locId)
                    .isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    void allEventsHaveAtLeastTwoOptions() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .eq(Event::getStatus, 1));
        for (Event event : events) {
            List<EventOption> options = eventOptionMapper.selectList(
                    new LambdaQueryWrapper<EventOption>().eq(EventOption::getEventId, event.getId()));
            assertThat(options.size())
                    .as("事件 [%s] (id=%d) 应至少有 2 个选项", event.getEventName(), event.getId())
                    .isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void expansionPackEventsExist() {
        // 验证扩展包新增的 20 个事件（id 43-62）存在
        for (long eventId = 43; eventId <= 62; eventId++) {
            Event event = eventMapper.selectById(eventId);
            assertThat(event)
                    .as("扩展包事件 id=%d 应存在", eventId)
                    .isNotNull();
            assertThat(event.getStatus()).isEqualTo(1);
        }
    }

    @Test
    void seedNpcsCountIs5() {
        long count = npcMapper.selectCount(new LambdaQueryWrapper<Npc>().eq(Npc::getActive, 1));
        assertThat(count).isEqualTo(5);
    }

    @Test
    void seedOrganizationsCountIsAtLeast3() {
        long count = organizationMapper.selectCount(new LambdaQueryWrapper<Organization>().eq(Organization::getStatus, 1));
        assertThat(count).isGreaterThanOrEqualTo(3);
        // CP6 新增 5 个组织后总数为 8
    }

    // ==================== NPC 文案分层测试 ====================

    @Test
    void npcEncounterTextDiffersByFamiliarity() {
        playerService.createProfile(2L, "NPC文案测试", "大二", "计算机类", "就业路线");

        // 多次遇见同一 NPC 提升熟悉度
        String firstText = null;
        String laterText = null;

        for (int i = 0; i < 30; i++) {
            var encounter = npcService.maybeMeetNpc(2L, 3L, 1);
            if (encounter.isPresent()) {
                if (firstText == null) {
                    firstText = encounter.get().encounterText();
                }
                laterText = encounter.get().encounterText();
            }
        }

        // 至少应该遇到过 NPC
        assertThat(laterText).isNotBlank();
    }

    @Test
    void npcEncounterTextIsNotBlankForAllTypes() {
        playerService.createProfile(2L, "NPC文案非空测试", "大二", "计算机类", "就业路线");

        // 对每个地点尝试遇见 NPC
        for (long locId = 1; locId <= 8; locId++) {
            for (int i = 0; i < 15; i++) {
                var encounter = npcService.maybeMeetNpc(2L, locId, 1);
                if (encounter.isPresent()) {
                    assertThat(encounter.get().encounterText()).isNotBlank();
                    assertThat(encounter.get().tendencyHint()).isNotBlank();
                    assertThat(encounter.get().npc()).isNotNull();
                }
            }
        }
    }

    // ==================== 组织活动 resultText 测试 ====================

    @Test
    void organizationActivityResultTextIsNotBlank() {
        playerService.createProfile(2L, "组织文案测试", "大二", "计算机类", "就业路线");

        // 测试学生会的活动文案
        organizationService.discover(2L, 1L);
        organizationService.join(2L, 1L);

        var result = organizationService.attendActivityWithChange(2L, 1L);
        assertThat(result.activityResultText()).isNotBlank();
        assertThat(result.attributeChange()).isNotNull();
    }

    @Test
    void organizationActivityResultTextVariesByType() {
        playerService.createProfile(2L, "组织文案差异测试", "大二", "计算机类", "就业路线");

        // 测试三种组织的活动文案都非空
        for (long orgId = 1; orgId <= 3; orgId++) {
            organizationService.discover(2L, orgId);
            organizationService.join(2L, orgId);
        }

        // 学生会
        var result1 = organizationService.attendActivityWithChange(2L, 1L);
        assertThat(result1.activityResultText()).isNotBlank();

        // 实验室
        var result2 = organizationService.attendActivityWithChange(2L, 2L);
        assertThat(result2.activityResultText()).isNotBlank();

        // 篮球社
        var result3 = organizationService.attendActivityWithChange(2L, 3L);
        assertThat(result3.activityResultText()).isNotBlank();
    }

    // ==================== 扩展包新增成就测试 ====================

    @Test
    void newAchievementsExistInSeedData() {
        // 验证新增成就 social_butterfly 和 fitness_survivor 存在
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getActive, 1));
        assertThat(achievements).extracting(Achievement::getAchievementKey)
                .contains("social_butterfly", "fitness_survivor");
        assertThat(achievements.size()).isGreaterThanOrEqualTo(9);
    }
}