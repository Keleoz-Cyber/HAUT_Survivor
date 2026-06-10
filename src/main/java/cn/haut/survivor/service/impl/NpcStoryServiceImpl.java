package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.UserNpcStoryProgress;
import cn.haut.survivor.mapper.UserNpcStoryProgressMapper;
import cn.haut.survivor.service.InfluenceLogService;
import cn.haut.survivor.service.NpcStoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NpcStoryServiceImpl implements NpcStoryService {

    private static final Map<Long, StoryDefinition> STORIES = Map.of(
            1L, new StoryDefinition("roommate_midnight_chat", List.of(
                    new StoryStage("宿舍夜聊开场",
                            "阿杰把零食往桌上一推，问你最近是不是有点绷得太紧。",
                            new AttributeChange(0, 0, 0, 1, 0, -1, 0, 0)),
                    new StoryStage("互相兜底的认可",
                            "你们约好谁先发现对方熬过头，就负责把灯关掉。宿舍开始有了可靠的节奏。",
                            new AttributeChange(0, 1, 0, 1, 0, -1, 0, 0))
            )),
            2L, new StoryDefinition("linran_review_notes", List.of(
                    new StoryStage("复习提纲交换",
                            "林然把易错点单独圈出来，让你第一次看见自己的复习盲区。",
                            new AttributeChange(2, 0, 0, 0, 0, 1, 1, 0)),
                    new StoryStage("考前互相抽题",
                            "你们开始互相抽题。过程有点痛，但知识点终于不再散着。",
                            new AttributeChange(3, 0, 0, 1, 0, 1, 1, 0))
            )),
            3L, new StoryDefinition("zhouyu_social_bridge", List.of(
                    new StoryStage("社交破冰局",
                            "周予把你拉进一次活动收尾局，顺手介绍了几个同专业同学。",
                            new AttributeChange(0, 0, 0, 2, 0, 0, 0, 0)),
                    new StoryStage("活动情报网",
                            "你发现周予总能提前知道哪里有机会，也愿意带你一起去看看。",
                            new AttributeChange(0, 0, 0, 2, 1, 0, 0, 0))
            )),
            4L, new StoryDefinition("zheng_lab_mentor", List.of(
                    new StoryStage("实验室第一杯咖啡",
                            "老郑没有急着讲大道理，只让你先把一个小模块跑起来。",
                            new AttributeChange(0, 0, 0, 0, 2, 0, 1, 0)),
                    new StoryStage("项目经验传递",
                            "你开始能听懂师兄说的坑，也知道遇到报错先看日志。",
                            new AttributeChange(0, 0, 0, 0, 3, 0, 1, 0))
            )),
            5L, new StoryDefinition("xiaoma_training_plan", List.of(
                    new StoryStage("操场慢跑约定",
                            "小马没有催你冲刺，只让你先跟着完成一圈。",
                            new AttributeChange(0, 2, 0, 1, 0, -1, 0, 0)),
                    new StoryStage("体测前的稳定训练",
                            "你们把训练拆成能坚持的小目标，体能焦虑也没那么吓人了。",
                            new AttributeChange(0, 3, 0, 1, 0, -1, 1, 0))
            ))
    );

    private static final Map<Long, BranchDefinition> BRANCHES = Map.of(
            1L, new BranchDefinition(NpcStoryBranchCatalog.AJIE_LIGHTS_OUT_PACT_ID,
                    "ajie_lights_out_pact", "熄灯前约定", 1,
                    "阿杰把床帘拉开一点，认真和你确认这周别再把作息拖到失控。",
                    "你们把熄灯、早八和外卖时间都约得更清楚了。宿舍关系没有轰轰烈烈，但变得很能兜底。",
                    new AttributeChange(0, 1, 0, 1, 0, -2, 2, 16), 3),
            2L, new BranchDefinition(NpcStoryBranchCatalog.LINRAN_KEY_WEEK_REVIEW_ID,
                    "linran_key_week_review", "关键周复盘", 1,
                    "林然拿出一张重新整理过的错题清单，让你先挑最不稳的三块补。",
                    "这次不是单纯借资料，而是一起把本周的学习节奏重新排了一遍。焦虑还在，但方向清楚多了。",
                    new AttributeChange(3, 0, 0, 1, 0, -1, 2, 20), 3),
            3L, new BranchDefinition(NpcStoryBranchCatalog.ZHOUYU_SMALL_CIRCLE_ID,
                    "zhouyu_small_circle", "小圈子补位", 1,
                    "周予说今晚有个不太正式的小活动，适合你这种刚好需要认识几个人的状态。",
                    "你没有被丢进陌生人堆里，而是被她自然地带进了一个能说上话的小圈子。",
                    new AttributeChange(0, 0, 0, 4, 1, 0, 0, 18), 3),
            4L, new BranchDefinition(NpcStoryBranchCatalog.LAOZHENG_REVIEW_SLOT_ID,
                    "laozheng_review_slot", "代码复盘位", 1,
                    "老郑给你留了十分钟白板时间，只看一个最卡的模块。",
                    "他没有替你写，但把问题拆成了你能继续推进的几步。项目突然从一团乱变成了可处理。",
                    new AttributeChange(1, 0, 0, 0, 4, -1, 2, 22), 3),
            5L, new BranchDefinition(NpcStoryBranchCatalog.XIAOMA_RECOVERY_RUN_ID,
                    "xiaoma_recovery_run", "恢复跑约定", 1,
                    "小马没有催你冲速度，只让你跟着完成一段能坚持下来的恢复跑。",
                    "跑完之后身体有点累，但脑子清醒了不少。你开始相信稳定比爆发更可靠。",
                    new AttributeChange(0, 4, 0, 1, 0, -2, 1, 18), 3)
    );

    private final UserNpcStoryProgressMapper progressMapper;
    private final InfluenceLogService influenceLogService;

    public NpcStoryServiceImpl(UserNpcStoryProgressMapper progressMapper,
                               InfluenceLogService influenceLogService) {
        this.progressMapper = progressMapper;
        this.influenceLogService = influenceLogService;
    }

    @Override
    @Transactional
    public Optional<NpcStoryResult> advanceOnInteraction(Long userId, Npc npc, int weekNumber) {
        if (userId == null || npc == null || npc.getId() == null) {
            return Optional.empty();
        }
        StoryDefinition definition = STORIES.get(npc.getId());
        if (definition == null) {
            return Optional.empty();
        }

        UserNpcStoryProgress progress = findProgress(userId, npc.getId(), definition.storyKey());
        if (progress != null && value(progress.getCompleted()) == 1) {
            return Optional.empty();
        }
        if (progress == null) {
            progress = new UserNpcStoryProgress();
            progress.setUserId(userId);
            progress.setNpcId(npc.getId());
            progress.setStoryKey(definition.storyKey());
            progress.setStage(0);
            progress.setCompleted(0);
            progress.setCreateTime(LocalDateTime.now());
        }

        int nextStage = value(progress.getStage()) + 1;
        if (nextStage > definition.stages().size()) {
            progress.setCompleted(1);
            progress.setUpdateTime(LocalDateTime.now());
            progressMapper.updateById(progress);
            return Optional.empty();
        }

        StoryStage stage = definition.stages().get(nextStage - 1);
        boolean completed = nextStage >= definition.stages().size();
        progress.setStage(nextStage);
        progress.setCompleted(completed ? 1 : 0);
        progress.setUpdateTime(LocalDateTime.now());
        if (progress.getId() == null) {
            progressMapper.insert(progress);
        } else {
            progressMapper.updateById(progress);
        }

        NpcStoryResult result = new NpcStoryResult(definition.storyKey(), npc.getId(), nextStage,
                stage.title(), stage.description(), stage.attributeChange(), completed);
        influenceLogService.recordInfluence(userId, weekNumber, npc.getHomeLocationId(), "npc_story",
                result.title(), result.description(), result.attributeChange(), 0);
        return Optional.of(result);
    }

    @Override
    public List<NpcInteraction> listUnlockedBranchInteractions(Long userId, Long npcId) {
        BranchDefinition branch = BRANCHES.get(npcId);
        StoryDefinition story = STORIES.get(npcId);
        if (branch == null || story == null) {
            return List.of();
        }
        UserNpcStoryProgress progress = findProgress(userId, npcId, story.storyKey());
        if (progress == null || value(progress.getStage()) < branch.requiredStoryStage()) {
            return List.of();
        }
        return List.of(toInteraction(npcId, branch));
    }

    @Override
    @Transactional
    public Optional<NpcStoryResult> recordBranchInteraction(Long userId, Npc npc, NpcInteraction interaction, int weekNumber) {
        if (userId == null || npc == null || npc.getId() == null || interaction == null) {
            return Optional.empty();
        }
        BranchDefinition branch = BRANCHES.get(npc.getId());
        if (branch == null || !branch.id().equals(interaction.getId())) {
            return Optional.empty();
        }
        StoryDefinition story = STORIES.get(npc.getId());
        UserNpcStoryProgress progress = story != null ? findProgress(userId, npc.getId(), story.storyKey()) : null;
        int stage = progress != null ? value(progress.getStage()) : branch.requiredStoryStage();
        boolean completed = progress != null && value(progress.getCompleted()) == 1;
        NpcStoryResult result = new NpcStoryResult(story != null ? story.storyKey() : branch.key(),
                npc.getId(), stage, branch.name(), branch.resultText(), branch.attributeChange(), completed);
        influenceLogService.recordInfluence(userId, weekNumber, npc.getHomeLocationId(), "npc_branch",
                result.title(), result.description(), result.attributeChange(), 0);
        return Optional.of(result);
    }

    private UserNpcStoryProgress findProgress(Long userId, Long npcId, String storyKey) {
        return progressMapper.selectOne(new LambdaQueryWrapper<UserNpcStoryProgress>()
                .eq(UserNpcStoryProgress::getUserId, userId)
                .eq(UserNpcStoryProgress::getNpcId, npcId)
                .eq(UserNpcStoryProgress::getStoryKey, storyKey)
                .last("LIMIT 1"));
    }

    private int value(Integer value) {
        return value != null ? value : 0;
    }

    private NpcInteraction toInteraction(Long npcId, BranchDefinition branch) {
        NpcInteraction interaction = new NpcInteraction();
        interaction.setId(branch.id());
        interaction.setNpcId(npcId);
        interaction.setInteractionKey(branch.key());
        interaction.setInteractionName(branch.name());
        interaction.setRequiredFamiliarity(25);
        interaction.setDescription(branch.description());
        interaction.setResultText(branch.resultText());
        interaction.setAcademicChange(branch.attributeChange().academicChange());
        interaction.setHealthChange(branch.attributeChange().healthChange());
        interaction.setMoneyChange(branch.attributeChange().moneyChange());
        interaction.setSocialChange(branch.attributeChange().socialChange());
        interaction.setSkillChange(branch.attributeChange().skillChange());
        interaction.setPressureChange(branch.attributeChange().pressureChange());
        interaction.setDisciplineChange(branch.attributeChange().disciplineChange());
        interaction.setFamiliarityChange(branch.familiarityGain());
        interaction.setExpChange(branch.attributeChange().expChange());
        interaction.setActive(1);
        return interaction;
    }

    private record StoryDefinition(String storyKey, List<StoryStage> stages) {}

    private record StoryStage(String title, String description, AttributeChange attributeChange) {}

    private record BranchDefinition(Long id, String key, String name, int requiredStoryStage,
                                    String description, String resultText, AttributeChange attributeChange,
                                    int familiarityGain) {}
}
