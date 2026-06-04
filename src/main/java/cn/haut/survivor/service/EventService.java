package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.EventRecord;

import java.util.List;

public interface EventService {

    List<CampusLocation> listEnabledLocations();

    List<Event> listEnabledEventsForLocation(Long locationId, Integer currentWeek);

    Event triggerRandomEvent(Long userId, Long locationId);

    List<EventOption> listOptions(Long eventId);

    EventRecord chooseOption(Long userId, Long eventId, Long optionId);
}
