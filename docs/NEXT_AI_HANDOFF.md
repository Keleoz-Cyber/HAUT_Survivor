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
Tests run: 245, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

本地 `main` 当前领先 `origin/main` 7 个提交，包含：

- Content Pack 3 设计文档
- Content Pack 3 实施计划
- NPC 搭子系统种子数据
- NPC 主动互动服务
- NPC 周目标类型
- NPC 页面与 Dashboard 熟人入口
- NPC 搭子周总结反馈

## 最近完成

Content Pack 3：校园搭子与人际关系线。

已完成：

- `npc_interaction`：NPC 主动互动定义表
- `user_npc_weekly_action`：每周 NPC 互动/搭子状态表
- 15 条 NPC 主动互动
- 3 个 NPC 周目标
- 5 个 NPC 成就
- 10 条传闻
- `/npcs/{id}` NPC 详情页
- NPC 互动结果页
- Dashboard 熟人列表可点击，显示关系阶段和本周搭子
- 周总结根据搭子/互动/高压状态生成不同评价

## 当前建议下一步

建议进入 Content Pack 4：传闻与周主题机制化 + 探索奇遇链。

目标不是继续堆静态文案，而是让已有的周主题、传闻、NPC 搭子真正影响玩法：

- 周主题影响事件概率、组织收益、副本风险或探索结果。
- 传闻影响指定地点的收益、风险或事件池。
- 探索触发 2-3 步短奇遇链，让地点更有“正在发生事情”的感觉。
- NPC 搭子在探索或事件中提供小概率救场/加成。

## 重要约束

- 不要重写整个系统。
- 继续小步迭代：设计文档 -> 实施计划 -> 测试驱动实现。
- 前端继续复用 `game-shell`、`game-hud`、`game-dock`、`vn-result`、`sticky-note__btn` 等现有 UI 组件。
- 数据库变更保持轻量；优先复用已有 `rumor`、`weekly_goal`、`campus_location`、`event`、`user_location_exploration`、`user_npc_relation`。
- 每次实现后必须运行 `.\mvnw.cmd clean test`，并做至少 dashboard/map/exploration/week summary 的 HTTP 冒烟。

## 已知风险

- 周主题和传闻目前主要是展示型内容，尚未影响核心机制。
- 随机 NPC 遇见仍只显示倾向提示；主动 NPC 互动已经真实修改属性。
- 移动端视觉需要实际截图检查，HTTP 冒烟不能替代视觉验收。
- 本地提交尚未推送到 GitHub。
