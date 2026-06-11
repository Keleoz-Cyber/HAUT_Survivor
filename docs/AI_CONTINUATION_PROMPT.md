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
9. docs/superpowers/plans/2026-06-11-full-game-v1-phase-1-semester-skeleton.md
10. docs/superpowers/plans/2026-06-11-full-game-v1-phase-2-week-theme-feedback.md
11. docs/superpowers/plans/2026-06-11-full-game-v1-phase-3-route-goals.md
12. docs/superpowers/plans/2026-06-11-full-game-v1-phase-4-ending-score.md

当前状态：
- 这是一个 Spring Boot 3.3.5 + Java 17 + MyBatis-Plus + MySQL + Thymeleaf 项目。
- 当前 Demo 是周回合制大学生模拟器，核心循环已闭合：
  创建角色 -> 探索/事件/组织/副本/NPC -> 周总结 -> 推进周次 -> 学期结局 -> 重开新学期。
- 最近一次全量验证：
  .\mvnw.cmd clean test
  Tests run: 402, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
- 最近一次 HTTP 冒烟覆盖：
  /login、/dashboard（含 stage-progress、route-tendency、game-dock、"共 16 周"）、/map（含 campus-map）、/exploration、POST /exploration/4、/week/summary、/organizations、/npcs/6101、/dungeons、/ending，均为 200，无 Whitelabel。
- CP4.1-CP4.9 已完成：传闻/周主题机制化、探索奇遇链、搭子救场、影响历史日志和历史周报。
- CP5 A/B 均衡已完成：NPC 关系成长、NPC 剧情进度、NPC 专属分支互动、学期档案和结局成长画像。
- CP5 UI 收尾已完成：移动端 Dock 遮挡复核和小范围 CSS 修复。
- CP6 第一批已完成：莲花街校区内容包、真实校区地图图片层、地图热点、组织/副本深链缺档案保护。
- CP6.3 已完成：开学迎新周机制化（事件偏向、组织门槛、NPC 互动）。
- CP6.4 已完成：期末与体测周机制化（图书馆/操场探索收益、physical 副本压力缓冲、体测副本和期末事件种子内容）。
- 项目方向已从 CP6.x 小内容包转向 Full Game V1，不再继续以 CP6.x 方式补零散内容。
- Full Game V1 Phase 1-5 全部完成：
  - Phase 1：16 周单学期骨架、SemesterCalendarService、阶段映射
  - Phase 2：周主题升级与阶段反馈深化、6 阶段专属叙事
  - Phase 3：路线倾向推导、阶段加权周目标、Dashboard/周总结路线反馈
  - Phase 4：结局评分升级（EndingScoreService）、5 维评分 read model、路线画像
  - Phase 5：16 周阶段内容补齐、midterm/route/project/final 四阶段事件/传闻/奇遇链/周目标/成就
- V1 五个阶段全部完成，后续可深化内容或推进多学期。
- Full Game V1 Stabilization 已完成：Phase 5 后基础页面和关键流程已复核，`data-v1-stage-fill.sql` 头部注释已澄清 final 阶段覆盖来源；本轮无 Java、模板、CSS、schema 或 seed 语义变更。未执行 headless/实机浏览器截图。

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
Full Game V1 Phase 1-5 已全部完成。后续可选项：
- 深化各阶段事件密度或路线专属事件
- 推进多学期兼容
- 结局评分与路线评分更深度结合
- NPC 属性影响落地、副本扩展

要求：
- 不要重复实现 CP6 第一批、CP6.1、CP6.2、CP6.3 或 CP6.4。
- 不要重复实现 Full Game V1 Phase 1-5。
- 不要继续按 CP6.x 命名新增零散内容包。

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
旧 CP6.1-CP6.4 执行计划稿已清理；CP6 完成情况以 `docs/PROJECT_COMPLETION_STATUS.md` 和 `docs/NEXT_AI_HANDOFF.md` 为准。
