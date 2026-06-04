package cn.haut.survivor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageOpens() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void invalidCredentialsReturnLoginError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "student")
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void validCredentialsStoreSessionAndRedirect() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "student")
                        .param("password", "student123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(request().sessionAttribute("LOGIN_USER_ID", equalTo(2L)))
                .andExpect(request().sessionAttribute("LOGIN_USER_ROLE", equalTo("USER")));
    }
}
