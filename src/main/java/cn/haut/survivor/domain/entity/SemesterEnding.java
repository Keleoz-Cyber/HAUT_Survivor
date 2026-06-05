package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("semester_ending")
public class SemesterEnding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String endingName;
    private String endingType;
    private String description;
    private String conditionRule;
    private Integer priority;
    private String themeColor;
    private String icon;
}
