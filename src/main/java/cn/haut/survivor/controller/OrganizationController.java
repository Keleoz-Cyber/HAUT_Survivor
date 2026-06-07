package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.Organization;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserOrganization;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.ExplorationService;
import cn.haut.survivor.service.OrganizationService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.WeeklyGoalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class OrganizationController {

    private final OrganizationService organizationService;
    private final PlayerService playerService;
    private final ExplorationService explorationService;
    private final WeeklyGoalService weeklyGoalService;
    private final AchievementService achievementService;

    public OrganizationController(OrganizationService organizationService, PlayerService playerService,
                                   ExplorationService explorationService, WeeklyGoalService weeklyGoalService,
                                   AchievementService achievementService) {
        this.organizationService = organizationService;
        this.playerService = playerService;
        this.explorationService = explorationService;
        this.weeklyGoalService = weeklyGoalService;
        this.achievementService = achievementService;
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
        Map<Long, UserOrganization> relationMap = relations.stream()
                .collect(Collectors.toMap(UserOrganization::getOrganizationId, Function.identity()));

        model.addAttribute("orgs", orgs);
        model.addAttribute("relationMap", relationMap);
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

        // 探索度门槛信息
        boolean exploreUnlocked = true;
        if (org.getUnlockLocationId() != null && org.getUnlockExploreLevel() != null && org.getUnlockExploreLevel() > 0) {
            exploreUnlocked = explorationService.isUnlocked(userId, org.getUnlockLocationId(), org.getUnlockExploreLevel());
        }
        model.addAttribute("exploreUnlocked", exploreUnlocked);
        return "organization/detail";
    }

    @PostMapping("/organizations/{id}/discover")
    public String discover(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        // 检查探索度门槛
        Organization org = organizationService.listAll().stream()
                .filter(o -> o.getId().equals(id)).findFirst().orElse(null);
        if (org != null && org.getUnlockLocationId() != null && org.getUnlockExploreLevel() != null && org.getUnlockExploreLevel() > 0) {
            if (!explorationService.isUnlocked(userId, org.getUnlockLocationId(), org.getUnlockExploreLevel())) {
                model.addAttribute("error", "探索度不足，无法发现此组织。继续探索对应地点吧！");
                return detail(id, session, model);
            }
        }
        organizationService.discover(userId, id);
        return "redirect:/organizations/" + id;
    }

    @PostMapping("/organizations/{id}/join")
    public String join(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        try {
            organizationService.join(userId, id);
            // 成就：加入组织
            achievementService.unlockAchievement(userId, "club_rookie");
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
            OrganizationService.OrganizationActivityResult result = organizationService.attendActivityWithChange(userId, id);
            model.addAttribute("activityChange", result.attributeChange());
            model.addAttribute("activityResultText", result.activityResultText());

            // 更新周目标进度：组织活动 +1
            PlayerProfile profile = playerService.findProfileByUserId(userId);
            weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "org_activity", 1);

            return detail(id, session, model);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return detail(id, session, model);
        }
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
