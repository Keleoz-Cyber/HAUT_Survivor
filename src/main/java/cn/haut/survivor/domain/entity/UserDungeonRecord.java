package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_dungeon_record")
public class UserDungeonRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long dungeonId;
    private Long currentTaskId;
    private String status;
    private Integer totalScore;
    private String riskFlags;
    private String finalEvaluation;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}
