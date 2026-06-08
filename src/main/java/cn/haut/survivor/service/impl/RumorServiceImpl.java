package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.mapper.RumorMapper;
import cn.haut.survivor.service.RumorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RumorServiceImpl implements RumorService {

    private final RumorMapper rumorMapper;

    public RumorServiceImpl(RumorMapper rumorMapper) {
        this.rumorMapper = rumorMapper;
    }

    @Override
    public List<Rumor> listByWeek(int weekNumber) {
        return rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .eq(Rumor::getWeekNumber, weekNumber)
                .eq(Rumor::getActive, 1));
    }

    @Override
    public List<Rumor> pickRumorsForUser(Long userId, int weekNumber, int count) {
        List<Rumor> pool = listByWeek(weekNumber);
        if (pool.isEmpty()) return Collections.emptyList();

        // 简单稳定抽取：用 userId 做偏移，同一用户同一周看到相同传闻
        int offset = (int) (userId % pool.size());
        List<Rumor> rotated = new ArrayList<>();
        for (int i = 0; i < pool.size(); i++) {
            rotated.add(pool.get((offset + i) % pool.size()));
        }

        return rotated.subList(0, Math.min(count, rotated.size()));
    }

    @Override
    public List<Rumor> pickVisibleRumorsForUser(Long userId, int weekNumber) {
        return pickRumorsForUser(userId, weekNumber, 3);
    }
}