# HAUT Survivor 项目学习文档

> 基于 Java 的高校校园生存挑战与成长管理系统——以河南工业大学莲花街校区为例

---

## 一、项目概述

### 1.1 项目简介

**HAUT Survivor**（河南工业大学幸存者）是一款以河南工业大学莲花街校区为背景的**周回合制大学生模拟器**。玩家扮演一名大学生，在 16 周的学期中做出各种选择——上课、摸鱼、社交、参加社团、挑战副本、结识 NPC——最终走向属于自己的学期结局。

核心游戏流程：

```
注册登录 → 创建角色 → 每周消耗行动点探索/社交/学习 → 周总结 → 16 周后学期结局
```

### 1.2 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 3.3.5 |
| 编程语言 | Java | 17 |
| ORM 框架 | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8 |
| 模板引擎 | Thymeleaf | — |
| 前端 | Bootstrap 5 + 自定义 CSS | — |
| 构建工具 | Maven Wrapper | — |
| 部署方式 | Docker Compose（可选） | — |

### 1.3 数据规模

| 内容 | 数量 |
|---|---|
| 数据库表 | 32 张 |
| 校园地点 | 8 个 |
| 随机事件（含隐藏） | 80+ |
| 事件选项 | 240+ |
| 副本 | 5 个（15+ 阶段） |
| 组织 | 8 个 |
| 学期结局 | 12 种 |
| 成长路线 | 5 条 |
| NPC | 8 个 |
| 机制型传闻 | 40+ |
| 探索奇遇链 | 10+ 条链 / 30+ 阶段 |
| 周目标 | 30+ |
| 成就 | 40+ |
| Thymeleaf 页面模板 | 23 个 |
| 单元测试 | 408 个 |

---

## 二、系统架构

### 2.1 项目结构

```
src/main/java/cn/haut/survivor/
  HautSurvivorApplication.java    # Spring Boot 启动类
  config/                         # 配置类
    WebConfig.java                # Web 配置（拦截器注册）
    LoginInterceptor.java         # 登录拦截器（未登录重定向）
  controller/                     # 页面控制器
    AuthController.java           # 认证（登录/注册/退出）
    DashboardController.java      # 仪表盘 + 角色创建 + 周目标
    MapController.java            # 校园地图 + 事件触发
    ExplorationController.java    # 探索系统
    NpcController.java            # NPC 互动
    OrganizationController.java   # 组织系统
    DungeonController.java        # 副本系统
    WeekSummaryController.java    # 周总结 + 历史周报
    SemesterEndingController.java # 学期结局
    TaskController.java           # 任务管理
    admin/
      AdminEventController.java   # 管理员事件管理
  domain/
    entity/                       # 实体类（对应数据库表）
    enums/                        # 枚举类型
  mapper/                         # MyBatis-Plus Mapper 接口
  service/                        # 业务接口
  service/impl/                   # 业务实现

src/main/resources/
  schema.sql                      # 表结构（启动自动重建）
  data.sql                        # 基础种子数据
  data-content-pack-2.sql         # 数据库课设答辩夜副本
  data-content-pack-3.sql         # 内容包 3
  data-content-pack-4.sql         # 内容包 4
  data-content-pack-6.sql         # 小组作业/体测副本、NPC、传闻、奇遇链、周目标
  data-v1-stage-fill.sql          # V1 阶段内容补齐
  application.yml                 # 主配置
  application-docker.yml          # Docker 专用配置
  templates/                      # Thymeleaf 模板（23 个 HTML 页面）
  static/css/                     # 自定义 CSS（游戏主题 + 响应式）
  static/images/                  # 地图图片等静态资源
```

### 2.2 MVC 架构

项目采用经典的 Spring Boot MVC 分层架构：

```
浏览器请求 → Controller（接收请求、调用 Service）→ Service（业务逻辑）→ Mapper（MyBatis-Plus 数据访问）→ MySQL
                                                                    ↓
                                                              Thymeleaf 模板渲染 → HTML 响应
```

- **Controller 层**：11 个控制器，负责请求路由和页面跳转
- **Service 层**：20+ 个 Service，封装核心业务逻辑
- **Mapper 层**：32 个 Mapper 接口，继承 MyBatis-Plus 的 BaseMapper，无需手写 SQL 即可完成 CRUD
- **Entity 层**：与数据库表一一对应的实体类
- **模板层**：23 个 Thymeleaf HTML 模板，服务端渲染

