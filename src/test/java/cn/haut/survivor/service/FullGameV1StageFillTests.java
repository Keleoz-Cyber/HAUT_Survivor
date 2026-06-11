package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.ExplorationStoryChainMapper;
import cn.haut.survivor.mapper.RumorMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
class FullGameV1StageFillTests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private ExplorationStoryChainMapper explorationStoryChainMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    // ===== midterm 阶段事件 (6-8 周) =====

    @Test
    void midtermEventsExistWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 7001L)
                .le(Event::getId, 7006L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "期中考试倒计时",
                        "图书馆复习撞车",
                        "实验报告截止提醒",
                        "高数小测突击",
                        "压力爆表求助信号",
                        "期中后成绩焦虑");

        // min_week/max_week 覆盖 midterm 阶段 (6-8)
        for (Event event : events) {
            assertThat(event.getMinWeek())
                    .as("event %d min_week should be >= 6", event.getId())
                    .isGreaterThanOrEqualTo(6);
            assertThat(event.getMaxWeek())
                    .as("event %d max_week should be <= 8", event.getId())
                    .isLessThanOrEqualTo(8);
        }

        // 每条事件至少 3 个选项
        for (Event event : events) {
            List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                    .eq(EventOption::getEventId, event.getId()));
            assertThat(options)
                    .as("event %d should have 3 options", event.getId())
                    .hasSize(3);
        }
    }

    // ===== route 阶段事件 (9-11 周) =====

    @Test
    void routeEventsExistWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 7007L)
                .le(Event::getId, 7012L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "考研信息分享会",
                        "实习招聘信息群",
                        "竞赛组队邀请",
                        "社团换届竞选",
                        "摆烂室友的诱惑",
                        "导师开放日");

        for (Event event : events) {
            assertThat(event.getMinWeek())
                    .as("event %d min_week should be >= 9", event.getId())
                    .isGreaterThanOrEqualTo(9);
            assertThat(event.getMaxWeek())
                    .as("event %d max_week should be <= 11", event.getId())
                    .isLessThanOrEqualTo(11);
        }

        for (Event event : events) {
            List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                    .eq(EventOption::getEventId, event.getId()));
            assertThat(options)
                    .as("event %d should have 3 options", event.getId())
                    .hasSize(3);
        }
    }

    // ===== project 阶段事件 (12-14 周) =====

    @Test
    void projectEventsExistWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 7013L)
                .le(Event::getId, 7018L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "Java 课设需求变更",
                        "小组作业划水危机",
                        "实验室服务器宕机",
                        "答辩 PPT 连夜赶",
                        "期末论文选题",
                        "DDL 三连预警");

        for (Event event : events) {
            assertThat(event.getMinWeek())
                    .as("event %d min_week should be >= 12", event.getId())
                    .isGreaterThanOrEqualTo(12);
            assertThat(event.getMaxWeek())
                    .as("event %d max_week should be <= 14", event.getId())
                    .isLessThanOrEqualTo(14);
        }

        for (Event event : events) {
            List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                    .eq(EventOption::getEventId, event.getId()));
            assertThat(options)
                    .as("event %d should have 3 options", event.getId())
                    .hasSize(3);
        }
    }

    // ===== CP6.4 事件迁移验证 =====

    @Test
    void cp64EventsMigratedToWeeks15And16() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6401L)
                .le(Event::getId, 6406L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        // CP6.4 事件应已迁移到 15-16 周
        for (Event event : events) {
            assertThat(event.getMinWeek())
                    .as("CP6.4 event %d should have min_week=15 after migration", event.getId())
                    .isEqualTo(15);
            assertThat(event.getMaxWeek())
                    .as("CP6.4 event %d should have max_week=16 after migration", event.getId())
                    .isEqualTo(16);
        }
    }

    // ===== 传闻验证 =====

    @Test
    void midtermRumorsExistWithValidEffectTypes() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 7001L)
                .le(Rumor::getId, 7004L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        for (Rumor rumor : rumors) {
            assertThat(rumor.getWeekNumber())
                    .as("midterm rumor %d week_number should be 6-8", rumor.getId())
                    .isBetween(6, 8);
            assertThat(rumor.getEffectType())
                    .as("rumor %d should have valid effect_type", rumor.getId())
                    .isIn("explore_bonus", "event_hint", "safe_zone", "attr_bonus");
            assertThat(rumor.getEffectTarget())
                    .as("rumor %d should have valid effect_target", rumor.getId())
                    .isIn("explore", "academic", "pressure", "money", "skill", "health", "social", "npc");
        }
    }

    @Test
    void routeRumorsExistWithValidEffectTypes() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 7005L)
                .le(Rumor::getId, 7008L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        for (Rumor rumor : rumors) {
            assertThat(rumor.getWeekNumber())
                    .as("route rumor %d week_number should be 9-11", rumor.getId())
                    .isBetween(9, 11);
            assertThat(rumor.getEffectType()).isIn("explore_bonus", "event_hint", "npc_boost", "attr_bonus");
        }
    }

    @Test
    void projectRumorsExistWithValidEffectTypes() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 7009L)
                .le(Rumor::getId, 7012L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        for (Rumor rumor : rumors) {
            assertThat(rumor.getWeekNumber())
                    .as("project rumor %d week_number should be 12-14", rumor.getId())
                    .isBetween(12, 14);
            assertThat(rumor.getEffectType()).isIn("event_hint", "safe_zone", "attr_bonus");
        }
    }

    @Test
    void finalRumorsExistWithValidEffectTypes() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 7013L)
                .le(Rumor::getId, 7016L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        for (Rumor rumor : rumors) {
            assertThat(rumor.getWeekNumber())
                    .as("final rumor %d week_number should be 15-16", rumor.getId())
                    .isBetween(15, 16);
            assertThat(rumor.getEffectType()).isIn("explore_bonus", "event_hint", "attr_bonus", "safe_zone");
        }
    }

    @Test
    void cp64RumorsMigratedToWeek15() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6401L)
                .le(Rumor::getId, 6404L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        for (Rumor rumor : rumors) {
            assertThat(rumor.getWeekNumber())
                    .as("CP6.4 rumor %d should have week_number=15 after migration", rumor.getId())
                    .isEqualTo(15);
        }
    }

    @Test
    void rumorsCoverWeeks6Through16() {
        // 验证 week 6-16 都有传闻覆盖
        List<Rumor> allNewRumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 7001L)
                .le(Rumor::getId, 7016L)
                .select(Rumor::getWeekNumber));

        List<Integer> coveredWeeks = allNewRumors.stream()
                .map(Rumor::getWeekNumber)
                .distinct()
                .sorted()
                .toList();

        // week 6-16 每周至少有一条传闻
        for (int w = 6; w <= 16; w++) {
            assertThat(coveredWeeks).as("week %d should have at least one rumor", w).contains(w);
        }
    }

    // ===== 奇遇链验证 =====

    @Test
    void midtermStoryChainExistsWithValidStepOrder() {
        List<ExplorationStoryChain> chains = explorationStoryChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .eq(ExplorationStoryChain::getChainKey, "midterm_review_route")
                .orderByAsc(ExplorationStoryChain::getStepNumber));

        assertThat(chains).hasSize(3);
        assertThat(chains).extracting(ExplorationStoryChain::getLocationId).containsOnly(2L);
        assertThat(chains).extracting(ExplorationStoryChain::getWeekNumber).containsOnly(6);

        // step 1 → step 2, step 2 → step 3, step 3 → null
        assertThat(chains.get(0).getNextStepNumber()).isEqualTo(2);
        assertThat(chains.get(1).getNextStepNumber()).isEqualTo(3);
        assertThat(chains.get(2).getNextStepNumber()).isNull();
    }

    @Test
    void routeCareerExpoChainExistsWithValidStepOrder() {
        List<ExplorationStoryChain> chains = explorationStoryChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .eq(ExplorationStoryChain::getChainKey, "route_career_expo")
                .orderByAsc(ExplorationStoryChain::getStepNumber));

        assertThat(chains).hasSize(3);
        assertThat(chains).extracting(ExplorationStoryChain::getLocationId).containsOnly(7L);
        assertThat(chains).extracting(ExplorationStoryChain::getWeekNumber).containsOnly(10);

        assertThat(chains.get(0).getNextStepNumber()).isEqualTo(2);
        assertThat(chains.get(1).getNextStepNumber()).isEqualTo(3);
        assertThat(chains.get(2).getNextStepNumber()).isNull();
    }

    @Test
    void projectDdlSurvivalChainExistsWithValidStepOrder() {
        List<ExplorationStoryChain> chains = explorationStoryChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .eq(ExplorationStoryChain::getChainKey, "project_ddl_survival")
                .orderByAsc(ExplorationStoryChain::getStepNumber));

        assertThat(chains).hasSize(3);
        assertThat(chains).extracting(ExplorationStoryChain::getLocationId).containsOnly(3L);
        assertThat(chains).extracting(ExplorationStoryChain::getWeekNumber).containsOnly(13);

        assertThat(chains.get(0).getNextStepNumber()).isEqualTo(2);
        assertThat(chains.get(1).getNextStepNumber()).isEqualTo(3);
        assertThat(chains.get(2).getNextStepNumber()).isNull();
    }

    @Test
    void cp64StoryChainsMigratedToWeek15() {
        List<ExplorationStoryChain> chains = explorationStoryChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .ge(ExplorationStoryChain::getId, 6401L)
                .le(ExplorationStoryChain::getId, 6406L)
                .orderByAsc(ExplorationStoryChain::getId));

        assertThat(chains).hasSize(6);
        for (ExplorationStoryChain chain : chains) {
            assertThat(chain.getWeekNumber())
                    .as("CP6.4 chain %d should have week_number=15 after migration", chain.getId())
                    .isEqualTo(15);
        }
    }

    // ===== 周目标验证 =====

    @Test
    void weeklyGoalsExistWithValidTypes() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .ge(WeeklyGoal::getId, 7001L)
                .le(WeeklyGoal::getId, 7006L)
                .orderByAsc(WeeklyGoal::getId));

        assertThat(goals).hasSize(6);
        assertThat(goals).extracting(WeeklyGoal::getGoalKey)
                .containsExactly(
                        "v1_midterm_exam_prep",
                        "v1_midterm_pressure_hold",
                        "v1_route_skill_up",
                        "v1_route_npc_connect",
                        "v1_project_dungeon_push",
                        "v1_project_skill_focus");
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .containsExactly("event", "pressure_hold", "explore", "npc_interaction", "dungeon_stage", "skill");
        assertThat(goals).allMatch(g -> g.getActive() == 1);
    }

    // ===== 成就验证 =====

    @Test
    void achievementsExistWithValidConditionTypes() {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .ge(Achievement::getId, 7001L)
                .le(Achievement::getId, 7006L)
                .orderByAsc(Achievement::getId));

        assertThat(achievements).hasSize(6);
        assertThat(achievements).extracting(Achievement::getAchievementKey)
                .containsExactly(
                        "v1_midterm_survivor",
                        "v1_exam_ready",
                        "v1_route_decided",
                        "v1_skill_master",
                        "v1_project_finisher",
                        "v1_semester_veteran");
        assertThat(achievements).extracting(Achievement::getConditionType)
                .containsExactly("explore_count", "event_count", "npc_interaction_count", "skill", "dungeon_completed", "explore_count");
        assertThat(achievements).allMatch(a -> a.getActive() == 1);
    }

    // ===== 全局覆盖验证 =====

    @Test
    void allStageEventsTotalEighteen() {
        List<Event> allV1Events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 7001L)
                .le(Event::getId, 7018L));

        assertThat(allV1Events).hasSize(18);
    }

    @Test
    void allStageRumorsTotalSixteen() {
        List<Rumor> allV1Rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 7001L)
                .le(Rumor::getId, 7016L));

        assertThat(allV1Rumors).hasSize(16);
    }

    @Test
    void allStoryChainsTotalNineSteps() {
        List<ExplorationStoryChain> allV1Chains = explorationStoryChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .ge(ExplorationStoryChain::getId, 7001L)
                .le(ExplorationStoryChain::getId, 7009L));

        assertThat(allV1Chains).hasSize(9);
        assertThat(allV1Chains).extracting(ExplorationStoryChain::getChainKey)
                .containsOnly("midterm_review_route", "route_career_expo", "project_ddl_survival");
    }
}
