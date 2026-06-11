package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.RumorMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CP6.3 开学迎新周 seed 数据验证：
 * - 事件 6301-6306 及选项
 * - 传闻 6301-6304（含 life 目标）
 * - 周目标和成就
 */
@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack6OpeningWeekTests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void cp63OpeningWeekEventsAreSeededWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6301L)
                .le(Event::getId, 6306L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "迎新路线导览",
                        "选课余量刷新",
                        "宿舍网络排查",
                        "校园卡绑定提醒",
                        "社团招新预热群",
                        "食堂错峰互助");
        assertThat(events).extracting(Event::getEventType)
                .contains("生活", "学习", "社交", "健康");
        assertThat(events).allMatch(event -> event.getMinWeek() == 1);
        assertThat(events).allMatch(event -> event.getMaxWeek() <= 2);
        assertThat(events).allMatch(event -> event.getStatus() == 1);

        List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                .ge(EventOption::getEventId, 6301L)
                .le(EventOption::getEventId, 6306L));

        Map<Long, Long> optionCountByEvent = options.stream()
                .collect(Collectors.groupingBy(EventOption::getEventId, Collectors.counting()));
        assertThat(optionCountByEvent).hasSize(6);
        assertThat(optionCountByEvent.values()).allMatch(count -> count == 3);
        assertThat(options).allMatch(option -> option.getPreviewText() != null && !option.getPreviewText().isBlank());
        assertThat(options).allMatch(option -> Set.of("low", "medium", "high").contains(option.getRiskLevel()));
        assertThat(options).anyMatch(option -> option.getSocialChange() > 0);
        assertThat(options).anyMatch(option -> option.getDisciplineChange() > 0);
        assertThat(options).anyMatch(option -> option.getPressureChange() < 0);
    }

    @Test
    void cp63RumorsUseExistingEffectTypesIncludingLifeHint() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6301L)
                .le(Rumor::getId, 6304L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .containsExactly(
                        "迎新导览队伍从韶华楼出发",
                        "博闻楼校园卡窗口早上更快",
                        "宿舍网络晚高峰前更容易修好",
                        "招新群里有人整理活动表");
        assertThat(rumors).extracting(Rumor::getEffectType)
                .contains("event_hint", "safe_zone", "explore_bonus", "npc_boost");
        assertThat(rumors).extracting(Rumor::getEffectTarget)
                .contains("life", "pressure", "explore", "npc");
        assertThat(rumors).allMatch(rumor -> rumor.getWeekNumber() == 1);
        assertThat(rumors).allMatch(rumor -> rumor.getActive() == 1);
    }

    @Test
    void cp63WeeklyGoalsAndAchievementsUseExistingConditionTypes() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "cp63_opening_week_route", "cp63_first_connections")
                .orderByAsc(WeeklyGoal::getId));

        assertThat(goals).hasSize(2);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .containsExactly("exploration", "npc_interaction");
        assertThat(goals).allMatch(goal -> goal.getTargetValue() != null && goal.getTargetValue() > 0);
        assertThat(goals).allMatch(goal -> goal.getActive() == 1);

        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey, "cp63_opening_week_survivor", "cp63_campus_affairs_stable")
                .orderByAsc(Achievement::getId));

        assertThat(achievements).hasSize(2);
        assertThat(achievements).extracting(Achievement::getConditionType)
                .containsExactly("exploration", "rumor_effect_used");
        assertThat(achievements).allMatch(achievement -> achievement.getActive() == 1);
    }
}
