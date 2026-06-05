package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dungeon_task")
public class DungeonTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dungeonId;
    private String taskName;
    private String taskType;
    private Integer taskOrder;
    private String sceneText;
    private String targetText;
    private String backgroundImage;
    private String minigameType;
    private String minigameConfig;
    private Integer timerSeconds;
    private String settlementRule;
    private Integer randomEnabled;
    private String attributeCheckRule;
    private String passCondition;
    private Integer required;
    private Integer status;
}
