# HAUT Survivor 开发文档

> 本文档面向希望理解、修改或扩展 HAUT Survivor 代码库的开发者。

## 目录

- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境搭建](#环境搭建)
- [数据库设计](#数据库设计)
- [核心架构](#核心架构)
- [业务系统详解](#业务系统详解)
- [配置说明](#配置说明)
- [Seed 数据与内容包](#seed-数据与内容包)
- [测试指南](#测试指南)
- [Docker 部署](#docker-部署)
- [扩展指南](#扩展指南)
- [编码规范与约束](#编码规范与约束)

---

## 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.3.5 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8.0 |
| 模板引擎 | Thymeleaf | 3.1.x（Spring Boot 管理） |
| 构建 | Maven | 3.9+（Maven Wrapper） |
| 容器 | Docker Compose | 可选 |

前端不使用前端框架，全部通过 Thymeleaf 模板 + 自定义 CSS 实现游戏化界面。

---

## 项目结构

```
HAUT_Survivor/
├── pom.xml                          # Maven 构建配置
├── Dockerfile                       # Docker 多阶段构建
├── docker-compose.yml               # MySQL + App 编排
├── .dockerignore
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── README.md                        # 项目概览与快速开始
├── docs/
│   ├── requirements/                 # 需求规格说明书（10 篇）
│   ├── superpowers/specs/           # Full Game V1 设计文档
│   ├── DEVELOPMENT.md               # 本文件：开发文档
│   ├── USER_GUIDE.md                # 使用说明文档
│   ├── PROJECT_COMPLETION_STATUS.md # 完成状态
│   ├── NEXT_AI_HANDOFF.md           # 交接文档
│   └── AI_CONTINUATION_PROMPT.md    # AI 交接提示词
└── src/
    ├── main/
    │   ├── java/cn/haut/survivor/
    │   │   ├── HautSurvivorApplication.java   # 启动类
    │   │   ├── config/                        # 配置类
    │   │   │   ├── WebConfig.java              # Web MVC 配置 + 拦截器注册
    │   │   │   └── LoginInterceptor.java       # 登录拦截器（Session 认证）
    │   │   ├── controller/                    # 页面控制器（10 个）
    │   │   ├── domain/
    │   │   │   ├── entity/                     # 实体类（34 个）
    │   │   │   └── enums/                      # 枚举类
    │   │   ├── mapper/                         # MyBatis-Plus Mapper
    │   │   ├── service/                        # 业务接口（23 个）
    │   │   └── service/impl/                   # 业务实现
    │   └── resources/
    │       ├── application.yml                 # 主配置
    │       ├── application-docker.yml          # Docker 专用配置
    │       ├── schema.sql                     # 表结构（启动自动重建）
    │       ├── data.sql                       # 基础种子数据
    │       ├── data-content-pack-*.sql         # 内容包种子
    │       ├── data-v1-stage-fill.sql          # V1 阶段内容补齐
    │       ├── templates/                     # Thymeleaf 模板（23 个）
    │       └── static/
    │           ├── css/app.css                 # 自定义游戏化 CSS
    │           └── images/                     # 静态图片（地图等）
    └── test/
        └── java/cn/haut/survivor/
            ├── HautSurvivorApplicationTests.java
            ├── controller/                     # 控制器测试（10 个测试类）
            ├── service/                        # 服务测试（30+ 个测试类）
            ├── mapper/                         # Mapper 测试
            └── view/                           # 模板资源测试
```

---

## 环境搭建

### 前置条件

- JDK 17+
- Maven 3.9+（或使用 Maven Wrapper）
- MySQL 8.0

### 本地开发

1. **创建数据库**：

```sql
CREATE DATABASE IF NOT EXISTS haut_survivor
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **配置数据库连接**：

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/haut_survivor?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
```

可通过环境变量 `DB_USERNAME` 和 `DB_PASSWORD` 覆盖。

3. **启动**：

```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

应用启动后会自动执行 `schema.sql`（重建表结构）和所有 `data-locations` 指定的种子文件。

4. **访问**：http://localhost:8080

### Docker 方式

```powershell
docker compose up --build
```

访问 http://localhost:8080

---

## 数据库设计

共 32 张表，按功能域划分：

### 用户与角色

| 表 | 说明 |
|---|---|
| `user` | 用户账号（username, password SHA-256, role: ADMIN/USER） |
| `player_profile` | 玩家档案（姓名、年级、专业、成长路线、等级、经验、当前周次、行动点、学期号） |
| `player_attribute` | 7 维属性（学业/健康/金钱/社交/技能/压力/自律，范围 0-100） |

### 地图与探索

| 表 | 说明 |
|---|---|
| `campus_location` | 8 个校园地点（教学楼/图书馆/宿舍/食堂/操场/实验室/社团活动区/快递站） |
| `user_location_exploration` | 玩家对各地点的探索度（0-100） |

### 事件系统

| 表 | 说明 |
|---|---|
| `event` | 事件定义（名称、类型、地点、描述、概率、周次范围、探索度门槛） |
| `event_option` | 事件选项（文案、风险等级、结果文案、7 维属性变化、经验） |
| `event_record` | 玩家事件记录 |

### 组织系统

| 表 | 说明 |
|---|---|
| `organization` | 组织定义（名称、类型、解锁地点、门槛属性、周 AP 消耗） |
| `user_organization` | 玩家-组织关系（成员状态、贡献值、声望、职位） |

### 副本系统

| 表 | 说明 |
|---|---|
| `dungeon` | 副本定义（名称、类型、难度、阶段数） |
| `dungeon_task` | 副本阶段任务 |
| `dungeon_task_option` | 阶段任务选项 |
| `user_dungeon_record` | 玩家副本通关记录 |
| `user_dungeon_task_record` | 玩家副本阶段记录 |

### NPC 系统

| 表 | 说明 |
|---|---|
| `npc` | NPC 定义（名称、性格、倾向属性、归属地点、头像图标） |
| `npc_interaction` | NPC 互动定义（文案、属性变化、熟悉度门槛） |
| `user_npc_relation` | 玩家-NPC 关系（熟悉度、遇见次数、本周搭子标记） |
| `user_npc_weekly_action` | 玩家每周 NPC 行动记录（互动次数限制） |
| `user_npc_story_progress` | NPC 个人故事进度（解锁分支互动条件） |

### 传闻与奇遇

| 表 | 说明 |
|---|---|
| `rumor` | 传闻定义（稀有度、效果类型、效果值、目标属性） |
| `exploration_story_chain` | 探索奇遇链定义（多阶段叙事） |
| `exploration_story_progress` | 玩家奇遇进度 |
| `user_influence_log` | 影响日志（探索触发的传闻/周主题/搭子/奇遇等） |

### 目标与成就

| 表 | 说明 |
|---|---|
| `weekly_goal` | 周目标定义（类型、条件参数、奖励） |
| `user_weekly_goal` | 玩家每周选定的目标及完成状态 |
| `achievement` | 成就定义（条件类型、条件参数） |
| `user_achievement` | 玩家已解锁成就 |

### 周总结与结局

| 表 | 说明 |
|---|---|
| `user_week_summary` | 每周总结快照 |
| `semester_ending` | 结局定义（名称、类型、条件规则、优先级） |
| `user_semester_ending` | 玩家结局结算记录 |

---

## 核心架构

### 分层结构

```
Controller（页面路由 + 模型填充）
    ↓
Service（业务逻辑 + 跨域协调）
    ↓
Mapper（MyBatis-Plus 数据访问）
    ↓
MySQL
```

- **Controller**：每个控制器对应一个页面域，负责 HTTP 请求处理、Session 认证检查、模型属性填充和 Thymeleaf 渲染。不包含业务逻辑。
- **Service**：业务逻辑核心。接口定义在 `service/` 包，实现在 `service/impl/` 包。服务间通过构造器注入互相调用。
- **Mapper**：MyBatis-Plus 自动生成的 CRUD，复杂查询使用 `LambdaQueryWrapper`。不使用 XML mapper 文件。
- **Entity**：使用 Lombok `@Data` 或手写 getter/setter。`PlayerProfile` 和 `PlayerAttribute` 是核心实体。

### 认证机制

`LoginInterceptor` 拦截所有请求（排除 `/login`、`/register`、`/css/**`、`/images/**`、`/webjars/**`）：

- 从 Session 获取 `userId`，不存在则重定向到 `/login`。
- 管理员路径（`/admin/**`）额外检查 `user.role == "ADMIN"`。
- 无 Spring Security，无 JWT，纯 Session 认证。

### 页面路由映射

| 路径 | 控制器 | 说明 |
|---|---|---|
| `/login`, `/register` | AuthController | 登录注册 |
| `/player/create` | PlayerController（内嵌 Dashboard） | 创建角色 |
| `/dashboard` | DashboardController | 主界面（寝室/手机） |
| `/map`, `/map/location/{id}/event` | MapController | 校园地图 + 事件触发 |
| `/exploration`, `/exploration/{id}` POST | ExplorationController | 地点探索 |
| `/organizations`, `/organizations/{id}` | OrganizationController | 组织列表 + 详情 |
| `/dungeons`, `/dungeons/{id}` | DungeonController | 副本海报墙 + 详情 |
| `/npcs/{id}` | NpcController | NPC 详情 |
| `/week/summary`, `/week/history` | WeekSummaryController | 周总结 + 历史周报 |
| `/ending` | SemesterEndingController | 学期结局 |
| `/admin/events` | AdminEventController | 管理员事件管理 |

### 模板体系

所有玩家端页面使用统一的视觉组件：

| CSS 类 | 用途 |
|---|---|
| `game-shell` | 全屏游戏外壳容器 |
| `game-hud` | 顶部信息栏（周次、AP、压力） |
| `game-dock` | 底部导航栏（桌面端流式布局，移动端 fixed） |
| `vn-result` | 视觉小说式结果展示（事件/探索/NPC 互动结果） |
| `sticky-note__btn` | 便利贴风格操作按钮 |

---

## 业务系统详解

### 周回合制核心流程

```
1. 玩家进入 Dashboard，查看当前周次、AP、属性、周主题、路线倾向
2. 消耗 AP 执行行动（探索/事件/组织/副本/NPC 互动）
3. 行动结束后返回 Dashboard
4. 当所有 AP 用完或玩家选择推进时，进入 /week/summary
5. 周总结展示本周数据快照，玩家点击"推进到下一周"
6. PlayerService.advanceWeek() 执行周结算
7. 如果 currentWeek 达到 17（学期结束），进入 /ending
```

#### advanceWeek 周结算规则

| 步骤 | 规则 |
|---|---|
| 1. 压力目标检查 | 调用 `weeklyGoalService.checkPressureKeepGoal` |
| 2. 周次递增 | `currentWeek + 1`，AP 重置为 `maxActionPoints` |
| 3. 学期阶段更新 | 根据新周次映射到 6 阶段之一 |
| 4. 高压健康惩罚 | 如果 `pressure > 80`，`health -= 3` |
| 5. 低血行动点惩罚 | 如果 `health < 20`，下一周 `maxActionPoints -= 1`（最低 2） |
| 6. 自然减压 | `pressure -= 5`（无条件，每周 -5） |
| 7. 属性范围钳制 | 所有属性值限制在 `[0, 100]` |

### 16 周学期与 6 阶段

`SemesterCalendarService` 统一管理学期日历，所有周主题/探索影响/事件偏向/组织加成都从这里读取：

| 阶段 | stageKey | 周数 | 事件偏向 | 核心地点 | 特色机制 |
|---|---|---|---|---|---|
| 开学适应 | opening | 1-2 | 生活 | 宿舍/食堂/社团 | NPC 互动熟悉度 +1，组织加入社交门槛 -5 |
| 节奏建立 | rhythm | 3-5 | 社交 | 食堂/社团/快递站 | 组织活动贡献/声望 +1 |
| 期中波动 | midterm | 6-8 | 学习 | 教学楼/图书馆/实验室 | 教学/图书馆/实验室探索学业 +1 |
| 路线分化 | route | 9-11 | 技能 | 图书馆/实验室/社团 | 图书馆/实验室/惟学楼探索技能 +1 |
| 项目与 DDL | project | 12-14 | 学习 | 教学楼/图书馆/实验室 | 副本结算压力 +1 |
| 期末与体测 | final | 15-16 | 健康 | 图书馆/操场 | 图书馆探索学业+1技能+1，操场健康+2，physical 副本压力 -1 |

### 成长路线与初始属性

创建角色时选择 5 条成长路线之一，影响初始属性：

| 路线 | 学业 | 健康 | 金钱 | 社交 | 技能 | 压力 | 自律 |
|---|---|---|---|---|---|---|---|
| 考研路线 | 70 | 70 | 80 | 50 | 40 | 40 | 55 |
| 就业路线 | 60 | 70 | 80 | 55 | 50 | 30 | 50 |
| 竞赛路线 | 65 | 70 | 80 | 50 | 52 | 35 | 50 |
| 六边形路线 | 63 | 73 | 80 | 53 | 43 | 30 | 53 |
| 摆烂求生路线 | 55 | 80 | 80 | 50 | 40 | 20 | 45 |

### 路线倾向推导

`RouteTendencyService` 基于当前属性实时推导路线倾向，用于阶段加权周目标选择和 Dashboard 展示：

| 倾向 | 推导逻辑 |
|---|---|
| academic | 学业 >= 60 得分学业值，否则半值；自律 >= 50 加自律/2；压力 <= 70 加 10；压力 > 80 减 15 |
| social | 社交 >= 50 得分社交值，否则半值；加健康/3；压力 <= 60 加 5 |
| skill | 技能 >= 50 得分技能值，否则半值；学业 >= 50 加学业/4 |
| survival | 健康 >= 60 得分健康值，否则半值；减压力值；压力 <= 40 加 15；压力 <= 30 加 10 |
| balanced | `100 - (max - min) * 2`，max/min 取学业/健康/社交/技能/自律 |

如果玩家选择的成长路线匹配到对应倾向，该倾向 +10 bonus。最终取最高分。

### 结局评分系统

`EndingScoreService` 在学期结算时计算 5 维评分（read model，不存储）：

| 维度 | 公式 | 说明 |
|---|---|---|
| 学业表现 | `academic * 0.5 + discipline * 0.3 + libraryExplore * 0.2` | 学业和自律为主，图书馆探索为辅 |
| 技能成长 | `skill * 0.5 + labExplore * 0.2 + completedDungeons * 15` | 技能为主，实验室探索和副本为辅 |
| 社交影响 | `social * 0.4 + orgContribution * 5 + npcRelationCount * 5` | 社交为主，组织和 NPC 关系为辅 |
| 生存能力 | `health * 0.5 + (100 - pressure) * 0.5` | 健康和低压力并重 |
| 均衡发展 | `max(0, 100 - (max-min)*2) + completedGoals * 3` | 属性差距越小越好，完成目标加分 |

评级：≥80 优秀，≥60 良好，≥40 一般，<40 不足。

### 结局匹配逻辑

`SemesterEndingServiceImpl.matchRouteEnding` 使用固定优先级，每个结局有"过程条件 OR 评分门槛"双通道：

| 优先级 | 结局 | 过程条件 | 评分门槛 |
|---|---|---|---|
| 1 | 课设战神 | 副本 1 评价 = "课设战神" | 无（仅稀缺条件） |
| 2 | 实验室编外研究员 | 实验室探索 ≥ 40 且技能 ≥ 55 | skill 维度 ≥ 70 |
| 3 | 社团风云人物 | 组织贡献 ≥ 6 且社交 ≥ 65 | social 维度 ≥ 70 |
| 4 | 图书馆常驻民 | 图书馆探索 ≥ 40 且学业 ≥ 65 | academic 维度 ≥ 70 |
| 5 | 体测幸存者 | 副本 2 已完成 | survival ≥ 85 且健康 ≥ 80 |

无路线匹配时 fallback 到 `semester_ending` 表的属性条件匹配（如六边形工大学子、快乐摆烂人、DDL 幸存者等）。

### 探索影响系统

探索地点时会触发多种"影响来源"，在探索结果页汇总展示：

| 影响来源 | 说明 | 触发条件 |
|---|---|---|
| 周主题 | 根据当前阶段提供属性加成 | 每次探索 |
| 传闻 | 转化为属性变化或探索加成 | 地点有活跃传闻 |
| NPC 搭子 | 匹配地点的本周搭子提供稳定加成 | 已选搭子且地点匹配 |
| 搭子救场 | 高压力时搭子随机救场 | 压力 ≥ 60 且随机命中 |
| 奇遇链 | 推进探索叙事支线 | 地点有奇遇链且进度匹配 |
| NPC 遇见 | 随机遇见 NPC | 35% 概率 |

### 事件偏向机制

地图事件触发时有权重偏向：

| 来源 | 权重 |
|---|---|
| event_hint（主偏向） | +30 |
| 周主题（次级偏向，与主偏向不同时） | +15 |
| 周主题（无 event_hint 时为主偏向） | +30 |

---

## 配置说明

### application.yml 主配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/haut_survivor?...
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
  sql:
    init:
      mode: always                    # 每次启动都执行 schema.sql + data
      data-locations:                # 种子文件加载顺序（重要！）
        - classpath:data.sql
        - classpath:data-content-pack-2.sql
        - classpath:data-content-pack-3.sql
        - classpath:data-content-pack-4.sql
        - classpath:data-content-pack-6.sql
        - classpath:data-v1-stage-fill.sql

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true

server:
  port: 8080

gameplay:
  semester-weeks: 16                # 学期总周数
  weekly-action-points: 4           # 每周行动点
```

### application-docker.yml Docker 专用配置

仅覆盖 datasource，不覆盖其他配置。`SPRING_PROFILES_ACTIVE=docker` 在 Dockerfile 中设置。

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://mysql:3306/haut_survivor?...}
    username: ${SPRING_DATASOURCE_USERNAME:haut}
    password: ${SPRING_DATASOURCE_PASSWORD:haut123456}
```

---

## Seed 数据与内容包

### 加载顺序

种子文件按 `application.yml` 中 `data-locations` 的顺序加载，每个文件使用独立的 ID 段避免冲突：

| 文件 | ID 段 | 内容 |
|---|---|---|
| `data.sql` | 1-999 | 用户账号、8 个地点、基础事件（42 个）、选项（110 个）、副本（2 个）、组织（3 个）、结局（12 个）、NPC（5 个）、互动、传闻、周目标、成就 |
| `data-content-pack-2.sql` | 2000+ | 学业危机内容包事件 |
| `data-content-pack-3.sql` | 3000+ | 校园搭子内容包（NPC 互动、传闻） |
| `data-content-pack-4.sql` | 4000+ | 传闻/周主题/探索奇遇机制化 |
| `data-content-pack-6.sql` | 6000+ | 莲花街校区内容包（组织/事件/传闻/副本/NPC/地点细分/开学迎新/期末体测） |
| `data-v1-stage-fill.sql` | 7001+ | V1 阶段内容补齐（midterm/route/project/final 四阶段） |

### 虚拟 ID 段保留

| ID 段 | 用途 |
|---|---|
| 900000-900999 | NPC 专属分支互动（虚拟互动，不存数据库，由 `NpcStoryBranchCatalog` 管理） |

---

## 测试指南

### 运行测试

```powershell
# 全量测试
.\mvnw.cmd clean test

# 单个测试类
.\mvnw.cmd test -Dtest="cn.haut.survivor.service.PlayerServiceTests"

# 单个测试方法
.\mvnw.cmd test -Dtest="cn.haut.survivor.service.PlayerServiceTests#createProfileWithGrowthRoute"
```

### 测试结构

测试使用 `@SpringBootTest` + `@Transactional`（每个测试方法自动回滚，不影响数据库）。

```java
@Transactional
@SpringBootTest(properties = {
    "spring.main.web-application-type=none",    // 不启动 Tomcat
    "spring.thymeleaf.check-template-location=false",
    "debug=false",
    "logging.level.org.springframework=INFO"
})
class SomeServiceTests {
    @Autowired
    private SomeService someService;

    @Test
    void someTestCase() {
        // arrange / act / assert
    }
}
```

### 测试覆盖范围

408 个测试，覆盖：

| 类型 | 数量 | 说明 |
|---|---|---|
| 服务测试 | ~280 | 核心业务逻辑、边界条件、跨域交互 |
| 控制器测试 | ~50 | 路由、重定向、模型属性、Thymeleaf 渲染 |
| Mapper 测试 | ~10 | 数据访问基础验证 |
| 视图测试 | ~5 | 模板资源引用检查 |
| 种子测试 | ~60 | Content Pack 1-6、V1 Stage Fill 的 seed 分布和合法性 |

---

## Docker 部署

### 构建流程

```
Dockerfile (多阶段构建):
  build stage:
    maven:3.9-eclipse-temurin-17
    → COPY pom.xml → dependency:go-offline (缓存依赖)
    → COPY src → mvn -DskipTests package
  runtime stage:
    eclipse-temurin:17-jre
    → COPY --from=build target/*.jar app.jar
    → SPRING_PROFILES_ACTIVE=docker
    → java -jar app.jar
```

### 编排

```yaml
# docker-compose.yml
mysql:8.0
  - healthcheck: mysqladmin ping
  - 数据库: haut_survivor
  - 用户: haut / haut123456
  - 端口: 不暴露（避免与本地冲突）

app:
  - depends_on: mysql (service_healthy)
  - 端口: 8080:8080
```

### 操作命令

```powershell
# 构建并启动
docker compose up --build -d

# 查看日志
docker compose logs app --tail=100

# 查看状态
docker compose ps

# 停止
docker compose down

# 停止并清除数据卷
docker compose down -v
```

---

## 扩展指南

### 新增事件

1. 在对应 seed 文件中添加 `INSERT INTO event` 和 `INSERT INTO event_option`（3 个选项/事件）。
2. 使用合适的 ID 段（避免冲突）。
3. 设置 `min_week`、`max_week` 控制事件出现周次范围。
4. 设置 `min_explore_level` 控制探索度门槛（0 = 无门槛，40/60/80 = 隐藏事件）。
5. 运行 `.\mvnw.cmd clean test` 确认。

### 新增组织

1. 在 seed 文件中添加 `INSERT INTO organization`。
2. 指定 `unlock_location_id` 和 `unlock_explore_level`。
3. 运行测试确认。

### 新增副本

1. 在 seed 文件中添加 `INSERT INTO dungeon`（定义副本）、`INSERT INTO dungeon_task`（阶段）、`INSERT INTO dungeon_task_option`（阶段选项）。
2. 在 `SemesterEndingServiceImpl.buildSettlementContext` 中添加对应副本的完成判定（如需接入路线结局）。
3. 运行测试确认。

### 新增 NPC

1. 在 seed 文件中添加 `INSERT INTO npc` 和 `INSERT INTO npc_interaction`。
2. NPC ID 使用 6000+ 段。
3. 互动 ID 按规则分配：普通互动使用 NPC ID * 100 + 序号，分支互动使用 900000 + NPC ID * 100 + 序号。
4. 如需分支互动，更新 `NpcStoryBranchCatalog`。
5. 运行测试确认。

### 新增阶段内容

1. 在 `data-v1-stage-fill.sql` 或新文件中添加事件/传闻/奇遇链/周目标/成就。
2. 设置正确的 `min_week`/`max_week` 匹配目标阶段。
3. 如新增 seed 文件，在 `application.yml` 的 `data-locations` 中添加。
4. 运行测试确认。

---

## 编码规范与约束

### 架构约束

- 不重写整个系统，保持小步迭代。
- 前端继续复用 `game-shell`、`game-hud`、`game-dock`、`vn-result`、`sticky-note__btn` 等现有 UI 组件。
- 不把玩家端 UI 改回后台管理风格。
- 数据库变更保持轻量，优先复用已有表。

### 代码规范

- Service 接口定义在 `service/` 包，实现在 `service/impl/` 包。
- 依赖注入使用构造器注入（不使用 `@Autowired` 字段注入，Service 实现类构造器）。
- 实体类使用 MyBatis-Plus 注解（`@TableName`、`@TableId`）。
- 属性值范围统一使用 `[0, 100]` 钳制。
- 每次修改后必须运行 `.\mvnw.cmd clean test`。
- 如果修改页面或控制器，必须做 HTTP 冒烟。

### 内容扩展规范

- 新 seed 使用独立 ID 段，避免与已有数据冲突。
- 不删除已有 seed 数据或测试。
- 事件必须包含 3 个选项，每个选项有完整的属性变化和结果文案。
- 副本必须包含 3 个以上阶段，每阶段 3 个选项。
