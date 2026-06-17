# 前端手绘笔记本风重做设计文档

> 将 HAUT Survivor 全部 23 个 Thymeleaf 页面模板从深色游戏主题改为「暖纸彩墨手绘笔记本风」

---

## 一、设计决策

### 1.1 风格

**暖纸彩墨手绘笔记本风**：米黄纸张底色 + 粗黑描边白色卡片 + 偏移阴影 + 不规则圆角 + 轻微旋转 + 彩色马克笔标签。

### 1.2 范围

全部 23 个页面模板，纯换皮，不改功能逻辑、页面结构、Thymeleaf 变量绑定。

### 1.3 技术方式

纯 CSS 重写 `app.css` + 调整模板 HTML 的 class 和结构（不动 Thymeleaf 变量）。不引入新前端依赖。

### 1.4 约束

- 保留 Bootstrap 5 作为栅格和基础工具类（已通过 CDN 引入）
- 保留所有 Thymeleaf `th:` 属性和变量绑定不变
- 保留所有 Controller、Service、Mapper 逻辑不变
- 保留 `fragments/layout.html` 作为页面骨架，但重写其视觉

---

## 二、设计系统（Design Tokens）

### 2.1 配色

| 用途 | 色值 | 说明 |
|---|---|---|
| 纸张底色 | `#fdf6e3` | 米黄，全局 body 背景 |
| 描边主色 | `#2d3436` | 近黑，所有卡片/标签描边 |
| 正文文字 | `#2d3436` | 主文字 |
| 次要文字 | `#636e72` | 辅助说明 |
| 卡片背景 | `#ffffff` | 白色 |
| 轨道/底纹 | `#dfe6e9` | 属性条轨道、输入框底 |

**强调色（马克笔色）**：

| 名称 | 浅色（底） | 深色（填充/文字） | 用途 |
|---|---|---|---|
| 黄 | `#ffeaa7` | `#fdcb6e` | 金钱、周主题、AP、周目标 |
| 绿 | `#55efc4` | `#00b894` | 健康、低风险、成功、适应阶段 |
| 蓝 | `#a8d8ea` | `#0984e3` | 学业、信息提示 |
| 粉 | `#fab1a0` → `#fd79a8` | `#e84393` | 社交、压力、高风险 |
| 紫 | `#a29bfe` | `#6c5ce7` | 技能、路线倾向、结局 |
| 橙 | `#ffe0b2` | `#e17055` | 副本、DDL、警告 |

### 2.2 字体

```css
--font-handwrite: 'Comic Sans MS', 'Ma Shan Zheng', 'ZCOOL KuaiLe', cursive;
--font-body: -apple-system, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
```

- 标题（h1-h3、卡片标题、区域标题）：`var(--font-handwrite)`，`font-weight: 800`
- 正文、表格、表单：`var(--font-body)`
- 标签/芯片：`var(--font-body)`，`font-weight: 700`

### 2.3 卡片规范

```css
--border-color: #2d3436;
--border-width: 2.5px;
--shadow-offset: 4px;
--shadow-color: #2d3436;

.paper-card {
    background: #fff;
    border: 2.5px solid #2d3436;
    border-radius: 16px 8px 14px 10px;  /* 不规则圆角 */
    box-shadow: 4px 4px 0 #2d3436;     /* 硬偏移阴影，无模糊 */
    padding: 16px 20px;
    /* 轻微旋转通过工具类 .tilt-left/.tilt-right 添加 */
}
```

**旋转工具类**（每张卡片轻微倾斜，活泼但不晕）：
- `.tilt-left { transform: rotate(-0.3deg); }`
- `.tilt-right { transform: rotate(0.3deg); }`
- `.tilt-strong-left { transform: rotate(-0.5deg); }`
- `.tilt-strong-right { transform: rotate(0.5deg); }`

**悬停效果**：
```css
.paper-card:hover {
    transform: translate(-2px, -2px) rotate(0deg);  /* 悬停时回正 */
    box-shadow: 6px 6px 0 #2d3436;
}
```

### 2.4 标签 / 芯片

```css
.paper-chip {
    border: 1.5px solid #2d3436;
    border-radius: 8px 4px 6px 10px;  /* 不规则 */
    font-weight: 700;
    font-size: 0.78rem;
    padding: 3px 10px;
    display: inline-block;
}
```

色变：`.paper-chip--yellow` / `--green` / `--blue` / `--pink` / `--purple` / `--orange`，各自设置对应浅色背景。

