package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_npc_weekly_action")
public class UserNpcWeeklyAction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long npcId;
    private Integer weekNumber;
    private Integer interacted;
    private Integer buddySelected;
    private LocalDateTime selectedAt;
    private LocalDateTime interactedAt;
}
