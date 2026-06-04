package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("player_attribute")
public class PlayerAttribute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer academic;
    private Integer health;
    private Integer money;
    private Integer social;
    private Integer skill;
    private Integer pressure;
    private Integer discipline;
    private LocalDateTime updateTime;
}
