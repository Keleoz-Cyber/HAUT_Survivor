package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.User;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    private static final List<String> GROWTH_ROUTES = List.of("考研路线", "就业路线", "竞赛路线", "六边形路线", "摆烂求生路线");

    private final PlayerService playerService;
    private final UserService userService;

    public DashboardController(PlayerService playerService, UserService userService) {
        this.playerService = playerService;
        this.userService = userService;
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
        return "dashboard/index";
    }

    @PostMapping("/week/advance")
    public String advanceWeek(HttpSession session) {
        Long userId = currentUserId(session);
        try {
            playerService.advanceWeek(userId);
        } catch (IllegalArgumentException ignored) {
            // 学期结束或其他异常，回到仪表盘显示状态
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
