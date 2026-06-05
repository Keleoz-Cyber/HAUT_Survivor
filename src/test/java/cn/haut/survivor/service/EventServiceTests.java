package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.EventRecord;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.mapper.EventRecordMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class EventServiceTests {

    @Autowired
    private EventService eventService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerAttributeMapper playerAttributeMapper;

    @Autowired
    private PlayerProfileMapper playerProfileMapper;

    @Autowired
    private EventRecordMapper eventRecordMapper;

    @BeforeEach
    void setUpPlayer() {
        playerService.createProfile(2L, "事件测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void locationsLoadFromSeedData() {
        List<CampusLocation> locations = eventService.listEnabledLocations();

        assertThat(locations).hasSize(8);
        assertThat(locations).extracting(CampusLocation::getLocationName).contains("教学楼", "实验室", "快递站");
        assertThat(locations).extracting(CampusLocation::getIconKey).contains("building", "code", "package");
        assertThat(locations).extracting(CampusLocation::getBackgroundImage).allMatch(value -> value.startsWith("scene-"));
        assertThat(locations).extracting(CampusLocation::getThemeColor).contains("#2563eb", "#7c3aed", "#0891b2");
    }

    @Test
    void eventOptionsExposePreviewAndRiskMetadata() {
        List<EventOption> options = eventService.listOptions(7L);

        assertThat(options).extracting(EventOption::getPreviewText)
                .contains("稳妥定位，技能收益高");
        assertThat(options).extracting(EventOption::getRiskLevel)
                .contains("low", "medium", "high");
    }

    @Test
    void enabledEventsAreFilteredByLocationAndWeek() {
        List<Event> events = eventService.listEnabledEventsForLocation(6L, 1);

        assertThat(events).extracting(Event::getEventName).contains("Java 代码报错", "实验数据异常");
        assertThat(events).allMatch(event -> event.getLocationId().equals(6L));
    }

    @Test
    void choosingEventOptionUpdatesAttributesAndExperience() {
        eventService.chooseOption(2L, 7L, 19L);

        PlayerAttribute attribute = playerService.findAttributeByUserId(2L);
        PlayerProfile profile = playerService.findProfileByUserId(2L);

        assertThat(attribute.getSkill()).isEqualTo(58);
        assertThat(attribute.getPressure()).isEqualTo(27);
        assertThat(attribute.getDiscipline()).isEqualTo(55);
        assertThat(profile.getExp()).isEqualTo(30);
    }

    @Test
    void attributeValuesStayBetweenZeroAndOneHundredExceptExperience() {
        PlayerAttribute attribute = playerService.findAttributeByUserId(2L);
        attribute.setHealth(99);
        playerAttributeMapper.updateById(attribute);

        eventService.chooseOption(2L, 6L, 16L);

        PlayerAttribute updatedAttribute = playerService.findAttributeByUserId(2L);
        PlayerProfile updatedProfile = playerService.findProfileByUserId(2L);

        assertThat(updatedAttribute.getHealth()).isEqualTo(100);
        assertThat(updatedProfile.getExp()).isEqualTo(24);
    }

    @Test
    void eventRecordIsSaved() {
        EventRecord record = eventService.chooseOption(2L, 7L, 19L);

        assertThat(record.getId()).isNotNull();
        assertThat(eventRecordMapper.selectById(record.getId()).getResultText()).isEqualTo(record.getResultText());
    }

    // ==================== 事件池随机化测试 ====================

    @Test
    void eachLocationHasAtLeastThreeEvents() {
        List<CampusLocation> locations = eventService.listEnabledLocations();

        for (CampusLocation location : locations) {
            List<Event> events = eventService.listEnabledEventsForLocation(location.getId(), 1);
            assertThat(events.size())
                    .as("地点 [%s] 应至少有 3 个事件，实际有 %d 个", location.getLocationName(), events.size())
                    .isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    void triggerRandomEventReturnsDifferentEventsAcrossMultipleCalls() {
        // 对实验室（地点 6）触发多次，验证不会总是返回同一个事件
        Map<String, Integer> eventCounts = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            Event event = eventService.triggerRandomEvent(2L, 6L);
            if (event != null) {
                eventCounts.merge(event.getEventName(), 1, Integer::sum);
            }
        }

        // 实验室有 3 个事件，30 次触发应该至少命中 2 个不同事件
        assertThat(eventCounts.size())
                .as("实验室多次触发应返回至少 2 种不同事件")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void allEventsHaveAtLeastTwoOptions() {
        for (long eventId = 1; eventId <= 42; eventId++) {
            List<EventOption> options = eventService.listOptions(eventId);
            if (!options.isEmpty()) {
                assertThat(options.size())
                        .as("事件 id=%d 应至少有 2 个选项", eventId)
                        .isGreaterThanOrEqualTo(2);
            }
        }
    }

    // ==================== 探索度过滤测试 ====================

    @Test
    void lowExploreLevelExcludesHiddenEvents() {
        // 探索度 0 时，图书馆（地点 2）不应有隐藏事件（id>=27, min_explore_level>=40）
        List<Event> events = eventService.listEnabledEventsForLocation(2L, 1, 0);

        assertThat(events).allMatch(event -> event.getMinExploreLevel() == null || event.getMinExploreLevel() == 0);
        assertThat(events).extracting(Event::getEventName)
                .doesNotContain("隐藏自习角", "考研资料库");
    }

    @Test
    void sufficientExploreLevelIncludesHiddenEvents() {
        // 探索度 50 时，图书馆（地点 2）应包含"隐藏自习角"（min_explore_level=40）但不包含"考研资料库"（min_explore_level=80）
        List<Event> events = eventService.listEnabledEventsForLocation(2L, 1, 50);

        assertThat(events).extracting(Event::getEventName).contains("隐藏自习角");
        assertThat(events).extracting(Event::getEventName).doesNotContain("考研资料库");
    }

    @Test
    void maxExploreLevelIncludesAllHiddenEvents() {
        // 探索度 100 时，图书馆（地点 2）应包含所有隐藏事件
        List<Event> events = eventService.listEnabledEventsForLocation(2L, 1, 100);

        assertThat(events).extracting(Event::getEventName).contains("隐藏自习角", "考研资料库");
    }

    @Test
    void hiddenEventsAlsoHaveAtLeastTwoOptions() {
        for (long eventId = 27; eventId <= 42; eventId++) {
            List<EventOption> options = eventService.listOptions(eventId);
            assertThat(options.size())
                    .as("隐藏事件 id=%d 应至少有 2 个选项", eventId)
                    .isGreaterThanOrEqualTo(2);
        }
    }

    // ==================== 属性变化记录测试 ====================

    @Test
    void chooseOptionReturnsAttributeChange() {
        EventRecord record = eventService.chooseOption(2L, 7L, 19L);

        assertThat(record.getAttributeChange()).isNotNull();
        assertThat(record.getAttributeChange().skillChange()).isNotZero();
        assertThat(record.getAttributeChange().hasAnyChange()).isTrue();
    }
}
