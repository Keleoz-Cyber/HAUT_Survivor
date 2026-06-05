package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.DungeonTaskOption;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserDungeonTaskRecord;
import cn.haut.survivor.mapper.DungeonMapper;
import cn.haut.survivor.mapper.DungeonTaskMapper;
import cn.haut.survivor.mapper.DungeonTaskOptionMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.mapper.UserDungeonRecordMapper;
import cn.haut.survivor.mapper.UserDungeonTaskRecordMapper;
import cn.haut.survivor.service.DungeonService;
import cn.haut.survivor.service.PlayerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class DungeonServiceImpl implements DungeonService {

    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";

    private final DungeonMapper dungeonMapper;
    private final DungeonTaskMapper dungeonTaskMapper;
    private final DungeonTaskOptionMapper dungeonTaskOptionMapper;
    private final UserDungeonRecordMapper userDungeonRecordMapper;
    private final UserDungeonTaskRecordMapper userDungeonTaskRecordMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;

    public DungeonServiceImpl(
            DungeonMapper dungeonMapper,
            DungeonTaskMapper dungeonTaskMapper,
            DungeonTaskOptionMapper dungeonTaskOptionMapper,
            UserDungeonRecordMapper userDungeonRecordMapper,
            UserDungeonTaskRecordMapper userDungeonTaskRecordMapper,
            PlayerAttributeMapper playerAttributeMapper,
            PlayerProfileMapper playerProfileMapper,
            PlayerService playerService
    ) {
        this.dungeonMapper = dungeonMapper;
        this.dungeonTaskMapper = dungeonTaskMapper;
        this.dungeonTaskOptionMapper = dungeonTaskOptionMapper;
        this.userDungeonRecordMapper = userDungeonRecordMapper;
        this.userDungeonTaskRecordMapper = userDungeonTaskRecordMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerService = playerService;
    }

    @Override
    public Dungeon findDemoDungeon() {
        return dungeonMapper.selectOne(new LambdaQueryWrapper<Dungeon>()
                .eq(Dungeon::getStatus, 1)
                .eq(Dungeon::getDungeonName, "Java 课设：DDL 前夜")
                .last("limit 1"));
    }

    @Override
    public List<DungeonTask> listTasks(Long dungeonId) {
        return dungeonTaskMapper.selectList(new LambdaQueryWrapper<DungeonTask>()
                .eq(DungeonTask::getDungeonId, dungeonId)
                .eq(DungeonTask::getStatus, 1)
                .orderByAsc(DungeonTask::getTaskOrder));
    }

    @Override
    public List<DungeonTaskOption> listOptions(Long taskId) {
        return dungeonTaskOptionMapper.selectList(new LambdaQueryWrapper<DungeonTaskOption>()
                .eq(DungeonTaskOption::getDungeonTaskId, taskId)
                .eq(DungeonTaskOption::getStatus, 1)
                .orderByAsc(DungeonTaskOption::getId));
    }

    @Override
    @Transactional
    public UserDungeonRecord startOrResumeDemoDungeon(Long userId) {
        if (!playerService.hasProfile(userId)) {
            throw new IllegalArgumentException("请先创建角色");
        }
        Dungeon dungeon = requireDemoDungeon();
        UserDungeonRecord record = userDungeonRecordMapper.selectOne(new LambdaQueryWrapper<UserDungeonRecord>()
                .eq(UserDungeonRecord::getUserId, userId)
                .eq(UserDungeonRecord::getDungeonId, dungeon.getId())
                .eq(UserDungeonRecord::getStatus, IN_PROGRESS)
                .last("limit 1"));
        if (record != null) {
            return record;
        }

        DungeonTask firstTask = listTasks(dungeon.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("副本缺少阶段任务"));
        record = new UserDungeonRecord();
        record.setUserId(userId);
        record.setDungeonId(dungeon.getId());
        record.setCurrentTaskId(firstTask.getId());
        record.setStatus(IN_PROGRESS);
        record.setTotalScore(0);
        record.setRiskFlags("");
        record.setStartTime(LocalDateTime.now());
        userDungeonRecordMapper.insert(record);
        return record;
    }

    @Override
    public DungeonTask findCurrentTask(UserDungeonRecord record) {
        if (record == null || record.getCurrentTaskId() == null) {
            return null;
        }
        return dungeonTaskMapper.selectById(record.getCurrentTaskId());
    }

    @Override
    public UserDungeonRecord findRecordById(Long userId, Long recordId) {
        return requireRecord(userId, recordId);
    }

    @Override
    @Transactional
    public UserDungeonTaskRecord chooseOption(Long userId, Long recordId, Long taskId, Long optionId, String minigameResult) {
        UserDungeonRecord record = requireRecord(userId, recordId);
        if (!IN_PROGRESS.equals(record.getStatus())) {
            throw new IllegalArgumentException("副本已经结束");
        }
        if (!taskId.equals(record.getCurrentTaskId())) {
            throw new IllegalArgumentException("只能结算当前阶段任务");
        }
        DungeonTask task = requireTask(taskId);
        DungeonTaskOption option = requireOption(taskId, optionId);

        applyRewards(userId, option);

        UserDungeonTaskRecord taskRecord = new UserDungeonTaskRecord();
        taskRecord.setUserDungeonRecordId(record.getId());
        taskRecord.setDungeonTaskId(task.getId());
        taskRecord.setTaskType(task.getTaskType());
        taskRecord.setSelectedOptionId(option.getId());
        taskRecord.setMinigameResult(StringUtils.hasText(minigameResult) ? minigameResult.trim() : null);
        taskRecord.setAttributeCheckResult(option.getScore() >= 80 ? "excellent" : option.getScore() >= 50 ? "pass" : "fail");
        taskRecord.setResultText(option.getResultText());
        taskRecord.setEvaluation(option.getEvaluation());
        taskRecord.setScore(option.getScore());
        taskRecord.setExpChange(option.getExpChange());
        taskRecord.setCreateTime(LocalDateTime.now());
        userDungeonTaskRecordMapper.insert(taskRecord);

        record.setTotalScore(record.getTotalScore() + option.getScore());
        applyOptionFlags(record, task, option);
        DungeonTask nextTask = findNextTask(task, option);
        if (nextTask == null) {
            record.setCurrentTaskId(null);
            record.setStatus(COMPLETED);
            record.setFinalEvaluation(buildFinalEvaluation(record));
            record.setFinishTime(LocalDateTime.now());
        } else {
            record.setCurrentTaskId(nextTask.getId());
        }
        userDungeonRecordMapper.updateById(record);
        return taskRecord;
    }

    @Override
    @Transactional
    public UserDungeonTaskRecord chooseMinigameRelations(Long userId, Long recordId, Long taskId,
                                                         List<String> selectedRelations, Integer elapsedSeconds) {
        UserDungeonRecord record = requireRecord(userId, recordId);
        if (!IN_PROGRESS.equals(record.getStatus())) {
            throw new IllegalArgumentException("副本已经结束");
        }
        if (!taskId.equals(record.getCurrentTaskId())) {
            throw new IllegalArgumentException("只能结算当前阶段任务");
        }
        DungeonTask task = requireTask(taskId);
        if (!"db_link".equals(task.getMinigameType())) {
            throw new IllegalArgumentException("当前阶段不是数据库拼图");
        }

        MinigameSettlement settlement = settleDatabaseLinks(userId, record, selectedRelations, elapsedSeconds);
        applyDynamicRewards(userId, settlement);

        UserDungeonTaskRecord taskRecord = new UserDungeonTaskRecord();
        taskRecord.setUserDungeonRecordId(record.getId());
        taskRecord.setDungeonTaskId(task.getId());
        taskRecord.setTaskType(task.getTaskType());
        taskRecord.setMinigameResult("relations=" + String.join("|", selectedRelations == null ? List.of() : selectedRelations)
                + ";elapsed=" + (elapsedSeconds == null ? 0 : elapsedSeconds));
        taskRecord.setAttributeCheckResult(settlement.score >= 80 ? "excellent" : settlement.score >= 50 ? "pass" : "fail");
        taskRecord.setResultText(settlement.resultText);
        taskRecord.setEvaluation(settlement.evaluation);
        taskRecord.setScore(settlement.score);
        taskRecord.setExpChange(settlement.expChange);
        taskRecord.setCreateTime(LocalDateTime.now());
        userDungeonTaskRecordMapper.insert(taskRecord);

        record.setTotalScore(record.getTotalScore() + settlement.score);
        addRiskFlag(record, settlement.flag);
        DungeonTask nextTask = findNextTask(task, new DungeonTaskOption());
        if (nextTask == null) {
            record.setCurrentTaskId(null);
            record.setStatus(COMPLETED);
            record.setFinalEvaluation(buildFinalEvaluation(record));
            record.setFinishTime(LocalDateTime.now());
        } else {
            record.setCurrentTaskId(nextTask.getId());
        }
        userDungeonRecordMapper.updateById(record);
        return taskRecord;
    }

    private void applyOptionFlags(UserDungeonRecord record, DungeonTask task, DungeonTaskOption option) {
        if (task.getTaskOrder() == 1) {
            if (option.getScore() >= 80) {
                addRiskFlag(record, "scope_controlled");
            } else if (option.getScore() < 50) {
                addRiskFlag(record, "scope_sprawl");
            } else {
                addRiskFlag(record, "report_first");
            }
        }
    }

    private void addRiskFlag(UserDungeonRecord record, String flag) {
        String existing = record.getRiskFlags() == null ? "" : record.getRiskFlags();
        if (("," + existing + ",").contains("," + flag + ",")) {
            return;
        }
        record.setRiskFlags(existing.isBlank() ? flag : existing + "," + flag);
    }

    private boolean hasRiskFlag(UserDungeonRecord record, String flag) {
        String existing = record == null || record.getRiskFlags() == null ? "" : record.getRiskFlags();
        return ("," + existing + ",").contains("," + flag + ",");
    }

    private Dungeon requireDemoDungeon() {
        Dungeon dungeon = findDemoDungeon();
        if (dungeon == null) {
            throw new IllegalStateException("Demo 副本不存在");
        }
        return dungeon;
    }

    private UserDungeonRecord requireRecord(Long userId, Long recordId) {
        UserDungeonRecord record = userDungeonRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new IllegalArgumentException("副本记录不存在");
        }
        return record;
    }

    private DungeonTask requireTask(Long taskId) {
        DungeonTask task = dungeonTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("副本阶段不存在");
        }
        return task;
    }

    private DungeonTaskOption requireOption(Long taskId, Long optionId) {
        DungeonTaskOption option = dungeonTaskOptionMapper.selectById(optionId);
        if (option == null || !option.getDungeonTaskId().equals(taskId)) {
            throw new IllegalArgumentException("副本选项不存在");
        }
        return option;
    }

    private DungeonTask findNextTask(DungeonTask task, DungeonTaskOption option) {
        if (option.getNextTaskId() != null) {
            return dungeonTaskMapper.selectById(option.getNextTaskId());
        }
        return listTasks(task.getDungeonId()).stream()
                .filter(candidate -> candidate.getTaskOrder() > task.getTaskOrder())
                .min(Comparator.comparing(DungeonTask::getTaskOrder))
                .orElse(null);
    }

    private void applyRewards(Long userId, DungeonTaskOption option) {
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (attribute == null || profile == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        attribute.setAcademic(clamp(attribute.getAcademic() + option.getAcademicChange()));
        attribute.setHealth(clamp(attribute.getHealth() + option.getHealthChange()));
        attribute.setMoney(clamp(attribute.getMoney() + option.getMoneyChange()));
        attribute.setSocial(clamp(attribute.getSocial() + option.getSocialChange()));
        attribute.setSkill(clamp(attribute.getSkill() + option.getSkillChange()));
        attribute.setPressure(clamp(attribute.getPressure() + option.getPressureChange()));
        attribute.setDiscipline(clamp(attribute.getDiscipline() + option.getDisciplineChange()));
        attribute.setUpdateTime(LocalDateTime.now());
        playerAttributeMapper.updateById(attribute);

        profile.setExp(profile.getExp() + option.getExpChange());
        playerProfileMapper.updateById(profile);
    }

    private void applyDynamicRewards(Long userId, MinigameSettlement settlement) {
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (attribute == null || profile == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        attribute.setAcademic(clamp(attribute.getAcademic() + settlement.academicChange));
        attribute.setHealth(clamp(attribute.getHealth() + settlement.healthChange));
        attribute.setSkill(clamp(attribute.getSkill() + settlement.skillChange));
        attribute.setPressure(clamp(attribute.getPressure() + settlement.pressureChange));
        attribute.setDiscipline(clamp(attribute.getDiscipline() + settlement.disciplineChange));
        attribute.setUpdateTime(LocalDateTime.now());
        playerAttributeMapper.updateById(attribute);

        profile.setExp(profile.getExp() + settlement.expChange);
        playerProfileMapper.updateById(profile);
    }

    private MinigameSettlement settleDatabaseLinks(Long userId, UserDungeonRecord record,
                                                   List<String> selectedRelations, Integer elapsedSeconds) {
        Set<String> correct = Set.of("user->player_attribute", "event->event_option", "dungeon->dungeon_task");
        Set<String> selected = new HashSet<>(selectedRelations == null ? List.of() : selectedRelations);
        int correctCount = (int) selected.stream().filter(correct::contains).count();
        int wrongCount = Math.max(0, selected.size() - correctCount);
        int elapsed = elapsedSeconds == null ? 60 : elapsedSeconds;
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);

        int score = correctCount * 30 - wrongCount * 12;
        if (elapsed <= 25) {
            score += 10;
        } else if (elapsed > 60) {
            score -= 12;
        }
        if (attribute != null && attribute.getSkill() >= 50) {
            score += 8;
        }
        if (hasRiskFlag(record, "scope_controlled")) {
            score += 8;
        }
        if (hasRiskFlag(record, "scope_sprawl")) {
            score -= 18;
        }
        score = Math.max(0, Math.min(100, score));

        if (score >= 80) {
            return new MinigameSettlement(score, "结构清晰",
                    "你把核心关系串起来了：用户有属性，事件有选项，副本有阶段。后续 Bug 阶段少了很多迷雾。",
                    "schema_clear", 5, 0, 10, -5, 6, 45);
        }
        if (score >= 50) {
            return new MinigameSettlement(score, "勉强能用",
                    "表关系大体能讲通，但仍有几个边界不清。后面遇到 Bug 时，你可能还要补救。",
                    "schema_shaky", 2, 0, 4, 3, 1, 20);
        }
        return new MinigameSettlement(score, "表关系迷雾",
                "数据库关系没有真正理顺。你感觉每个页面都在追问：这个字段到底从哪来？",
                "schema_mist", 0, -2, 1, 10, -4, 8);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String buildFinalEvaluation(UserDungeonRecord record) {
        int totalScore = record.getTotalScore();
        if (hasRiskFlag(record, "schema_mist") && totalScore < 140) {
            return "答辩沉默现场";
        }
        if (totalScore >= 230) {
            return "课设战神";
        }
        if (totalScore >= 180) {
            return "DDL 幸存者";
        }
        if (totalScore >= 100) {
            return "勉强过关";
        }
        return "答辩沉默现场";
    }

    private record MinigameSettlement(
            int score,
            String evaluation,
            String resultText,
            String flag,
            int academicChange,
            int healthChange,
            int skillChange,
            int pressureChange,
            int disciplineChange,
            int expChange
    ) {
    }
}
