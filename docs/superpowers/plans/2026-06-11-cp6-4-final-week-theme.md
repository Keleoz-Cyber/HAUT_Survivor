# CP6.4 Final Week Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make week 4 "期末与体测周" affect actual play through final-week events, library/playground exploration feedback, a physical-test dungeon, and focused week-4 seed content.

**Architecture:** Keep the change lightweight and data-first. Centralize new week-4 gameplay hooks in `WeeklyThemeService`, let `WeeklyModifierService` and `DungeonServiceImpl` consume those hooks, and append CP6.4 seed data to the existing `data-content-pack-6.sql` file. Do not add tables, do not rewrite UI, and do not change the existing week 1/2/3 behavior except where tests explicitly guard it.

**Tech Stack:** Spring Boot 3.3.5, Java 17, MyBatis-Plus, H2/MySQL schema-compatible SQL seed files, Thymeleaf existing dungeon/exploration UI.

---

## Execution Prompt

Copy this prompt to the coding AI:

```text
你现在接手 HAUT Survivor 的 CP6.4。

项目路径：
D:\study\code\java\classlearn\HAUT_Survivor

请先阅读：
1. README.md
2. docs/PROJECT_COMPLETION_STATUS.md
3. docs/NEXT_AI_HANDOFF.md
4. docs/AI_CONTINUATION_PROMPT.md
5. docs/superpowers/plans/2026-06-11-cp6-4-final-week-theme.md

任务目标：
实现 CP6.4「期末与体测周机制化」。

必须遵守：
- 不新增表，不改 schema。
- 不重写 UI，不改 game-shell/game-dock/vn-result/sticky-note__btn 体系。
- 继续复用 data-content-pack-6.sql，CP6.4 使用 6400 ID 段。
- 地点 ID 必须以 data.sql 为准：图书馆=2，操场=5，食堂=4，宿舍=3，生活服务点/博闻楼营业厅=8。不要把操场写成 8。
- 周主题 hook 统一放在 WeeklyThemeService；WeeklyModifierService 和 DungeonServiceImpl 只消费 hook。
- 先写/改测试，再实现。
- 修改后必须运行 .\mvnw.cmd clean test。
- 如有页面或流程可访问，做 HTTP 冒烟：/dashboard、/map、/map/location/2/event、/map/location/5/event、/exploration、POST /exploration/5、/week/summary、/dungeons、/dungeons/6401、/dungeons/6401/start、/dungeons/6401/play。
- 最后提交 git，并给出完整执行反馈。

本计划的验收重点：
1. 第 4 周仍偏向健康事件。
2. 第 4 周图书馆探索有复习收益，操场探索有体测收益；来源面板显示为周主题影响。
3. 第 4 周 physical 类型副本压力变化有 -1 缓冲；第 3 周 DDL 压力 +1 不被破坏。
4. CP6.4 seed 包含 6 事件、18 选项、4 传闻、2 条奇遇链/6 阶段、1 个体测副本/3 阶段/9 选项、2 周目标、2 成就。
5. 所有测试通过，无 Whitelabel。
```

## File Map

- Modify `src/main/java/cn/haut/survivor/service/WeeklyThemeService.java`
  - Add final-week exploration attribute hook.
  - Add final-week physical dungeon pressure relief hook.
  - Add final-week dungeon result suffix hook.

- Modify `src/main/java/cn/haut/survivor/service/WeeklyModifierService.java`
  - Inject `WeeklyThemeService`.
  - Fix week-4 key location list from library + old `8` to library `2` + playground `5`.
  - Include the final-week `AttributeChange` in the returned `ExplorationInfluence`.

- Modify `src/main/java/cn/haut/survivor/service/impl/DungeonServiceImpl.java`
  - Pass `DungeonTask` into reward settlement.
  - Apply `WeeklyThemeService.finalWeekDungeonPressureRelief(...)` when computing pressure deltas.
  - Append `WeeklyThemeService.finalWeekDungeonResultSuffix(...)` to task result text when relevant.

- Modify `src/main/resources/data-content-pack-6.sql`
  - Append CP6.4 seed using `6400` ID ranges.

- Add `src/test/java/cn/haut/survivor/service/ContentPack6FinalWeekTests.java`
  - Validate all CP6.4 seed rows.

- Modify `src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java`
  - Cover the new week-4 hooks.

- Modify `src/test/java/cn/haut/survivor/service/WeeklyModifierServiceTests.java`
  - Cover library/playground week-4 influence.
  - Guard against location `8` incorrectly receiving the final-week physical-test bonus.

- Modify `src/test/java/cn/haut/survivor/service/DungeonServiceTests.java`
  - Cover week-4 physical dungeon pressure relief and result text suffix.
  - Guard existing week-3 DDL pressure behavior.

- Modify docs after implementation:
  - `docs/PROJECT_COMPLETION_STATUS.md`
  - `docs/NEXT_AI_HANDOFF.md`
  - `docs/AI_CONTINUATION_PROMPT.md`

## Task 1: Add CP6.4 Seed Verification Tests

**Files:**
- Create: `src/test/java/cn/haut/survivor/service/ContentPack6FinalWeekTests.java`

- [ ] **Step 1: Create the failing test class**

