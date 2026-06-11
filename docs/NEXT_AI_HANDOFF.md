# NEXT AI HANDOFF

最后更新：2026-06-11

## 当前项目状态

HAUT Survivor 当前是一个可运行的周回合制大学生模拟器 Demo。核心循环已闭合：

```text
创建角色 -> 探索/事件/组织/副本/NPC -> 周总结 -> 推进周次 -> 学期结局 -> 重开新学期
```

最近一次全量验证：

```text
.\mvnw.cmd clean test
Tests run: 333, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

CP5 A/B 第二批补充：已基于 `user_npc_story_progress` 解锁 5 个 NPC 的轻量专属分支互动。分支互动复用现有 `/npcs/{npcId}/interactions/{interactionId}` 结算流程，使用 Java 层虚拟互动定义和 900xxx ID，不新增表；互动仍消耗 AP、受每周同 NPC 一次限制约束，并写入 `user_influence_log` 的 `npc_branch` 来源。

CP5 UI 收尾：已完成移动端 Dock 遮挡复核和小范围 CSS-only 修复。本轮没有数据库变更、没有新增 seed、没有新增 Java 测试；核心改动集中在移动端安全区留白，覆盖 `game-shell`、`dorm-room`、探索页底部导航、NPC 页面和 NPC 结果页操作区。

最近一次 HTTP 冒烟：2026-06-11，端口 `8080`，登录并创建角色后，`/dashboard`、`/map`、`/map/location/2/event`、`/exploration`、`/exploration/4` POST、`/week/summary`、`/organizations`、`/organizations/1`、`/organizations/6001`、`/dungeons`、`/dungeons/1`、`/dungeons/6001`、`/dungeons/1/start`、`/dungeons/6001/start`、`/dungeons/1/play`、`/dungeons/6001/play` 均为 200，无 Whitelabel，且玩家页均包含 `game-dock`。缺少角色档案的组织/副本深链返回 302 到 `/player/create`。

最近一次浏览器视觉复核：2026-06-11，使用 Chrome headless + 临时 Playwright 环境，以 1366x768 与 375x812 检查 `/dashboard`、`/map`、`/exploration`、`/week/summary`、`/organizations`、`/dungeons`。所有页面无横向滚动、Dock 未遮挡可交互元素；`/map` 图片加载完成，8 个热点均在地图范围内。

本地 `main` 当前领先 `origin/main` 多个提交，最近内容包含：

- Content Pack 3：校园搭子与人际关系线
- Content Pack 4 设计文档与实施计划
- Content Pack 4：传闻、周主题与探索奇遇机制化
- NPC 搭子系统、NPC 主动互动、NPC 页面与 Dashboard 熟人入口
- 机制型传闻、周主题修正、探索奇遇链、探索影响来源展示
- CP4.1：`npc_boost` 接入 NPC 遇见概率，`event_hint` 接入事件池倾向
- CP4 视觉复核 + UI 小修：移动端 Dock 留白、“有传闻”标记、“本次影响来源”面板
- CP4.2：`event_hint` 支持 academic/social/skill/health/money 目标映射
- CP4.3：`event_hint` 主偏向存在时保留周主题次级事件偏向
- CP4.4：第 2 周“社团招新周”组织活动贡献/声望额外 +1，并在活动反馈文案中说明来源
- CP4.5：第 3 周“DDL 高压周”副本阶段结算压力额外 +1，覆盖普通选项和小游戏动态结算
- CP4.6：高压力探索时，本周搭子有可复现概率触发“搭子救场”，在探索结果中显示并压力 -2
- CP4.7：`event_hint` 补充 `pressure -> 学习`，周主题事件/组织/副本钩子集中到 `WeeklyThemeService`
- CP4.8：新增影响历史日志，探索触发的传闻、周主题、搭子/救场和奇遇会进入周总结“本周影响回放”
- CP4.9：基于 `user_influence_log` 新增 `/week/history` 历史周报，按周倒序回放影响来源
- CP5 A/B 均衡第一批：NPC 关系阶段、NPC story progress、学期档案摘要和结局成长画像
- CP5 A/B 均衡第二批：NPC 专属分支互动，复用现有 NPC 互动结算流程并写入影响日志
- CP5 UI 收尾：移动端 Dock 遮挡复核和安全区留白修复
- CP6：莲花街校区内容包、真实地图图片层和组织/副本深链缺档案保护

## 最近完成

CP6 莲花街校区内容包与真实地图接入。

已完成：

- `src/main/resources/data-content-pack-6.sql` 新增莲花街校区组织、事件、传闻和“小组作业”副本。
- `/map` 顶部接入真实校区地图图片和 8 个热点，保留原地点卡片作为稳定入口。
- 组织/副本深链在缺少角色档案时会重定向到 `/player/create`，不再出现 500。
- HTTP 冒烟覆盖 CP6 组织、副本、地图、探索和周总结主链路。
- 最近全量测试为 307 个测试全绿。

前置 CP5 A/B 均衡第一批：关系成长与学期档案。

- 新增 NPC 关系阶段 read model：熟悉度会稳定映射为关系阶段、阶段说明和进度百分比
- NPC 详情页、Dashboard 熟人列表和 NPC 互动结果页展示关系阶段/关系说明
- 新增 NPC story progress：主动互动会推进 NPC 个人故事进度，结果页展示阶段反馈
- 新增 `SemesterArchiveService`，基于 `user_influence_log` 和已知 NPC 关系聚合学期档案摘要
- `/week/history` 顶部新增“学期档案”，展示成长关键词、影响统计、关键周、关键关系和亮点
- `/ending` 在学期结算后展示“学期成长画像”
- 新增/扩展 `SemesterArchiveServiceTests`、`SemesterEndingControllerTests`、`WeekSummaryControllerTests`、NPC 相关服务/控制器测试

CP5 A/B 均衡第二批：NPC 专属分支互动。

- 基于 `user_npc_story_progress` 解锁 5 个 NPC 的轻量专属互动
- 专属互动复用现有 `/npcs/{npcId}/interactions/{interactionId}` 结算流程，不新增数据库表
- 专属互动消耗 AP、受每周同 NPC 一次限制约束，并真实修改属性/熟悉度
- 专属互动写入 `user_influence_log`，source_type 为 `npc_branch`，可进入当前周总结和 `/week/history`
- 新增 `NpcStoryBranchCatalog` 集中管理 900xxx 虚拟互动 ID，并补充 ID 唯一性/分块规则测试

前置 CP4 已完成内容包括：

- `rumor` 表新增 `effect_type`、`effect_value`、`effect_target`
- 新增 `exploration_story_chain`：探索奇遇链定义表
- 新增 `exploration_story_progress`：玩家奇遇进度表
- 新增 16 条机制型传闻
- 新增 5 条探索奇遇链，共 13 个阶段
- 新增 4 个 CP4 周目标：情报猎人、校园奇遇追踪者、顺势而为、搭子救场
- 新增 5 个 CP4 成就：情报新生、校园目击者、支线清理大师、节奏感选手、有人罩着
- 新增 `WeeklyModifierService`、`RumorEffectService`、`ExplorationStoryService`
- 探索结果页展示“本次影响来源”和奇遇链卡片
- 地图页、探索页对有传闻地点显示“有传闻”标记
- 周总结会根据本周奇遇进度生成不同评价
- CP4.1 已补强 `npc_boost` 与 `event_hint`：
  - `npc_boost` 在探索结果中表现为社交 +1，并提升 NPC 遇见概率
  - `event_hint` 在探索结果中表现为情报优势，并让地图事件更偏向 `academic_crisis`
- CP4.2 已扩展 `event_hint`：
  - `academic -> academic_crisis`
  - `social -> 社交`
  - `skill -> 技能`
  - `health -> 健康`
  - `money -> 金钱`
- CP4.3 已组合事件偏向：
  - `event_hint` 作为主偏向时仍获得 +30 权重
  - 当前周主题若偏向不同事件类型，继续作为次级偏向获得 +15 权重
  - 无 `event_hint` 时，周主题保持 +30 权重
- CP4.4 已补强周主题组织收益：
  - 第 2 周“社团招新周”参加组织活动时，贡献从 +3 提高到 +4
  - 声望从 +2 提高到 +3
  - 属性变化保持原有组织类型结算，活动反馈会提示周主题加成
- CP4.5 已补强周主题副本风险：
  - 第 3 周“DDL 高压周”结算副本阶段时，压力变化额外 +1
  - 覆盖普通选项和数据库拼图/Bug 定位小游戏动态结算
  - 不改变副本分数、风险旗标、最终评价和页面结构
- CP4.6 已补强搭子外溢：
  - 匹配地点的本周搭子稳定小加成保持不变
  - 玩家压力较高（>= 60）时，探索会基于用户、搭子、地点、周次和探索次数做可复现救场判定
  - 救场命中后追加 `buddy_rescue` 影响来源，压力 -2，并推进“搭子救场”周目标/成就进度
- CP4.7 已整合影响系统钩子：
  - `event_hint` 目标映射补充 `pressure -> 学习`
  - 周主题事件偏向、组织活动收益加成、副本压力加成集中在 `WeeklyThemeService`
  - 既有周主题行为保持不变：第 2 周社交/组织加成，第 3 周学习/副本压力，第 4 周健康事件偏向
- CP4 视觉复核已完成：移动端 Dock 不遮挡目标页面内容，“有传闻”标记和探索结果影响来源面板已做小范围 UI 修正

## CP6 完成细节

CP6 莲花街校区内容包与真实地图接入。

内容包阶段：
- `src/main/resources/data-content-pack-6.sql` — 5 个组织、8 个事件、6 条传闻、1 个副本（5 阶段）。
- `src/test/java/cn/haut/survivor/service/ContentPack6Tests.java` — 4 个 seed 验证测试。
- `application.yml` 已加载 CP6 seed 文件。
- 修复 `ContentPack1Tests` 组织计数断言（3 → ≥3）。

真实地图接入阶段：
- 从 `docs/补充信息.docx` 提取地图图片至 `src/main/resources/static/images/lianhuajie-campus-map.jpeg`（1267×679，174KB）。
- `MapController` 新增 `CampusMapHotspot` record 和 `buildCampusMapHotspots()` 方法，提供 8 个热点坐标。
- `/map` 页面顶部新增真实地图展示层，热点点击复用 `/map/location/{id}/event` 流程。
- 原地点卡片未删除，仍作为主要稳定入口。
- 新增 `MapControllerTests#mapPageProvidesRealCampusMapHotspots` 和 `MapTemplateResourceTests`。
- 新增桌面端和移动端 CSS（`.campus-map-photo`、`.campus-map-hotspot`）。

