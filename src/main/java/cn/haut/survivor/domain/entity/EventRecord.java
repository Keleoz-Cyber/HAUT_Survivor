package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_record")
public class EventRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long eventId;
    private Long optionId;
    private String resultText;
    private LocalDateTime createTime;

    /** 本次行动导致的属性变化（非持久化，仅用于页面展示） */
    @TableField(exist = false)
    private AttributeChange attributeChange;
}
