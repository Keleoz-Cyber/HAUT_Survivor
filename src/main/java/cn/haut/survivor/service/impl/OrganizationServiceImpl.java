package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
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
import cn.haut.survivor.service.WeeklyThemeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    /** 每个组织类型对应多组活动文案，随机选一条让每次活动更有差异感 */
    private static final Map<String, List<String>> ACTIVITY_TEXTS = Map.of(
            "学生会", List.of(
                    "你帮学长布置了会议室，桌椅排列得像军训一样整齐。学长说'下次还找你'，你不知道该高兴还是害怕。",
                    "你负责统计了一整天的活动报名表，Excel 函数用得比写代码还溜。辅导员路过夸了一句'这表格做得好看'。",
                    "你维持了一场讲座的现场秩序，前排的学弟学妹们还算配合，就是有人偷偷在后排刷手机。你假装没看到。",
                    "你帮忙策划了一次班级团建活动，虽然预算只有 200 块，但你硬是安排出了'很有仪式感'的效果。",
                    "你参与了校运会志愿者安排，站在起点线旁吹了一上午哨子。嗓子哑了，但组织能力确实提升了。"
            ),
            "实验室", List.of(
                    "师兄让你修一个神奇的 Bug：页面在本地跑得好好的，部署就 404。你排查了两小时发现是路径大小写写错了。师兄说'这就是实战'。",
                    "你花了整个下午写接口文档，每个字段都加了注释，请求示例写得像教科书。师兄说'这是我见过最规范的文档'。",
                    "你帮师兄跑了一组实验数据，对着 Excel 整理了三百行。虽然枯燥，但师兄请你喝了杯咖啡，还分享了项目经验。",
                    "你参加了一次项目评审会，老师问了一个你完全没准备的问题。好在师兄帮你圆了过去，但你决定以后一定要提前看会议纪要。",
                    "你在实验室调试了一个下午的接口，Postman 发了上百次请求。最后一个 200 状态码出现的时候，你差点从椅子上跳起来。"
            ),
            "社团", List.of(
                    "晚训跑完十圈，教练让你们做拉伸。你的腿在发抖，但旁边那个一直在鼓励你'最后三秒'。训练结束大家一起去买了冰棍。",
                    "友谊赛你被安排打控卫，虽然运球被断了两次，但传出了三个好球。队友说'意识不错，就是手跟不上脑'。",
                    "你帮忙记了一场比赛的分数，顺便在场边捡了十几颗球。虽然没有上场，但和几个外院的同学混了脸熟。",
                    "社团组织了一次三对三对抗赛，你虽然输了但进了一个漂亮的上篮。对手都说'下次还要一起打'。",
                    "训练结束后大家坐在场边聊天，队长讲了去年联赛差点进八强的故事。你突然觉得篮球社不止是打球，更像一个家。"
            )
    );

    private final OrganizationMapper organizationMapper;
    private final UserOrganizationMapper userOrganizationMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerAttributeMapper playerAttributeMapper;
    private final PlayerService playerService;
    private final WeeklyThemeService weeklyThemeService;

    public OrganizationServiceImpl(OrganizationMapper organizationMapper,
                                    UserOrganizationMapper userOrganizationMapper,
                                    PlayerProfileMapper playerProfileMapper,
                                    PlayerAttributeMapper playerAttributeMapper,
                                    PlayerService playerService,
                                    WeeklyThemeService weeklyThemeService) {
        this.organizationMapper = organizationMapper;
        this.userOrganizationMapper = userOrganizationMapper;
        this.playerProfileMapper = playerProfileMapper;
        this.playerAttributeMapper = playerAttributeMapper;
        this.playerService = playerService;
        this.weeklyThemeService = weeklyThemeService;
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

        // 面试判定：社交 >= 40 即可通过（开学适应周降低 5 点）
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        int baseRequiredSocial = 40;
        int requiredSocial = Math.max(0, baseRequiredSocial
                - weeklyThemeService.organizationJoinSocialRequirementReduction(profile.getCurrentWeek()));
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
        if (attribute.getSocial() < requiredSocial) {
            throw new IllegalArgumentException("社交值不足，" + org.getOrgName()
                    + "面试未通过。当前社交：" + attribute.getSocial()
                    + "，需要：" + requiredSocial);
        }

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

        PlayerProfile profile = playerService.findProfileByUserId(userId);
        int recruitmentBonus = organizationActivityBonus(profile);

        // 增加贡献和声望。社团招新周让组织活动收益更高，但不额外改变属性。
        relation.setContribution(relation.getContribution() + 3 + recruitmentBonus);
        relation.setReputation(relation.getReputation() + 2 + recruitmentBonus);

        // 按组织类型差异化属性结算
        PlayerAttribute attribute = playerService.findAttributeByUserId(userId);

        switch (org.getOrgType()) {
            case "学生会" -> {
                // 学生会偏社交/自律，活动繁忙增加压力
                attribute.setSocial(clamp(attribute.getSocial() + 3));
                attribute.setDiscipline(clamp(attribute.getDiscipline() + 2));
                attribute.setPressure(clamp(attribute.getPressure() + 2));
            }
            case "实验室" -> {
                // 实验室偏技能/学业，项目压力大
                attribute.setSkill(clamp(attribute.getSkill() + 3));
                attribute.setAcademic(clamp(attribute.getAcademic() + 2));
                attribute.setPressure(clamp(attribute.getPressure() + 3));
            }
            case "社团" -> {
                // 篮球社偏健康/社交，运动减压
                attribute.setHealth(clamp(attribute.getHealth() + 3));
                attribute.setSocial(clamp(attribute.getSocial() + 1));
                attribute.setPressure(clamp(attribute.getPressure() - 2));
            }
            default -> {
                // 通用：社交+3，自律+1，压力+2
                attribute.setSocial(clamp(attribute.getSocial() + 3));
                attribute.setDiscipline(clamp(attribute.getDiscipline() + 1));
                attribute.setPressure(clamp(attribute.getPressure() + 2));
            }
        }
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

    private int organizationActivityBonus(PlayerProfile profile) {
        if (profile == null || profile.getCurrentWeek() == null) {
            return 0;
        }
        return weeklyThemeService.organizationActivityBonus(profile.getCurrentWeek());
    }

    private boolean isRecruitmentWeek(PlayerProfile profile) {
        return organizationActivityBonus(profile) > 0;
    }

    @Override
    public OrganizationActivityResult attendActivityWithChange(Long userId, Long organizationId) {
        Organization org = requireOrg(organizationId);
        // 保存旧值用于计算实际变化
        PlayerAttribute before = playerService.findAttributeByUserId(userId);
        int oldAcademic = before.getAcademic(), oldHealth = before.getHealth();
        int oldSocial = before.getSocial(), oldSkill = before.getSkill();
        int oldPressure = before.getPressure(), oldDiscipline = before.getDiscipline();

        UserOrganization relation = attendActivity(userId, organizationId);

        // 读取更新后的值，计算实际变化
        PlayerAttribute after = playerService.findAttributeByUserId(userId);
        AttributeChange change = switch (org.getOrgType()) {
            case "学生会" -> new AttributeChange(after.getAcademic() - oldAcademic, 0, 0, after.getSocial() - oldSocial, 0, after.getPressure() - oldPressure, after.getDiscipline() - oldDiscipline, 0);
            case "实验室" -> new AttributeChange(after.getAcademic() - oldAcademic, 0, 0, 0, after.getSkill() - oldSkill, after.getPressure() - oldPressure, 0, 0);
            case "社团"  -> new AttributeChange(0, after.getHealth() - oldHealth, 0, after.getSocial() - oldSocial, 0, after.getPressure() - oldPressure, 0, 0);
            default      -> new AttributeChange(0, 0, 0, after.getSocial() - oldSocial, 0, after.getPressure() - oldPressure, after.getDiscipline() - oldDiscipline, 0);
        };

        // 随机选一条活动文案
        String activityResultText = generateActivityResultText(org.getOrgType());
        PlayerProfile profile = playerService.findProfileByUserId(userId);
        if (isRecruitmentWeek(profile)) {
            activityResultText += " 赶上社团招新周，摊位人气正旺，本次活动额外获得贡献 +1、声望 +1。";
        }

        return new OrganizationActivityResult(relation, change, activityResultText);
    }

    private String generateActivityResultText(String orgType) {
        List<String> texts = ACTIVITY_TEXTS.getOrDefault(orgType, List.of(
                "你参加了组织活动，完成了今天的任务。"
        ));
        return texts.get(ThreadLocalRandom.current().nextInt(texts.size()));
    }
}
