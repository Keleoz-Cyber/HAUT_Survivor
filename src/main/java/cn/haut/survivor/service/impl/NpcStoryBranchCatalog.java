package cn.haut.survivor.service.impl;

import java.util.List;

public final class NpcStoryBranchCatalog {

    public static final long RESERVED_ID_MIN = 900000L;
    public static final long RESERVED_ID_MAX = 900999L;

    public static final long AJIE_LIGHTS_OUT_PACT_ID = 900101L;
    public static final long LINRAN_KEY_WEEK_REVIEW_ID = 900201L;
    public static final long ZHOUYU_SMALL_CIRCLE_ID = 900301L;
    public static final long LAOZHENG_REVIEW_SLOT_ID = 900401L;
    public static final long XIAOMA_RECOVERY_RUN_ID = 900501L;

    private static final List<BranchSlot> BRANCHES = List.of(
            new BranchSlot(1L, AJIE_LIGHTS_OUT_PACT_ID, "ajie_lights_out_pact"),
            new BranchSlot(2L, LINRAN_KEY_WEEK_REVIEW_ID, "linran_key_week_review"),
            new BranchSlot(3L, ZHOUYU_SMALL_CIRCLE_ID, "zhouyu_small_circle"),
            new BranchSlot(4L, LAOZHENG_REVIEW_SLOT_ID, "laozheng_review_slot"),
            new BranchSlot(5L, XIAOMA_RECOVERY_RUN_ID, "xiaoma_recovery_run")
    );

    private NpcStoryBranchCatalog() {
    }

    public static List<BranchSlot> list() {
        return BRANCHES;
    }

    public record BranchSlot(Long npcId, Long id, String key) {
    }
}
