package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.mapper.NpcInteractionMapper;
import cn.haut.survivor.mapper.NpcMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.UserNpcRelationMapper;
import cn.haut.survivor.mapper.UserNpcWeeklyActionMapper;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.NpcStoryService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.RumorEffectService;
import cn.haut.survivor.service.WeeklyGoalService;
import cn.haut.survivor.service.WeeklyThemeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class NpcServiceImpl implements NpcService {

    private static final double MEET_PROBABILITY = 0.35;
    private static final int BUDDY_REQUIRED_FAMILIARITY = 50;

    private final NpcMapper npcMapper;
    private final UserNpcRelationMapper relationMapper;
    private final NpcInteractionMapper interactionMapper;
    private final UserNpcWeeklyActionMapper weeklyActionMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final PlayerService playerService;
    private final WeeklyGoalService weeklyGoalService;
    private final AchievementService achievementService;
    private final RumorEffectService rumorEffectService;
    private final NpcStoryService npcStoryService;
    private final WeeklyThemeService weeklyThemeService;

    public NpcServiceImpl(
            NpcMapper npcMapper,
            UserNpcRelationMapper relationMapper,
            NpcInteractionMapper interactionMapper,
            UserNpcWeeklyActionMapper weeklyActionMapper,
            PlayerAttributeMapper playerAttributeMapper,
            PlayerService playerService,
            WeeklyGoalService weeklyGoalService,
            AchievementService achievementService,
            RumorEffectService rumorEffectService,
            NpcStoryService npcStoryService,
            WeeklyThemeService weeklyThemeService) {
        this.npcMapper = npcMapper;
        this.relationMapper = relationMapper;
        this.interactionMapper = interactionMapper;
        this.weeklyActionMapper = weeklyActionMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.playerService = playerService;
        this.weeklyGoalService = weeklyGoalService;
        this.achievementService = achievementService;
        this.rumorEffectService = rumorEffectService;
        this.npcStoryService = npcStoryService;
        this.weeklyThemeService = weeklyThemeService;
    }

    @Override
    public List<Npc> listActiveNpcs() {
        return npcMapper.selectList(new LambdaQueryWrapper<Npc>().eq(Npc::getActive, 1));
    }

    @Override
    public List<UserNpcRelation> listKnownNpcs(Long userId) {
        List<UserNpcRelation> relations = relationMapper.selectList(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId)
                .orderByDesc(UserNpcRelation::getFamiliarity));

        // Populate the npc transient field for each relation
        for (UserNpcRelation rel : relations) {
            Npc npc = npcMapper.selectById(rel.getNpcId());
            if (npc != null) {
                rel.setNpc(npc);
            }
        }
        return relations;
    }

    @Override
    @Transactional
    public Optional<NpcEncounter> maybeMeetNpc(Long userId, Long locationId, int currentWeek) {
        // 基础概率 + npc_boost 传闻加成
        double meetProbability = MEET_PROBABILITY;
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        int week = profile != null ? profile.getCurrentWeek() : currentWeek;
        int npcBoost = rumorEffectService.getNpcBoostForLocation(userId, week, locationId);
        // npc_boost effect_value 10 → 约增加 10 个百分点遇见概率
        meetProbability = Math.min(0.80, meetProbability + npcBoost / 100.0);

        if (ThreadLocalRandom.current().nextDouble() > meetProbability) {
            return Optional.empty();
        }

        // 查找该地点的活跃 NPC
        List<Npc> candidates = npcMapper.selectList(new LambdaQueryWrapper<Npc>()
                .eq(Npc::getHomeLocationId, locationId)
                .eq(Npc::getActive, 1));

        if (candidates.isEmpty()) {
            // 没有特定 NPC 的地点，从所有 NPC 中随机选一个
            List<Npc> all = listActiveNpcs();
            if (all.isEmpty()) return Optional.empty();
            candidates = all;
        }

        // 随机选一个
        Npc npc = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));

        // 查找或创建关系
        UserNpcRelation relation = relationMapper.selectOne(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId)
                .eq(UserNpcRelation::getNpcId, npc.getId()));

        int familiarityGain;
        if (relation == null) {
            familiarityGain = ThreadLocalRandom.current().nextInt(2, 4);
            relation = new UserNpcRelation();
            relation.setUserId(userId);
            relation.setNpcId(npc.getId());
            relation.setFamiliarity(familiarityGain);
            relation.setMetCount(1);
            relation.setLastMetWeek(currentWeek);
            relationMapper.insert(relation);
        } else {
            familiarityGain = ThreadLocalRandom.current().nextInt(1, 3);
            relation.setFamiliarity(Math.min(100, relation.getFamiliarity() + familiarityGain));
            relation.setMetCount(relation.getMetCount() + 1);
            relation.setLastMetWeek(currentWeek);
            relationMapper.updateById(relation);
        }

        // 填充 npc 字段供模板使用
        relation.setNpc(npc);

        // 生成遇见文案和倾向提示（不实际修改属性）
        String encounterText = generateEncounterText(npc, relation);
        String tendencyHint = generateTendencyHint(npc);

        return Optional.of(new NpcEncounter(
                npc, relation, familiarityGain, encounterText, tendencyHint
        ));
    }

    @Override
    @Transactional
    public void increaseFamiliarity(Long userId, Long npcId, int amount) {
        UserNpcRelation relation = requireRelation(userId, npcId);
        relation.setFamiliarity(clamp(value(relation.getFamiliarity()) + amount));
        relationMapper.updateById(relation);
    }

    @Override
    public List<NpcInteraction> listAvailableInteractions(Long userId, Long npcId, int weekNumber) {
        UserNpcRelation relation = requireRelation(userId, npcId);
        int familiarity = value(relation.getFamiliarity());
        List<NpcInteraction> interactions = new ArrayList<>(interactionMapper.selectList(new LambdaQueryWrapper<NpcInteraction>()
                .eq(NpcInteraction::getNpcId, npcId)
                .eq(NpcInteraction::getActive, 1)
                .le(NpcInteraction::getRequiredFamiliarity, familiarity)
                .orderByAsc(NpcInteraction::getRequiredFamiliarity)
                .orderByAsc(NpcInteraction::getId)));
        npcStoryService.listUnlockedBranchInteractions(userId, npcId).stream()
                .filter(interaction -> value(interaction.getRequiredFamiliarity()) <= familiarity)
                .forEach(interactions::add);
        return interactions;
    }

    @Override
    @Transactional
    public NpcInteractionResult interact(Long userId, Long npcId, Long interactionId, int weekNumber) {
        Npc npc = npcMapper.selectById(npcId);
        if (npc == null || value(npc.getActive()) != 1) {
            throw new IllegalArgumentException("NPC不存在或已不可互动");
        }

        UserNpcRelation relation = requireRelation(userId, npcId);
        NpcInteraction interaction = interactionMapper.selectById(interactionId);
        boolean branchInteraction = false;
        if (interaction == null) {
            interaction = npcStoryService.listUnlockedBranchInteractions(userId, npcId).stream()
                    .filter(candidate -> interactionId.equals(candidate.getId()))
                    .findFirst()
                    .orElse(null);
            branchInteraction = interaction != null;
        }
        if (interaction == null || value(interaction.getActive()) != 1 || !npcId.equals(interaction.getNpcId())) {
            throw new IllegalArgumentException("互动不存在或不属于该NPC");
        }
        if (value(relation.getFamiliarity()) < value(interaction.getRequiredFamiliarity())) {
            throw new IllegalStateException("熟悉度不足，暂时解锁不了这个互动");
        }

        UserNpcWeeklyAction action = getOrCreateWeeklyAction(userId, npcId, weekNumber);
        if (value(action.getInteracted()) == 1) {
            throw new IllegalStateException("本周已经和这个NPC互动过了");
        }

        playerService.consumeActionPoint(userId);
        PlayerAttribute attribute = playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
                .eq(PlayerAttribute::getUserId, userId)
                .last("LIMIT 1"));
        if (attribute == null) {
            throw new IllegalStateException("角色属性不存在");
        }

        int beforeAcademic = value(attribute.getAcademic());
        int beforeHealth = value(attribute.getHealth());
        int beforeMoney = value(attribute.getMoney());
        int beforeSocial = value(attribute.getSocial());
        int beforeSkill = value(attribute.getSkill());
        int beforePressure = value(attribute.getPressure());
        int beforeDiscipline = value(attribute.getDiscipline());

        boolean weeklyBuddy = getCurrentBuddy(userId, weekNumber)
                .map(buddy -> npcId.equals(buddy.getNpcId()))
                .orElse(false);
        applyInteractionChange(attribute, interaction, npc, weeklyBuddy);
        attribute.setUpdateTime(LocalDateTime.now());
        playerAttributeMapper.updateById(attribute);

        AttributeChange change = new AttributeChange(
                value(attribute.getAcademic()) - beforeAcademic,
                value(attribute.getHealth()) - beforeHealth,
                value(attribute.getMoney()) - beforeMoney,
                value(attribute.getSocial()) - beforeSocial,
                value(attribute.getSkill()) - beforeSkill,
                value(attribute.getPressure()) - beforePressure,
                value(attribute.getDiscipline()) - beforeDiscipline,
                value(interaction.getExpChange()));

        int openingWeekBonus = weeklyThemeService.npcOpeningWeekFamiliarityBonus(weekNumber, weeklyBuddy);
        int familiarityGain = value(interaction.getFamiliarityChange()) + (weeklyBuddy ? 1 : 0) + openingWeekBonus;
        relation.setFamiliarity(clamp(value(relation.getFamiliarity()) + familiarityGain));
        relation.setMetCount(value(relation.getMetCount()) + 1);
        relation.setLastMetWeek(weekNumber);
        relationMapper.updateById(relation);
        relation.setNpc(npc);

        action.setInteracted(1);
        action.setInteractedAt(LocalDateTime.now());
        weeklyActionMapper.updateById(action);

        weeklyGoalService.updateProgress(userId, weekNumber, "npc_interaction", 1);
        weeklyGoalService.updateProgress(userId, weekNumber, "familiarity_gain", familiarityGain);
        unlockNpcAchievements(userId, npc, relation);
        NpcStoryService.NpcStoryResult storyResult = branchInteraction
                ? npcStoryService.recordBranchInteraction(userId, npc, interaction, weekNumber).orElse(null)
                : npcStoryService.advanceOnInteraction(userId, npc, weekNumber).orElse(null);

        return new NpcInteractionResult(
                npc,
                interaction,
                relation,
                change,
                familiarityGain,
                interaction.getResultText() + weeklyThemeService.openingWeekNpcInteractionSuffix(weekNumber, weeklyBuddy),
                getRelationStage(relation.getFamiliarity()),
                storyResult);
    }

    @Override
    public Optional<UserNpcWeeklyAction> getCurrentBuddy(Long userId, int weekNumber) {
        return Optional.ofNullable(weeklyActionMapper.selectOne(new LambdaQueryWrapper<UserNpcWeeklyAction>()
                .eq(UserNpcWeeklyAction::getUserId, userId)
                .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
                .eq(UserNpcWeeklyAction::getBuddySelected, 1)
                .last("LIMIT 1")));
    }

    @Override
    @Transactional
    public void chooseWeeklyBuddy(Long userId, Long npcId, int weekNumber) {
        if (getCurrentBuddy(userId, weekNumber).isPresent()) {
            throw new IllegalStateException("本周已经选择过校园搭子");
        }
        Npc npc = npcMapper.selectById(npcId);
        if (npc == null || value(npc.getActive()) != 1) {
            throw new IllegalArgumentException("NPC不存在或已不可选择");
        }
        UserNpcRelation relation = requireRelation(userId, npcId);
        if (value(relation.getFamiliarity()) < BUDDY_REQUIRED_FAMILIARITY) {
            throw new IllegalStateException("熟悉度不足，至少需要50才能成为本周搭子");
        }

        UserNpcWeeklyAction action = getOrCreateWeeklyAction(userId, npcId, weekNumber);
        action.setBuddySelected(1);
        action.setSelectedAt(LocalDateTime.now());
        weeklyActionMapper.updateById(action);

        weeklyGoalService.updateProgress(userId, weekNumber, "buddy_selected", 1);
        achievementService.unlockAchievement(userId, "first_buddy");
        unlockNpcAchievements(userId, npc, relation);
    }

    @Override
    public String getRelationStage(Integer familiarity) {
        return getRelationSummary(familiarity).label();
    }

    @Override
    public RelationSummary getRelationSummary(Integer familiarity) {
        int value = value(familiarity);
        if (value >= 80) {
            return new RelationSummary(
                    "close",
                    "重要关系",
                    "TA 已经是你这学期绕不开的重要关系。",
                    null,
                    "关系已经进入稳定阶段，后续互动会留下更清晰的学期记忆。",
                    100);
        }
        if (value >= 50) {
            return new RelationSummary(
                    "buddy",
                    "搭子",
                    "你们已经可以稳定约在一起做事。",
                    80,
                    "熟悉度达到 80 后，TA 会成为你的重要关系。",
                    progressBetween(value, 50, 80));
        }
        if (value >= 25) {
            return new RelationSummary(
                    "familiar",
                    "熟人",
                    "见面会自然打招呼，也开始记得彼此的节奏。",
                    50,
                    "熟悉度达到 50 后，可以选择 TA 作为本周搭子。",
                    progressBetween(value, 25, 50));
        }
        return new RelationSummary(
                "acquaintance",
                "点头之交",
                "你们刚开始认识，还需要更多共同经历。",
                25,
                "熟悉度达到 25 后，关系会进入熟人阶段。",
                progressBetween(value, 0, 25));
    }

    private UserNpcRelation requireRelation(Long userId, Long npcId) {
        UserNpcRelation relation = relationMapper.selectOne(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId)
                .eq(UserNpcRelation::getNpcId, npcId)
                .last("LIMIT 1"));
        if (relation != null) {
            return relation;
        }

        Npc npc = npcMapper.selectById(npcId);
        if (npc == null || value(npc.getActive()) != 1) {
            throw new IllegalArgumentException("NPC不存在或已不可互动");
        }

        relation = new UserNpcRelation();
        relation.setUserId(userId);
        relation.setNpcId(npcId);
        relation.setFamiliarity(0);
        relation.setMetCount(0);
        relation.setLastMetWeek(0);
        relationMapper.insert(relation);
        relation.setNpc(npc);
        return relation;
    }

    private UserNpcWeeklyAction getOrCreateWeeklyAction(Long userId, Long npcId, int weekNumber) {
        UserNpcWeeklyAction action = weeklyActionMapper.selectOne(new LambdaQueryWrapper<UserNpcWeeklyAction>()
                .eq(UserNpcWeeklyAction::getUserId, userId)
                .eq(UserNpcWeeklyAction::getNpcId, npcId)
                .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
                .last("LIMIT 1"));
        if (action != null) {
            return action;
        }

        action = new UserNpcWeeklyAction();
        action.setUserId(userId);
        action.setNpcId(npcId);
        action.setWeekNumber(weekNumber);
        action.setInteracted(0);
        action.setBuddySelected(0);
        weeklyActionMapper.insert(action);
        return action;
    }

    private void applyInteractionChange(PlayerAttribute attribute, NpcInteraction interaction, Npc npc, boolean weeklyBuddy) {
        int academicDelta = value(interaction.getAcademicChange());
        int healthDelta = value(interaction.getHealthChange());
        int moneyDelta = value(interaction.getMoneyChange());
        int socialDelta = value(interaction.getSocialChange());
        int skillDelta = value(interaction.getSkillChange());
        int pressureDelta = value(interaction.getPressureChange());
        int disciplineDelta = value(interaction.getDisciplineChange());

        if (weeklyBuddy) {
            switch (npc.getFavoriteAttribute() != null ? npc.getFavoriteAttribute() : "") {
                case "academic" -> academicDelta += 1;
                case "health" -> healthDelta += 1;
                case "money" -> moneyDelta += 1;
                case "social" -> socialDelta += 1;
                case "skill" -> skillDelta += 1;
                case "pressure" -> pressureDelta -= 1;
                case "discipline" -> disciplineDelta += 1;
                default -> socialDelta += 1;
            }
        }

        attribute.setAcademic(clamp(value(attribute.getAcademic()) + academicDelta));
        attribute.setHealth(clamp(value(attribute.getHealth()) + healthDelta));
        attribute.setMoney(clamp(value(attribute.getMoney()) + moneyDelta));
        attribute.setSocial(clamp(value(attribute.getSocial()) + socialDelta));
        attribute.setSkill(clamp(value(attribute.getSkill()) + skillDelta));
        attribute.setPressure(clamp(value(attribute.getPressure()) + pressureDelta));
        attribute.setDiscipline(clamp(value(attribute.getDiscipline()) + disciplineDelta));
    }

    private void unlockNpcAchievements(Long userId, Npc npc, UserNpcRelation relation) {
        if (value(relation.getFamiliarity()) >= 80) {
            achievementService.unlockAchievement(userId, "iron_buddy");
        }
        if (npc.getId() != null && npc.getId() == 2L && value(relation.getFamiliarity()) >= 50) {
            achievementService.unlockAchievement(userId, "study_partner");
        }
        if (npc.getId() != null && npc.getId() == 4L && value(relation.getFamiliarity()) >= 50) {
            achievementService.unlockAchievement(userId, "lab_apprentice");
        }
        long knownCount = relationMapper.selectCount(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId));
        if (knownCount >= 5) {
            achievementService.unlockAchievement(userId, "social_web");
        }
    }

    private int value(Integer value) {
        return value != null ? value : 0;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int progressBetween(int value, int start, int end) {
        if (end <= start) {
            return 100;
        }
        int clamped = Math.max(start, Math.min(end, value));
        return Math.round((clamped - start) * 100.0f / (end - start));
    }

    private String generateEncounterText(Npc npc, UserNpcRelation relation) {
        String name = npc.getNpcName();
        String type = npc.getNpcType();
        String personality = npc.getPersonality() != null ? npc.getPersonality() : "";

        // 初次见面
        if (relation.getMetCount() <= 1) {
            return pickRandom(switch (type) {
                case "室友" -> List.of(
                        "推开宿舍门，一个" + personality + "的人正坐在床上打游戏，嘴里喊着「冲冲冲」。原来是你的室友" + name + "。",
                        name + "正在宿舍里组装电脑，看到你进来说了句「你也住这儿？那以后一起点外卖」。看起来" + personality + "的人果然自来熟。",
                        "你推开宿舍门，" + name + "正躺在床上刷短视频，看到你来了从床上坐起来说了句「来了兄弟」。看起来" + personality + "。"
                );
                case "学霸" -> List.of(
                        "在图书馆偶遇了一个笔记比课本还整齐的人，看起来" + personality + "。这就是传说中的" + name + "。",
                        name + "正在自习室里用五种颜色的笔做笔记，旁边放着一杯已经凉了的咖啡。" + personality + "的人果然不一样。",
                        "你在图书馆找座位的时候，看到一个桌上摆了三本教材的人。这就是" + name + "，据说期末从不低于 90。"
                );
                case "社牛" -> List.of(
                        "一个" + personality + "的人突然冲过来跟你打招呼，自来熟到让你有点懵。这就是" + name + "。",
                        name + "在食堂门口发传单，看到你就笑着说「同学了解一下」，三句话之内已经知道了你的专业和爱好。",
                        "社团招新现场，一个" + personality + "的人站在摊位上用大喇叭喊「加我们社团送奶茶」。这就是" + name + "。"
                );
                case "师兄" -> List.of(
                        "实验室角落里一个" + personality + "的身影正在调试代码，看起来很靠谱。师兄" + name + "注意到你了。",
                        name + "正在对着满屏红色报错皱眉，看到你进来问了句「你会看日志吗？」。师兄看起来" + personality + "。",
                        "实验室的门半开着，" + name + "正对着两个屏幕疯狂敲代码。你敲门进去，他头也不回地说「帮我看看第 42 行」。"
                );
                case "搭子" -> List.of(
                        name + "正在操场上拉伸，看起来活力满满、" + personality + "。你们对视了一眼，感觉会成为不错的搭子。",
                        "操场上一个穿着运动服的人在原地高抬腿热身，看到你说「来跑步的？一起啊」。这就是" + name + "，" + personality + "。",
                        name + "刚跑完五圈回来，满头大汗但精神特别好。TA 冲你竖了个大拇指说「今天天气适合跑步」。"
                );
                default -> List.of("在校园里偶然遇到了" + name + "。" + personality);
            });
        }

        // 熟悉度低（见过但不太熟）
        if (relation.getFamiliarity() < 30) {
            return pickRandom(switch (type) {
                case "室友" -> List.of(
                        name + "正好从外面回来，手里拎着外卖。你们点了点头，算是开始熟了。",
                        name + "在洗衣服的时候问你「周末回不回家」，你们简单聊了两句。",
                        "你听到" + name + "在跟家里打电话，挂了之后你们对视了一下，都有点尴尬但礼貌地笑了。"
                );
                case "学霸" -> List.of(
                        "又碰到" + name + "了，这次 TA 没在刷题而是在看手机。你们简单聊了两句课。",
                        name + "在食堂一个人吃饭，你路过的时候 TA 冲你点了个头。学霸的社交方式就是这么简洁。",
                        "你在图书馆门口遇到" + name + "，TA 手里抱着一摞书但还是腾出一只手跟你打了个招呼。"
                );
                case "社牛" -> List.of(
                        name + "远远就跟你挥手，说性格随和的人运气不会差。你有点信了。",
                        name + "路过你旁边说「今晚有个活动来不来？」，你还没反应过来 TA 已经把邀请函塞你手里了。",
                        "在食堂排队的时候，" + name + "从你后面拍了拍你的肩膀说「又见面了！今天吃什么？」。"
                );
                case "师兄" -> List.of(
                        name + "路过你的位置，随口问了一句「代码跑起来了吗？」你感觉离融入实验室更近了。",
                        name + "在实验室茶水间泡面的时候跟你聊了两句项目的事，你发现师兄其实挺好说话的。",
                        "你在实验室门口遇到" + name + "，他说「下次组会你来看看，可以先熟悉一下」。"
                );
                case "搭子" -> List.of(
                        name + "跟你打招呼说下次一起跑，你答应了但不知道自己能坚持多久。",
                        "在操场碰到了" + name + "，TA 说「今天跑了两圈就不行了」，你觉得这个搭子很真实。",
                        name + "看到你路过操场喊了一声「来都来了，跑一圈吧！」你犹豫了一下。"
                );
                default -> List.of("又碰到了" + name + "，你们开始熟悉起来了。");
            });
        }

        // 熟悉度中等
        if (relation.getFamiliarity() < 60) {
            return pickRandom(switch (type) {
                case "室友" -> List.of(
                        name + "主动给你分享了零食，你们聊起了最近追的番。宿舍生活开始有味道了。",
                        name + "半夜突然来了一句「你说人活着到底为了什么」，你们聊到了凌晨两点。宿舍深夜哲学讨论开始了。",
                        "你和" + name + "已经能互相帮忙拿外卖了。在宿舍，这种信任比什么都重要。"
                );
                case "学霸" -> List.of(
                        name + "主动跟你打招呼，还把整理的复习笔记分享给你。学霸的友谊是有实际价值的。",
                        name + "发消息问你「这道题你会吗？」你们讨论了半小时，最后发现是题目印错了。但过程很充实。",
                        name + "考试前给你划了重点，说「这些肯定考」。你决定相信学霸的判断。"
                );
                case "社牛" -> List.of(
                        name + "拉你加入了一个学习小组，说认识人多了机会就多了。TA 说的好像是对的。",
                        name + "给你介绍了一个实习机会，说「你的话肯定没问题」。社牛的人脉网络让你叹为观止。",
                        name + "约你一起去参加社团联谊，说「多认识点人没坏处」。你发现跟 TA 在一起社交确实没那么累。"
                );
                case "师兄" -> List.of(
                        name + "递给你一杯咖啡说「熬夜也要讲究效率」。你感觉师兄在认真带你。",
                        name + "让你帮忙做一个简单的模块，说「先从简单的开始」。你在实验室的存在感越来越强了。",
                        name + "在组会上帮你挡了一个问题，会后说「别怕，谁都有第一次」。师兄的靠谱不是装出来的。"
                );
                case "搭子" -> List.of(
                        name + "拍了拍你的肩膀说「今天跑完了请你喝水」。运动搭子的承诺比减肥计划靠谱多了。",
                        name + "发消息说「今天操场见」，你居然真的去了。有人监督的感觉还挺不一样的。",
                        name + "告诉你「跑步的时候听什么歌很重要」，然后给你分享了一个跑步歌单。你们现在跑完会一起拉伸了。"
                );
                default -> List.of(name + "主动跟你打招呼，你们聊了几句。");
            });
        }

        // 熟悉度高（搭子级别）
        return pickRandom(switch (type) {
            case "室友" -> List.of(
                    name + "已经完全融入你的宿舍生活，你们默契十足。晚上开黑或者聊人生都行。",
                    name + "不用说话就知道你要借充电线。你们已经进化到可以用眼神交流了。",
                    "你和" + name + "的默契到了一个电话就能搞定拼单的程度。室友做久了就是这样。"
            );
            case "学霸" -> List.of(
                    name + "已经成为你的学习搭子，考前互相监督、课后互相吐槽。学霸也能很接地气。",
                    name + "已经习惯性地把整理好的资料先发给你一份。你们现在连复习计划都是一起做的。",
                    name + "说「下次考试咱俩坐一起复习吧」，你发现跟学霸一起学习效率确实高了不少。"
            );
            case "社牛" -> List.of(
                    name + "已经成为你在校园里最靠谱的社交桥梁，认识新人的事交给 TA 就行。",
                    name + "逢人就介绍你说「这是我朋友」，你在校园里走路都能碰到认识的人了。",
                    name + "已经把你的微信推给了三个他认为你应该认识的人。社牛的社交网络你永远不懂。"
            );
            case "师兄" -> List.of(
                    name + "把你当半个徒弟了，项目里有活都想着叫你。实验室的日子因为师兄而没那么难熬。",
                    name + "已经开始跟你吐槽导师了，这在实验室里意味着你们已经是真正的自己人了。",
                    name + "说「等你毕业了也来实验室吧」，你发现自己在实验室已经有了归属感。"
            );
            case "搭子" -> List.of(
                    name + "已经成为你的固定运动搭子，默契十足。跑步的时候不说话也觉得舒服。",
                    name + "已经不需要约了，每天固定时间操场见。搭子之间的默契比闹钟还准时。",
                    name + "说「跑步的时候你是唯一能跟上我节奏的人」。你突然觉得坚持运动也没那么难。"
            );
            default -> List.of(name + "已经成为你的搭子了，默契十足。");
        });
    }

    private String pickRandom(List<String> texts) {
        if (texts.size() == 1) return texts.get(0);
        return texts.get(ThreadLocalRandom.current().nextInt(texts.size()));
    }

    private String generateTendencyHint(Npc npc) {
        String fav = npc.getFavoriteAttribute();
        if ("academic".equals(fav)) return "和 TA 在一起学业容易进步";
        if ("health".equals(fav)) return "和 TA 在一起更想运动";
        if ("social".equals(fav)) return "和 TA 在一起社交机会更多";
        if ("skill".equals(fav)) return "和 TA 在一起技能提升更快";
        if ("pressure".equals(fav)) return "和 TA 在一起压力会低一些";
        if ("discipline".equals(fav)) return "和 TA 在一起自律更容易坚持";
        return "和 TA 在一起总有点好事";
    }
}
