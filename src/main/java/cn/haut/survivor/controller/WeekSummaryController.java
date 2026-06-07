package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.WeekSummaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WeekSummaryController {

    private final PlayerService playerService;
    private final WeekSummaryService weekSummaryService;

    public WeekSummaryController(PlayerService playerService, WeekSummaryService weekSummaryService) {
        this.playerService = playerService;
        this.weekSummaryService = weekSummaryService;
    }

    @GetMapping("/week/summary")
    public String summary(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        if (playerService.isSemesterOver(userId)) {
            return "redirect:/ending";
        }

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        WeekSummaryService.WeekSummaryView summaryView =
                weekSummaryService.buildCurrentWeekSummary(userId, profile.getCurrentWeek());

        model.addAttribute("summary", summaryView);
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        model.addAttribute("semesterOver", false);
        return "week/summary";
    }

    @PostMapping("/week/advance")
    public String advanceWeek(HttpSession session) {
        Long userId = currentUserId(session);
        try {
            playerService.advanceWeek(userId);
        } catch (IllegalArgumentException ignored) {
            // 学期结束或其他异常
        }

        if (playerService.isSemesterOver(userId)) {
            return "redirect:/ending";
        }
        return "redirect:/dashboard";
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
