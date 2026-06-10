package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.CampusLocation;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.EventRecord;

import java.util.List;

public interface EventService {

    List<CampusLocation> listEnabledLocations();

    List<Event> listEnabledEventsForLocation(Long locationId, Integer currentWeek);

    List<Event> listEnabledEventsForLocation(Long locationId, Integer currentWeek, Integer exploreLevel);

    Event triggerRandomEvent(Long userId, Long locationId);

    /** Trigger a random event with an optional type hint that boosts matching events' weight. */
    Event triggerRandomEventWithHint(Long userId, Long locationId, String preferredEventType);

    /** Returns the event type boosted by the current weekly theme, or null when the week has no bias. */
    String getWeeklyThemePreferredEventType(Integer currentWeek);

    List<EventOption> listOptions(Long eventId);

    EventRecord chooseOption(Long userId, Long eventId, Long optionId);

    List<Event> listAllEvents();

    Event findEventById(Long eventId);

    Event createEvent(String eventName, String eventType, Long locationId, String description,
                      Integer probability, Integer minWeek, Integer maxWeek);

    Event updateEvent(Long eventId, String eventName, String eventType, Long locationId, String description,
                      Integer probability, Integer minWeek, Integer maxWeek);

    void disableEvent(Long eventId);
}
