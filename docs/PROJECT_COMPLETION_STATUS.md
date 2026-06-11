# 项目完成状态

最后更新：2026-06-11

## 总体状态

HAUT Survivor 已完成一个功能完整的**周回合制大学生模拟器 Demo**，核心循环闭合：

```
创建角色 → 探索/行动/组织 → 推周 → 结局结算 → 重开新学期
```

333 个测试全绿 三批重构完成，可玩性内容包 1-6 已上线，并完成 CP4.1/CP4.2/CP4.3/CP4.4/CP4.5/CP4.6/CP4.7 机制补强、CP4.8 影响历史日志、CP4.9 历史周报、CP5 A/B 均衡前两批、CP5 UI 收尾和 CP6 莲花街校区内容接入。当前 Demo 已从”基础周回合模拟器”推进到”有周目标、成就、周总结、学业危机内容、NPC 搭子互动、传闻/周主题机制化、探索奇遇链、搭子救场反馈、跨周影响复盘、NPC 关系成长、NPC 专属分支互动、学期档案、真实校园地图和移动端游戏化界面复核”的可玩版本。

## 已完成功能

### 基础框架 ✅
- Spring Boot 3.3.5 + Java 17 项目结构
- MyBatis-Plus + MySQL 持久层
- Thymeleaf 页面 + 自定义 CSS
- 注册登录 + Session 认证 + 管理员权限
- Schema 启动自动重建 + 种子数据

### 角色系统 ✅
- 角色创建（5 条成长路线影响初始属性）
- 7 维属性面板（学业/健康/金钱/社交/技能/压力/自律）
- 等级和经验值

### 周回合制 ✅
- 4 周压缩 Demo 学期
- 每周 4 行动点，所有行动消耗行动点
- 周结算：压力自然衰减、健康惩罚、行动点上限调整
- 学期阶段标签（开学适应期/期中节奏期/DDL 高压期）

### 探索系统 ✅
- 8 个地点探索度 0-100
- 探索行动消耗 1 AP，随机增加探索度
- 随机探索结果（发现/社交/技能/挫折/传说等 12 种类型）
- 按阈值解锁隐藏事件（40/60/80）
- 组织发现需要对应地点探索度达标

### 事件系统 ✅
- 42 个事件（26 普通 + 16 隐藏）
- 110 个选项，风险等级标注（low/medium/high）
- 按地点、周次、探索度三层过滤
- 属性 + 经验结算，结果页展示变化
- 视觉小说式事件演出（场景背景、氛围标签、风险色条选择按钮）

### 组织系统 ✅
- 3 个组织（学生会/实验室项目组/篮球社）
- 发现（需探索度）→ 加入（需社交值）→ 活动（消耗 AP）→ 晋升
- 贡献值 + 声望 + 职位晋升
- 社团招新周参加组织活动时额外获得贡献 +1、声望 +1，并在活动反馈文案中说明来源
- 社团招新广场 UI、组织活动场景 UI

### 副本系统 ✅
- 2 个完整副本：
  - Java 课设：DDL 前夜（需求风暴 → 数据库拼图 → Bug 定位）
  - 体测生存挑战（1000米 → 引体向上 → 坐位体前屈）
- 多副本架构，动态 URL 路径
- 过程标签、属性判定、最终评价
- 数据库拼图小游戏（关系选择 + 计时）
- Bug 定位小游戏（5 题随机抽 3 + 计时 + 技能加成）
- DDL 高压周副本结算压力额外 +1，覆盖普通阶段选项和小游戏动态结算
- 挑战海报墙 UI、副本封面页 UI、阶段战报 UI

### 结局系统 ✅
- 12 种结局（7 基础 + 5 隐藏趣味）
- 按属性条件优先级匹配
- 结算属性快照 + 结局图鉴历史
- 重开新学期（保留路线，重置属性/探索/组织）
- 学期回忆报告 UI、结局图鉴墙

### UI 2.0 游戏化界面 ✅

三批重构完成，玩家端不再是"后台管理系统"风格：

**第一批 — 游戏外壳 + 寝室主界面**
- game-shell、game-hud、game-dock 统一游戏视觉系统
- dashboard 改为寝室/手机主界面（台灯氛围、便利贴、手机桌面入口）
- 属性条、压力预警、周推进控制
- 桌面端 Dock 改为 static 流式布局（不遮挡内容）

