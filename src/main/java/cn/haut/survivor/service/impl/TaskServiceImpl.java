package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.Task;
import cn.haut.survivor.domain.enums.TaskStatus;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.mapper.TaskMapper;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.TaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Map<String, Integer> REWARD_EXP_BY_DIFFICULTY = Map.of(
            "C", 20,
            "B", 40,
            "A", 70,
            "S", 120
    );

    private final TaskMapper taskMapper;
    private final PlayerService playerService;
    private final PlayerProfileMapper playerProfileMapper;

    public TaskServiceImpl(TaskMapper taskMapper, PlayerService playerService, PlayerProfileMapper playerProfileMapper) {
        this.taskMapper = taskMapper;
        this.playerService = playerService;
        this.playerProfileMapper = playerProfileMapper;
    }

    @Override
    @Transactional
    public Task createTask(Long userId, String taskName, String taskType, String difficulty, LocalDateTime deadline, String description) {
        requireProfile(userId);
        String normalizedDifficulty = normalizeDifficulty(difficulty);

        Task task = new Task();
        task.setUserId(userId);
        task.setTaskName(requireText(taskName, "任务名称不能为空"));
        task.setTaskType(requireText(taskType, "任务类型不能为空"));
        task.setDifficulty(normalizedDifficulty);
        task.setDeadline(deadline);
        task.setStatus(TaskStatus.PENDING.name());
        task.setRewardExp(REWARD_EXP_BY_DIFFICULTY.get(normalizedDifficulty));
        task.setDescription(description);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
        return task;
    }

    @Override
    public List<Task> listTasksByUserId(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .orderByAsc(Task::getStatus)
                .orderByAsc(Task::getDeadline)
                .orderByDesc(Task::getCreateTime));
    }

    @Override
    @Transactional
    public Task completeTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("不能完成其他用户的任务");
        }
        if (TaskStatus.DONE.name().equals(task.getStatus())) {
            return task;
        }

        task.setStatus(TaskStatus.DONE.name());
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);

        PlayerProfile profile = requireProfile(userId);
        profile.setExp(profile.getExp() + task.getRewardExp());
        playerProfileMapper.updateById(profile);
        return task;
    }

    private PlayerProfile requireProfile(Long userId) {
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("请先创建角色");
        }
        return profile;
    }

    private String normalizeDifficulty(String difficulty) {
        String normalized = requireText(difficulty, "任务难度不能为空").toUpperCase(Locale.ROOT);
        if (!REWARD_EXP_BY_DIFFICULTY.containsKey(normalized)) {
            throw new IllegalArgumentException("任务难度只能是 C、B、A 或 S");
        }
        return normalized;
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
