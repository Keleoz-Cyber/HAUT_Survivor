# Content Pack 4: 传闻、周主题与探索奇遇机制化设计

## 1. 设计目标

Content Pack 4 的目标是把当前“展示型”的周主题、校园传闻和探索提示升级为会影响玩家决策的轻量机制。

当前项目已经具备：

- 4 周 Demo 学期主题：开学适应周、社团招新周、DDL 高压周、期末与体测周。
- `rumor` 表和 `RumorService`：每周稳定抽取 2-3 条传闻展示。
- 8 个校园地点和探索度系统。
- 事件系统、副本系统、组织系统、NPC 搭子系统、周目标、成就、周总结。
- UI 2.0 的 dashboard、map、exploration、event、dungeon、organization、ending 等玩家端页面。

当前问题：

- 周主题主要是文案，不改变玩法。
- 传闻主要是提示，不改变地点收益或风险。
- 探索结果是单次随机结算，缺少连续故事和“校园正在发生事”的感觉。
- NPC 搭子只影响 NPC 主动互动，还没有进入探索/事件/副本的日常节奏。

本内容包要让玩家产生更明确的判断：

> “这周传闻说图书馆有事，我要不要去赌一把？”
> “这周是 DDL 高压，去实验室可能收益高，但压力会爆。”
> “我选了小马当搭子，今天去操场是不是更稳？”

核心体验目标：

- 传闻从氛围提示变成可被利用的“校园情报”。
- 周主题影响不同周的收益、风险和事件倾向。
- 探索形成短奇遇链，而不是每次都像抽独立卡片。
- NPC 搭子开始在探索或事件中产生小型救场/加成。
- 不引入过重系统，继续保持 Demo 可控和测试可覆盖。

## 2. 范围边界

### 本次要做

- 新增“传闻效果”机制，让指定传闻影响地点探索、事件触发或属性变化。
- 新增“周主题效果”机制，让不同周对某些行动有轻量全局修正。
- 新增“探索奇遇链”机制，让特定地点在探索时可能触发 2-3 步短故事。
- 新增少量传闻、周目标和成就，围绕情报利用、奇遇完成、搭子救场。
- 在探索结果页、地图页、周总结中展示机制化反馈。
- 给 NPC 搭子增加极小范围的探索/事件加成，不做大范围复杂联动。
- 补充 Service、Controller、seed smoke、页面渲染测试。

### 本次不做

- 不做完整天气系统。
- 不做动态经济系统。
- 不做复杂事件权重引擎。
- 不做自由剧情编辑器。
- 不做大型多分支长剧情。
- 不做前端大规模重构。
- 不做管理员 CRUD 扩展。
- 不做真实 AI 文案生成。

## 3. 设计方案选择

### 方案 A：只给传闻加属性奖励

做法：传闻命中地点后，探索结果额外加一点属性或探索度。

优点：实现快，表结构简单。

缺点：玩法感偏薄，玩家很快会把传闻看成“加成标签”。

### 方案 B：传闻 + 周主题 + 奇遇链三者轻量联动

做法：传闻提供地点情报，周主题提供全局倾向，探索奇遇链提供连续反馈。

优点：能显著提升“校园正在发生事”的感觉；仍然可用少量表和服务完成。

缺点：需要更谨慎设计测试，避免随机性导致不稳定。

### 方案 C：做完整校园动态系统

做法：每周生成校园状态、地点状态、NPC 行程、组织活动波动、事件权重。

优点：最像人生模拟器。

缺点：范围过大，不适合当前 Demo 阶段。

### 推荐选择

采用方案 B。

理由：CP4 应该显著提高可玩性，但不能把项目拖进大型模拟引擎。方案 B 可以复用现有周主题、传闻、探索、NPC、周总结结构，只新增少量状态表和效果解释服务，收益明显、风险可控。

## 4. 核心机制

## 4.1 周主题效果

周主题仍由 `WeeklyThemeService` 提供基础信息，但新增一个“机制效果解释层”。

建议新增服务：

```java
WeeklyModifierService
```

职责：

