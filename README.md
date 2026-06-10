# HAUT Survivor

> 基于 Java 的高校校园生存挑战与成长管理系统——以河南工业大学为例

Spring Boot 3.3 + MyBatis-Plus + MySQL + Thymeleaf 的周回合制大学生模拟器。

当前版本已完成 UI 2.0、周目标、成就称号、周总结、学业危机内容包、NPC 搭子系统、传闻/周主题机制化、探索奇遇链、CP4.1-CP4.9 影响系统小步补强，以及 CP5 A/B 均衡：NPC 关系成长、NPC 剧情进度、NPC 专属分支互动、学期档案和结局成长画像。

## 快速开始

```sql
CREATE DATABASE IF NOT EXISTS haut_survivor
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

确认 `application.yml` 中 MySQL 账号密码（默认 `root / 123456`），然后：

```powershell
.\mvnw.cmd spring-boot:run
```

访问 http://localhost:8080/login

Demo 账号：

```
student / student123
admin / admin123
```

## Demo 演示路线（5-10 分钟）

```
登录 student/student123
  → 创建角色（选择成长路线：考研/就业/竞赛/六边形/摆烂）
  → 仪表盘查看周次、行动点、属性
  → 探索校园（消耗行动点，提升探索度，随机发现隐藏事件）
  → 校园地图触发事件（42 个事件，16 个需要探索度解锁）
  → 加入组织（学生会/实验室项目组/篮球社，需探索度门槛）
  → 挑战副本
      - Java 课设：DDL 前夜（3 阶段：需求风暴→数据库拼图→Bug定位）
      - 体测生存挑战（3 阶段：1000米→引体向上→坐位体前屈）
  → 推进周次（4 周压缩学期）
  → 学期结局（12 种结局，含隐藏趣味结局）
  → 重开新学期（保留路线选择，刷新属性和探索度）
```

## 系统架构

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3.5, Java 17 |
| ORM | MyBatis-Plus 3.5.9 |
| 数据库 | MySQL 8 |
| 模板 | Thymeleaf |
| 构建 | Maven Wrapper |

## 核心功能

- **周回合制**：4 周压缩学期，每周 4 行动点，周结算（压力衰减、健康惩罚）
- **探索系统**：8 个地点探索度 0-100，按阈值解锁隐藏事件（40/60/80）和组织入口
- **事件系统**：42 个事件（26 普通 + 16 隐藏），110 个选项，属性+经验结算
- **组织系统**：3 个组织（学生会/实验室/篮球社），发现→加入→活动→晋升；社团招新周活动贡献/声望更高
- **副本系统**：2 个副本，含数据库拼图和 Bug 定位小游戏
- **结局系统**：12 种结局（7 基础 + 5 隐藏），按属性条件匹配
- **5 条成长路线**：考研/就业/竞赛/六边形/摆烂，影响初始属性
- **NPC 搭子系统**：5 个校园 NPC，熟悉度、主动互动、本周搭子、故事进度和专属分支互动
- **周目标与成就**：每周目标、经验/属性奖励、称号解锁
- **传闻/周主题机制**：传闻影响探索收益、NPC 遇见概率和事件倾向，`event_hint` 已覆盖 academic/social/skill/health/money/pressure；周主题影响事件偏向、社团招新周组织收益和 DDL 高压周副本压力；探索触发的传闻、周主题、搭子和奇遇影响会进入周总结回放，并可在历史周报中跨周复盘
- **搭子救场机制**：本周搭子保留匹配地点稳定小加成；高压力探索时有可复现的随机救场，结果页会显示“搭子救场”来源并降低压力
- **关系成长与学期档案**：NPC 熟悉度会转化为关系阶段和故事进度；故事进度会解锁轻量专属互动并进入影响日志；`/week/history` 升级为学期档案摘要；学期结局页会在结算后展示成长画像
- **探索奇遇链**：5 条探索短支线，共 13 个阶段

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
  data.sql         # 种子数据
  templates/       # Thymeleaf 模板
  static/css/      # 自定义 CSS
```

## 测试

```powershell
.\mvnw.cmd clean test
```

最近一次全量验证：295 个测试，0 失败，0 错误。

## 数据规模

| 内容 | 数量 |
|---|---|
| 校园地点 | 8 |
| 事件（含隐藏） | 42 |
| 事件选项 | 110 |
| 副本 | 2（6 个阶段） |
| 组织 | 3 |
| 学期结局 | 12 |
| 成长路线 | 5 |
| NPC | 5 |
| 机制型传闻 | 16 |
| 探索奇遇链 | 5 条链 / 13 个阶段 |
| CP4 周目标 | 4 |
| CP4 成就 | 5 |

## 文档

- [docs/AI_CONTINUATION_PROMPT.md](docs/AI_CONTINUATION_PROMPT.md) — 交接给下一个 AI 的复制提示词
- [docs/NEXT_AI_HANDOFF.md](docs/NEXT_AI_HANDOFF.md) — 当前状态和下一步建议
- [docs/PROJECT_COMPLETION_STATUS.md](docs/PROJECT_COMPLETION_STATUS.md) — 完成状态
- [docs/requirements/](docs/requirements/) — 需求规格说明书（10 篇拆分文档）
- [docs/superpowers/plans/](docs/superpowers/plans/) — 当前待执行实施计划（CP6 内容包、真实校园地图接入）
- [docs/补充信息.docx](docs/补充信息.docx) — CP6 莲花街校区内容与地图来源材料