### 2.3 URL 路由总览

| 模块 | URL 路径 | 控制器 | 页面模板 |
|---|---|---|---|
| 登录 | GET/POST `/login` | AuthController | auth/login |
| 注册 | GET/POST `/register` | AuthController | auth/register |
| 退出 | POST `/logout` | AuthController | 重定向登录 |
| 角色创建 | GET/POST `/player/create` | DashboardController | player/create |
| 仪表盘 | GET `/dashboard` | DashboardController | dashboard/index |
| 校园地图 | GET `/map` | MapController | map/index |
| 事件触发 | GET `/map/location/{id}/event` | MapController | map/event |
| 事件选择 | POST `/map/event/{id}/option/{id}` | MapController | map/event |
| 探索 | GET `/exploration` | ExplorationController | exploration/index |
| 执行探索 | POST `/exploration/{locationId}` | ExplorationController | exploration/result |
| NPC 详情 | GET `/npcs/{npcId}` | NpcController | npc/detail |
| NPC 互动 | POST `/npcs/{id}/interactions/{id}` | NpcController | npc/result |
| 选择搭子 | POST `/npcs/{id}/buddy` | NpcController | 重定向 |
| 组织广场 | GET `/organizations` | OrganizationController | organization/index |
| 组织详情 | GET `/organizations/{id}` | OrganizationController | organization/detail |
| 副本列表 | GET `/dungeons` | DungeonController | dungeon/index |
| 副本封面 | GET `/dungeons/{id}` | DungeonController | dungeon/detail |
| 副本进行 | GET `/dungeons/{id}/play` | DungeonController | dungeon/play |
| 副本选择 | POST `/dungeons/{id}/task/{id}/option/{id}` | DungeonController | dungeon/result |
| 周总结 | GET `/week/summary` | WeekSummaryController | week/summary |
| 历史周报 | GET `/week/history` | WeekSummaryController | week/history |
| 推进周次 | POST `/week/advance` | WeekSummaryController | 重定向 |
| 学期结局 | GET `/ending` | SemesterEndingController | ending/index |
| 结局结算 | POST `/ending/settle` | SemesterEndingController | 重定向 |
| 重开 | POST `/ending/restart` | SemesterEndingController | 重定向 |
| 任务管理 | GET/POST `/tasks` | TaskController | task/index |
| 事件管理 | GET/POST `/admin/events` | AdminEventController | admin/* |

---

## 三、功能模块详解

### 3.1 用户认证模块

**路由**：`/login`、`/register`、`/logout`

**功能**：
- 用户注册（用户名、昵称、密码），注册后自动登录
- 用户登录，Session 认证
- 退出登录，清除 Session
- 未登录访问受保护页面自动重定向到登录页（`LoginInterceptor`）

**页面**：
- **登录页**：居中卡片布局，标题"HAUT Survivor"，用户名/密码输入框，登录按钮，注册链接
- **注册页**：同风格卡片，用户名/昵称/密码输入框，"注册并进入"按钮

**技术要点**：
- 通过 `LoginInterceptor` 拦截器统一处理未登录访问，排除 `/login`、`/register`、静态资源等路径
- 用户角色通过数据库 `role` 字段区分（`student` / `admin`）
- 密码明文存储（Demo 阶段简化处理）

### 3.2 角色创建模块

**路由**：GET/POST `/player/create`

**功能**：首次登录后进入角色创建，填写角色信息并选择成长路线，系统初始化 7 维属性。

**页面**：
- 居中卡片表单，包含：角色名输入、年级选择、专业类型选择、成长路线下拉框（5 个选项）、"进入校园"提交按钮

**成长路线对比**：

| 路线 | 初始优势 | 适合玩法 |
|---|---|---|
| 考研路线 | 学业 70，自律 55 | 图书馆学习，探索学术路线 |
| 就业路线 | 社交 55，技能 50 | 参加组织、挑战副本、积累技能 |
| 竞赛路线 | 技能 52，学业 65 | 去实验室，刷副本拿高分 |
| 六边形路线 | 全部 53+ | 均衡发展 |
| 摆烂求生路线 | 压力 20，健康 80 | 休闲体验 |

**技术要点**：
- `PlayerService.createProfile()` 根据路线设置不同的初始属性值
- 创建 `player_profile` 和 `player_attribute` 两条记录
- 已有角色时自动跳过创建页，直接进入仪表盘

### 3.3 仪表盘模块（寝室主界面）

**路由**：GET `/dashboard`

**功能**：玩家的"大本营"，集中展示所有游戏状态，提供快捷导航。

**页面布局**（从上到下）：

1. **游戏状态栏（HUD）**：固定顶部，显示当前周次、行动点（AP）、压力预警、退出按钮
2. **角色信息卡**：角色名、称号、周次标签、成长路线、等级
3. **学期阶段进度条**：6 阶段可视化，高亮当前阶段
4. **路线倾向**：系统根据属性实时推导的成长方向
5. **压力预警横幅**：压力 ≥ 60 黄色、≥ 75 红色
6. **周主题卡**：当前阶段主题名称、描述、行动建议
7. **周目标区**：未选目标时展示 3 个候选目标；已选时展示进度条和领取奖励按钮
8. **传闻栏**：2-3 条本周传闻卡片，带稀有度标记
9. **手机主屏图标网格**：4 列图标入口（去校园/踩点/挑战/社团/待办/学期回忆）
10. **属性面板**：7 维属性条，彩色渐变填充（学业=蓝/健康=绿/金钱=金/社交=粉/技能=紫/压力=红/自律=青）
11. **快捷便签**：地图、探索、副本、组织快速链接
12. **预警提示**：低健康/低金钱/低学业/高压/低行动点警告
13. **成就展示架**：最近解锁的 5 个成就
14. **熟人列表**：已认识 NPC 横向滚动条，显示头像、名字、熟悉度、关系阶段、搭子标记
15. **底部导航栏（Dock）**：寝室/去校园/挑战/社团

**技术要点**：
- 仪表盘是信息密度最高的页面，一个页面聚合了 10+ 个数据来源
- 周目标候选由 `WeeklyGoalService` 根据当前阶段和路线倾向智能推荐
- 传闻由 `RumorService` 基于用户 ID 和周次稳定抽取（同一周刷新页面结果不变）

### 3.4 校园地图与事件模块

**路由**：`/map`、`/map/location/{id}/event`、`/map/event/{id}/option/{id}`

**功能**：展示莲花街校区地图，点击地点触发随机事件，视觉小说式互动选择。

**地图页面**：
- 顶部：标题"莲花街校区行动地图"、角色名、剩余行动点
- 周主题卡（精简版）、传闻栏
- **真实校区地图**：莲花街校区地图图片，8 个可点击热点（绝对定位在地图对应位置）
- **地点卡片网格**：2 列布局，每张卡片显示 emoji 图标、地点名、描述、传闻标记、探索度进度条、AP 消耗、"前往"按钮

**8 个校园地点**：

| 地点 | 特色 | 解锁内容 |
|---|---|---|
| 教学楼 | 上课、点名、课堂事件 | 基础学习事件 |
| 图书馆 | 自习、抢座、期末复习 | 隐藏事件（探索度 40/60/80）、组织"计算机协会" |
| 宿舍 | 休息、赶 DDL、室友互动 | 生活事件 |
| 食堂 | 吃饭、控制预算 | 生活事件、社交事件 |
| 操场 | 运动、体测 | 健康事件、副本"体测生存挑战" |
| 实验室 | 写代码、做实验 | 隐藏事件、组织"实验室项目组" |
| 社团活动区 | 招新、活动 | 社交事件、多个组织 |
| 快递站 | 取快递、排队 | 生活事件 |

**事件页面（视觉小说式演出）**：

- **事件场景**：深色氛围背景（根据地点类型：实验室蓝光/宿舍暖光/教室白光等），地点/氛围标签，事件标题，叙事文案，玩家状态条（技能/压力/健康/自律），**2-3 个选项按钮**（按风险等级着色：绿色=低风险/黄色=中风险/红色=高风险），离开按钮
- **事件结果**：结果标题、叙事文案、属性变化标记（绿色上升/红色下降），完整属性概览

**技术要点**：
- `EventService.triggerRandomEvent()` 根据地点、周次范围、探索度阈值、启用状态加权随机选取事件
- 传闻效果 `event_hint` 可改变事件类型权重（+30 偏向对应类型）
- 探索度达到 40/60/80 阈值解锁隐藏事件

### 3.5 探索模块

**路由**：GET `/exploration`、POST `/exploration/{locationId}`

**功能**：消耗 1 行动点探索地点，提升探索度，获得随机结果。

**页面**：
- **探索主页**：8 个地点卡片，每个显示探索度进度条和解锁里程碑（20%/40%/60%/80%/100%），"踩点探索"按钮
- **探索结果页**：结果类型图标、描述文案、属性变化、影响来源说明

**12 种探索结果**：

| 类型 | 说明 |
|---|---|
| 学业发现 | 学业 +1~+3 |
| 技能提升 | 技能 +1~+3 |
| 社交机会 | 社交 +1~+2 |
| 健康恢复 | 健康 +1~+2 |
| 捡到钱 | 金钱 +1~+3 |
| 压力事件 | 压力 +1~+3 |
| 发现传闻 | 获得可利用传闻 |
| 遇见 NPC | 随机遇到一个 NPC |
| 奇遇触发 | 推进探索奇遇链 |
| 搭子加成 | 本周搭子提供额外属性加成 |
| 搭子救场 | 高压力时搭子出手相救 |
| 探索度提升 | 探索度 +5~+15 |

**技术要点**：
- `ExplorationService.explore()` 消耗 1 AP，随机增加 5-15% 探索度
- 探索度门槛控制隐藏事件和组织解锁
- 探索奇遇链（`exploration_story_chain`）提供多步骤叙事支线

### 3.6 NPC 互动模块

**路由**：GET `/npcs/{npcId}`、POST `/npcs/{id}/interactions/{id}`、POST `/npcs/{id}/buddy`

**功能**：与 8 个校园 NPC 建立关系，主动互动消耗行动点，选择搭子获得周加成。

**8 个 NPC**：

| NPC | 地点 | 性格 | 特色 |
|---|---|---|---|
| 阿杰（室友） | 宿舍 | 随和 | 基础互动 + 莲花街互动 |
| 林然（学霸） | 教学楼/图书馆 | 内向 | 学习相关互动 |
| 周予（社牛） | 社团活动区/食堂 | 外向 | 社交相关互动 |
| 老郑（师兄） | 实验室 | 稳重 | 技术相关互动 |
| 小马（运动搭子） | 操场 | 活泼 | 健康相关互动 |
| 富少 | 实验室 | 话多 | 莲花街互动 |
| 小鱼 | 图书馆 | 安静 | 莲花街互动 |
| 柳如烟 | 社团活动区 | 神秘 | 莲花街互动 |

**NPC 详情页**：
- 头像图标、NPC 类型、名字、描述
- 关系阶段：认识 → 熟人 → 搭子 → 铁搭子（由熟悉度 0-19/20-49/50-79/80+ 决定）
- 熟悉度进度条
- "设为本周搭子"按钮（熟悉度 ≥ 50）
- 互动选项列表（每个选项显示名称、描述、所需熟悉度、"互动"按钮）

**搭子机制**：
- 每周选择 1 名熟悉度 ≥ 50 的 NPC 作为搭子
- 在搭子归属地点探索时获得稳定属性加成
- 高压力（≥ 60）时概率触发"搭子救场"（压力 -2）

**技术要点**：
- 探索时 35% 概率随机遇见地点关联 NPC（`NpcService.maybeMeetNpc()`）
- 每个 NPC 每周最多主动互动 1 次（`user_npc_weekly_action` 记录）
- NPC 故事进度（`user_npc_story_progress`）解锁专属分支互动

### 3.7 组织系统模块

**路由**：`/organizations`、`/organizations/{id}`

**功能**：8 个校园组织，发现→加入→活动→晋升的完整流程。

**8 个组织**：

| 组织 | 类型 | 解锁地点 | 探索度门槛 | 推荐属性 |
|---|---|---|---|---|
| 学生会 | 学生组织 | 社团活动区 | 0 | 社交 |
| 实验室项目组 | 技术社团 | 实验室 | 10 | 技能 |
| 篮球社 | 兴趣社团 | 操场 | 5 | 健康 |
| 计算机协会 | 技术社团 | 实验室 | 15 | 技能 |
| 信息学院学生会 | 学生组织 | 社团活动区 | 20 | 社交 |
| 信息学院辩论队 | 竞赛队 | 社团活动区 | 25 | 社交 |
| 轮滑社 | 兴趣社团 | 操场 | 10 | 健康 |
| 校合唱团 | 文艺社团 | 社团活动区 | 20 | 社交 |

**组织广场页**：每个组织卡片显示类型图标、名称、类型标签、描述、推荐属性、状态标记（未发现/已发现/职位+贡献+声望）

**组织详情页**（三种状态）：
- **未发现**：锁定图标 + 探索度要求 + "发现"按钮
- **已发现**：状态 + "申请加入"按钮（需社交 ≥ 40）
- **已加入**：职位名、贡献值、声望值、"参加本周活动"按钮（消耗 1 AP）

**技术要点**：
- `OrganizationService.discover()` 检查探索度是否达标
- `OrganizationService.join()` 检查社交属性门槛
- `OrganizationService.attendActivity()` 消耗 1 AP，增加贡献和声望，应用属性变化
- 贡献值 ≥ 10 自动晋升为"部长"（核心成员）

### 3.8 副本挑战模块

**路由**：`/dungeons`、`/dungeons/{id}`、`/dungeons/{id}/play`、`/dungeons/{id}/task/{id}/option/{id}` 等

**功能**：4 个多阶段副本挑战，含小游戏（数据库拼图、Bug 定位）。

**5 个副本**：

| 副本 | 类型 | 阶段数 | 特色 |
|---|---|---|---|
| Java 课设：DDL 前夜 | 课程 | 3 | 需求风暴 → 数据库拼图小游戏 → Bug 定位小游戏 |
| 体测生存挑战 | 体能 | 3 | 1000 米配速 → 引体向上排队 → 坐位体前屈补救 |
| 数据库课设答辩夜 | DDL | 多阶段 | 需求梳理 → ER 图 → SQL 报错 → 答辩追问 |
| 小组作业 | 团队 | 5 | 公布名单 → 线上开会 → 各自开荒 → 整合攻坚 → 卡点提交 |
| 体测生存挑战（莲花街版） | 体能 | 3 | 莲花街校区版体测 |

**页面流程**：
1. **副本列表**（挑战海报墙）：4 张海报卡片，显示主题图标、名称、描述、难度、预估时间、奖励称号
2. **副本封面**：类型、名称、描述、难度、奖励、阶段路线可视化（编号步骤 + 任务名）、"开始挑战"按钮
3. **副本进行**：阶段元信息（副本名、阶段号、计时器）、任务标题、场景叙事、目标文本
4. **阶段结果**：评价标题、结果叙事、属性变化、分数汇总、下一阶段入口或最终评价

**小游戏**：
- **数据库拼图**（`db_link`）：展示表名，勾选正确的表关系，倒计时（技能越高时间越长）
- **Bug 定位**（`bug_hunt`）：3 道题（从 5 题随机抽取），4 选 1 找出 Bug，技能影响可用时间

**技术要点**：
- `DungeonService.startOrResumeDungeon()` 创建或恢复副本进度
- `DungeonService.chooseMinigameRelations()` 验证数据库关系选择，按正确数和时间计分
- `DungeonService.chooseBugHunt()` 评分 Bug 定位答案
- 副本完成后检查风险标记（scope_sprawl/schema_mist/bug_avalanche）生成最终评价

### 3.9 周总结与推进模块

**路由**：GET `/week/summary`、GET `/week/history`、POST `/week/advance`

**功能**：每周行动结束后查看周总结，推进到下一周；查看历史周报。

**周总结页**：
- 英雄区：周次、主题图标/名称/描述、阶段提示、路线倾向、评级标签、总结文案
- 周目标卡：完成状态和奖励领取
- 属性快照：本周属性变化概览
- 影响回放列表：该周触发的各种影响来源（传闻/周主题/搭子/奇遇等）
- NPC 熟人区：已认识 NPC 列表
- 成就区：新解锁的成就
- 操作按钮："进入下一周"/"回寝室"/"查看历史周报"

**历史周报页**：
- 学期档案卡（顶部）：成长关键词、影响统计、关键周、关键关系
- 每周影响回放：按周倒序，展示该周触发的传闻、周主题、搭子、奇遇等

**周结算规则**（推进时自动执行）：
1. 压力检查
2. 周次 +1，AP 恢复满（4 点）
3. 学期阶段更新
4. 高压惩罚：压力 > 80 → 健康 -3
5. 低血惩罚：健康 < 20 → 下周最大 AP -1
6. 自然减压：压力 -5

**技术要点**：
- `WeekSummaryService.buildCurrentWeekSummary()` 聚合 10+ 个数据来源
- `PlayerService.advanceWeek()` 执行周结算逻辑
- 第 16 周推进后触发学期结局

### 3.10 学期结局模块

**路由**：GET `/ending`、POST `/ending/settle`、POST `/ending/restart`

**功能**：16 周结束后结算学期，展示结局、评分和成长画像。

**结局页**（三种状态）：
- **学期未结束**：提示信息 + 返回仪表盘链接
- **已结束未结算**：属性快照 + "揭晓你的结局"按钮
- **已结算**：结局图标、类型、名称、描述；路线评分卡片（5 维评分条 + 关键证据列表 + 学期总结文案）；成长画像（关键词、描述、关键关系、关键周、主要来源）；"再来一学期"按钮

**12 种结局**（7 基础 + 5 路线隐藏）：

| 结局 | 触发条件 |
|---|---|
| 六边形工大学子 | 各属性均衡（全部 ≥ 50，压力 < 60） |
| 快乐摆烂人 | 压力低但自律和学业低 |
| DDL 幸存者 | 高压力但健康尚可 |
| 工大过客 | 默认兜底结局 |
| 课设战神 | 完成课设副本且获最高评价（最稀有） |
| 实验室编外研究员 | 高技能或技能评分 ≥ 70 |
| 社团风云人物 | 高社交或社交评分 ≥ 70 |
| 图书馆常驻民 | 高学业或学业评分 ≥ 70 |
| 体测幸存者 | 完成体测副本或生存评分 ≥ 85 且健康 ≥ 80 |

**5 维结局评分**（0-100 分）：

| 维度 | 计算方式 |
|---|---|
| 学业表现 | 学业×0.5 + 自律×0.3 + 图书馆探索度×0.2 |
| 技能成长 | 技能×0.5 + 实验室探索度×0.2 + 完成副本数×15 |
| 社交影响 | 社交×0.4 + 组织贡献×5 + NPC 数量×5 |
| 生存能力 | 健康×0.5 + (100-压力)×0.5 |
| 均衡发展 | 100 - (最高-最低)×2 + 完成目标数×3 |

**技术要点**：
- `SemesterEndingService.settleSemester()` 按优先级匹配结局
- `EndingScoreService.buildScoreReport()` 计算 5 维评分并生成证据文本
- `RouteTendencyService.deriveTendency()` 基于属性推导路线倾向，选择路线匹配时 +10 bonus
- 重开新学期保留结局历史记录（`user_semester_ending`）

### 3.11 任务管理模块

**路由**：GET/POST `/tasks`

**功能**：创建、查看、完成任务，获得经验奖励。

**页面**：标准 Bootstrap 风格（非游戏主题），包含创建任务表单（名称/类型/难度/截止日期/描述）和任务列表表格。

### 3.12 管理员事件管理模块

**路由**：`/admin/events`、`/admin/events/new`、`/admin/events/{id}/edit`

**功能**：管理员对事件进行增删改查。

**页面**：标准 Bootstrap 风格，事件列表表格（名称/类型/地点/概率/周范围/状态/操作）和事件编辑表单。

---

## 四、数据库设计

### 4.1 核心实体关系

```
user (1) ──→ (1) player_profile ──→ (1) player_attribute
  │
  ├──→ (*) task
  ├──→ (*) event_record ──→ event ──→ campus_location
  │                          │
  │                          └──→ (*) event_option
  │
  ├──→ (*) user_dungeon_record ──→ dungeon
  │        │
  │        └──→ (*) user_dungeon_task_record ──→ dungeon_task ──→ (*) dungeon_task_option
  │
  ├──→ (*) user_organization ──→ organization ──→ campus_location
  ├──→ (*) user_location_exploration ──→ campus_location
  ├──→ (*) user_npc_relation ──→ npc ──→ campus_location
  ├──→ (*) user_npc_weekly_action
  ├──→ (*) user_npc_story_progress
  ├──→ (*) user_weekly_goal ──→ weekly_goal
  ├──→ (*) user_achievement ──→ achievement
  ├──→ (*) user_week_summary
  ├──→ (*) user_influence_log
  ├──→ (*) user_semester_ending ──→ semester_ending
  └──→ (*) exploration_story_progress
```

### 4.2 数据库表清单（32 张）

| 分类 | 表名 | 说明 |
|---|---|---|
| 用户 | `user` | 用户账号（用户名、密码、角色） |
| 玩家 | `player_profile` | 角色档案（周次、AP、路线、等级） |
| 玩家 | `player_attribute` | 7 维属性值 |
| 地点 | `campus_location` | 8 个校园地点 |
| 事件 | `event` | 80+ 随机事件 |
| 事件 | `event_option` | 240+ 事件选项 |
| 事件 | `event_record` | 事件触发记录 |
| 任务 | `task` | 用户自定义任务 |
| 副本 | `dungeon` | 5 个副本 |
| 副本 | `dungeon_task` | 副本阶段任务 |
| 副本 | `dungeon_task_option` | 阶段选项 |
| 副本 | `user_dungeon_record` | 用户副本记录 |
| 副本 | `user_dungeon_task_record` | 用户阶段完成记录 |
| 组织 | `organization` | 8 个组织 |
| 组织 | `user_organization` | 用户组织关系 |
| 探索 | `user_location_exploration` | 探索度记录 |
| 探索 | `exploration_story_chain` | 探索奇遇链 |
| 探索 | `exploration_story_progress` | 奇遇进度 |
| NPC | `npc` | 8 个 NPC |
| NPC | `npc_interaction` | NPC 互动选项 |
| NPC | `user_npc_relation` | 用户-NPC 关系 |
| NPC | `user_npc_weekly_action` | 每周互动记录 |
| NPC | `user_npc_story_progress` | NPC 故事进度 |
| 周目标 | `weekly_goal` | 30+ 周目标模板 |
| 周目标 | `user_weekly_goal` | 用户周目标记录 |
| 成就 | `achievement` | 40+ 成就 |
| 成就 | `user_achievement` | 用户成就记录 |
| 周报 | `user_week_summary` | 周总结记录 |
| 影响 | `user_influence_log` | 影响来源日志 |
| 结局 | `semester_ending` | 12 种结局定义 |
| 结局 | `user_semester_ending` | 用户结局记录 |
| 传闻 | `rumor` | 机制型传闻 |

### 4.3 数据初始化

项目通过 6 个 SQL 文件初始化数据：

| 文件 | 插入数 | 内容 |
|---|---|---|
| data.sql | 23 | 基础地点、事件、副本、组织、NPC、成就、结局 |
| data-content-pack-2.sql | 11 | 数据库课设答辩夜副本 |
| data-content-pack-3.sql | 4 | 内容包 3 |
| data-content-pack-4.sql | 4 | 内容包 4 |
| data-content-pack-6.sql | 29 | 小组作业副本、体测副本、NPC、传闻、奇遇链、周目标 |
| data-v1-stage-fill.sql | 15 | V1 阶段内容补齐 |

启动时 `schema.sql` 自动重建表结构，然后按顺序加载种子数据。

---

## 五、游戏机制详解

### 5.1 属性系统

7 维属性，范围 0-100：

| 属性 | 含义 | 影响 |
|---|---|---|
| 学业 | 学习能力和成绩 | 学习事件结果、路线倾向、结局匹配 |
| 健康 | 身体状况 | 压力 > 80 时每周 -3；< 20 时下周 AP -1 |
| 金钱 | 经济状况 | 部分事件选项可用性 |
| 社交 | 人际关系 | 组织加入、社交事件、路线倾向 |
| 技能 | 实践能力 | 副本小游戏时间、路线倾向、结局匹配 |
| 压力 | 心理压力（越高越差） | 每周自然 -5；> 80 损害健康 |
| 自律 | 自我管理 | 学业评分、路线倾向 |

### 5.2 学期阶段

16 周学期分为 6 个阶段：

| 阶段 | 周数 | 主题 | 特色机制 |
|---|---|---|---|
| 开学适应 | 1-2 | 新生适应期 | NPC 熟悉度 +1，组织加入门槛降低 |
| 节奏建立 | 3-5 | 社团活跃期 | 组织活动贡献和声望 +1 |
| 期中波动 | 6-8 | 考试逼近 | 教学楼/图书馆/实验室学业加成 |
| 路线分化 | 9-11 | 成长分化 | 图书馆/实验室/惟学楼技能加成 |
| 项目与 DDL | 12-14 | 课设高压期 | 副本结算压力额外 +1 |
| 期末与体测 | 15-16 | 冲刺期 | 图书馆/操场收益增强 |

### 5.3 行动点系统

每周 4 个行动点，所有行动消耗 AP：

| 行动 | AP 消耗 |
|---|---|
| 探索地点 | 1 |
| 触发地图事件 | 0（需先到达） |
| 主动 NPC 互动 | 1 |
| 参加组织活动 | 1 |
| 副本阶段 | 0（进入后不额外消耗） |

### 5.4 传闻系统

每周系统基于用户 ID 和周次稳定抽取 2-3 条传闻：

| 类型 | 效果 |
|---|---|
| explore_bonus | 探索度加成 |
| npc_boost | NPC 遇见概率 +10% |
| event_hint | 事件偏向（+30 权重） |
| safe_zone | 减少负面结果 |
| attr_bonus | 属性加成 |

### 5.5 路线倾向

系统根据当前属性实时推导 5 种路线倾向：学业路线、社交路线、技能路线、稳定生活路线、均衡路线。倾向影响周目标推荐。

---

## 六、UI 设计

### 6.1 双主题设计

项目采用双主题 UI 范式：

- **游戏页面**：深色游戏主题（`game-shell` 类），深蓝背景 + 金琥珀色（#fbbf24）强调色，半透明卡片，emoji 驱动图标，固定底部导航栏
- **管理/任务页面**：标准 Bootstrap 浅色主题

### 6.2 游戏主题视觉元素

- **游戏 HUD**：固定顶部栏，品牌标识 + 状态芯片（周次/AP/压力）
- **游戏 Dock**：移动端固定底部 4 项导航（寝室/去校园/挑战/社团），桌面端静态内联
- **视觉小说舞台**（`.vn-stage`）：全屏氛围背景，按地点类型分层 CSS 渐变
- **属性条**：7 色渐变填充
- **动画**：页面淡入、图标弹出（交错延迟）、压力脉冲、结局揭晓缩放、属性条交错淡入
- **校区地图**：真实照片 + 绝对定位热点标记

### 6.3 响应式设计

- 网格从 4 列 → 2 列 → 1 列自适应
- Dock 在 993px 断点处切换行为
- 移动端优先设计

---

## 七、项目运行

### 7.1 环境要求

- JDK 17+
- MySQL 8
- Maven（项目自带 Maven Wrapper）

### 7.2 本地运行

1. 创建数据库：
   ```sql
   CREATE DATABASE IF NOT EXISTS haut_survivor DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 确认 `application.yml` 中 MySQL 账号密码（默认 `root / 123456`）

3. 启动：
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. 访问：http://localhost:8080

### 7.3 Docker 运行

```powershell
docker compose up --build
```

访问：http://localhost:8080

### 7.4 默认账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 普通玩家 | student | student123 |
| 管理员 | admin | admin123 |

### 7.5 演示路线（5-10 分钟）

```
登录 student/student123
  → 创建角色（选择成长路线）
  → 仪表盘查看周次、阶段进度、行动点、属性、路线倾向
  → 探索校园（消耗 AP，提升探索度，随机发现隐藏事件）
  → 校园地图触发事件（80+ 事件，部分需探索度解锁）
  → 加入组织（8 个组织，需探索度门槛）
  → 挑战副本（课设/体测/小组作业，含小游戏）
  → NPC 互动（8 个 NPC，搭子提供加成）
  → 推进周次（16 周学期，6 阶段各含主题）
  → 学期结局（12 种结局，含路线评分卡片和成长画像）
  → 重开新学期
```

### 7.6 运行测试

```powershell
.\mvnw.cmd clean test
```

408 个测试，0 失败。
