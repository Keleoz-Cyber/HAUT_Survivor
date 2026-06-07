package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserAchievement;
import cn.haut.survivor.domain.entity.UserLocationExploration;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.mapper.UserAchievementMapper;
import cn.haut.survivor.mapper.UserLocationExplorationMapper;
import cn.haut.survivor.mapper.UserNpcRelationMapper;
import cn.haut.survivor.service.AchievementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final UserLocationExplorationMapper explorationMapper;
    private final UserNpcRelationMapper userNpcRelationMapper;

    public AchievementServiceImpl(
            AchievementMapper achievementMapper,
            UserAchievementMapper userAchievementMapper,
            PlayerProfileMapper playerProfileMapper,
            PlayerAttributeMapper playerAttributeMapper,
            UserLocationExplorationMapper explorationMapper,
            UserNpcRelationMapper userNpcRelationMapper) {
        this.achievementMapper = achievementMapper;
        this.userAchievementMapper = userAchievementMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.explorationMapper = explorationMapper;
        this.userNpcRelationMapper = userNpcRelationMapper;
    }

    @Override
    public List<Achievement> listActiveAchievements() {
        return achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getActive, 1));
    }

    @Override
    public List<UserAchievement> listUserAchievements(Long userId) {
        List<UserAchievement> list = userAchievementMapper.selectList(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .orderByDesc(UserAchievement::getUnlockedAt));
        populateAchievementField(list);
        return list;
    }

    @Override
    public List<UserAchievement> listRecentUnlocked(Long userId, int limit) {
        List<UserAchievement> list = userAchievementMapper.selectList(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .orderByDesc(UserAchievement::getUnlockedAt)
                .last("LIMIT " + limit));
        populateAchievementField(list);
        return list;
    }

    @Override
    @Transactional
    public List<Achievement> unlockIfEligible(Long userId, String conditionType, int currentValue) {
        List<Achievement> candidates = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getConditionType, conditionType)
                .eq(Achievement::getActive, 1));

        List<Achievement> newlyUnlocked = new ArrayList<>();

        for (Achievement achievement : candidates) {
            if (isAlreadyUnlocked(userId, achievement.getId())) {
                continue;
            }

            if (achievement.getConditionValue() != null && currentValue >= achievement.getConditionValue()) {
                doUnlock(userId, achievement);
                newlyUnlocked.add(achievement);
            }
        }

        return newlyUnlocked;
    }

    @Override
    @Transactional
    public boolean unlockAchievement(Long userId, String achievementKey) {
        Achievement achievement = achievementMapper.selectOne(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getAchievementKey, achievementKey)
                .eq(Achievement::getActive, 1)
                .last("LIMIT 1"));

        if (achievement == null) {
            return false;
        }

        if (isAlreadyUnlocked(userId, achievement.getId())) {
            return false;
        }

        doUnlock(userId, achievement);
        return true;
    }

    @Override
    public boolean hasUnlocked(Long userId, String achievementKey) {
        Achievement achievement = achievementMapper.selectOne(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getAchievementKey, achievementKey)
                .last("LIMIT 1"));

        if (achievement == null) {
            return false;
        }

        return isAlreadyUnlocked(userId, achievement.getId());
    }

    @Override
    @Transactional
    public void checkAllAchievements(Long userId) {
        PlayerAttribute attribute = playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
                .eq(PlayerAttribute::getUserId, userId)
                .last("LIMIT 1"));

        if (attribute == null) {
            return;
        }

        // 属性类成就
        unlockIfEligible(userId, "academic", attribute.getAcademic());
        unlockIfEligible(userId, "health", attribute.getHealth());
        unlockIfEligible(userId, "social", attribute.getSocial());
        unlockIfEligible(userId, "skill", attribute.getSkill());
        unlockIfEligible(userId, "discipline", attribute.getDiscipline());
        unlockIfEligible(userId, "pressure", attribute.getPressure());

        // 等级/经验类成就
        PlayerProfile profile = playerProfileMapper.selectOne(new LambdaQueryWrapper<PlayerProfile>()
                .eq(PlayerProfile::getUserId, userId)
                .last("LIMIT 1"));

        if (profile != null) {
            unlockIfEligible(userId, "level", profile.getLevel());
            unlockIfEligible(userId, "exp", profile.getExp());
        }

        // 探索类成就：使用 exploreCount 求和（累计探索次数）
        List<UserLocationExploration> explorations = explorationMapper.selectList(
                new LambdaQueryWrapper<UserLocationExploration>()
                        .eq(UserLocationExploration::getUserId, userId));
        int totalExploreCount = explorations.stream()
                .mapToInt(e -> e.getExploreCount() != null ? e.getExploreCount() : 0)
                .sum();
        unlockIfEligible(userId, "explore_count", totalExploreCount);

        // NPC 类成就
        Long npcCount = userNpcRelationMapper.selectCount(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId));
        unlockIfEligible(userId, "npc_count", npcCount.intValue());
    }

    // ==================== 内部方法 ====================

    private boolean isAlreadyUnlocked(Long userId, Long achievementId) {
        return userAchievementMapper.selectCount(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getAchievementId, achievementId)) > 0;
    }

    private void doUnlock(Long userId, Achievement achievement) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievementId(achievement.getId());
        userAchievement.setUnlockedAt(LocalDateTime.now());
        userAchievementMapper.insert(userAchievement);

        // 应用称号奖励
        if (achievement.getRewardTitle() != null && !achievement.getRewardTitle().isEmpty()) {
            PlayerProfile profile = playerProfileMapper.selectOne(new LambdaQueryWrapper<PlayerProfile>()
                    .eq(PlayerProfile::getUserId, userId)
                    .last("LIMIT 1"));
            if (profile != null) {
                profile.setCurrentTitle(achievement.getRewardTitle());
                playerProfileMapper.updateById(profile);
            }
        }
    }

    private void populateAchievementField(List<UserAchievement> list) {
        for (UserAchievement ua : list) {
            Achievement achievement = achievementMapper.selectById(ua.getAchievementId());
            ua.setAchievement(achievement);
        }
    }
}
