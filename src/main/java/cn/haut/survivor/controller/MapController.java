package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventRecord;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.service.EventService;
import cn.haut.survivor.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class MapController {

    private final EventService eventService;
    private final PlayerService playerService;

    public MapController(EventService eventService, PlayerService playerService) {
        this.eventService = eventService;
        this.playerService = playerService;
    }

    @GetMapping("/map")
    public String map(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        model.addAttribute("locations", eventService.listEnabledLocations());
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("statusLines", buildStatusLines(profile, attribute));
        return "map/index";
    }

    @GetMapping("/map/location/{locationId}/event")
    public String triggerEvent(@PathVariable Long locationId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        Event event = eventService.triggerRandomEvent(userId, locationId);
        if (event == null) {
            model.addAttribute("locations", eventService.listEnabledLocations());
            model.addAttribute("message", "这里暂时没有可触发的事件。");
            return "map/index";
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
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        model.addAttribute("profile", playerService.findProfileByUserId(userId));
        return "map/event";
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
        String ddlLine = profile.getCurrentWeek() >= 12 ? "DDL 高发期" : "日常探索";
        return List.of(
                "第 " + profile.getCurrentWeek() + " 周",
                pressureLine + " " + attribute.getPressure(),
                healthLine + " " + attribute.getHealth(),
                ddlLine
        );
    }
}