**第二批 — 校园行动 + 事件 + 探索**
- map 改为莲花街校区行动地图（可点击校园节点）
- 事件页改为视觉小说式选择（场景背景、氛围标签、风险色条、移动端顶部离开按钮）
- 探索页改为校园踩点/发现界面
- iconKey → emoji 映射（building→🏫, code→💻 等）
- 移动端 Dock 遮挡修复、移动端事件页布局优化

**第三批 — 组织 + 副本 + 结局**
- organization 改为社团招新广场 + 组织活动场景
- dungeon 改为挑战海报墙 + 副本封面页 + 关卡挑战页 + 阶段战报
- ending 改为学期回忆报告
- 所有玩家端页面使用 game-hud + game-dock，不再出现旧 navbar

### 可玩性第一批 ✅

**周主题系统**
- 4 周各有主题名称、描述、行动提示（开学适应周/社团招新周/DDL 高压周/期末与体测周）
- WeeklyThemeService 静态映射，不建表
- 在 dashboard、map、exploration 页面显示当前周主题

**校园传闻系统**
- 16 条种子传闻（每周 3-4 条，含 common/rare 稀有度）
- 每周为玩家稳定抽取 2-3 条（基于 userId + week 哈希）
- Rumor 表 + RumorMapper + RumorService
- 在 dashboard 和 map/exploration 页面展示

**NPC/搭子系统**
- 5 个 NPC（室友阿杰/学霸林然/社牛周予/师兄老郑/运动搭子小马）
- NPC 有归属地点、性格、倾向属性、头像图标
- 探索地点后 35% 概率遇见 NPC
- UserNpcRelation 记录熟悉度、遇见次数、最近遇见周次
- 遇见后显示倾向提示（如"和 TA 在一起学业容易进步"），不显示虚假属性变化
- dashboard 显示熟人列表（头像 + 名称 + 熟悉度 + 关系阶段 + 本周搭子标记）
- NPC 详情页支持主动互动，按熟悉度解锁不同互动
- 每个 NPC 每周最多主动互动 1 次，互动消耗 1 AP
- 每周可选择 1 名熟悉度达到 50 的 NPC 作为本周搭子
- 本周搭子会给对应 NPC 互动提供额外属性/熟悉度加成
- 周总结会根据是否选择搭子、是否互动、高压力状态生成不同评价

### 可玩性第二批：周目标、成就、周总结 ✅

**本周目标**
- 每周可从候选目标中选择 1 个目标
- 已支持探索、NPC 遇见、组织活动、副本阶段、压力保持、NPC 主动互动、选择搭子、熟悉度增长等目标类型
- 目标完成后可领取经验和属性奖励
- 压力保持目标在 dashboard 和周推进时都会检查

**成就称号**
- 成就可按条件或事件触发解锁
- 已接入探索、NPC、组织、副本、周目标、压力保持、Java 课设等流程
- 解锁成就会更新玩家当前称号
- dashboard 展示成就称号货架

**周回忆报告**
- dashboard 的周推进前先进入 `/week/summary`
- 周总结展示周主题、目标状态、属性快照、NPC 熟人、近期成就、评价文案和评级
- 推进周次由周总结页触发，学期结束后进入结局页

### 可玩性第三批：学业危机与 NPC 搭子内容包 ✅

**Content Pack 2：学业危机**
- 新增学业危机事件、DDL/期末相关周总结反馈和成就
- Java 课设副本增强，加入 Git 合并地狱等更有大学生语境的内容
- 数据库防御、Bug 暴走等副本体验增强

**Content Pack 3：校园搭子与人际关系线**
- 新增 `npc_interaction` 和 `user_npc_weekly_action` 两张表
- 新增 15 条 NPC 主动互动、3 个 NPC 周目标、5 个 NPC 成就、10 条传闻
- 新增 `/npcs/{id}` NPC 详情页和 NPC 互动结果页
- NPC 主动互动真实修改属性，返回实际属性变化 delta
- 接入周目标、成就和周总结反馈

### 可玩性第四批：传闻、周主题与探索奇遇机制化 ✅

