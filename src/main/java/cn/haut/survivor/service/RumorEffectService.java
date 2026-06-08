package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;

import java.util.List;

public interface RumorEffectService {
    List<ExplorationInfluence> getExplorationInfluences(Long userId, int weekNumber, Long locationId);
}
