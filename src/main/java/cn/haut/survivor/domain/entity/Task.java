package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String taskName;
    private String taskType;
    private String difficulty;
    private LocalDateTime deadline;
    private String status;
    private Integer rewardExp;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
