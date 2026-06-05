package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("organization")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgName;
    private String orgType;
    private String description;
    private Long unlockLocationId;
    private Integer unlockExploreLevel;
    private String recommendedAttribute;
    private Integer weeklyApCost;
    private String themeColor;
    private Integer status;
}
