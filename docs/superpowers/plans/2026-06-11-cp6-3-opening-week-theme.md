# CP6.3 Opening Week Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the week 1 theme, "开学适应周", mechanically visible through event bias, organization joining, NPC interaction, and a small CP6.3 seed pack about orientation, course selection, campus card/network affairs, dorm adaptation, and recruitment warm-up.

**Architecture:** Keep weekly-theme gameplay hooks centralized in `WeeklyThemeService`. Reuse existing tables and existing page templates; CP6.3 should be a light mechanism and seed expansion, not a new subsystem. Tests must prove each hook independently before implementation.

**Tech Stack:** Spring Boot 3.3.5, Java 17, MyBatis-Plus, MySQL/H2 test seed SQL, Thymeleaf, JUnit 5, AssertJ.

---

## 给编码 AI 的执行提示词

你现在接手 HAUT Survivor 项目的 CP6.3。项目路径是：

```text
D:\study\code\java\classlearn\HAUT_Survivor
```

请先读：

1. `README.md`
2. `docs/PROJECT_COMPLETION_STATUS.md`
3. `docs/NEXT_AI_HANDOFF.md`
4. `docs/AI_CONTINUATION_PROMPT.md`
5. `docs/superpowers/plans/2026-06-11-cp6-1-npc-prototypes.md`
6. `docs/superpowers/plans/2026-06-11-cp6-2-location-detailing.md`

当前已完成 CP6、CP6.1、CP6.2，不要重复实现。你要做的是 CP6.3：让第 1 周「开学适应周」从文本提示变成更明确的玩法影响。

硬约束：

- 不新增表，不改 schema。
- 不重写 UI，不改地图图片系统。
- 不删除已有 seed、测试、功能。
- 周主题钩子必须集中在 `WeeklyThemeService`，不要在多个 service 里散写 week number magic。
- CP6.3 seed 追加到 `src/main/resources/data-content-pack-6.sql`，使用 6300 段 ID。
- 先写失败测试，再实现。
- 完成后必须运行 `.\mvnw.cmd clean test`。
- 如果没有改模板/CSS，不要求浏览器视觉检查；但必须做 HTTP 冒烟。
- 如果发现和本计划冲突的现有代码，以现有代码为准，小范围调整并在最终反馈中说明。

CP6.3 目标效果：

- 第 1 周地图事件偏向「生活」类事件，用于突出校园卡、网络、宿舍、迎新路线等开学事务。
- 第 1 周加入组织的社交门槛降低 5 点，让“迎新期更容易迈出第一步”。
- 第 1 周主动 NPC 互动在没有本周搭子加成时额外熟悉度 +1，并在结果文案中显示开学适应周反馈。
- 追加少量 CP6.3 开学周 seed：6 个事件、18 个选项、4 条传闻、2 个周目标、2 个成就。
- 更新项目状态与交接文档，把 CP6.3 标记为完成，并给下一步建议。

---

## Scope And Non-Goals

CP6.3 只做一批可验证的小机制：

- `WeeklyThemeService.preferredEventType(1)` 从 `null` 改为 `"生活"`。
- `WeeklyThemeService` 新增组织加入门槛减免 hook。
- `WeeklyThemeService` 新增 NPC 开学互动熟悉度 hook 和文案 hook。
- `RumorEffectServiceImpl` 支持 `event_hint` 的 `effect_target = "life"` 映射到 `"生活"`。
- `OrganizationServiceImpl.join` 使用周主题门槛减免。
- `NpcServiceImpl.interact` 使用周主题熟悉度奖励。
- CP6.3 seed 只追加内容，不修改 CP6/CP6.1/CP6.2 已有 ID。

不要做这些事：

- 不新增 UI 页面。
- 不修改 Dock、地图热点、CSS。
- 不新增 NPC 原型。
- 不新增地点或地图层级。
- 不把周主题存入数据库。
- 不重构事件随机算法。
- 不改学期结局规则。

---

## File Map

Modify:

- `src/main/java/cn/haut/survivor/service/WeeklyThemeService.java`
  - Centralize week 1 event bias, organization join threshold reduction, and NPC interaction familiarity bonus.

- `src/main/java/cn/haut/survivor/service/impl/EventServiceImpl.java`
  - No algorithm rewrite. It should automatically pick up `preferredEventType(1) == "生活"`.

- `src/main/java/cn/haut/survivor/service/impl/RumorEffectServiceImpl.java`
  - Add `life -> 生活` to `preferredEventTypeForTarget`.
  - Keep existing mappings unchanged.

- `src/main/java/cn/haut/survivor/service/impl/OrganizationServiceImpl.java`
  - Use `WeeklyThemeService.organizationJoinSocialRequirementReduction(currentWeek)` in `join`.
  - Keep base threshold 40.

