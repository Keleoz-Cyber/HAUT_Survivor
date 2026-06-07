package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.NpcInteractionMapper;
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
class ContentPack3Tests {

    @Autowired
    private NpcInteractionMapper npcInteractionMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Test
    void npcInteractionsSeeded() {
        List<NpcInteraction> interactions = npcInteractionMapper.selectList(
                new LambdaQueryWrapper<NpcInteraction>().eq(NpcInteraction::getActive, 1));

        assertThat(interactions).hasSizeGreaterThanOrEqualTo(15);
        assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
                .contains(
                        "ajie_night_talk",
                        "linran_study",
                        "zhouyu_news",
                        "laozheng_debug",
                        "xiaoma_jog");
    }

    @Test
    void npcBuddyWeeklyGoalsSeeded() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey,
                        "buddy_chat",
                        "buddy_week",
                        "relationship_builder"));

        assertThat(goals).hasSize(3);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .contains("npc_interaction", "buddy_selected", "familiarity_gain");
    }

    @Test
    void npcBuddyAchievementsSeeded() {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey,
                        "first_buddy",
                        "iron_buddy",
                        "social_web",
                        "study_partner",
                        "lab_apprentice"));

        assertThat(achievements).hasSize(5);
        assertThat(achievements).extracting(Achievement::getRewardTitle)
                .contains("有搭子的人", "默契满分", "到哪都有熟人", "自习室常驻队友", "师兄认证");
    }

    @Test
    void npcBuddyRumorsSeeded() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 3001L)
                .le(Rumor::getId, 3010L));

        assertThat(rumors).hasSizeGreaterThanOrEqualTo(8);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .contains("阿杰的拼单邀请", "林然的固定座位", "老郑的白板建议");
    }
}
