package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("event")
public class Event {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String eventName;
    private String eventType;
    private Long locationId;
    private String description;
    private String sceneImage;
    private String moodTag;
    private Integer probability;
    private Integer minWeek;
    private Integer maxWeek;
    private Integer minExploreLevel;
    private Integer status;
}