- 根据当前周次返回本周机制倾向。
- 为探索、事件、组织、NPC 遇见提供轻量修正。
- 给页面返回解释文案，避免玩家看不懂为什么奖励变化。

建议周主题效果：

| 周次 | 主题 | 机制效果 |
|---:|---|---|
| 1 | 开学适应周 | 探索度收益 +1；NPC 遇见概率略高；压力变化较温和 |
| 2 | 社团招新周 | 组织相关地点事件更活跃；社交收益 +1；组织活动贡献 +1 |
| 3 | DDL 高压周 | 学业/技能收益 +1；压力增加类结果 +1；实验室/图书馆事件更容易出现 |
| 4 | 期末与体测周 | 健康/学业判定更重要；操场和图书馆传闻效果增强；压力保持目标更有价值 |

MVP 约束：

- 周主题效果只做小幅修正，通常为 `-1 / +1 / +2`。
- 修正必须返回说明，例如“DDL 高压周：技能收益 +1，但压力也更容易上升”。
- 不直接重写现有事件选择算法，优先在探索结果、组织活动、NPC 遇见和少量事件触发处接入。

## 4.2 传闻效果

当前 `rumor` 表有：

- `week_number`
- `location_id`
- `rumor_title`
- `rumor_text`
- `effect_hint`
- `rarity`
- `active`

本次建议新增轻量字段，而不是新建复杂效果表：

| 字段 | 类型 | 说明 |
|---|---|---|
| effect_type | VARCHAR(50) | 效果类型，如 `explore_bonus`、`pressure_risk`、`npc_boost` |
| effect_value | INT | 效果数值，通常 1-5 |
| effect_target | VARCHAR(50) | 目标属性或机制，如 `skill`、`pressure`、`npc`、`event` |

如果担心改表影响已有逻辑，也可以新建 `rumor_effect` 表。但 MVP 推荐直接扩展 `rumor`，因为当前传闻数据量小，查询简单。

建议效果类型：

| effect_type | 用途 | 示例 |
|---|---|---|
| explore_bonus | 探索该地点时额外增加探索度 | 图书馆“空座传闻”让探索度 +2 |
| attr_bonus | 探索该地点时额外增加指定属性 | 实验室“白板复盘”让技能 +2 |
| pressure_risk | 探索或事件后压力额外变化 | DDL 周实验室压力 +1 |
| npc_boost | 提高该地点 NPC 遇见概率或熟悉度收益 | 食堂“拼桌局”提高社交 NPC 遇见概率 |
| event_hint | 提高地点事件出现概率，但不强制触发 | 教学楼“小测风声”更容易触发学业事件 |
| safe_zone | 抵消一次轻微负面变化 | 宿舍“推迟断电”抵消压力 +1 |

传闻效果原则：

- 只有玩家本周看到的传闻才生效。
- 地点型传闻只影响对应地点。
- common 传闻效果小，rare 传闻效果更明显。
- 传闻效果必须在结果页展示，例如“传闻生效：图书馆空座传闻，探索度 +2”。
- 传闻不应让结果完全确定，只提供倾向或小加成。

## 4.3 探索奇遇链

探索奇遇链用于把“单次探索结果”升级为 2-3 步短故事。

建议新增表：

```sql
exploration_story_chain
exploration_story_progress
```

### exploration_story_chain

用于定义奇遇链。

| 字段 | 说明 |
|---|---|
| id | 主键 |
| chain_key | 唯一 key |
| chain_name | 奇遇链名称 |
| location_id | 所属地点 |
| week_number | 可选，限制周次；0 表示不限 |
| required_explore_level | 需要探索等级 |
| step_number | 第几步 |
| scenario_text | 情境文案 |
| result_text | 结算文案 |
| academic_change / health_change / ... | 属性变化 |
| pressure_change | 压力变化 |
| exp_change | 经验变化 |
| next_step_number | 下一步 |
| completion_reward_type | 完成奖励类型，可为空 |
| active | 是否启用 |

### exploration_story_progress

记录玩家奇遇进度。

| 字段 | 说明 |
|---|---|
| id | 主键 |
| user_id | 玩家 |
| chain_key | 奇遇链 key |
| current_step | 当前步骤 |
| completed | 是否完成 |
| last_trigger_week | 最近触发周 |
| update_time | 更新时间 |

