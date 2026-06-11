# HAUT Survivor Full Game V1 Design

最后更新：2026-06-11

## 目标

HAUT Survivor 下一阶段不再继续按 CP6.x 小内容包推进，而是从当前 4 周 Demo 过渡到一个更接近完整游戏的 V1：

- 先做 **16 周单学期完整版本**，覆盖开学、适应、期中、项目、期末和体测等完整节奏。
- 同时保留 `semester_number`、成长路线、学期档案、结局历史等多学期兼容结构，后续可以扩展到大一到大四。
- 不重写当前系统，不推翻 UI 2.0，不把玩家端改回后台管理风格。

本设计的重点是“骨架升级”，不是继续追加零散事件、NPC 或副本 seed。

## 当前基础

当前项目已经具备以下可复用能力：

- `player_profile` 已有 `current_week`、`semester_phase`、`semester_number`、行动点字段。
- 事件、探索、组织、副本、NPC、周目标、成就、周总结、历史周报、学期结局已经闭环。
- `WeeklyThemeService` 已集中承载周主题钩子，但目前仍按 4 周 Demo 写死。
- `PlayerServiceImpl` 仍有 `MAX_SEMESTER_WEEKS = 4`，`isSemesterOver` 和阶段文案依赖该值。
- 结局系统已有 `SemesterEndingServiceImpl`，但结局匹配仍偏 4 周 Demo 条件。
- 页面已经进入 UI 2.0 体系，核心组件包括 `game-shell`、`game-hud`、`game-dock`、`vn-result`、`sticky-note__btn`。

## V1 范围

### 必做

1. 把学期长度从硬编码 4 周升级为可配置的 16 周。
2. 建立 16 周阶段结构，让每个阶段有明确主题、玩法倾向和提示文案。
3. 让事件、传闻、周目标、周总结、学期结局能理解 16 周节奏。
4. 引入“路线进度”概念，用现有成长路线驱动长期目标和结局评分。
5. 保留现有 CP1-CP6 内容，并把它们迁移为 16 周节奏中的早期/中期/期末内容。
6. 增加必要测试，确保旧 4 周闭环不被意外破坏，新的 16 周学期能正常推进到结局。

### 暂缓

1. 不做完整多学年系统，不引入大一到大四跨年课程树。
2. 不做复杂经济系统、装备系统或背包系统。
3. 不做大型地图引擎，真实校区图仍作为行动地图视觉层。
4. 不重做管理后台。
5. 不一次性补齐 16 周全部内容量，先保证骨架能承载完整学期。

## 学期结构

V1 使用 16 周单学期：

| 阶段 | 周数 | 阶段名 | 主要体验 | 机制倾向 |
|---|---:|---|---|---|
| Stage 1 | 1-2 | 开学适应 | 熟悉地图、NPC、组织入口 | 生活事件、NPC 熟悉度、加入组织门槛减免 |
| Stage 2 | 3-5 | 节奏建立 | 课程压力开始出现，组织活动变多 | 周目标、组织贡献、探索奇遇 |
| Stage 3 | 6-8 | 期中波动 | 学业风险、第一次强压力判断 | 学习事件、副本压力、学业补救 |
| Stage 4 | 9-11 | 路线分化 | 考研/就业/竞赛/社交/摆烂路线开始拉开 | 路线目标、NPC 分支、组织晋升 |
| Stage 5 | 12-14 | 项目与 DDL | Java 课设、小组作业、副本集中 | 副本进度、技能判定、压力管理 |
| Stage 6 | 15-16 | 期末与体测 | 期末复习、体测、生存结算 | 结局评分、体测副本、最终周总结 |

`semester_phase` 建议不再只用 `early/mid/final` 表达全部阶段。V1 可以先兼容旧字段，新增 Java 层 read model：

```text
SemesterStage {
  stageKey: opening | rhythm | midterm | route | project | final
  weekStart: int
  weekEnd: int
  name: String
  description: String
  preferredEventType: String
  primaryLocationIds: List<Long>
}
```

第一阶段可以不建新表，先集中在 `SemesterCalendarService` 或重构后的 `WeeklyThemeService` 中。后续多学期版本再考虑表配置。

## 架构设计

### 学期日历服务

新增或重构出 `SemesterCalendarService`，负责：

- 当前学期总周数：默认 16。
- 判断是否学期结束。
- 根据周次返回阶段信息。
- 提供周主题、阶段主题、推荐行动、核心地点。
- 为 `PlayerServiceImpl`、`WeeklyThemeService`、`WeekSummaryServiceImpl`、Dashboard/Map/Exploration 控制器提供统一入口。

建议先保留 `WeeklyThemeService`，但把“4 周主题列表”和具体 hook 逐步迁移到 `SemesterCalendarService` 或让 `WeeklyThemeService` 依赖它。不要两个服务各自判断周次，避免第 4 周、第 16 周逻辑分裂。

### 学期配置

短期可以使用常量：

```text
DEFAULT_SEMESTER_WEEKS = 16
```

但不要继续散落在多个 Service 中。所有判断必须通过同一服务或同一配置类读取。

如果要更进一步，可以加配置项：