Add this file:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.DungeonTaskOption;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.DungeonMapper;
import cn.haut.survivor.mapper.DungeonTaskMapper;
import cn.haut.survivor.mapper.DungeonTaskOptionMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.ExplorationStoryChainMapper;
import cn.haut.survivor.mapper.RumorMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
class ContentPack6FinalWeekTests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private ExplorationStoryChainMapper explorationStoryChainMapper;

    @Autowired
    private DungeonMapper dungeonMapper;

    @Autowired
    private DungeonTaskMapper dungeonTaskMapper;

    @Autowired
    private DungeonTaskOptionMapper dungeonTaskOptionMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void cp64FinalWeekEventsAreSeededWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6401L)
                .le(Event::getId, 6406L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "图书馆闭馆前冲刺",
                        "考前重点互认",
                        "体测前热身队列",
                        "1000 米配速选择",
                        "清淡补给窗口",
                        "宿舍早睡协议");
        assertThat(events).extracting(Event::getLocationId)
                .containsExactly(2L, 2L, 5L, 5L, 4L, 3L);
        assertThat(events).extracting(Event::getEventType)
                .contains("学习", "健康", "生活");

        for (Event event : events) {
            List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                    .eq(EventOption::getEventId, event.getId()));
            assertThat(options)
                    .as("event " + event.getId() + " should have 3 options")
                    .hasSize(3);
        }
    }

    @Test
    void cp64RumorsUseExistingEffectTypesAndTargets() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6401L)
                .le(Rumor::getId, 6404L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        assertThat(rumors).extracting(Rumor::getEffectType)
                .containsExactly("event_hint", "explore_bonus", "safe_zone", "attr_bonus");
        assertThat(rumors).extracting(Rumor::getEffectTarget)
                .containsExactly("health", "explore", "pressure", "academic");
        assertThat(rumors).allMatch(rumor -> rumor.getWeekNumber() == 4);
    }

    @Test
    void cp64StoryChainsCoverLibraryReviewAndPhysicalTestRoutes() {
        List<ExplorationStoryChain> chains = explorationStoryChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .ge(ExplorationStoryChain::getId, 6401L)
                .le(ExplorationStoryChain::getId, 6406L)
                .orderByAsc(ExplorationStoryChain::getChainKey)
                .orderByAsc(ExplorationStoryChain::getStepNumber));

        assertThat(chains).hasSize(6);
        assertThat(chains).extracting(ExplorationStoryChain::getChainKey)
                .containsOnly("final_library_review", "physical_test_route");
        assertThat(chains).filteredOn(chain -> "final_library_review".equals(chain.getChainKey()))
                .extracting(ExplorationStoryChain::getLocationId)
                .containsOnly(2L);
        assertThat(chains).filteredOn(chain -> "physical_test_route".equals(chain.getChainKey()))
                .extracting(ExplorationStoryChain::getLocationId)
                .containsOnly(5L);
        assertThat(chains).filteredOn(chain -> chain.getStepNumber() == 3)
                .allMatch(chain -> chain.getNextStep() == null);
    }

    @Test
    void cp64PhysicalTestDungeonIsSeededWithOrderedStages() {
        Dungeon dungeon = dungeonMapper.selectById(6401L);

        assertThat(dungeon).isNotNull();
        assertThat(dungeon.getDungeonName()).isEqualTo("体测生存挑战");
        assertThat(dungeon.getDungeonType()).isEqualTo("physical");
        assertThat(dungeon.getRewardTitle()).isEqualTo("体测通关者");

        List<DungeonTask> tasks = dungeonTaskMapper.selectList(new LambdaQueryWrapper<DungeonTask>()
                .eq(DungeonTask::getDungeonId, 6401L)
                .orderByAsc(DungeonTask::getTaskOrder));
        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(DungeonTask::getTaskName)
                .containsExactly("1000 米配速", "引体向上排队", "坐位体前屈补救");

        for (DungeonTask task : tasks) {
            List<DungeonTaskOption> options = dungeonTaskOptionMapper.selectList(new LambdaQueryWrapper<DungeonTaskOption>()
                    .eq(DungeonTaskOption::getDungeonTaskId, task.getId()));
            assertThat(options)
                    .as("task " + task.getId() + " should have 3 options")
                    .hasSize(3);
        }
    }

    @Test
    void cp64WeeklyGoalsAndAchievementsUseExistingConditionTypes() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "cp64_final_review_route", "cp64_physical_test_push")
                .orderByAsc(WeeklyGoal::getId));
        assertThat(goals).hasSize(2);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .containsExactly("exploration_story_step", "dungeon_stage");

        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey, "cp64_final_week_stable", "cp64_physical_test_started")
                .orderByAsc(Achievement::getId));
        assertThat(achievements).hasSize(2);
        assertThat(achievements).extracting(Achievement::getConditionType)
                .containsExactly("exploration_story_step", "dungeon_stage");
    }
}
```

- [ ] **Step 2: Run the new tests and confirm they fail**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6FinalWeekTests test
```

Expected:

```text
Tests run: 5, Failures: 5
```

The failures should be missing CP6.4 seed rows. If the test class does not compile, fix imports or entity getter names before continuing.

## Task 2: Add Weekly Theme Hook Tests

**Files:**
- Modify: `src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java`
- Modify: `src/test/java/cn/haut/survivor/service/WeeklyModifierServiceTests.java`

- [ ] **Step 1: Extend `WeeklyThemeServiceTests.weeklyGameplayHooksAreCentralizedByThemeWeek`**

Add these assertions near the existing `dungeonPressureBonus` assertions:

```java
assertThat(service.finalWeekExplorationAttributeChange(4, 2L).academicChange()).isEqualTo(1);
assertThat(service.finalWeekExplorationAttributeChange(4, 2L).skillChange()).isEqualTo(1);
assertThat(service.finalWeekExplorationAttributeChange(4, 2L).pressureChange()).isEqualTo(-1);
assertThat(service.finalWeekExplorationAttributeChange(4, 5L).healthChange()).isEqualTo(2);
assertThat(service.finalWeekExplorationAttributeChange(4, 5L).pressureChange()).isEqualTo(-1);
assertThat(service.finalWeekExplorationAttributeChange(4, 8L).hasAnyChange()).isFalse();
assertThat(service.finalWeekExplorationAttributeChange(3, 2L).hasAnyChange()).isFalse();

assertThat(service.finalWeekDungeonPressureRelief(4, "physical")).isEqualTo(-1);
assertThat(service.finalWeekDungeonPressureRelief(4, "academic")).isZero();
assertThat(service.finalWeekDungeonPressureRelief(3, "physical")).isZero();
assertThat(service.finalWeekDungeonResultSuffix(4, "physical")).contains("期末与体测周");
assertThat(service.finalWeekDungeonResultSuffix(3, "physical")).isBlank();
```

- [ ] **Step 2: Extend `WeeklyModifierServiceTests`**

Add these two tests:

```java
@Test
void weekFourLibraryAddsReviewInfluence() {
    ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(4, 2L);

    assertThat(influence.sourceType()).isEqualTo("weekly_theme");
    assertThat(influence.sourceName()).isEqualTo("期末与体测周");
    assertThat(influence.exploreBonus()).isEqualTo(1);
    assertThat(influence.attributeChange().academicChange()).isEqualTo(1);
    assertThat(influence.attributeChange().skillChange()).isEqualTo(1);
    assertThat(influence.attributeChange().pressureChange()).isEqualTo(-1);
}

@Test
void weekFourPlaygroundAddsPhysicalTestInfluenceButServiceWindowDoesNot() {
    ExplorationInfluence playground = weeklyModifierService.getExplorationInfluence(4, 5L);
    ExplorationInfluence serviceWindow = weeklyModifierService.getExplorationInfluence(4, 8L);

    assertThat(playground.sourceName()).isEqualTo("期末与体测周");
    assertThat(playground.exploreBonus()).isEqualTo(1);
    assertThat(playground.attributeChange().healthChange()).isEqualTo(2);
    assertThat(playground.attributeChange().pressureChange()).isEqualTo(-1);

    assertThat(serviceWindow.hasEffect()).isFalse();
}
```

- [ ] **Step 3: Run hook tests and confirm they fail**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyThemeServiceTests,WeeklyModifierServiceTests test
```

Expected: fail because the new methods do not exist and week 4 currently targets location `8`.

## Task 3: Implement Weekly Theme Hooks

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/WeeklyThemeService.java`
- Modify: `src/main/java/cn/haut/survivor/service/WeeklyModifierService.java`

- [ ] **Step 1: Add `AttributeChange` import and hook methods to `WeeklyThemeService`**

Add the import:

```java
import cn.haut.survivor.domain.entity.AttributeChange;
```

Add these methods after `dungeonPressureBonus`:

```java
/** Final week makes library review and playground physical-test routes more productive. */
public AttributeChange finalWeekExplorationAttributeChange(Integer currentWeek, Long locationId) {
    if (getTheme(currentWeek == null ? 1 : currentWeek).week() != 4 || locationId == null) {
        return AttributeChange.EMPTY;
    }
    if (locationId == 2L) {
        return new AttributeChange(1, 0, 0, 0, 1, -1, 1, 0);
    }
    if (locationId == 5L) {
        return new AttributeChange(0, 2, 0, 0, 0, -1, 1, 0);
    }
    return AttributeChange.EMPTY;
}

/** Final week slightly buffers pressure in physical-test dungeons. */
public int finalWeekDungeonPressureRelief(Integer currentWeek, String dungeonType) {
    if (getTheme(currentWeek == null ? 1 : currentWeek).week() != 4) {
        return 0;
    }
    return "physical".equalsIgnoreCase(dungeonType) ? -1 : 0;
}

/** Short suffix shown on physical dungeon result text when final-week relief applies. */
public String finalWeekDungeonResultSuffix(Integer currentWeek, String dungeonType) {
    return finalWeekDungeonPressureRelief(currentWeek, dungeonType) < 0
            ? " 期末与体测周：你提前适应了节奏，本阶段压力额外 -1。"
            : "";
}
```

- [ ] **Step 2: Modify `WeeklyModifierService` constructor and week-4 branch**

Replace the field-free class shape with constructor injection:

```java
@Service
public class WeeklyModifierService {

    private final WeeklyThemeService weeklyThemeService;

    public WeeklyModifierService(WeeklyThemeService weeklyThemeService) {
        this.weeklyThemeService = weeklyThemeService;
    }

    public ExplorationInfluence getExplorationInfluence(int weekNumber, Long locationId) {
        // existing branches
    }
}
```

Replace the current week-4 branch with:

```java
if (weekNumber == 4 && (locationId == 2L || locationId == 5L)) {
    AttributeChange finalWeekChange = weeklyThemeService.finalWeekExplorationAttributeChange(weekNumber, locationId);
    String description = locationId == 2L
            ? "期末与体测周：图书馆复习路线更清楚，学业 +1、技能 +1、压力 -1，探索度额外 +1。"
            : "期末与体测周：操场体测路线更明确，健康 +2、压力 -1，探索度额外 +1。";
    return new ExplorationInfluence(
            "weekly_theme",
            "期末与体测周",
            description,
            finalWeekChange,
            1);
}
```

Do not change the week 1/2/3 branches except for constructor-related formatting.

- [ ] **Step 3: Run hook tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyThemeServiceTests,WeeklyModifierServiceTests test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 4: Commit hook changes**

```powershell
git add src/main/java/cn/haut/survivor/service/WeeklyThemeService.java src/main/java/cn/haut/survivor/service/WeeklyModifierService.java src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java src/test/java/cn/haut/survivor/service/WeeklyModifierServiceTests.java
git commit -m "feat: add final week theme hooks"
```

## Task 4: Apply Final-Week Physical Dungeon Pressure Relief

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/DungeonServiceImpl.java`
- Modify: `src/test/java/cn/haut/survivor/service/DungeonServiceTests.java`

- [ ] **Step 1: Add failing dungeon service tests**

Add these tests to `DungeonServiceTests`:

```java
@Test
void finalWeekPhysicalDungeonReducesStagePressureAndShowsSuffix() {
    setCurrentWeek(4);
    UserDungeonRecord record = dungeonService.startOrResumeDungeon(2L, 6401L);
    DungeonTask task = dungeonService.findCurrentTask(record);
    DungeonTaskOption option = dungeonService.listOptions(task.getId()).get(0);

    UserDungeonTaskRecord taskRecord = dungeonService.chooseOption(
            2L, record.getId(), task.getId(), option.getId(), null);

    assertThat(taskRecord.getAttributeChange().pressureChange())
            .isEqualTo(option.getPressureChange() - 1);
    assertThat(taskRecord.getResultText()).contains("期末与体测周");
}

