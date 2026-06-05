package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserDungeonTaskRecord;
import cn.haut.survivor.service.DungeonService;
import cn.haut.survivor.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DungeonController {

    private static final List<String> RELATION_CHOICES = List.of(
            "user->player_attribute",
            "event->event_option",
            "dungeon->dungeon_task",
            "user->event",
            "task->event_option",
            "campus_location->player_attribute"
    );

    private final DungeonService dungeonService;
    private final PlayerService playerService;

    public DungeonController(DungeonService dungeonService, PlayerService playerService) {
        this.dungeonService = dungeonService;
        this.playerService = playerService;
    }

    @GetMapping("/dungeons")
    public String index(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        Dungeon dungeon = dungeonService.findDemoDungeon();
        model.addAttribute("dungeon", dungeon);
        model.addAttribute("tasks", dungeonService.listTasks(dungeon.getId()));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        return "dungeon/index";
    }

    @GetMapping("/dungeons/demo/start")
    public String startDemo(HttpSession session) {
        Long userId = currentUserId(session);
        dungeonService.startOrResumeDemoDungeon(userId);
        return "redirect:/dungeons/demo/play";
    }

    @GetMapping("/dungeons/demo/play")
    public String playDemo(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(userId);
        DungeonTask task = dungeonService.findCurrentTask(record);
        if (task == null) {
            return "redirect:/dungeons";
        }
        model.addAttribute("dungeon", dungeonService.findDemoDungeon());
        model.addAttribute("record", record);
        model.addAttribute("task", task);
        model.addAttribute("options", dungeonService.listOptions(task.getId()));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        model.addAttribute("isMinigameTask", "db_link".equals(task.getMinigameType()));
        model.addAttribute("relationChoices", RELATION_CHOICES);
        return "dungeon/play";
    }

    @PostMapping("/dungeons/demo/task/{taskId}/option/{optionId}")
    public String chooseOption(
            @PathVariable Long taskId,
            @PathVariable Long optionId,
            @RequestParam(required = false) String minigameResult,
            HttpSession session,
            Model model
    ) {
        Long userId = currentUserId(session);
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(userId);
        UserDungeonTaskRecord taskRecord = dungeonService.chooseOption(userId, record.getId(), taskId, optionId, minigameResult);
        UserDungeonRecord updatedRecord = dungeonService.findRecordById(userId, record.getId());

        model.addAttribute("taskRecord", taskRecord);
        model.addAttribute("record", updatedRecord);
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        return "dungeon/result";
    }

    @PostMapping("/dungeons/demo/task/{taskId}/minigame")
    public String submitMinigame(
            @PathVariable Long taskId,
            @RequestParam(required = false) List<String> selectedRelations,
            @RequestParam(required = false) Integer elapsedSeconds,
            HttpSession session,
            Model model
    ) {
        Long userId = currentUserId(session);
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(userId);
        UserDungeonTaskRecord taskRecord = dungeonService.chooseMinigameRelations(userId, record.getId(), taskId,
                selectedRelations == null ? List.of() : selectedRelations, elapsedSeconds);
        UserDungeonRecord updatedRecord = dungeonService.findRecordById(userId, record.getId());

        model.addAttribute("taskRecord", taskRecord);
        model.addAttribute("record", updatedRecord);
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        return "dungeon/result";
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
