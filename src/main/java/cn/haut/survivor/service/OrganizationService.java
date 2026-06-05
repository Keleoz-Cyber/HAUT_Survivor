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
        // 默认实现：组织活动固定属性变化 社交+3 自律+1 压力+2
        return new OrganizationActivityResult(relation, new AttributeChange(0, 0, 0, 3, 0, 2, 1, 0));
    }

    /** 查找用户对某个组织的关系，没有则返回 null。 */
    UserOrganization findRelation(Long userId, Long organizationId);

    /** 组织活动结果 */
    record OrganizationActivityResult(
            UserOrganization relation,
            AttributeChange attributeChange
    ) {}
}
