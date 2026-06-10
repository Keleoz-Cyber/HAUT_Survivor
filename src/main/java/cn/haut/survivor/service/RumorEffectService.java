package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;

import java.util.List;

public interface RumorEffectService {
    List<ExplorationInfluence> getExplorationInfluences(Long userId, int weekNumber, Long locationId);

    /** Returns the total npc_boost effect_value for the given location, or 0 if none. */
    int getNpcBoostForLocation(Long userId, int weekNumber, Long locationId);

    /** Returns the event_hint effect_target for the given location, or null if none. */
    String getEventHintTarget(Long userId, int weekNumber, Long locationId);

    /** Returns the preferred event type for the visible event_hint, or null if none is supported. */
    String getEventHintPreferredEventType(Long userId, int weekNumber, Long locationId);
}
