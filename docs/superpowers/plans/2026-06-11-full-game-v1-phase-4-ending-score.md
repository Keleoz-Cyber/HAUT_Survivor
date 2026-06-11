# Full Game V1 Phase 4：结局评分升级

最后更新：2026-06-11

## 目标

让 16 周完整学期经历影响结局判断，扩展结局页展示路线画像和关键证据。

## 约束

- 不新增数据库表
- 不新增 seed
- 不做大型 UI overhaul
- 不重复实现 Phase 1/2/3
- 保留现有 game-shell / game-dock / vn-result 风格

## 现有基础

- `SemesterEndingServiceImpl` 已有 `SettlementContext`（探索度、组织贡献、副本完成）
- `SemesterArchiveServiceImpl` 已有 `buildSummary`（影响日志聚合）
- `RouteTendencyService` 已有路线倾向推导
- `InfluenceLogService` 已有 `listSemesterInfluenceRecaps`
- `UserWeeklyGoalMapper` 可查询周目标完成情况
- 结局页已展示：结局名、属性快照、成长路线、学期成长画像

## 实施范围

### 1. 新增 `EndingScoreService`（纯 Java 层计算）

输出 `EndingScoreReport` read model，包含：

```
EndingScoreReport {
    routeTendencyName: String       // 当前主路线画像（复用 RouteTendencyService）
    routeTendencyDesc: String       // 路线描述
    scores: List<RouteDimensionScore>  // 各维度评分
    evidence: List<String>         // 2-4 条关键证据文案
    semesterSummaryText: String   // 学期总结文案（游戏结算风格）
}

RouteDimensionScore {
    dimensionKey: String   // academic / skill / social / survival / balanced
    dimensionName: String  // 学业表现 / 技能成长 / 社交影响 / 生存能力 / 均衡发展
    score: int             // 0-100
    label: String          // 评级标签：优秀/良好/一般/不足
}
```

评分规则（简单、可测试、可解释）：

| 维度 | 证据来源 | 评分逻辑 |
|---|---|---|
| academic | academic + discipline + 图书馆探索 + 学术类影响日志 | academic*0.5 + discipline*0.3 + 图书馆探索度*0.2，上限 100 |
| skill | skill + 实验室探索 + 副本完成 + 技能类影响日志 | skill*0.5 + 实验室探索度*0.2 + 副本完成数*15 |
| social | social + 组织贡献 + NPC 关系数 + 社交类影响日志 | social*0.4 + 组织贡献*5 + NPC关系数*5 |
| survival | health + 压力控制（100-pressure） | health*0.5 + (100-pressure)*0.5 |
| balanced | 各属性 min/均衡 + 周目标完成覆盖度 | 100 - (max-min)*2 + 目标完成数*3 |

评级标签：
- 80+：优秀
- 60-79：良好
- 40-59：一般
- 0-39：不足

关键证据生成：
- 从各维度中取分数最高的 2-3 个维度，各生成 1 条证据文案
- 证据文案要像游戏结算，不要像后台报表

### 2. 接入 `SemesterEndingController`

- 注入 `EndingScoreService`
- 在 `endingPage` 中调用 `endingScoreService.buildScoreReport(userId)`
- 新增 model 属性：`endingScoreReport`
- 仅在 `hasSettled` 状态下有意义，但始终计算（未结算时显示 null 或空）

### 3. 结局页模板增强

在现有结局展示区域（`semester-growth-portrait` 之后、`semester-report__actions` 之前）新增：

```
.route-score-card          // 路线评分卡片
  .route-score-card__header   // "🧭 路线画像" + 路线名称
  .route-score-card__scores   // 5 个维度评分条
  .route-score-card__evidence // 2-4 条关键证据
  .route-score-card__summary // 学期总结文案
```

保持现有视觉风格（暗色背景、圆角、渐变），移动端不被 Dock 遮挡。

### 4. 测试

- `EndingScoreServiceTests`：覆盖 5 种路线倾向组合、属性极端值、无影响日志 fallback、无周目标 fallback、评级标签
- `SemesterEndingControllerTests`：新增 `endingScoreReport` model attribute 断言
- 覆盖结算后和非结算两种状态

## 不做

- 不改 `SettlementContext` record 定义（保持向后兼容）
- 不改结局匹配规则（`matchRouteEnding` 和 `evaluateCondition` 不变）
- 不改 `SemesterEndingServiceImpl` 核心结算流程
- 不新增数据库表或字段
- 不重写 ending 模板结构

## 文件变更清单

| 文件 | 操作 |
|---|---|
| `src/main/java/.../service/EndingScoreService.java` | 新增 |
| `src/main/java/.../controller/SemesterEndingController.java` | 修改 |
| `src/main/resources/templates/ending/index.html` | 修改 |
| `src/main/resources/static/css/app.css` | 修改 |
| `src/test/java/.../service/EndingScoreServiceTests.java` | 新增 |
| `src/test/java/.../controller/SemesterEndingControllerTests.java` | 修改 |
| docs 更新 | 修改 |
