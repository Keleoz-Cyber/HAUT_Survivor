package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("npc")
public class Npc {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String npcName;
    private String npcType;
    private Long homeLocationId;
    private String personality;
    private String description;
    private String avatarIcon;
    private String favoriteAttribute;
    private Integer active;
}
