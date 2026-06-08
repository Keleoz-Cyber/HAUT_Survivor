package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("rumor")
public class Rumor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer weekNumber;
    private Long locationId;
    private String rumorTitle;
    private String rumorText;
    private String effectHint;
    private String effectType;
    private Integer effectValue;
    private String effectTarget;
    private String rarity;
    private Integer active;
}