**Content Pack 4：传闻/周主题/探索奇遇**
- `rumor` 表扩展 `effect_type`、`effect_value`、`effect_target`，传闻可转化为探索加成或属性变化
- 新增 `exploration_story_chain` 和 `exploration_story_progress` 两张表
- 新增 16 条机制型传闻、5 条探索奇遇链（13 个阶段）、4 个周目标、5 个成就
- 新增 `WeeklyModifierService`、`RumorEffectService`、`ExplorationStoryService`
- 探索结果会汇总“本次影响来源”：周主题、传闻、奇遇链、NPC 搭子外溢/救场
- 地图页和探索页对有传闻地点显示“有传闻”标记
- 周总结会识别本周是否推进过探索奇遇，并生成对应评价
- CP4 已接入周目标和成就：情报猎人、校园奇遇追踪者、顺势而为、搭子救场等

**CP4.1 小修：传闻类型补强**
- `npc_boost` 不再只是提示：会在探索影响来源面板中体现为社交 +1，并提高该地点 NPC 遇见概率（`effect_value=10` 约等于 +10 个百分点，上限 80%）
- `event_hint` 不再只是提示：会在探索影响来源面板中体现为情报优势，并在地图事件触发时让对应事件类型获得权重加成

**CP4 视觉复核 + UI 小修**
- 已检查 `/dashboard`、`/map`、`/exploration`、`POST /exploration/4` 后的探索结果页、`/week/summary`
- 已做 1366x768 与 375x812 浏览器视觉检查，目标页面无横向滚动，移动端 Dock overlap 为 0
- 小范围优化移动端 Dock 留白、“有传闻”标记、探索结果“本次影响来源”面板文案与视觉层级

**CP4.2 小修：事件线索映射扩展**
- `event_hint` 目标映射已支持 `academic -> academic_crisis`、`social -> 社交`、`skill -> 技能`、`health -> 健康`、`money -> 金钱`
- `event_hint` 探索反馈会按 target 展示对应属性变化，不再固定为学业/技能

**CP4.3 小修：传闻/周主题事件偏向组合**
- 地图事件触发时，`event_hint` 仍作为主事件偏向（+30 权重）
- 如果当前周主题也有不同事件类型偏向，会作为次级偏向继续生效（+15 权重）
- 没有 `event_hint` 时，周主题保持原有主偏向强度（+30 权重）

**CP4.4 小修：周主题组织收益补强**
- 第 2 周“社团招新周”参加组织活动时，贡献从 +3 提高到 +4，声望从 +2 提高到 +3
- 属性变化保持组织类型原有结算，不额外放大属性收益
- 组织活动结果文案会提示“社团招新周”带来的贡献/声望加成来源

**CP4.5 小修：周主题副本风险补强**
- 第 3 周“DDL 高压周”结算副本阶段时，压力变化额外 +1
- 覆盖普通副本选项和数据库拼图/Bug 定位小游戏动态结算
- 不改变副本分数、风险旗标、最终评价和页面结构

**CP4.6 小修：搭子外溢随机救场**
- 本周搭子保留匹配地点的稳定小加成，例如学霸林然在教学楼/图书馆提供学业 +1
- 当玩家压力较高（>= 60）时，探索会基于 userId、搭子、地点、周次和探索次数做可复现的随机救场判定
- 救场命中后追加 `buddy_rescue` 影响来源，压力 -2，并复用现有“搭子救场”周目标/成就进度
- 探索结果页补充 `buddy`、`buddy_rescue`、`story` 等影响来源标签，避免显示成笼统的“现场变化”

**CP4.7 小修：影响系统钩子整合**
- `event_hint` 目标映射补充 `pressure -> 学习`，高压/DDL 类线索可偏向学习事件池
- 周主题的事件偏向、组织活动收益加成、副本压力加成集中到 `WeeklyThemeService`
- 保持既有行为不变：第 2 周偏向社交事件并提高组织活动贡献/声望，第 3 周偏向学习事件并提高副本压力，第 4 周偏向健康事件

### 管理员 ✅
- 事件管理基础 CRUD

## 测试覆盖

最近一次全量验证：

