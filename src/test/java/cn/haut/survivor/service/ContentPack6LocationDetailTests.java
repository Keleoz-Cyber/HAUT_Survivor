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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CP6.2 莲花街校区地点细分 seed 数据验证：
 * - 事件 6201-6210 及选项
 * - 传闻 6201-6206
 * - 探索奇遇链 6201-6212
 * - 周目标和成就
 */
@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack6LocationDetailTests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private ExplorationStoryChainMapper storyChainMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void cp62LocationEventsAreSeededWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6201L)
                .le(Event::getId, 6210L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(10);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "图书馆电梯口排队",
                        "八楼 TP 区空位",
                        "惟学楼实验课临时换机房",
                        "惟学楼报告厅临时讲座",
                        "博闻楼营业厅补卡",
                        "博闻楼网络账号申诉",
                        "韶华楼前社团摊位",
                        "韶华楼走廊展示板",
                        "知味餐厅饭点错峰",
                        "知雅餐厅拼桌");
        assertThat(events).extracting(Event::getLocationId)
                .contains(2L, 6L, 8L, 7L, 4L);
        assertThat(events).allMatch(event -> event.getProbability() >= 25 && event.getProbability() <= 75);
        assertThat(events).allMatch(event -> event.getMinWeek() >= 1 && event.getMaxWeek() <= 20);
        assertThat(events).allMatch(event -> event.getStatus() == 1);

        List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                .ge(EventOption::getEventId, 6201L)
                .le(EventOption::getEventId, 6210L));

        Map<Long, Long> optionCountByEvent = options.stream()
                .collect(Collectors.groupingBy(EventOption::getEventId, Collectors.counting()));
        assertThat(optionCountByEvent).hasSize(10);
        assertThat(optionCountByEvent.values()).allMatch(count -> count >= 3);
        assertThat(options).allMatch(option -> option.getPreviewText() != null && !option.getPreviewText().isBlank());
        assertThat(options).allMatch(option -> Set.of("low", "medium", "high").contains(option.getRiskLevel()));
        assertThat(options).anyMatch(option -> option.getAcademicChange() > 0);
        assertThat(options).anyMatch(option -> option.getHealthChange() > 0);
        assertThat(options).anyMatch(option -> option.getSocialChange() > 0);
        assertThat(options).anyMatch(option -> option.getSkillChange() > 0);
        assertThat(options).anyMatch(option -> option.getPressureChange() < 0);
    }

    @Test
    void cp62RumorsUseExistingEffectTypesAndTargets() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6201L)
                .le(Rumor::getId, 6206L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(6);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .contains(
                        "图书馆八楼上午空位多",
                        "惟学楼报告厅有临时讲座",
                        "博闻楼营业厅午后人少",
                        "知味餐厅二楼错峰更稳");
        assertThat(rumors).extracting(Rumor::getEffectType)
                .contains("explore_bonus", "event_hint", "safe_zone", "npc_boost", "attr_bonus");
        assertThat(rumors).allMatch(rumor -> Set.of(
                "attr_bonus", "npc_boost", "explore_bonus", "safe_zone", "event_hint"
        ).contains(rumor.getEffectType()));
        assertThat(rumors).allMatch(rumor -> Set.of(
                "academic", "health", "money", "social", "skill", "pressure", "discipline", "explore", "npc"
        ).contains(rumor.getEffectTarget()));
        assertThat(rumors).allMatch(rumor -> rumor.getEffectValue() != null && rumor.getEffectValue() > 0);
        assertThat(rumors).allMatch(rumor -> rumor.getActive() == 1);
    }

    @Test
    void cp62ExplorationStoryChainsHaveOrderedStepsAndValidNextSteps() {
        List<ExplorationStoryChain> chains = storyChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .ge(ExplorationStoryChain::getId, 6201L)
                .le(ExplorationStoryChain::getId, 6212L)
                .orderByAsc(ExplorationStoryChain::getId));

        assertThat(chains).hasSize(12);
        assertThat(chains).extracting(ExplorationStoryChain::getChainKey)
                .contains(
                        "library_floor_trace",
                        "weixue_lab_route",
                        "bowen_service_window",
                        "canteen_peak_shift");
        assertThat(chains).allMatch(chain -> chain.getActive() == 1);
        assertThat(chains).allMatch(chain -> chain.getLocationId() >= 1L && chain.getLocationId() <= 8L);
        assertThat(chains).allMatch(chain -> chain.getRequiredExploreLevel() >= 0);

        Map<String, List<ExplorationStoryChain>> byChainKey = chains.stream()
                .collect(Collectors.groupingBy(ExplorationStoryChain::getChainKey));
        assertThat(byChainKey).hasSize(4);
        byChainKey.values().forEach(steps -> {
            List<Integer> stepNumbers = steps.stream()
                    .map(ExplorationStoryChain::getStepNumber)
                    .sorted()
                    .toList();
            assertThat(stepNumbers).containsExactly(1, 2, 3);
            assertThat(steps).anyMatch(step -> Integer.valueOf(2).equals(step.getNextStepNumber()));
            assertThat(steps).anyMatch(step -> Integer.valueOf(3).equals(step.getNextStepNumber()));
            assertThat(steps).anyMatch(step -> step.getNextStepNumber() == null);
        });

        assertThat(chains).anyMatch(chain -> chain.getAcademicChange() > 0);
        assertThat(chains).anyMatch(chain -> chain.getSkillChange() > 0);
        assertThat(chains).anyMatch(chain -> chain.getSocialChange() > 0);
        assertThat(chains).anyMatch(chain -> chain.getPressureChange() < 0);
    }

    @Test
    void cp62WeeklyGoalsAndAchievementsUseExistingConditionTypes() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "cp62_location_story_chaser", "cp62_rumor_route_planner")
                .orderByAsc(WeeklyGoal::getId));

        assertThat(goals).hasSize(2);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .containsExactly("exploration_story_step", "rumor_effect_used");
        assertThat(goals).allMatch(goal -> goal.getTargetValue() != null && goal.getTargetValue() > 0);
        assertThat(goals).allMatch(goal -> goal.getRewardExp() != null && goal.getRewardExp() > 0);
        assertThat(goals).allMatch(goal -> goal.getActive() == 1);

        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey, "cp62_location_mapper", "cp62_rumor_commuter")
                .orderByAsc(Achievement::getId));

        assertThat(achievements).hasSize(2);
        assertThat(achievements).extracting(Achievement::getConditionType)
                .containsExactly("exploration_story_step", "rumor_effect_used");
        assertThat(achievements).allMatch(achievement -> achievement.getConditionValue() != null
                && achievement.getConditionValue() > 0);
        assertThat(achievements).allMatch(achievement -> achievement.getRewardTitle() != null
                && !achievement.getRewardTitle().isBlank());
        assertThat(achievements).allMatch(achievement -> achievement.getActive() == 1);
    }
}
