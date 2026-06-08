package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exploration_story_progress")
public class ExplorationStoryProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String chainKey;
    private Integer currentStep;
    private Integer completed;
    private Integer lastTriggerWeek;
    private LocalDateTime updateTime;
}
