package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("player_profile")
public class PlayerProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String playerName;
    private String grade;
    private String majorType;
    private String growthRoute;
    private Integer level;
    private Integer exp;
    private Integer currentWeek;
    private Integer actionPoints;
    private Integer maxActionPoints;
    private String semesterPhase;
    private Integer semesterNumber;
    private String currentTitle;
    private LocalDateTime createTime;
}
