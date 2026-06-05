package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.DungeonTaskOption;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserDungeonTaskRecord;

import java.util.List;

public interface DungeonService {

    Dungeon findDemoDungeon();

    List<DungeonTask> listTasks(Long dungeonId);

    List<DungeonTaskOption> listOptions(Long taskId);

    UserDungeonRecord startOrResumeDemoDungeon(Long userId);

    UserDungeonRecord findRecordById(Long userId, Long recordId);

    DungeonTask findCurrentTask(UserDungeonRecord record);

    UserDungeonTaskRecord chooseOption(Long userId, Long recordId, Long taskId, Long optionId, String minigameResult);

    UserDungeonTaskRecord chooseMinigameRelations(Long userId, Long recordId, Long taskId,
                                                  List<String> selectedRelations, Integer elapsedSeconds);
}
