# Full Game V1 Phase 1 Semester Skeleton Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 HAUT Survivor 从 4 周 Demo 学期骨架升级为 16 周单学期 V1 骨架，并保留后续多学期扩展空间。

**Architecture:** 新增 `SemesterCalendarService` 作为唯一学期长度和阶段判定入口；`PlayerServiceImpl`、`WeeklyThemeService`、周总结和页面文案只消费该服务，不再各自写死 4 周。Phase 1 只改骨架、主题映射和必要页面文案，不新增数据库表，不补新 seed，不做路线评分。

**Tech Stack:** Spring Boot 3.3.5, Java 17, MyBatis-Plus, Thymeleaf, JUnit 5, AssertJ, MockMvc.

---

## Scope Guard

本计划只实现 Full Game V1 的 Phase 1：学期骨架。

本计划不做：

- 不新增 `semester_stage`、`route_progress`、`course_schedule` 等表。
- 不重写 UI 2.0。
- 不新增 CP6.x seed。
- 不做路线评分、结局画像升级或 16 周完整内容补齐。
- 不删除现有 CP1-CP6 数据、测试或功能。

执行前先确认当前基线：

```powershell
git status --short
git log -3 --oneline
```

期望：工作区干净，最新提交包含 `docs: add full game V1 design`。

---

## File Map

### Create

- `src/main/java/cn/haut/survivor/service/SemesterCalendarService.java`
  - 统一提供学期总周数、是否结束、阶段映射、阶段文案、阶段事件偏向和核心地点。

- `src/test/java/cn/haut/survivor/service/SemesterCalendarServiceTests.java`
  - 覆盖 16 周阶段边界、结束判定、旧字段兼容 phase、阶段偏向。

### Modify

- `src/main/resources/application.yml`
  - 增加 `gameplay.semester-weeks: 16` 和 `gameplay.weekly-action-points: 4`。

- `src/main/java/cn/haut/survivor/service/PlayerService.java`
  - 更新 4 周 Demo 注释。

- `src/main/java/cn/haut/survivor/service/impl/PlayerServiceImpl.java`
  - 删除 `MAX_SEMESTER_WEEKS = 4`。
  - 注入 `SemesterCalendarService`。
  - `createProfile`、`advanceWeek`、`resetSemester` 使用日历服务和行动点配置。
  - `isSemesterOver`、`getWeekPhaseLabel` 统一走日历服务。

- `src/main/java/cn/haut/survivor/service/WeeklyThemeService.java`
  - 注入 `SemesterCalendarService`。
  - `getTheme` 改为 6 阶段主题，保留 `WeekTheme` record，调用方不用改。
  - 第 1-2 周开学加成，第 3 周组织招新峰值，第 12-14 周 DDL 压力，第 15-16 周期末/体测加成。

- `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`
  - 把第 3/4 周固定文案改成基于阶段 key 的判断。

- `src/main/resources/templates/dashboard/index.html`
  - 把 “4 周的大学生活” 改为 “这一学期”。

- `src/main/resources/templates/ending/index.html`
  - 把 “4 周的大学生活” 改为 “这一学期”。

- `src/test/java/cn/haut/survivor/service/PlayerServiceTests.java`
  - 改为 16 周结束语义。

- `src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java`
  - 改为 6 阶段主题与旧 hook 映射。

- `src/test/java/cn/haut/survivor/service/SemesterEndingServiceTests.java`
  - `advanceToSemesterEnd()` 推进 16 次。

- `src/test/java/cn/haut/survivor/controller/WeekSummaryControllerTests.java`
  - 最后一周从第 4 周改为第 16 周。

- `src/test/java/cn/haut/survivor/controller/SemesterEndingControllerTests.java`
  - `advanceToSemesterEnd()` 推进 16 次。

- `src/test/java/cn/haut/survivor/controller/DashboardControllerTests.java`
  - 增加 Dashboard 显示 `第 1 周` 和 `共 16 周` 的断言。

- `docs/PROJECT_COMPLETION_STATUS.md`
  - 记录 Full Game V1 Phase 1 完成情况。

- `docs/NEXT_AI_HANDOFF.md`
  - 更新当前方向：不再继续 CP6.x，小步扩展转为 V1 Phase 2/3/4。

- `docs/AI_CONTINUATION_PROMPT.md`
  - 更新交接提示：当前基线为 16 周单学期骨架。

---

## Task 1: Add Semester Calendar Service

**Files:**
- Create: `src/main/java/cn/haut/survivor/service/SemesterCalendarService.java`
- Create: `src/test/java/cn/haut/survivor/service/SemesterCalendarServiceTests.java`

- [ ] **Step 1: Write failing tests**

Create `src/test/java/cn/haut/survivor/service/SemesterCalendarServiceTests.java`:

```java
package cn.haut.survivor.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterCalendarServiceTests {

    private final SemesterCalendarService service = new SemesterCalendarService(16, 4);

    @Test
    void defaultCalendarUsesSixteenWeeks() {
        assertThat(service.semesterWeeks()).isEqualTo(16);
        assertThat(service.weeklyActionPoints()).isEqualTo(4);
        assertThat(service.isSemesterOver(16)).isFalse();
        assertThat(service.isSemesterOver(17)).isTrue();
        assertThat(service.isSemesterOver(null)).isFalse();
    }

    @Test
    void mapsWeeksToSixStages() {
        assertThat(service.stageForWeek(1).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(2).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(3).stageKey()).isEqualTo("rhythm");
        assertThat(service.stageForWeek(5).stageKey()).isEqualTo("rhythm");
        assertThat(service.stageForWeek(6).stageKey()).isEqualTo("midterm");
        assertThat(service.stageForWeek(8).stageKey()).isEqualTo("midterm");
        assertThat(service.stageForWeek(9).stageKey()).isEqualTo("route");
        assertThat(service.stageForWeek(11).stageKey()).isEqualTo("route");
        assertThat(service.stageForWeek(12).stageKey()).isEqualTo("project");
        assertThat(service.stageForWeek(14).stageKey()).isEqualTo("project");
        assertThat(service.stageForWeek(15).stageKey()).isEqualTo("final");
        assertThat(service.stageForWeek(16).stageKey()).isEqualTo("final");
    }

    @Test
    void stageForOutOfRangeWeekClampsToUsefulStage() {
        assertThat(service.stageForWeek(0).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(null).stageKey()).isEqualTo("opening");
        assertThat(service.stageForWeek(99).stageKey()).isEqualTo("final");
    }

    @Test
    void legacySemesterPhaseIsKeptCompatible() {
        assertThat(service.legacySemesterPhaseForWeek(1)).isEqualTo("early");
        assertThat(service.legacySemesterPhaseForWeek(5)).isEqualTo("early");
        assertThat(service.legacySemesterPhaseForWeek(6)).isEqualTo("mid");
        assertThat(service.legacySemesterPhaseForWeek(11)).isEqualTo("mid");
        assertThat(service.legacySemesterPhaseForWeek(12)).isEqualTo("final");
        assertThat(service.legacySemesterPhaseForWeek(16)).isEqualTo("final");
        assertThat(service.legacySemesterPhaseForWeek(17)).isEqualTo("final");
    }

    @Test
    void weekPhaseLabelIncludesTotalWeeksAndStageName() {
        assertThat(service.weekPhaseLabel(1)).isEqualTo("第 1 周 · 开学适应（共 16 周）");
        assertThat(service.weekPhaseLabel(6)).isEqualTo("第 6 周 · 期中波动（共 16 周）");
        assertThat(service.weekPhaseLabel(15)).isEqualTo("第 15 周 · 期末与体测（共 16 周）");
        assertThat(service.weekPhaseLabel(17)).isEqualTo("学期结束");
    }

    @Test
    void preferredEventTypeFollowsSemesterStage() {
        assertThat(service.preferredEventType(1)).isEqualTo("生活");
        assertThat(service.preferredEventType(3)).isEqualTo("社交");
        assertThat(service.preferredEventType(6)).isEqualTo("学习");
        assertThat(service.preferredEventType(9)).isEqualTo("技能");
        assertThat(service.preferredEventType(12)).isEqualTo("学习");
        assertThat(service.preferredEventType(15)).isEqualTo("健康");
    }

    @Test
    void primaryLocationIdsFollowSemesterStage() {
        assertThat(service.stageForWeek(1).primaryLocationIds()).containsExactly(3L, 4L, 7L);
        assertThat(service.stageForWeek(6).primaryLocationIds()).containsExactly(1L, 2L, 6L);
        assertThat(service.stageForWeek(15).primaryLocationIds()).containsExactly(2L, 5L);
    }
}
```

- [ ] **Step 2: Run tests and verify they fail**

Run:

```powershell
.\mvnw.cmd -Dtest=SemesterCalendarServiceTests test
```

Expected: compilation failure because `SemesterCalendarService` does not exist.

- [ ] **Step 3: Implement calendar service**

Create `src/main/java/cn/haut/survivor/service/SemesterCalendarService.java`:

```java
package cn.haut.survivor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemesterCalendarService {

    public static final int DEFAULT_SEMESTER_WEEKS = 16;
    public static final int DEFAULT_WEEKLY_ACTION_POINTS = 4;

    public record SemesterStage(
            String stageKey,
            int weekStart,
            int weekEnd,
            String name,
            String description,
            String hint,
            String icon,
            String preferredEventType,
            List<Long> primaryLocationIds
    ) {
    }

    private final int semesterWeeks;
    private final int weeklyActionPoints;

    private static final List<SemesterStage> STAGES = List.of(
            new SemesterStage("opening", 1, 2, "开学适应",
                    "刚开学，一切还没失控。", "趁节奏慢，多探索校园、了解组织。", "🎒", "生活",
                    List.of(3L, 4L, 7L)),
            new SemesterStage("rhythm", 3, 5, "节奏建立",
                    "课程和社团开始进入稳定节奏。", "适合建立周目标、认识组织和推进校园奇遇。", "🎉", "社交",
                    List.of(4L, 7L, 8L)),
            new SemesterStage("midterm", 6, 8, "期中波动",
                    "复习、实验和阶段作业开始制造压力。", "学业和技能事件更重要，注意别让压力失控。", "📚", "学习",
                    List.of(1L, 2L, 6L)),
            new SemesterStage("route", 9, 11, "路线分化",
                    "你的成长路线开始拉开差异。", "围绕成长路线选择行动，关系和组织会影响后续走向。", "🧭", "技能",
                    List.of(2L, 6L, 7L)),
            new SemesterStage("project", 12, 14, "项目与 DDL",
                    "课设、小组作业和副本挑战集中出现。", "副本和技能判定更关键，别忘了压力管理。", "⏰", "学习",
                    List.of(1L, 2L, 6L)),
            new SemesterStage("final", 15, 16, "期末与体测",
                    "复习、体测、结算都来了。", "图书馆和操场是关键，坚持就是胜利。", "🏁", "健康",
                    List.of(2L, 5L))
    );

    public SemesterCalendarService(
            @Value("${gameplay.semester-weeks:16}") int semesterWeeks,
            @Value("${gameplay.weekly-action-points:4}") int weeklyActionPoints
    ) {
        this.semesterWeeks = semesterWeeks > 0 ? semesterWeeks : DEFAULT_SEMESTER_WEEKS;
        this.weeklyActionPoints = weeklyActionPoints > 0 ? weeklyActionPoints : DEFAULT_WEEKLY_ACTION_POINTS;
    }

    public int semesterWeeks() {
        return semesterWeeks;
    }

    public int weeklyActionPoints() {
        return weeklyActionPoints;
    }

    public boolean isSemesterOver(Integer currentWeek) {
        return currentWeek != null && currentWeek > semesterWeeks;
    }

    public SemesterStage stageForWeek(Integer currentWeek) {
        int week = currentWeek == null ? 1 : currentWeek;
        if (week <= 1) {
            return STAGES.get(0);
        }
        if (week >= semesterWeeks) {
            return STAGES.get(STAGES.size() - 1);
        }
        return STAGES.stream()
                .filter(stage -> week >= stage.weekStart() && week <= stage.weekEnd())
                .findFirst()
                .orElse(STAGES.get(STAGES.size() - 1));
    }

    public String legacySemesterPhaseForWeek(Integer currentWeek) {
        int week = currentWeek == null ? 1 : currentWeek;
        if (week <= 5) {
            return "early";
        }
        if (week <= 11) {
            return "mid";
        }
        return "final";
    }

    public String weekPhaseLabel(Integer currentWeek) {
        if (isSemesterOver(currentWeek)) {
            return "学期结束";
        }
        int week = currentWeek == null ? 1 : currentWeek;
        SemesterStage stage = stageForWeek(week);
        return "第 " + week + " 周 · " + stage.name() + "（共 " + semesterWeeks + " 周）";
    }

    public String preferredEventType(Integer currentWeek) {
        return stageForWeek(currentWeek).preferredEventType();
    }

    public List<SemesterStage> allStages() {
        return STAGES;
    }
}
```