### 2.5 按钮

```css
.paper-btn {
    background: #fff;
    border: 2.5px solid #2d3436;
    border-radius: 10px 6px 8px 12px;
    box-shadow: 3px 3px 0 #2d3436;
    font-weight: 700;
    padding: 8px 18px;
    transition: all 0.1s;
}
.paper-btn:hover {
    transform: translate(-1px, -1px);
    box-shadow: 4px 4px 0 #2d3436;
}
.paper-btn:active {
    transform: translate(3px, 3px);  /* 按下去阴影消失 */
    box-shadow: 0 0 0 #2d3436;
}
```

主按钮 `.paper-btn--primary` 用黄色底，危险按钮 `.paper-btn--danger` 用粉/红底，成功按钮 `.paper-btn--success` 用绿底。

### 2.6 属性条

```css
.stat-track {
    background: #dfe6e9;
    border: 1.5px solid #2d3436;
    border-radius: 4px;
    height: 8px;
    overflow: hidden;
}
.stat-fill {
    height: 100%;
    border-radius: 4px;
    transition: width 0.4s ease-out;
}
/* 各属性填充色保持现有渐变 */
```

### 2.7 头像

```css
.paper-avatar {
    border: 2px solid #2d3436;
    border-radius: 50% 45% 50% 40%;  /* 不规则手绘圆 */
}
```

### 2.8 输入框

```css
.paper-input {
    background: #fff;
    border: 2px solid #2d3436;
    border-radius: 8px 4px 6px 10px;
    padding: 8px 12px;
    font-family: var(--font-body);
}
.paper-input:focus {
    outline: none;
    box-shadow: 3px 3px 0 #2d3436;
}
```

### 2.9 动画

| 动画 | 效果 | 应用 |
|---|---|---|
| 页面淡入 | `opacity 0→1` + `translateY(8px→0)` | `.app-shell` |
| 卡片入场 | `scale(0.95→1)` + `opacity(0→1)` | 各卡片，交错延迟 |
| 属性条过渡 | `width` 0.4s ease-out | 属性条填充 |
| 按钮按压 | `translate(3px,3px)` + 阴影消失 | 所有 `.paper-btn` |
| 悬停回正 | `rotate→0` + 上移 | 卡片悬停 |
| 结局揭晓 | `scale(0.9→1)` + 弹跳 | 结局页 |
| 压力脉冲 | `opacity 0.7↔1` | 压力预警标签 |

### 2.10 纸张纹理

全局 body 背景：
```css
background: #fdf6e3;
background-image: radial-gradient(circle, rgba(45,52,54,0.04) 1px, transparent 1px);
background-size: 20px 20px;
```

---

## 三、页面改造详解

### 3.1 第 1 组：认证与角色创建（3 页）

#### auth/login（登录页）
- 米黄纸底居中白色卡片，`.tilt-right`
- 标题「HAUT Survivor」手写体大字，配 📒 emoji
- 用户名/密码输入框用 `.paper-input`
- 登录按钮 `.paper-btn--primary`（黄底）
- 注册链接下方小字
- 错误提示用粉色 `.paper-chip--pink` 横幅

#### auth/register（注册页）
- 同登录页布局，多一个昵称字段
- 「注册并进入」按钮绿色 `.paper-btn--success`

#### player/create（角色创建页）
- 标题「创建你的校园角色」手写体
- 角色名/年级/专业用 `.paper-input`
- **成长路线选择改为 5 张手绘卡片单选**（而非下拉框）：
  - 每张卡片：emoji + 路线名（手写体）+ 初始属性小标签 + 描述
  - 选中状态：边框加粗 + 黄色底 + `transform: scale(1.03)`
  - 未选中：白底 + 轻微透明
  - 实现方式：保留原 `<select name="growthRoute">` 但用 CSS 隐藏，5 张卡片用 radio + label 实现，点击卡片通过 JS 同步选中对应 option，确保表单提交字段名和值不变
- 「进入校园」按钮 `.paper-btn--primary`

### 3.2 第 2 组：核心游戏页面（4 页）

#### fragments/layout（页面骨架）
- HUD 顶部栏：白底卡片 `.tilt-left`，品牌手写体，状态芯片用彩色 `.paper-chip`
- Dock 底部导航：白底卡片，4 项导航，图标 emoji + 手写体小标签
- 移动端 Dock 固定底部，桌面端流式

