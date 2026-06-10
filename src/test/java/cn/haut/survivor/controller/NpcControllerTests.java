package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class NpcControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private NpcService npcService;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "npc controller test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 2L, 55);
    }

    @Test
    void npcDetailPageRenders() throws Exception {
        mockMvc.perform(get("/npcs/2")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("npc/detail"))
                .andExpect(model().attributeExists("npc", "relation", "relationStage", "relationSummary", "interactions"))
                .andExpect(model().attribute("currentBuddy", org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void npcDetailPageShowsUnlockedStoryBranchInteraction() throws Exception {
        npcService.interact(2L, 2L, 3004L, 1);

        mockMvc.perform(get("/npcs/2")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("interactions", hasItem(
                        hasProperty("interactionKey", equalTo("linran_key_week_review")))));
    }

    @Test
    void chooseBuddyRedirectsToNpcDetail() throws Exception {
        mockMvc.perform(post("/npcs/2/buddy")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/npcs/2"));
    }

    @Test
    void interactingShowsResultPage() throws Exception {
        mockMvc.perform(post("/npcs/2/interactions/3004")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("npc/result"))
                .andExpect(model().attributeExists("result", "relationSummary", "profile", "attribute"));
    }

    @Test
    void cp6NpcDetailPageRenders() throws Exception {
        npcService.increaseFamiliarity(2L, 6101L, 25);

        mockMvc.perform(get("/npcs/6101")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("npc/detail"))
                .andExpect(model().attributeExists("npc", "relation", "relationSummary", "interactions"))
                .andExpect(model().attribute("interactions", hasItem(
                        hasProperty("interactionKey", equalTo("cp6_fushao_canteen_tip")))));
    }

    @Test
    void cp6NpcInteractionShowsResultPage() throws Exception {
        npcService.increaseFamiliarity(2L, 6101L, 25);

        mockMvc.perform(post("/npcs/6101/interactions/610003")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("npc/result"))
                .andExpect(model().attributeExists("result", "relationSummary", "profile", "attribute"));
    }
}
