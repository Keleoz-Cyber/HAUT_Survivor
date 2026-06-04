package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Task;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void setUpPlayer() {
        playerService.createProfile(2L, "任务页面玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void taskPageShowsCurrentUserTasks() throws Exception {
        taskService.createTask(2L, "预习数据库", "课程", "B", LocalDateTime.now().plusDays(2), "看完 ER 图");

        mockMvc.perform(get("/tasks")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("task/index"))
                .andExpect(model().attributeExists("tasks"));
    }

    @Test
    void userCanCreateTaskFromPage() throws Exception {
        mockMvc.perform(post("/tasks")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER")
                        .param("taskName", "完成 Java 课设")
                        .param("taskType", "课程")
                        .param("difficulty", "A")
                        .param("deadline", "2026-06-10T18:00")
                        .param("description", "完成登录模块"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }

    @Test
    void userCanCompleteTaskFromPage() throws Exception {
        Task task = taskService.createTask(2L, "整理实验报告", "实验", "C", LocalDateTime.now().plusDays(1), "补截图");

        mockMvc.perform(post("/tasks/" + task.getId() + "/complete")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }
}