- `src/main/java/cn/haut/survivor/service/impl/NpcServiceImpl.java`
  - Inject `WeeklyThemeService`.
  - Add opening-week familiarity bonus only when the NPC is not already the current weekly buddy.
  - Append a short theme feedback sentence to `NpcInteractionResult.resultText()`.

- `src/main/resources/data-content-pack-6.sql`
  - Append CP6.3 seed with IDs in the 6300 range.

- `docs/PROJECT_COMPLETION_STATUS.md`
  - Add CP6.3 completion notes and update test count after running tests.

- `docs/NEXT_AI_HANDOFF.md`
  - Add CP6.3 completion notes, test count, smoke result, remaining risks, and next recommendation.

- `docs/AI_CONTINUATION_PROMPT.md`
  - Remove CP6.3 from “当前最建议做的任务”; replace with the next sensible CP6.x direction.

Create:

- `src/test/java/cn/haut/survivor/service/ContentPack6OpeningWeekTests.java`
  - CP6.3 seed validation.

Modify tests:

- `src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java`
- `src/test/java/cn/haut/survivor/service/EventServiceTests.java`
- `src/test/java/cn/haut/survivor/service/RumorEffectServiceTests.java`
- `src/test/java/cn/haut/survivor/service/OrganizationServiceTests.java`
- `src/test/java/cn/haut/survivor/service/NpcServiceTests.java`

---

## ID Rules

Use these CP6.3 IDs:

- Events: `6301-6306`
- Event options: `630101-630603`
- Rumors: `6301-6304`
- Weekly goals: `6301-6302`
- Achievements: `6301-6302`

Do not use:

- `6001-6008` events, `600101-600803` options: CP6 base.
- `6101-6103` NPCs, `610001-610011` interactions: CP6.1.
- `6201-6210` events, `620101-621003` options, `6201-6212` story chains: CP6.2.
- `900000-900999`: virtual NPC story branch range.

---

### Task 1: Extend WeeklyThemeService Tests First

**Files:**

- Modify: `src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java`
- Modify after failing test: `src/main/java/cn/haut/survivor/service/WeeklyThemeService.java`

- [ ] **Step 1: Update the failing test**

Change `weeklyGameplayHooksAreCentralizedByThemeWeek` so week 1 expects `"生活"`, and add assertions for the new hooks:

```java
@Test
void weeklyGameplayHooksAreCentralizedByThemeWeek() {
    assertThat(service.preferredEventType(1)).isEqualTo("生活");
    assertThat(service.preferredEventType(2)).isEqualTo("社交");
    assertThat(service.preferredEventType(3)).isEqualTo("学习");
    assertThat(service.preferredEventType(4)).isEqualTo("健康");
    assertThat(service.preferredEventType(99)).isEqualTo("健康");

    assertThat(service.organizationActivityBonus(1)).isZero();
    assertThat(service.organizationActivityBonus(2)).isEqualTo(1);
    assertThat(service.organizationActivityBonus(3)).isZero();

    assertThat(service.organizationJoinSocialRequirementReduction(1)).isEqualTo(5);
    assertThat(service.organizationJoinSocialRequirementReduction(2)).isZero();
    assertThat(service.organizationJoinSocialRequirementReduction(null)).isEqualTo(5);

    assertThat(service.npcOpeningWeekFamiliarityBonus(1, false)).isEqualTo(1);
    assertThat(service.npcOpeningWeekFamiliarityBonus(1, true)).isZero();
    assertThat(service.npcOpeningWeekFamiliarityBonus(2, false)).isZero();
    assertThat(service.openingWeekNpcInteractionSuffix(1, false)).contains("开学适应周");
    assertThat(service.openingWeekNpcInteractionSuffix(1, true)).isBlank();

    assertThat(service.dungeonPressureBonus(2)).isZero();
    assertThat(service.dungeonPressureBonus(3)).isEqualTo(1);
    assertThat(service.dungeonPressureBonus(4)).isZero();
}
```

- [ ] **Step 2: Run the focused test and confirm it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyThemeServiceTests test
```

Expected: compile failure because the new methods do not exist, or assertion failure because week 1 still returns `null`.

- [ ] **Step 3: Implement centralized hooks**

In `WeeklyThemeService`, change `preferredEventType` and add these methods:

```java
/** Returns the event type boosted by the current weekly theme. */
public String preferredEventType(Integer currentWeek) {
    int week = getTheme(currentWeek == null ? 1 : currentWeek).week();
    return switch (week) {
        case 1 -> "生活";
        case 2 -> "社交";
        case 3 -> "学习";
        case 4 -> "健康";
        default -> null;
    };
}

/** Opening week lowers the social threshold for joining organizations. */
public int organizationJoinSocialRequirementReduction(Integer currentWeek) {
    return getTheme(currentWeek == null ? 1 : currentWeek).week() == 1 ? 5 : 0;
}

