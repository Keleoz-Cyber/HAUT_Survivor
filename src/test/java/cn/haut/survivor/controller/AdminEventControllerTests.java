package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.service.EventService;
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
class AdminEventControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventService eventService;

    @Test
    void adminCanOpenEventList() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 1L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/event-list"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    void studentCannotOpenEventList() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAddEvent() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 1L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "ADMIN")
                        .param("eventName", "管理员新增事件")
                        .param("eventType", "学习")
                        .param("locationId", "1")
                        .param("description", "测试新增")
                        .param("probability", "60")
                        .param("minWeek", "1")
                        .param("maxWeek", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));
    }

    @Test
    void adminCanEditEvent() throws Exception {
        Event event = eventService.createEvent("待编辑事件", "学习", 1L, "测试描述", 50, 1, 20);

        mockMvc.perform(post("/admin/events/" + event.getId())
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 1L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "ADMIN")
                        .param("eventName", "已编辑事件")
                        .param("eventType", "生活")
                        .param("locationId", "3")
                        .param("description", "编辑后的描述")
                        .param("probability", "70")
                        .param("minWeek", "2")
                        .param("maxWeek", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));
    }

    @Test
    void adminCanDisableEvent() throws Exception {
        Event event = eventService.createEvent("待禁用页面事件", "生活", 3L, "测试描述", 30, 1, 20);

        mockMvc.perform(post("/admin/events/" + event.getId() + "/disable")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 1L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));
    }
}
