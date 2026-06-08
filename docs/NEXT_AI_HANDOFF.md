# NEXT AI HANDOFF

最后更新：2026-06-08

## 当前项目状态

HAUT Survivor 当前是一个可运行的周回合制大学生模拟器 Demo。核心循环已闭合：

```text
创建角色 -> 探索/事件/组织/副本/NPC -> 周总结 -> 推进周次 -> 学期结局 -> 重开新学期
```

最近一次全量验证：

```text
.\mvnw.cmd clean test
Tests run: 263, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

最近一次 HTTP 冒烟：2026-06-08，端口 `8081`，登录 `student/student123` 后创建临时角色，`/dashboard`、`/map`、`/exploration`、`/exploration/2` POST、`/week/summary` 均为 200，无 Whitelabel；`/map` 和 `/exploration` 可见“有传闻”，探索结果页可见“本次影响来源”。

本地 `main` 当前领先 `origin/main` 多个提交，最近内容包含：

- Content Pack 3：校园搭子与人际关系线
- Content Pack 4 设计文档与实施计划
- Content Pack 4：传闻、周主题与探索奇遇机制化
- NPC 搭子系统、NPC 主动互动、NPC 页面与 Dashboard 熟人入口
- 机制型传闻、周主题修正、探索奇遇链、探索影响来源展示

## 最近完成

Content Pack 4：传闻、周主题与探索奇遇机制化。

已完成：

- `rumor` 表新增 `effect_type`、`effect_value`、`effect_target`
- 新增 `exploration_story_chain`：探索奇遇链定义表
- 新增 `exploration_story_progress`：玩家奇遇进度表
- 新增 16 条机制型传闻
- 新增 5 条探索奇遇链，共 13 个阶段
- 新增 4 个 CP4 周目标：情报猎人、校园奇遇追踪者、顺势而为、搭子救场
- 新增 5 个 CP4 成就：情报新生、校园目击者、支线清理大师、节奏感选手、有人罩着
- 新增 `WeeklyModifierService`、`RumorEffectService`、`ExplorationStoryService`
- 探索结果页展示“本次影响来源”和奇遇链卡片
- 地图页、探索页对有传闻地点显示“有传闻”标记
- 周总结会根据本周奇遇进度生成不同评价

## 当前建议下一步

建议先做 CP4.1 小修和视觉复核，不要马上继续堆 CP5。

优先检查：

- `/exploration/result` 影响来源面板在桌面端和移动端是否遮挡 Dock
- `/map` 和 `/exploration` 的“有传闻”标记是否过密或位置突兀
- `/week/summary` 奇遇评价是否能被玩家感知
- `npc_boost` 传闻是否需要接入 NPC 遇见概率
- `event_hint` 传闻是否需要接入事件池倾向
- 搭子外溢是否保持确定性小加成，还是改成概率救场

## 重要约束

- 不要重写整个系统。
- 继续小步迭代：设计文档 -> 实施计划 -> 测试驱动实现。
- 前端继续复用 `game-shell`、`game-hud`、`game-dock`、`vn-result`、`sticky-note__btn` 等现有 UI 组件。
- 数据库变更保持轻量；优先复用已有 `rumor`、`weekly_goal`、`campus_location`、`event`、`user_location_exploration`、`user_npc_relation`、`exploration_story_chain`、`exploration_story_progress`。
- 每次实现后必须运行 `.\mvnw.cmd clean test`，并做至少 dashboard/map/exploration/week summary 的 HTTP 冒烟。

## 已知风险

- `explore_bonus`、`attr_bonus`、`safe_zone` 已能影响探索；`npc_boost` 和 `event_hint` 当前不会产生数值变化，也不会进入影响来源面板。
- 周主题目前主要影响探索结果，尚未影响组织收益、副本风险或事件概率。
- 搭子外溢当前是匹配地点确定触发，不是概率救场。
- 随机 NPC 遇见仍只显示倾向提示；主动 NPC 互动已经真实修改属性。
- 移动端视觉需要实际截图检查，HTTP 冒烟不能替代视觉验收。
- 本地提交尚未推送到 GitHub。
