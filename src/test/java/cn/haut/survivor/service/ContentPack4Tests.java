package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
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
class ContentPack4Tests {

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private ExplorationStoryChainMapper storyChainMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void mechanicRumorsAreSeeded() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 4001L)
                .le(Rumor::getId, 4016L));

        assertThat(rumors).hasSize(16);
        assertThat(rumors).extracting(Rumor::getEffectType)
                .contains("explore_bonus", "attr_bonus", "npc_boost", "safe_zone");
        assertThat(rumors).allMatch(r -> r.getEffectValue() != null);
    }

    @Test
    void explorationStoryChainsAreSeeded() {
        List<ExplorationStoryChain> chains = storyChainMapper.selectList(
                new LambdaQueryWrapper<ExplorationStoryChain>().eq(ExplorationStoryChain::getActive, 1));

        assertThat(chains).hasSizeGreaterThanOrEqualTo(12);
        assertThat(chains).extracting(ExplorationStoryChain::getChainKey)
                .contains("library_seat", "lab_whiteboard", "track_night_run", "canteen_gossip", "dorm_lights_out");
    }

    @Test
    void contentPack4WeeklyGoalsAreSeeded() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey,
                        "rumor_hunter", "story_chaser", "theme_survivor", "buddy_rescue"));

        assertThat(goals).hasSize(4);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .contains("rumor_effect_used", "exploration_story_step", "weekly_modifier_used", "buddy_assist");
    }

    @Test
    void contentPack4AchievementsAreSeeded() {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey,
                        "first_rumor_effect", "story_first_step", "story_completed", "theme_master", "buddy_saved_me"));

        assertThat(achievements).hasSize(5);
        assertThat(achievements).extracting(Achievement::getRewardTitle)
                .contains("情报新生", "校园目击者", "支线清理大师", "节奏感选手", "有人罩着");
    }
}