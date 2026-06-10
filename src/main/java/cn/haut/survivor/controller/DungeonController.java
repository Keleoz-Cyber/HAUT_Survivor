package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserDungeonTaskRecord;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.DungeonService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.WeeklyGoalService;
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
    private final WeeklyGoalService weeklyGoalService;
    private final AchievementService achievementService;

    public DungeonController(DungeonService dungeonService, PlayerService playerService,
                              WeeklyGoalService weeklyGoalService, AchievementService achievementService) {
        this.dungeonService = dungeonService;
        this.playerService = playerService;
        this.weeklyGoalService = weeklyGoalService;
        this.achievementService = achievementService;
    }

    @GetMapping("/dungeons")
    public String index(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        List<Dungeon> dungeons = dungeonService.listAllDungeons();
        model.addAttribute("dungeons", dungeons);
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        return "dungeon/index";
    }

    @GetMapping("/dungeons/{dungeonId}")
    public String detail(@PathVariable Long dungeonId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        Dungeon dungeon = dungeonService.findDungeonById(dungeonId);
        if (dungeon == null) return "redirect:/dungeons";
        model.addAttribute("dungeon", dungeon);
        model.addAttribute("tasks", dungeonService.listTasks(dungeonId));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        return "dungeon/detail";
    }

    @GetMapping("/dungeons/{dungeonId}/start")
    public String startDungeon(@PathVariable Long dungeonId, HttpSession session) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        dungeonService.startOrResumeDungeon(userId, dungeonId);
        return "redirect:/dungeons/" + dungeonId + "/play";
    }

    @GetMapping("/dungeons/{dungeonId}/play")
    public String playDungeon(@PathVariable Long dungeonId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        UserDungeonRecord record = dungeonService.startOrResumeDungeon(userId, dungeonId);
        DungeonTask task = dungeonService.findCurrentTask(record);
        if (task == null) {
            return "redirect:/dungeons";
        }
        Dungeon dungeon = dungeonService.findDungeonById(dungeonId);
        model.addAttribute("dungeon", dungeon);
        model.addAttribute("record", record);
        model.addAttribute("task", task);
        model.addAttribute("options", dungeonService.listOptions(task.getId()));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));

        String minigameType = task.getMinigameType();
        model.addAttribute("isMinigameTask", "db_link".equals(minigameType));
        model.addAttribute("isBugHuntTask", "bug_hunt".equals(minigameType));
        model.addAttribute("relationChoices", RELATION_CHOICES);

        if ("bug_hunt".equals(minigameType)) {
            model.addAttribute("bugQuestions", dungeonService.generateBugQuestions());
        }

        return "dungeon/play";
    }

    @PostMapping("/dungeons/{dungeonId}/task/{taskId}/option/{optionId}")
    public String chooseOption(
            @PathVariable Long dungeonId,
            @PathVariable Long taskId,
            @PathVariable Long optionId,
            @RequestParam(required = false) String minigameResult,
            HttpSession session,
            Model model
    ) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        UserDungeonRecord record = dungeonService.startOrResumeDungeon(userId, dungeonId);
        UserDungeonTaskRecord taskRecord = dungeonService.chooseOption(userId, record.getId(), taskId, optionId, minigameResult);
        UserDungeonRecord updatedRecord = dungeonService.findRecordById(userId, record.getId());

        model.addAttribute("taskRecord", taskRecord);
        model.addAttribute("record", updatedRecord);
        model.addAttribute("dungeon", dungeonService.findDungeonById(dungeonId));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));

        // 更新周目标进度：副本阶段 +1
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "dungeon_stage", 1);

        // 成就：完成副本阶段
        achievementService.unlockAchievement(userId, "dungeon_beginner");

        // 如果副本已完成，检查对应成就
        unlockDungeonCompletionAchievement(userId, dungeonId, updatedRecord);

        return "dungeon/result";
    }

    @PostMapping("/dungeons/{dungeonId}/task/{taskId}/minigame")
    public String submitMinigame(
            @PathVariable Long dungeonId,
            @PathVariable Long taskId,
            @RequestParam(required = false) List<String> selectedRelations,
            @RequestParam(required = false) Integer elapsedSeconds,
            HttpSession session,
            Model model
    ) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        UserDungeonRecord record = dungeonService.startOrResumeDungeon(userId, dungeonId);
        UserDungeonTaskRecord taskRecord = dungeonService.chooseMinigameRelations(userId, record.getId(), taskId,
                selectedRelations == null ? List.of() : selectedRelations, elapsedSeconds);
        UserDungeonRecord updatedRecord = dungeonService.findRecordById(userId, record.getId());

        model.addAttribute("taskRecord", taskRecord);
        model.addAttribute("record", updatedRecord);
        model.addAttribute("dungeon", dungeonService.findDungeonById(dungeonId));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));

        // 更新周目标进度：副本阶段 +1
        PlayerProfile profile2 = playerService.findProfileByUserId(userId);
        weeklyGoalService.updateProgress(userId, profile2.getCurrentWeek(), "dungeon_stage", 1);

        // 成就：完成副本阶段
        achievementService.unlockAchievement(userId, "dungeon_beginner");
        unlockDungeonCompletionAchievement(userId, dungeonId, updatedRecord);

        return "dungeon/result";
    }

    @PostMapping("/dungeons/{dungeonId}/task/{taskId}/bughunt")
    public String submitBugHunt(
            @PathVariable Long dungeonId,
            @PathVariable Long taskId,
            @RequestParam("questionIds") List<Integer> questionIds,
            @RequestParam("answers") List<Integer> answers,
            @RequestParam(required = false) Integer elapsedSeconds,
            HttpSession session,
            Model model
    ) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        UserDungeonRecord record = dungeonService.startOrResumeDungeon(userId, dungeonId);
        UserDungeonTaskRecord taskRecord = dungeonService.chooseBugHunt(
                userId, record.getId(), taskId, questionIds, answers, elapsedSeconds);
        UserDungeonRecord updatedRecord = dungeonService.findRecordById(userId, record.getId());

        model.addAttribute("taskRecord", taskRecord);
        model.addAttribute("record", updatedRecord);
        model.addAttribute("dungeon", dungeonService.findDungeonById(dungeonId));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));

        // 更新周目标进度：副本阶段 +1
        PlayerProfile profile3 = playerService.findProfileByUserId(userId);
        weeklyGoalService.updateProgress(userId, profile3.getCurrentWeek(), "dungeon_stage", 1);

        // 成就：完成副本阶段
        achievementService.unlockAchievement(userId, "dungeon_beginner");
        unlockDungeonCompletionAchievement(userId, dungeonId, updatedRecord);

        return "dungeon/result";
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }

    private void unlockDungeonCompletionAchievement(Long userId, Long dungeonId, UserDungeonRecord updatedRecord) {
        if (!"COMPLETED".equals(updatedRecord.getStatus())) {
            return;
        }
        if (dungeonId == 1L) {
            achievementService.unlockAchievement(userId, "java_survivor");
        } else if (dungeonId == 2L) {
            achievementService.unlockAchievement(userId, "fitness_survivor");
        } else if (dungeonId == 3L) {
            achievementService.unlockAchievement(userId, "ddl_survivor_plus");
        }
    }
}