```text
.\mvnw.cmd clean test
Tests run: 335, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

覆盖范围：

| 范围 | 说明 |
|---|---|
| 核心服务 | Player、Event、Exploration、Organization、Dungeon、NPC、WeeklyGoal、WeekSummary、Achievement、SemesterEnding |
| 控制器 | Auth、Dashboard、Map、Organization、Dungeon、NPC、WeekSummary、Task、AdminEvent |
| 内容包 | ContentPack1、ContentPack2、ContentPack3、ContentPack4、ContentPack6 种子数据 smoke tests |
| 数据访问 | MapperContext、Rumor、User、Task 等基础验证 |
| 页面渲染 | 多个 Controller 测试覆盖 Thymeleaf 模板解析和关键 model 属性 |

## 最近一次 HTTP 冒烟

验证时间：2026-06-11，本地端口 `8080`，登录并创建角色后检查。

| 页面 | HTTP | Whitelabel | game-dock | 验证信号 |
|---|---:|---|---|---|
| `/dashboard` | 200 | 否 | 是 | 正常进入游戏主页 |
| `/map` | 200 | 否 | 是 | 显示”有传闻”地点标记 |
| `/exploration` | 200 | 否 | 是 | 显示”有传闻”探索卡标记 |
| `/exploration/4` POST | 200 | 否 | 是 | 结果页显示”本次影响来源”，检测到 `npc_boost` 带来的社交 +1 |
| `/week/summary` | 200 | 否 | 是 | 周总结正常渲染 |
| `/week/history` | 200 | 否 | 是 | 历史周报和学期档案正常渲染 |
| `/npcs/2` | 200 | 否 | 是 | NPC 详情页展示关系阶段 |
| `/npcs/2/interactions/3004` POST | 200 | 否 | 是 | NPC 互动结果正常渲染 |
| `/ending` | 200 | 否 | 是 | 未结算/已结算状态均正常；已结算状态包含成长画像 |
| `/dungeons` | 200 | 否 | 是 | 副本海报墙正常渲染 |
| `/dungeons/6401` | 200 | 否 | 是 | 体测生存挑战详情页正常渲染 |
| `/dungeons/6401/start` | 200 | 否 | — | 副本开始后跳转到进行页 |
| `/dungeons/6401/play` | 200 | 否 | 是 | 副本进行页正常渲染 |
| `/organizations/1`、`/organizations/6001` | 200 | 否 | 是 | 组织详情页正常渲染 |
| `/dungeons/1`、`/dungeons/6001` | 200 | 否 | 是 | 副本详情页正常渲染 |
| `/dungeons/1/start`、`/dungeons/6001/start` | 200 | 否 | 是 | 副本开始后跳转到进行页 |
| `/dungeons/1/play`、`/dungeons/6001/play` | 200 | 否 | 是 | 副本进行页正常渲染 |

备注：CP6 收尾已用 Chrome headless + 临时 Playwright 环境检查 1366x768 与 375x812 的 `/dashboard`、`/map`、`/exploration`、`/week/summary`、`/organizations`、`/dungeons`。所有页面无横向滚动，Dock 未遮挡可交互元素；`/map` 图片加载完成，8 个热点均在地图范围内。缺少角色档案时，组织和副本深链返回 302 到 `/player/create`。

## 剩余风险

| 风险 | 说明 | 严重度 |
|---|---|---|
| 随机遇见 NPC 不改属性 | 探索后的随机 NPC 遇见仍只显示倾向提示；主动 NPC 互动已经真实修改属性 | 低 — 保持遇见轻量，主动互动负责结算 |
| `event_hint` 依赖事件池分布 | CP4.2 已映射 academic/social/skill/health/money，CP4.7 补充 pressure -> 学习，CP4.3 已保留周主题次级偏向；如果某地点没有对应事件类型，偏好加权不会产生明显命中变化 | 低 — 复用现有事件池，不新增 seed |
| 周主题玩法影响仍较轻 | CP4.4 覆盖第 2 周组织活动贡献/声望，CP4.5 覆盖第 3 周副本压力 +1，CP6.3 覆盖第 1 周事件/组织/NPC，CP6.4 覆盖第 4 周图书馆/操场探索收益和 physical 副本压力缓冲；所有钩子集中在 `WeeklyThemeService` | 低 — 四个周主题均有明确玩法影响 |
| `npc_boost` 同时给社交 +1 | CP4.1 为了让 `npc_boost` 在影响来源面板中可感知，额外给社交 +1；语义上应理解为“拼桌/社交机会增加” | 低 — 数值很小，后续可改成纯提示型 influence |
| 历史周报仍是轻量回放 | `/week/history` 已按周展示影响来源，但暂不支持筛选、统计或对比 | 低 — 当前目标是复盘入口，不做大型分析系统 |
| 传闻/周主题统计只依赖触发动作 | 周目标记录的是行动触发次数，不保存完整影响历史 | 低 — 当前周目标足够，历史复盘需要额外日志表 |
| lastMetWeek 更新 | maybeMeetNpc 需要传入 currentWeek，如果调用方忘记传会导致周次不准 | 低 — 接口已强制参数 |
| 移动端 Dock | CP5 UI 收尾已增加移动端底部安全区留白；极端超长内容或非典型 Android WebView 仍建议实机复核 | 低 — 已用 padding + safe-area 做多层防御 |
| 管理员页面 | 管理员事件管理仍使用旧 nav，与玩家端 UI 2.0 不一致 | 低 — 管理员非核心体验 |
| NPC 删除 | 如果 NPC 被从数据库删除，listKnownNpcs 返回的 relation.npc 可能为 null | 低 — 模板已加 null 检查 |

## CP4.8 补充：影响历史日志与周报回放

- 新增 `user_influence_log`，探索时记录实际触发的传闻、周主题、搭子、搭子救场和奇遇影响。
- 新增 `InfluenceLogService`，统一过滤无效果影响，并生成“社交 +2、探索 +1”这类可读变化文本。
- 周总结页新增“本周影响回放”，最多展示 5 条本周影响来源，避免影响只停留在当次探索结果页。

## CP4.9 补充：历史周报与跨周复盘入口

- 新增 `/week/history`，复用 `user_influence_log`，按周倒序展示当前学期的影响来源。
- 周总结页新增“查看历史周报”入口，玩家可以从当前周总结跳到跨周复盘。
- 新增控制器和服务测试，覆盖无角色重定向、历史页渲染、按周分组与近期优先排序。

## CP5 A/B 均衡第一批：关系成长与学期档案

- 新增 NPC 关系阶段 read model：熟悉度会稳定映射为关系阶段、阶段说明和进度百分比，并在 Dashboard、NPC 详情和互动结果中展示。
- 新增 NPC story progress：主动互动会推进 NPC 个人故事进度，结果页展示阶段反馈，周总结和成就仍复用既有流程。
- 新增 `SemesterArchiveService`：基于 `user_influence_log` 和已知 NPC 关系聚合学期档案摘要，不新增报表 SQL，不改结局匹配规则。
- `/week/history` 从单纯历史周报升级为“历史周报 + 学期档案”，顶部展示成长关键词、影响统计、关键周、关键关系和亮点。
- `/ending` 在学期结算后展示“学期成长画像”，把结局和本学期实际经历连接起来。
- 新增 `SemesterArchiveServiceTests` 和 `SemesterEndingControllerTests`，并扩展 WeekSummary/NPC 相关测试覆盖。

## CP5 A/B 均衡第二批：NPC 专属分支互动

- 基于 `user_npc_story_progress` 解锁 5 个 NPC 的轻量专属互动，不新增数据库表，不引入复杂剧情引擎。
- 专属互动复用现有 `/npcs/{npcId}/interactions/{interactionId}` 结算流程，仍消耗 AP、受每周同 NPC 一次互动限制约束，并真实修改属性/熟悉度。
- 专属互动会写入 `user_influence_log`，source_type 为 `npc_branch`，可进入当前周总结和 `/week/history` 历史复盘。
- 新增 `NpcServiceTests` 覆盖分支解锁、属性变更和影响日志；新增 `NpcControllerTests` 覆盖 NPC 详情页模型中出现已解锁分支互动。

## CP5 稳定化补充：NPC 分支资源与 ID 规则

- 移除 NPC 玩家页中对缺失 `/webjars/bootstrap/5.3.3/...` 资源的硬编码引用，继续统一复用 `/css/app.css` 和现有 game-shell/game-dock 视觉体系。
- 新增 `NpcStoryBranchCatalog`，集中管理 NPC 专属分支的虚拟互动 ID，保留 `900000-900999` 区间。
- 当前 NPC 分支 ID 按 `900000 + npcId * 100` 分块，降低后续手写 `900xxx` 时撞号风险。
- 新增 `NpcTemplateResourceTests` 与 `NpcStoryBranchCatalogTests`，覆盖 NPC 模板资源引用和分支 ID 唯一性/分块规则。

## CP5 UI 收尾：移动端 Dock 遮挡修复

- 本轮为 CSS-only 收尾，无数据库变更、无新增 seed、无新增 Java 测试。
- `src/main/resources/static/css/app.css` 微调 6 处移动端规则，其中 4 处为核心 Dock 安全区 padding 调整。
- 覆盖 `.game-shell`、`.dorm-room`、`.explore-page__nav`、`.npc-page`、`.npc-actions-panel > .vn-result__actions` 和 `.explore-page__grid`，重点解决探索页底部操作和 NPC 结果页“回寝室”按钮靠近 fixed Dock 的问题。
- 已完成 `.\mvnw.cmd clean test`，295 个测试全绿。
- HTTP 冒烟覆盖 `/dashboard`、`/map`、`/exploration`、`/exploration/4` POST、`/week/summary`、`/week/history`、`/npcs/2`、`/npcs/2/interactions/3004` POST、`/ending`、`/dungeons`，均为 200、无 Whitelabel、玩家页包含 `game-dock`。
- 浏览器视觉复核覆盖 1366x768 和 375x812；已人工确认 Playwright 对部分 padding 安全区按钮的 overlap 报告为误报，不影响实际视觉和点击体验。

## CP6 莲花街校区真实校园内容包

状态：已完成

范围：
- 新增 `data-content-pack-6.sql`，使用 6001+ id 段。
- 新增 5 个莲花街校区组织（计算机协会、信息学院学生会、信息学院辩论队、轮滑社、校合唱团）。
- 新增 8 个校园生活事件（实验数据蒸发、突发实训任务、大佬讲座、断网危机、健康餐挑战、通宵自习室陌生人、社团招新、座位之战），每个事件 3 个选项。
- 新增 6 条机制型传闻，复用 `attr_bonus`、`npc_boost`、`explore_bonus`、`safe_zone`、`event_hint`。
- 新增副本「小组作业」，5 个阶段（公布名单→线上开会→各自开荒→整合攻坚→卡点提交），每阶段 3 个选项。
- 未新增数据库表，未修改核心机制。
- 信息学院学生会描述已做中性化处理，未直接使用原始负面表述。

验证：
- `.\mvnw.cmd clean test`：Tests run: 307, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- HTTP 冒烟：`/dashboard`、`/map`、`/map/location/2/event`、`/exploration`、`/exploration/4` POST、`/week/summary`、`/organizations`、`/organizations/1`、`/organizations/6001`、`/dungeons`、`/dungeons/1`、`/dungeons/6001`、`/dungeons/1/start`、`/dungeons/6001/start`、`/dungeons/1/play`、`/dungeons/6001/play` 均为 200，无 Whitelabel。
- 缺少角色档案时，组织和副本深链会重定向到 `/player/create`，不再返回 500。

## CP6 真实校园地图接入

状态：已完成

范围：
- 从 `docs/补充信息.docx` 提取莲花街校区地图图片。
- 新增静态资源 `src/main/resources/static/images/lianhuajie-campus-map.jpeg`（1267×679，174KB）。
- `/map` 页面顶部新增真实地图展示层和 8 个地点热点（百分比坐标定位）。
- 热点复用现有地点 id 和 `/map/location/{id}/event` 事件流程。
- 保留原有地点卡片作为稳定入口。
- 未新增数据库表，未修改地图核心机制。

验证：
- `.\mvnw.cmd clean test`：Tests run: 307, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- HTTP 冒烟：`/map` 返回 200，地图图片 `/images/lianhuajie-campus-map.jpeg` 返回 200（174826 bytes）。
- 浏览器视觉复核：Chrome headless + Playwright 临时环境检查 1366x768 与 375x812，`/map` 图片加载完成，8 个热点均在地图范围内；核心玩家页无横向滚动，Dock 未遮挡可交互元素。
- 新增 `MapControllerTests#mapPageProvidesRealCampusMapHotspots` 和 `MapTemplateResourceTests`。

