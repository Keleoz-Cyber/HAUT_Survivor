package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class OrganizationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void organizationDetailRedirectsToPlayerCreationWhenProfileMissing() throws Exception {
        mockMvc.perform(get("/organizations/1")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 999L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));
    }

    @Test
    void organizationActionsRedirectToPlayerCreationWhenProfileMissing() throws Exception {
        mockMvc.perform(post("/organizations/1/discover")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 999L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));

        mockMvc.perform(post("/organizations/1/join")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 999L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));

        mockMvc.perform(post("/organizations/1/activity")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 999L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));
    }
}
