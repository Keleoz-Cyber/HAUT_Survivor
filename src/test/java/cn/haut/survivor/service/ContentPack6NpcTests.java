package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.mapper.NpcInteractionMapper;
import cn.haut.survivor.mapper.NpcMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack6NpcTests {

    @Autowired
    private NpcMapper npcMapper;

    @Autowired
    private NpcInteractionMapper npcInteractionMapper;

    @Autowired
    private NpcService npcService;

    @Autowired
    private PlayerService playerService;

    @Test
    void cp6NpcPrototypesAreSeededWithoutDuplicatingAjie() {
        List<Npc> cp6Npcs = npcMapper.selectList(new LambdaQueryWrapper<Npc>()
                .in(Npc::getId, 6101L, 6102L, 6103L)
                .orderByAsc(Npc::getId));

        assertThat(cp6Npcs).hasSize(3);
        assertThat(cp6Npcs).extracting(Npc::getNpcName)
                .containsExactly("富少", "小鱼", "柳如烟");
        assertThat(cp6Npcs).allMatch(npc -> npc.getActive() == 1);
        assertThat(cp6Npcs).allMatch(npc -> npc.getHomeLocationId() != null
                && npc.getHomeLocationId() >= 1L
                && npc.getHomeLocationId() <= 8L);
        assertThat(cp6Npcs).extracting(Npc::getFavoriteAttribute)
                .contains("money", "health", "academic");

        long ajieCount = npcMapper.selectCount(new LambdaQueryWrapper<Npc>()
                .like(Npc::getNpcName, "阿杰")
                .eq(Npc::getActive, 1));
        assertThat(ajieCount).isEqualTo(1);

        Npc ajie = npcMapper.selectById(1L);
        assertThat(ajie).isNotNull();
        assertThat(ajie.getNpcName()).contains("阿杰");
    }

    @Test
    void cp6NpcInteractionsAreNormalSeedRowsOutsideVirtualBranchRange() {
        List<NpcInteraction> interactions = npcInteractionMapper.selectList(new LambdaQueryWrapper<NpcInteraction>()
                .ge(NpcInteraction::getId, 610001L)
                .le(NpcInteraction::getId, 610011L)
                .orderByAsc(NpcInteraction::getId));

        assertThat(interactions).hasSize(11);
        assertThat(interactions).allMatch(interaction -> interaction.getActive() == 1);
        assertThat(interactions).allMatch(interaction -> interaction.getId() < 900000L
                || interaction.getId() > 900999L);
        assertThat(interactions).allMatch(interaction -> interaction.getInteractionKey().startsWith("cp6_"));
        assertThat(interactions).allMatch(interaction -> interaction.getFamiliarityChange() != null
                && interaction.getFamiliarityChange() > 0);
        assertThat(interactions).allMatch(interaction -> interaction.getExpChange() != null
                && interaction.getExpChange() > 0);

        Map<Long, Long> countByNpc = interactions.stream()
                .collect(Collectors.groupingBy(NpcInteraction::getNpcId, Collectors.counting()));
        assertThat(countByNpc).containsEntry(1L, 2L);
        assertThat(countByNpc).containsEntry(6101L, 3L);
        assertThat(countByNpc).containsEntry(6102L, 3L);
        assertThat(countByNpc).containsEntry(6103L, 3L);

        assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
                .contains(
                        "cp6_ajie_lianhuajie_walk",
                        "cp6_fushao_canteen_tip",
                        "cp6_xiaoyu_light_meal",
                        "cp6_liuruyan_library_focus");
    }

    @Test
    void cp6NpcInteractionsUnlockByFamiliarity() {
        playerService.createProfile(2L, "cp6 npc unlock test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 6101L, 25);

        List<NpcInteraction> interactions = npcService.listAvailableInteractions(2L, 6101L, 1);

        assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
                .contains("cp6_fushao_canteen_tip", "cp6_fushao_budget_chat")
                .doesNotContain("cp6_fushao_resource_trade");
    }

    @Test
    void cp6NpcInteractionSettlesAttributesAndConsumesActionPoint() {
        playerService.createProfile(2L, "cp6 npc interaction test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 6103L, 25);
        int beforeAp = playerService.findProfileByUserId(2L).getActionPoints();
        PlayerAttribute before = playerService.findAttributeByUserId(2L);

        NpcService.NpcInteractionResult result = npcService.interact(2L, 6103L, 610009L, 1);

        PlayerAttribute after = playerService.findAttributeByUserId(2L);
        assertThat(playerService.findProfileByUserId(2L).getActionPoints()).isEqualTo(beforeAp - 1);
        assertThat(result.interaction().getInteractionKey()).isEqualTo("cp6_liuruyan_library_focus");
        assertThat(result.attributeChange().academicChange()).isPositive();
        assertThat(result.attributeChange().pressureChange()).isGreaterThanOrEqualTo(0);
        assertThat(after.getAcademic()).isEqualTo(before.getAcademic() + result.attributeChange().academicChange());
        assertThat(result.familiarityGain()).isPositive();
        assertThat(result.storyResult()).isNull();
    }
}
