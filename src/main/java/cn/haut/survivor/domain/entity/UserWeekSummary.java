package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_week_summary")
public class UserWeekSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer weekNumber;
    private String themeName;
    private String goalResult;
    private String summaryText;
    private LocalDateTime createdAt;
}