/** Opening week makes first contacts slightly easier, but does not stack with weekly buddy familiarity bonus. */
public int npcOpeningWeekFamiliarityBonus(Integer currentWeek, boolean weeklyBuddy) {
    if (weeklyBuddy) {
        return 0;
    }
    return getTheme(currentWeek == null ? 1 : currentWeek).week() == 1 ? 1 : 0;
}

/** Short suffix shown on NPC interaction result text when the opening week bonus applies. */
public String openingWeekNpcInteractionSuffix(Integer currentWeek, boolean weeklyBuddy) {
    return npcOpeningWeekFamiliarityBonus(currentWeek, weeklyBuddy) > 0
            ? " 开学适应周：新学期大家都在重新认识彼此，本次熟悉度额外 +1。"
            : "";
}
```

Do not change existing theme names or icons.

- [ ] **Step 4: Run the focused test**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyThemeServiceTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/WeeklyThemeService.java src/test/java/cn/haut/survivor/service/WeeklyThemeServiceTests.java
git commit -m "feat: add opening week theme hooks"
```

---

### Task 2: Wire Week 1 Event Bias And Rumor Target

**Files:**

- Modify: `src/test/java/cn/haut/survivor/service/EventServiceTests.java`
- Modify: `src/test/java/cn/haut/survivor/service/RumorEffectServiceTests.java`
- Modify after failing test: `src/main/java/cn/haut/survivor/service/impl/RumorEffectServiceImpl.java`

- [ ] **Step 1: Update EventService test expectation**

In `EventServiceTests.weeklyThemeMapsToPreferredEventType`, change week 1 from `null` to `"生活"`:

```java
@Test
void weeklyThemeMapsToPreferredEventType() {
    assertThat(eventService.getWeeklyThemePreferredEventType(1)).isEqualTo("生活");
    assertThat(eventService.getWeeklyThemePreferredEventType(2)).isEqualTo("社交");
    assertThat(eventService.getWeeklyThemePreferredEventType(3)).isEqualTo("学习");
    assertThat(eventService.getWeeklyThemePreferredEventType(4)).isEqualTo("健康");
    assertThat(eventService.getWeeklyThemePreferredEventType(99)).isEqualTo("健康");
}
```

- [ ] **Step 2: Add RumorEffectService test for life mapping**

Find the existing test class and add a test equivalent to:

```java
@Test
void eventHintLifeTargetMapsToLifeEventType() {
    insertRumor(63001L, 1, 8L, "生活事务窗口", "event_hint", 1, "life");

    String preferred = rumorEffectService.getEventHintPreferredEventType(2L, 1, 8L);

    assertThat(preferred).isEqualTo("生活");
}
```

Use the helper style already present in `RumorEffectServiceTests`. If the helper takes different parameters, adapt only the helper call, not the production behavior.

- [ ] **Step 3: Run focused tests and confirm failure**

Run:

```powershell
.\mvnw.cmd -Dtest=EventServiceTests,RumorEffectServiceTests test
```

Expected: `EventServiceTests` should pass after Task 1; `RumorEffectServiceTests` should fail until `life` is mapped.

- [ ] **Step 4: Add mapping in RumorEffectServiceImpl**

In `preferredEventTypeForTarget`, add:

```java
case "life" -> "生活";
```

The full switch should preserve the existing mappings:

```java
private String preferredEventTypeForTarget(String target) {
    return switch (target != null ? target : "") {
        case "academic" -> "academic_crisis";
        case "social" -> "社交";
        case "skill" -> "技能";
        case "health" -> "健康";
        case "money" -> "金钱";
        case "pressure" -> "学习";
        case "life" -> "生活";
        default -> null;
    };
}
```

- [ ] **Step 5: Run focused tests**

Run:

```powershell
.\mvnw.cmd -Dtest=EventServiceTests,RumorEffectServiceTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/RumorEffectServiceImpl.java src/test/java/cn/haut/survivor/service/EventServiceTests.java src/test/java/cn/haut/survivor/service/RumorEffectServiceTests.java
git commit -m "feat: map opening week event bias"
```

---

### Task 3: Add Opening Week Organization Join Tolerance

**Files:**

- Modify: `src/test/java/cn/haut/survivor/service/OrganizationServiceTests.java`
- Modify after failing test: `src/main/java/cn/haut/survivor/service/impl/OrganizationServiceImpl.java`

- [ ] **Step 1: Add failing organization tests**

Append these tests to `OrganizationServiceTests`:

