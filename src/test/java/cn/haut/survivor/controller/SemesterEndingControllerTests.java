package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.SemesterEndingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class SemesterEndingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SemesterEndingService semesterEndingService;

    @Test
    void endingPageIncludesGrowthPortraitWithoutChangingEndingModel() throws Exception {
        playerService.createProfile(2L, "ending portrait test", "大二", "计算机类", "就业路线");
        advanceToSemesterEnd();
        semesterEndingService.settleSemester(2L);

        mockMvc.perform(get("/ending")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("ending/index"))
                .andExpect(model().attributeExists(
                        "profile",
                        "attribute",
                        "semesterOver",
                        "hasSettled",
                        "userEnding",
                        "history",
                        "ending",
                        "growthPortrait"));
    }

    private void advanceToSemesterEnd() {
        for (int i = 0; i < 16; i++) {
            playerService.advanceWeek(2L);
        }
    }
}
