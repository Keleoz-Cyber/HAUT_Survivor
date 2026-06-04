package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class AdminEventServiceTests {

    @Autowired
    private EventService eventService;

    @Test
    void adminCanCreateEvent() {
        Event event = eventService.createEvent("测试事件", "学习", 1L, "测试描述", 50, 1, 20);

        assertThat(event.getId()).isNotNull();
        assertThat(event.getStatus()).isEqualTo(1);
    }

    @Test
    void adminCanUpdateEvent() {
        Event event = eventService.createEvent("旧事件", "学习", 1L, "旧描述", 40, 1, 20);

        Event updated = eventService.updateEvent(event.getId(), "新事件", "生活", 3L, "新描述", 70, 2, 12);

        assertThat(updated.getEventName()).isEqualTo("新事件");
        assertThat(updated.getLocationId()).isEqualTo(3L);
        assertThat(updated.getProbability()).isEqualTo(70);
    }

    @Test
    void adminCanDisableEvent() {
        Event event = eventService.createEvent("待禁用事件", "生活", 3L, "测试描述", 30, 1, 20);

        eventService.disableEvent(event.getId());

        assertThat(eventService.findEventById(event.getId()).getStatus()).isZero();
    }
}