```java
@Test
void openingWeekLowersOrganizationJoinSocialRequirement() {
    PlayerAttribute attr = playerService.findAttributeByUserId(2L);
    attr.setSocial(36);
    playerAttributeMapper.updateById(attr);

    organizationService.discover(2L, 1L);
    UserOrganization relation = organizationService.join(2L, 1L);

    assertThat(relation.getMembershipStatus()).isEqualTo("member");
    assertThat(relation.getJoinWeek()).isEqualTo(1);
}

@Test
void organizationJoinRequirementReturnsToNormalAfterOpeningWeek() {
    playerService.advanceWeek(2L);
    PlayerAttribute attr = playerService.findAttributeByUserId(2L);
    attr.setSocial(36);
    playerAttributeMapper.updateById(attr);

    organizationService.discover(2L, 1L);

    assertThatThrownBy(() -> organizationService.join(2L, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("社交值不足")
            .hasMessageContaining("40");
}
```

- [ ] **Step 2: Run focused test and confirm failure**

Run:

```powershell
.\mvnw.cmd -Dtest=OrganizationServiceTests test
```

Expected: first new test fails because social 36 cannot currently join.

- [ ] **Step 3: Implement reduced threshold**

In `OrganizationServiceImpl.join`, replace the hardcoded social check with:

```java
PlayerProfile profile = playerService.findProfileByUserId(userId);
int baseRequiredSocial = 40;
int requiredSocial = Math.max(0, baseRequiredSocial
        - weeklyThemeService.organizationJoinSocialRequirementReduction(profile.getCurrentWeek()));
PlayerAttribute attribute = playerService.findAttributeByUserId(userId);
if (attribute.getSocial() < requiredSocial) {
    throw new IllegalArgumentException("社交值不足，" + org.getOrgName()
            + "面试未通过。当前社交：" + attribute.getSocial()
            + "，需要：" + requiredSocial);
}
```

Keep the later `relation.setJoinWeek(profile.getCurrentWeek())` logic. If the method already declares `PlayerProfile profile` later, do not declare it twice; move the existing declaration up.

- [ ] **Step 4: Run focused test**

Run:

```powershell
.\mvnw.cmd -Dtest=OrganizationServiceTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/OrganizationServiceImpl.java src/test/java/cn/haut/survivor/service/OrganizationServiceTests.java
git commit -m "feat: ease organization joining in opening week"
```

---

### Task 4: Add Opening Week NPC Familiarity Bonus

**Files:**

- Modify: `src/test/java/cn/haut/survivor/service/NpcServiceTests.java`
- Modify after failing test: `src/main/java/cn/haut/survivor/service/impl/NpcServiceImpl.java`

- [ ] **Step 1: Add failing NPC test**

Append this test to `NpcServiceTests`:

```java
@Test
void openingWeekAddsSmallNpcInteractionFamiliarityWhenNotWeeklyBuddy() {
    playerService.createProfile(2L, "opening week npc test", "大二", "计算机类", "就业路线");
    npcService.increaseFamiliarity(2L, 6101L, 25);

    NpcInteractionResult result = npcService.interact(2L, 6101L, 610003L, 1);

    assertThat(result.familiarityGain()).isEqualTo(5);
    assertThat(result.resultText()).contains("开学适应周");
}
```

This uses CP6.1 interaction `610003`, whose base familiarity gain is 4. Opening week should make it 5.

- [ ] **Step 2: Protect existing buddy behavior**

Update the existing `weeklyBuddyAddsSmallInteractionBonus` test so it remains explicit:

```java
assertThat(result.familiarityGain()).isEqualTo(5);
assertThat(result.resultText()).doesNotContain("开学适应周");
```

This prevents the opening-week bonus from stacking with the weekly buddy bonus.

- [ ] **Step 3: Run focused test and confirm failure**

Run:

```powershell
.\mvnw.cmd -Dtest=NpcServiceTests test
```

Expected: new test fails because no opening-week bonus exists yet.

- [ ] **Step 4: Inject WeeklyThemeService in NpcServiceImpl**

Add import:

```java
import cn.haut.survivor.service.WeeklyThemeService;
```

Add field:

```java
private final WeeklyThemeService weeklyThemeService;
```

Update constructor parameter list:

```java
WeeklyThemeService weeklyThemeService
```

Assign:

```java
this.weeklyThemeService = weeklyThemeService;
```

- [ ] **Step 5: Apply bonus in interact**

Replace:

```java
int familiarityGain = value(interaction.getFamiliarityChange()) + (weeklyBuddy ? 1 : 0);
```

with:

```java
int openingWeekBonus = weeklyThemeService.npcOpeningWeekFamiliarityBonus(weekNumber, weeklyBuddy);
int familiarityGain = value(interaction.getFamiliarityChange()) + (weeklyBuddy ? 1 : 0) + openingWeekBonus;
```

Replace the result text argument in `new NpcInteractionResult(...)`:

```java
interaction.getResultText(),
```

with:

```java
interaction.getResultText() + weeklyThemeService.openingWeekNpcInteractionSuffix(weekNumber, weeklyBuddy),
```

