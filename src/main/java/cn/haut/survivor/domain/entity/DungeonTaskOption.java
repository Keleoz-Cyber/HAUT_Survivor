package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dungeon_task_option")
public class DungeonTaskOption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dungeonTaskId;
    private String optionType;
    private String optionText;
    private Integer isCorrect;
    private Integer triggerProbability;
    private String resultText;
    private String evaluation;
    private Integer score;
    private Integer academicChange;
    private Integer healthChange;
    private Integer moneyChange;
    private Integer socialChange;
    private Integer skillChange;
    private Integer pressureChange;
    private Integer disciplineChange;
    private Integer expChange;
    private Long nextTaskId;
    private Integer status;
}
