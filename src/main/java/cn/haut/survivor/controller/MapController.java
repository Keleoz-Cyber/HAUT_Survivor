package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventRecord;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserLocationExploration;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.service.EventService;
import cn.haut.survivor.service.ExplorationService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.RumorService;
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

@Controller
public class MapController {

    private final EventService eventService;
    private final PlayerService playerService;
    private final ExplorationService explorationService;
    private final WeeklyThemeService weeklyThemeService;
    private final RumorService rumorService;

    public MapController(EventService eventService, PlayerService playerService,
                         ExplorationService explorationService,
                         WeeklyThemeService weeklyThemeService, RumorService rumorService) {
        this.eventService = eventService;
        this.playerService = playerService;
        this.explorationService = explorationService;
        this.weeklyThemeService = weeklyThemeService;
        this.rumorService = rumorService;
    }

    @GetMapping("/map")
    public String map(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        List<CampusLocation> locations = eventService.listEnabledLocations();

        // 获取探索度映射
        Map<Long, Integer> exploreLevels = buildExploreLevelMap(userId);

        model.addAttribute("locations", locations);
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("exploreLevels", exploreLevels);
        model.addAttribute("statusLines", buildStatusLines(profile, attribute));
        model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
        model.addAttribute("weekTheme", weeklyThemeService.getTheme(profile.getCurrentWeek()));
        model.addAttribute("rumors", rumorService.pickRumorsForUser(userId, profile.getCurrentWeek(), 3));
        return "map/index";
    }

    @GetMapping("/map/location/{locationId}/event")
    public String triggerEvent(@PathVariable Long locationId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        // 检查学期是否结束
        if (playerService.isSemesterOver(userId)) {
            return buildMapRedirect(userId, model, "学期已结束，无法继续行动。", true);
        }

        // 先检查是否有可触发的事件，没有则不消耗行动点
        Event event = eventService.triggerRandomEvent(userId, locationId);
        if (event == null) {
            return buildMapRedirect(userId, model, "这里暂时没有可触发的事件。", false);
        }

        // 有事件才消耗行动点
        try {
            playerService.consumeActionPoint(userId);
        } catch (IllegalArgumentException e) {
            return buildMapRedirect(userId, model, e.getMessage(), false);
        }

        model.addAttribute("event", event);
        model.addAttribute("options", eventService.listOptions(event.getId()));
        model.addAttribute("location", findLocation(locationId));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        return "map/event";
    }

    @PostMapping("/map/event/{eventId}/option/{optionId}")
    public String chooseOption(@PathVariable Long eventId, @PathVariable Long optionId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        EventRecord record = eventService.chooseOption(userId, eventId, optionId);
        model.addAttribute("resultText", record.getResultText());
        model.addAttribute("attributeChange", record.getAttributeChange());
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        model.addAttribute("profile", playerService.findProfileByUserId(userId));
        return "map/event";
    }

    private String buildMapRedirect(Long userId, Model model, String message, boolean semesterOver) {
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        model.addAttribute("locations", eventService.listEnabledLocations());
        model.addAttribute("message", message);
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("exploreLevels", buildExploreLevelMap(userId));
        model.addAttribute("statusLines", buildStatusLines(profile, attribute));
        model.addAttribute("semesterOver", semesterOver);
        return "map/index";
    }

    private Map<Long, Integer> buildExploreLevelMap(Long userId) {
        List<UserLocationExploration> explorations = explorationService.listUserExplorations(userId);
        Map<Long, Integer> exploreLevels = new HashMap<>();
        for (UserLocationExploration e : explorations) {
            exploreLevels.put(e.getLocationId(), e.getExploreLevel());
        }
        return exploreLevels;
    }

    private CampusLocation findLocation(Long locationId) {
        List<CampusLocation> locations = eventService.listEnabledLocations();
        return locations.stream()
                .filter(location -> location.getId().equals(locationId))
                .findFirst()
                .orElse(null);
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }

    private List<String> buildStatusLines(PlayerProfile profile, PlayerAttribute attribute) {
        String pressureLine = attribute.getPressure() >= 75 ? "压力警报" : "压力可控";
        String healthLine = attribute.getHealth() < 45 ? "健康偏低" : "还能行动";
        String actionLine = profile.getActionPoints() + "/" + profile.getMaxActionPoints() + " 行动点";
        String weekLine = playerService.getWeekPhaseLabel(profile);
        return List.of(
                weekLine,
                actionLine,
                pressureLine + " " + attribute.getPressure(),
                healthLine + " " + attribute.getHealth()
        );
    }
}
