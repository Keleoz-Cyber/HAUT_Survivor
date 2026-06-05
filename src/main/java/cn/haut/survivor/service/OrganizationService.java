package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.Organization;
import cn.haut.survivor.domain.entity.UserOrganization;

import java.util.List;

public interface OrganizationService {

    /** 列出所有活跃组织。 */
    List<Organization> listAll();

    /** 查看用户对所有组织的关系（含未加入的）。 */
    List<UserOrganization> listUserOrganizations(Long userId);

    /** 用户发现组织（状态变为 discovered）。 */
    UserOrganization discover(Long userId, Long organizationId);

    /** 用户申请加入组织（面试/报名）。 */
    UserOrganization join(Long userId, Long organizationId);

    /** 参加本周组织活动，消耗行动点，加贡献和属性。返回更新后的关系。 */
    UserOrganization attendActivity(Long userId, Long organizationId);

    /** 参加本周组织活动并返回属性变化。 */
    default OrganizationActivityResult attendActivityWithChange(Long userId, Long organizationId) {
        UserOrganization relation = attendActivity(userId, organizationId);
        // 属性变化由组织类型决定，这里按 orgType 返回对应快照
        // Controller 需要拿到 orgType 才能选择正确的 AttributeChange，
        // 但默认实现无法获取 orgType。由 OrganizationServiceImpl 覆盖。
        return new OrganizationActivityResult(relation, AttributeChange.EMPTY);
    }

    /** 查找用户对某个组织的关系，没有则返回 null。 */
    UserOrganization findRelation(Long userId, Long organizationId);

    /** 组织活动结果 */
    record OrganizationActivityResult(
            UserOrganization relation,
            AttributeChange attributeChange
    ) {}
}
