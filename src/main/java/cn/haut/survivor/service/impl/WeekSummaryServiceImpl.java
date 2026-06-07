package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.UserAchievement;
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.domain.entity.UserWeekSummary;
import cn.haut.survivor.mapper.NpcMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.UserNpcRelationMapper;
import cn.haut.survivor.mapper.UserNpcWeeklyActionMapper;
import cn.haut.survivor.mapper.UserWeeklyGoalMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import cn.haut.survivor.mapper.UserWeekSummaryMapper;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.WeekSummaryService;
import cn.haut.survivor.service.WeeklyThemeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeekSummaryServiceImpl implements WeekSummaryService {

    private final UserWeekSummaryMapper userWeekSummaryMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final UserWeeklyGoalMapper userWeeklyGoalMapper;
    private final WeeklyGoalMapper weeklyGoalMapper;
    private final UserNpcRelationMapper userNpcRelationMapper;
    private final UserNpcWeeklyActionMapper userNpcWeeklyActionMapper;
    private final NpcMapper npcMapper;
    private final WeeklyThemeService weeklyThemeService;
    private final AchievementService achievementService;

    public WeekSummaryServiceImpl(
            UserWeekSummaryMapper userWeekSummaryMapper,
            PlayerAttributeMapper playerAttributeMapper,
            UserWeeklyGoalMapper userWeeklyGoalMapper,
            WeeklyGoalMapper weeklyGoalMapper,
            UserNpcRelationMapper userNpcRelationMapper,
            UserNpcWeeklyActionMapper userNpcWeeklyActionMapper,
            NpcMapper npcMapper,
            WeeklyThemeService weeklyThemeService,
            AchievementService achievementService) {
        this.userWeekSummaryMapper = userWeekSummaryMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.userWeeklyGoalMapper = userWeeklyGoalMapper;
        this.weeklyGoalMapper = weeklyGoalMapper;
        this.userNpcRelationMapper = userNpcRelationMapper;
        this.userNpcWeeklyActionMapper = userNpcWeeklyActionMapper;
        this.npcMapper = npcMapper;
        this.weeklyThemeService = weeklyThemeService;
        this.achievementService = achievementService;
    }

    @Override
    @Transactional
    public UserWeekSummary generateSummary(Long userId, int weekNumber) {
        UserWeekSummary existing = getSummary(userId, weekNumber);
        if (existing != null) {
            return existing;
        }

        WeeklyThemeService.WeekTheme theme = weeklyThemeService.getTheme(weekNumber);
        String themeName = theme.name();
        String goalResult = buildGoalResult(userId, weekNumber);
        String summaryText = buildSummaryText(userId, weekNumber, theme);

        UserWeekSummary summary = new UserWeekSummary();
        summary.setUserId(userId);
        summary.setWeekNumber(weekNumber);
        summary.setThemeName(themeName);
        summary.setGoalResult(goalResult);
        summary.setSummaryText(summaryText);
        summary.setCreatedAt(LocalDateTime.now());

        userWeekSummaryMapper.insert(summary);
        return summary;
    }

    @Override
    public UserWeekSummary getSummary(Long userId, int weekNumber) {
        return userWeekSummaryMapper.selectOne(new LambdaQueryWrapper<UserWeekSummary>()
                .eq(UserWeekSummary::getUserId, userId)
                .eq(UserWeekSummary::getWeekNumber, weekNumber)
                .last("LIMIT 1"));
    }

    @Override
    public WeekSummaryView buildCurrentWeekSummary(Long userId, int weekNumber) {
        WeeklyThemeService.WeekTheme theme = weeklyThemeService.getTheme(weekNumber);
        PlayerAttribute attribute = playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
                .eq(PlayerAttribute::getUserId, userId)
                .last("LIMIT 1"));

        // 本周目标
        UserWeeklyGoal userGoal = userWeeklyGoalMapper.selectOne(new LambdaQueryWrapper<UserWeeklyGoal>()
                .eq(UserWeeklyGoal::getUserId, userId)
                .eq(UserWeeklyGoal::getWeekNumber, weekNumber)
                .last("LIMIT 1"));

        String goalName = null;
        boolean goalCompleted = false;
        boolean goalClaimed = false;
        String goalProgressText = "本周未设定目标";

        if (userGoal != null) {
            WeeklyGoal goalDef = weeklyGoalMapper.selectById(userGoal.getGoalId());
            if (goalDef != null) {
                goalName = goalDef.getGoalName();
                goalCompleted = userGoal.getCompleted() == 1;
                goalClaimed = userGoal.getClaimed() == 1;
                int current = userGoal.getCurrentValue();
                int target = goalDef.getTargetValue();

                if ("pressure_keep".equals(goalDef.getGoalType())) {
                    goalProgressText = attribute != null && attribute.getPressure() <= target
                            ? "压力控制良好 ✅" : "压力未达标";
                } else {
                    goalProgressText = current + "/" + target + (goalCompleted ? " ✅" : "");
                }
            }
        }

        // NPC：通过 npc_id 查询真实 NPC 名称，而非依赖 transient 字段
        List<UserNpcRelation> npcRelations = userNpcRelationMapper.selectList(
                new LambdaQueryWrapper<UserNpcRelation>()
                        .eq(UserNpcRelation::getUserId, userId));
        int knownNpcCount = npcRelations.size();
        List<String> recentNpcNames = npcRelations.stream()
                .map(r -> {
                    if (r.getNpcId() == null) return null;
                    Npc npc = npcMapper.selectById(r.getNpcId());
                    return npc != null ? npc.getNpcName() : null;
                })
                .filter(name -> name != null)
                .limit(5)
                .toList();

        UserNpcWeeklyAction currentBuddy = userNpcWeeklyActionMapper.selectOne(
                new LambdaQueryWrapper<UserNpcWeeklyAction>()
                        .eq(UserNpcWeeklyAction::getUserId, userId)
                        .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
                        .eq(UserNpcWeeklyAction::getBuddySelected, 1)
                        .last("LIMIT 1"));

        boolean hasNpcInteractionThisWeek = userNpcWeeklyActionMapper.selectCount(
                new LambdaQueryWrapper<UserNpcWeeklyAction>()
                        .eq(UserNpcWeeklyAction::getUserId, userId)
                        .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
                        .eq(UserNpcWeeklyAction::getInteracted, 1)) > 0;

        // 成就
        List<UserAchievement> recentAchievements = achievementService.listRecentUnlocked(userId, 5);

        // 生成评价
        String summaryText = generateSummaryText(attribute, goalCompleted, goalClaimed, knownNpcCount, weekNumber,
                currentBuddy != null, hasNpcInteractionThisWeek);
        String ratingLabel = generateRatingLabel(attribute, goalCompleted, knownNpcCount);

        return new WeekSummaryView(
                weekNumber,
                theme.name(),
                theme.description(),
                theme.icon(),
                goalName,
                goalCompleted,
                goalClaimed,
                goalProgressText,
                knownNpcCount,
                recentNpcNames,
                recentAchievements,
                attribute != null ? attribute.getAcademic() : 0,
                attribute != null ? attribute.getHealth() : 0,
                attribute != null ? attribute.getMoney() : 0,
                attribute != null ? attribute.getSocial() : 0,
                attribute != null ? attribute.getSkill() : 0,
                attribute != null ? attribute.getPressure() : 0,
                attribute != null ? attribute.getDiscipline() : 0,
                summaryText,
                ratingLabel
        );
    }

    // ==================== 内部方法 ====================

    private String buildGoalResult(Long userId, int weekNumber) {
        UserWeeklyGoal userGoal = userWeeklyGoalMapper.selectOne(new LambdaQueryWrapper<UserWeeklyGoal>()
                .eq(UserWeeklyGoal::getUserId, userId)
                .eq(UserWeeklyGoal::getWeekNumber, weekNumber)
                .last("LIMIT 1"));

        if (userGoal == null) {
            return "本周未设定目标";
        }

        WeeklyGoal goal = weeklyGoalMapper.selectById(userGoal.getGoalId());
        if (goal == null) {
            return "目标数据缺失";
        }

        if (userGoal.getCompleted() == 1) {
            return "已完成: " + goal.getGoalName();
        } else {
            int progress = userGoal.getCurrentValue() - userGoal.getStartValue();
            int target = goal.getTargetValue();
            return "未完成: " + goal.getGoalName() + " (" + progress + "/" + target + ")";
        }
    }

    private String buildSummaryText(Long userId, int weekNumber, WeeklyThemeService.WeekTheme theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(theme.name()).append("】\n");
        sb.append(theme.description()).append("\n\n");

        PlayerAttribute attribute = playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
                .eq(PlayerAttribute::getUserId, userId)
                .last("LIMIT 1"));

        if (attribute != null) {
            sb.append("当前属性: 学业").append(attribute.getAcademic())
                    .append(" 健康").append(attribute.getHealth())
                    .append(" 社交").append(attribute.getSocial())
                    .append(" 技能").append(attribute.getSkill())
                    .append(" 自律").append(attribute.getDiscipline())
                    .append(" 压力").append(attribute.getPressure())
                    .append("\n\n");
        }

        List<UserNpcRelation> npcRelations = userNpcRelationMapper.selectList(
                new LambdaQueryWrapper<UserNpcRelation>()
                        .eq(UserNpcRelation::getUserId, userId)
                        .eq(UserNpcRelation::getLastMetWeek, weekNumber));

        if (!npcRelations.isEmpty()) {
            sb.append("本周遇到了 ").append(npcRelations.size()).append(" 位 NPC\n");
        }

        sb.append("\n").append(theme.hint());
        return sb.toString();
    }

    private String generateSummaryText(PlayerAttribute attr, boolean goalCompleted, boolean goalClaimed, int npcCount,
                                       int weekNumber, boolean hasWeeklyBuddy, boolean hasNpcInteractionThisWeek) {
        if (attr == null) {
            return "这一周过去了。";
        }

        int pressure = attr.getPressure();
        boolean highPressure = pressure >= 60;
        boolean lowHealth = attr.getHealth() < 40;
        boolean highAcademic = attr.getAcademic() >= 70;
        boolean highSkill = attr.getSkill() >= 70;

        if (weekNumber == 3 && highPressure && (highAcademic || highSkill)) {
            return "你把 DDL 压成了可控范围，但精神状态已经像被控制台红字照了一晚上。";
        }
        if (weekNumber == 3 && highPressure) {
            return "这一周 DDL 明显压上来了。你还在推进，但下周最好别继续硬撑。";
        }
        if (weekNumber == 3 && (highAcademic || highSkill)) {
            return "你这周认真处理了课设和复习，DDL 没消失，但至少开始听你指挥。";
        }
        if (weekNumber == 4 && (highAcademic || highSkill) && pressure < 60) {
            return "期末前你稳住了复习节奏，没有把最后一周过成灾难片。";
        }
        if (weekNumber == 4 && pressure >= 70) {
            return "期末周把你推到了边缘，复习、体测和报告像同时响起的闹钟。";
        }

        if (hasWeeklyBuddy && goalCompleted) {
            return "这周你不是一个人硬扛，校园搭子把你的节奏拉住了。";
        }
        if (hasWeeklyBuddy) {
            return "这周你选了一个搭子，虽然计划未必完美，但至少不是单机求生。";
        }
        if (highPressure && !hasNpcInteractionThisWeek) {
            return "这周你几乎全靠自己硬撑。下周也许该找个人一起扛。";
        }
        if (hasNpcInteractionThisWeek && !goalCompleted) {
            return "你这周和校园里的人有了更多连接，虽然目标没完全拿下，但已经不算单机。";
        }

        // 目标完成 + 压力低
        if (goalCompleted && !highPressure) {
            return "这一周你稳得像提前写完作业的人。";
        }
        // 目标完成 + 压力高
        if (goalCompleted && highPressure) {
            return "你完成了计划，但精神状态像被 DDL 追着跑。";
        }
        // 目标未完成 + NPC 多
        if (!goalCompleted && npcCount >= 2) {
            return "计划没完全实现，但你在校园里认识了不少人。";
        }
        // 压力高
        if (highPressure) {
            return "这一周有点硬撑，建议下周去操场或宿舍缓一缓。";
        }
        // 技能/学业高
        if (highAcademic || highSkill) {
            return "你这周明显在认真搞学习。";
        }
        // 健康低
        if (lowHealth) {
            return "身体电量见底了，下周别只顾冲刺。";
        }
        // 默认
        return "这一周平平淡淡，但也算平安度过了。";
    }

    private String generateRatingLabel(PlayerAttribute attr, boolean goalCompleted, int npcCount) {
        if (attr == null) {
            return "平淡一周";
        }

        int score = 0;
        if (goalCompleted) score += 2;
        if (npcCount >= 2) score += 1;
        if (attr.getPressure() < 40) score += 1;
        if (attr.getAcademic() >= 70 || attr.getSkill() >= 70) score += 1;

        if (score >= 4) return "🌟 满分周";
        if (score >= 3) return "✨ 优秀周";
        if (score >= 2) return "👍 不错的一周";
        if (score >= 1) return "📝 还行的一周";
        return "😅 凑合过的一周";
    }
}
