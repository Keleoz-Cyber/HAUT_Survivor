package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.mapper.UserLocationExplorationMapper;
import cn.haut.survivor.mapper.UserOrganizationMapper;
import cn.haut.survivor.mapper.UserSemesterEndingMapper;
import cn.haut.survivor.service.PlayerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class PlayerServiceImpl implements PlayerService {

    /** Demo 版学期总周数 */
    private static final int MAX_SEMESTER_WEEKS = 4;

    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final UserLocationExplorationMapper explorationMapper;
    private final UserOrganizationMapper userOrganizationMapper;
    private final UserSemesterEndingMapper userSemesterEndingMapper;

    public PlayerServiceImpl(PlayerProfileMapper playerProfileMapper,
                             PlayerAttributeMapper playerAttributeMapper,
                             UserLocationExplorationMapper explorationMapper,
                             UserOrganizationMapper userOrganizationMapper,
                             UserSemesterEndingMapper userSemesterEndingMapper) {
        this.playerProfileMapper = playerProfileMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.explorationMapper = explorationMapper;
        this.userOrganizationMapper = userOrganizationMapper;
        this.userSemesterEndingMapper = userSemesterEndingMapper;
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
        profile.setActionPoints(4);
        profile.setMaxActionPoints(4);
        profile.setSemesterPhase("early");
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

    @Override
    @Transactional
    public PlayerProfile consumeActionPoint(Long userId) {
        PlayerProfile profile = requireProfile(userId);
        if (isSemesterOver(userId)) {
            throw new IllegalArgumentException("学期已结束");
        }
        if (profile.getActionPoints() <= 0) {
            throw new IllegalArgumentException("本周行动点已用完，请结束本周或等待下周");
        }
        profile.setActionPoints(profile.getActionPoints() - 1);
        playerProfileMapper.updateById(profile);
        return profile;
    }

    @Override
    @Transactional
    public PlayerProfile advanceWeek(Long userId) {
        PlayerProfile profile = requireProfile(userId);
        if (isSemesterOver(userId)) {
            throw new IllegalArgumentException("学期已结束，无法推进周次");
        }

        int nextWeek = profile.getCurrentWeek() + 1;
        profile.setCurrentWeek(nextWeek);
        profile.setActionPoints(profile.getMaxActionPoints());

        // 更新学期阶段
        if (nextWeek <= 2) {
            profile.setSemesterPhase("early");
        } else if (nextWeek <= 3) {
            profile.setSemesterPhase("mid");
        } else {
            profile.setSemesterPhase("final");
        }

        // 周结算：压力过高扣健康
        PlayerAttribute attribute = findAttributeByUserId(userId);
        if (attribute != null) {
            if (attribute.getPressure() > 80) {
                attribute.setHealth(clamp(attribute.getHealth() - 3));
            }
            if (attribute.getHealth() < 20) {
                // 健康过低，下周少 1 行动点
                profile.setMaxActionPoints(Math.max(2, profile.getMaxActionPoints() - 1));
            }
            attribute.setPressure(clamp(attribute.getPressure() - 5)); // 每周自然减压
            attribute.setUpdateTime(LocalDateTime.now());
            playerAttributeMapper.updateById(attribute);
        }

        playerProfileMapper.updateById(profile);
        return profile;
    }

    @Override
    public boolean isSemesterOver(Long userId) {
        PlayerProfile profile = findProfileByUserId(userId);
        return profile != null && profile.getCurrentWeek() > MAX_SEMESTER_WEEKS;
    }

    @Override
    public String getWeekPhaseLabel(PlayerProfile profile) {
        if (profile == null) return "";
        int week = profile.getCurrentWeek();
        if (week > MAX_SEMESTER_WEEKS) return "学期结束";
        String phase = switch (profile.getSemesterPhase()) {
            case "early" -> "开学适应期";
            case "mid" -> "期中节奏期";
            case "final" -> "DDL 高压期";
            default -> "学期中";
        };
        return "第 " + week + " 周 · " + phase + "（共 " + MAX_SEMESTER_WEEKS + " 周）";
    }

    private PlayerProfile requireProfile(Long userId) {
        PlayerProfile profile = findProfileByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        return profile;
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

    @Override
    @Transactional
    public void resetSemester(Long userId) {
        PlayerProfile profile = requireProfile(userId);
        String growthRoute = profile.getGrowthRoute();

        // 重置 profile
        profile.setCurrentWeek(1);
        profile.setActionPoints(4);
        profile.setMaxActionPoints(4);
        profile.setSemesterPhase("early");
        playerProfileMapper.updateById(profile);

        // 重置属性为默认值 + 成长路线加成
        PlayerAttribute attribute = findAttributeByUserId(userId);
        attribute.setAcademic(60);
        attribute.setHealth(70);
        attribute.setMoney(80);
        attribute.setSocial(50);
        attribute.setSkill(40);
        attribute.setPressure(30);
        attribute.setDiscipline(50);
        applyGrowthRoute(attribute, growthRoute);
        attribute.setUpdateTime(LocalDateTime.now());
        playerAttributeMapper.updateById(attribute);

        // 清理探索记录
        explorationMapper.delete(new LambdaQueryWrapper<cn.haut.survivor.domain.entity.UserLocationExploration>()
                .eq(cn.haut.survivor.domain.entity.UserLocationExploration::getUserId, userId));

        // 清理组织关系
        userOrganizationMapper.delete(new LambdaQueryWrapper<cn.haut.survivor.domain.entity.UserOrganization>()
                .eq(cn.haut.survivor.domain.entity.UserOrganization::getUserId, userId));

        // 清理本学期结局记录（保留历史）
        userSemesterEndingMapper.delete(new LambdaQueryWrapper<cn.haut.survivor.domain.entity.UserSemesterEnding>()
                .eq(cn.haut.survivor.domain.entity.UserSemesterEnding::getUserId, userId));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
