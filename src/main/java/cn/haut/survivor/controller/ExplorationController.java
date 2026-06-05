package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserLocationExploration;
import cn.haut.survivor.service.EventService;
import cn.haut.survivor.service.ExplorationService;
import cn.haut.survivor.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ExplorationController {

    private final ExplorationService explorationService;
    private final PlayerService playerService;
    private final EventService eventService;

    public ExplorationController(ExplorationService explorationService, PlayerService playerService, EventService eventService) {
        this.explorationService = explorationService;
        this.playerService = playerService;
        this.eventService = eventService;
    }

    @GetMapping("/exploration")
    public String explorationPage(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        List<CampusLocation> locations = eventService.listEnabledLocations();
        List<UserLocationExploration> explorations = explorationService.listUserExplorations(userId);

        // 构建地点探索度映射
        Map<Long, Integer> exploreLevels = new HashMap<>();
        for (UserLocationExploration e : explorations) {
            exploreLevels.put(e.getLocationId(), e.getExploreLevel());
        }

        model.addAttribute("locations", locations);
        model.addAttribute("exploreLevels", exploreLevels);
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
        return "exploration/index";
    }

    @PostMapping("/exploration/{locationId}")
    public String explore(@PathVariable Long locationId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        try {
            ExplorationService.ExplorationResult result = explorationService.explore(userId, locationId);
            model.addAttribute("result", result);
            model.addAttribute("location", findLocation(locationId));
            model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
            model.addAttribute("profile", playerService.findProfileByUserId(userId));
            return "exploration/result";
        } catch (IllegalArgumentException e) {
            // 行动点不足或学期结束
            return "redirect:/exploration?error=" + e.getMessage();
        }
    }

    private CampusLocation findLocation(Long locationId) {
        List<CampusLocation> locations = eventService.listEnabledLocations();
        return locations.stream()
                .filter(loc -> loc.getId().equals(locationId))
                .findFirst()
                .orElse(null);
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
