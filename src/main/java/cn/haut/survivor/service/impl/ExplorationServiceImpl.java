package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserLocationExploration;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.UserLocationExplorationMapper;
import cn.haut.survivor.service.ExplorationService;
import cn.haut.survivor.service.PlayerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExplorationServiceImpl implements ExplorationService {

    private final UserLocationExplorationMapper explorationMapper;
    private final PlayerService playerService;
    private final PlayerAttributeMapper playerAttributeMapper;

    public ExplorationServiceImpl(
            UserLocationExplorationMapper explorationMapper,
            PlayerService playerService,
            PlayerAttributeMapper playerAttributeMapper) {
        this.explorationMapper = explorationMapper;
        this.playerService = playerService;
        this.playerAttributeMapper = playerAttributeMapper;
    }

    @Override
    public UserLocationExploration findExploration(Long userId, Long locationId) {
        return explorationMapper.selectOne(new LambdaQueryWrapper<UserLocationExploration>()
                .eq(UserLocationExploration::getUserId, userId)
                .eq(UserLocationExploration::getLocationId, locationId));
    }

    @Override
    public List<UserLocationExploration> listUserExplorations(Long userId) {
        return explorationMapper.selectList(new LambdaQueryWrapper<UserLocationExploration>()
                .eq(UserLocationExploration::getUserId, userId));
    }

    @Override
    @Transactional
    public ExplorationResult explore(Long userId, Long locationId) {
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        if (playerService.isSemesterOver(userId)) {
            throw new IllegalArgumentException("学期已结束");
        }

        // 消耗行动点
        playerService.consumeActionPoint(userId);

        // 获取或创建探索记录
        UserLocationExploration exploration = findExploration(userId, locationId);
        if (exploration == null) {
            exploration = new UserLocationExploration();
            exploration.setUserId(userId);
            exploration.setLocationId(locationId);
            exploration.setExploreLevel(0);
            exploration.setExploreCount(0);
            exploration.setLastExploreWeek(0);
        }

        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);

        // 计算探索度增益（受属性、当前探索度、随机数影响）
        int baseGain = ThreadLocalRandom.current().nextInt(5, 16); // 5-15 基础
        int currentLevel = exploration.getExploreLevel();

        // 高探索度时增益递减
        if (currentLevel >= 80) {
            baseGain = Math.max(3, baseGain - 5);
        } else if (currentLevel >= 60) {
            baseGain = Math.max(4, baseGain - 3);
        }

        // 社交加成：探索某些地点时社交高有优势
        int socialBonus = attribute.getSocial() >= 60 ? 2 : 0;
        // 技能加成：实验室类地点技能高有优势
        int skillBonus = (locationId == 6L) && attribute.getSkill() >= 50 ? 3 : 0;

        int exploreLevelGain = Math.min(baseGain + socialBonus + skillBonus, 100 - currentLevel);

        // 随机探索结果
        ExplorationOutcome outcome = rollExplorationOutcome(currentLevel, attribute);

        // 应用属性变化
        attribute.setAcademic(clamp(attribute.getAcademic() + outcome.academicChange));
        attribute.setHealth(clamp(attribute.getHealth() + outcome.healthChange));
        attribute.setMoney(clamp(attribute.getMoney() + outcome.moneyChange));
        attribute.setSocial(clamp(attribute.getSocial() + outcome.socialChange));
        attribute.setSkill(clamp(attribute.getSkill() + outcome.skillChange));
        attribute.setPressure(clamp(attribute.getPressure() + outcome.pressureChange));
        attribute.setDiscipline(clamp(attribute.getDiscipline() + outcome.disciplineChange));
        playerAttributeMapper.updateById(attribute);

        // 更新探索记录
        exploration.setExploreLevel(currentLevel + exploreLevelGain);
        exploration.setExploreCount(exploration.getExploreCount() + 1);
        exploration.setLastExploreWeek(profile.getCurrentWeek());

        if (exploration.getId() == null) {
            explorationMapper.insert(exploration);
        } else {
            explorationMapper.updateById(exploration);
        }

        return new ExplorationResult(
                exploration,
                outcome.resultType,
                outcome.description,
                exploreLevelGain,
                outcome.academicChange,
                outcome.healthChange,
                outcome.moneyChange,
                outcome.socialChange,
                outcome.skillChange,
                outcome.pressureChange,
                outcome.disciplineChange
        );
    }

    @Override
    public boolean isUnlocked(Long userId, Long locationId, int requiredLevel) {
        int level = getExploreLevel(userId, locationId);
        return level >= requiredLevel;
    }

    @Override
    public int getExploreLevel(Long userId, Long locationId) {
        UserLocationExploration exploration = findExploration(userId, locationId);
        return exploration != null ? exploration.getExploreLevel() : 0;
    }

    // ---- 探索结果随机表 ----

    private ExplorationOutcome rollExplorationOutcome(int currentLevel, PlayerAttribute attribute) {
        int roll = ThreadLocalRandom.current().nextInt(100);

        // 探索度低时：更多发现类结果
        if (currentLevel < 20) {
            if (roll < 40) return new ExplorationOutcome("discover", "你发现了一条之前没注意到的小路，这地方比想象的大。", 0, 0, 0, 0, 0, -2, 1);
            if (roll < 65) return new ExplorationOutcome("discover", "角落里有个有趣的标识，你决定记住它。", 1, 0, 0, 1, 0, -1, 1);
            if (roll < 85) return new ExplorationOutcome("nothing", "你转了一圈，没有特别发现，但心情放松了些。", 0, 1, 0, 0, 0, -3, 0);
            return new ExplorationOutcome("setback", "你迷路了一会儿，浪费了时间。", 0, -1, 0, 0, 0, 2, -1);
        }

        // 探索度 20-40：开始有收获
        if (currentLevel < 40) {
            if (roll < 30) return new ExplorationOutcome("discover", "你找到了一个隐藏的角落，似乎有人在这里留下过痕迹。", 2, 0, 0, 2, 1, -2, 1);
            if (roll < 55) return new ExplorationOutcome("event_hint", "你无意间听到了一些有用的消息。", 2, 0, 0, 1, 0, -1, 1);
            if (roll < 75) return new ExplorationOutcome("nothing", "这次探索没什么特别收获，但你对这里更熟悉了。", 0, 0, 0, 0, 0, -2, 0);
            if (roll < 90) return new ExplorationOutcome("social", "你遇到了一个同样在探索的同学，聊了几句。", 0, 0, 0, 3, 0, -1, 0);
            return new ExplorationOutcome("setback", "你不小心踩到了水坑，鞋子湿了。", 0, -2, 0, 0, 0, 1, -1);
        }

        // 探索度 40-60：更丰富的发现
        if (currentLevel < 60) {
            if (roll < 25) return new ExplorationOutcome("hidden", "你发现了一个之前不知道的空间，里面有不少好东西。", 3, 0, 3, 2, 2, -2, 2);
            if (roll < 45) return new ExplorationOutcome("npc", "一个经常在这里出没的人注意到了你，主动搭话。", 1, 0, 0, 4, 1, -1, 1);
            if (roll < 65) return new ExplorationOutcome("skill_gain", "你在探索中练习了某种技巧，感觉有所提升。", 1, 0, 0, 0, 4, -1, 2);
            if (roll < 80) return new ExplorationOutcome("nothing", "熟悉的地方没有新发现，但你感到很自在。", 0, 1, 0, 0, 0, -3, 0);
            return new ExplorationOutcome("setback", "你被一个突然出现的问题打断了节奏。", -1, -1, 0, 0, 0, 3, -1);
        }

        // 探索度 60-80：深度发现
        if (currentLevel < 80) {
            if (roll < 25) return new ExplorationOutcome("secret", "你发现了一个很少有人知道的秘密通道或隐藏功能。", 4, 0, 5, 3, 3, -2, 3);
            if (roll < 45) return new ExplorationOutcome("route_unlock", "你解锁了一条新的路线或可能性！", 2, 0, 0, 3, 2, -1, 2);
            if (roll < 65) return new ExplorationOutcome("skill_gain", "深度探索让你对这个地方的理解更上一层楼。", 2, 0, 0, 0, 5, -2, 3);
            if (roll < 80) return new ExplorationOutcome("social", "你遇到了一位资深的前辈，获得了宝贵的建议。", 3, 0, 0, 5, 2, -1, 2);
            return new ExplorationOutcome("setback", "探索过于深入，你遇到了一些麻烦。", -1, -2, -3, 0, 0, 4, -2);
        }

        // 探索度 80+：稀有发现
        if (roll < 20) return new ExplorationOutcome("legendary", "你发现了这个地方最隐秘的角落，获得了一个传说级发现！", 5, 2, 8, 5, 5, -3, 4);
        if (roll < 40) return new ExplorationOutcome("title_hint", "你对这个地方了如指掌，似乎可以在这里获得一个称号。", 3, 0, 3, 3, 3, -2, 3);
        if (roll < 60) return new ExplorationOutcome("skill_gain", "你已经是这里的常客了，每次来都有新感悟。", 3, 1, 0, 0, 5, -2, 3);
        if (roll < 80) return new ExplorationOutcome("social", "你在这里被认出来了，大家觉得你是这个地方的专家。", 2, 0, 0, 6, 2, -1, 2);
        return new ExplorationOutcome("nothing", "你对这里已经太熟悉了，这次没有新的发现。", 0, 1, 0, 0, 0, -2, 1);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private record ExplorationOutcome(
            String resultType,
            String description,
            int academicChange,
            int healthChange,
            int moneyChange,
            int socialChange,
            int skillChange,
            int pressureChange,
            int disciplineChange
    ) {}
}
