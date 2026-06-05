package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.DungeonTaskOption;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserDungeonTaskRecord;

import java.util.List;

public interface DungeonService {

    /** Bug 定位小游戏的题目。 */
    record BugQuestion(int id, String symptom, List<String> options, int correctIndex) {
    }

    Dungeon findDemoDungeon();

    List<Dungeon> listAllDungeons();

    Dungeon findDungeonById(Long dungeonId);

    UserDungeonRecord startOrResumeDungeon(Long userId, Long dungeonId);

    List<DungeonTask> listTasks(Long dungeonId);

    List<DungeonTaskOption> listOptions(Long taskId);

    UserDungeonRecord startOrResumeDemoDungeon(Long userId);

    UserDungeonRecord findRecordById(Long userId, Long recordId);

    DungeonTask findCurrentTask(UserDungeonRecord record);

    UserDungeonTaskRecord chooseOption(Long userId, Long recordId, Long taskId, Long optionId, String minigameResult);

    UserDungeonTaskRecord chooseMinigameRelations(Long userId, Long recordId, Long taskId,
                                                  List<String> selectedRelations, Integer elapsedSeconds);

    /** 随机抽取 3 道 Bug 定位题目。 */
    List<BugQuestion> generateBugQuestions();

    /** 提交 Bug 定位答案并结算。 */
    UserDungeonTaskRecord chooseBugHunt(Long userId, Long recordId, Long taskId,
                                        List<Integer> questionIds, List<Integer> answers,
                                        Integer elapsedSeconds);
}