## CP6.1 NPC 原型接入

状态：已完成

本批次复用现有 NPC 系统和 CP6 莲花街内容包，不新增数据库表。

新增/调整内容：
- 新增 NPC：富少（6101）、小鱼（6102）、柳如烟（6103）。
- 复用已有 NPC：室友阿杰（1），追加莲花街校区相关普通互动，不新增重复阿杰。
- 新增普通 NPC 互动：610001-610011（阿杰 2 条 + 富少 3 条 + 小鱼 3 条 + 柳如烟 3 条）。
- 未使用 900000-900999 虚拟分支互动保留段。

验证：
- `.\mvnw.cmd clean test`：Tests run: 313, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- HTTP 冒烟覆盖 `/npcs/1`、`/npcs/6101`、`/npcs/6102`、`/npcs/6103`，均为 200。

## CP6.2 莲花街校区地点细分

状态：已完成

本批次复用现有地点、事件、传闻、探索奇遇链、周目标和成就系统，不新增数据库表。

新增内容：
- 事件：10 条，ID 6201-6210，覆盖图书馆（2）、惟学楼（6）、博闻楼营业厅/生活服务点（8）、韶华楼（7）、知味/知雅餐厅（4）。
- 事件选项：30 条，ID 620101-621003，每个事件 3 个选项。
- 传闻：6 条，ID 6201-6206，继续使用现有 effect_type（explore_bonus/event_hint/safe_zone/npc_boost/attr_bonus）。
- 探索奇遇链：4 条链、12 个阶段，ID 6201-6212（library_floor_trace/weixue_lab_route/bowen_service_window/canteen_peak_shift）。
- 周目标：2 个，ID 6201-6202。
- 成就：2 个，ID 6201-6202。

