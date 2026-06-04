package cn.haut.survivor.controller.admin;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/admin/events")
    public String events(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("events", eventService.listAllEvents());
        return "admin/event-list";
    }

    @GetMapping("/admin/events/new")
    public String newEvent(HttpSession session, Model model) {
        requireAdmin(session);
        fillFormModel(model, new Event(), "/admin/events");
        return "admin/event-form";
    }

    @PostMapping("/admin/events")
    public String createEvent(
            @RequestParam String eventName,
            @RequestParam String eventType,
            @RequestParam Long locationId,
            @RequestParam String description,
            @RequestParam Integer probability,
            @RequestParam Integer minWeek,
            @RequestParam Integer maxWeek,
            HttpSession session
    ) {
        requireAdmin(session);
        eventService.createEvent(eventName, eventType, locationId, description, probability, minWeek, maxWeek);
        return "redirect:/admin/events";
    }

    @GetMapping("/admin/events/{eventId}/edit")
    public String editEvent(@PathVariable Long eventId, HttpSession session, Model model) {
        requireAdmin(session);
        Event event = eventService.findEventById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        fillFormModel(model, event, "/admin/events/" + eventId);
        return "admin/event-form";
    }

    @PostMapping("/admin/events/{eventId}")
    public String updateEvent(
            @PathVariable Long eventId,
            @RequestParam String eventName,
            @RequestParam String eventType,
            @RequestParam Long locationId,
            @RequestParam String description,
            @RequestParam Integer probability,
            @RequestParam Integer minWeek,
            @RequestParam Integer maxWeek,
            HttpSession session
    ) {
        requireAdmin(session);
        eventService.updateEvent(eventId, eventName, eventType, locationId, description, probability, minWeek, maxWeek);
        return "redirect:/admin/events";
    }

    @PostMapping("/admin/events/{eventId}/disable")
    public String disableEvent(@PathVariable Long eventId, HttpSession session) {
        requireAdmin(session);
        eventService.disableEvent(eventId);
        return "redirect:/admin/events";
    }

    private void fillFormModel(Model model, Event event, String action) {
        model.addAttribute("event", event);
        model.addAttribute("action", action);
        model.addAttribute("locations", eventService.listEnabledLocations());
    }

    private void requireAdmin(HttpSession session) {
        Object role = session.getAttribute(LoginInterceptor.LOGIN_USER_ROLE);
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
