package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("npc_interaction")
public class NpcInteraction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long npcId;
    private String interactionKey;
    private String interactionName;
    private Integer requiredFamiliarity;
    private String description;
    private String resultText;
    private Integer academicChange;
    private Integer healthChange;
    private Integer moneyChange;
    private Integer socialChange;
    private Integer skillChange;
    private Integer pressureChange;
    private Integer disciplineChange;
    private Integer familiarityChange;
    private Integer expChange;
    private Integer active;
}