验证：
- `.\mvnw.cmd clean test`：Tests run: 317, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- HTTP 冒烟覆盖 `/dashboard`、`/map`、`/map/location/2/event`、`/map/location/4/event`、`/map/location/6/event`、`/map/location/8/event`、`/exploration`、`POST /exploration/4`、`/week/summary`、`/npcs/6101`、`/dungeons`、`/organizations`，均为 200，无 Whitelabel。
- 未修改 UI 文件，未执行浏览器视觉检查。

## CP6.3 开学迎新周机制化

状态：已完成

本批次为机制 + seed 扩展，不新增数据库表。所有周主题钩子集中在 `WeeklyThemeService`。

机制变更：
- 第 1 周周主题事件偏向改为「生活」，用于承载迎新、校园卡、网络、宿舍适应等开学事务。
- 第 1 周加入组织时社交门槛降低 5 点（40 → 35），体现迎新期更容易建立连接。
- 第 1 周主动 NPC 互动在没有本周搭子加成时熟悉度额外 +1，并在互动结果中显示开学适应周反馈文案。
- `event_hint` 新增 `life → 生活` 映射。

新增 seed：
- 事件：6 条，ID 6301-6306，覆盖迎新导览、选课、宿舍网络、校园卡、社团预热、食堂错峰。
- 事件选项：18 条，ID 630101-630603，每个事件 3 个选项。
- 传闻：4 条，ID 6301-6304，包含 `life` 目标的 event_hint。
- 周目标：2 个，ID 6301-6302。
- 成就：2 个，ID 6301-6302。