- [ ] **Step 4: Run calendar tests**

Run:

```powershell
.\mvnw.cmd -Dtest=SemesterCalendarServiceTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/SemesterCalendarService.java src/test/java/cn/haut/survivor/service/SemesterCalendarServiceTests.java
git commit -m "feat: add semester calendar service"
```

---

## Task 2: Wire Player Service To 16-Week Calendar

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/cn/haut/survivor/service/PlayerService.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/PlayerServiceImpl.java`
- Modify: `src/test/java/cn/haut/survivor/service/PlayerServiceTests.java`

- [ ] **Step 1: Update PlayerService tests first**

Modify these test methods in `src/test/java/cn/haut/survivor/service/PlayerServiceTests.java`:

```java
@Test
void advanceWeekUpdatesSemesterPhase() {
    for (int i = 0; i < 5; i++) {
        playerService.advanceWeek(2L);
    }

    PlayerProfile profile = playerService.findProfileByUserId(2L);
    assertThat(profile.getCurrentWeek()).isEqualTo(6);
    assertThat(profile.getSemesterPhase()).isEqualTo("mid");
}

@Test
void advanceToFinalPhase() {
    for (int i = 0; i < 11; i++) {
        playerService.advanceWeek(2L);
    }

    PlayerProfile profile = playerService.findProfileByUserId(2L);
    assertThat(profile.getCurrentWeek()).isEqualTo(12);
    assertThat(profile.getSemesterPhase()).isEqualTo("final");
}

@Test
void semesterEndsAfterSixteenWeeks() {
    for (int i = 0; i < 15; i++) {
        playerService.advanceWeek(2L);
    }
    assertThat(playerService.findProfileByUserId(2L).getCurrentWeek()).isEqualTo(16);
    assertThat(playerService.isSemesterOver(2L)).isFalse();

    playerService.advanceWeek(2L);

    assertThat(playerService.findProfileByUserId(2L).getCurrentWeek()).isEqualTo(17);
    assertThat(playerService.isSemesterOver(2L)).isTrue();
    assertThat(playerService.getWeekPhaseLabel(playerService.findProfileByUserId(2L))).contains("学期结束");
}

