package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_npc_relation")
public class UserNpcRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long npcId;
    private Integer familiarity;
    private Integer metCount;
    private Integer lastMetWeek;

    /** 关联的 NPC 对象（非数据库字段，由 Service 填充） */
    @TableField(exist = false)
    private Npc npc;
}
