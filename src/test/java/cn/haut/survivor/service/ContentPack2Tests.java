package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.DungeonMapper;
import cn.haut.survivor.mapper.DungeonTaskMapper;
import cn.haut.survivor.mapper.EventMapper;
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
class ContentPack2Tests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private DungeonMapper dungeonMapper;

    @Autowired
    private DungeonTaskMapper dungeonTaskMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Test
    void academicCrisisEventsSeeded() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .eq(Event::getEventType, "academic_crisis")
                .eq(Event::getStatus, 1));

        assertThat(events).hasSizeGreaterThanOrEqualTo(20);
        assertThat(events).extracting(Event::getEventName)
                .contains("早八点名危机", "Git 合并地狱", "考前抱佛脚");
    }

    @Test
    void databaseDefenseDungeonSeededWithFourStages() {
        Dungeon dungeon = dungeonMapper.selectOne(new LambdaQueryWrapper<Dungeon>()
                .eq(Dungeon::getDungeonName, "数据库课设答辩夜")
                .last("LIMIT 1"));

        assertThat(dungeon).isNotNull();
        assertThat(dungeon.getStatus()).isEqualTo(1);

        List<DungeonTask> tasks = dungeonTaskMapper.selectList(new LambdaQueryWrapper<DungeonTask>()
                .eq(DungeonTask::getDungeonId, dungeon.getId())
                .eq(DungeonTask::getStatus, 1)
                .orderByAsc(DungeonTask::getTaskOrder));

        assertThat(tasks).hasSize(4);
        assertThat(tasks).extracting(DungeonTask::getTaskName)
                .containsExactly("需求梳理", "ER 图连线", "SQL 暴走", "答辩现场");
    }

    @Test
    void academicCrisisWeeklyGoalsSeeded() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "study_twice", "ddl_survivor", "keep_calm_exam", "ask_for_help"));

        assertThat(goals).hasSize(4);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .contains("academic_event", "dungeon_stage", "pressure_keep", "npc_meet");
    }

    @Test
    void academicCrisisAchievementsSeeded() {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey,
                        "early_class_warrior",
                        "ddl_survivor_plus",
                        "last_minute_master",
                        "calm_under_pressure",
                        "help_seeker"));

        assertThat(achievements).hasSize(5);
        assertThat(achievements).extracting(Achievement::getRewardTitle)
                .contains("早八幸存者", "表结构守夜人", "考前冲刺型选手", "情绪稳定大师", "不单打独斗");
    }

    @Test
    void academicCrisisRumorsSeeded() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 2001L)
                .le(Rumor::getId, 2012L));

        assertThat(rumors).hasSizeGreaterThanOrEqualTo(8);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .contains("三楼靠窗复习位", "老郑的合并忠告", "阿杰的开黑陷阱");
    }
}
