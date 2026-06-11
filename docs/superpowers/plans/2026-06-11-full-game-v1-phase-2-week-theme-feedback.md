# Full Game V1 Phase 2：周主题升级与阶段反馈深化

最后更新：2026-06-11

## 目标

让 16 周不是简单拉长 4 周 Demo，而是有明确的阶段节奏。每个阶段在 dashboard、周总结、探索、副本、组织、NPC 互动中都有可感知的差异。

## 约束

- 不新增表，不改 schema。
- 不新增 seed。
- 不做路线评分（Phase 3）。
- 不做结局评分升级（Phase 4）。
- 不重写 UI 体系，继续复用 game-shell/game-hud/game-dock。
- 继续复用 data-content-pack-6.sql。

## 现状分析

Phase 1 已完成 6 阶段骨架映射，但存在以下问题：

1. **midterm 和 route 阶段零玩法影响**：这两个阶段有名字和事件偏向，但无任何组织/NPC/副本/探索 hook。
2. **WeeklyModifierService 重复硬编码地点 ID**：`SemesterStage.primaryLocationIds` 已定义，但未被消费。
3. **周总结只有 project 和 final 有特殊文案**：opening/rhythm/midterm/route 四个阶段无专属叙事。
4. **Dashboard 无阶段进度感知**：只显示"第 X 周 · 阶段名"，没有阶段剩余周数或学期进度条。
5. **stageKey 字符串散落**：`"opening"`/`"rhythm"` 等字符串在 WeeklyThemeService、WeeklyModifierService、WeekSummaryServiceImpl 中重复 `.equals()`。

## 实施任务

### Task 1：扩展 SemesterStage，集中阶段玩法数据

**文件**：`SemesterCalendarService.java`

扩展 `SemesterStage` record，新增阶段玩法字段：

```java
record SemesterStage(
    String stageKey,
    int weekStart,
    int weekEnd,
    String name,
    String description,
    String hint,
    String icon,
    String preferredEventType,
    List<Long> primaryLocationIds,
    // Phase 2 新增
    int organizationActivityBonus,        // 组织活动贡献加成
    int organizationReputationBonus,      // 组织活动声望加成
    int organizationJoinSocialReduction, // 加入组织社交门槛减免
    int dungeonPressureBonus,             // 副本压力加成
    int dungeonPressureRelief,            // 副本压力缓解（仅 physical）
    int npcFamiliarityBonus,              // NPC 主动互动熟悉度加成
    String stageSummaryHint               // 周总结阶段提示文案
) {}
```

在 STAGES 静态列表中为 6 个阶段填入对应数值。这样所有消费者只需要 `stage.getXxx()` 即可，不再各自硬编码。

**测试**：扩展 `SemesterCalendarServiceTests`，验证每个阶段的玩法数值。

### Task 2：归一化 WeeklyThemeService，消费阶段数据

**文件**：`WeeklyThemeService.java`

改造目标：所有 hook 方法改为从 `stage.getXxx()` 读取数值，不再各自硬编码 stageKey 比较和 magic number。

具体改动：
- `organizationActivityBonus(week)` → `stage.organizationActivityBonus()`
- `organizationJoinSocialRequirementReduction(week)` → `stage.organizationJoinSocialReduction()`
- `dungeonPressureBonus(week)` → `stage.dungeonPressureBonus()`
- `npcOpeningWeekFamiliarityBonus(week, buddy)` → `stage.npcFamiliarityBonus()`
- `finalWeekExplorationAttributeChange(week, locationId)` → 仍保留（涉及具体地点属性变化，不适合放到通用阶段字段），但文案改为阶段名
- `finalWeekDungeonPressureRelief(week, type)` → `stage.dungeonPressureRelief()`（仅 physical 类型生效）
- 新增 `stageSummaryHint(week)` → 返回 `stage.stageSummaryHint()`
- 新增 `stageDescription(week)` → 返回 `stage.description()`

**测试**：扩展 `WeeklyThemeServiceTests`，覆盖 midterm/route 阶段行为。

### Task 3：归一化 WeeklyModifierService，消费 primaryLocationIds

**文件**：`WeeklyModifierService.java`

改造目标：不再硬编码地点 ID，改为 `stage.primaryLocationIds().contains(locationId)`。

同时为 midterm 和 route 阶段新增探索影响：
- **midterm**：图书馆(2)、教学楼(1)、实验室(6) 提供学业 +1，压力 +1（期中考试压力）
- **route**：按地点多样性探索加分（鼓励在不同地点探索）

**测试**：扩展 `WeeklyModifierServiceTests`，验证 midterm/route 探索影响。

### Task 4：强化 WeekSummaryServiceImpl 阶段反馈

**文件**：`WeekSummaryServiceImpl.java`

改动：
1. 为 6 个阶段各写专属叙事文案（像游戏内总结，不是系统日志）：
   - **opening**：强调探索和社交起步
   - **rhythm**：强调组织参与和节奏建立
   - **midterm**：强调学业压力和第一次危机
   - **route**：强调路线分化和深度选择
   - **project**：强调 DDL 压力和副本进度
   - **final**：强调期末冲刺和学期回顾
2. 在 `WeekSummaryView` 中新增 `stageKey`、`stageDescription`、`stageSummaryHint`、`weeksLeftInStage` 字段
3. 周总结页展示当前阶段名、阶段描述、阶段剩余周数

**测试**：扩展 `WeekSummaryServiceTests`，验证各阶段叙事文案和新增字段。

### Task 5：强化 Dashboard 阶段提示

**文件**：`DashboardController.java`、`dashboard/index.html`、`app.css`

改动（小范围模板/CSS）：
1. Dashboard model 新增：`stageKey`、`stageName`、`stageDescription`、`stageIcon`、`weeksLeftInStage`、`semesterWeeks`
2. 模板新增"学期阶段进度"区域：
   - 显示 6 个阶段名称，当前阶段高亮
   - 当前阶段剩余周数提示（如"本阶段剩余 2 周"）
   - 下一阶段预告（如"下一阶段：期中波动"）
3. 保持 game-shell/game-hud/game-dock 体系不变
4. 移动端 Dock 不遮挡新内容

**测试**：扩展 `DashboardControllerTests`，验证新增 model 属性。

### Task 6：全量验证

1. `.\mvnw.cmd clean test` — 全绿
2. HTTP 冒烟：/dashboard、/map、/exploration、POST /exploration/4、/week/summary、/organizations、/npcs/6101、/dungeons、/ending
3. 检查 dashboard 阶段进度显示、周总结阶段反馈文案
4. 检查移动端 Dock 是否遮挡

### Task 7：更新项目文档

更新 `docs/PROJECT_COMPLETION_STATUS.md`、`docs/NEXT_AI_HANDOFF.md`、`docs/AI_CONTINUATION_PROMPT.md`。

## 不做的事

- 不新增表、不改 schema
- 不新增 seed
- 不做路线评分
- 不做结局评分升级
- 不重写 UI 体系
- 不删除现有功能
- 不改 `min_week`/`max_week` seed 值（那是内容补齐 Phase 5 的事）
