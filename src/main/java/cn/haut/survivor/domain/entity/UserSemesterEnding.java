package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_semester_ending")
public class UserSemesterEnding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long endingId;
    private String growthRoute;
    private Integer semesterNumber;
    private Integer academic;
    private Integer health;
    private Integer social;
    private Integer skill;
    private Integer pressure;
    private Integer discipline;
    private java.time.LocalDateTime createTime;
}