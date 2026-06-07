package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("weekly_goal")
public class WeeklyGoal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String goalKey;
    private String goalName;
    private String description;
    private String goalType;
    private Integer targetValue;
    private Integer rewardExp;
    private String rewardAttribute;
    private Integer rewardAmount;
    private Integer active;
}
