package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.Event;
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

import java.util.List;

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
}
