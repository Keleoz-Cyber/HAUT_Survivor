# Full Game V1 Phase 5：内容补齐

最后更新：2026-06-11

## 目标

在 16 周学期骨架稳定后（Phase 1-4 已完成），按阶段补齐事件、传闻、奇遇链、周目标和成就，让 midterm（6-8）、route（9-11）、project（12-14）、final（15-16）四个薄弱阶段有可感知的阶段内容。opening/rhythm 阶段只做少量校准。

## 现状分析

### 种子分布（Phase 5 前）

| 内容类型 | opening(1-2) | rhythm(3-5) | midterm(6-8) | route(9-11) | project(12-14) | final(15-16) |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| 阶段专属事件 | 16 | 16 | **0** | **0** | **0** | **0** |
| 传闻 | 39 | 39 | **0** | **0** | **0** | **0** |
| 奇遇链 | 4+5 all | 2+5 all | **5 all** | **5 all** | **5 all** | **5 all** |
| 周目标 | 22 all | 22 all | 22 all | 22 all | 22 all | 22 all |
| 成就 | 30 all | 30 all | 30 all | 30 all | 30 all | 30 all |

### 核心问题

1. **期中（6-8）、路线分化（9-11）、项目/DDL（12-14）、期末（15-16）四个阶段零专属事件**
2. **传闻系统在 week 5+ 完全失效**——78 条传闻全部钉在 week 1-4
3. **奇遇链全部在 week 1-4 或 all-stage**，中后期无阶段特色链
4. **CP6.4 的"期末与体测"事件 min_week/max_week=4**——是旧 4 周时代的产物，需要迁移到 15-16

### 旧内容迁移说明

CP6.4 的事件（6401-6406）当前 min_week=4, max_week=4，这是旧 4 周时代为"第 4 周=期末周"写的。Phase 1 已把学期改为 16 周，第 4 周实际属于 rhythm 阶段，不是 final。这些事件内容（图书馆闭馆冲刺、考前重点互认、体测热身等）天然适合 final 阶段。

**策略**：更新 6401-6406 的 min_week/max_week 为 15-16，让它们回到正确的阶段位置。CP6.4 的奇遇链（final_library_review、physical_test_route）同理，week_number 从 4 改为 15。CP6.4 的传闻（6401-6404）从 week 4 改为 week 15。这些改动在已有 data-content-pack-6.sql 中直接修改。

## 约束

- 不新增数据库表
- 不改 schema
- 不重写 UI
- 不改 Phase 1-4 已完成机制
- 不恢复 CP6.x 命名
- ID 使用 7001+ 段，不覆盖已有 seed
- 先写测试，再实现

## 实施范围

### 1. 旧内容迁移（data-content-pack-6.sql 修改）

将 CP6.4 种子从旧 week 4 迁移到 16 周正确位置：

| 种子类型 | ID 范围 | 旧范围 | 新范围 |
|---|---|---|---|
| 事件 6401-6406 | min_week=4, max_week=4 | week 4 | min_week=15, max_week=16 |
| 传闻 6401-6404 | week_number=4 | week 4 | week_number=15 |
| 奇遇链 6401-6406 | week_number=4 | week 4 | week_number=15 |

### 2. 新增内容文件

新增 `data-v1-stage-fill.sql`，ID 段 7001+：

#### 事件（每阶段 6 条，每条 3 个选项）

**midterm 阶段（6-8 周）**——期中考试、课程压力、复习策略

| ID | 事件名 | 类型 | 地点 | min_week | max_week |
|---|---|---|---|:---:|:---:|
| 7001 | 期中考试倒计时 | 学习 | 1 教学楼 | 6 | 8 |
| 7002 | 图书馆复习撞车 | 学习 | 2 图书馆 | 6 | 8 |
| 7003 | 实验报告截止 | 学习 | 6 惟学楼 | 6 | 8 |
| 7004 | 高数小测突击 | 学习 | 1 教学楼 | 7 | 8 |
| 7005 | 压力爆表求助信号 | 生活 | 3 宿舍 | 6 | 8 |
| 7006 | 期中后成绩焦虑 | 社交 | 4 食堂 | 7 | 8 |

**route 阶段（9-11 周）**——路线分化：考研/就业/竞赛/社交/摆烂

| ID | 事件名 | 类型 | 地点 | min_week | max_week |
|---|---|---|---|:---:|:---:|
| 7007 | 考研信息分享会 | 学习 | 2 图书馆 | 9 | 11 |
| 7008 | 实习招聘信息群 | 技能 | 7 韶华楼 | 9 | 11 |
| 7009 | 竞赛组队邀请 | 技能 | 6 惟学楼 | 9 | 11 |
| 7010 | 社团换届竞选 | 社交 | 7 韶华楼 | 9 | 11 |
| 7011 | 摆烂室友的诱惑 | 生活 | 3 宿舍 | 9 | 11 |
| 7012 | 导师开放日 | 学习 | 6 惟学楼 | 10 | 11 |

**project 阶段（12-14 周）**——课程项目、DDL、小组作业

