package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.service.InfluenceLogService;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.SemesterArchiveService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SemesterArchiveServiceImpl implements SemesterArchiveService {

    private final InfluenceLogService influenceLogService;
    private final NpcService npcService;

    public SemesterArchiveServiceImpl(InfluenceLogService influenceLogService, NpcService npcService) {
        this.influenceLogService = influenceLogService;
        this.npcService = npcService;
    }

    @Override
    public SemesterArchiveSummary buildSummary(Long userId) {
        List<InfluenceLogService.WeekInfluenceRecap> recaps =
                influenceLogService.listSemesterInfluenceRecaps(userId);
        List<InfluenceLogService.InfluenceLogEntry> entries = recaps.stream()
                .flatMap(recap -> recap.entries().stream())
                .toList();

        Counts counts = countInfluences(entries);
        Integer keyWeek = pickKeyWeek(recaps);
        InfluenceLogService.InfluenceLogEntry mainSource = pickMainSource(entries);
        Optional<UserNpcRelation> keyRelation = pickKeyNpc(userId);

        String keyNpcName = keyRelation
                .map(UserNpcRelation::getNpc)
                .map(npc -> npc != null ? npc.getNpcName() : null)
                .orElse(null);
        String keyNpcStage = keyRelation
                .map(UserNpcRelation::getFamiliarity)
                .map(npcService::getRelationSummary)
                .map(NpcService.RelationSummary::label)
                .orElse(null);

        List<ArchiveHighlight> highlights = buildHighlights(counts, keyWeek, mainSource, keyNpcName, keyNpcStage);
        return new SemesterArchiveSummary(
                growthKeyword(counts, keyNpcName),
                growthDescription(counts, keyNpcName, keyWeek),
                mainSource != null ? mainSource.sourceLabel() : "校园影响",
                keyNpcName,
                keyNpcStage,
                keyWeek,
                counts.positive(),
                counts.negative(),
                counts.neutral(),
                highlights);
    }

    private Counts countInfluences(List<InfluenceLogService.InfluenceLogEntry> entries) {
        int positive = 0;
        int negative = 0;
        int neutral = 0;

        for (InfluenceLogService.InfluenceLogEntry entry : entries) {
            switch (classify(entry)) {
                case POSITIVE -> positive++;
                case NEGATIVE -> negative++;
                case NEUTRAL -> neutral++;
            }
        }
        return new Counts(positive, negative, neutral);
    }

    private InfluenceTone classify(InfluenceLogService.InfluenceLogEntry entry) {
        if (entry == null) {
            return InfluenceTone.NEUTRAL;
        }
        if ("npc_story".equals(entry.sourceType())) {
            return InfluenceTone.NEUTRAL;
        }
        boolean hasNegativeSignal = entry.pressureChange() > 0
                || entry.healthChange() < 0
                || entry.moneyChange() < 0;
        if (hasNegativeSignal) {
            return InfluenceTone.NEGATIVE;
        }
        boolean hasPositiveSignal = entry.academicChange() > 0
                || entry.healthChange() > 0
                || entry.moneyChange() > 0
                || entry.socialChange() > 0
                || entry.skillChange() > 0
                || entry.pressureChange() < 0
                || entry.disciplineChange() > 0
                || entry.expChange() > 0
                || entry.exploreBonus() > 0;
        return hasPositiveSignal ? InfluenceTone.POSITIVE : InfluenceTone.NEUTRAL;
    }

    private Integer pickKeyWeek(List<InfluenceLogService.WeekInfluenceRecap> recaps) {
        return recaps.stream()
                .filter(recap -> recap.entries() != null && !recap.entries().isEmpty())
                .max(Comparator
                        .comparingInt((InfluenceLogService.WeekInfluenceRecap recap) -> recap.entries().size())
                        .thenComparingInt(InfluenceLogService.WeekInfluenceRecap::weekNumber))
                .map(InfluenceLogService.WeekInfluenceRecap::weekNumber)
                .orElse(null);
    }

    private InfluenceLogService.InfluenceLogEntry pickMainSource(
            List<InfluenceLogService.InfluenceLogEntry> entries) {
        Map<String, SourceBucket> buckets = new LinkedHashMap<>();
        for (InfluenceLogService.InfluenceLogEntry entry : entries) {
            if (entry == null) {
                continue;
            }
            String key = entry.sourceType() != null ? entry.sourceType() : "";
            buckets.compute(key, (ignored, bucket) -> {
                if (bucket == null) {
                    return new SourceBucket(entry, 1);
                }
                return new SourceBucket(bucket.firstEntry(), bucket.count() + 1);
            });
        }

        return buckets.values().stream()
                .max(Comparator.comparingInt(SourceBucket::count))
                .map(SourceBucket::firstEntry)
                .orElse(null);
    }

    private Optional<UserNpcRelation> pickKeyNpc(Long userId) {
        return npcService.listKnownNpcs(userId).stream()
                .filter(relation -> relation.getNpc() != null)
                .max(Comparator
                        .comparingInt((UserNpcRelation relation) -> value(relation.getFamiliarity()))
                        .thenComparingInt(relation -> value(relation.getLastMetWeek()))
                        .thenComparingLong(relation -> -value(relation.getNpcId())));
    }

    private List<ArchiveHighlight> buildHighlights(Counts counts,
                                                   Integer keyWeek,
                                                   InfluenceLogService.InfluenceLogEntry mainSource,
                                                   String keyNpcName,
                                                   String keyNpcStage) {
        List<ArchiveHighlight> highlights = new ArrayList<>();
        highlights.add(new ArchiveHighlight(
                "成长",
                growthKeyword(counts, keyNpcName),
                growthDescription(counts, keyNpcName, keyWeek)));

        highlights.add(new ArchiveHighlight(
                "主线",
                mainSource != null ? mainSource.sourceLabel() : "还在开局",
                mainSource != null
                        ? "这个学期里，" + safeText(mainSource.sourceLabel()) + "留下的痕迹最明显。"
                        : "还没有足够多的校园影响，先把这一页当成空白档案。"));

        highlights.add(new ArchiveHighlight(
                "关系",
                keyNpcName != null ? keyNpcName : "暂无关键关系",
                keyNpcName != null
                        ? "目前最稳定的关系是「" + keyNpcName + "」，阶段为「" + keyNpcStage + "」。"
                        : "多和熟人互动后，这里会记录本学期最重要的关系。"));
        return List.copyOf(highlights);
    }

    private String growthKeyword(Counts counts, String keyNpcName) {
        if (counts.negative() > counts.positive() && counts.negative() >= 2) {
            return "压力管理中";
        }
        if (keyNpcName != null && counts.neutral() >= counts.positive()) {
            return "稳健搭子线";
        }
        if (counts.positive() > counts.negative()) {
            return "成长加速";
        }
        return "探索新生";
    }

    private String growthDescription(Counts counts, String keyNpcName, Integer keyWeek) {
        if (counts.total() == 0) {
            return "这个学期的档案刚刚展开，去探索、互动或推进周次后，会留下更具体的成长记录。";
        }
        String weekText = keyWeek != null ? "第 " + keyWeek + " 周" : "某一周";
        if (keyNpcName != null) {
            return weekText + "的影响最密集，和「" + keyNpcName + "」的关系也开始成为学期记忆的一部分。";
        }
        return weekText + "的影响最密集，校园传闻、周主题和探索结果共同塑造了这段学期轨迹。";
    }

    private String safeText(String value) {
        return value != null && !value.isBlank() ? value : "校园影响";
    }

    private int value(Integer value) {
        return value != null ? value : 0;
    }

    private long value(Long value) {
        return value != null ? value : 0L;
    }

    private enum InfluenceTone {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    private record Counts(int positive, int negative, int neutral) {
        int total() {
            return positive + negative + neutral;
        }
    }

    private record SourceBucket(InfluenceLogService.InfluenceLogEntry firstEntry, int count) {}
}