- [ ] **Step 6: Run focused test**

Run:

```powershell
.\mvnw.cmd -Dtest=NpcServiceTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/NpcServiceImpl.java src/test/java/cn/haut/survivor/service/NpcServiceTests.java
git commit -m "feat: add opening week npc familiarity bonus"
```

---

### Task 5: Append CP6.3 Seed Data

**Files:**

- Modify: `src/main/resources/data-content-pack-6.sql`
- Create: `src/test/java/cn/haut/survivor/service/ContentPack6OpeningWeekTests.java`

- [ ] **Step 1: Write failing seed test**

Create `ContentPack6OpeningWeekTests.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.RumorMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack6OpeningWeekTests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void cp63OpeningWeekEventsAreSeededWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6301L)
                .le(Event::getId, 6306L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(6);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "迎新路线导览",
                        "选课余量刷新",
                        "宿舍网络排查",
                        "校园卡绑定提醒",
                        "社团招新预热群",
                        "食堂错峰互助");
        assertThat(events).extracting(Event::getEventType)
                .contains("生活", "学习", "社交", "健康");
        assertThat(events).allMatch(event -> event.getMinWeek() == 1);
        assertThat(events).allMatch(event -> event.getMaxWeek() <= 2);
        assertThat(events).allMatch(event -> event.getStatus() == 1);

        List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                .ge(EventOption::getEventId, 6301L)
                .le(EventOption::getEventId, 6306L));

        Map<Long, Long> optionCountByEvent = options.stream()
                .collect(Collectors.groupingBy(EventOption::getEventId, Collectors.counting()));
        assertThat(optionCountByEvent).hasSize(6);
        assertThat(optionCountByEvent.values()).allMatch(count -> count == 3);
        assertThat(options).allMatch(option -> option.getPreviewText() != null && !option.getPreviewText().isBlank());
        assertThat(options).allMatch(option -> Set.of("low", "medium", "high").contains(option.getRiskLevel()));
        assertThat(options).anyMatch(option -> option.getSocialChange() > 0);
        assertThat(options).anyMatch(option -> option.getDisciplineChange() > 0);
        assertThat(options).anyMatch(option -> option.getPressureChange() < 0);
    }

    @Test
    void cp63RumorsUseExistingEffectTypesIncludingLifeHint() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6301L)
                .le(Rumor::getId, 6304L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(4);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .containsExactly(
                        "迎新导览队伍从韶华楼出发",
                        "博闻楼校园卡窗口早上更快",
                        "宿舍网络晚高峰前更容易修好",
                        "招新群里有人整理活动表");
        assertThat(rumors).extracting(Rumor::getEffectType)
                .contains("event_hint", "safe_zone", "explore_bonus", "npc_boost");
        assertThat(rumors).extracting(Rumor::getEffectTarget)
                .contains("life", "pressure", "explore", "npc");
        assertThat(rumors).allMatch(rumor -> rumor.getWeekNumber() == 1);
        assertThat(rumors).allMatch(rumor -> rumor.getActive() == 1);
    }

    @Test
    void cp63WeeklyGoalsAndAchievementsUseExistingConditionTypes() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "cp63_opening_week_route", "cp63_first_connections")
                .orderByAsc(WeeklyGoal::getId));

        assertThat(goals).hasSize(2);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .containsExactly("exploration", "npc_interaction");
        assertThat(goals).allMatch(goal -> goal.getTargetValue() != null && goal.getTargetValue() > 0);
        assertThat(goals).allMatch(goal -> goal.getActive() == 1);

        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey, "cp63_opening_week_survivor", "cp63_campus_affairs_stable")
                .orderByAsc(Achievement::getId));

        assertThat(achievements).hasSize(2);
        assertThat(achievements).extracting(Achievement::getConditionType)
                .containsExactly("exploration", "rumor_effect_used");
        assertThat(achievements).allMatch(achievement -> achievement.getActive() == 1);
    }
}
```

- [ ] **Step 2: Run failing seed test**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6OpeningWeekTests test
```

Expected: failure because CP6.3 seed rows do not exist.

- [ ] **Step 3: Append SQL seed**

Append this exact block to the end of `src/main/resources/data-content-pack-6.sql`:

```sql

-- ============================================================
-- Content Pack 6.3: 开学迎新周机制化
-- ============================================================