@Test
void cannotAdvanceAfterSemesterEnds() {
    advanceToSemesterEnd();
    assertThatThrownBy(() -> playerService.advanceWeek(2L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("学期已结束");
}

@Test
void cannotActAfterSemesterEnds() {
    advanceToSemesterEnd();
    assertThatThrownBy(() -> playerService.consumeActionPoint(2L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("学期已结束");
}

@Test
void weekPhaseLabelShowsCorrectPhase() {
    PlayerProfile profile = playerService.findProfileByUserId(2L);
    assertThat(playerService.getWeekPhaseLabel(profile))
            .contains("第 1 周")
            .contains("开学适应")
            .contains("共 16 周");
}

private void advanceToSemesterEnd() {
    for (int i = 0; i < 16; i++) {
        playerService.advanceWeek(2L);
    }
}
```

Then replace repeated 4-week ending loops in reset tests:

```java
advanceToSemesterEnd();
```

Specifically update:

- `resetSemesterPreservesHistoryEndings`
- `resetSemesterClearsDungeonRecords`

For `resetSemesterResetsAttributes`, do not require semester end; keep the test focused on reset behavior and replace its three manual `advanceWeek` calls with:

```java
playerService.advanceWeek(2L);
```

- [ ] **Step 2: Run PlayerService tests and verify failure**

Run:

```powershell
.\mvnw.cmd -Dtest=PlayerServiceTests test
```

Expected: FAIL because implementation still ends after 4 weeks and labels say `共 4 周`.

- [ ] **Step 3: Add gameplay config**

Modify `src/main/resources/application.yml` and append:

```yaml
gameplay:
  semester-weeks: 16
  weekly-action-points: 4
```

Keep existing Spring, MyBatis and server config unchanged.

- [ ] **Step 4: Wire `PlayerServiceImpl` to `SemesterCalendarService`**

Modify imports in `PlayerServiceImpl`:

```java
import cn.haut.survivor.service.SemesterCalendarService;
```

Delete:

```java
/** Demo 版学期总周数 */
private static final int MAX_SEMESTER_WEEKS = 4;
```

Add field:

```java
private final SemesterCalendarService semesterCalendarService;
```

Update constructor signature and assignment:

```java
public PlayerServiceImpl(PlayerProfileMapper playerProfileMapper,
                         PlayerAttributeMapper playerAttributeMapper,
                         UserLocationExplorationMapper explorationMapper,
                         UserOrganizationMapper userOrganizationMapper,
                         UserDungeonRecordMapper userDungeonRecordMapper,
                         UserDungeonTaskRecordMapper userDungeonTaskRecordMapper,
                         WeeklyGoalService weeklyGoalService,
                         SemesterCalendarService semesterCalendarService) {
    this.playerProfileMapper = playerProfileMapper;
    this.playerAttributeMapper = playerAttributeMapper;
    this.explorationMapper = explorationMapper;
    this.userOrganizationMapper = userOrganizationMapper;
    this.userDungeonRecordMapper = userDungeonRecordMapper;
    this.userDungeonTaskRecordMapper = userDungeonTaskRecordMapper;
    this.weeklyGoalService = weeklyGoalService;
    this.semesterCalendarService = semesterCalendarService;
}
```

In `createProfile`, replace:

```java
profile.setActionPoints(4);
profile.setMaxActionPoints(4);
profile.setSemesterPhase("early");
```

with:

```java
profile.setActionPoints(semesterCalendarService.weeklyActionPoints());
profile.setMaxActionPoints(semesterCalendarService.weeklyActionPoints());
profile.setSemesterPhase(semesterCalendarService.legacySemesterPhaseForWeek(1));
```

In `advanceWeek`, replace the manual phase block:

```java
// 更新学期阶段
if (nextWeek <= 2) {
    profile.setSemesterPhase("early");
} else if (nextWeek <= 3) {
    profile.setSemesterPhase("mid");
} else {
    profile.setSemesterPhase("final");
}
```

with:

```java
profile.setSemesterPhase(semesterCalendarService.legacySemesterPhaseForWeek(nextWeek));
```

Replace `isSemesterOver`:

```java
@Override
public boolean isSemesterOver(Long userId) {
    PlayerProfile profile = findProfileByUserId(userId);
    return profile != null && semesterCalendarService.isSemesterOver(profile.getCurrentWeek());
}
```

Replace `getWeekPhaseLabel`:

```java
@Override
public String getWeekPhaseLabel(PlayerProfile profile) {
    if (profile == null) {
        return "";
    }
    return semesterCalendarService.weekPhaseLabel(profile.getCurrentWeek());
}
```

In `resetSemester`, replace:

```java
profile.setActionPoints(4);
profile.setMaxActionPoints(4);
profile.setSemesterPhase("early");
```

with:

```java
profile.setActionPoints(semesterCalendarService.weeklyActionPoints());
profile.setMaxActionPoints(semesterCalendarService.weeklyActionPoints());
profile.setSemesterPhase(semesterCalendarService.legacySemesterPhaseForWeek(1));
```

- [ ] **Step 5: Update PlayerService comment**

Modify `src/main/java/cn/haut/survivor/service/PlayerService.java`:

```java
/** 检查学期是否结束。 */
boolean isSemesterOver(Long userId);
```

- [ ] **Step 6: Run PlayerService tests**

Run:

```powershell
.\mvnw.cmd -Dtest=PlayerServiceTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit**

```powershell
git add src/main/resources/application.yml src/main/java/cn/haut/survivor/service/PlayerService.java src/main/java/cn/haut/survivor/service/impl/PlayerServiceImpl.java src/test/java/cn/haut/survivor/service/PlayerServiceTests.java
git commit -m "feat: wire player progression to semester calendar"
```

---

## Task 3: Convert Weekly Theme Hooks To 16-Week Stages

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/WeeklyThemeService.java`
- Modify: `src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`

- [ ] **Step 1: Replace WeeklyThemeService tests**

Replace `src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java` with:

```java
package cn.haut.survivor.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyThemeServiceTests {

    private final SemesterCalendarService calendarService = new SemesterCalendarService(16, 4);
    private final WeeklyThemeService service = new WeeklyThemeService(calendarService);

    @Test
    void weekThemeFollowsSixteenWeekSemesterStages() {
        assertThat(service.getTheme(1).name()).isEqualTo("开学适应");
        assertThat(service.getTheme(2).name()).isEqualTo("开学适应");
        assertThat(service.getTheme(3).name()).isEqualTo("节奏建立");
        assertThat(service.getTheme(6).name()).isEqualTo("期中波动");
        assertThat(service.getTheme(9).name()).isEqualTo("路线分化");
        assertThat(service.getTheme(12).name()).isEqualTo("项目与 DDL");
        assertThat(service.getTheme(15).name()).isEqualTo("期末与体测");
        assertThat(service.getTheme(99).name()).isEqualTo("期末与体测");
    }

    @Test
    void allThemesReturnsSixStages() {
        assertThat(service.allThemes()).hasSize(6);
        assertThat(service.allThemes()).extracting(WeeklyThemeService.WeekTheme::name)
                .containsExactly("开学适应", "节奏建立", "期中波动", "路线分化", "项目与 DDL", "期末与体测");
    }

    @Test
    void weeklyGameplayHooksAreMappedToV1Stages() {
        assertThat(service.preferredEventType(1)).isEqualTo("生活");
        assertThat(service.preferredEventType(3)).isEqualTo("社交");
        assertThat(service.preferredEventType(6)).isEqualTo("学习");
        assertThat(service.preferredEventType(9)).isEqualTo("技能");
        assertThat(service.preferredEventType(12)).isEqualTo("学习");
        assertThat(service.preferredEventType(15)).isEqualTo("健康");
        assertThat(service.preferredEventType(99)).isEqualTo("健康");

        assertThat(service.organizationActivityBonus(2)).isZero();
        assertThat(service.organizationActivityBonus(3)).isEqualTo(1);
        assertThat(service.organizationActivityBonus(5)).isEqualTo(1);
        assertThat(service.organizationActivityBonus(6)).isZero();

        assertThat(service.organizationJoinSocialRequirementReduction(1)).isEqualTo(5);
        assertThat(service.organizationJoinSocialRequirementReduction(2)).isEqualTo(5);
        assertThat(service.organizationJoinSocialRequirementReduction(3)).isZero();
        assertThat(service.organizationJoinSocialRequirementReduction(null)).isEqualTo(5);

        assertThat(service.npcOpeningWeekFamiliarityBonus(1, false)).isEqualTo(1);
        assertThat(service.npcOpeningWeekFamiliarityBonus(2, false)).isEqualTo(1);
        assertThat(service.npcOpeningWeekFamiliarityBonus(2, true)).isZero();
        assertThat(service.npcOpeningWeekFamiliarityBonus(3, false)).isZero();
        assertThat(service.openingWeekNpcInteractionSuffix(2, false)).contains("开学适应");
        assertThat(service.openingWeekNpcInteractionSuffix(2, true)).isBlank();

        assertThat(service.dungeonPressureBonus(11)).isZero();
        assertThat(service.dungeonPressureBonus(12)).isEqualTo(1);
        assertThat(service.dungeonPressureBonus(14)).isEqualTo(1);
        assertThat(service.dungeonPressureBonus(15)).isZero();

        assertThat(service.finalWeekExplorationAttributeChange(15, 2L).academicChange()).isEqualTo(1);
        assertThat(service.finalWeekExplorationAttributeChange(16, 2L).skillChange()).isEqualTo(1);
        assertThat(service.finalWeekExplorationAttributeChange(15, 5L).healthChange()).isEqualTo(2);
        assertThat(service.finalWeekExplorationAttributeChange(16, 5L).pressureChange()).isEqualTo(-1);
        assertThat(service.finalWeekExplorationAttributeChange(14, 2L).hasAnyChange()).isFalse();
        assertThat(service.finalWeekExplorationAttributeChange(15, 8L).hasAnyChange()).isFalse();

        assertThat(service.finalWeekDungeonPressureRelief(15, "physical")).isEqualTo(-1);
        assertThat(service.finalWeekDungeonPressureRelief(16, "physical")).isEqualTo(-1);
        assertThat(service.finalWeekDungeonPressureRelief(15, "academic")).isZero();
        assertThat(service.finalWeekDungeonPressureRelief(14, "physical")).isZero();
        assertThat(service.finalWeekDungeonResultSuffix(15, "physical")).contains("期末与体测");
        assertThat(service.finalWeekDungeonResultSuffix(14, "physical")).isBlank();
    }
}
```

- [ ] **Step 2: Run WeeklyTheme tests and verify failure**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyThemeServiceTests test
```

Expected: FAIL because service still exposes 4 themes and 4-week hook semantics.

- [ ] **Step 3: Update WeeklyThemeService implementation**

Modify `src/main/java/cn/haut/survivor/service/WeeklyThemeService.java`.

Replace class comment:

```java
/**
 * 周主题系统：消费 16 周学期阶段，不再在各业务服务中散落周次判断。
 */
```

Delete the static `THEMES` list.

Add field and constructor:

```java
private final SemesterCalendarService semesterCalendarService;

public WeeklyThemeService(SemesterCalendarService semesterCalendarService) {
    this.semesterCalendarService = semesterCalendarService;
}
```

Replace `getTheme`:

```java
public WeekTheme getTheme(int currentWeek) {
    SemesterCalendarService.SemesterStage stage = semesterCalendarService.stageForWeek(currentWeek);
    return new WeekTheme(currentWeek, stage.name(), stage.description(), stage.hint(), stage.icon());
}
```

Replace `preferredEventType`:

```java
public String preferredEventType(Integer currentWeek) {
    return semesterCalendarService.preferredEventType(currentWeek);
}
```

Replace organization and NPC hooks:

```java
public int organizationActivityBonus(Integer currentWeek) {
    String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
    return "rhythm".equals(stageKey) ? 1 : 0;
}

public int organizationJoinSocialRequirementReduction(Integer currentWeek) {
    String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
    return "opening".equals(stageKey) ? 5 : 0;
}

public int npcOpeningWeekFamiliarityBonus(Integer currentWeek, boolean weeklyBuddy) {
    if (weeklyBuddy) {
        return 0;
    }
    String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
    return "opening".equals(stageKey) ? 1 : 0;
}

public String openingWeekNpcInteractionSuffix(Integer currentWeek, boolean weeklyBuddy) {
    return npcOpeningWeekFamiliarityBonus(currentWeek, weeklyBuddy) > 0
            ? " 开学适应阶段：新学期大家都在重新认识彼此，本次熟悉度额外 +1。"
            : "";
}
```

Replace dungeon and final hooks:

```java
public int dungeonPressureBonus(Integer currentWeek) {
    String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
    return "project".equals(stageKey) ? 1 : 0;
}

public AttributeChange finalWeekExplorationAttributeChange(Integer currentWeek, Long locationId) {
    String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
    if (!"final".equals(stageKey) || locationId == null) {
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

public int finalWeekDungeonPressureRelief(Integer currentWeek, String dungeonType) {
    String stageKey = semesterCalendarService.stageForWeek(currentWeek).stageKey();
    if (!"final".equals(stageKey)) {
        return 0;
    }
    return "physical".equalsIgnoreCase(dungeonType) ? -1 : 0;
}

public String finalWeekDungeonResultSuffix(Integer currentWeek, String dungeonType) {
    return finalWeekDungeonPressureRelief(currentWeek, dungeonType) < 0
            ? " 期末与体测阶段：你提前适应了节奏，本阶段压力额外 -1。"
            : "";
}
```

Replace `allThemes`:

```java
public List<WeekTheme> allThemes() {
    return semesterCalendarService.allStages().stream()
            .map(stage -> new WeekTheme(stage.weekStart(), stage.name(), stage.description(), stage.hint(), stage.icon()))
            .toList();
}
```

- [ ] **Step 4: Update WeekSummaryServiceImpl phase-specific text**

In `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`, add constructor field:

```java
private final SemesterCalendarService semesterCalendarService;
```

Update imports:

```java
import cn.haut.survivor.service.SemesterCalendarService;
```

Update constructor parameter list and assignment:

```java
SemesterCalendarService semesterCalendarService,
InfluenceLogService influenceLogService
```

```java
this.semesterCalendarService = semesterCalendarService;
```

Update the constructor call order carefully: keep all existing parameters and append or insert the new service in a consistent place. Spring will autowire by type.

Inside `generateSummaryText`, before the week-specific checks, add:

```java
String stageKey = semesterCalendarService.stageForWeek(weekNumber).stageKey();
```

Replace:

```java
if (weekNumber == 3 && highPressure && (highAcademic || highSkill)) {
```

with:

```java
if ("project".equals(stageKey) && highPressure && (highAcademic || highSkill)) {
```

Replace the next two `weekNumber == 3` checks with `"project".equals(stageKey)`.

Replace:

```java
if (weekNumber == 4 && (highAcademic || highSkill) && pressure < 60) {
```

with:

```java
if ("final".equals(stageKey) && (highAcademic || highSkill) && pressure < 60) {
```

Replace:

```java
if (weekNumber == 4 && pressure >= 70) {
```

with:

```java
if ("final".equals(stageKey) && pressure >= 70) {
```

- [ ] **Step 5: Run targeted service tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyThemeServiceTests,WeeklyModifierServiceTests,DungeonServiceTests,NpcServiceTests,OrganizationServiceTests test
```

Expected: `BUILD SUCCESS`. If existing tests still assert literal “第 4 周” behavior, update them to assert final-stage behavior on week 15 or 16.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/WeeklyThemeService.java src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java
git commit -m "feat: map weekly themes to semester stages"
```

---

## Task 4: Update Ending And Controller Tests For 16 Weeks

**Files:**
- Modify: `src/test/java/cn/haut/survivor/service/SemesterEndingServiceTests.java`
- Modify: `src/test/java/cn/haut/survivor/controller/WeekSummaryControllerTests.java`
- Modify: `src/test/java/cn/haut/survivor/controller/SemesterEndingControllerTests.java`
- Modify: `src/test/java/cn/haut/survivor/controller/DashboardControllerTests.java`

- [ ] **Step 1: Update SemesterEndingServiceTests helper**

In `src/test/java/cn/haut/survivor/service/SemesterEndingServiceTests.java`, replace:

```java
private void advanceToSemesterEnd() {
    playerService.advanceWeek(2L); // week 2
    playerService.advanceWeek(2L); // week 3
    playerService.advanceWeek(2L); // week 4
    playerService.advanceWeek(2L); // week 5 → over
}
```

with:

```java
private void advanceToSemesterEnd() {
    for (int i = 0; i < 16; i++) {
        playerService.advanceWeek(2L);
    }
}
```

- [ ] **Step 2: Update WeekSummaryControllerTests**

In `advanceWeekOnLastWeekRedirectsToEnding`, replace:

```java
// Advance to week 4 (last week)
for (int i = 0; i < 3; i++) {
    playerService.advanceWeek(2L);
}

// Now at week 4, advance should go to ending
```

with:

```java
// Advance to week 16 (last week)
for (int i = 0; i < 15; i++) {
    playerService.advanceWeek(2L);
}

// Now at week 16, advance should go to ending
```

In `semesterOverRedirectsToEnding`, replace:

```java
// Advance past week 4
for (int i = 0; i < 4; i++) {
    playerService.advanceWeek(2L);
}
```

with:

```java
// Advance past week 16
for (int i = 0; i < 16; i++) {
    playerService.advanceWeek(2L);
}
```

- [ ] **Step 3: Update SemesterEndingControllerTests helper**

In `src/test/java/cn/haut/survivor/controller/SemesterEndingControllerTests.java`, replace:

```java
private void advanceToSemesterEnd() {
    playerService.advanceWeek(2L);
    playerService.advanceWeek(2L);
    playerService.advanceWeek(2L);
    playerService.advanceWeek(2L);
}
```

with:

```java
private void advanceToSemesterEnd() {
    for (int i = 0; i < 16; i++) {
        playerService.advanceWeek(2L);
    }
}
```

- [ ] **Step 4: Strengthen DashboardControllerTests**

In `userWithProfileCanOpenDashboard`, append content assertions:

```java
.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
        .string(org.hamcrest.Matchers.containsString("第 1 周")))
.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
        .string(org.hamcrest.Matchers.containsString("共 16 周")));
```

If imports become noisy, add static imports:

```java
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
```

and use:

```java
.andExpect(content().string(org.hamcrest.Matchers.containsString("第 1 周")))
.andExpect(content().string(org.hamcrest.Matchers.containsString("共 16 周")));
```

- [ ] **Step 5: Run controller and ending tests**

Run:

```powershell
.\mvnw.cmd -Dtest=SemesterEndingServiceTests,WeekSummaryControllerTests,SemesterEndingControllerTests,DashboardControllerTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```powershell
git add src/test/java/cn/haut/survivor/service/SemesterEndingServiceTests.java src/test/java/cn/haut/survivor/controller/WeekSummaryControllerTests.java src/test/java/cn/haut/survivor/controller/SemesterEndingControllerTests.java src/test/java/cn/haut/survivor/controller/DashboardControllerTests.java
git commit -m "test: update semester flow tests for sixteen weeks"
```

---

## Task 5: Update User-Facing Copy

**Files:**
- Modify: `src/main/resources/templates/dashboard/index.html`
- Modify: `src/main/resources/templates/ending/index.html`

- [ ] **Step 1: Update dashboard semester-end copy**

In `src/main/resources/templates/dashboard/index.html`, replace:

```html
<p class="dorm-semester-end__desc">4 周的大学生活已经过去，来看看你的学期回忆吧。</p>
```

with:

```html
<p class="dorm-semester-end__desc">这一学期已经过去，来看看你的学期回忆吧。</p>
```

- [ ] **Step 2: Update ending copy**

In `src/main/resources/templates/ending/index.html`, replace:

```html
<p>你已经度过了 4 周的大学生活。现在来看看你的路线画像吧！</p>
```

with:

```html
<p>你已经度过了这一学期。现在来看看你的路线画像吧！</p>
```

- [ ] **Step 3: Search for remaining source-level 4-week copy**

Run:

```powershell
rg -n "4 周|4周|共 4|Demo 版 4 周|MAX_SEMESTER_WEEKS|semesterEndsAfterFourWeeks" src/main/java src/main/resources/templates src/test/java -g "*.java" -g "*.html"
```

Expected: no matches in source, templates, or tests. Matches in historical docs are acceptable and should not be edited in this task.

- [ ] **Step 4: Run template-related tests**

Run:

```powershell
.\mvnw.cmd -Dtest=DashboardControllerTests,SemesterEndingControllerTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```powershell
git add src/main/resources/templates/dashboard/index.html src/main/resources/templates/ending/index.html
git commit -m "fix: update semester copy for full game v1"
```

---

## Task 6: Full Verification And HTTP Smoke

**Files:**
- No source files unless verification exposes a bug.

- [ ] **Step 1: Run full clean test**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
Failures: 0
Errors: 0
```

Record the final test count from Maven output.

- [ ] **Step 2: Start local app**

If no server is already running on port 8080, run:

```powershell
.\mvnw.cmd spring-boot:run
```

If port 8080 is occupied by this app, reuse it. If occupied by another app, start with another port:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

- [ ] **Step 3: HTTP smoke**

Smoke these endpoints with a logged-in test session or the existing project smoke helper pattern:

```text
GET  /dashboard
GET  /map
GET  /exploration
POST /exploration/4
GET  /week/summary
GET  /week/history
GET  /ending
GET  /dungeons
GET  /organizations
GET  /npcs/6101
```

Expected for each rendered page:

- HTTP 200, except redirects expected only when intentionally navigating without session.
- No `Whitelabel Error Page`.
- Page contains `game-dock` for player pages.
- Dashboard contains `共 16 周`.
- Before week 17, `/ending` shows “学期尚未结束”.

- [ ] **Step 4: Advance smoke account to week 16 and week 17**

Using the same session:

1. Create or reuse a test player.
2. POST `/week/advance` until current week is 16.
3. GET `/dashboard`; expected: 200, text contains `第 16 周` and `期末与体测`.
4. POST `/week/advance` once more.
5. GET `/week/summary`; expected: redirect to `/ending`.
6. GET `/ending`; expected: 200, settle page visible, no Whitelabel.

- [ ] **Step 5: Browser visual check if templates changed**

Because Task 5 changes templates, use Browser or Playwright to check:

```text
1366x768:
- /dashboard
- /week/summary
- /ending

375x812:
- /dashboard
- /week/summary
- /ending
```

Expected:

- Mobile Dock does not cover primary actions.
- No horizontal scrolling.
- `第 X/16 周` text fits.
- Ending copy no longer says `4 周`.

- [ ] **Step 6: Commit verification-only doc note if needed**

If no code changed during verification, do not create an empty commit.

---

## Task 7: Update Project Docs And Handoff

**Files:**
- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
- Modify: `docs/NEXT_AI_HANDOFF.md`
- Modify: `docs/AI_CONTINUATION_PROMPT.md`

- [ ] **Step 1: Update PROJECT_COMPLETION_STATUS**

Add a new section near the latest status area:

```markdown
## Full Game V1 Phase 1：16 周学期骨架

- 项目方向从 CP6.x 小内容包转为 Full Game V1。
- 学期长度由 4 周 Demo 升级为 16 周单学期。
- 新增 `SemesterCalendarService`，统一管理学期总周数、阶段映射、阶段文案、事件偏向和核心地点。
- `PlayerServiceImpl` 不再持有 `MAX_SEMESTER_WEEKS = 4`，学期结束判断改为第 17 周开始。
- `WeeklyThemeService` 改为消费 16 周阶段：开学适应、节奏建立、期中波动、路线分化、项目与 DDL、期末与体测。
- 第 1-2 周保留开学适应加成；第 12-14 周保留 DDL 副本压力；第 15-16 周保留期末/体测探索与 physical 副本压力缓冲。
- Dashboard 和 Ending 不再显示 “4 周大学生活”。
- 最近验证：`.\mvnw.cmd clean test`，Tests run 数量使用 Task 6 记录的 Maven 输出，Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
```

- [ ] **Step 2: Update NEXT_AI_HANDOFF**

Update top status and next suggested work:

```markdown
当前方向：
- 不再继续 CP6.x 小内容包。
- 已进入 Full Game V1。
- Phase 1 已完成：16 周单学期骨架与阶段日历。

下一步建议：
1. Full Game V1 Phase 2：周主题升级与阶段反馈深化。
2. Full Game V1 Phase 3：路线目标与阶段目标。
3. Full Game V1 Phase 4：结局评分升级。

注意：
- 不要把 Phase 2/3/4 一次性混做。
- 不要新增大表，除非能证明 Java 层聚合无法满足需求。
- 每次修改后运行 `.\mvnw.cmd clean test`。
```

Remove or demote old “CP6.6 NPC 与体测周联动” as next-step recommendation. It can stay in historical notes, but must not appear as primary next task.

- [ ] **Step 3: Update AI_CONTINUATION_PROMPT**

Update current status:

```markdown
当前状态：
- 项目已从 4 周 Demo 小内容包阶段转向 Full Game V1。
- Full Game V1 Phase 1 已完成：16 周单学期骨架、统一学期日历服务、阶段化周主题映射。
- 当前主循环为：创建角色 -> 16 周探索/事件/组织/副本/NPC/周总结 -> 第 17 周学期结局 -> 重开新学期。

当前最建议做的任务：
- Full Game V1 Phase 2：周主题升级与阶段反馈深化。
- 不要继续按 CP6.x 命名新增零散内容包。
```

Update the latest test count and smoke results using Task 6 output.

- [ ] **Step 4: Run docs/source search**

Run:

```powershell
rg -n "当前最建议做的任务|CP6\\.6|4 周 Demo|Tests run:" docs/PROJECT_COMPLETION_STATUS.md docs/NEXT_AI_HANDOFF.md docs/AI_CONTINUATION_PROMPT.md
```

Expected:

- `CP6.6` is not listed as the primary next task.
- `4 周 Demo` only appears as historical context, not current status.
- Latest test count appears in all active handoff docs.

- [ ] **Step 5: Commit docs**

```powershell
git add docs/PROJECT_COMPLETION_STATUS.md docs/NEXT_AI_HANDOFF.md docs/AI_CONTINUATION_PROMPT.md
git commit -m "docs: update handoff for full game v1 phase 1"
```

---

## Final Verification Report Required

The implementing AI must report:

1. 修改文件列表。
2. 是否新增表/字段：本计划期望为“无”。
3. 是否新增 seed：本计划期望为“无”。
4. 学期周数行为：
   - 第 16 周是否仍未结束。
   - 第 17 周是否进入学期结束。
   - 重开学期是否回到第 1 周且 `semester_number + 1`。
5. 周主题映射：
   - 1-2 opening。
   - 3-5 rhythm。
   - 6-8 midterm。
   - 9-11 route。
   - 12-14 project。
   - 15-16 final。
6. `.\mvnw.cmd clean test` 结果，含 Tests run 数量。
7. HTTP 冒烟结果。
8. 浏览器视觉检查结果，至少覆盖 1366x768 和 375x812 的 `/dashboard`、`/week/summary`、`/ending`。
9. Git 提交 hash。
10. 剩余风险。

---

## Known Risks

| 风险 | 说明 | 处理 |
|---|---|---|
| 旧 seed 的 `min_week/max_week` 仍按 4 周体验设计 | Phase 1 不补内容，只保证 16 周骨架可运行 | Phase 2/5 再做阶段内容迁移 |
| 旧结局阈值可能偏低 | 16 周属性成长会让部分结局更容易满足 | Phase 4 做路线评分升级 |
| 周目标池可能出现阶段错位 | Phase 1 不改目标筛选表结构 | Phase 3 做阶段目标过滤 |
| 全量测试中大量 helper 仍手写 4 次 advance | 本计划已覆盖已知核心测试，执行时还要用 `rg "advanceWeek"` 复查 | 发现测试失败时优先修 helper，不改业务语义 |

---

## Plan Self-Review

- Spec coverage: 本计划覆盖 Phase 1 的统一日历、16 周结束判定、阶段标签、旧 4 周文案替换、主题 hook 映射、测试、HTTP 冒烟和交接文档。
- Out of scope: 路线评分、阶段目标过滤、16 周内容补齐、数据库配置化均留到后续 Phase。
- Placeholder scan: 无占位项或延后实现项。
- Type consistency: `SemesterCalendarService.SemesterStage`、`WeeklyThemeService.WeekTheme`、`legacySemesterPhaseForWeek`、`weekPhaseLabel` 在计划中命名一致。
