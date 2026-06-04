package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TaskController {

    private static final List<String> DIFFICULTIES = List.of("C", "B", "A", "S");

    private final TaskService taskService;
    private final PlayerService playerService;

    public TaskController(TaskService taskService, PlayerService playerService) {
        this.taskService = taskService;
        this.playerService = playerService;
    }

    @GetMapping("/tasks")
    public String tasks(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        fillTaskPageModel(userId, model);
        return "task/index";
    }

    @PostMapping("/tasks")
    public String createTask(
            @RequestParam String taskName,
            @RequestParam String taskType,
            @RequestParam String difficulty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline,
            @RequestParam(required = false) String description,
            HttpSession session,
            Model model
    ) {
        Long userId = currentUserId(session);
        try {
            taskService.createTask(userId, taskName, taskType, difficulty, deadline, description);
            return "redirect:/tasks";
        } catch (IllegalArgumentException e) {
            fillTaskPageModel(userId, model);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("taskName", taskName);
            model.addAttribute("taskType", taskType);
            model.addAttribute("difficulty", difficulty);
            model.addAttribute("description", description);
            return "task/index";
        }
    }

    @PostMapping("/tasks/{taskId}/complete")
    public String completeTask(@PathVariable Long taskId, HttpSession session) {
        taskService.completeTask(currentUserId(session), taskId);
        return "redirect:/tasks";
    }

    private void fillTaskPageModel(Long userId, Model model) {
        model.addAttribute("tasks", taskService.listTasksByUserId(userId));
        model.addAttribute("difficulties", DIFFICULTIES);
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