INSERT INTO `event`
(id, event_name, event_type, location_id, description, scene_image, mood_tag, probability, min_week, max_week, min_explore_level, status) VALUES
(6301, '迎新路线导览', '生活', 7, '韶华楼前临时集合了一支迎新导览队伍，学长学姐准备带新生绕一圈常用地点。你也可以跟着走，把校区路线重新过一遍。', 'scene-campus', '迎新', 65, 1, 2, 0, 1),
(6302, '选课余量刷新', '学习', 1, '教学楼大厅有人盯着选课系统刷新余量，旁边同学互相交换课程评价。时间不多，但信息很关键。', 'scene-classroom', '选课', 55, 1, 2, 0, 1),
(6303, '宿舍网络排查', '生活', 5, '宿舍网络突然不稳，群里有人说可能是路由器设置，也有人建议直接去营业厅登记。今晚还有作业要传。', 'scene-dorm', '网络', 60, 1, 2, 0, 1),
(6304, '校园卡绑定提醒', '生活', 8, '博闻楼营业厅门口贴着校园卡绑定流程，队伍还不长。错过今天，后面可能要排更久。', 'scene-campus', '事务', 58, 1, 2, 0, 1),
(6305, '社团招新预热群', '社交', 7, '招新还没正式开始，几个社团已经在群里发了活动预告。你可以先观察，也可以主动问问节奏。', 'scene-campus', '招新', 50, 1, 2, 0, 1),
(6306, '食堂错峰互助', '健康', 4, '食堂饭点刚过，几个同学在讨论哪层窗口排队最短。新学期第一周，吃饭路线也需要磨合。', 'scene-canteen', '错峰', 52, 1, 2, 0, 1);

