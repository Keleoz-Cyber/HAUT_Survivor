package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class DungeonServiceImpl implements DungeonService {

    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";

    // Bug 定位题库：5 道题，每次随机抽 3 道
    private static final List<BugQuestion> ALL_BUG_QUESTIONS = List.of(
            new BugQuestion(0,
                    "提交事件选项后页面显示 404",
                    List.of("数据库连接失败", "Controller 路径或表单 action 不一致", "前端 CSS 加载错误", "用户 Session 过期"),
                    1),
            new BugQuestion(1,
                    "数据库字段读出来全是 null",
                    List.of("实体字段和数据库列名映射不一致", "页面渲染模板错误", "Service 层未调用 Mapper", "MySQL 服务未启动"),
                    0),
            new BugQuestion(2,
                    "启动报错：Mapper 无法注入",
                    List.of("Mapper 接口未加 @Mapper 或未配置扫描", "数据库密码错误", "Controller 路径冲突", "Thymeleaf 模板语法错误"),
                    0),
            new BugQuestion(3,
                    "启动失败：端口 8080 已被占用",
                    List.of("application.yml 配置语法错误", "MySQL 占用了 8080", "防火墙拦截请求", "上一个 Spring Boot 进程未关闭"),
                    3),
            new BugQuestion(4,
                    "选择事件选项后属性值没有变化",
                    List.of("前端表单提交地址错误", "数据库字段类型不匹配", "Service 方法中没有调用 updateById", "Thymeleaf 缓存问题"),
                    2)
    );

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
    public List<Dungeon> listAllDungeons() {
        return dungeonMapper.selectList(new LambdaQueryWrapper<Dungeon>()
                .eq(Dungeon::getStatus, 1)
                .orderByAsc(Dungeon::getId));
    }

    @Override
    public Dungeon findDungeonById(Long dungeonId) {
        return dungeonMapper.selectById(dungeonId);
    }

    @Override
    @Transactional
    public UserDungeonRecord startOrResumeDungeon(Long userId, Long dungeonId) {
        if (!playerService.hasProfile(userId)) {
            throw new IllegalArgumentException("请先创建角色");
        }
        Dungeon dungeon = dungeonMapper.selectById(dungeonId);
        if (dungeon == null || dungeon.getStatus() != 1) {
            throw new IllegalArgumentException("副本不存在");
        }
        UserDungeonRecord record = userDungeonRecordMapper.selectOne(new LambdaQueryWrapper<UserDungeonRecord>()
                .eq(UserDungeonRecord::getUserId, userId)
                .eq(UserDungeonRecord::getDungeonId, dungeonId)
                .eq(UserDungeonRecord::getStatus, IN_PROGRESS)
                .last("limit 1"));
        if (record != null) {
            return record;
        }

        DungeonTask firstTask = listTasks(dungeonId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("副本缺少阶段任务"));
        record = new UserDungeonRecord();
        record.setUserId(userId);
        record.setDungeonId(dungeonId);
        record.setCurrentTaskId(firstTask.getId());
        record.setStatus(IN_PROGRESS);
        record.setTotalScore(0);
        record.setStartTime(LocalDateTime.now());
        userDungeonRecordMapper.insert(record);
        return record;
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

        // 记录旧值用于计算实际变化
        PlayerAttribute beforeAttr = playerService.findAttributeByUserId(userId);
        int oldAcademic = beforeAttr.getAcademic(), oldHealth = beforeAttr.getHealth();
        int oldMoney = beforeAttr.getMoney(), oldSocial = beforeAttr.getSocial();
        int oldSkill = beforeAttr.getSkill(), oldPressure = beforeAttr.getPressure();
        int oldDiscipline = beforeAttr.getDiscipline();

        applyRewards(userId, option);

        PlayerAttribute afterAttr = playerService.findAttributeByUserId(userId);

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
        // 使用实际变化值，避免 clamp 边界不准
        taskRecord.setAttributeChange(new AttributeChange(
                afterAttr.getAcademic() - oldAcademic, afterAttr.getHealth() - oldHealth,
                afterAttr.getMoney() - oldMoney, afterAttr.getSocial() - oldSocial,
                afterAttr.getSkill() - oldSkill, afterAttr.getPressure() - oldPressure,
                afterAttr.getDiscipline() - oldDiscipline, option.getExpChange()));
        userDungeonTaskRecordMapper.insert(taskRecord);

        record.setTotalScore(record.getTotalScore() + option.getScore());
        applyOptionFlags(record, task, option);
        advanceToNextTask(record, task);
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

        // 记录旧值用于计算实际变化
        PlayerAttribute beforeAttr = playerService.findAttributeByUserId(userId);
        int oldAcademic = beforeAttr.getAcademic(), oldHealth = beforeAttr.getHealth();
        int oldSkill = beforeAttr.getSkill(), oldPressure = beforeAttr.getPressure();
        int oldDiscipline = beforeAttr.getDiscipline();

        applyDynamicRewards(userId, settlement);

        PlayerAttribute afterAttr = playerService.findAttributeByUserId(userId);

        UserDungeonTaskRecord taskRecord = buildMinigameTaskRecord(record, task, settlement,
                "relations=" + String.join("|", selectedRelations == null ? List.of() : selectedRelations)
                        + ";elapsed=" + (elapsedSeconds == null ? 0 : elapsedSeconds));
        // 替换为实际变化值
        taskRecord.setAttributeChange(new AttributeChange(
                afterAttr.getAcademic() - oldAcademic, afterAttr.getHealth() - oldHealth, 0,
                0, afterAttr.getSkill() - oldSkill, afterAttr.getPressure() - oldPressure,
                afterAttr.getDiscipline() - oldDiscipline, settlement.expChange));
        userDungeonTaskRecordMapper.insert(taskRecord);

        record.setTotalScore(record.getTotalScore() + settlement.score);
        addRiskFlag(record, settlement.flag);
        advanceToNextTask(record, task);
        userDungeonRecordMapper.updateById(record);
        return taskRecord;
    }

    @Override
    public List<BugQuestion> generateBugQuestions() {
        List<BugQuestion> shuffled = new ArrayList<>(ALL_BUG_QUESTIONS);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, 3);
    }

    @Override
    @Transactional
    public UserDungeonTaskRecord chooseBugHunt(Long userId, Long recordId, Long taskId,
                                                List<Integer> questionIds, List<Integer> answers,
                                                Integer elapsedSeconds) {
        UserDungeonRecord record = requireRecord(userId, recordId);
        if (!IN_PROGRESS.equals(record.getStatus())) {
            throw new IllegalArgumentException("副本已经结束");
        }
        if (!taskId.equals(record.getCurrentTaskId())) {
            throw new IllegalArgumentException("只能结算当前阶段任务");
        }
        DungeonTask task = requireTask(taskId);
        if (!"bug_hunt".equals(task.getMinigameType())) {
            throw new IllegalArgumentException("当前阶段不是 Bug 定位小游戏");
        }

        MinigameSettlement settlement = settleBugHunt(userId, record, questionIds, answers, elapsedSeconds);

        // 记录旧值用于计算实际变化
        PlayerAttribute beforeAttr2 = playerService.findAttributeByUserId(userId);
        int oldAcademic2 = beforeAttr2.getAcademic(), oldHealth2 = beforeAttr2.getHealth();
        int oldSkill2 = beforeAttr2.getSkill(), oldPressure2 = beforeAttr2.getPressure();
        int oldDiscipline2 = beforeAttr2.getDiscipline();

        applyDynamicRewards(userId, settlement);

        PlayerAttribute afterAttr2 = playerService.findAttributeByUserId(userId);

        UserDungeonTaskRecord taskRecord = buildMinigameTaskRecord(record, task, settlement,
                "correct=" + settlement.score + ";elapsed=" + (elapsedSeconds == null ? 0 : elapsedSeconds));
        // 替换为实际变化值
        taskRecord.setAttributeChange(new AttributeChange(
                afterAttr2.getAcademic() - oldAcademic2, afterAttr2.getHealth() - oldHealth2, 0,
                0, afterAttr2.getSkill() - oldSkill2, afterAttr2.getPressure() - oldPressure2,
                afterAttr2.getDiscipline() - oldDiscipline2, settlement.expChange));
        userDungeonTaskRecordMapper.insert(taskRecord);

        record.setTotalScore(record.getTotalScore() + settlement.score);
        addRiskFlag(record, settlement.flag);
        advanceToNextTask(record, task);
        userDungeonRecordMapper.updateById(record);
        return taskRecord;
    }

    // ==================== Bug Hunt 结算 ====================

    private MinigameSettlement settleBugHunt(Long userId, UserDungeonRecord record,
                                              List<Integer> questionIds, List<Integer> answers,
                                              Integer elapsedSeconds) {
        int correctCount = 0;
        for (int i = 0; i < questionIds.size() && i < answers.size(); i++) {
            int qId = questionIds.get(i);
            if (qId >= 0 && qId < ALL_BUG_QUESTIONS.size()) {
                BugQuestion question = ALL_BUG_QUESTIONS.get(qId);
                if (answers.get(i) == question.correctIndex()) {
                    correctCount++;
                }
            }
        }

        int score = correctCount * 30;
        int elapsed = elapsedSeconds == null ? 60 : elapsedSeconds;
        if (elapsed <= 30) {
            score += 10;
        } else if (elapsed > 60) {
            score -= 10;
        }

        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        if (attribute != null && attribute.getSkill() >= 50) {
            score += 8;
        }
        if (hasRiskFlag(record, "schema_clear")) {
            score += 8;
        }
        if (hasRiskFlag(record, "schema_mist")) {
            score -= 15;
        }
        if (hasRiskFlag(record, "scope_sprawl")) {
            score -= 10;
        }
        score = Math.max(0, Math.min(100, score));

        if (score >= 80) {
            return new MinigameSettlement(score, "Bug 猎人",
                    "你精准定位了关键 Bug，控制台终于安静了。答辩前夜，你第一次觉得项目真的能跑。",
                    "bug_crushed", 3, -1, 12, -8, 5, 50);
        }
        if (score >= 50) {
            return new MinigameSettlement(score, "勉强修复",
                    "部分 Bug 被修掉了，但还有几个隐患在暗处等待。答辩时祈祷老师别点得太深。",
                    "bug_survived", 1, 0, 5, 3, 1, 25);
        }
        return new MinigameSettlement(score, "Bug 反杀",
                "Bug 没修掉几个，还引入了新的。控制台比之前更红了。",
                "bug_avalanche", -2, -2, -3, 12, -4, 8);
    }

    // ==================== 数据库拼图结算 ====================

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

    // ==================== 共享逻辑 ====================

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

    private void advanceToNextTask(UserDungeonRecord record, DungeonTask currentTask) {
        DungeonTask nextTask = listTasks(currentTask.getDungeonId()).stream()
                .filter(candidate -> candidate.getTaskOrder() > currentTask.getTaskOrder())
                .min(Comparator.comparing(DungeonTask::getTaskOrder))
                .orElse(null);
        if (nextTask == null) {
            record.setCurrentTaskId(null);
            record.setStatus(COMPLETED);
            record.setFinalEvaluation(buildFinalEvaluation(record));
            record.setFinishTime(LocalDateTime.now());
        } else {
            record.setCurrentTaskId(nextTask.getId());
        }
    }

    private UserDungeonTaskRecord buildMinigameTaskRecord(UserDungeonRecord record, DungeonTask task,
                                                           MinigameSettlement settlement, String minigameResult) {
        UserDungeonTaskRecord taskRecord = new UserDungeonTaskRecord();
        taskRecord.setUserDungeonRecordId(record.getId());
        taskRecord.setDungeonTaskId(task.getId());
        taskRecord.setTaskType(task.getTaskType());
        taskRecord.setMinigameResult(minigameResult);
        taskRecord.setAttributeCheckResult(settlement.score >= 80 ? "excellent" : settlement.score >= 50 ? "pass" : "fail");
        taskRecord.setResultText(settlement.resultText);
        taskRecord.setEvaluation(settlement.evaluation);
        taskRecord.setScore(settlement.score);
        taskRecord.setExpChange(settlement.expChange);
        taskRecord.setCreateTime(LocalDateTime.now());
        taskRecord.setAttributeChange(new AttributeChange(
                settlement.academicChange, settlement.healthChange, 0,
                0, settlement.skillChange, settlement.pressureChange,
                settlement.disciplineChange, settlement.expChange));
        return taskRecord;
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

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String buildFinalEvaluation(UserDungeonRecord record) {
        int totalScore = record.getTotalScore();
        if (hasRiskFlag(record, "schema_mist") && totalScore < 140) {
            return "答辩沉默现场";
        }
        if (hasRiskFlag(record, "bug_avalanche") && totalScore < 160) {
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
