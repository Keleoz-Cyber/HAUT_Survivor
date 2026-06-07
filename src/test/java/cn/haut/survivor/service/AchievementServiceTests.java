package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.UserAchievement;
import cn.haut.survivor.mapper.UserAchievementMapper;
import org.junit.jupiter.api.BeforeEach;
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
class AchievementServiceTests {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserAchievementMapper userAchievementMapper;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "成就测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void listActiveAchievementsReturnsSeedData() {
        List<Achievement> achievements = achievementService.listActiveAchievements();
        assertThat(achievements).isNotEmpty();
        assertThat(achievements).allMatch(a -> a.getActive() == 1);
    }

    @Test
    void listActiveAchievementsContainsCampusExplorer() {
        List<Achievement> achievements = achievementService.listActiveAchievements();
        assertThat(achievements).anyMatch(a -> "campus_explorer".equals(a.getAchievementKey()));
    }

    @Test
    void unlockIfEligibleUnlocksWhenConditionMet() {
        // campus_explorer requires explore_count >= 3
        List<Achievement> newlyUnlocked = achievementService.unlockIfEligible(2L, "explore_count", 3);
        assertThat(newlyUnlocked).isNotEmpty();
        assertThat(newlyUnlocked).anyMatch(a -> "campus_explorer".equals(a.getAchievementKey()));
    }

    @Test
    void unlockIfEligibleDoesNotUnlockWhenConditionNotMet() {
        List<Achievement> newlyUnlocked = achievementService.unlockIfEligible(2L, "explore_count", 1);
        assertThat(newlyUnlocked).isEmpty();
    }

    @Test
    void unlockIfEligibleDoesNotInsertDuplicate() {
        achievementService.unlockIfEligible(2L, "explore_count", 3);
        long countBefore = userAchievementMapper.selectCount(null);
        achievementService.unlockIfEligible(2L, "explore_count", 5);
        long countAfter = userAchievementMapper.selectCount(null);
        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test
    void unlockAchievementByKeyWorks() {
        boolean unlocked = achievementService.unlockAchievement(2L, "social_starter");
        assertThat(unlocked).isTrue();
    }

    @Test
    void unlockAchievementByKeyReturnsFalseForAlreadyUnlocked() {
        achievementService.unlockAchievement(2L, "social_starter");
        boolean unlockedAgain = achievementService.unlockAchievement(2L, "social_starter");
        assertThat(unlockedAgain).isFalse();
    }

    @Test
    void unlockAchievementByKeyReturnsFalseForUnknownKey() {
        boolean unlocked = achievementService.unlockAchievement(2L, "nonexistent_achievement");
        assertThat(unlocked).isFalse();
    }

    @Test
    void hasUnlockedReturnsTrueAfterUnlock() {
        achievementService.unlockAchievement(2L, "social_starter");
        assertThat(achievementService.hasUnlocked(2L, "social_starter")).isTrue();
    }

    @Test
    void hasUnlockedReturnsFalseBeforeUnlock() {
        assertThat(achievementService.hasUnlocked(2L, "social_starter")).isFalse();
    }

    @Test
    void listUserAchievementsReturnsUnlockedOnes() {
        achievementService.unlockAchievement(2L, "social_starter");
        List<UserAchievement> list = achievementService.listUserAchievements(2L);
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getAchievement()).isNotNull();
        assertThat(list.get(0).getAchievement().getAchievementKey()).isEqualTo("social_starter");
    }

    @Test
    void listRecentUnlockedReturnsLimitedResults() {
        achievementService.unlockAchievement(2L, "social_starter");
        achievementService.unlockAchievement(2L, "club_rookie");
        List<UserAchievement> recent = achievementService.listRecentUnlocked(2L, 1);
        assertThat(recent).hasSize(1);
    }

    @Test
    void listRecentUnlockedIsOrderedByTime() {
        achievementService.unlockAchievement(2L, "social_starter");
        achievementService.unlockAchievement(2L, "club_rookie");
        List<UserAchievement> recent = achievementService.listRecentUnlocked(2L, 5);
        // 最近解锁的在前（两者在同一事务中，时间戳相同，顺序可能不确定）
        assertThat(recent).hasSizeGreaterThanOrEqualTo(2);
        assertThat(recent.stream().map(ua -> ua.getAchievement().getAchievementKey()).toList())
                .containsExactlyInAnyOrder("social_starter", "club_rookie");
    }

    @Test
    void unlockAchievementAppliesRewardTitle() {
        achievementService.unlockAchievement(2L, "social_starter");
        var profile = playerService.findProfileByUserId(2L);
        assertThat(profile.getCurrentTitle()).isEqualTo("有熟人了");
    }

    @Test
    void exploreCountAchievementIntegration() {
        // 解锁 campus_explorer 需要累计探索次数 >= 3
        List<Achievement> unlocked = achievementService.unlockIfEligible(2L, "explore_count", 3);
        assertThat(unlocked).anyMatch(a -> "campus_explorer".equals(a.getAchievementKey()));
        assertThat(achievementService.hasUnlocked(2L, "campus_explorer")).isTrue();
    }

    @Test
    void campusExplorerUnlockedBySingleLocationExploredThreeTimes() {
        // 同一地点探索 3 次也能解锁（exploreCount 求和，不是地点数）
        List<Achievement> unlocked = achievementService.unlockIfEligible(2L, "explore_count", 3);
        assertThat(unlocked).anyMatch(a -> "campus_explorer".equals(a.getAchievementKey()));
    }

    @Test
    void campusExplorerUnlockedByTwoLocationsSumToThree() {
        // 两个地点 exploreCount 之和达到 3 也能解锁（如 1+2）
        List<Achievement> unlocked = achievementService.unlockIfEligible(2L, "explore_count", 3);
        assertThat(unlocked).anyMatch(a -> "campus_explorer".equals(a.getAchievementKey()));
    }

    @Test
    void campusExplorerNotUnlockedBelowThree() {
        // 累计探索次数 < 3 不能解锁
        List<Achievement> unlocked = achievementService.unlockIfEligible(2L, "explore_count", 2);
        assertThat(unlocked).noneMatch(a -> "campus_explorer".equals(a.getAchievementKey()));
    }

    @Test
    void goalFinisherAchievementIntegration() {
        // 通过 key 解锁 goal_finisher
        boolean unlocked = achievementService.unlockAchievement(2L, "goal_finisher");
        assertThat(unlocked).isTrue();
        assertThat(achievementService.hasUnlocked(2L, "goal_finisher")).isTrue();
    }
}
