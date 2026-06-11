# NEXT AI HANDOFF

最后更新：2026-06-11

## 当前项目状态

**Full Game V1 已结项。** Docker Compose 一键运行已支持。

HAUT Survivor 是一个功能完整的周回合制大学生模拟器，核心循环闭合：

```text
创建角色 → 探索/事件/组织/副本/NPC → 周总结 → 推进周次 → 学期结局 → 重开新学期
```

技术栈：Spring Boot 3.3.5 + Java 17 + MyBatis-Plus + MySQL 8 + Thymeleaf + Docker Compose。

16 周单学期、6 阶段、5 条成长路线、80+ 事件、8 个 NPC、8 个组织、4 个副本、12 种结局（含路线评分匹配）。

最近一次全量验证：

```text
.\mvnw.cmd clean test
Tests run: 408, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## V1 结项交付物

Full Game V1 Phase 1-5 全部完成，Phase 5 后 Stabilization 和 Route Ending Integration 已完成，Docker 封装已完成。

| 阶段 | 内容 |
|---|---|
| Phase 1 | 16 周单学期骨架、SemesterCalendarService、6 阶段映射 |
| Phase 2 | 周主题升级与阶段反馈深化、6 阶段专属叙事 |
| Phase 3 | 路线倾向推导、阶段加权周目标、Dashboard/周总结路线反馈 |
| Phase 4 | 结局评分升级（EndingScoreService）、5 维评分、路线画像 |
| Phase 5 | 16 周阶段内容补齐、midterm/route/project/final 四阶段事件/传闻/奇遇链/周目标/成就 |
| Route Ending Integration | 评分接入结局匹配，过程条件 OR 评分门槛双通道 |
| Docker 封装 | Dockerfile + docker-compose.yml + application-docker.yml，一键运行 |
| 文档整理 | 删除历史实施计划，保留最终设计、需求、完成状态和交接文档 |

## V1 结项后续迭代建议

以下方向可作为 V1.1 或 V2，**当前不需要继续收尾，V1 可结项**：

1. **V1.1 NPC 关系线深化**：更多 NPC 专属分支互动、好感度事件、搭子组队加成
2. **更多路线专属副本**：图书馆席位战争、蓝桥杯突击、实习面试等
3. **完整管理后台**：组织/结局/副本/成就/NPC/传闻的 CRUD（当前仅有事件管理）
4. **结局解释文案细化**：区分"过程触发"和"评分触发"路线结局，丰富结局叙事
5. **更多阶段内容包**：继续补齐各阶段事件密度、路线专属事件
6. **多学期兼容**：学期间属性继承、成长曲线、跨学期成就

## 运行方式

### 本地

```powershell
.\mvnw.cmd spring-boot:run
```

### Docker

```powershell
docker compose up --build
```

访问 http://localhost:8080，默认账号 `admin / admin123`、`student / student123`。

## 历史完成记录

以下为各阶段完成记录，仅作追溯，不需要重复实现。历史实施计划文件已在结项整理中删除，后续以本文件、`PROJECT_COMPLETION_STATUS.md` 和 Full Game V1 设计文档为准。

### Full Game V1 Route Ending Integration

`SemesterEndingServiceImpl` 注入 `EndingScoreService`，路线结局匹配升级为"过程条件 OR 评分门槛"双通道。新增 6 个测试，408 个测试全绿。

### Full Game V1 Phase 5

新增 `data-v1-stage-fill.sql`，ID 段 7001+，覆盖 midterm、route、project、final 四个薄弱阶段。18 条事件、54 条选项、16 条传闻、3 条奇遇链（9 步）、6 个周目标、6 个成就。

### Full Game V1 Phase 1-4

Phase 1：16 周学期骨架。Phase 2：周主题升级与阶段反馈深化。Phase 3：路线倾向与阶段目标。Phase 4：结局评分升级。

### CP4-CP6

CP4：传闻/周主题/探索奇遇机制化及 CP4.1-CP4.9 小步补强。
CP5：NPC 关系成长、专属分支互动、学期档案、UI 收尾。
CP6：莲花街校区内容包、真实地图、NPC 原型、地点细分、开学迎新周、期末体测周。

详细完成记录见 `docs/PROJECT_COMPLETION_STATUS.md`。

## 重要约束

- 不要重写整个系统。
- 不要把玩家端 UI 改回后台管理风格。
- 前端继续复用 `game-shell`、`game-hud`、`game-dock`、`vn-result`、`sticky-note__btn` 等现有组件。
- 数据库变更保持轻量，优先复用已有表。
- 每次修改后必须运行 `.\mvnw.cmd clean test`。
- 如果做页面修改，必须做 HTTP 冒烟。
- 不要删除已有功能、seed 数据或测试，除非能明确证明已废弃。