@Test
void finalWeekDoesNotReduceAcademicDungeonPressure() {
    setCurrentWeek(4);
    UserDungeonRecord record = dungeonService.startOrResumeDungeon(2L, 6001L);
    DungeonTask task = dungeonService.findCurrentTask(record);
    DungeonTaskOption option = dungeonService.listOptions(task.getId()).get(0);

    UserDungeonTaskRecord taskRecord = dungeonService.chooseOption(
            2L, record.getId(), task.getId(), option.getId(), null);

    assertThat(taskRecord.getAttributeChange().pressureChange())
            .isEqualTo(option.getPressureChange());
    assertThat(taskRecord.getResultText()).doesNotContain("期末与体测周");
}
```

These tests depend on CP6.4 seed dungeon `6401`, so they may fail with missing dungeon until Task 5 is implemented.

- [ ] **Step 2: Change `chooseOption` to pass the task into reward application**

Replace:

```java
applyRewards(userId, option);
```

with:

```java
applyRewards(userId, task, option);
```

Replace:

```java
taskRecord.setResultText(option.getResultText());
```

with:

```java
taskRecord.setResultText(option.getResultText() + dungeonThemeResultSuffix(userId, task));
```

- [ ] **Step 3: Change minigame reward calls to pass the task**

Replace both occurrences of:

```java
applyDynamicRewards(userId, settlement);
```

with:

```java
applyDynamicRewards(userId, task, settlement);
```

Replace the minigame `taskRecord` result text after `buildMinigameTaskRecord(...)` by appending the suffix:

```java
taskRecord.setResultText(taskRecord.getResultText() + dungeonThemeResultSuffix(userId, task));
```

Add this line in both `chooseMinigameRelations` and `chooseBugHunt` immediately after the `buildMinigameTaskRecord(...)` call.

- [ ] **Step 4: Update reward helper signatures**

Replace:

```java
private void applyRewards(Long userId, DungeonTaskOption option) {
```

with:

```java
private void applyRewards(Long userId, DungeonTask task, DungeonTaskOption option) {
```

Replace:

```java
private void applyDynamicRewards(Long userId, MinigameSettlement settlement) {
```

with:

```java
private void applyDynamicRewards(Long userId, DungeonTask task, MinigameSettlement settlement) {
```

Inside both helpers, replace:

```java
+ dungeonThemePressureBonus(profile)
```

with:

```java
+ dungeonThemePressureDelta(profile, task)
```

- [ ] **Step 5: Replace pressure helper and add suffix helper**

Replace the existing `dungeonThemePressureBonus(PlayerProfile profile)` helper with:

```java
private int dungeonThemePressureDelta(PlayerProfile profile, DungeonTask task) {
    if (profile == null || profile.getCurrentWeek() == null) {
        return 0;
    }
    int delta = weeklyThemeService.dungeonPressureBonus(profile.getCurrentWeek());
    Dungeon dungeon = task == null ? null : dungeonMapper.selectById(task.getDungeonId());
    if (dungeon != null) {
        delta += weeklyThemeService.finalWeekDungeonPressureRelief(profile.getCurrentWeek(), dungeon.getDungeonType());
    }
    return delta;
}

private String dungeonThemeResultSuffix(Long userId, DungeonTask task) {
    PlayerProfile profile = playerService.findProfileByUserId(userId);
    if (profile == null || task == null) {
        return "";
    }
    Dungeon dungeon = dungeonMapper.selectById(task.getDungeonId());
    if (dungeon == null) {
        return "";
    }
    return weeklyThemeService.finalWeekDungeonResultSuffix(profile.getCurrentWeek(), dungeon.getDungeonType());
}
```

- [ ] **Step 6: Run focused tests**

Run:

```powershell
.\mvnw.cmd -Dtest=DungeonServiceTests,WeeklyThemeServiceTests test
```

Expected:

```text
BUILD SUCCESS
```

If `DungeonServiceTests` still fails because dungeon `6401` is missing, finish Task 5 seed first, then rerun this command before committing Task 4.

## Task 5: Append CP6.4 Seed Data

**Files:**
- Modify: `src/main/resources/data-content-pack-6.sql`

- [ ] **Step 1: Append CP6.4 SQL block**

Append this block at the end of `data-content-pack-6.sql`:

```sql
-- =====================================================
-- CP6.4: 期末与体测周机制化
-- =====================================================

INSERT INTO event
(id, event_name, event_type, location_id, description, scene_image, event_tag, trigger_probability, min_week, max_week, min_explore_level, status) VALUES
(6401, '图书馆闭馆前冲刺', '学习', 2, '闭馆音乐快要响起，图书馆里还有几排人没有动。你面前摊着两门课的复习资料，最难的是决定先救哪一门。', 'scene-library', '期末', 70, 4, 4, 0, 1),
(6402, '考前重点互认', '学习', 2, '复习区里有人小声对答案，你听到几个熟悉又陌生的知识点。也许这是查漏补缺的机会，也可能只是新的焦虑来源。', 'scene-library', '复盘', 60, 4, 4, 10, 1),
(6403, '体测前热身队列', '健康', 5, '操场边排着等测试的人，有人原地高抬腿，有人盯着成绩表沉默。轮到你之前，还能做最后一点准备。', 'scene-track', '体测', 75, 4, 4, 0, 1),
(6404, '1000 米配速选择', '健康', 5, '跑道上风不大，但每个人都知道最后一圈会变长。你需要决定是稳住节奏，还是一开始就跟住前排。', 'scene-track', '耐力', 65, 4, 4, 15, 1),
(6405, '清淡补给窗口', '生活', 4, '知雅餐厅的清淡窗口排队不长，旁边却飘来炸物香味。期末周的身体管理，往往从一顿饭开始。', 'scene-canteen', '补给', 55, 4, 4, 0, 1),
(6406, '宿舍早睡协议', '健康', 3, '宿舍里有人还在刷题，有人已经关灯戴眼罩。你们临时约定今晚别互相拖后腿，至少让明天的大脑能启动。', 'scene-dorm', '休息', 55, 4, 4, 0, 1);

INSERT INTO event_option
(id, event_id, option_text, effect_hint, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
(640101, 6401, '先救最容易提分的一门', '学业和自律提升，压力略降', 'low', '你把复习范围缩到最可能拿分的章节，进度条终于开始移动。', 3, 0, 0, 0, 1, -1, 2, 26),
(640102, 6401, '把两门都粗略扫一遍', '覆盖面更广，但压力仍在', 'medium', '你没有押宝，但每个知识点都只碰到表面。至少考场上不会完全陌生。', 2, 0, 0, 0, 1, 1, 1, 20),
(640103, 6401, '继续翻聊天记录找重点', '可能有用，但节奏变散', 'high', '群消息越翻越多，你找到几个重点，也顺手吸收了不少焦虑。', 1, -1, 0, 1, 0, 3, -1, 12),
(640201, 6402, '把听到的重点回到教材里核对', '复盘扎实，技能提升', 'low', '你没有直接照抄答案，而是回到教材确认来源。知识点终于落到纸面上。', 2, 0, 0, 0, 2, -1, 2, 24),
(640202, 6402, '加入讨论，交换复习盲区', '社交和学业小升', 'medium', '你们互相提醒了几个容易漏掉的点，焦虑被拆成了清单。', 2, 0, 0, 2, 0, 0, 1, 22),
(640203, 6402, '听完就走，避免继续焦虑', '压力下降，收益较轻', 'low', '你记下两个关键词后离开讨论区，保住了自己的复习节奏。', 1, 0, 0, 0, 0, -2, 1, 16),
(640301, 6403, '跟着队伍完整热身', '健康提升，压力下降', 'low', '你活动开了肩和腿，轮到自己时没有那么僵。', 0, 3, 0, 1, 0, -2, 1, 24),
(640302, 6403, '只做关键拉伸，保留体力', '稳妥但收益较轻', 'low', '你没有把体力花在等待区，至少上场时状态还算平稳。', 0, 2, 0, 0, 0, -1, 1, 18),
(640303, 6403, '临时刷手机转移注意', '压力短降，自律下降', 'medium', '短视频确实让你忘了一会儿排队，但回过神时心跳更快了。', 0, 0, 0, 0, 0, -1, -2, 10),
(640401, 6404, '前两圈压住速度，最后冲刺', '健康和自律提升', 'low', '你没有被前排带乱节奏，最后一圈还能提速。成绩不夸张，但很稳。', 0, 4, 0, 0, 0, -1, 2, 28),
(640402, 6404, '跟住最快的人', '高风险高压力', 'high', '前半程很有气势，后半程肺开始抗议。你撑完了，但压力也跟着上来。', 0, 2, 0, 1, 0, 3, 0, 18),
(640403, 6404, '找熟人互相报圈速', '社交辅助，节奏稳定', 'medium', '有人提醒你别乱冲，你也帮对方稳住了最后一圈。', 0, 3, 0, 2, 0, -1, 1, 24),
(640501, 6405, '选择清淡套餐和热汤', '健康恢复，压力下降', 'low', '身体没有立刻变强，但胃至少没有继续加班。下午复习也轻了一点。', 0, 2, -2, 0, 0, -1, 1, 18),
(640502, 6405, '和同学拼桌交换考试安排', '生活信息更清楚', 'low', '你们把考试时间和体测安排对了一遍，少了一个记错时间的风险。', 1, 1, 0, 2, 0, -1, 1, 20),
(640503, 6405, '奖励自己一顿重口味', '短期快乐，健康下降', 'medium', '快乐是真的，饭后困意也是真的。你决定晚上至少走一圈。', 0, -1, -3, 0, 0, -1, -1, 12),
(640601, 6406, '约定固定熄灯时间', '健康和自律提升', 'low', '你们把闹钟统一提前，宿舍难得形成了同一条作息战线。', 0, 3, 0, 1, 0, -2, 2, 24),
(640602, 6406, '戴耳塞继续复习一小时', '学业提升但恢复一般', 'medium', '你没有被宿舍动静带乱，但大脑已经明显变慢。', 2, 0, 0, 0, 1, 1, 0, 18),
(640603, 6406, '躺下继续刷重点截图', '看似复习，实则拖延', 'high', '屏幕越刷越亮，重点越看越碎。第二天醒来时，你只记得自己很晚才睡。', 0, -2, 0, 0, 0, 3, -2, 8);

INSERT INTO rumor
(id, week_number, location_id, rumor_title, rumor_content, effect_hint, effect_type, effect_value, effect_target, rarity, status) VALUES
(6401, 4, 5, '体测队伍下午会分流', '有人说下午操场队伍会短一点，但热身时间也更难把握。', '操场事件更偏向健康', 'event_hint', 1, 'health', 'common', 1),
(6402, 4, 2, '图书馆闭馆前半小时空位会松动', '有人离开得早，适合补最后一段复习。', '图书馆探索收益提高', 'explore_bonus', 2, 'explore', 'common', 1),
(6403, 4, 3, '宿舍今晚有人约定早睡', '如果能跟上节奏，明天压力会轻一点。', '宿舍压力下降', 'safe_zone', 2, 'pressure', 'rare', 1),
(6404, 4, 2, '林然整理了最后错题清单', '据说图书馆附近能听到几个最后重点。', '图书馆学业收益提高', 'attr_bonus', 2, 'academic', 'rare', 1);

INSERT INTO exploration_story_chain
(id, chain_key, chain_name, location_id, min_week, min_explore_level, step_number, story_text, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_step, status) VALUES
(6401, 'final_library_review', '最后复习路线', 2, 4, 0, 1, '你发现图书馆不同楼层的复习氛围差别很大，有人背书，有人刷题，也有人只是盯着屏幕发呆。', '你选了一个适合整理错题的位置，复习开始有了入口。', 2, 0, 0, 0, 1, -1, 1, 18, 2, 1),
(6402, 'final_library_review', '最后复习路线', 2, 4, 15, 2, '你把错题、课件和群里重点重新对齐，发现有些焦虑其实来自没有排序。', '你列出了最后一天的复习顺序，压力下降了一点。', 3, 0, 0, 0, 1, -2, 2, 24, 3, 1),
(6403, 'final_library_review', '最后复习路线', 2, 4, 30, 3, '闭馆前，你把最容易忘的知识点写成一页纸。它不保证满分，但能让你明天不慌。', '你完成了一份能带进考前十分钟的复习索引。', 4, 0, 0, 1, 2, -2, 3, 34, NULL, 1),
(6404, 'physical_test_route', '体测缓冲路线', 5, 4, 0, 1, '操场上每个人都像在和自己的身体谈判。你先绕场走了一圈，确认测试点和排队位置。', '路线清楚以后，体测不再像突然袭击。', 0, 2, 0, 0, 0, -1, 1, 18, 2, 1),
(6405, 'physical_test_route', '体测缓冲路线', 5, 4, 15, 2, '你跟着前面同学做了几组热身，顺便记住老师喊号的节奏。', '等待时间没有白耗，身体逐渐进入状态。', 0, 3, 0, 1, 0, -2, 1, 24, 3, 1),
(6406, 'physical_test_route', '体测缓冲路线', 5, 4, 30, 3, '测试结束后，你没有立刻瘫坐，而是慢走到跑道边把呼吸缓下来。', '这次体测没有变成灾难，甚至让你觉得身体还能救。', 0, 5, 0, 1, 0, -3, 2, 34, NULL, 1);

INSERT INTO dungeon
(id, dungeon_name, dungeon_type, description, cover_image, theme_style, estimated_minutes, difficulty_label, reward_exp, reward_title, status) VALUES
(6401, '体测生存挑战', 'physical', '1000 米、引体向上和坐位体前屈排成一条线。你不需要成为运动达人，只需要把节奏稳住，活着走出操场。', 'scene-track', 'HEALTH', 8, '中等', 80, '体测通关者', 1);

INSERT INTO dungeon_task
(id, dungeon_id, task_name, task_type, task_order, scene_text, target_text, background_image, minigame_type, minigame_config, timer_seconds, settlement_rule, random_enabled, attribute_check_rule, pass_condition, required, status) VALUES
(640101, 6401, '1000 米配速', 'choice', 1, '第一项是 1000 米。起跑线前大家都很安静，只有秒表和呼吸声越来越明显。', '选择你的配速策略。', 'scene-track', 'none', NULL, NULL, '配速越稳定，后续压力越低。', 0, 'health>=35', 'score >= 40', 1, 1),
(640102, 6401, '引体向上排队', 'choice', 2, '引体向上区域排队很长，前面有人一次过，也有人挂在杠上开始怀疑人生。', '在等待和上场之间稳住状态。', 'scene-track', 'none', NULL, NULL, '准备方式影响健康和压力。', 0, NULL, 'score >= 40', 1, 1),
(640103, 6401, '坐位体前屈补救', 'choice', 3, '最后一项看起来最安静，但大家的表情说明它并不简单。你还有一点时间活动腿后侧。', '完成最后一项，不让体测周翻车。', 'scene-track', 'none', NULL, NULL, '最后选择决定评价标签。', 0, NULL, 'score >= 50', 1, 1);

INSERT INTO dungeon_task_option
(id, dungeon_task_id, option_type, option_text, is_correct, trigger_probability, result_text, evaluation, score, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_task_id, status) VALUES
(64010101, 640101, 'strategy', '前两圈压速，最后一圈再提', 1, 100, '你没有被起跑气氛带走，最后一圈还能听见自己的呼吸。', '配速稳定', 80, 0, 4, 0, 0, 0, 2, 2, 18, 640102, 1),
(64010102, 640101, 'strategy', '跟住前排，先冲出去', 0, 100, '前半程很有存在感，后半程每一步都像在还债。', '开局过猛', 55, 0, 2, 0, 1, 0, 5, 0, 12, 640102, 1),
(64010103, 640101, 'strategy', '全程保守慢跑', 0, 100, '你顺利跑完，但成绩边缘，后面还得补一点表现。', '勉强完成', 45, 0, 1, 0, 0, 0, 1, 0, 10, 640102, 1),
(64010201, 640102, 'strategy', '排队时做肩背热身', 1, 100, '肩背活动开之后，上杠时没有那么僵。', '准备充分', 75, 0, 3, 0, 0, 0, 1, 2, 16, 640103, 1),
(64010202, 640102, 'strategy', '请同学帮忙看动作节奏', 1, 100, '同学提醒你别乱摆，动作虽然不华丽，但有效。', '有人提醒', 70, 0, 2, 0, 2, 0, 1, 1, 15, 640103, 1),
(64010203, 640102, 'strategy', '一直盯着别人次数焦虑', 0, 100, '你看了太多高手，轮到自己时手心全是汗。', '心态飘了', 40, 0, 0, 0, 0, 0, 4, -1, 8, 640103, 1),
(64010301, 640103, 'strategy', '先拉伸再测，动作慢一点', 1, 100, '你没有急着硬压，最后成绩比预想好一点。', '稳住收尾', 80, 0, 3, 0, 0, 0, -1, 2, 18, NULL, 1),
(64010302, 640103, 'strategy', '找老师确认动作标准', 1, 100, '老师提醒你脚尖和膝盖位置，少走了一次弯路。', '标准清楚', 70, 0, 2, 0, 1, 0, 0, 1, 15, NULL, 1),
(64010303, 640103, 'strategy', '直接硬压一次看运气', 0, 100, '腿后侧当场发出抗议，成绩过了线，但人也安静了。', '侥幸过线', 45, 0, -1, 0, 0, 0, 2, -1, 8, NULL, 1);

INSERT INTO weekly_goal
(id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, status) VALUES
(6401, 'cp64_final_review_route', '最后路线稳住', '本周推进 2 次期末或体测相关探索奇遇，把复习和体测路线排清楚。', 'exploration_story_step', 2, 45, 'discipline', 2, 1),
(6402, 'cp64_physical_test_push', '体测阶段推进', '本周完成 2 个体测生存挑战阶段，在压力里把身体状态稳住。', 'dungeon_stage', 2, 45, 'health', 2, 1);

INSERT INTO achievement
(id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, status) VALUES
(6401, 'cp64_final_week_stable', '最后一周没乱套', '累计推进 3 次期末与体测周奇遇，证明你能把最后一周拆成可执行路线。', '📚', 'exploration_story_step', 3, '期末稳住员', 1),
(6402, 'cp64_physical_test_started', '体测不是玄学', '累计完成 3 个副本阶段，至少认真走完一次挑战流程。', '🏃', 'dungeon_stage', 3, '操场幸存者', 1);
```

- [ ] **Step 2: Run CP6.4 seed tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6FinalWeekTests test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 3: Commit seed and seed tests**

```powershell
git add src/main/resources/data-content-pack-6.sql src/test/java/cn/haut/survivor/service/ContentPack6FinalWeekTests.java
git commit -m "feat: add CP6.4 final week seeds"
```

## Task 6: Run Integration Tests And Update Docs

**Files:**
- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
- Modify: `docs/NEXT_AI_HANDOFF.md`
- Modify: `docs/AI_CONTINUATION_PROMPT.md`

- [ ] **Step 1: Run focused test suite**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6FinalWeekTests,WeeklyThemeServiceTests,WeeklyModifierServiceTests,DungeonServiceTests test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 2: Run full clean test**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
```

Record the exact test count in the final feedback. The count should be higher than CP6.3's `Tests run: 324`.

- [ ] **Step 3: HTTP smoke**

Start the app:

```powershell
.\mvnw.cmd spring-boot:run
```

In another PowerShell session, smoke these routes after logging in or using the existing test login flow:

```text
/dashboard
/map
/map/location/2/event
/map/location/5/event
/exploration
POST /exploration/5
/week/summary
/dungeons
/dungeons/6401
/dungeons/6401/start
/dungeons/6401/play
```

Expected for each player page:

```text
HTTP 200
No Whitelabel
Contains game-dock
```

If `/dungeons/6401/start` redirects to `/player/create` because no profile exists, create/login a demo player and retry. Do not report a 302 as success unless you explicitly state it was the no-profile guard.

- [ ] **Step 4: Update `PROJECT_COMPLETION_STATUS.md`**

Add a CP6.4 completion section near the CP6.3 section:

```markdown
### CP6.4 期末与体测周机制化（已完成）

- 第 4 周继续偏向健康事件，并新增图书馆/操场期末体测内容。
- `WeeklyThemeService` 新增第 4 周探索收益 hook：图书馆提供学业/技能/减压，操场提供健康/减压。
- `WeeklyModifierService` 第 4 周关键地点修正为图书馆（2）和操场（5），不再把生活服务点（8）当作操场。
- `DungeonServiceImpl` 对 `physical` 类型副本在第 4 周提供压力 -1 缓冲，并在结果文案里标注来源。
- Seed 新增事件 6 条、事件选项 18 条、传闻 4 条、探索奇遇链 2 条/6 阶段、体测副本 1 个/3 阶段/9 选项、周目标 2 个、成就 2 个，均使用 6400 ID 段。
- 测试新增/扩展 `ContentPack6FinalWeekTests`、`WeeklyThemeServiceTests`、`WeeklyModifierServiceTests`、`DungeonServiceTests`。
```

Also remove or update future-direction rows that still list CP6.4 as pending.

- [ ] **Step 5: Update `NEXT_AI_HANDOFF.md`**

Add:

```markdown
## CP6.4 已完成：期末与体测周机制化

- 第 4 周核心地点是图书馆（2）和操场（5）。注意：生活服务点/博闻楼营业厅是 8，不是操场。
- `WeeklyThemeService` 集中提供第 4 周探索属性收益、physical 副本压力缓冲和结果文案后缀。
- `data-content-pack-6.sql` 追加 6400 段 seed：
  - 事件 6401-6406，选项 640101-640603。
  - 传闻 6401-6404。
  - 探索奇遇链 6401-6406。
  - 副本 6401，阶段 640101-640103，选项 64010101-64010303。
  - 周目标 6401-6402，成就 6401-6402。
- 最近一次验证：写入实际 `.\mvnw.cmd clean test` 结果和 HTTP 冒烟结果。
```

Update the "next recommended work" section so CP6.4 is no longer listed as next. Recommended next items after CP6.4:

```markdown
1. CP6 浏览器视觉复核：重点看地图热点、体测副本详情页、移动端 Dock。
2. CP6.5 期末结局联动：让体测副本/期末奇遇影响 semester ending 评分。
3. CP6.6 NPC 与体测周联动：小马/柳如烟在第 4 周给更明确的辅助反馈。
```

- [ ] **Step 6: Update `AI_CONTINUATION_PROMPT.md`**

Remove CP6.4 from "当前最建议做的任务" if present. Add a concise current-state bullet:

```markdown
- CP6.4 已完成：第 4 周期末与体测周现在影响图书馆/操场探索、physical 副本压力和 6400 段期末体测内容。
```

- [ ] **Step 7: Commit docs**

```powershell
git add docs/PROJECT_COMPLETION_STATUS.md docs/NEXT_AI_HANDOFF.md docs/AI_CONTINUATION_PROMPT.md
git commit -m "docs: update handoff for CP6.4 final week"
```

## Final Verification Checklist

- [ ] `git status --short` shows no unintended files.
- [ ] `.\mvnw.cmd clean test` passes.
- [ ] HTTP smoke routes listed in Task 6 are checked.
- [ ] CP6.4 uses no schema changes.
- [ ] CP6.4 uses no template/CSS changes unless a real rendering bug is discovered.
- [ ] Location ID `5` is used for 操场; location ID `8` remains 生活服务点/博闻楼营业厅.
- [ ] Week 3 DDL pressure +1 behavior still passes.
- [ ] Week 1 opening-week org/NPC behavior still passes.

## Required Final Feedback Format

Return this structure after implementation:

```markdown
CP6.4 执行反馈

1. 修改文件
| 文件 | 类型 | 原因 |
|---|---|---|

2. 新增表/字段
无。

3. 新增 Seed 数据
| 类型 | 数量 | ID 范围 | 详情 |
|---|---:|---|---|

4. 新增/修改测试
| 测试类 | 测试方法 | 覆盖点 |
|---|---|---|

5. Clean Test 结果
命令：
.\mvnw.cmd clean test
结果：
Tests run: ?, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

6. HTTP 冒烟结果
| 页面 | HTTP | Whitelabel | game-dock | 备注 |
|---|---:|---|---|---|

7. 浏览器视觉检查
说明是否执行。若未执行，必须说明原因；若执行，列出 1366x768 和 375x812 结果。

8. Git 提交
| Commit Hash | Message |
|---|---|

9. 剩余风险
| 风险 | 说明 | 严重度 |
|---|---|---|
```
