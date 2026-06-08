package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exploration_story_chain")
public class ExplorationStoryChain {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String chainKey;
    private String chainName;
    private Long locationId;
    private Integer weekNumber;
    private Integer requiredExploreLevel;
    private Integer stepNumber;
    private String scenarioText;
    private String resultText;
    private Integer academicChange;
    private Integer healthChange;
    private Integer moneyChange;
    private Integer socialChange;
    private Integer skillChange;
    private Integer pressureChange;
    private Integer disciplineChange;
    private Integer expChange;
    private Integer nextStepNumber;
    private Integer active;
}
