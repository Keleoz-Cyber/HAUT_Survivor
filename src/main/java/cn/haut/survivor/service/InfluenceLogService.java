package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.AttributeChange;

import java.util.List;

public interface InfluenceLogService {

    void recordExplorationInfluences(Long userId, int weekNumber, Long locationId, List<ExplorationInfluence> influences);

    void recordInfluence(Long userId, int weekNumber, Long locationId, String sourceType,
                         String sourceName, String description, AttributeChange change, int exploreBonus);

    List<InfluenceLogEntry> listWeekInfluences(Long userId, int weekNumber);

    List<WeekInfluenceRecap> listSemesterInfluenceRecaps(Long userId);

    record InfluenceLogEntry(
            Long id,
            String sourceType,
            String sourceLabel,
            String sourceName,
            String description,
            int academicChange,
            int healthChange,
            int moneyChange,
            int socialChange,
            int skillChange,
            int pressureChange,
            int disciplineChange,
            int expChange,
            int exploreBonus,
            String changeText
    ) {}

    record WeekInfluenceRecap(
            int weekNumber,
            List<InfluenceLogEntry> entries
    ) {}
}
