package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_organization")
public class UserOrganization {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long organizationId;
    private String membershipStatus;
    private String positionName;
    private Integer contribution;
    private Integer reputation;
    private Integer joinWeek;
}