验证：
- `.\mvnw.cmd clean test`：Tests run: 324, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- HTTP 冒烟覆盖 `/dashboard`、`/map`、`/map/location/7/event`、`/map/location/8/event`、`/exploration`、`POST /exploration/4`、`/week/summary`、`/organizations`、`/npcs/6101`、`/dungeons`，均为 200，无 Whitelabel。
- 未修改模板/CSS，未执行浏览器视觉检查。

### CP6.4 期末与体测周机制化 ✅

本批次为机制 + seed 扩展，不新增数据库表。所有周主题钩子集中在 `WeeklyThemeService`。

机制变更：
- 第 4 周继续偏向健康事件，并新增图书馆/操场期末体测内容。
- `WeeklyThemeService` 新增第 4 周探索收益 hook：图书馆提供学业 +1、技能 +1、压力 -1、自律 +1；操场提供健康 +2、压力 -1、自律 +1。
- `WeeklyModifierService` 第 4 周关键地点修正为图书馆（2）和操场（5），不再把生活服务点（8）当作操场。
- `DungeonServiceImpl` 对 `physical` 类型副本在第 4 周提供压力 -1 缓冲，并在结果文案里标注来源。第 3 周 DDL 压力 +1 不受影响。

新增 seed：
- 事件：6 条，ID 6401-6406，覆盖图书馆复习、操场体测、食堂补给、宿舍早睡。
- 事件选项：18 条，ID 640101-640603，每个事件 3 个选项。
- 传闻：4 条，ID 6401-6404，包含 health 目标的 event_hint。
- 探索奇遇链：2 条链、6 个阶段，ID 6401-6406（final_library_review、physical_test_route）。
- 体测副本：1 个（体测生存挑战）、3 个阶段（1000 米配速/引体向上排队/坐位体前屈补救）、9 个选项，ID 6401/640101-640103/64010101-64010303。
- 周目标：2 个，ID 6401-6402。
- 成就：2 个，ID 6401-6402。