MVP 规则：

- 玩家探索地点时，有小概率触发该地点可用奇遇链。
- 同一奇遇链完成后不再重复触发。
- 每次探索最多触发 1 个奇遇链步骤。
- 奇遇链不需要自由选择，先做“情境 + 自动结算 + 下一步提示”。
- 后续可扩展为“情境选择式奇遇”，但本次先避免复杂前端。

建议首批奇遇链：

| 地点 | 奇遇链 | 步数 | 主题 |
|---|---|---:|---|
| 图书馆 | 被占的自习座 | 3 | 抢座、让座、固定学习角 |
| 实验室 | 白板上的神秘报错 | 3 | 师兄留下的报错线索 |
| 操场 | 夜跑打卡局 | 3 | 和小马/陌生同学形成运动节奏 |
| 食堂 | 拼桌情报局 | 2 | 听到社团/课程/副本传闻 |
| 宿舍 | 熄灯后的寝室会议 | 2 | 室友阿杰的压力缓冲 |

## 4.4 NPC 搭子轻量外溢

CP3 中，本周搭子只影响 NPC 主动互动。CP4 可以给它一点外溢，但必须克制。

建议规则：

| 搭子 | 探索/事件外溢 |
|---|---|
| 阿杰 | 宿舍探索时，压力增加类结果有 30% 概率少 +1 |
| 林然 | 图书馆/教学楼探索时，学业收益有 30% 概率 +1 |
| 周予 | 食堂/社团区探索时，传闻生效提示更容易出现；NPC 遇见概率 +10% |
| 老郑 | 实验室探索或副本阶段完成时，技能收益有 30% 概率 +1 |
| 小马 | 操场探索时，健康减少类结果有 30% 概率抵消 1 |

实现原则：

- 搭子外溢只在当前周有效。
- 外溢触发后必须在结果页展示。
- 不改变所有行动，只覆盖探索结果和少量事件结果。
- 失败也可以展示“搭子提示”，但不要让玩家以为必定生效。

## 4.5 结果展示

探索结果页应新增“本次影响来源”区域，展示：

- 周主题修正。
- 传闻生效。
- 搭子加成。
- 奇遇链进度。

示例：

```text
本次影响来源

DDL 高压周：技能收益 +1，压力风险 +1
传闻生效：老郑的白板建议，技能 +2
搭子加成：师兄老郑帮你少走了一点弯路，技能 +1
奇遇进度：白板上的神秘报错 2/3
```

如果没有任何修正，不展示该区域，避免页面噪音。

## 5. 数据设计

## 5.1 扩展 rumor

推荐新增字段：

```sql
ALTER TABLE rumor
ADD effect_type VARCHAR(50),
ADD effect_value INT NOT NULL DEFAULT 0,
ADD effect_target VARCHAR(50);
```

由于项目 schema 每次启动重建，实际实现时应直接修改 `schema.sql` 中的 `rumor` 表定义，并更新 `data.sql`、`data-content-pack-*.sql` 的插入字段。

## 5.2 新增 exploration_story_chain

```sql
CREATE TABLE IF NOT EXISTS exploration_story_chain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_key VARCHAR(80) NOT NULL,
    chain_name VARCHAR(100) NOT NULL,
    location_id BIGINT NOT NULL,
    week_number INT NOT NULL DEFAULT 0,
    required_explore_level INT NOT NULL DEFAULT 0,
    step_number INT NOT NULL,
    scenario_text TEXT,
    result_text TEXT,
    academic_change INT NOT NULL DEFAULT 0,
    health_change INT NOT NULL DEFAULT 0,
    money_change INT NOT NULL DEFAULT 0,
    social_change INT NOT NULL DEFAULT 0,
    skill_change INT NOT NULL DEFAULT 0,
    pressure_change INT NOT NULL DEFAULT 0,
    discipline_change INT NOT NULL DEFAULT 0,
    exp_change INT NOT NULL DEFAULT 0,
    next_step_number INT,
    active INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_chain_step (chain_key, step_number),
    CONSTRAINT fk_esc_location FOREIGN KEY (location_id) REFERENCES campus_location(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 5.3 新增 exploration_story_progress

```sql
CREATE TABLE IF NOT EXISTS exploration_story_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    chain_key VARCHAR(80) NOT NULL,
    current_step INT NOT NULL DEFAULT 1,
    completed INT NOT NULL DEFAULT 0,
    last_trigger_week INT,
    update_time DATETIME,
    UNIQUE KEY uk_user_chain (user_id, chain_key),
    CONSTRAINT fk_esp_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 6. 服务设计

