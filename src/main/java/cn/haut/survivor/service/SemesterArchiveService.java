package cn.haut.survivor.service;

import java.util.List;

public interface SemesterArchiveService {

    SemesterArchiveSummary buildSummary(Long userId);

    record SemesterArchiveSummary(
            String growthKeyword,
            String growthDescription,
            String mainSourceLabel,
            String keyNpcName,
            String keyNpcStage,
            Integer keyWeekNumber,
            int positiveCount,
            int negativeCount,
            int neutralCount,
            List<ArchiveHighlight> highlights
    ) {}

    record ArchiveHighlight(
            String label,
            String title,
            String description
    ) {}
}
