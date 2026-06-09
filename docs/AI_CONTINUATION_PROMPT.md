# AI Continuation Prompt

把下面整段复制给下一个 AI，用于继续开发 HAUT Survivor。

```text
你现在接手 HAUT Survivor 项目。

项目路径：
D:\study\code\java\classlearn\HAUT_Survivor

请先阅读这些文件，不要跳过：
1. README.md
2. docs/PROJECT_COMPLETION_STATUS.md
3. docs/NEXT_AI_HANDOFF.md
4. docs/requirements/README.md
5. docs/requirements/02-core-gameplay.md
6. docs/requirements/05-data-and-technical-design.md
7. docs/requirements/10-ui-2-game-interface-redesign.md

当前状态：
- 这是一个 Spring Boot 3.3.5 + Java 17 + MyBatis-Plus + MySQL + Thymeleaf 项目。
- 当前 Demo 是周回合制大学生模拟器，核心循环已闭合：
  创建角色 -> 探索/事件/组织/副本/NPC -> 周总结 -> 推进周次 -> 学期结局 -> 重开新学期。
- 最近一次验证：
  .\mvnw.cmd clean test
  Tests run: 268, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
- 最近一次 HTTP 冒烟：/dashboard、/map、/exploration、/exploration/4 POST、/week/summary 均为 200，无 Whitelabel。
- CP4.1 已完成：npc_boost 会影响 NPC 遇见概率并在探索结果里体现社交 +1；event_hint 会让地图事件偏向 academic_crisis。

重要约束：
- 不要重写整个系统。
- 不要把玩家端 UI 改回后台管理风格。
- 前端继续复用 game-shell、game-hud、game-dock、vn-result、sticky-note__btn 等现有组件。
- 数据库变更保持轻量，优先复用已有表。
- 每次修改后必须运行 .\mvnw.cmd clean test。
- 如果做页面修改，必须做 HTTP 冒烟；如果有浏览器能力，额外做 1366×768 和 375×812 视觉检查。
- 不要删除已有功能、seed 数据或测试，除非你能明确证明它们已经废弃。

当前最建议做的任务：
先做“视觉复核 + UI 小修”，不要马上开大型 CP5。

请检查这些页面：
1. /dashboard
2. /map
3. /exploration
4. POST /exploration/4 后的探索结果页
5. /week/summary

视觉检查重点：
- 移动端 Dock 是否遮挡内容或按钮。
- “有传闻”标记是否太挤、太突兀或挡住主要信息。
- “本次影响来源”面板是否像游戏反馈，而不是系统日志。
- 属性变化、传闻效果、周主题修正是否容易理解。
- 页面是否过于单调、过密或出现横向滚动。

如果发现问题：
- 只做小范围 CSS/模板修复。
- 不要大改后端机制。
- 不要重写 UI 体系。
- 修完后运行 .\mvnw.cmd clean test。
- 再做 HTTP 冒烟。

完成后请汇报：
1. 检查了哪些页面和尺寸。
2. 发现了哪些视觉/交互问题。
3. 修改了哪些文件。
4. 测试结果。
5. HTTP/浏览器冒烟结果。
6. 剩余风险。

如果要继续做机制扩展，优先考虑：
- event_hint 支持 social/skill/health 等更多 target 映射。
- 周主题影响组织收益、副本风险或事件概率。
- 搭子外溢从“确定性小加成”升级为“稳定小加成 + 随机救场”。
- 建立影响历史日志，用于历史周报和复盘。
```

## 当前文档阅读建议

交接时优先读：

1. `README.md`
2. `docs/PROJECT_COMPLETION_STATUS.md`
3. `docs/NEXT_AI_HANDOFF.md`
4. `docs/requirements/README.md`

历史实施计划和已完成内容包设计稿已经删除，避免下一个 AI 误以为还要实现 CP2、CP3 或 CP4。
