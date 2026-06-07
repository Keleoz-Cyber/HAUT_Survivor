package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String achievementKey;
    private String achievementName;
    private String description;
    private String icon;
    private String conditionType;
    private Integer conditionValue;
    private String rewardTitle;
    private Integer active;
}
