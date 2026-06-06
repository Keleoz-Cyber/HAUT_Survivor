package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.service.PlayerService;
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

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerService playerService;

    @Test
    void userWithoutProfileIsRoutedToPlayerCreation() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));
    }

    @Test
    void playerCreationPageOpens() throws Exception {
        mockMvc.perform(get("/player/create")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("player/create"));
    }

    @Test
    void creatingPlayerRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/player/create")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER")
                        .param("playerName", "莲花街新生")
                        .param("grade", "大一")
                        .param("majorType", "计算机类")
                        .param("growthRoute", "考研路线"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void userWithProfileCanOpenDashboard() throws Exception {
        playerService.createProfile(2L, "仪表盘玩家", "大一", "计算机类", "就业路线");

        mockMvc.perform(get("/dashboard")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("user", "profile", "attribute", "warnings",
                        "weekTheme", "rumors", "knownNpcs"));
    }

    @Test
    void dashboardWithNpcRelationDoesNotError() throws Exception {
        playerService.createProfile(2L, "NPC仪表盘测试", "大一", "计算机类", "就业路线");
        // Force NPC encounters by hitting exploration multiple times
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/exploration/1")
                            .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                            .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"));
        }
        // Dashboard should still render fine even with NPC relations
        mockMvc.perform(get("/dashboard")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"));
    }
}