## 6.1 WeeklyModifierService

建议接口：

```java
WeeklyModifier getModifier(int weekNumber);
ExplorationModifier getExplorationModifier(Long userId, Long locationId, int weekNumber);
```

职责：

- 根据周次返回探索、属性、压力和 NPC 概率倾向。
- 不直接修改数据库。
- 返回解释文本，供结果页展示。

## 6.2 RumorEffectService

建议接口：

```java
List<RumorEffect> getActiveEffectsForUser(Long userId, int weekNumber, Long locationId);
RumorEffectResult applyToExploration(...);
```

职责：

- 只使用玩家本周可见传闻。
- 根据地点过滤传闻效果。
- 给探索结果附加修正和解释。

注意：

- 当前 `RumorService.pickRumorsForUser()` 是稳定抽取，但不落库。
- 如果要保证“页面看到的传闻”和“行动生效的传闻”完全一致，需让同一算法在服务层复用，不要在 Controller 里各自随机。

## 6.3 ExplorationStoryService

建议接口：

```java
Optional<ExplorationStoryResult> maybeTrigger(Long userId, Long locationId, int weekNumber);
```

职责：

- 查找当前地点可用奇遇链。
- 根据探索等级、周次、完成状态过滤。
- 触发一个步骤，应用属性变化，推进进度。
- 返回结果和展示文案。

触发概率建议：

- 基础概率 25%。
- 如果本周有该地点相关 rare 传闻，概率 +10%。
- 如果当前地点探索等级越高，可逐步提高概率。
- 每次探索最多触发 1 条奇遇链。

## 6.4 ExplorationService 接入

当前 `ExplorationService.explore()` 已经负责：

- 消耗 AP。
- 增加探索度。
- 修改属性。
- 返回 `ExplorationResult`。

CP4 建议扩展返回结果，而不是新建 Controller 流程：

```java
record ExplorationInfluence(
    String sourceType,
    String sourceName,
    String description,
    AttributeChange attributeChange,
    Integer exploreBonus
)
```

`ExplorationResult` 增加：

```java
List<ExplorationInfluence> influences
Optional<ExplorationStoryResult> storyResult
```

如果不想改 record 太多，也可以给 Controller 额外查询展示信息，但推荐放在 service result 中，测试更稳定。

## 7. 内容设计

## 7.1 新增传闻方向

建议新增 16 条机制型传闻：

| 周次 | 数量 | 方向 |
|---:|---:|---|
| 1 | 4 | 开学问路、宿舍拼单、图书馆空座、食堂新窗口 |
| 2 | 4 | 社团招新、篮球社夜训、学生会点名、实验室开放 |
| 3 | 4 | DDL 爆炸、实验室白板、图书馆通宵、Git 合并求救 |
| 4 | 4 | 体测路线、考前资料、食堂补给、宿舍早睡联盟 |

示例：

| 标题 | 地点 | 效果 |
|---|---|---|
| 图书馆二楼突然空了一排座 | 图书馆 | `explore_bonus +2` |
| 老郑的白板还没擦 | 实验室 | `attr_bonus skill +2` |
| 操场今晚有人组夜跑 | 操场 | `attr_bonus health +2` |
| 食堂三楼有人拼桌聊课设 | 食堂 | `npc_boost +10` |
| 宿舍今晚不断电传闻 | 宿舍 | `safe_zone pressure 1` |

## 7.2 奇遇链首批内容

### 图书馆：被占的自习座

1. 你发现一个总被书包占着的位置。
2. 你终于遇见座位主人，对方其实也在赶 DDL。
3. 你们形成“谁先到谁占座”的默契。

