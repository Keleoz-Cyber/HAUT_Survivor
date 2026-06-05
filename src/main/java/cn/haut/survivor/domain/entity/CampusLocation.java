package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("campus_location")
public class CampusLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String locationName;
    private String campus;
    private String description;
    private String iconKey;
    private String backgroundImage;
    private String themeColor;
    private Integer status;
}
