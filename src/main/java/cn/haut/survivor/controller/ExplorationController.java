package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.UserLocationExploration;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.EventService;
import cn.haut.survivor.service.ExplorationService;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.RumorService;
import cn.haut.survivor.service.WeeklyGoalService;
import cn.haut.survivor.service.WeeklyThemeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ExplorationController {

    private final ExplorationService explorationService;
    private final PlayerService playerService;
    private final EventService eventService;
    private final WeeklyThemeService weeklyThemeService;
    private final RumorService rumorService;
    private final NpcService npcService;
    private final WeeklyGoalService weeklyGoalService;
    private final AchievementService achievementService;

    public ExplorationController(ExplorationService explorationService, PlayerService playerService,
                                 EventService eventService, WeeklyThemeService weeklyThemeService,
                                 RumorService rumorService, NpcService npcService,
                                 WeeklyGoalService weeklyGoalService, AchievementService achievementService) {
        this.explorationService = explorationService;
        this.playerService = playerService;
        this.eventService = eventService;
        this.weeklyThemeService = weeklyThemeService;
        this.rumorService = rumorService;
        this.npcService = npcService;
        this.weeklyGoalService = weeklyGoalService;
        this.achievementService = achievementService;
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
        model.addAttribute("weekTheme", weeklyThemeService.getTheme(profile.getCurrentWeek()));
        model.addAttribute("rumors", rumorService.pickRumorsForUser(userId, profile.getCurrentWeek(), 3));
        model.addAttribute("rumorCountsByLocation", rumorService.pickVisibleRumorsForUser(userId, profile.getCurrentWeek()).stream()
                .filter(r -> r.getLocationId() != null)
                .collect(Collectors.groupingBy(Rumor::getLocationId, Collectors.counting())));
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
            PlayerProfile profile = playerService.findProfileByUserId(userId);

            // 更新周目标进度：探索次数 +1
            weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "explore_count", 1);

            model.addAttribute("result", result);
            model.addAttribute("location", findLocation(locationId));
            model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
            model.addAttribute("profile", profile);

            // NPC 遇见
            var npcEncounter = npcService.maybeMeetNpc(userId, locationId, profile.getCurrentWeek());
            model.addAttribute("npcEncounter", npcEncounter);

            // 如果遇见了 NPC，更新周目标进度
            if (npcEncounter != null && npcEncounter.isPresent()) {
                weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "npc_meet", 1);
                // 成就：认识 NPC
                achievementService.unlockAchievement(userId, "social_starter");
            }

            // 成就：探索次数检查
            List<UserLocationExploration> allExplorations = explorationService.listUserExplorations(userId);
            int totalExploreCount = allExplorations.stream()
                    .mapToInt(e -> e.getExploreCount() != null ? e.getExploreCount() : 0)
                    .sum();
            achievementService.unlockIfEligible(userId, "explore_count", totalExploreCount);

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
