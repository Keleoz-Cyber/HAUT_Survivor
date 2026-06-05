package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.DungeonTaskOption;
import cn.haut.survivor.domain.entity.UserDungeonRecord;
import cn.haut.survivor.domain.entity.UserDungeonTaskRecord;
import cn.haut.survivor.service.DungeonService.BugQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class DungeonServiceTests {

    @Autowired
    private DungeonService dungeonService;

    @Autowired
    private PlayerService playerService;

    @BeforeEach
    void setUpPlayer() {
        playerService.createProfile(2L, "副本测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void demoDungeonLoadsSeededStages() {
        Dungeon dungeon = dungeonService.findDemoDungeon();

        assertThat(dungeon.getDungeonName()).isEqualTo("Java 课设：DDL 前夜");
        assertThat(dungeon.getEstimatedMinutes()).isEqualTo(8);
        assertThat(dungeonService.listTasks(dungeon.getId()))
                .extracting(DungeonTask::getTaskName)
                .contains("需求风暴", "数据库拼图", "Bug 暴走");
    }

    @Test
    void choosingDungeonOptionUpdatesProgressAndAttributes() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        DungeonTask task = dungeonService.findCurrentTask(record);
        DungeonTaskOption option = dungeonService.listOptions(task.getId()).get(0);

        UserDungeonTaskRecord taskRecord = dungeonService.chooseOption(2L, record.getId(), task.getId(), option.getId(), null);
        UserDungeonRecord updatedRecord = dungeonService.startOrResumeDemoDungeon(2L);

        assertThat(taskRecord.getEvaluation()).isNotBlank();
        assertThat(taskRecord.getResultText()).contains("课设");
        assertThat(updatedRecord.getCurrentTaskId()).isNotEqualTo(task.getId());
        assertThat(updatedRecord.getTotalScore()).isGreaterThan(0);
    }

    @Test
    void firstStageChoiceStoresConsequenceFlags() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);

        dungeonService.chooseOption(2L, record.getId(), 1L, 1L, null);

        UserDungeonRecord updatedRecord = dungeonService.startOrResumeDemoDungeon(2L);
        assertThat(updatedRecord.getRiskFlags()).contains("scope_controlled");
    }

    @Test
    void riskyFirstStageChoiceStoresScopeSprawlFlag() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);

        dungeonService.chooseOption(2L, record.getId(), 1L, 2L, null);

        UserDungeonRecord updatedRecord = dungeonService.startOrResumeDemoDungeon(2L);
        assertThat(updatedRecord.getRiskFlags()).contains("scope_sprawl");
    }

    @Test
    void databaseRelationMinigameRewardsCorrectRelations() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseOption(2L, record.getId(), 1L, 1L, null);
        UserDungeonRecord minigameRecord = dungeonService.startOrResumeDemoDungeon(2L);

        UserDungeonTaskRecord taskRecord = dungeonService.chooseMinigameRelations(2L, minigameRecord.getId(), 2L,
                List.of("user->player_attribute", "event->event_option", "dungeon->dungeon_task"), 18);
        UserDungeonRecord updatedRecord = dungeonService.startOrResumeDemoDungeon(2L);

        assertThat(taskRecord.getEvaluation()).isEqualTo("结构清晰");
        assertThat(taskRecord.getScore()).isGreaterThanOrEqualTo(90);
        assertThat(updatedRecord.getCurrentTaskId()).isEqualTo(3L);
        assertThat(updatedRecord.getRiskFlags()).contains("schema_clear");
    }

    @Test
    void databaseRelationMinigameCarriesBadConsequences() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseOption(2L, record.getId(), 1L, 2L, null);
        UserDungeonRecord minigameRecord = dungeonService.startOrResumeDemoDungeon(2L);

        UserDungeonTaskRecord taskRecord = dungeonService.chooseMinigameRelations(2L, minigameRecord.getId(), 2L,
                List.of("user->event"), 70);
        UserDungeonRecord updatedRecord = dungeonService.startOrResumeDemoDungeon(2L);

        assertThat(taskRecord.getEvaluation()).isEqualTo("表关系迷雾");
        assertThat(taskRecord.getScore()).isLessThan(50);
        assertThat(updatedRecord.getRiskFlags()).contains("scope_sprawl", "schema_mist");
    }

    // ==================== Bug Hunt 小游戏测试 ====================

    @Test
    void generateBugQuestionsReturnsThreeFromPool() {
        List<BugQuestion> questions = dungeonService.generateBugQuestions();

        assertThat(questions).hasSize(3);
        assertThat(questions).allMatch(q -> q.symptom() != null && !q.symptom().isBlank());
        assertThat(questions).allMatch(q -> q.options().size() == 4);
        assertThat(questions).allMatch(q -> q.correctIndex() >= 0 && q.correctIndex() < 4);
    }

    @Test
    void bugHuntAllCorrectProducesHighScore() {
        // 完成前两阶段
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseOption(2L, record.getId(), 1L, 1L, null);
        UserDungeonRecord minigameRecord = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseMinigameRelations(2L, minigameRecord.getId(), 2L,
                List.of("user->player_attribute", "event->event_option", "dungeon->dungeon_task"), 18);
        UserDungeonRecord bugStageRecord = dungeonService.startOrResumeDemoDungeon(2L);

        // 提交全部正确答案：题目 0→1, 1→0, 2→0
        UserDungeonTaskRecord taskRecord = dungeonService.chooseBugHunt(
                2L, bugStageRecord.getId(), 3L,
                List.of(0, 1, 2), List.of(1, 0, 0), 20);

        assertThat(taskRecord.getScore()).isGreaterThanOrEqualTo(80);
        assertThat(taskRecord.getEvaluation()).isEqualTo("Bug 猎人");
        assertThat(taskRecord.getResultText()).contains("控制台终于安静了");
    }

    @Test
    void bugHuntSomeWrongProducesMediumScore() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseOption(2L, record.getId(), 1L, 3L, null);
        UserDungeonRecord minigameRecord = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseMinigameRelations(2L, minigameRecord.getId(), 2L,
                List.of("user->player_attribute"), 40);
        UserDungeonRecord bugStageRecord = dungeonService.startOrResumeDemoDungeon(2L);

        // 只答对 1 题：题目 0→1（正确）, 1→99（错误）, 2→99（错误）
        UserDungeonTaskRecord taskRecord = dungeonService.chooseBugHunt(
                2L, bugStageRecord.getId(), 3L,
                List.of(0, 1, 2), List.of(1, 3, 3), 40);

        assertThat(taskRecord.getScore()).isBetween(20, 70);
        assertThat(taskRecord.getEvaluation()).isIn("勉强修复", "Bug 反杀");
    }

    @Test
    void bugHuntAllWrongProducesLowScore() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseOption(2L, record.getId(), 1L, 2L, null);
        UserDungeonRecord minigameRecord = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseMinigameRelations(2L, minigameRecord.getId(), 2L,
                List.of("user->event"), 70);
        UserDungeonRecord bugStageRecord = dungeonService.startOrResumeDemoDungeon(2L);

        // 全部答错
        UserDungeonTaskRecord taskRecord = dungeonService.chooseBugHunt(
                2L, bugStageRecord.getId(), 3L,
                List.of(0, 1, 2), List.of(3, 3, 3), 70);

        assertThat(taskRecord.getScore()).isLessThan(50);
        assertThat(taskRecord.getEvaluation()).isEqualTo("Bug 反杀");
    }

    @Test
    void strongRunWithBugHuntProducesWarriorEnding() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseOption(2L, record.getId(), 1L, 1L, null);
        UserDungeonRecord minigameRecord = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseMinigameRelations(2L, minigameRecord.getId(), 2L,
                List.of("user->player_attribute", "event->event_option", "dungeon->dungeon_task"), 18);
        UserDungeonRecord bugStageRecord = dungeonService.startOrResumeDemoDungeon(2L);

        dungeonService.chooseBugHunt(2L, bugStageRecord.getId(), 3L,
                List.of(0, 1, 2), List.of(1, 0, 0), 20);

        UserDungeonRecord completedRecord = dungeonService.findRecordById(2L, bugStageRecord.getId());
        assertThat(completedRecord.getStatus()).isEqualTo("COMPLETED");
        assertThat(completedRecord.getFinalEvaluation()).isEqualTo("课设战神");
        assertThat(completedRecord.getRiskFlags()).contains("scope_controlled", "schema_clear", "bug_crushed");
    }

    @Test
    void weakRunWithBugAvalancheProducesSilentEnding() {
        UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseOption(2L, record.getId(), 1L, 2L, null);
        UserDungeonRecord minigameRecord = dungeonService.startOrResumeDemoDungeon(2L);
        dungeonService.chooseMinigameRelations(2L, minigameRecord.getId(), 2L,
                List.of("user->event"), 70);
        UserDungeonRecord bugStageRecord = dungeonService.startOrResumeDemoDungeon(2L);

        dungeonService.chooseBugHunt(2L, bugStageRecord.getId(), 3L,
                List.of(0, 1, 2), List.of(3, 3, 3), 70);

        UserDungeonRecord completedRecord = dungeonService.findRecordById(2L, bugStageRecord.getId());
        assertThat(completedRecord.getStatus()).isEqualTo("COMPLETED");
        assertThat(completedRecord.getRiskFlags()).contains("scope_sprawl", "schema_mist", "bug_avalanche");
        assertThat(completedRecord.getFinalEvaluation()).isEqualTo("答辩沉默现场");
    }
}
