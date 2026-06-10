package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_npc_story_progress")
public class UserNpcStoryProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long npcId;
    private String storyKey;
    private Integer stage;
    private Integer completed;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
