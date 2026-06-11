# HAUT Survivor

> 基于 Java 的高校校园生存挑战与成长管理系统——以河南工业大学莲花街校区为背景

Spring Boot 3.3.5 + Java 17 + MyBatis-Plus + MySQL + Thymeleaf 的周回合制大学生模拟器。

Full Game V1 已完成：16 周单学期、6 阶段、行动点、属性成长、地图探索、随机事件、NPC、组织、副本、周总结、路线倾向、结局评分与结局匹配。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3.5, Java 17 |
| ORM | MyBatis-Plus 3.5.9 |
| 数据库 | MySQL 8 |
| 模板 | Thymeleaf |
| 构建 | Maven Wrapper |
| 部署 | Docker Compose（可选） |

## V1 核心玩法

- **16 周单学期**：开学适应（1-2）→ 节奏建立（3-5）→ 期中波动（6-8）→ 路线分化（9-11）→ 项目与 DDL（12-14）→ 期末与体测（15-16）
- **6 阶段**：各有专属事件偏向、探索收益、组织加成、副本压力调整和周总结叙事
- **行动点系统**：每周 4 行动点，探索/事件/组织/副本/NPC 均消耗行动点
- **7 维属性成长**：学业、健康、金钱、社交、技能、压力、自律
- **5 条成长路线**：考研/就业/竞赛/六边形/摆烂，影响初始属性并驱动路线倾向推导
- **地图探索**：8 个校园地点，真实莲花街校区地图，按阈值解锁隐藏事件和组织入口
- **随机事件**：80+ 事件，视觉小说式演出（场景背景、氛围标签、风险色条）
- **NPC 系统**：8 个校园 NPC，熟悉度 → 关系阶段 → 专属分支互动，搭子加成与救场机制
- **组织系统**：8 个组织，发现 → 加入 → 活动 → 晋升
- **副本系统**：4 个副本，含数据库拼图和 Bug 定位小游戏
- **周目标与成就**：每周可选目标 + 40+ 成就称号
- **传闻与探索奇遇链**：机制型传闻影响探索收益，探索奇遇链提供多步骤叙事支线
- **路线倾向**：基于属性推导成长路线倾向，阶段加权周目标选择
- **结局评分**：5 维评分（学业/技能/社交/生存/均衡）+ 关键证据 + 路线画像
- **路线结局匹配**：过程条件 OR 评分门槛双通道，12 种结局按优先级匹配

## 快速开始

### 本地运行

需要本地 MySQL 8，创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS haut_survivor
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

确认 `application.yml` 中 MySQL 账号密码（默认 `root / 123456`），然后：

```powershell
.\mvnw.cmd spring-boot:run
```

### Docker 运行

需要 Docker 和 Docker Compose：

```powershell
docker compose up --build
```

首次启动需要等待 MySQL 初始化完成（约 30 秒），Spring Boot 会在 MySQL 就绪后自动建表和加载种子数据。

两种方式均访问：http://localhost:8080

## Demo 账号

```
student / student123    （普通玩家）
admin / admin123         （管理员）
```

## Demo 演示路线（5-10 分钟）

```
登录 student/student123
  → 创建角色（选择成长路线：考研/就业/竞赛/六边形/摆烂）
  → 仪表盘查看周次、阶段进度、行动点、属性、路线倾向
  → 探索校园（消耗行动点，提升探索度，随机发现隐藏事件）
  → 校园地图触发事件（80+ 事件，部分需要探索度解锁）
  → 加入组织（8 个组织，需探索度门槛）
  → 挑战副本（课设/体测/小组作业等，含小游戏）
  → NPC 互动（8 个 NPC，主动互动消耗行动点，搭子提供加成）
  → 推进周次（16 周学期，6 个阶段各有主题）
  → 学期结局（12 种结局，含路线评分卡片和成长画像）
  → 重开新学期
```

## 管理端

已实现管理员事件管理 CRUD，路径 `/admin/events`。

管理端不是完整运营后台，仅提供基础的事件增删改查功能。

## 测试

```powershell
.\mvnw.cmd clean test
```

最近一次全量验证：408 个测试，0 失败，0 错误。

## 项目结构

```
src/main/java/cn/haut/survivor/
  config/          # 登录拦截器、Web 配置
  controller/      # 页面控制器
  domain/entity/   # 实体类
  domain/enums/    # 枚举
  mapper/          # MyBatis-Plus Mapper
  service/         # 业务接口
  service/impl/    # 业务实现

src/main/resources/
  schema.sql       # 表结构（启动自动重建）
  data.sql         # 基础种子数据
  data-content-pack-*.sql  # 内容包种子
  data-v1-stage-fill.sql    # V1 阶段内容补齐
  application.yml           # 主配置
  application-docker.yml    # Docker 专用配置
  templates/       # Thymeleaf 模板
  static/css/      # 自定义 CSS
  static/images/   # 地图图片等静态资源
```

## 数据规模

| 内容 | 数量 |
|---|---|
| 校园地点 | 8 |
| 事件（含隐藏） | 80+ |
| 事件选项 | 240+ |
| 副本 | 4（15+ 个阶段） |
| 组织 | 8 |
| 学期结局 | 12 |
| 成长路线 | 5 |
| NPC | 8 |
| 机制型传闻 | 40+ |
| 探索奇遇链 | 10+ 条链 / 30+ 阶段 |
| 周目标 | 30+ |
| 成就 | 40+ |

## 文档

- [docs/PROJECT_COMPLETION_STATUS.md](docs/PROJECT_COMPLETION_STATUS.md) — 完成状态与 V1 结项说明
- [docs/NEXT_AI_HANDOFF.md](docs/NEXT_AI_HANDOFF.md) — V1 结项后续迭代建议
- [docs/AI_CONTINUATION_PROMPT.md](docs/AI_CONTINUATION_PROMPT.md) — 交接给下一个 AI 的复制提示词
- [docs/requirements/](docs/requirements/) — 需求规格说明书
- [docs/superpowers/specs/2026-06-11-full-game-v1-design.md](docs/superpowers/specs/2026-06-11-full-game-v1-design.md) — V1 设计文档
- [docs/补充信息.docx](docs/补充信息.docx) — 莲花街校区内容与地图来源材料

## V1 结项说明

Full Game V1 已完成，涵盖 16 周单学期完整体验、6 阶段节奏、路线倾向与结局评分系统。后续扩展可作为 V1.1 或 V2，包括：NPC 关系线深化、更多路线专属副本、完整管理后台、结局文案细化、更多阶段内容包。
