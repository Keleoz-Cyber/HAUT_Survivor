package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NpcController {

    private final NpcService npcService;
    private final PlayerService playerService;

    public NpcController(NpcService npcService, PlayerService playerService) {
        this.npcService = npcService;
        this.playerService = playerService;
    }

    @GetMapping("/npcs/{npcId}")
    public String detail(@PathVariable Long npcId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        Npc npc = findActiveNpc(npcId);
        UserNpcRelation relation = findRelation(userId, npcId);
        if (relation == null) {
            npcService.increaseFamiliarity(userId, npcId, 0);
            relation = findRelation(userId, npcId);
        }

        model.addAttribute("profile", profile);
        model.addAttribute("attribute", attribute);
        model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
        model.addAttribute("npc", npc);
        model.addAttribute("relation", relation);
        model.addAttribute("relationStage", npcService.getRelationStage(relation != null ? relation.getFamiliarity() : 0));
        model.addAttribute("interactions", npcService.listAvailableInteractions(userId, npcId, profile.getCurrentWeek()));
        model.addAttribute("currentBuddy", npcService.getCurrentBuddy(userId, profile.getCurrentWeek()).orElse(null));
        return "npc/detail";
    }

    @PostMapping("/npcs/{npcId}/buddy")
    public String chooseBuddy(@PathVariable Long npcId, HttpSession session) {
        Long userId = currentUserId(session);
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (profile == null) {
            return "redirect:/player/create";
        }
        try {
            npcService.chooseWeeklyBuddy(userId, npcId, profile.getCurrentWeek());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // Keep the page forgiving; the detail page still shows the current state.
        }
        return "redirect:/npcs/" + npcId;
    }

    @PostMapping("/npcs/{npcId}/interactions/{interactionId}")
    public String interact(@PathVariable Long npcId, @PathVariable Long interactionId,
                           HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        try {
            NpcService.NpcInteractionResult result =
                    npcService.interact(userId, npcId, interactionId, profile.getCurrentWeek());
            model.addAttribute("result", result);
            model.addAttribute("profile", playerService.findProfileByUserId(userId));
            model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
            model.addAttribute("semesterOver", playerService.isSemesterOver(userId));
            return "npc/result";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/npcs/" + npcId;
        }
    }

    private Npc findActiveNpc(Long npcId) {
        return npcService.listActiveNpcs().stream()
                .filter(npc -> npc.getId().equals(npcId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("NPC不存在"));
    }

    private UserNpcRelation findRelation(Long userId, Long npcId) {
        return npcService.listKnownNpcs(userId).stream()
                .filter(relation -> relation.getNpcId().equals(npcId))
                .findFirst()
                .orElse(null);
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