验证：
- `.\mvnw.cmd clean test`：Tests run: 333, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- HTTP 冒烟覆盖 `/dashboard`、`/map`、`/map/location/2/event`、`/map/location/5/event`、`/exploration`、`POST /exploration/5`、`/week/summary`、`/dungeons`、`/dungeons/6401`、`/dungeons/6401/start`、`/dungeons/6401/play`、`/npcs/6101`、`/organizations`，均为 200，无 Whitelabel。
- 未修改模板/CSS，未执行浏览器视觉检查。

## Full Game V1 Phase 1：16 周学期骨架

- 项目方向从 CP6.x 小内容包转为 Full Game V1。
- 学期长度由 4 周 Demo 升级为 16 周单学期。
- 新增 `SemesterCalendarService`，统一管理学期总周数、阶段映射、阶段文案、事件偏向和核心地点。
- `PlayerServiceImpl` 不再持有 `MAX_SEMESTER_WEEKS = 4`，学期结束判断改为第 17 周开始。
- `WeeklyThemeService` 改为消费 16 周阶段：开学适应、节奏建立、期中波动、路线分化、项目与 DDL、期末与体测。
- 第 1-2 周保留开学适应加成；第 3-5 周保留节奏建立组织加成；第 12-14 周保留 DDL 副本压力；第 15-16 周保留期末/体测探索与 physical 副本压力缓冲。
- `WeeklyModifierService` 改为消费 `SemesterCalendarService` 阶段映射。
- Dashboard 和 Ending 不再显示 "4 周大学生活"。
- `WeekSummaryServiceImpl` 使用阶段 key 代替硬编码周次。
- `OrganizationServiceImpl` 招新文案从"社团招新周"改为"节奏建立阶段"。
- 最近验证：`.\mvnw.cmd clean test`，Tests run: 335, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- HTTP 冒烟覆盖 `/dashboard`（含"第 1 周"、"共 16 周"）、`/map`、`/exploration`、`/week/summary`、`/ending`、`/dungeons`、`/organizations`，均为 200，无 Whitelabel，玩家页包含 `game-dock`。

## 当前文档状态

- CP6 莲花街校区内容包与真实地图接入、CP6.1、CP6.2、CP6.3、CP6.4 已完成，对应执行计划稿已清理，避免后续 AI 重复实现。
- 已完成的 CP5 历史设计稿和实施计划已清理；CP5 实际完成情况以本文档和 `docs/NEXT_AI_HANDOFF.md` 为准。
- 项目方向已从 CP6.x 小内容包转向 Full Game V1；下一步以 `docs/superpowers/specs/2026-06-11-full-game-v1-design.md` 和 `docs/superpowers/plans/2026-06-11-full-game-v1-phase-1-semester-skeleton.md` 为准。
- 当前主线下一步是 Full Game V1 Phase 1：16 周单学期骨架。不要继续按 CP6.x 命名新增零散内容包。

## 可扩展方向

以下功能未实现，可作为后续开发方向：

| 方向 | 说明 |
|---|---|
| Full Game V1 Phase 1 | 当前主线下一步：16 周单学期骨架、统一学期日历、阶段主题映射、必要页面文案和测试 |
| Full Game V1 Phase 2 | 周主题升级与阶段反馈深化，把 CP6 机制纳入 16 周阶段节奏 |
| Full Game V1 Phase 3 | 路线目标与阶段目标，避免 16 周目标池阶段错位 |
| Full Game V1 Phase 4 | 结局评分升级，让完整学期经历影响结局判断 |
| NPC 属性影响落地 | 遇见 NPC 实际修改属性，熟悉度高时加成更大 |
| 深化周主题影响 | 第 1 周影响事件、组织和 NPC；第 2 周影响组织活动收益；第 3 周影响副本压力；第 4 周影响图书馆/操场探索和体测副本压力。四个周主题均有明确玩法影响 |
| CP6.4 期末与体测周 | 已完成：图书馆/操场探索收益、physical 副本压力缓冲、体测副本和期末事件种子内容 |
| CP6 浏览器视觉复核 | 已降级为回归检查项；如后续改地图或移动端页面，再检查地图热点、体测副本详情页和 Dock |
| 更多副本 | 图书馆席位战争、蓝桥杯突击、实习面试 |
| 成就称号 | 收集要素，解锁条件绑定属性/探索/组织 |
| 管理后台完善 | 组织/结局/副本/成就/NPC/传闻的 CRUD |
| 多学期 | 学期间属性继承、成长曲线 |
| NPC 深化 | NPC 好感度事件、专属支线、搭子组队加成 |
