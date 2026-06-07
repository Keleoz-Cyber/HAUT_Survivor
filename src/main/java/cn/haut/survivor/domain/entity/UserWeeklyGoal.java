package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_weekly_goal")
public class UserWeeklyGoal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer weekNumber;
    private Long goalId;
    private Integer startValue;
    private Integer currentValue;
    private Integer completed;
    private Integer claimed;
    private LocalDateTime createdAt;
}
