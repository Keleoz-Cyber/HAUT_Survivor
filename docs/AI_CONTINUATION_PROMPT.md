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
- 最近一次全量验证：
  .\mvnw.cmd clean test
  Tests run: 324, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
- 最近一次 HTTP 冒烟覆盖：
  /dashboard、/map、/map/location/2/event、/exploration、/exploration/4 POST、/week/summary、
  /organizations、/organizations/1、/organizations/6001、/dungeons、/dungeons/1、/dungeons/6001、
  /dungeons/1/start、/dungeons/6001/start、/dungeons/1/play、/dungeons/6001/play。
- CP4.1-CP4.9 已完成：传闻/周主题机制化、探索奇遇链、搭子救场、影响历史日志和历史周报。
- CP5 A/B 均衡已完成：NPC 关系成长、NPC 剧情进度、NPC 专属分支互动、学期档案和结局成长画像。
- CP5 UI 收尾已完成：移动端 Dock 遮挡复核和小范围 CSS 修复。
- CP6 第一批已完成：莲花街校区内容包、真实校区地图图片层、地图热点、组织/副本深链缺档案保护。

重要约束：
- 不要重写整个系统。
- 不要把玩家端 UI 改回后台管理风格。
- 前端继续复用 game-shell、game-hud、game-dock、vn-result、sticky-note__btn 等现有组件。
- 数据库变更保持轻量，优先复用已有表。
- 每次修改后必须运行 .\mvnw.cmd clean test。
- 如果做页面修改，必须做 HTTP 冒烟；如果有浏览器能力，额外做 1366×768 和 375×812 视觉检查。
- 不要删除已有功能、seed 数据或测试，除非你能明确证明它们已经废弃。
- 当前 worktree 可能已有未提交改动，先看 git status，不要回滚不属于你的改动。

当前最建议做的任务：
不要重复实现 CP6 第一批、CP6.1、CP6.2 或 CP6.3。优先从以下方向继续：

可选方向：
1. CP6.4 期末与体测周机制化：让第 4 周主题进一步影响健康事件、操场/图书馆路线、体测压力和复习收益。
2. 或先做 CP6 浏览器视觉复核：地图热点、开学周新增事件页、NPC 详情页移动端检查。

执行要求：
- 先基于 docs/PROJECT_COMPLETION_STATUS.md 和 docs/NEXT_AI_HANDOFF.md 写小范围计划，再实现。
- 完成后汇报修改文件、测试结果、HTTP/浏览器冒烟和剩余风险。
```

## 当前文档阅读建议

交接时优先读：

1. `README.md`
2. `docs/PROJECT_COMPLETION_STATUS.md`
3. `docs/NEXT_AI_HANDOFF.md`
4. `docs/requirements/README.md`

已完成的历史实施计划已经清理，避免下一个 AI 误以为 CP5 或 CP6 第一批仍待实现。
