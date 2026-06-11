package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.DungeonTaskOption;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.DungeonMapper;
import cn.haut.survivor.mapper.DungeonTaskMapper;
import cn.haut.survivor.mapper.DungeonTaskOptionMapper;
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
class ContentPack6FinalWeekTests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private ExplorationStoryChainMapper explorationStoryChainMapper;

    @Autowired
    private DungeonMapper dungeonMapper;

    @Autowired
    private DungeonTaskMapper dungeonTaskMapper;

    @Autowired
    private DungeonTaskOptionMapper dungeonTaskOptionMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void cp64FinalWeekEventsAreSeededWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6401L)
                .le(Event::getId, 6406L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "图书馆闭馆前冲刺",
                        "考前重点互认",
                        "体测前热身队列",
                        "1000 米配速选择",
                        "清淡补给窗口",
                        "宿舍早睡协议");
        assertThat(events).extracting(Event::getLocationId)
                .containsExactly(2L, 2L, 5L, 5L, 4L, 3L);
        assertThat(events).extracting(Event::getEventType)
                .contains("学习", "健康", "生活");

        for (Event event : events) {
            List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                    .eq(EventOption::getEventId, event.getId()));
            assertThat(options)
                    .as("event " + event.getId() + " should have 3 options")
                    .hasSize(3);
        }
    }

    @Test
    void cp64RumorsUseExistingEffectTypesAndTargets() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6401L)
                .le(Rumor::getId, 6404L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        assertThat(rumors).extracting(Rumor::getEffectType)
                .containsExactly("event_hint", "explore_bonus", "safe_zone", "attr_bonus");
        assertThat(rumors).extracting(Rumor::getEffectTarget)
                .containsExactly("health", "explore", "pressure", "academic");
        assertThat(rumors).allMatch(rumor -> rumor.getWeekNumber() == 15);
    }

    @Test
    void cp64StoryChainsCoverLibraryReviewAndPhysicalTestRoutes() {
        List<ExplorationStoryChain> chains = explorationStoryChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .ge(ExplorationStoryChain::getId, 6401L)
                .le(ExplorationStoryChain::getId, 6406L)
                .orderByAsc(ExplorationStoryChain::getChainKey)
                .orderByAsc(ExplorationStoryChain::getStepNumber));

        assertThat(chains).hasSize(6);
        assertThat(chains).extracting(ExplorationStoryChain::getChainKey)
                .containsOnly("final_library_review", "physical_test_route");
        assertThat(chains).filteredOn(chain -> "final_library_review".equals(chain.getChainKey()))
                .extracting(ExplorationStoryChain::getLocationId)
                .containsOnly(2L);
        assertThat(chains).filteredOn(chain -> "physical_test_route".equals(chain.getChainKey()))
                .extracting(ExplorationStoryChain::getLocationId)
                .containsOnly(5L);
        assertThat(chains).filteredOn(chain -> chain.getStepNumber() == 3)
                .allMatch(chain -> chain.getNextStepNumber() == null);
    }

    @Test
    void cp64PhysicalTestDungeonIsSeededWithOrderedStages() {
        Dungeon dungeon = dungeonMapper.selectById(6401L);

        assertThat(dungeon).isNotNull();
        assertThat(dungeon.getDungeonName()).isEqualTo("体测生存挑战");
        assertThat(dungeon.getDungeonType()).isEqualTo("physical");
        assertThat(dungeon.getRewardTitle()).isEqualTo("体测通关者");

        List<DungeonTask> tasks = dungeonTaskMapper.selectList(new LambdaQueryWrapper<DungeonTask>()
                .eq(DungeonTask::getDungeonId, 6401L)
                .orderByAsc(DungeonTask::getTaskOrder));
        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(DungeonTask::getTaskName)
                .containsExactly("1000 米配速", "引体向上排队", "坐位体前屈补救");

        for (DungeonTask task : tasks) {
            List<DungeonTaskOption> options = dungeonTaskOptionMapper.selectList(new LambdaQueryWrapper<DungeonTaskOption>()
                    .eq(DungeonTaskOption::getDungeonTaskId, task.getId()));
            assertThat(options)
                    .as("task " + task.getId() + " should have 3 options")
                    .hasSize(3);
        }
    }

    @Test
    void cp64WeeklyGoalsAndAchievementsUseExistingConditionTypes() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "cp64_final_review_route", "cp64_physical_test_push")
                .orderByAsc(WeeklyGoal::getId));
        assertThat(goals).hasSize(2);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .containsExactly("exploration_story_step", "dungeon_stage");

        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey, "cp64_final_week_stable", "cp64_physical_test_started")
                .orderByAsc(Achievement::getId));
        assertThat(achievements).hasSize(2);
        assertThat(achievements).extracting(Achievement::getConditionType)
                .containsExactly("exploration_story_step", "dungeon_stage");
    }
}