#### dashboard/index（仪表盘）
- 已出 mockup，按 mockup 实现
- 所有区块用 `.paper-card`，交替 `.tilt-left` / `.tilt-right`
- 属性面板、阶段进度、周主题、周目标、传闻栏、手机桌面入口、熟人列表、成就架、预警横幅全部手绘卡片化

#### map/index（校园地图）
- 顶部标题手写体
- 保留真实校区照片，照片容器用黑描边卡片
- **热点标记改为手绘图钉**：黄底圆点 + 黑描边 + 不规则圆角 + 悬停放大
- 地点卡片网格：8 张 `.paper-card`，每张交替微旋，显示 emoji、名称、描述、探索度条、AP 消耗、前往按钮
- 传闻标记用粉色小圆点角标

#### map/event（事件页）
- **从深色氛围背景改为纸张底 + 手绘场景感**
- 场景容器：白底 `.paper-card` 大卡，顶部彩色条带表示地点类型（蓝=教学楼/绿=图书馆/紫=宿舍等）
- 地点/氛围标签用 `.paper-chip`
- 事件标题手写体大字
- 叙事文案正文体，行距宽松
- 玩家状态条：4 个小 `.paper-chip` 横排（技能/压力/健康/自律）
- **选项按钮**：`.paper-btn` 风格，左侧 6px 彩色条（绿=低风险/黄=中/红=高），悬停上移
- 结果页：属性变化用彩色 `.paper-chip`（绿=上升/红=下降）

#### exploration/index（探索页）
- 标题「校园踩点」手写体
- 8 张地点卡片网格，每张显示探索度条（`.stat-track` + `.stat-fill`）+ 解锁里程碑标记 + 「踩点探索」按钮
- 探索结果页（`exploration/result`）：手绘便签风格，结果类型用大 emoji + 彩色标签，属性变化用 `.paper-chip`

### 3.3 第 3 组：社交与组织（4 页）

#### npc/detail（NPC 详情页）
- 头像用 `.paper-avatar`（不规则圆），配 emoji
- NPC 名字手写体，类型/性格用 `.paper-chip`
- 关系阶段进度条：`.stat-track` + 渐变填充，阶段名手写体标签
- 「设为本周搭子」按钮 `.paper-btn--primary`
- 互动选项列表：每项一张小 `.paper-card`，显示名称、描述、所需熟悉度标签、互动按钮

#### npc/result（NPC 互动结果页）
- 手绘便签风格，互动结果文案 + 属性变化 `.paper-chip` + 熟悉度变化

#### organization/index（组织广场）
- 标题「社团招新广场」手写体
- 8 张组织摊位卡片：类型 emoji + 组织名（手写体）+ 类型标签（彩色 `.paper-chip`）+ 描述 + 推荐属性标签 + 状态标记
- 卡片交替微旋，像贴在公告栏上

#### organization/detail（组织详情页）
- 三态展示：
  - **未发现**：大锁图标 + 探索度要求手写体说明 + 「发现」按钮（禁用态灰底）
  - **已发现**：「申请加入」按钮 `.paper-btn--primary`
  - **已加入**：职位/贡献/声望用手绘小标签横排 + 「参加本周活动」按钮 `.paper-btn--success`
- 活动结果：属性变化 `.paper-chip`

### 3.4 第 4 组：副本与小游戏（4 页）

#### dungeon/index（副本列表）
- 标题「本周挑战」手写体
- 5 张副本海报卡片，**卡片倾斜幅度加大**（±1-2°），像贴在墙上
- 每张：主题 emoji + 副本名（手写体）+ 类型标签 + 难度标签 + 预估时间 + 奖励称号 + 「查看挑战」按钮

#### dungeon/detail（副本封面）
- 大手写体标题
- 副本信息卡：类型/描述/难度/奖励，用 `.paper-chip` 排列
- 阶段路线：手绘时间线，编号圆圈（黑描边不规则圆）+ 任务名 + 连接线（虚线）
- 「开始挑战」按钮 `.paper-btn--primary`

#### dungeon/play（副本进行）
- 场景叙事用大 `.paper-card` 信纸风格，顶部彩色条带
- 任务标题手写体，目标文本用引用块样式（左侧粗黑竖线）
- 小游戏区域：
  - 数据库拼图：黑描边框，表名用等宽字体 `.paper-chip`，选项用复选框卡片
  - Bug 定位：每题一张小卡，症状粉色标签，选项单选按钮
