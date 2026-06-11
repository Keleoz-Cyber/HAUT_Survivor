package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.User;
import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.RumorService;
import cn.haut.survivor.service.SemesterCalendarService;
import cn.haut.survivor.service.UserService;
import cn.haut.survivor.service.WeeklyGoalService;
import cn.haut.survivor.service.WeeklyThemeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    private static final List<String> GROWTH_ROUTES = List.of("考研路线", "就业路线", "竞赛路线", "六边形路线", "摆烂求生路线");

    private final PlayerService playerService;
    private final UserService userService;
    private final WeeklyThemeService weeklyThemeService;
    private final SemesterCalendarService semesterCalendarService;
    private final RumorService rumorService;
    private final NpcService npcService;
    private final WeeklyGoalService weeklyGoalService;
    private final AchievementService achievementService;

    public DashboardController(PlayerService playerService, UserService userService,
                               WeeklyThemeService weeklyThemeService,
                               SemesterCalendarService semesterCalendarService,
                               RumorService rumorService,
                               NpcService npcService, WeeklyGoalService weeklyGoalService,
                               AchievementService achievementService) {
        this.playerService = playerService;
        this.userService = userService;
        this.weeklyThemeService = weeklyThemeService;
        this.semesterCalendarService = semesterCalendarService;
        this.rumorService = rumorService;
        this.npcService = npcService;
        this.weeklyGoalService = weeklyGoalService;
        this.achievementService = achievementService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        User user = userService.findById(userId);
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("warnings", buildWarnings(attribute, profile));
        model.addAttribute("weekPhaseLabel", playerService.getWeekPhaseLabel(profile));
        model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
        model.addAttribute("weekTheme", weeklyThemeService.getTheme(profile.getCurrentWeek()));

        // 阶段进度信息
        var currentStage = semesterCalendarService.stageForWeek(profile.getCurrentWeek());
        var nextStage = semesterCalendarService.nextStage(profile.getCurrentWeek());
        model.addAttribute("stageKey", currentStage.stageKey());
        model.addAttribute("stageName", currentStage.name());
        model.addAttribute("stageIcon", currentStage.icon());
        model.addAttribute("weeksLeftInStage", semesterCalendarService.weeksLeftInStage(profile.getCurrentWeek()));
        model.addAttribute("semesterWeeks", semesterCalendarService.semesterWeeks());
        model.addAttribute("allStages", semesterCalendarService.allStages());
        model.addAttribute("nextStageName", nextStage != null ? nextStage.name() : null);
        model.addAttribute("rumors", rumorService.pickRumorsForUser(userId, profile.getCurrentWeek(), 3));
        model.addAttribute("knownNpcs", npcService.listKnownNpcs(userId));
        model.addAttribute("currentBuddy", npcService.getCurrentBuddy(userId, profile.getCurrentWeek()).orElse(null));

        // 本周目标
        UserWeeklyGoal currentGoal = weeklyGoalService.getCurrentGoal(userId, profile.getCurrentWeek());
        WeeklyGoal currentGoalDef = weeklyGoalService.getCurrentGoalDefinition(userId, profile.getCurrentWeek());

        // 检查压力保持目标
        if (currentGoal != null && currentGoalDef != null
                && "pressure_keep".equals(currentGoalDef.getGoalType())
                && currentGoal.getCompleted() == 0) {
            weeklyGoalService.checkPressureKeepGoal(userId, profile.getCurrentWeek());
            // 重新获取更新后的目标
            currentGoal = weeklyGoalService.getCurrentGoal(userId, profile.getCurrentWeek());
        }

        model.addAttribute("currentWeeklyGoal", currentGoal);
        model.addAttribute("currentWeeklyGoalDef", currentGoalDef);
        if (currentGoal == null) {
            model.addAttribute("goalCandidates", weeklyGoalService.pickCandidateGoals(userId, profile.getCurrentWeek()));
        } else {
            model.addAttribute("goalCandidates", List.of());
        }

        // 成就称号
        model.addAttribute("unlockedAchievements", achievementService.listUserAchievements(userId));
        model.addAttribute("recentAchievements", achievementService.listRecentUnlocked(userId, 5));

        return "dashboard/index";
    }

    @PostMapping("/weekly-goals/{goalId}/choose")
    public String chooseGoal(@PathVariable Long goalId, HttpSession session) {
        Long userId = currentUserId(session);
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        try {
            weeklyGoalService.chooseGoal(userId, profile.getCurrentWeek(), goalId);
        } catch (IllegalArgumentException ignored) {
            // 已选择目标或目标无效，忽略
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/weekly-goals/claim")
    public String claimReward(HttpSession session) {
        Long userId = currentUserId(session);
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        try {
            weeklyGoalService.claimReward(userId, profile.getCurrentWeek());
        } catch (IllegalArgumentException ignored) {
            // 未完成或已领取，忽略
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/player/create")
    public String createPlayerPage(HttpSession session, Model model) {
        if (playerService.hasProfile(currentUserId(session))) {
            return "redirect:/dashboard";
        }
        model.addAttribute("growthRoutes", GROWTH_ROUTES);
        return "player/create";
    }

    @PostMapping("/player/create")
    public String createPlayer(
            String playerName,
            String grade,
            String majorType,
            String growthRoute,
            HttpSession session,
            Model model
    ) {
        try {
            playerService.createProfile(currentUserId(session), playerName, grade, majorType, growthRoute);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("playerName", playerName);
            model.addAttribute("grade", grade);
            model.addAttribute("majorType", majorType);
            model.addAttribute("growthRoute", growthRoute);
            model.addAttribute("growthRoutes", GROWTH_ROUTES);
            return "player/create";
        }
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }

    private List<String> buildWarnings(PlayerAttribute attribute, PlayerProfile profile) {
        List<String> warnings = new ArrayList<>();
        if (attribute.getHealth() < 40) {
            warnings.add("健康值偏低，建议安排休息或运动。");
        }
        if (attribute.getMoney() < 30) {
            warnings.add("生活费余额紧张，注意控制消费。");
        }
        if (attribute.getAcademic() < 40) {
            warnings.add("学业值偏低，近期需要补课或复习。");
        }
        if (attribute.getPressure() > 75) {
            warnings.add("压力值过高，建议降低任务强度。");
        }
        if (profile.getActionPoints() <= 1 && !playerService.isSemesterOver(profile.getUserId())) {
            warnings.add("本周行动点即将耗尽，请合理安排剩余行动。");
        }
        return warnings;
    }
}
