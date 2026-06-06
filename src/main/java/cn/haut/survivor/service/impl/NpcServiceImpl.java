package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.mapper.NpcMapper;
import cn.haut.survivor.mapper.UserNpcRelationMapper;
import cn.haut.survivor.service.NpcService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class NpcServiceImpl implements NpcService {

    private static final double MEET_PROBABILITY = 0.35;

    private final NpcMapper npcMapper;
    private final UserNpcRelationMapper relationMapper;

    public NpcServiceImpl(NpcMapper npcMapper, UserNpcRelationMapper relationMapper) {
        this.npcMapper = npcMapper;
        this.relationMapper = relationMapper;
    }

    @Override
    public List<Npc> listActiveNpcs() {
        return npcMapper.selectList(new LambdaQueryWrapper<Npc>().eq(Npc::getActive, 1));
    }

    @Override
    public List<UserNpcRelation> listKnownNpcs(Long userId) {
        List<UserNpcRelation> relations = relationMapper.selectList(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId)
                .orderByDesc(UserNpcRelation::getFamiliarity));

        // Populate the npc transient field for each relation
        for (UserNpcRelation rel : relations) {
            Npc npc = npcMapper.selectById(rel.getNpcId());
            if (npc != null) {
                rel.setNpc(npc);
            }
        }
        return relations;
    }

    @Override
    @Transactional
    public Optional<NpcEncounter> maybeMeetNpc(Long userId, Long locationId, int currentWeek) {
        // 概率判定
        if (ThreadLocalRandom.current().nextDouble() > MEET_PROBABILITY) {
            return Optional.empty();
        }

        // 查找该地点的活跃 NPC
        List<Npc> candidates = npcMapper.selectList(new LambdaQueryWrapper<Npc>()
                .eq(Npc::getHomeLocationId, locationId)
                .eq(Npc::getActive, 1));

        if (candidates.isEmpty()) {
            // 没有特定 NPC 的地点，从所有 NPC 中随机选一个
            List<Npc> all = listActiveNpcs();
            if (all.isEmpty()) return Optional.empty();
            candidates = all;
        }

        // 随机选一个
        Npc npc = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));

        // 查找或创建关系
        UserNpcRelation relation = relationMapper.selectOne(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId)
                .eq(UserNpcRelation::getNpcId, npc.getId()));

        int familiarityGain;
        if (relation == null) {
            familiarityGain = ThreadLocalRandom.current().nextInt(2, 4);
            relation = new UserNpcRelation();
            relation.setUserId(userId);
            relation.setNpcId(npc.getId());
            relation.setFamiliarity(familiarityGain);
            relation.setMetCount(1);
            relation.setLastMetWeek(currentWeek);
            relationMapper.insert(relation);
        } else {
            familiarityGain = ThreadLocalRandom.current().nextInt(1, 3);
            relation.setFamiliarity(Math.min(100, relation.getFamiliarity() + familiarityGain));
            relation.setMetCount(relation.getMetCount() + 1);
            relation.setLastMetWeek(currentWeek);
            relationMapper.updateById(relation);
        }

        // 填充 npc 字段供模板使用
        relation.setNpc(npc);

        // 生成遇见文案和倾向提示（不实际修改属性）
        String encounterText = generateEncounterText(npc, relation);
        String tendencyHint = generateTendencyHint(npc);

        return Optional.of(new NpcEncounter(
                npc, relation, familiarityGain, encounterText, tendencyHint
        ));
    }

    @Override
    @Transactional
    public void increaseFamiliarity(Long userId, Long npcId, int amount) {
        UserNpcRelation relation = relationMapper.selectOne(new LambdaQueryWrapper<UserNpcRelation>()
                .eq(UserNpcRelation::getUserId, userId)
                .eq(UserNpcRelation::getNpcId, npcId));

        if (relation != null) {
            relation.setFamiliarity(Math.min(100, relation.getFamiliarity() + amount));
            relationMapper.updateById(relation);
        }
    }

    private String generateEncounterText(Npc npc, UserNpcRelation relation) {
        if (relation.getMetCount() <= 1) {
            return "在校园里偶然遇到了 " + npc.getNpcName() + "。" + (npc.getPersonality() != null ? npc.getPersonality() : "");
        } else if (relation.getFamiliarity() < 30) {
            return "又碰到了 " + npc.getNpcName() + "，你们开始熟悉起来了。";
        } else if (relation.getFamiliarity() < 60) {
            return npc.getNpcName() + "主动跟你打招呼，你们聊了几句。";
        } else {
            return npc.getNpcName() + "已经成为你的搭子了，默契十足。";
        }
    }

    private String generateTendencyHint(Npc npc) {
        String fav = npc.getFavoriteAttribute();
        if ("academic".equals(fav)) return "和 TA 在一起学业容易进步";
        if ("health".equals(fav)) return "和 TA 在一起更想运动";
        if ("social".equals(fav)) return "和 TA 在一起社交机会更多";
        if ("skill".equals(fav)) return "和 TA 在一起技能提升更快";
        if ("pressure".equals(fav)) return "和 TA 在一起压力会低一些";
        if ("discipline".equals(fav)) return "和 TA 在一起自律更容易坚持";
        return "和 TA 在一起总有点好事";
    }
}