```yaml
gameplay:
  semester-weeks: 16
  weekly-action-points: 4
```

V1 推荐用配置项，因为测试可以临时覆盖为较短周数，验证结局流程更方便。

### 周主题与阶段主题

当前 4 周主题应迁移为 16 周中的关键周或阶段钩子：

| 旧主题 | V1 映射 |
|---|---|
| 第 1 周 开学适应周 | 第 1-2 周 opening 阶段 |
| 第 2 周 社团招新周 | 第 3-5 周 rhythm 阶段，可保留第 3 周为招新峰值 |
| 第 3 周 DDL 高压周 | 第 12-14 周 project 阶段 |
| 第 4 周 期末与体测周 | 第 15-16 周 final 阶段 |

事件偏向不应只看单周，应允许阶段偏向：

- opening：生活、社交。
- rhythm：社交、生活、探索。
- midterm：学习、压力。
- route：技能、社交、学习，按成长路线加权。
- project：学习、技能、压力。
- final：学习、健康、压力。

### 路线进度

现有成长路线目前主要影响初始属性。V1 要让路线成为长期目标：

| 路线 | 长期目标倾向 | 结局评分倾向 |
|---|---|---|
| 考研路线 | 学业、自律、图书馆、期末复习 | 学业高、压力可控、复习链推进 |
| 就业路线 | 技能、社交、项目、副本 | 技能高、项目副本完成、NPC/组织支持 |
| 竞赛路线 | 技能、实验室、挑战副本 | 技能高、实验室探索、课设/挑战高评价 |
| 六边形路线 | 均衡发展 | 多属性均衡、低短板、多个系统轻度参与 |
| 摆烂求生路线 | 压力控制、健康、最低风险通关 | 压力低、健康不崩、关键事件没翻车 |

第一版不新增复杂路线表，可以通过现有数据聚合：

- 属性快照。
- 地点探索度。
- 副本完成记录。
- 组织贡献。
- NPC 关系阶段。
- `user_influence_log` 中的关键影响来源。
- 周目标完成情况。

如果需要持久化路线进度，优先复用 `user_influence_log` 和 `user_weekly_goal`；只有当结局评分无法可靠复盘时，再考虑新增轻量表。

## 数据策略

### 不建议立即新增的大表

暂不新增：

- `semester_stage`
- `route_progress`
- `course_schedule`
- `inventory`
- `relationship_event_history`

这些表会把 V1 扩成另一个大型项目，当前收益不够。

### 允许的轻量变更

可选新增配置表或字段：

1. `game_config`：如果希望学期长度能后台配置。
2. `weekly_goal.min_week/max_week`：如果当前目标池无法按 16 周阶段筛选。
3. `achievement.min_week/max_week`：如果成就也需要阶段性出现。

但推荐第一阶段先不改表，先用 Java 层阶段过滤，确认玩法成立后再配置化。

### Seed 整理原则

后续 seed 不再叫 CP6.x。建议改为 V1 内容分层：

```text
data-v1-semester-opening.sql
data-v1-semester-midterm.sql
data-v1-semester-route.sql
data-v1-semester-project.sql
data-v1-semester-final.sql
```

也可以先保留 `data-content-pack-6.sql`，但新增内容应按 16 周阶段加清晰注释，避免继续变成一锅 SQL。

## 业务流设计

### 推周流程

`PlayerService.advanceWeek` 的目标行为：

1. 校验未超过学期总周数。
2. 结算当前周目标和压力保持目标。
3. 应用周末恢复或惩罚。
4. 推进到下一周。
5. 更新 `semester_phase`，保持旧字段可读。
6. 恢复行动点。
7. 如果推进后超过 16 周，进入学期结束状态。

注意：周总结仍应该在推进前展示当前周。第 16 周总结之后，推进到第 17 周，`isSemesterOver` 为 true，然后进入结局页。

### 事件选择

事件过滤继续使用 `min_week/max_week`。V1 要求：

- 早期事件：`min_week=1, max_week=5`。
- 中期事件：`min_week=6, max_week=11`。
- 后期/项目事件：`min_week=12, max_week=14`。
- 期末/体测事件：`min_week=15, max_week=16`。

现有 `max_week=20` 的通用事件可以继续保留，作为全学期普通事件池。

### 周目标

周目标候选不应在第 1 周就抽到明显后期目标。V1 需要按阶段过滤：

- opening/rhythm：探索、NPC、组织加入、低风险目标。
- midterm：学习事件、压力控制、复习链。
- route：路线行为、组织贡献、NPC 关系。
- project：副本阶段、小组作业、技能提升。
- final：期末复习、体测、压力和健康稳定。

第一版可以在 `WeeklyGoalService.pickCandidateGoals` 中做 Java 层过滤，避免改表。

### 学期结局

结局不再只看最终属性，还要看过程：

- 结局匹配仍优先使用现有 `semester_ending` 表。
- Java 层 `SettlementContext` 扩展为 V1 过程上下文。
- 路线结局优先级要从“单点条件”升级为“路线评分”。

建议的评分维度：

