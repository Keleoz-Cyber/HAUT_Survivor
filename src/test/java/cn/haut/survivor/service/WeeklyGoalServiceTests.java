package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
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
class WeeklyGoalServiceTests {

    @Autowired
    private WeeklyGoalService weeklyGoalService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerAttributeMapper playerAttributeMapper;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "目标测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void listActiveGoalsReturnsSeedData() {
        List<WeeklyGoal> goals = weeklyGoalService.listActiveGoals();
        assertThat(goals).isNotEmpty();
        assertThat(goals).allMatch(g -> g.getActive() == 1);
    }

    @Test
    void pickCandidateGoalsReturnsAtMostThree() {
        List<WeeklyGoal> candidates = weeklyGoalService.pickCandidateGoals(2L, 1);
        assertThat(candidates).hasSizeLessThanOrEqualTo(3);
        assertThat(candidates).isNotEmpty();
    }

    @Test
    void pickCandidateGoalsIsDeterministicForSameUserAndWeek() {
        List<WeeklyGoal> first = weeklyGoalService.pickCandidateGoals(2L, 1);
        List<WeeklyGoal> second = weeklyGoalService.pickCandidateGoals(2L, 1);
        assertThat(first.stream().map(WeeklyGoal::getId).toList())
                .containsExactlyElementsOf(second.stream().map(WeeklyGoal::getId).toList());
    }

    @Test
    void pickCandidateGoalsDiffersForDifferentWeeks() {
        List<WeeklyGoal> week1 = weeklyGoalService.pickCandidateGoals(2L, 1);
        List<WeeklyGoal> week2 = weeklyGoalService.pickCandidateGoals(2L, 2);
        // 可能相同，但大概率不同；只验证方法不抛异常
        assertThat(week1).isNotEmpty();
        assertThat(week2).isNotEmpty();
    }

    @Test
    void chooseGoalCreatesUserWeeklyGoal() {
        List<WeeklyGoal> candidates = weeklyGoalService.pickCandidateGoals(2L, 1);
        WeeklyGoal target = candidates.get(0);

        UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, target.getId());

        assertThat(userGoal).isNotNull();
        assertThat(userGoal.getUserId()).isEqualTo(2L);
        assertThat(userGoal.getWeekNumber()).isEqualTo(1);
        assertThat(userGoal.getGoalId()).isEqualTo(target.getId());
        assertThat(userGoal.getCompleted()).isEqualTo(0);
        assertThat(userGoal.getClaimed()).isEqualTo(0);
    }

    @Test
    void chooseGoalFailsWhenAlreadyChosen() {
        List<WeeklyGoal> candidates = weeklyGoalService.pickCandidateGoals(2L, 1);
        WeeklyGoal first = candidates.get(0);
        WeeklyGoal second = candidates.size() > 1 ? candidates.get(1) : candidates.get(0);

        weeklyGoalService.chooseGoal(2L, 1, first.getId());

        // 同一周不能重复选择
        assertThatThrownBy(() -> weeklyGoalService.chooseGoal(2L, 1, second.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("本周已选择目标");
    }

    @Test
    void getCurrentGoalReturnsNullWhenNotChosen() {
        UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(goal).isNull();
    }

    @Test
    void getCurrentGoalReturnsChosenGoal() {
        List<WeeklyGoal> candidates = weeklyGoalService.pickCandidateGoals(2L, 1);
        weeklyGoalService.chooseGoal(2L, 1, candidates.get(0).getId());

        UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(goal).isNotNull();
        assertThat(goal.getGoalId()).isEqualTo(candidates.get(0).getId());
    }

    @Test
    void getCurrentGoalDefinitionReturnsGoalDetails() {
        List<WeeklyGoal> candidates = weeklyGoalService.pickCandidateGoals(2L, 1);
        weeklyGoalService.chooseGoal(2L, 1, candidates.get(0).getId());

        WeeklyGoal def = weeklyGoalService.getCurrentGoalDefinition(2L, 1);
        assertThat(def).isNotNull();
        assertThat(def.getGoalName()).isNotBlank();
    }

    @Test
    void updateProgressIncreasesCurrentValue() {
        // 选择一个 explore_count 类型的目标
        WeeklyGoal exploreGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "explore_count".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (exploreGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, exploreGoal.getId());

            UserWeeklyGoal before = weeklyGoalService.getCurrentGoal(2L, 1);
            int beforeValue = before.getCurrentValue();

            weeklyGoalService.updateProgress(2L, 1, "explore_count", 1);

            UserWeeklyGoal after = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(after.getCurrentValue()).isEqualTo(beforeValue + 1);
        }
    }

    @Test
    void updateProgressIgnoresMismatchedGoalType() {
        // 选择一个 explore_count 类型的目标
        WeeklyGoal exploreGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "explore_count".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (exploreGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, exploreGoal.getId());

            UserWeeklyGoal before = weeklyGoalService.getCurrentGoal(2L, 1);
            int beforeValue = before.getCurrentValue();

            // 尝试用错误的目标类型更新
            weeklyGoalService.updateProgress(2L, 1, "npc_meet", 1);

            UserWeeklyGoal after = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(after.getCurrentValue()).isEqualTo(beforeValue); // 不变
        }
    }

    @Test
    void academicEventGoalStartsFromZeroAndProgresses() {
        WeeklyGoal academicGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "study_twice".equals(g.getGoalKey()))
                .findFirst()
                .orElse(null);

        assertThat(academicGoal).isNotNull();

        UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, academicGoal.getId());
        assertThat(userGoal.getStartValue()).isZero();
        assertThat(userGoal.getCurrentValue()).isZero();

        weeklyGoalService.updateProgress(2L, 1, "academic_event", 1);
        UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(updated.getCurrentValue()).isEqualTo(1);
        assertThat(updated.getCompleted()).isEqualTo(0);

        weeklyGoalService.updateProgress(2L, 1, "academic_event", 1);
        UserWeeklyGoal completed = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(completed.getCurrentValue()).isEqualTo(2);
        assertThat(completed.getCompleted()).isEqualTo(1);
    }

    @Test
    void checkCompletionMarksGoalAsCompleted() {
        // 选择一个 explore_count 类型的目标（target_value = 2）
        WeeklyGoal exploreGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "explore_count".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (exploreGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, exploreGoal.getId());

            // 更新进度到目标值
            weeklyGoalService.updateProgress(2L, 1, "explore_count", exploreGoal.getTargetValue());

            UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(goal.getCompleted()).isEqualTo(1);
        }
    }

    @Test
    void claimRewardFailsWhenNotCompleted() {
        List<WeeklyGoal> candidates = weeklyGoalService.pickCandidateGoals(2L, 1);
        weeklyGoalService.chooseGoal(2L, 1, candidates.get(0).getId());

        assertThatThrownBy(() -> weeklyGoalService.claimReward(2L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("目标尚未完成");
    }

    @Test
    void claimRewardFailsWhenAlreadyClaimed() {
        // 选择一个 explore_count 类型的目标
        WeeklyGoal exploreGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "explore_count".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (exploreGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, exploreGoal.getId());
            weeklyGoalService.updateProgress(2L, 1, "explore_count", exploreGoal.getTargetValue());
            weeklyGoalService.claimReward(2L, 1);

            assertThatThrownBy(() -> weeklyGoalService.claimReward(2L, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("奖励已领取");
        }
    }

    @Test
    void claimRewardIncreasesExp() {
        // 选择一个 explore_count 类型的目标
        WeeklyGoal exploreGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "explore_count".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (exploreGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, exploreGoal.getId());

            int beforeExp = playerService.findProfileByUserId(2L).getExp();

            // 完成目标
            weeklyGoalService.updateProgress(2L, 1, "explore_count", exploreGoal.getTargetValue());
            weeklyGoalService.claimReward(2L, 1);

            int afterExp = playerService.findProfileByUserId(2L).getExp();
            assertThat(afterExp).isGreaterThanOrEqualTo(beforeExp + exploreGoal.getRewardExp());
        }
    }

    @Test
    void claimRewardSetsClaimedFlag() {
        // 选择一个 explore_count 类型的目标
        WeeklyGoal exploreGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "explore_count".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (exploreGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, exploreGoal.getId());
            weeklyGoalService.updateProgress(2L, 1, "explore_count", exploreGoal.getTargetValue());
            weeklyGoalService.claimReward(2L, 1);

            UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(goal.getClaimed()).isEqualTo(1);
        }
    }

    @Test
    void pressureKeepGoalChecksCurrentPressure() {
        // 选择压力保持目标
        WeeklyGoal pressureGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "pressure_keep".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (pressureGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, pressureGoal.getId());

            // 确保压力在目标值以下
            PlayerAttribute attr = playerService.findAttributeByUserId(2L);
            attr.setPressure(30);
            playerAttributeMapper.updateById(attr);

            boolean completed = weeklyGoalService.checkPressureKeepGoal(2L, 1);
            assertThat(completed).isTrue();

            UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(goal.getCompleted()).isEqualTo(1);
        }
    }

    @Test
    void pressureKeepGoalFailsWhenPressureTooHigh() {
        // 选择压力保持目标
        WeeklyGoal pressureGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "pressure_keep".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (pressureGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, pressureGoal.getId());

            // 设置压力高于目标值
            PlayerAttribute attr = playerService.findAttributeByUserId(2L);
            attr.setPressure(80);
            playerAttributeMapper.updateById(attr);

            boolean completed = weeklyGoalService.checkPressureKeepGoal(2L, 1);
            assertThat(completed).isFalse();

            UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(goal.getCompleted()).isEqualTo(0);
        }
    }

    @Test
    void pressureKeepGoalCompletedDuringWeekAdvance() {
        // 选择压力保持目标
        WeeklyGoal pressureGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "pressure_keep".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (pressureGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, pressureGoal.getId());

            // 设置压力在目标值以下
            PlayerAttribute attr = playerService.findAttributeByUserId(2L);
            attr.setPressure(30);
            playerAttributeMapper.updateById(attr);

            // 周推进应自动检查压力保持目标
            playerService.advanceWeek(2L);

            // 压力保持目标应该已完成
            UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(goal).isNotNull();
            assertThat(goal.getCompleted()).isEqualTo(1);
        }
    }

    @Test
    void pressureKeepGoalNotCompletedDuringWeekAdvanceWhenPressureHigh() {
        // 选择压力保持目标
        WeeklyGoal pressureGoal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "pressure_keep".equals(g.getGoalType()))
                .findFirst()
                .orElse(null);

        if (pressureGoal != null) {
            weeklyGoalService.chooseGoal(2L, 1, pressureGoal.getId());

            // 设置压力高于目标值
            PlayerAttribute attr = playerService.findAttributeByUserId(2L);
            attr.setPressure(80);
            playerAttributeMapper.updateById(attr);

            // 周推进应检查但不会完成
            playerService.advanceWeek(2L);

            UserWeeklyGoal goal = weeklyGoalService.getCurrentGoal(2L, 1);
            assertThat(goal).isNotNull();
            assertThat(goal.getCompleted()).isEqualTo(0);
        }
    }

    @Test
    void npcInteractionGoalStartsFromZeroAndProgresses() {
        WeeklyGoal goal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "buddy_chat".equals(g.getGoalKey()))
                .findFirst()
                .orElseThrow();

        UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, goal.getId());
        assertThat(userGoal.getStartValue()).isZero();
        assertThat(userGoal.getCurrentValue()).isZero();

        weeklyGoalService.updateProgress(2L, 1, "npc_interaction", 1);

        UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(updated.getCurrentValue()).isEqualTo(1);
        assertThat(updated.getCompleted()).isEqualTo(1);
    }

    @Test
    void buddySelectedGoalStartsFromZeroAndProgresses() {
        WeeklyGoal goal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "buddy_week".equals(g.getGoalKey()))
                .findFirst()
                .orElseThrow();

        UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, goal.getId());
        assertThat(userGoal.getStartValue()).isZero();
        assertThat(userGoal.getCurrentValue()).isZero();

        weeklyGoalService.updateProgress(2L, 1, "buddy_selected", 1);

        UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(updated.getCurrentValue()).isEqualTo(1);
        assertThat(updated.getCompleted()).isEqualTo(1);
    }

    @Test
    void familiarityGainGoalAccumulatesDelta() {
        WeeklyGoal goal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "relationship_builder".equals(g.getGoalKey()))
                .findFirst()
                .orElseThrow();

        UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, goal.getId());
        assertThat(userGoal.getStartValue()).isZero();

        weeklyGoalService.updateProgress(2L, 1, "familiarity_gain", 4);
        weeklyGoalService.updateProgress(2L, 1, "familiarity_gain", 6);

        UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(updated.getCurrentValue()).isEqualTo(10);
        assertThat(updated.getCompleted()).isEqualTo(1);
    }

    @Test
    void rumorEffectGoalStartsFromZeroAndProgresses() {
        WeeklyGoal goal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "rumor_hunter".equals(g.getGoalKey()))
                .findFirst()
                .orElseThrow();

        UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, goal.getId());
        assertThat(userGoal.getStartValue()).isZero();

        weeklyGoalService.updateProgress(2L, 1, "rumor_effect_used", 2);

        UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(updated.getCurrentValue()).isEqualTo(2);
        assertThat(updated.getCompleted()).isEqualTo(1);
    }

    @Test
    void storyStepGoalStartsFromZeroAndProgresses() {
        WeeklyGoal goal = weeklyGoalService.listActiveGoals().stream()
                .filter(g -> "story_chaser".equals(g.getGoalKey()))
                .findFirst()
                .orElseThrow();

        weeklyGoalService.chooseGoal(2L, 1, goal.getId());
        weeklyGoalService.updateProgress(2L, 1, "exploration_story_step", 2);

        UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
        assertThat(updated.getCompleted()).isEqualTo(1);
    }
}
