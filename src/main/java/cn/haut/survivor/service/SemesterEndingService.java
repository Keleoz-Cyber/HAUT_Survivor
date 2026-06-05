package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.SemesterEnding;
import cn.haut.survivor.domain.entity.UserSemesterEnding;

import java.util.List;

public interface SemesterEndingService {

    /** 获取所有结局定义 */
    List<SemesterEnding> listAllEndings();

    /** 为用户执行学期结算，返回匹配的结局 */
    SemesterEnding settleSemester(Long userId);

    /** 获取用户的结局记录 */
    UserSemesterEnding findUserEnding(Long userId);

    /** 获取用户已解锁的所有结局历史 */
    List<UserSemesterEnding> listUserEndingHistory(Long userId);

    /** 检查用户当前学期是否已完成结算 */
    boolean hasSettled(Long userId);

    /** 结局结算上下文：汇总探索/组织/副本等维度数据 */
    record SettlementContext(
            int labExploreLevel,
            int libraryExploreLevel,
            int playgroundExploreLevel,
            int orgContribution,
            boolean dungeon1Completed,
            boolean dungeon2Completed,
            String dungeon1Evaluation,
            String dungeon2Evaluation
    ) {}

    /** 获取用户的结算上下文数据 */
    SettlementContext buildSettlementContext(Long userId);
}