最近一次全量验证：
```text
.\mvnw.cmd clean test
Tests run: 313, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

HTTP 冒烟：`/dashboard`、`/map`、`/map/location/2/event`、`/exploration`、`/exploration/4` POST、`/week/summary`、`/organizations`、`/organizations/1`、`/organizations/6001`、`/dungeons`、`/dungeons/1`、`/dungeons/6001`、`/dungeons/1/start`、`/dungeons/6001/start`、`/dungeons/1/play`、`/dungeons/6001/play` 均为 200，无 Whitelabel。地图图片 `/images/lianhuajie-campus-map.jpeg` 返回 200（174826 bytes）。

CP6 收尾补修：缺少角色档案时，`/organizations/{id}`、组织操作 POST、`/dungeons/{id}`、`/dungeons/{id}/start`、`/dungeons/{id}/play`、副本阶段提交 POST 均会重定向到 `/player/create`，不再打出 500。

CP6.1 NPC 原型接入：
- 新增 NPC：富少（6101）、小鱼（6102）、柳如烟（6103）。
- 复用已有 NPC 室友阿杰（1），追加 2 条莲花街普通互动，未新增重复阿杰。
- 新增普通 NPC 互动 610001-610011（共 11 条），ID 在 610xxx 段，未碰 900xxx 虚拟分支保留段。
- 新增 `ContentPack6NpcTests`（4 个测试）和 `NpcControllerTests` CP6.1 烟雾测试。
- 最近全量测试为 313 个测试全绿。

CP6.2 莲花街校区地点细分：
- 事件 10 条（6201-6210），覆盖图书馆、惟学楼、博闻楼营业厅、韶华楼、知味/知雅餐厅。
- 事件选项 30 条（620101-621003）。
- 传闻 6 条（6201-6206），使用现有 effect_type。
- 探索奇遇链 4 条链 12 阶段（6201-6212）：library_floor_trace、weixue_lab_route、bowen_service_window、canteen_peak_shift。
- 周目标 2 个（6201-6202）、成就 2 个（6201-6202）。
- 新增 `ContentPack6LocationDetailTests`（4 个测试）。
- 未修改 UI 文件，未执行浏览器视觉检查。
- 最近全量测试为 317 个测试全绿。

CP6.3 开学迎新周机制化：
- 周主题 hook 仍集中在 `WeeklyThemeService`。
- 第 1 周事件偏向「生活」；第 2/3/4 周仍保持社交/学习/健康。
- 组织加入门槛：第 1 周社交要求 40 → 35。
- NPC 主动互动：第 1 周非搭子互动熟悉度额外 +1，并在结果文案中显示开学适应周反馈。
- `event_hint` 新增 `life → 生活` 映射。
- CP6.3 seed 使用 6300 段 ID：事件 6 条、选项 18 条、传闻 4 条、周目标 2 个、成就 2 个。
- 新增 `ContentPack6OpeningWeekTests`（3 个测试），修改 `WeeklyThemeServiceTests`、`EventServiceTests`、`RumorEffectServiceTests`、`OrganizationServiceTests`、`NpcServiceTests`。
- 最近全量测试为 324 个测试全绿。

## CP6.4 期末与体测周机制化

- 第 4 周核心地点是图书馆（2）和操场（5）。注意：生活服务点/博闻楼营业厅是 8，不是操场。
- `WeeklyThemeService` 集中提供第 4 周探索属性收益、physical 副本压力缓冲和结果文案后缀。
- `WeeklyModifierService` 注入 `WeeklyThemeService`，第 4 周关键地点修正为图书馆（2）和操场（5）。
- `DungeonServiceImpl` 对 `physical` 类型副本在第 4 周提供压力 -1 缓冲，结果文案显示"期末与体测周"后缀。第 3 周 DDL 压力 +1 不受影响。
- `data-content-pack-6.sql` 追加 6400 段 seed：
  - 事件 6401-6406，选项 640101-640603。
  - 传闻 6401-6404。
  - 探索奇遇链 6401-6406（final_library_review、physical_test_route）。
  - 副本 6401，阶段 640101-640103，选项 64010101-64010303。
  - 周目标 6401-6402，成就 6401-6402。
- 新增 `ContentPack6FinalWeekTests`（5 个测试），扩展 `WeeklyThemeServiceTests`、`WeeklyModifierServiceTests`、`DungeonServiceTests`。
- 最近全量测试为 333 个测试全绿。

## 当前建议下一步

CP6 第一批已经完成，已删除对应的执行计划稿，避免后续 AI 重复实现同一批内容。

建议下一步优先级：
- CP6 浏览器视觉复核：重点看地图热点、体测副本详情页、移动端 Dock。
- CP6.5 期末结局联动：让体测副本/期末奇遇影响 semester ending 评分。
- CP6.6 NPC 与体测周联动：小马/柳如烟在第 4 周给更明确的辅助反馈。

CP5 后续深化可以暂缓，除非产品方向重新转回学期档案筛选、结局画像跳转或 NPC 分支扩展。

## CP6 交接备注

设计边界：
- CP6 第一批只做 seed-heavy 内容包 + 地图图片接入。
- CP6.1 新增 NPC 原型（富少、小鱼、柳如烟）和莲花街互动。
- CP6.2 做地点细分事件/传闻/奇遇链。
- CP6.3 做开学迎新周机制化（事件偏向、组织门槛、NPC 互动）。
- CP6.4 做期末与体测周机制化（图书馆/操场探索收益、physical 副本压力缓冲、体测副本种子）。
- 未扩展地点表结构，未新增数据库表。
- 未新增 NPC 原型，避免和当前 NPC 关系/故事线改动交叉。
- 信息学院学生会描述已做中性化处理，未直接使用原始负面表述。

剩余风险：
- 热点坐标是按当前图片人工估算的，后续如果替换更高清地图，需要重新微调。
- 移动端热点标签空间有限，目前以短标签为主。
- 这不是完整地图系统，没有缩放、拖拽或楼层切换。

建议下一步：
- CP6 浏览器视觉复核：重点看地图热点、体测副本详情页、移动端 Dock。
- CP6.5 期末结局联动：让体测副本/期末奇遇影响 semester ending 评分。
- CP6.6 NPC 与体测周联动：小马/柳如烟在第 4 周给更明确的辅助反馈。

## 重要约束

- 不要重写整个系统。
- 继续小步迭代：设计文档 -> 实施计划 -> 测试驱动实现。
- 前端继续复用 `game-shell`、`game-hud`、`game-dock`、`vn-result`、`sticky-note__btn` 等现有 UI 组件。
- 数据库变更保持轻量；优先复用已有 `rumor`、`weekly_goal`、`campus_location`、`event`、`user_location_exploration`、`user_npc_relation`、`user_npc_story_progress`、`user_influence_log`、`exploration_story_chain`、`exploration_story_progress`。
- 每次实现后必须运行 `.\mvnw.cmd clean test`，并做至少 dashboard/map/exploration/week summary 的 HTTP 冒烟。

## 已知风险

- `explore_bonus`、`attr_bonus`、`safe_zone`、`npc_boost`、`event_hint` 均已能影响探索反馈；其中 `event_hint` 已映射 academic/social/skill/health/money/pressure，并会和周主题事件偏向组合，但仍依赖具体地点是否有对应事件类型。
- 周主题目前影响探索结果、地图事件概率、第 2 周组织活动收益和第 3 周副本压力，相关钩子已集中到 `WeeklyThemeService`；第 1 周已影响事件/组织/NPC，第 4 周已影响图书馆/操场探索收益和 physical 副本压力缓冲。
- 搭子救场已进入 `user_influence_log`，会在当前周总结和 `/week/history` 历史周报中展示；历史页暂不提供筛选或统计。
- 学期档案是 Java 层聚合，不是 SQL 报表；如果后续数据量扩大，需要再评估分页或聚合性能。
- 结局成长画像只展示总结，不修改 `SemesterEndingService` 的结局匹配规则。
- 随机 NPC 遇见仍只显示倾向提示；主动 NPC 互动已经真实修改属性。
- CP5 UI 收尾后，移动端 Dock 仍依赖底部 padding 与 `env(safe-area-inset-bottom)` 双保险；极端超长内容或非典型 Android WebView 仍建议实机复核。
- 若后续继续改页面，仍需做 HTTP 冒烟；有浏览器能力时继续检查 1366x768 与 375x812。