- 倒计时：红色 `.paper-chip` 手写体数字
- 玩家状态条

#### dungeon/result（阶段结果）
- 手绘战报风格：评价标题手写体大字
- 结果叙事 + 属性变化 `.paper-chip`
- 分数汇总用小表格（横线本风格，虚线分隔）
- 副本完成时：最终评价 + 风险标记 + 「返回副本列表」
- 未完成时：「进入下一阶段」按钮

### 3.5 第 5 组：周总结与结局（4 页）

#### week/summary（周总结）
- **手绘周报日记风格**
- 英雄区：周次手写体大字 + 主题 emoji/名称 + 阶段提示 + 评级标签
- 周目标卡：完成状态（绿色✓/黄色进行中）+ 奖励领取按钮
- 属性快照：手绘表格，每行一个属性，变化用绿↑/红↓标注
- 影响回放：每条一张小便签，来源标签 + 描述 + 变化
- NPC 熟人区：横向头像列表
- 成就区：新解锁成就卡片
- 操作按钮：「进入下一周」`.paper-btn--primary` / 「回寝室」/ 「查看历史周报」

#### week/history（历史周报）
- **手绘档案卡堆叠风格**
- 学期档案卡（顶部）：成长关键词标签 + 影响统计 + 关键周 + 关键关系
- 每周影响回放：按周倒序，每周一张 `.paper-card`，轻微旋转交替

#### ending/index（学期结局）
- **手绘奖状风格**
- 结局图标大字 + 结局类型标签 + 结局名（手写体超大字）+ 描述
- 装饰边框：卡片用双层描边模拟奖状框
- 路线评分卡：5 维评分条（`.stat-track`）+ 评级标签（优秀=绿/良好=黄/一般=橙/不足=红）+ 关键证据列表（手绘对勾✓）
- 成长画像：关键词标签 + 描述 + 关键关系 + 关键周
- 学期总结文案
- 「再来一学期」按钮 `.paper-btn--primary`
- 历史结局画廊：小卡片横排

#### exploration/result（探索结果）
- 已在第 2 组描述

### 3.6 第 6 组：管理端与任务（4 页）

#### task/index（任务管理）
- 统一手绘风格
- 创建任务表单：`.paper-input` 输入框 + 难度选择用 `.paper-chip` 单选组
- 任务列表：**横线本风格**，行间用虚线分隔，每行显示名称/类型/难度/状态/完成按钮

#### admin/event-list（事件列表）
- 管理端导航栏用手绘风格
- 「新增事件」按钮 `.paper-btn--primary`
- 事件表格：横线本风格，操作列用小 `.paper-btn`

#### admin/event-form（事件表单）
- 所有输入用 `.paper-input`
- 地点选择用 `.paper-input` select
- 提交按钮 `.paper-btn--primary`

#### fragments/layout（已在第 2 组描述）
- 管理端和任务页不显示游戏 HUD 和 Dock，用简单的手绘导航栏

---

## 四、改造顺序

1. **设计系统 CSS**：在 `app.css` 中写入所有 Design Tokens 和工具类（`.paper-card` / `.paper-chip` / `.paper-btn` / `.paper-input` / `.paper-avatar` / `.stat-track` / 倾斜工具类 / 动画）
2. **fragments/layout**：重写骨架（HUD + Dock + body 背景）
3. **认证组**：login → register → player/create
4. **核心组**：dashboard → map/index → map/event → exploration/index + result
5. **社交组**：npc/detail → npc/result → organization/index → organization/detail
6. **副本组**：dungeon/index → detail → play → result
7. **周总结组**：week/summary → week/history → ending/index
8. **管理端组**：task/index → admin/event-list → admin/event-form

---

## 五、不动的东西

以下内容**完全不改动**：

- 所有 Java 代码（Controller / Service / Mapper / Entity / Config）
- 所有 SQL 文件（schema.sql / data*.sql）
- `application.yml` 配置
- Thymeleaf 模板中的所有 `th:` 属性和变量表达式
- 所有页面的功能逻辑、数据流、跳转路径
- Bootstrap 5 CDN 引用（保留作为栅格基础）
- 静态图片资源（校区地图照片等）

---

## 六、验证标准

- 所有页面在浏览器中正常渲染，无样式错乱
- 所有功能（登录/探索/事件/副本/NPC/组织/周推进/结局）正常工作
- 移动端和桌面端响应式正常
- 视觉风格统一为暖纸彩墨手绘笔记本风
- 无控制台报错
