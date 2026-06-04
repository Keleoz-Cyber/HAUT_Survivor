package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;

public interface PlayerService {

    PlayerProfile createProfile(Long userId, String playerName, String grade, String majorType, String growthRoute);

    PlayerProfile findProfileByUserId(Long userId);

    PlayerAttribute findAttributeByUserId(Long userId);

    boolean hasProfile(Long userId);
}