| ID | 事件名 | 类型 | 地点 | min_week | max_week |
|---|---|---|---|:---:|:---:|
| 7013 | Java 课设需求变更 | 学习 | 6 惟学楼 | 12 | 14 |
| 7014 | 小组作业划水危机 | 社交 | 3 宿舍 | 12 | 14 |
| 7015 | 实验室服务器宕机 | 技能 | 6 惟学楼 | 12 | 14 |
| 7016 | 答辩 PPT 连夜赶 | 学习 | 3 宿舍 | 13 | 14 |
| 7017 | 期末论文选题 | 学习 | 2 图书馆 | 12 | 14 |
| 7018 | DDL 三连预警 | 生活 | 3 宿舍 | 13 | 14 |

事件选项 ID：700101-701803（每个事件 3 个选项）

#### 传闻（每阶段 4 条）

| ID | week_number | 地点 | 主题 |
|---|:---:|---|---|
| 7001 | 6 | 2 | midterm: 图书馆期中占座攻略 |
| 7002 | 6 | 1 | midterm: 往年期中题型流出 |
| 7003 | 7 | 3 | midterm: 期中后"报复性放松" |
| 7004 | 8 | 4 | midterm: 食堂期中优惠安慰餐 |
| 7005 | 9 | 2 | route: 考研自习室预约开放 |
| 7006 | 10 | 7 | route: 竞赛报名截止提醒 |
| 7007 | 10 | 6 | route: 实习内推名额 |
| 7008 | 11 | 3 | route: 学长学姐考研上岸经验 |
| 7009 | 12 | 6 | project: 课设 Git 合并地狱高发期 |
| 7010 | 13 | 3 | project: 小组作业"背锅"风险 |
| 7011 | 13 | 2 | project: 论文查重要求升级 |
| 7012 | 14 | 4 | project: DDL 前食堂外卖爆单 |
| 7013 | 15 | 2 | final: 图书馆期末座位争夺战 |
| 7014 | 15 | 5 | final: 体测补测通知 |
| 7015 | 16 | 2 | final: 考前突击复习路线 |
| 7016 | 16 | 3 | final: 宿舍期末生存公约 |

#### 奇遇链（每薄弱阶段 1 条短链，3 步）

| chain_key | 名称 | 地点 | week_number | 步骤 |
|---|---|---|:---:|:---:|
| midterm_review_route | 期中复习路线 | 2 图书馆 | 6 | 3 |
| route_career_expo | 路线分化招聘会 | 7 韶华楼 | 10 | 3 |
| project_ddl_survival | DDL 生存路线 | 3 宿舍 | 13 | 3 |

奇遇链 ID：7001-7009（3 条链 × 3 步）

#### 周目标（每阶段 2 个）

| ID | goal_key | 类型 | 描述 |
|---|---|---|---|
| 7001 | midterm_exam_prep | event | 期中复习：完成 2 次学习类事件 |
| 7002 | midterm_pressure_hold | pressure | 期中抗压：压力保持在 60 以下 |
| 7003 | route_skill_up | explore | 路线提升：完成 2 次探索 |
| 7004 | route_npc_connect | npc_interaction | 路线社交：完成 1 次 NPC 主动互动 |
| 7005 | project_dungeon_push | dungeon | 项目推进：完成 1 次副本阶段 |
| 7006 | project_skill_focus | skill | 技能专注：技能提升到 70+ |

#### 成就（每阶段 2 个）

| ID | achievement_key | 类型 | 描述 |
|---|---|---|---|
| 7001 | midterm_survivor | explore_count | 期中幸存者：期中阶段完成 3 次探索 |
| 7002 | exam_ready | event | 考前备战：完成 5 次学习类事件 |
| 7003 | route_decided | npc_interaction | 路线抉择：与 3 个不同 NPC 互动 |
| 7004 | skill_master | skill | 技能达人：技能达到 80 |
| 7005 | project_finisher | dungeon_completed | 项目终结者：完成 2 次副本 |
| 7006 | semester_veteran | explore_count | 学期老手：累计完成 10 次探索 |

### 3. 配置加载

在 `application.yml` 的 `data-locations` 末尾追加 `classpath:data-v1-stage-fill.sql`。

### 4. 测试

新增 `FullGameV1StageFillTests.java`，覆盖：
- 所有新增事件存在且每条至少 3 个选项
- 新增事件 min_week/max_week 覆盖目标阶段
- 新增传闻存在，effect_type/effect_target 合法
- 新增奇遇链 step 顺序和 next_step 合法
- 新增周目标 condition_type 合法
- 新增成就 condition_type 合法
- 旧 CP6.4 种子已迁移到 week 15-16（通过原始 ID 查询验证）
- 传闻 week_number 覆盖 6-16

## 不做

- 不新增副本（5 个副本已够用）
- 不新增 NPC（8 个 NPC 已够用）
- 不修改 UI
- 不修改 Phase 1-4 Java 代码
- 不做 opening/rhythm 阶段大量内容（已有 16+16 条专属事件）
- 不改结局匹配规则

## 文件变更清单

| 文件 | 操作 |
|---|---|
| `src/main/resources/data-v1-stage-fill.sql` | **新增** — Phase 5 种子内容 |
| `src/main/resources/data-content-pack-6.sql` | **修改** — CP6.4 种子从 week 4 迁移到 week 15-16 |
| `src/main/resources/application.yml` | **修改** — 加载新 SQL 文件 |
| `src/test/java/.../service/FullGameV1StageFillTests.java` | **新增** — 种子验证测试 |
| docs 更新 | 修改 |
