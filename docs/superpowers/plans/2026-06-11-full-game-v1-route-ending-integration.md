# Full Game V1 Route Ending Integration

最后更新：2026-06-11

## 目标

把 EndingScoreService 的 5 维评分接入 SemesterEndingServiceImpl 的结局匹配，让玩家 16 周过程形成的路线画像不仅显示在结局页，也能参与最终结局判定。

## 现状

- `EndingScoreService.buildScoreReport(userId)` 已能生成 5 维评分 read model（academic/skill/social/survival/balanced），但目前仅用于展示。
- `SemesterEndingServiceImpl.matchRouteEnding(ctx, vars)` 使用硬编码过程条件匹配路线结局：
  - 课设战神：`dungeon1Evaluation == "课设战神"`
  - 实验室编外研究员：`labExplore >= 40 && skill >= 55`
  - 社团风云人物：`orgContribution >= 6 && social >= 65`
  - 图书馆常驻民：`libraryExplore >= 40 && academic >= 65`
  - 体测幸存者：`dungeon2Completed`
- 这两个系统目前互不关联。

## 约束

- 不新增表。
- 不改 schema。
- 不改 ending/index.html（除非必要）。
- 不删除现有结局、seed 或测试。
- 不继续按 CP6.x 命名。
- 保留现有稀缺硬条件优先级。

## 实现方案

### 1. 注入 EndingScoreService

在 `SemesterEndingServiceImpl` 中注入 `EndingScoreService`。

### 2. 升级 matchRouteEnding 签名

```java
private String matchRouteEnding(SettlementContext ctx, Map<String, Integer> vars, EndingScoreService.EndingScoreReport report)
```

在 `settleSemester(...)` 中调用 `endingScoreService.buildScoreReport(userId)` 构建报告，传入 matchRouteEnding。

### 3. 升级路线结局匹配逻辑

保留现有优先级，但为每个路线结局增加评分门槛作为"更强证据"：

| 优先级 | 结局 | 原有条件 | 新增评分门槛 |
|:---:|---|---|---|
| 1 | 课设战神 | `dungeon1Evaluation == "课设战神"` | 不变（稀缺且明确） |
| 2 | 实验室编外研究员 | `labExplore >= 40 && skill >= 55` | 或 `skill dimension >= 70` |
| 3 | 社团风云人物 | `orgContribution >= 6 && social >= 65` | 或 `social dimension >= 70` |
| 4 | 图书馆常驻民 | `libraryExplore >= 40 && academic >= 65` | 或 `academic dimension >= 70` |
| 5 | 体测幸存者 | `dungeon2Completed` | 或 `survival dimension >= 75` |

### 4. 决策规则

- 稀缺硬条件（课设战神、体测副本完成）仍优先，因为它们是明确的行为证据。
- 评分门槛作为"平行证据"：当原条件不满足但评分达标时，仍可命中。
- 多个评分同时满足时，取最强维度的对应结局。
- 无路线结局命中时，继续 fallback 到 `evaluateCondition(...)` 属性匹配。

### 5. 辅助方法

新增一个私有方法从 `EndingScoreReport` 中提取指定维度的分数：

```java
private int getDimensionScore(EndingScoreService.EndingScoreReport report, String key)
```

### 6. 不做的事

- 不新增 ending_score 表。
- 不把 EndingScoreReport 存入数据库。
- 不改 ending/index.html。
- 不改 seed 结局名称。
- 不把所有结局改成评分制。
- 不新增复杂策略类。

## 测试计划

在 `SemesterEndingServiceTests` 中新增 6 个测试：

1. `highAcademicScoreCanMatchLibraryResidentEnding` — academic dimension >= 70 但 libraryExplore < 40 时仍命中图书馆常驻民。
2. `highSkillScoreCanMatchLabResearcherEnding` — skill dimension >= 70 但 labExplore < 40 时仍命中实验室编外研究员。
3. `highSocialScoreCanMatchClubInfluencerEnding` — social dimension >= 70 但 orgContribution < 6 时仍命中社团风云人物。
4. `highSurvivalScoreCanMatchPhysicalSurvivorEnding` — survival dimension >= 75 但未完成体测副本时仍命中体测幸存者。
5. `courseDesignWarriorStillHasTopPriority` — 同时满足课设战神和评分条件时，课设战神优先。
6. `noScoreRouteMatchFallsBackToAttributeEnding` — 不满足任何路线结局时，回退到属性匹配。

所有现有测试必须仍通过。

## 验收

- `.\mvnw.cmd clean test` 全绿。
- `/ending` HTTP 200（未改页面，但需验证不报错）。
