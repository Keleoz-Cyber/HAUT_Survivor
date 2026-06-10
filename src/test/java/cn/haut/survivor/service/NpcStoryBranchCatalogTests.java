package cn.haut.survivor.service;

import cn.haut.survivor.service.impl.NpcStoryBranchCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NpcStoryBranchCatalogTests {

    @Test
    void branchIdsStayUniqueInsideReservedVirtualRange() {
        Set<Long> ids = new HashSet<>();

        NpcStoryBranchCatalog.list().forEach(branch -> {
            assertThat(branch.id())
                    .as("branch %s id must use the 900xxx virtual range", branch.key())
                    .isBetween(900000L, 900999L);
            assertThat(ids.add(branch.id()))
                    .as("branch id %s must be unique", branch.id())
                    .isTrue();
        });

        assertThat(ids).hasSize(NpcStoryBranchCatalog.list().size());
    }

    @Test
    void branchIdsAreGroupedByNpcIdToAvoidManualCollisions() {
        NpcStoryBranchCatalog.list().forEach(branch -> {
            long expectedPrefix = 900000L + branch.npcId() * 100L;

            assertThat(branch.id())
                    .as("branch %s should stay in npc %s reserved block", branch.key(), branch.npcId())
                    .isBetween(expectedPrefix, expectedPrefix + 99L);
        });
    }
}
