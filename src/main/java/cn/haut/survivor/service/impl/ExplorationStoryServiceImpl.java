package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.ExplorationStoryProgress;
import cn.haut.survivor.mapper.ExplorationStoryChainMapper;
import cn.haut.survivor.mapper.ExplorationStoryProgressMapper;
import cn.haut.survivor.service.ExplorationStoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExplorationStoryServiceImpl implements ExplorationStoryService {

    private static final int TRIGGER_PROBABILITY = 25;

    private final ExplorationStoryChainMapper chainMapper;
    private final ExplorationStoryProgressMapper progressMapper;

    public ExplorationStoryServiceImpl(ExplorationStoryChainMapper chainMapper,
                                       ExplorationStoryProgressMapper progressMapper) {
        this.chainMapper = chainMapper;
        this.progressMapper = progressMapper;
    }

    @Override
    @Transactional
    public Optional<ExplorationStoryResult> maybeTrigger(Long userId, Long locationId, int weekNumber, int exploreLevel) {
        if (ThreadLocalRandom.current().nextInt(100) >= TRIGGER_PROBABILITY) {
            return Optional.empty();
        }

        List<ExplorationStoryChain> candidates = chainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .eq(ExplorationStoryChain::getLocationId, locationId)
                .eq(ExplorationStoryChain::getActive, 1)
                .le(ExplorationStoryChain::getRequiredExploreLevel, exploreLevel)
                .and(q -> q.eq(ExplorationStoryChain::getWeekNumber, 0).or().eq(ExplorationStoryChain::getWeekNumber, weekNumber))
                .orderByAsc(ExplorationStoryChain::getId));

        for (ExplorationStoryChain firstStep : candidates) {
            ExplorationStoryProgress progress = findProgress(userId, firstStep.getChainKey());
            if (progress != null && value(progress.getCompleted()) == 1) {
                continue;
            }
            int step = progress == null ? 1 : value(progress.getCurrentStep());
            return triggerSpecificStep(userId, firstStep.getChainKey(), step, weekNumber);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<ExplorationStoryResult> triggerSpecificStep(Long userId, String chainKey, int stepNumber, int weekNumber) {
        ExplorationStoryProgress progress = findProgress(userId, chainKey);
        if (progress != null && value(progress.getCompleted()) == 1) {
            return Optional.empty();
        }

        ExplorationStoryChain chain = chainMapper.selectOne(new LambdaQueryWrapper<ExplorationStoryChain>()
                .eq(ExplorationStoryChain::getChainKey, chainKey)
                .eq(ExplorationStoryChain::getStepNumber, stepNumber)
                .eq(ExplorationStoryChain::getActive, 1)
                .last("LIMIT 1"));

        if (chain == null) {
            return Optional.empty();
        }

        if (progress == null) {
            progress = new ExplorationStoryProgress();
            progress.setUserId(userId);
            progress.setChainKey(chainKey);
            progress.setCurrentStep(stepNumber);
            progress.setCompleted(0);
            progress.setLastTriggerWeek(weekNumber);
            progress.setUpdateTime(LocalDateTime.now());
            progressMapper.insert(progress);
        }

        boolean completed = chain.getNextStepNumber() == null;
        progress.setCurrentStep(completed ? stepNumber : chain.getNextStepNumber());
        progress.setCompleted(completed ? 1 : 0);
        progress.setLastTriggerWeek(weekNumber);
        progress.setUpdateTime(LocalDateTime.now());
        progressMapper.updateById(progress);

        AttributeChange change = new AttributeChange(
                value(chain.getAcademicChange()),
                value(chain.getHealthChange()),
                value(chain.getMoneyChange()),
                value(chain.getSocialChange()),
                value(chain.getSkillChange()),
                value(chain.getPressureChange()),
                value(chain.getDisciplineChange()),
                value(chain.getExpChange()));

        String storyText = chain.getScenarioText() + " " + chain.getResultText();
        return Optional.of(new ExplorationStoryResult(chain, progress, change, storyText, completed));
    }

    private ExplorationStoryProgress findProgress(Long userId, String chainKey) {
        return progressMapper.selectOne(new LambdaQueryWrapper<ExplorationStoryProgress>()
                .eq(ExplorationStoryProgress::getUserId, userId)
                .eq(ExplorationStoryProgress::getChainKey, chainKey)
                .last("LIMIT 1"));
    }

    private int value(Integer value) {
        return value != null ? value : 0;
    }
}