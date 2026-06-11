package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.SemesterEnding;
import cn.haut.survivor.domain.entity.UserSemesterEnding;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.SemesterArchiveService;
import cn.haut.survivor.service.SemesterEndingService;
import cn.haut.survivor.service.EndingScoreService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class SemesterEndingController {

    private final SemesterEndingService semesterEndingService;
    private final PlayerService playerService;
    private final SemesterArchiveService semesterArchiveService;
    private final EndingScoreService endingScoreService;

    public SemesterEndingController(SemesterEndingService semesterEndingService,
                                    PlayerService playerService,
                                    SemesterArchiveService semesterArchiveService,
                                    EndingScoreService endingScoreService) {
        this.semesterEndingService = semesterEndingService;
        this.playerService = playerService;
        this.semesterArchiveService = semesterArchiveService;
        this.endingScoreService = endingScoreService;
    }

    @GetMapping("/ending")
    public String endingPage(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        UserSemesterEnding userEnding = semesterEndingService.findUserEnding(userId);
        List<UserSemesterEnding> history = semesterEndingService.listUserEndingHistory(userId);

        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
        model.addAttribute("hasSettled", semesterEndingService.hasSettled(userId));
        model.addAttribute("userEnding", userEnding);
        model.addAttribute("history", history);
        model.addAttribute("growthPortrait", semesterArchiveService.buildSummary(userId));
        model.addAttribute("endingScoreReport", endingScoreService.buildScoreReport(userId));

        if (userEnding != null) {
            SemesterEnding ending = semesterEndingService.listAllEndings().stream()
                    .filter(e -> e.getId().equals(userEnding.getEndingId()))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("ending", ending);
        }

        return "ending/index";
    }

    @PostMapping("/ending/settle")
    public String settleSemester(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        try {
            SemesterEnding ending = semesterEndingService.settleSemester(userId);
            model.addAttribute("ending", ending);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/ending";
    }

    @PostMapping("/ending/restart")
    public String restartSemester(HttpSession session) {
        Long userId = currentUserId(session);
        playerService.resetSemester(userId);
        return "redirect:/dashboard";
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
