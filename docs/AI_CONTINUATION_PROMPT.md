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
8. docs/superpowers/specs/2026-06-11-full-game-v1-design.md

当前状态：
- 这是一个 Spring Boot 3.3.5 + Java 17 + MyBatis-Plus + MySQL + Thymeleaf 项目。
- Full Game V1 已结项，Docker Compose 一键运行已支持。
- 核心循环已闭合：
  创建角色 -> 探索/事件/组织/副本/NPC -> 周总结 -> 推进周次 -> 学期结局 -> 重开新学期
- 16 周单学期、6 阶段、5 条成长路线、80+ 事件、8 个 NPC、8 个组织、4 个副本、12 种结局。
- 最近一次全量验证：
  .\mvnw.cmd clean test
  Tests run: 408, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
- 本地运行：.\mvnw.cmd spring-boot:run
- Docker 运行：docker compose up --build
- 默认账号：admin / admin123、student / student123
- 管理端仅基础事件管理 CRUD（/admin/events），不是完整运营后台。

重要约束：
- 不要重写整个系统。
- 不要把玩家端 UI 改回后台管理风格。
- 前端继续复用 game-shell、game-hud、game-dock、vn-result、sticky-note__btn 等现有组件。
- 数据库变更保持轻量，优先复用已有表。
- 每次修改后必须运行 .\mvnw.cmd clean test。
- 如果做页面修改，必须做 HTTP 冒烟。
- 不要删除已有功能、seed 数据或测试，除非你能明确证明它们已经废弃。
- 当前 worktree 可能已有未提交改动，先看 git status，不要回滚不属于你的改动。

后续可选项（V1 已结项，以下为建议而非必做）：
- V1.1 NPC 关系线深化
- 更多路线专属副本
- 完整管理后台
- 结局解释文案细化
- 更多阶段内容包
- 多学期兼容

要求：
- 不要重复实现 CP6 第一批、CP6.1、CP6.2、CP6.3、CP6.4。
- 不要重复实现 Full Game V1 Phase 1-5。
- 不要继续按 CP6.x 命名新增零散内容包。
- 不要删除历史计划文档（docs/superpowers/plans/），仅作为追溯参考。

执行要求：
- 先基于 docs/PROJECT_COMPLETION_STATUS.md 和 docs/NEXT_AI_HANDOFF.md 了解项目全貌。
- 完成后汇报修改文件、测试结果、HTTP 冒烟和剩余风险。
```

## 当前文档阅读建议

交接时优先读：

1. `README.md`
2. `docs/PROJECT_COMPLETION_STATUS.md`
3. `docs/NEXT_AI_HANDOFF.md`
4. `docs/requirements/README.md`

历史实施计划保留在 `docs/superpowers/plans/` 中，仅作追溯，不需要重复实现。
