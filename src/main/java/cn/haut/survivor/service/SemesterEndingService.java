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

    /** 检查用户是否已完成结算 */
    boolean hasSettled(Long userId);
}
