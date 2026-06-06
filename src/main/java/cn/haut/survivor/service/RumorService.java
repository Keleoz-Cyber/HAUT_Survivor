package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Rumor;
import java.util.List;

/**
 * 校园传闻系统：每周给玩家 2-3 条校园传闻。
 */
public interface RumorService {

    /** 获取指定周的活跃传闻列表 */
    List<Rumor> listByWeek(int weekNumber);

    /** 为指定用户稳定抽取本周传闻（同一用户同一周刷新页面看到相同传闻） */
    List<Rumor> pickRumorsForUser(Long userId, int weekNumber, int count);
}