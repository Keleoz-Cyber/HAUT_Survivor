package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.Organization;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserOrganization;
import cn.haut.survivor.mapper.OrganizationMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
import cn.haut.survivor.mapper.UserOrganizationMapper;
import cn.haut.survivor.service.OrganizationService;
import cn.haut.survivor.service.PlayerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationMapper organizationMapper;
    private final UserOrganizationMapper userOrganizationMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final PlayerService playerService;

    public OrganizationServiceImpl(OrganizationMapper organizationMapper,
                                    UserOrganizationMapper userOrganizationMapper,
                                    PlayerProfileMapper playerProfileMapper,
                                    PlayerAttributeMapper playerAttributeMapper,
                                    PlayerService playerService) {
        this.organizationMapper = organizationMapper;
        this.userOrganizationMapper = userOrganizationMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.playerService = playerService;
    }

    @Override
    public List<Organization> listAll() {
        return organizationMapper.selectList(new LambdaQueryWrapper<Organization>()
                .eq(Organization::getStatus, 1)
                .orderByAsc(Organization::getId));
    }

    @Override
    public List<UserOrganization> listUserOrganizations(Long userId) {
        return userOrganizationMapper.selectList(new LambdaQueryWrapper<UserOrganization>()
                .eq(UserOrganization::getUserId, userId));
    }

    @Override
    @Transactional
    public UserOrganization discover(Long userId, Long organizationId) {
        UserOrganization existing = findRelation(userId, organizationId);
        if (existing != null) {
            return existing;
        }
        UserOrganization relation = new UserOrganization();
        relation.setUserId(userId);
        relation.setOrganizationId(organizationId);
        relation.setMembershipStatus("discovered");
        relation.setPositionName("非成员");
        relation.setContribution(0);
        relation.setReputation(0);
        userOrganizationMapper.insert(relation);
        return relation;
    }

    @Override
    @Transactional
    public UserOrganization join(Long userId, Long organizationId) {
        requireProfile(userId);
        Organization org = requireOrg(organizationId);

        UserOrganization relation = findRelation(userId, organizationId);
        if (relation == null) {
            relation = discover(userId, organizationId);
        }
        if ("member".equals(relation.getMembershipStatus())
                || "core_member".equals(relation.getMembershipStatus())
                || "leader".equals(relation.getMembershipStatus())) {
            throw new IllegalArgumentException("你已经加入了" + org.getOrgName());
        }

        // 面试判定：社交 >= 40 即可通过
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        if (attribute.getSocial() < 40) {
            throw new IllegalArgumentException("社交值不足，" + org.getOrgName() + "面试未通过。当前社交：" + attribute.getSocial());
        }

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        relation.setMembershipStatus("member");
        relation.setPositionName("干事");
        relation.setJoinWeek(profile.getCurrentWeek());
        userOrganizationMapper.updateById(relation);
        return relation;
    }

    @Override
    @Transactional
    public UserOrganization attendActivity(Long userId, Long organizationId) {
        requireProfile(userId);
        Organization org = requireOrg(organizationId);

        UserOrganization relation = findRelation(userId, organizationId);
        if (relation == null || !"member".equals(relation.getMembershipStatus())
                && !"core_member".equals(relation.getMembershipStatus())
                && !"leader".equals(relation.getMembershipStatus())) {
            throw new IllegalArgumentException("你不是" + org.getOrgName() + "的成员");
        }

        // 消耗行动点
        playerService.consumeActionPoint(userId);

        // 增加贡献和声望
        relation.setContribution(relation.getContribution() + 3);
        relation.setReputation(relation.getReputation() + 2);

        // 属性结算：社交 +3，自律 +1，压力 +2（时间被占用了）
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        attribute.setSocial(clamp(attribute.getSocial() + 3));
        attribute.setDiscipline(clamp(attribute.getDiscipline() + 1));
        attribute.setPressure(clamp(attribute.getPressure() + 2));
        attribute.setUpdateTime(LocalDateTime.now());
        playerAttributeMapper.updateById(attribute);

        // 贡献够了晋升
        if (relation.getContribution() >= 10 && "member".equals(relation.getMembershipStatus())) {
            relation.setMembershipStatus("core_member");
            relation.setPositionName("部长");
        }

        userOrganizationMapper.updateById(relation);
        return relation;
    }

    @Override
    public UserOrganization findRelation(Long userId, Long organizationId) {
        return userOrganizationMapper.selectOne(new LambdaQueryWrapper<UserOrganization>()
                .eq(UserOrganization::getUserId, userId)
                .eq(UserOrganization::getOrganizationId, organizationId)
                .last("LIMIT 1"));
    }

    private Organization requireOrg(Long id) {
        Organization org = organizationMapper.selectById(id);
        if (org == null) {
            throw new IllegalArgumentException("组织不存在");
        }
        return org;
    }

    private void requireProfile(Long userId) {
        if (!playerService.hasProfile(userId)) {
            throw new IllegalArgumentException("请先创建角色");
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
