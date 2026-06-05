package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_location_exploration")
public class UserLocationExploration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long locationId;
    private Integer exploreLevel;
    private Integer exploreCount;
    private Integer lastExploreWeek;
}
