package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
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
class DungeonControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerService playerService;

    @BeforeEach
    void setUpPlayer() {
        playerService.createProfile(2L, "副本页面玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void dungeonIndexShowsDemoDungeon() throws Exception {
        mockMvc.perform(get("/dungeons")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dungeon/index"))
                .andExpect(model().attributeExists("dungeon", "tasks"));
    }

    @Test
    void startDemoDungeonRedirectsToPlayPage() throws Exception {
        mockMvc.perform(get("/dungeons/demo/start")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dungeons/demo/play"));
    }

    @Test
    void playPageShowsCurrentDungeonTask() throws Exception {
        mockMvc.perform(get("/dungeons/demo/play")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dungeon/play"))
                .andExpect(model().attributeExists("record", "task", "options", "attribute"));
    }

    @Test
    void choosingDungeonOptionShowsResult() throws Exception {
        mockMvc.perform(post("/dungeons/demo/task/1/option/1")
                        .param("minigameResult", "selected=core-loop")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dungeon/result"))
                .andExpect(model().attributeExists("taskRecord", "record", "attribute"));
    }

    @Test
    void minigamePageShowsRelationChoicesAfterFirstStage() throws Exception {
        mockMvc.perform(post("/dungeons/demo/task/1/option/1")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"));

        mockMvc.perform(get("/dungeons/demo/play")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dungeon/play"))
                .andExpect(model().attributeExists("relationChoices"))
                .andExpect(model().attribute("isMinigameTask", true));
    }

    @Test
    void submittingDatabaseMinigameShowsResult() throws Exception {
        mockMvc.perform(post("/dungeons/demo/task/1/option/1")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"));

        mockMvc.perform(post("/dungeons/demo/task/2/minigame")
                        .param("selectedRelations", "user->player_attribute")
                        .param("selectedRelations", "event->event_option")
                        .param("selectedRelations", "dungeon->dungeon_task")
                        .param("elapsedSeconds", "18")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dungeon/result"))
                .andExpect(model().attributeExists("taskRecord", "record", "attribute"));
    }

    @Test
    void finishingDemoDungeonShowsCompletedEndingRecord() throws Exception {
        mockMvc.perform(post("/dungeons/demo/task/1/option/1")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"));
        mockMvc.perform(post("/dungeons/demo/task/2/minigame")
                        .param("selectedRelations", "user->player_attribute")
                        .param("selectedRelations", "event->event_option")
                        .param("selectedRelations", "dungeon->dungeon_task")
                        .param("elapsedSeconds", "18")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"));

        mockMvc.perform(post("/dungeons/demo/task/3/option/7")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("dungeon/result"))
                .andExpect(model().attribute("record", hasProperty("status", equalTo("COMPLETED"))))
                .andExpect(model().attribute("record",
                        hasProperty("finalEvaluation", equalTo("\u8bfe\u8bbe\u6218\u795e"))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(containsString("\u8fc7\u7a0b\u6807\u7b7e")));
    }
}
