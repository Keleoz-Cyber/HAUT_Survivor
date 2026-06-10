package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_influence_log")
public class UserInfluenceLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer weekNumber;
    private Long locationId;
    private String sourceType;
    private String sourceName;
    private String description;
    private Integer academicChange;
    private Integer healthChange;
    private Integer moneyChange;
    private Integer socialChange;
    private Integer skillChange;
    private Integer pressureChange;
    private Integer disciplineChange;
    private Integer expChange;
    private Integer exploreBonus;
    private LocalDateTime createdAt;
}