效果倾向：学业、自律增加，压力小幅增加。

### 实验室：白板上的神秘报错

1. 白板上留下了一串报错和箭头。
2. 你根据线索定位到一个项目结构问题。
3. 老郑发现你看懂了，开始把你当半个自己人。

效果倾向：技能增加，压力增加，可能提高老郑熟悉度。

### 操场：夜跑打卡局

1. 你看到一群人在操场边喊“今天第三天”。
2. 小马邀请你一起跑一圈。
3. 你开始把夜跑当成减压手段。

效果倾向：健康增加，压力降低，自律增加。

### 食堂：拼桌情报局

1. 你被迫和陌生同学拼桌。
2. 你听到一个关于社团/课设/副本的真实情报。

效果倾向：社交增加，传闻刷新或额外展示 1 条。

### 宿舍：熄灯后的寝室会议

1. 熄灯后大家开始聊最近谁最惨。
2. 阿杰提出一个离谱但有用的减压方案。

效果倾向：压力降低，社交增加，自律可能降低。

## 7.3 新增周目标

| key | 名称 | 类型 | 目标 | 奖励 |
|---|---|---|---:|---|
| rumor_hunter | 情报猎人 | rumor_effect_used | 2 | 经验 +35，社交 +2 |
| story_chaser | 校园奇遇追踪者 | exploration_story_step | 2 | 经验 +40，技能 +2 |
| theme_survivor | 顺势而为 | weekly_modifier_used | 2 | 经验 +30，自律 +2 |
| buddy_rescue | 搭子救场 | buddy_assist | 1 | 经验 +35，压力 -3 |

## 7.4 新增成就

| key | 名称 | 条件 | 称号 |
|---|---|---|---|
| first_rumor_effect | 听劝一次 | 第一次触发传闻效果 | 情报新生 |
| story_first_step | 奇遇开端 | 第一次触发奇遇链 | 校园目击者 |
| story_completed | 有始有终 | 完成 1 条奇遇链 | 支线清理大师 |
| theme_master | 看懂周节奏 | 触发 3 次周主题效果 | 节奏感选手 |
| buddy_saved_me | 搭子救我 | 触发 1 次搭子外溢加成 | 有人罩着 |

## 8. UI/UX 设计

## 8.1 Map 页面

地图节点可增加轻量情报标记：

- 有本周地点传闻：显示“有传闻”标签。
- 有可用奇遇链：显示“可能有事发生”标签。
- 当前周主题强化地点：显示“本周热点”标签。

注意：

- 标签数量最多 2 个，避免地图变成信息噪音。
- 移动端节点卡片不能被 Dock 遮挡。

## 8.2 Exploration 页面

探索卡片增加：

- 本周传闻提示。
- 已触发奇遇链进度。
- 周主题提示。

示例：

```text
图书馆
传闻：二楼突然空了一排座
奇遇：被占的自习座 1/3
本周：DDL 高压，学业收益更高但压力也更高
```

## 8.3 Exploration Result 页面

结果页新增“影响来源”区域。

建议复用 `vn-result__changes` 或新增：

```css
.influence-panel
.influence-chip
.story-progress-card
```

展示优先级：

1. 奇遇链结果。
2. 传闻效果。
3. 周主题修正。
4. 搭子加成。

## 8.4 Week Summary 页面

周总结新增：

- 本周触发了多少次传闻效果。
- 是否触发/推进奇遇链。
- 是否利用了周主题。
- 搭子是否救场。

文案示例：

```text
这周你终于开始听校园传闻了。虽然有些消息像玄学，但至少图书馆那条是真的。
```

## 9. 测试设计

## 9.1 Seed Smoke Tests

新增 `ContentPack4Tests`：

- 机制型传闻字段存在且 seed 数量正确。
- 奇遇链 seed 至少 5 条 chain、每条 2-3 步。
- 新周目标、成就存在。

## 9.2 Service Tests

`WeeklyModifierServiceTests`：

- 第 1 周探索收益修正。
- 第 3 周 DDL 压力/技能修正。
- 无效周次返回默认修正。

