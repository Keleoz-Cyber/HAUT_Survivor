package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dungeon")
public class Dungeon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dungeonName;
    private String dungeonType;
    private String description;
    private String coverImage;
    private String themeStyle;
    private Integer estimatedMinutes;
    private String difficultyLabel;
    private Integer rewardExp;
    private String rewardTitle;
    private Integer status;
}
