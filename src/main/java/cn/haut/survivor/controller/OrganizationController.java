package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Organization;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserOrganization;
import cn.haut.survivor.service.OrganizationService;
import cn.haut.survivor.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class OrganizationController {

    private final OrganizationService organizationService;
    private final PlayerService playerService;

    public OrganizationController(OrganizationService organizationService, PlayerService playerService) {
        this.organizationService = organizationService;
        this.playerService = playerService;
    }

    @GetMapping("/organizations")
    public String list(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        List<Organization> orgs = organizationService.listAll();
        List<UserOrganization> relations = organizationService.listUserOrganizations(userId);

        model.addAttribute("orgs", orgs);
        model.addAttribute("relations", relations);
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
        return "organization/index";
    }

    @GetMapping("/organizations/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        Organization org = organizationService.listAll().stream()
                .filter(o -> o.getId().equals(id)).findFirst().orElse(null);
        if (org == null) return "redirect:/organizations";

        UserOrganization relation = organizationService.findRelation(userId, id);

        model.addAttribute("org", org);
        model.addAttribute("relation", relation);
        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
        return "organization/detail";
    }

    @PostMapping("/organizations/{id}/discover")
    public String discover(@PathVariable Long id, HttpSession session) {
        Long userId = currentUserId(session);
        organizationService.discover(userId, id);
        return "redirect:/organizations/" + id;
    }

    @PostMapping("/organizations/{id}/join")
    public String join(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        try {
            organizationService.join(userId, id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return detail(id, session, model);
        }
        return "redirect:/organizations/" + id;
    }

    @PostMapping("/organizations/{id}/activity")
    public String attendActivity(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        try {
            organizationService.attendActivity(userId, id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return detail(id, session, model);
        }
        return "redirect:/organizations/" + id;
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