```text
RouteScore {
  routeKey
  attributeScore
  activityScore
  relationshipScore
  riskScore
  milestoneScore
  totalScore
}
```

第一版可以不落表，只在结算时计算并写入 `user_semester_ending` 已有属性快照。若需要在结局页展示更详细画像，可继续复用 `SemesterArchiveService` 动态聚合。

## UI 设计

V1 不需要推翻当前 UI 2.0，但需要让玩家感到“这是一整个学期”：

- Dashboard HUD 显示 `第 X/16 周`，并显示阶段名。
- Dashboard 增加“学期进度条”，按 6 个阶段分段。
- 地图页根据阶段突出推荐地点。
- 周总结页显示“距离期末还有 N 周”或“本阶段剩余 N 周”。
- 结局页把“4 周大学生活”文案改为“这一学期”或 `16 周`。

移动端继续遵守 CP5 的 Dock 安全区规则。

## 测试策略

必须新增或修改以下测试：

- `PlayerServiceTests`
  - 默认学期长度为 16。
  - 第 16 周未结束，第 17 周结束。
  - 阶段标签在 1/3/6/9/12/15/17 周正确。
- `WeeklyThemeServiceTests` 或 `SemesterCalendarServiceTests`
  - 16 周阶段映射正确。
  - 各阶段事件偏向正确。
  - 旧 hook 被映射到新阶段后仍可用。
- `WeeklyGoalServiceTests`
  - 候选目标按阶段过滤。
- `SemesterEndingServiceTests`
  - 16 周结束后可以结算。
  - 未到第 17 周不能结算。
  - 路线评分能影响结局选择。
- Controller Tests
  - Dashboard/Map/WeekSummary 显示 16 周进度，不报错。

每次实现后仍必须运行：

```powershell
.\mvnw.cmd clean test
```

页面修改后必须做 HTTP 冒烟：

```text
/dashboard
/map
/exploration
/week/summary
/ending
/dungeons
/organizations
/npcs/6101
```

如果改 CSS 或模板，继续做 1366x768 和 375x812 视觉检查。

## 实施分期

### Phase 1：学期骨架

目标：把 4 周 Demo 变成 16 周 V1，不补大量新内容。

- 抽出学期长度和阶段服务。
- 替换 `PlayerServiceImpl.MAX_SEMESTER_WEEKS = 4`。
- 修正文案中的“4 周”。
- 保证第 16 周总结后能进入结局。

这是第一优先级。

### Phase 2：周主题升级

目标：让 16 周不是简单拉长，而是有阶段节奏。

- 把 4 个旧周主题迁移成 6 个阶段主题。
- 保留 CP6.3/CP6.4 的开学、期末、体测 hook。
- 扩展事件偏向和周总结文案。

### Phase 3：路线目标与阶段目标

目标：成长路线从初始属性变为贯穿一学期的方向。

- 按成长路线和阶段过滤周目标。
- 增加少量 V1 路线目标 seed。
- Dashboard 展示路线提示。

### Phase 4：结局评分升级

目标：结局能反映整个 16 周过程。

- 扩展 `SettlementContext`。
- 增加路线评分 read model。
- 结局页展示路线画像。

### Phase 5：内容补齐

目标：在骨架稳定后，再按阶段补内容。

- 中期事件。
- 路线分化事件。
- 项目/DDL 事件。
- 期末/体测事件。
- 必要副本扩展。

内容补齐必须服务于 16 周结构，不再以 CP6.x 命名。

## 验收标准

V1 骨架完成后，至少满足：

1. 新角色从第 1 周开始，能推进到第 17 周并进入学期结束。
2. UI 清楚显示第 X/16 周和当前阶段。
3. 原有地图、探索、组织、副本、NPC、周总结、结局主流程不报错。
4. 第 1-2 周仍有开学适应加成，第 15-16 周仍有期末/体测加成。
5. 周目标候选不会明显错位，例如第 1 周抽到期末体测冲刺。
6. 结局至少能区分属性型结局和路线型结局。
7. `.\mvnw.cmd clean test` 全绿。
8. 核心 HTTP 冒烟 200，无 Whitelabel。

## 风险与处理

| 风险 | 说明 | 处理 |
|---|---|---|
| 只把 4 改成 16，体验变稀 | 内容密度不够，玩家会觉得中间很多周重复 | 先做阶段主题和目标过滤，再补内容 |
| 周次逻辑散落 | `PlayerService`、`WeeklyThemeService`、周总结、结局各自判断 | 抽统一日历服务 |
| 旧 seed 周次错位 | 旧第 4 周期末内容在 16 周中过早出现 | 保留旧内容可用，但把关键期末内容迁移到 15-16 周或做兼容映射 |
| 结局条件过早满足 | 16 周属性增长后旧阈值太低 | 结局评分引入过程条件，不只看最终属性 |
| 测试大面积改动 | 很多测试默认 4 周 | 分阶段改测试，先集中修 `PlayerServiceTests` 和主题测试 |

## 下一步

下一步不是直接写代码，而是基于本设计写实施计划。第一份计划应只覆盖 **Phase 1：学期骨架**，不要把 16 周内容、路线评分和 UI 大改混在同一批实现里。
