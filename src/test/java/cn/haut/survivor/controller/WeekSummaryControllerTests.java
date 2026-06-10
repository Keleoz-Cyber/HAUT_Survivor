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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class WeekSummaryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerService playerService;

    @Test
    void weekSummaryRequiresProfile() throws Exception {
        mockMvc.perform(get("/week/summary")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));
    }

    @Test
    void weekSummaryReturnsSummaryView() throws Exception {
        playerService.createProfile(2L, "周总结控制器测试", "大一", "计算机类", "就业路线");

        mockMvc.perform(get("/week/summary")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("week/summary"))
                .andExpect(model().attributeExists("summary", "profile", "attribute"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本周影响回放")));
    }

    @Test
    void advanceWeekRedirectsToDashboard() throws Exception {
        playerService.createProfile(2L, "推进周测试", "大一", "计算机类", "就业路线");

        mockMvc.perform(post("/week/advance")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void weekHistoryRequiresProfile() throws Exception {
        mockMvc.perform(get("/week/history")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));
    }

    @Test
    void weekHistoryReturnsHistoryView() throws Exception {
        playerService.createProfile(2L, "history-test", "grade", "major", "\u5c31\u4e1a\u8def\u7ebf");

        mockMvc.perform(get("/week/history")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("week/history"))
                .andExpect(model().attributeExists("historyWeeks", "archiveSummary", "profile", "attribute"));
    }

    @Test
    void advanceWeekOnLastWeekRedirectsToEnding() throws Exception {
        playerService.createProfile(2L, "最后一周测试", "大一", "计算机类", "就业路线");

        // Advance to week 4 (last week)
        for (int i = 0; i < 3; i++) {
            playerService.advanceWeek(2L);
        }

        // Now at week 4, advance should go to ending
        mockMvc.perform(post("/week/advance")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ending"));
    }

    @Test
    void semesterOverRedirectsToEnding() throws Exception {
        playerService.createProfile(2L, "学期结束测试", "大一", "计算机类", "就业路线");

        // Advance past week 4
        for (int i = 0; i < 4; i++) {
            playerService.advanceWeek(2L);
        }

        mockMvc.perform(get("/week/summary")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ending"));
    }
}
