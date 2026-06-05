# HAUT Survivor

> 基于 Java 的高校校园生存挑战与成长管理系统——以河南工业大学为例

Spring Boot 3.3 + MyBatis-Plus + MySQL + Thymeleaf 的周回合制大学生模拟器。

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
  → 学期结局（11 种结局，含 4 种隐藏趣味结局）
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
- **组织系统**：3 个组织（学生会/实验室/篮球社），发现→加入→活动→晋升
- **副本系统**：2 个副本，含数据库拼图和 Bug 定位小游戏
- **结局系统**：11 种结局（7 基础 + 4 隐藏），按属性条件匹配
- **5 条成长路线**：考研/就业/竞赛/六边形/摆烂，影响初始属性

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
.\mvnw.cmd test
```

114 个测试，覆盖所有核心服务层和控制器。

## 数据规模

| 内容 | 数量 |
|---|---|
| 校园地点 | 8 |
| 事件（含隐藏） | 42 |
| 事件选项 | 110 |
| 副本 | 2（6 个阶段） |
| 组织 | 3 |
| 学期结局 | 11 |
| 成长路线 | 5 |

## 文档

- [docs/requirements/](docs/requirements/) — 需求规格说明书（9 篇拆分文档）
- [docs/PROJECT_COMPLETION_STATUS.md](docs/PROJECT_COMPLETION_STATUS.md) — 完成状态
