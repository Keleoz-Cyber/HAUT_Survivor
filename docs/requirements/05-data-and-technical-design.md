# 05. Data And Technical Design

## 当前技术路线

推荐并当前采用：

| 层次 | 技术 |
|---|---|
| 后端 | Java 17+ / Spring Boot 3.3.x |
| 持久层 | MyBatis-Plus |
| 数据库 | MySQL |
| 模板渲染 | Thymeleaf |
| 前端基础 | Bootstrap + 自定义 CSS + 少量 JavaScript |
| 测试 | JUnit 5 / Spring Boot Test / MockMvc |

采用该路线的原因：

- 适合 Java 课程设计；
- 容易展示 MVC、Service、Mapper、数据库表设计；
- 能快速完成登录、任务、后台和副本；
- 比前后端分离更适合短期课程设计交付；
- 后续可以逐步扩展为 REST API 或前后端分离。

## 推荐包结构

```text
cn.haut.survivor
  config
  controller
  domain.entity
  mapper
  service
  service.impl
```

当前项目已经基本按该结构实现。

## 核心数据表

### user

用途：保存登录账号。

关键字段：

- id
- username
- password
- nickname
- role
- status
- create_time
- update_time

规则：

- username 唯一；
- role 区分 USER 和 ADMIN；
- status 用于禁用账号。

### player_profile

用途：保存用户校园角色信息。

关键字段：

- id
- user_id
- player_name
- grade
- major_type
- growth_route
- level
- exp
- current_week
- create_time
- update_time

规则：

- 一个用户当前只需要一个角色；
- exp 可持续增加；
- level 后续可根据 exp 自动计算。

### player_attribute

用途：保存角色当前属性。

关键字段：

- id
- user_id
- academic
- health
- money
- social
- skill
- pressure
- discipline
- update_time

规则：

- 大多数属性范围 0-100；
- pressure 越高越差；
- 每次事件、任务、副本结算都应更新该表。

### campus_location

用途：保存校园地点。

关键字段：

- id
- location_name
- location_type
- description
- campus_area
- visual_theme
- background_image
- icon_key
- status

扩展建议：

- `visual_theme` 用于页面主题；
- `background_image` 用于后续背景图；
- `icon_key` 用于地点图标。

### event

用途：保存随机事件。

关键字段：

- id
- location_id
- event_name
- event_type
- description
- trigger_probability
- min_week
- max_week
- required_attribute
- background_image
- visual_theme
- status

扩展建议：

- 后续支持同一地点多事件随机触发；
- 可增加 required_flag、blocked_flag 等字段；
- 可增加 priority 控制特殊事件优先级。

### event_option

用途：保存事件选项和结算影响。

关键字段：

- id
- event_id
- option_text
- result_text
- academic_change
- health_change
- money_change
- social_change
- skill_change
- pressure_change
- discipline_change
- exp_change
- visual_hint
- status

规则：

- 每个事件建议 2-4 个选项；
- 选项必须有结果文案；
- 属性变化不应全部为正，应体现取舍。

### task

用途：保存普通任务。

关键字段：

- id
- user_id
- task_name
- task_type
- difficulty
- status
- deadline
- exp_reward
- attribute_changes
- create_time
- finish_time

Demo 阶段可以简单实现，完整版本再拆分任务模板和用户任务。

### dungeon

用途：保存副本基础信息。

关键字段：

- id
- dungeon_name
- dungeon_type
- description
- recommended_attribute
- estimated_minutes
- background_image
- status

### dungeon_task

用途：保存副本阶段任务。

关键字段：

- id
- dungeon_id
- task_name
- task_order
- task_type
- task_context
- task_goal
- minigame_type
- status

任务类型示例：

- `single_choice`
- `minigame`
- `quiz`
- `attribute_check`
- `confirm`
- `key_task`

### dungeon_task_option

用途：保存副本阶段选项。

关键字段：

- id
- dungeon_task_id
- option_text
- result_text
- evaluation
- score
- exp_change
- academic_change
- health_change
- money_change
- social_change
- skill_change
- pressure_change
- discipline_change
- next_task_id
- status

### user_dungeon_record

用途：保存用户一次副本挑战记录。

关键字段：

- id
- user_id
- dungeon_id
- current_task_id
- status
- total_score
- risk_flags
- final_evaluation
- start_time
- finish_time

状态：

- `IN_PROGRESS`
- `COMPLETED`
- 后续可扩展 `FAILED`、`ABANDONED`

### user_dungeon_task_record

用途：保存用户某个副本阶段的结算记录。

关键字段：

- id
- user_dungeon_record_id
- dungeon_task_id
- task_type
- selected_option_id
- minigame_result
- attribute_check_result
- result_text
- evaluation
- score
- exp_change
- create_time

## 核心业务规则

### 属性结算

所有属性变化应统一经过 clamp：

```text
new_value = max(0, min(100, old_value + change))
```

经验不需要上限。

### 事件结算

事件选项结算步骤：

1. 校验用户和事件是否存在；
2. 校验选项属于该事件；
3. 应用属性变化；
4. 增加经验；
5. 写入事件记录；
6. 返回结果页模型。

### 副本开始

副本开始步骤：

1. 校验用户已创建角色；
2. 查找当前进行中的记录；
3. 如果存在，继续该记录；
4. 如果不存在，创建新记录并指向第一个阶段。

注意：完成副本后，结果页不能再用“开始或继续副本”拿记录，否则会误开新副本。应按 recordId 回查已完成记录。

### 副本阶段结算

阶段结算步骤：

1. 校验副本记录属于当前用户；
2. 校验状态为 `IN_PROGRESS`；
3. 校验提交的 taskId 是当前阶段；
4. 根据任务类型调用对应结算逻辑；
5. 写入阶段记录；
6. 更新总分、属性、经验、过程标签；
7. 推进到下一阶段；
8. 若无下一阶段，写入最终评价和完成时间。

### 小游戏结算

小游戏应避免前端完全决定结果。前端只提交用户操作结果，后端按规则计算分数。

数据库拼图示例：

- 前端提交 selectedRelations 和 elapsedSeconds；
- 后端定义正确关系集合；
- 后端根据正确数、错误数、用时、技能、过程标签计算分数；
- 后端决定评价和属性变化。

## 测试建议

每个新增业务功能应有测试。

推荐测试层次：

| 层次 | 工具 | 目的 |
|---|---|---|
| Service Tests | Spring Boot Test | 验证业务规则、属性结算、过程标签 |
| Controller Tests | MockMvc | 验证页面路由、模型、权限跳转 |
| Mapper Context Tests | Spring Context | 验证 Mapper 能注入 |
| HTTP Smoke | PowerShell/curl | 验证真实本地流程 |

后续另一个 AI 继续开发时，建议保持测试优先：

1. 先写失败测试；
2. 确认失败原因正确；
3. 再改代码；
4. 跑定向测试；
5. 跑全量测试；
6. 做 HTTP 冒烟。

## 技术风险

| 风险 | 影响 | 建议 |
|---|---|---|
| 功能过多 | 难按时完成 | 先强化 demo 体验，再做后台扩展 |
| 文案硬编码过多 | 后续难维护 | 默认数据先 SQL seed，完整版再后台维护 |
| 小游戏前端结算 | 容易作弊且难测试 | 结算放后端 |
| 表设计过早复杂化 | 开发慢 | Demo 可适度简化，完整版本再拆分 |
| 页面视觉单调 | 展示弱 | 优先地图、事件、副本页视觉增强 |
