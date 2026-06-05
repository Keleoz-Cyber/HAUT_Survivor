package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_dungeon_task_record")
public class UserDungeonTaskRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userDungeonRecordId;
    private Long dungeonTaskId;
    private String taskType;
    private Long selectedOptionId;
    private Long randomResultId;
    private String minigameResult;
    private String attributeCheckResult;
    private String resultText;
    private String evaluation;
    private Integer score;
    private Integer expChange;
    private LocalDateTime createTime;
}