`RumorEffectServiceTests`：

- 只对本周可见传闻生效。
- 地点不匹配不生效。
- rare 传闻效果大于 common。

`ExplorationStoryServiceTests`：

- 符合地点和探索等级时可触发奇遇。
- 完成后不重复触发。
- 触发步骤会推进 progress。

`ExplorationServiceTests`：

- 探索结果包含 influences。
- 传闻/周主题/搭子加成显示实际 delta。
- clamp 边界下 influence 显示实际效果。

## 9.3 Controller / Template Tests

- exploration result model 包含 influence/stories。
- map/exploration 页面能展示传闻和奇遇提示。
- week summary 能展示 CP4 统计。
- 所有新增页面/模板无 Thymeleaf 解析错误。

## 9.4 Full Verification

最终必须运行：

```powershell
.\mvnw.cmd clean test
```

并做 HTTP 冒烟：

- `/dashboard`
- `/map`
- `/exploration`
- 探索一次进入 `/exploration/result`
- `/week/summary`

期望：

- HTTP 200。
- 无 Whitelabel。
- 玩家端页面含 `game-dock`。
- 结果页能看到影响来源区域。

## 10. 实施顺序建议

1. 更新 schema 和 seed：rumor 字段、奇遇链表、CP4 种子。
2. 增加 seed smoke tests。
3. 实现 `WeeklyModifierService`。
4. 实现 `RumorEffectService`。
5. 实现 `ExplorationStoryService`。
6. 扩展 `ExplorationService.ExplorationResult`。
7. 接入探索结果页和地图/探索页提示。
8. 接入周目标、成就、周总结统计。
9. 全量测试和 HTTP 冒烟。

## 11. 风险与约束

| 风险 | 处理方式 |
|---|---|
| 随机触发导致测试不稳定 | Service 暴露可控方法或测试直接调用判定逻辑，不依赖概率 |
| 结果页信息过多 | 无修正时隐藏影响来源，有修正时最多展示 4 条 |
| 效果叠加破坏平衡 | 单项修正控制在 1-2，rare 最多 3 |
| 表结构变多 | 只新增 2 张奇遇表，传闻直接扩字段 |
| 周主题机制过度复杂 | 只做固定周次映射，不做动态生成 |
| 搭子加成过强 | 采用概率触发或小幅抵消，不做必定大幅收益 |

## 12. 验收标准

完成后应满足：

- 玩家能在 map/exploration 看到本周哪些地点有传闻或奇遇倾向。
- 探索结果页能明确展示“为什么这次多了收益/风险”。
- 至少 5 条奇遇链可触发并推进。
- 至少 8 条机制型传闻可实际影响探索。
- 周主题至少影响探索结果或事件倾向。
- 本周搭子至少能在探索中触发 1 类外溢加成。
- 新增周目标和成就能被正常触发。
- 周总结能提到传闻、奇遇或搭子救场。
- `.\mvnw.cmd clean test` 全绿。

## 13. 给下一个 AI 的提示词

```text
继续开发 HAUT Survivor。当前任务是实现 Content Pack 4：传闻、周主题与探索奇遇机制化。

请先阅读：
- docs/PROJECT_COMPLETION_STATUS.md
- docs/NEXT_AI_HANDOFF.md
- docs/superpowers/specs/2026-06-08-content-pack-4-rumor-theme-exploration-design.md

当前要求：
1. 不要重写项目，不要大规模改 UI。
2. 先根据设计文档写实施计划，放到 docs/superpowers/plans/。
3. 使用 TDD，小步实现。
4. 优先实现 schema/seed smoke tests，再做 service，再接 controller/template。
5. 保持玩家端 UI 使用 game-shell/game-hud/game-dock。
6. 最后必须运行 .\mvnw.cmd clean test，并做 dashboard/map/exploration/week summary HTTP 冒烟。

重点目标：
- 让传闻实际影响探索/地点收益。
- 让周主题实际影响每周机制。
- 增加 2-3 步探索奇遇链。
- 让 NPC 搭子在探索中有轻量外溢加成。
- 在探索结果页展示影响来源。
```
