package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    Task createTask(Long userId, String taskName, String taskType, String difficulty, LocalDateTime deadline, String description);

    List<Task> listTasksByUserId(Long userId);

    Task completeTask(Long userId, Long taskId);
}