INSERT INTO event_option
(id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
(630101, 6301, '跟完整段导览并记路线', '探索路线更稳，自律上升', 'low', '你把图书馆、食堂、营业厅和韶华楼的位置串了起来，脑子里的校区地图清楚不少。', 0, 1, 0, 1, 0, -2, 2, 24),
(630102, 6301, '只听重点，提前离队', '节省时间，收益较轻', 'low', '你记下最常用的几处地点就离开了。路线没完全熟，但至少知道遇事该往哪里走。', 0, 0, 0, 0, 0, -1, 1, 14),
(630103, 6301, '帮别人指路顺便聊天', '社交收益更高', 'medium', '你把刚听来的路线讲给另一个迷路同学，对方很感激，你也发现自己真的记住了。', 0, 0, 0, 3, 0, -1, 1, 22),
(630201, 6302, '整理评价后再调整课表', '学业和纪律稳定', 'low', '你先比较课程评价和时间冲突，再做调整。课表没有完美，但比随手乱点稳很多。', 3, 0, 0, 0, 1, -1, 2, 28),
(630202, 6302, '跟同学交换选课情报', '社交和信息收益', 'low', '你们互相补了几条老师和作业量信息，最后都避开了一个明显不适合自己的选择。', 1, 0, 0, 3, 0, -1, 0, 24),
(630203, 6302, '看到空位立刻抢课', '效率高但风险高', 'high', '你抢到了一个余量，但上课时间和另一门课贴得很紧。短期兴奋，长期可能有点累。', 1, 0, 0, 0, 0, 2, -1, 14),
(630301, 6303, '按步骤重启并记录问题', '技能小升，压力下降', 'low', '你按顺序排查电源、网线和设置，问题居然真的解决了。以后再断网，你不会第一秒就慌。', 1, 0, 0, 0, 3, -2, 2, 28),
(630302, 6303, '找阿杰或室友一起看', '社交救场，速度更快', 'low', '室友帮你看了半天，最后发现是一个很小的设置。大家顺手吐槽了半小时校园网。', 0, 0, 0, 3, 1, -2, 0, 24),
(630303, 6303, '先用热点硬撑', '短期能用，钱和压力受损', 'medium', '热点顶住了今晚，但流量提醒一直弹。问题被推迟了，不是被解决了。', 1, 0, -15, 0, 0, 2, -1, 12),
(630401, 6304, '按流程一次办完', '事务稳定，纪律提升', 'low', '你提前准备好证件、截图和绑定信息，窗口办理很顺。杂事被清掉一件，心里轻了不少。', 0, 0, -10, 0, 1, -2, 2, 24),
(630402, 6304, '排队时问清隐藏步骤', '社交换效率', 'low', '前面的同学提醒你先打开某个页面，省下一次返工。你顺手把步骤记进备忘录。', 0, 0, -10, 2, 1, -1, 1, 22),
(630403, 6304, '今天先不处理', '省时间但埋隐患', 'medium', '你离开了营业厅，暂时没花时间。但想到门禁和消费都靠它，心里还是有点悬。', 0, 0, 0, 0, 0, 2, -1, 8),
(630501, 6305, '先加群观察活动频率', '信息收益，压力较低', 'low', '你把几个社团的活动频率记下来，发现有些并没有传闻中那么占时间。', 0, 0, 0, 2, 0, -1, 1, 18),
(630502, 6305, '主动问一次报名要求', '社交更强，机会增加', 'medium', '学长学姐回答得很详细，还提醒你正式招新时可以先来体验一次。', 0, 0, 0, 4, 0, 0, 1, 26),
(630503, 6305, '暂时屏蔽消息专心适应', '压力下降，错过机会', 'low', '你把群消息静音，先处理眼前的课表和生活事务。安静了一点，但信息也少了一点。', 1, 0, 0, -1, 0, -2, 1, 12),
(630601, 6306, '整理一条错峰吃饭路线', '健康和纪律稳定', 'low', '你记住了几个不那么挤的窗口，吃饭终于不再像随机副本。', 0, 3, -16, 0, 0, -2, 2, 24),
(630602, 6306, '和同学拼桌同步情报', '社交和恢复均衡', 'low', '你们边吃边交换课表、宿舍和招新消息。一顿饭下来，校园生活像有了点轮廓。', 0, 2, -18, 3, 0, -1, 0, 26),
(630603, 6306, '随便买点赶去下一处', '省时间但恢复不足', 'medium', '你很快解决了午饭，但下午状态一般。新学期不是每件事都能靠赶路解决。', 1, -1, -12, 0, 0, 1, 0, 12);

INSERT INTO rumor
(id, week_number, location_id, rumor_title, rumor_text, effect_hint, effect_type, effect_value, effect_target, rarity, active) VALUES
(6301, 1, 7, '迎新导览队伍从韶华楼出发', '韶华楼前这周常有导览队伍，跟一段路就能把常用地点串起来。', '韶华楼事件更偏向生活事务', 'event_hint', 1, 'life', 'common', 1),
(6302, 1, 8, '博闻楼校园卡窗口早上更快', '早上去博闻楼营业厅处理校园卡和网络问题，排队通常更稳定。', '生活服务点压力风险降低', 'safe_zone', 1, 'pressure', 'common', 1),
(6303, 1, 5, '宿舍网络晚高峰前更容易修好', '晚高峰前排查宿舍网络，成功率比深夜临时救火高得多。', '宿舍探索收益提高', 'explore_bonus', 1, 'explore', 'common', 1),
(6304, 1, 7, '招新群里有人整理活动表', '社团招新预热群里有人整理了活动时间表，愿意开口就能少走弯路。', '社团区更容易遇见 NPC', 'npc_boost', 8, 'npc', 'rare', 1);

INSERT INTO weekly_goal
(id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, active) VALUES
(6301, 'cp63_opening_week_route', '开学校园路线', '本周探索 2 次，把迎新、食堂、宿舍和事务窗口串成自己的校园路线。', 'exploration', 2, 35, 'discipline', 1, 1),
(6302, 'cp63_first_connections', '新学期先打招呼', '本周完成 1 次 NPC 互动，先建立一个能继续发展的校园连接。', 'npc_interaction', 1, 35, 'social', 1, 1);

INSERT INTO achievement
(id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, active) VALUES
(6301, 'cp63_opening_week_survivor', '开学路线不迷路', '累计探索 3 次开学周相关地点，开始掌握莲花街校区的基本节奏。', '🧭', 'exploration', 3, '开学路线员', 1),
(6302, 'cp63_campus_affairs_stable', '事务窗口不慌了', '累计使用 2 次传闻效果，把校园卡、网络和导览消息转成实际优势。', '🪪', 'rumor_effect_used', 2, '事务稳定器', 1);
```

- [ ] **Step 4: Run seed test**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6OpeningWeekTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Run related content tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6Tests,ContentPack6NpcTests,ContentPack6LocationDetailTests,ContentPack6OpeningWeekTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```powershell
git add src/main/resources/data-content-pack-6.sql src/test/java/cn/haut/survivor/service/ContentPack6OpeningWeekTests.java
git commit -m "feat: add CP6.3 opening week seeds"
```

---

### Task 6: Full Regression And HTTP Smoke

**Files:**

- No code changes unless failures require fixes.

- [ ] **Step 1: Run all tests**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected: all tests pass. Test count should be greater than 317.

- [ ] **Step 2: Start the app**

Run in a separate terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

Wait until Spring Boot reports startup complete.

- [ ] **Step 3: HTTP smoke pages**

Use PowerShell session cookies if login/profile creation is required, following the existing README workflow. Smoke at least:

```text
GET  /dashboard
GET  /map
GET  /map/location/7/event
GET  /map/location/8/event
GET  /exploration
POST /exploration/4
GET  /week/summary
GET  /organizations
GET  /npcs/6101
GET  /dungeons
```

Expected for each page:

- HTTP `200`.
- No Whitelabel page.
- Existing `game-dock` remains present on player pages.

- [ ] **Step 4: Browser visual check only if UI was changed**

If you modified templates or CSS despite the plan, check:

- 1366 x 768
- 375 x 812

Focus on Dock overlap and horizontal scrolling. If you did not touch UI, state clearly that no visual check was required.

---

### Task 7: Update Documentation

**Files:**

- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
- Modify: `docs/NEXT_AI_HANDOFF.md`
- Modify: `docs/AI_CONTINUATION_PROMPT.md`

- [ ] **Step 1: Update PROJECT_COMPLETION_STATUS**

Add a CP6.3 section near CP6/CP6.1/CP6.2:

```markdown
**CP6.3 开学迎新周机制化**
- 第 1 周周主题事件偏向改为「生活」，用于承载迎新、校园卡、网络、宿舍适应等开学事务。
- 第 1 周加入组织时社交门槛降低 5 点，体现迎新期更容易建立连接。
- 第 1 周主动 NPC 互动在没有本周搭子加成时熟悉度额外 +1，并在互动结果中显示开学适应周反馈。
- `event_hint` 新增 `life -> 生活` 映射。
- 新增 CP6.3 seed：事件 6 条、选项 18 条、传闻 4 条、周目标 2 个、成就 2 个。
- 最近全量测试：粘贴 `.\mvnw.cmd clean test` 输出中的 Maven 汇总行，例如 `Tests run: 320, Failures: 0, Errors: 0, Skipped: 0`。
```

Also update any “周主题第 1/4 周仍缺少更明确玩法影响” risk so it becomes “第 4 周仍可继续深化” rather than saying week 1 is missing.

- [ ] **Step 2: Update NEXT_AI_HANDOFF**

Add:

```markdown
CP6.3 开学迎新周机制化：
- 周主题 hook 仍集中在 `WeeklyThemeService`。
- 第 1 周事件偏向「生活」；第 2/3/4 周仍保持社交/学习/健康。
- 组织加入门槛：第 1 周社交要求 40 -> 35。
- NPC 主动互动：第 1 周非搭子互动熟悉度额外 +1。
- CP6.3 seed 使用 6300 段 ID。
- 最近全量测试：粘贴 `.\mvnw.cmd clean test` 输出中的 Maven 汇总行，例如 `Tests run: 320, Failures: 0, Errors: 0, Skipped: 0`。
```

Next recommendation:

```markdown
建议下一步：
- CP6.4 期末与体测周机制化：让第 4 周主题进一步影响健康事件、操场/图书馆路线、体测压力和复习收益。
- 或先做 CP6 浏览器视觉复核：地图热点、开学周新增事件页、NPC 详情页移动端检查。
```

- [ ] **Step 3: Update AI_CONTINUATION_PROMPT**

Replace the current next task:

```markdown
1. CP6.3 周主题「开学迎新周」机制化，影响组织、事件或 NPC 行为。
```

with:

```markdown
1. CP6.4 期末与体测周机制化：让第 4 周主题进一步影响健康事件、操场/图书馆路线、体测压力和复习收益。
2. 或先做 CP6 浏览器视觉复核：地图热点、开学周新增事件页、NPC 详情页移动端检查。
```

- [ ] **Step 4: Commit docs**

```powershell
git add docs/PROJECT_COMPLETION_STATUS.md docs/NEXT_AI_HANDOFF.md docs/AI_CONTINUATION_PROMPT.md
git commit -m "docs: update handoff for CP6.3 opening week"
```

---

## Final Verification Checklist

Before reporting back, run:

```powershell
git status --short
.\mvnw.cmd clean test
```

HTTP smoke:

```text
/dashboard
/map
/map/location/7/event
/map/location/8/event
/exploration
POST /exploration/4
/week/summary
/organizations
/npcs/6101
/dungeons
```

If any page returns 500:

- Check whether it is a pre-existing issue by testing an old ID and a CP6.3-related page.
- Do not hide the failure.
- Include exact route, status code, and whether it is CP6.3-specific.

---

## Required Final Feedback Format

Report in Chinese using this structure:

```markdown
CP6.3 执行反馈

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
命令：`.\mvnw.cmd clean test`
结果：粘贴 Maven 输出中的汇总行，例如 `Tests run: 320, Failures: 0, Errors: 0, Skipped: 0`

6. HTTP 冒烟结果
| 页面 | HTTP | Whitelabel | 备注 |
|---|---:|---|---|

7. 浏览器视觉检查
未修改模板/CSS则写：未执行，CP6.3 未修改模板/CSS，复用现有页面展示。
如果执行了，列出 1366x768 和 375x812 结果。

8. Git 提交
| Commit Hash | Message |
|---|---|

9. 剩余风险
| 风险 | 说明 | 严重度 |
|---|---|---|
```

---

## Self-Review Notes

- CP6.3 scope is one mechanism package, not a full CP7.
- No schema changes are required.
- All proposed hooks are centralized in `WeeklyThemeService`.
- Existing CP6 IDs are not reused.
- Existing UI is untouched unless the implementing AI finds a real rendering bug.
- The plan updates tests before implementation and ends with full regression plus HTTP smoke.
