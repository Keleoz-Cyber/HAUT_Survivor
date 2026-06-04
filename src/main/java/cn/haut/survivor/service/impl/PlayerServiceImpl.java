package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.service.PlayerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerAttributeMapper playerAttributeMapper;

    public PlayerServiceImpl(PlayerProfileMapper playerProfileMapper, PlayerAttributeMapper playerAttributeMapper) {
        this.playerProfileMapper = playerProfileMapper;
        this.playerAttributeMapper = playerAttributeMapper;
    }

    @Override
    @Transactional
    public PlayerProfile createProfile(Long userId, String playerName, String grade, String majorType, String growthRoute) {
        if (userId == null) {
            throw new IllegalArgumentException("用户不能为空");
        }
        if (hasProfile(userId)) {
            throw new IllegalArgumentException("角色档案已存在");
        }

        PlayerProfile profile = new PlayerProfile();
        profile.setUserId(userId);
        profile.setPlayerName(requireText(playerName, "角色名不能为空"));
        profile.setGrade(requireText(grade, "年级不能为空"));
        profile.setMajorType(requireText(majorType, "专业类型不能为空"));
        profile.setGrowthRoute(requireText(growthRoute, "成长路线不能为空"));
        profile.setLevel(1);
        profile.setExp(0);
        profile.setCurrentWeek(1);
        profile.setCurrentTitle("新生求生者");
        profile.setCreateTime(LocalDateTime.now());
        playerProfileMapper.insert(profile);

        PlayerAttribute attribute = createDefaultAttribute(userId);
        applyGrowthRoute(attribute, profile.getGrowthRoute());
        playerAttributeMapper.insert(attribute);

        return profile;
    }

    @Override
    public PlayerProfile findProfileByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return playerProfileMapper.selectOne(new LambdaQueryWrapper<PlayerProfile>()
                .eq(PlayerProfile::getUserId, userId)
                .last("LIMIT 1"));
    }

    @Override
    public PlayerAttribute findAttributeByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
                .eq(PlayerAttribute::getUserId, userId)
                .last("LIMIT 1"));
    }

    @Override
    public boolean hasProfile(Long userId) {
        return findProfileByUserId(userId) != null;
    }

    private PlayerAttribute createDefaultAttribute(Long userId) {
        PlayerAttribute attribute = new PlayerAttribute();
        attribute.setUserId(userId);
        attribute.setAcademic(60);
        attribute.setHealth(70);
        attribute.setMoney(80);
        attribute.setSocial(50);
        attribute.setSkill(40);
        attribute.setPressure(30);
        attribute.setDiscipline(50);
        attribute.setUpdateTime(LocalDateTime.now());
        return attribute;
    }

    private void applyGrowthRoute(PlayerAttribute attribute, String growthRoute) {
        switch (growthRoute) {
            case "考研路线" -> {
                attribute.setAcademic(attribute.getAcademic() + 10);
                attribute.setPressure(attribute.getPressure() + 5);
                attribute.setDiscipline(attribute.getDiscipline() + 5);
            }
            case "就业路线" -> {
                attribute.setSkill(attribute.getSkill() + 10);
                attribute.setSocial(attribute.getSocial() + 5);
            }
            case "竞赛路线" -> {
                attribute.setAcademic(attribute.getAcademic() + 5);
                attribute.setSkill(attribute.getSkill() + 12);
                attribute.setPressure(attribute.getPressure() + 5);
            }
            case "六边形路线" -> {
                attribute.setAcademic(attribute.getAcademic() + 3);
                attribute.setHealth(attribute.getHealth() + 3);
                attribute.setSocial(attribute.getSocial() + 3);
                attribute.setSkill(attribute.getSkill() + 3);
                attribute.setDiscipline(attribute.getDiscipline() + 3);
            }
            case "摆烂求生路线" -> {
                attribute.setAcademic(attribute.getAcademic() - 5);
                attribute.setHealth(attribute.getHealth() + 10);
                attribute.setPressure(attribute.getPressure() - 10);
                attribute.setDiscipline(attribute.getDiscipline() - 5);
            }
            default -> throw new IllegalArgumentException("未知成长路线");
        }
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
