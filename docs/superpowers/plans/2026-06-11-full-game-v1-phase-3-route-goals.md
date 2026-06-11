# Full Game V1 Phase 3：路线目标与阶段目标

最后更新：2026-06-11

## 目标

让"成长路线"从初始属性标签变为贯穿 16 周学期的实际方向。周目标池按阶段和路线倾向过滤/加权，避免 16 周目标错位（如第 1 周抽到期末体测冲刺）。

## 约束

- 不新增表，不改 schema。
- 不新增 seed。
- 不做结局评分升级（Phase 4）。
- 不重写 UI 体系，继续复用 game-shell/game-hud/game-dock。
- 不删除现有功能、seed 或测试。

## 现状分析

Phase 2 已完成 6 阶段反馈深化，但存在以下问题：

1. **成长路线只是标签**：`player_profile.growth_route` 仅在创建角色时影响初始属性，之后从未被读取用于任何玩法逻辑。
2. **周目标池是平的**：`WeeklyGoalServiceImpl.pickCandidateGoals()` 从所有 `active=1` 的目标中哈希抽取 3 个，不考虑周次、阶段或路线。22+ 个目标全部等权参与，第 1 周可能出现"期末复习冲刺"或"体测副本挑战"。
3. **路线分化阶段（9-11 周）无路线逻辑**：虽然阶段名叫"路线分化"，但完全不理解玩家的成长路线。
4. **Dashboard/周总结无路线反馈**：玩家看不到自己的成长路线是否在推进。

## 实施任务

### Task 1：新增 RouteTendencyService

**文件**：`src/main/java/cn/haut/survivor/service/RouteTendencyService.java`（新增）

基于玩家当前属性和阶段，推导实际成长路线倾向。不新增表，纯 Java 层计算。

```java
@Service
public class RouteTendencyService {

    record RouteTendency(
        String routeKey,       // "academic", "social", "skill", "balanced", "survival"
        String routeName,       // 中文显示名
        String description,    // 一句话描述
        int score               // 0-100，当前匹配度
    ) {}

    /** 推导当前最可能的路线倾向 */
    RouteTendency deriveTendency(PlayerAttribute attribute, String chosenRoute);

    /** 获取所有路线倾向分数（用于展示对比） */
    List<RouteTendency> deriveAllTendencies(PlayerAttribute attribute, String chosenRoute);
}
```

推导规则：
- **academic（学业路线）**：academic ≥ 60, discipline ≥ 50, pressure ≤ 70 → score = academic + discipline - pressure/2
- **social（社交路线）**：social ≥ 50 → score = social + health
- **skill（技能路线）**：skill ≥ 50 → score = skill * 2
- **survival（稳定生活路线）**：health ≥ 60, pressure ≤ 40 → score = health * 2 - pressure
- **balanced（均衡路线）**：所有属性方差小 → score = 100 - max差距

如果推导结果与 chosenRoute 一致，给 +10 bonus，鼓励玩家坚持选择的方向。

### Task 2：调整周目标选择逻辑

**文件**：`src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`（修改）

在 `pickCandidateGoals()` 中增加阶段过滤和路线加权：

1. **阶段过滤**：按当前阶段给目标类型加权重
   - opening：explore_count, npc_meet, buddy_selected, exploration +3
   - rhythm：org_activity, npc_interaction, familiarity_gain +3
   - midterm：academic_event, pressure_keep, exploration_story_step +3
   - route：org_activity, npc_interaction, dungeon_stage +3
   - project：dungeon_stage, skill 相关 +3
   - final：pressure_keep, health 相关 +3

2. **路线加权**：按路线倾向给目标类型加权重
   - academic：academic_event +2, exploration_story_step +1
   - social：npc_meet, npc_interaction, buddy_selected +2
   - skill：dungeon_stage, org_activity +2
   - survival：pressure_keep, health +2
   - balanced：所有类型 +1

3. 不排除任何目标，只调整权重。保证候选至少 3 个。

### Task 3：Dashboard 展示路线倾向

**文件**：`DashboardController.java`、`dashboard/index.html`、`app.css`（修改）

在阶段进度条下方新增路线倾向提示：

```html
<section class="route-tendency">
    <p>🧭 成长倾向：<strong>学业路线</strong> · 你的学业和自律正在稳步推进</p>
</section>
```

小范围 CSS，不重写 UI 体系。移动端 Dock 不遮挡。

### Task 4：WeekSummary 展示路线反馈

**文件**：`WeekSummaryService.java`、`WeekSummaryServiceImpl.java`、`week/summary.html`（修改）

在 `WeekSummaryView` 新增 `routeTendencyName`、`routeTendencyHint` 字段。

周总结页在阶段提示下方展示路线倾向反馈（仅 route 阶段 9-11 周及之后显示）。

### Task 5：测试

- `RouteTendencyServiceTests`（新增）：测试推导逻辑覆盖 5 种路线、属性边界、chosenRoute 一致性 bonus
- `WeeklyGoalServiceTests`（扩展）：测试不同阶段/路线的目标选择偏向
- `DashboardControllerTests`（扩展）：验证 `routeTendencyName` model 属性
- `WeekSummaryServiceTests`（扩展）：验证 route 阶段路线倾向字段

### Task 6：全量验证

1. `.\mvnw.cmd clean test` — 全绿
2. HTTP 冒烟：/dashboard、/map、/exploration、POST /exploration/4、/week/summary、/organizations、/npcs/6101、/dungeons、/ending
3. 检查 dashboard 路线倾向显示、周总结路线反馈
4. 检查移动端 Dock 是否遮挡

### Task 7：更新项目文档

更新 `docs/PROJECT_COMPLETION_STATUS.md`、`docs/NEXT_AI_HANDOFF.md`、`docs/AI_CONTINUATION_PROMPT.md`。

## 不做的事

- 不新增表/字段/seed
- 不做结局评分升级
- 不重写 UI 体系
- 不删除现有功能
- 不改 min_week/max_week seed 值
