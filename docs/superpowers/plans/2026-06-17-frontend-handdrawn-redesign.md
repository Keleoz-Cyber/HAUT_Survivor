# 前端手绘笔记本风重做 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 HAUT Survivor 全部 23 个 Thymeleaf 页面模板从深色游戏主题改为「暖纸彩墨手绘笔记本风」。

**Architecture:** 纯 CSS 重写 `app.css` 中的设计系统层，然后逐页调整模板 HTML 的 class 和结构。保留所有 Thymeleaf `th:` 属性和变量绑定不变，保留 Bootstrap 5 作为栅格基础，不改任何 Java/SQL/配置。

**Tech Stack:** Thymeleaf + Bootstrap 5 + 纯 CSS（无新依赖）

**Spec:** `docs/superpowers/specs/2026-06-17-frontend-handdrawn-redesign.md`

---

## File Structure

### 修改的文件

| 文件 | 职责 |
|---|---|
| `src/main/resources/static/css/app.css` | **完全重写**。写入设计系统 tokens + 所有 `.paper-*` 工具类 + 全局背景 + 动画。删除旧深色主题样式。 |
| `src/main/resources/templates/fragments/layout.html` | 重写 HUD/Dock/admin-nav 的 HTML class |
| `src/main/resources/templates/auth/login.html` | 替换 class 为手绘风 |
| `src/main/resources/templates/auth/register.html` | 替换 class 为手绘风 |
| `src/main/resources/templates/player/create.html` | 替换 class + 成长路线改卡片单选 |
| `src/main/resources/templates/dashboard/index.html` | 替换所有 section class |
| `src/main/resources/templates/map/index.html` | 替换 class + 热点标记样式 |
| `src/main/resources/templates/map/event.html` | 替换 class + 舞台背景 |
| `src/main/resources/templates/exploration/index.html` | 替换 class |
| `src/main/resources/templates/exploration/result.html` | 替换 class |
| `src/main/resources/templates/npc/detail.html` | 替换 class |
| `src/main/resources/templates/npc/result.html` | 替换 class |
| `src/main/resources/templates/organization/index.html` | 替换 class |
| `src/main/resources/templates/organization/detail.html` | 替换 class |
| `src/main/resources/templates/dungeon/index.html` | 替换 class |
| `src/main/resources/templates/dungeon/detail.html` | 替换 class |
| `src/main/resources/templates/dungeon/play.html` | 替换 class |
| `src/main/resources/templates/dungeon/result.html` | 替换 class |
| `src/main/resources/templates/week/summary.html` | 替换 class |
| `src/main/resources/templates/week/history.html` | 替换 class |
| `src/main/resources/templates/ending/index.html` | 替换 class |
| `src/main/resources/templates/task/index.html` | 替换 class |
| `src/main/resources/templates/admin/event-list.html` | 替换 class |
| `src/main/resources/templates/admin/event-form.html` | 替换 class |

### 不改的文件
- 所有 Java 代码、SQL、`application.yml`、静态图片

---

## Task 1: 设计系统 CSS 基础

**Files:**
- Modify: `src/main/resources/static/css/app.css`（完全重写前半部分：tokens + 工具类）

- [ ] **Step 1: 备份当前 app.css**

Run: `Copy-Item "src\main\resources\static\css\app.css" "src\main\resources\static\css\app.css.bak"`

- [ ] **Step 2: 重写 app.css — 写入 CSS 变量 + 全局背景 + 字体**

将 `app.css` 开头替换为以下内容（保留文件后续旧样式暂时不动，后续 task 逐步删除）：

```css
/* ============================================================
   HAUT Survivor · 暖纸彩墨手绘笔记本风 设计系统
   ============================================================ */

:root {
    /* 纸张配色 */
    --paper-bg: #fdf6e3;
    --paper-card: #ffffff;
    --ink: #2d3436;
    --ink-muted: #636e72;
    --track: #dfe6e9;

    /* 马克笔色（浅底 / 深填充） */
    --yellow-light: #ffeaa7;
    --yellow-deep: #fdcb6e;
    --green-light: #55efc4;
    --green-deep: #00b894;
    --blue-light: #a8d8ea;
    --blue-deep: #0984e3;
    --pink-light: #fab1a0;
    --pink-deep: #e84393;
    --purple-light: #a29bfe;
    --purple-deep: #6c5ce7;
    --orange-light: #ffe0b2;
    --orange-deep: #e17055;

    /* 描边/阴影 */
    --border-color: #2d3436;
    --border-width: 2.5px;
    --shadow-offset: 4px;
    --shadow-color: #2d3436;

    /* 字体 */
    --font-handwrite: 'Comic Sans MS', 'Ma Shan Zheng', 'ZCOOL KuaiLe', cursive;
    --font-body: -apple-system, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ---------- 全局背景：米黄纸 + 墨点纹理 ---------- */

body {
    background: var(--paper-bg);
    background-image: radial-gradient(circle, rgba(45, 52, 54, 0.04) 1px, transparent 1px);
    background-size: 20px 20px;
    color: var(--ink);
    font-family: var(--font-body);
    min-height: 100vh;
}

/* ---------- 手写体标题 ---------- */

h1, h2, h3, .handwrite {
    font-family: var(--font-handwrite);
    font-weight: 800;
    color: var(--ink);
}
```

- [ ] **Step 3: 写入卡片工具类**

在 CSS 变量之后追加：

```css
/* ---------- 纸卡片 ---------- */

.paper-card {
    background: var(--paper-card);
    border: var(--border-width) solid var(--border-color);
    border-radius: 16px 8px 14px 10px;
    box-shadow: var(--shadow-offset) var(--shadow-offset) 0 var(--shadow-color);
    padding: 16px 20px;
    margin-bottom: 16px;
    transition: transform 0.15s ease, box-shadow 0.15s ease;
    position: relative;
}

.paper-card:hover {
    transform: translate(-2px, -2px) rotate(0deg);
    box-shadow: 6px 6px 0 var(--shadow-color);
}

/* 倾斜工具类 */
.tilt-left  { transform: rotate(-0.3deg); }
.tilt-right { transform: rotate(0.3deg); }
.tilt-strong-left  { transform: rotate(-0.5deg); }
.tilt-strong-right { transform: rotate(0.5deg); }

/* 倾斜类 hover 时回正 */
.tilt-left:hover, .tilt-right:hover,
.tilt-strong-left:hover, .tilt-strong-right:hover {
    transform: translate(-2px, -2px) rotate(0deg);
}
```

- [ ] **Step 4: 写入标签/芯片工具类**

```css
/* ---------- 彩色标签 ---------- */

.paper-chip {
    border: 1.5px solid var(--border-color);
    border-radius: 8px 4px 6px 10px;
    font-weight: 700;
    font-size: 0.78rem;
    padding: 3px 10px;
    display: inline-block;
    color: var(--ink);
    text-decoration: none;
}

.paper-chip--yellow  { background: var(--yellow-light); }
.paper-chip--green   { background: var(--green-light); }
.paper-chip--blue    { background: var(--blue-light); }
.paper-chip--pink    { background: var(--pink-light); }
.paper-chip--purple  { background: var(--purple-light); }
.paper-chip--orange  { background: var(--orange-light); }
.paper-chip--white   { background: #fff; }
```

- [ ] **Step 5: 写入按钮工具类**

```css
/* ---------- 手绘按钮 ---------- */

.paper-btn {
    background: var(--paper-card);
    border: 2.5px solid var(--border-color);
    border-radius: 10px 6px 8px 12px;
    box-shadow: 3px 3px 0 var(--shadow-color);
    color: var(--ink);
    font-weight: 700;
    font-size: 0.9rem;
    padding: 8px 18px;
    text-decoration: none;
    display: inline-block;
    transition: all 0.1s ease;
    cursor: pointer;
}

.paper-btn:hover {
    transform: translate(-1px, -1px);
    box-shadow: 4px 4px 0 var(--shadow-color);
    color: var(--ink);
}

.paper-btn:active {
    transform: translate(3px, 3px);
    box-shadow: 0 0 0 var(--shadow-color);
}

.paper-btn--primary { background: var(--yellow-light); }
.paper-btn--success { background: var(--green-light); }
.paper-btn--danger  { background: var(--pink-light); }
.paper-btn:disabled,
.paper-btn--disabled {
    background: #e0e0e0;
    color: var(--ink-muted);
    box-shadow: 2px 2px 0 #b0b0b0;
    cursor: not-allowed;
    opacity: 0.6;
}
```

- [ ] **Step 6: 写入输入框、属性条、头像工具类**

```css
/* ---------- 输入框 ---------- */

.paper-input {
    background: #fff;
    border: 2px solid var(--border-color);
    border-radius: 8px 4px 6px 10px;
    padding: 8px 12px;
    font-family: var(--font-body);
    font-size: 0.95rem;
    color: var(--ink);
    width: 100%;
    transition: box-shadow 0.15s;
}

.paper-input:focus {
    outline: none;
    box-shadow: 3px 3px 0 var(--shadow-color);
}

/* ---------- 属性条 ---------- */

.stat-track {
    background: var(--track);
    border: 1.5px solid var(--border-color);
    border-radius: 4px;
    height: 8px;
    overflow: hidden;
    flex: 1;
}

.stat-fill {
    height: 100%;
    border-radius: 4px;
    transition: width 0.4s ease-out;
}

.stat-fill--academic   { background: linear-gradient(90deg, #74b9ff, #0984e3); }
.stat-fill--health     { background: linear-gradient(90deg, #55efc4, #00b894); }
.stat-fill--money      { background: linear-gradient(90deg, #ffeaa7, #fdcb6e); }
.stat-fill--social     { background: linear-gradient(90deg, #fab1a0, #e84393); }
.stat-fill--skill      { background: linear-gradient(90deg, #a29bfe, #6c5ce7); }
.stat-fill--pressure   { background: linear-gradient(90deg, #fab1a0, #e17055); }
.stat-fill--discipline { background: linear-gradient(90deg, #a8d8ea, #0984e3); }

/* ---------- 头像（不规则手绘圆） ---------- */

.paper-avatar {
    border: 2px solid var(--border-color);
    border-radius: 50% 45% 50% 40%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}
```

- [ ] **Step 7: 写入动画**

```css
/* ---------- 动画 ---------- */

@keyframes pageFadeIn {
    from { opacity: 0; transform: translateY(8px); }
    to   { opacity: 1; transform: translateY(0); }
}

@keyframes cardPopIn {
    from { opacity: 0; transform: scale(0.95); }
    to   { opacity: 1; transform: scale(1); }
}

@keyframes pressurePulse {
    0%, 100% { opacity: 1; }
    50%      { opacity: 0.7; }
}

@keyframes endingReveal {
    0%   { opacity: 0; transform: scale(0.9) translateY(20px); }
    60%  { transform: scale(1.02) translateY(-4px); }
    100% { opacity: 1; transform: scale(1) translateY(0); }
}

main { animation: pageFadeIn 0.3s ease-out; }
```

- [ ] **Step 8: 删除 app.css 中旧的深色主题样式**

删除以下旧区块（从 `body { color: #1f2933; }` 到文件末尾的所有旧样式）：
- `.survivor-body` 到 `.metric` 相关
- `.campus-hero` 到 `.scene-*` 场景背景
- `.game-shell` / `.game-hud` / `.game-dock` 旧深色样式
- `.dorm-*` / `.phone-home` / `.stat-bar` / `.stage-progress` / `.route-tendency` 等旧组件样式
- `.vn-stage` / `.vn-scene` 旧视觉小说样式
- `.dungeon-*` 旧副本样式
- `.campus-map-*` / `.campus-node` 旧地图样式
- `.event-stage` / `.event-card` / `.event-option` 旧事件样式
- `.bug-*` / `.relation-*` 旧小游戏样式
- 所有旧响应式 `@media` 查询

**保留**：刚写入的设计系统部分。

- [ ] **Step 9: 启动应用验证页面不报错**

Run: `.\mvnw.cmd spring-boot:run`
访问 http://localhost:8080/login，确认页面加载无白屏（样式会变乱，这是预期的，后续 task 修复）。

- [ ] **Step 10: Commit**

```bash
git add src/main/resources/static/css/app.css
git commit -m "style: 重写 app.css 设计系统（手绘笔记本风 tokens + 工具类）"
```

---

## Task 2: 页面骨架 layout.html

**Files:**
- Modify: `src/main/resources/templates/fragments/layout.html`

- [ ] **Step 1: 重写 game-hud 片段**

将 `game-hud` 片段替换为：

```html
<header th:fragment="game-hud" class="paper-card tilt-left" style="border-radius: 16px 10px 14px 8px; margin-bottom: 16px; display: flex; align-items: center; gap: 12px; padding: 10px 16px;">
    <a class="handwrite" href="/dashboard" style="color: var(--ink); font-weight: 800; font-size: 1.1rem; text-decoration: none;">📒 HAUT Survivor</a>
    <div style="flex:1; display: flex; gap: 8px; justify-content: center;">
        <span class="paper-chip paper-chip--blue" th:if="${profile != null}" th:text="'第 ' + ${profile.currentWeek} + ' 周'">第 1 周</span>
        <span class="paper-chip paper-chip--yellow" th:if="${profile != null}">
            ⚡ <span th:text="${profile.actionPoints}">4</span>/<span th:text="${profile.maxActionPoints}">4</span>
        </span>
        <span class="paper-chip paper-chip--pink" th:if="${attribute != null and attribute.pressure >= 60}"
              style="animation: pressurePulse 2s ease-in-out infinite;">
            😰 <span th:text="${attribute.pressure}">30</span>
        </span>
    </div>
    <form method="post" action="/logout">
        <button type="submit" class="paper-btn" style="font-size: 0.78rem; padding: 4px 10px;">离开</button>
    </form>
</header>
```

- [ ] **Step 2: 重写 game-dock 片段**

将 `game-dock` 片段替换为：

```html
<nav th:fragment="game-dock" class="paper-card" style="display: flex; justify-content: space-around; padding: 8px; border-radius: 14px 10px 16px 8px; position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; max-width: 960px; margin: 0 auto;">
    <a href="/dashboard" class="paper-dock__item" style="text-align: center; padding: 4px 12px; text-decoration: none; color: var(--ink);">
        <div style="font-size: 1.3rem;">🏠</div>
        <div style="font-size: 0.65rem; font-weight: 700;">寝室</div>
    </a>
    <a href="/map" class="paper-dock__item" style="text-align: center; padding: 4px 12px; text-decoration: none; color: var(--ink-muted);">
        <div style="font-size: 1.3rem;">🗺️</div>
        <div style="font-size: 0.65rem; font-weight: 700;">去校园</div>
    </a>
    <a href="/dungeons" class="paper-dock__item" style="text-align: center; padding: 4px 12px; text-decoration: none; color: var(--ink-muted);">
        <div style="font-size: 1.3rem;">⚔️</div>
        <div style="font-size: 0.65rem; font-weight: 700;">挑战</div>
    </a>
    <a href="/organizations" class="paper-dock__item" style="text-align: center; padding: 4px 12px; text-decoration: none; color: var(--ink-muted);">
        <div style="font-size: 1.3rem;">🎉</div>
        <div style="font-size: 0.65rem; font-weight: 700;">社团</div>
    </a>
</nav>
```

- [ ] **Step 3: 在 app.css 追加 Dock 桌面端适配**

```css
/* ---------- Dock 响应式 ---------- */

@media (min-width: 993px) {
    .paper-dock__item { /* 桌面端 Dock 不 fixed */
        position: static !important;
    }
}

/* 移动端给 Dock 留底部空间 */
body:has(.paper-dock__item) {
    padding-bottom: 80px;
}
```

注意：由于 Dock 使用了 `position: fixed`，需要在 body 添加底部 padding。检查 layout.html 中 game-dock 是否在 body 末尾，确认 padding 生效。

- [ ] **Step 4: 重写 admin-nav 和 nav 片段**

将 `admin-nav` 和 `nav` 片段替换为手绘风格：

```html
<nav th:fragment="admin-nav" class="paper-card tilt-left" style="display: flex; align-items: center; gap: 20px; margin-bottom: 20px; padding: 10px 20px; border-radius: 16px 10px 14px 8px;">
    <a class="handwrite" href="/dashboard" style="color: var(--ink); font-weight: 800; text-decoration: none;">📒 HAUT Survivor</a>
    <div style="display: flex; gap: 14px; flex: 1;">
        <a href="/dashboard" style="color: var(--ink-muted); text-decoration: none; font-size: 0.9rem;">仪表盘</a>
        <a href="/admin/events" style="color: var(--ink-muted); text-decoration: none; font-size: 0.9rem;">事件管理</a>
    </div>
    <form method="post" action="/logout">
        <button type="submit" class="paper-btn" style="font-size: 0.78rem; padding: 4px 12px;">退出</button>
    </form>
</nav>
```

同样替换 `nav` 片段（结构类似，链接更多）。

- [ ] **Step 5: 验证 layout 不报错**

Run: `.\mvnw.cmd spring-boot:run`
访问 http://localhost:8080/login，检查控制台无报错。

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/templates/fragments/layout.html src/main/resources/static/css/app.css
git commit -m "style: 重写 layout 骨架（手绘风 HUD + Dock + 导航）"
```

---

## Task 3: 认证页面 login + register

**Files:**
- Modify: `src/main/resources/templates/auth/login.html`
- Modify: `src/main/resources/templates/auth/register.html`

- [ ] **Step 1: 重写 login.html**

完整替换 `<body>` 内容为：

```html
<body>
<main class="container py-5">
    <div class="row justify-content-center">
        <section class="col-12 col-md-6 col-lg-4">
            <div class="text-center mb-4">
                <h1 class="handwrite" style="font-size: 2rem;">📒 HAUT Survivor</h1>
                <p style="color: var(--ink-muted); font-size: 0.9rem;">校园生存挑战</p>
            </div>
            <form method="post" action="/login" class="paper-card tilt-right">
                <div th:if="${error}" class="paper-chip paper-chip--pink" style="display:block; margin-bottom: 12px; text-align: center;" th:text="${error}"></div>
                <div class="mb-3">
                    <label for="username" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">用户名</label>
                    <input id="username" name="username" class="paper-input" th:value="${username}" required>
                </div>
                <div class="mb-3">
                    <label for="password" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">密码</label>
                    <input id="password" name="password" type="password" class="paper-input" required>
                </div>
                <button type="submit" class="paper-btn paper-btn--primary w-100" style="text-align: center;">登录</button>
                <a href="/register" class="paper-btn w-100 mt-2" style="text-align: center;">注册新账号</a>
            </form>
        </section>
    </div>
</main>
</body>
```

注意：删除 `<body class="bg-light">`，改为 `<body>`（全局背景由 CSS 控制）。

- [ ] **Step 2: 重写 register.html**

与 login 类似，多一个昵称字段，按钮用 `.paper-btn--success`：

```html
<body>
<main class="container py-5">
    <div class="row justify-content-center">
        <section class="col-12 col-md-6 col-lg-4">
            <div class="text-center mb-4">
                <h1 class="handwrite" style="font-size: 2rem;">✏️ 注册账号</h1>
            </div>
            <form method="post" action="/register" class="paper-card tilt-left">
                <div th:if="${error}" class="paper-chip paper-chip--pink" style="display:block; margin-bottom: 12px; text-align: center;" th:text="${error}"></div>
                <div class="mb-3">
                    <label for="username" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">用户名</label>
                    <input id="username" name="username" class="paper-input" th:value="${username}" required>
                </div>
                <div class="mb-3">
                    <label for="nickname" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">昵称</label>
                    <input id="nickname" name="nickname" class="paper-input" th:value="${nickname}" required>
                </div>
                <div class="mb-3">
                    <label for="password" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">密码</label>
                    <input id="password" name="password" type="password" class="paper-input" required>
                </div>
                <button type="submit" class="paper-btn paper-btn--success w-100" style="text-align: center;">注册并进入</button>
                <a href="/login" class="paper-btn w-100 mt-2" style="text-align: center;">返回登录</a>
            </form>
        </section>
    </div>
</main>
</body>
```

- [ ] **Step 3: 验证登录/注册页面**

Run: `.\mvnw.cmd spring-boot:run`
访问 http://localhost:8080/login 和 http://localhost:8080/register，确认手绘风卡片正常显示，表单可提交。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/auth/login.html src/main/resources/templates/auth/register.html
git commit -m "style: 重写登录/注册页（手绘笔记本风）"
```

---

## Task 4: 角色创建页 player/create

**Files:**
- Modify: `src/main/resources/templates/player/create.html`

- [ ] **Step 1: 重写 create.html — 基础表单手绘化**

替换 `<body>` 为：

```html
<body>
<main class="container py-5">
    <div class="row justify-content-center">
        <section class="col-12 col-lg-8">
            <h1 class="handwrite text-center mb-4" style="font-size: 2rem;">🎓 创建你的校园角色</h1>
            <form method="post" action="/player/create" class="paper-card tilt-right">
                <div th:if="${error}" class="paper-chip paper-chip--pink" style="display:block; margin-bottom: 12px; text-align: center;" th:text="${error}"></div>

                <div class="row mb-3">
                    <div class="col-md-6 mb-2">
                        <label for="playerName" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">角色名</label>
                        <input id="playerName" name="playerName" class="paper-input" th:value="${playerName}" placeholder="你在游戏中的名字" required>
                    </div>
                    <div class="col-md-3 mb-2">
                        <label for="grade" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">年级</label>
                        <input id="grade" name="grade" class="paper-input" th:value="${grade}" placeholder="大一" required>
                    </div>
                    <div class="col-md-3 mb-2">
                        <label for="majorType" style="font-weight: 700; font-size: 0.88rem; margin-bottom: 4px; display: block;">专业类型</label>
                        <input id="majorType" name="majorType" class="paper-input" th:value="${majorType}" placeholder="计算机类" required>
                    </div>
                </div>
```

- [ ] **Step 2: 添加成长路线卡片单选**

接续表单，将原 `<select>` 替换为卡片单选（保留隐藏 select 确保表单提交）：

```html
                <div class="mb-4">
                    <label style="font-weight: 700; font-size: 0.88rem; margin-bottom: 8px; display: block;">📚 成长路线</label>
                    <!-- 隐藏的 select，保留 Thymeleaf 绑定 -->
                    <select id="growthRoute" name="growthRoute" style="display:none;" required>
                        <option th:each="route : ${growthRoutes}" th:value="${route}" th:text="${route}" th:selected="${route == growthRoute}"></option>
                    </select>
                    <!-- 卡片单选 -->
                    <div class="row g-2">
                        <div class="col-md-4 col-6">
                            <label class="route-card" data-route="考研路线" onclick="selectRoute(this)">
                                <div style="font-size: 1.5rem;">📚</div>
                                <div class="handwrite" style="font-size: 0.95rem; margin: 4px 0;">考研路线</div>
                                <div style="font-size: 0.72rem; color: var(--ink-muted);">学业 70 自律 55</div>
                            </label>
                        </div>
                        <div class="col-md-4 col-6">
                            <label class="route-card" data-route="就业路线" onclick="selectRoute(this)">
                                <div style="font-size: 1.5rem;">💼</div>
                                <div class="handwrite" style="font-size: 0.95rem; margin: 4px 0;">就业路线</div>
                                <div style="font-size: 0.72rem; color: var(--ink-muted);">社交 55 技能 50</div>
                            </label>
                        </div>
                        <div class="col-md-4 col-6">
                            <label class="route-card" data-route="竞赛路线" onclick="selectRoute(this)">
                                <div style="font-size: 1.5rem;">🏆</div>
                                <div class="handwrite" style="font-size: 0.95rem; margin: 4px 0;">竞赛路线</div>
                                <div style="font-size: 0.72rem; color: var(--ink-muted);">技能 52 学业 65</div>
                            </label>
                        </div>
                        <div class="col-md-6 col-6">
                            <label class="route-card" data-route="六边形路线" onclick="selectRoute(this)">
                                <div style="font-size: 1.5rem;">⬡</div>
                                <div class="handwrite" style="font-size: 0.95rem; margin: 4px 0;">六边形路线</div>
                                <div style="font-size: 0.72rem; color: var(--ink-muted);">全部 53+ 均衡发展</div>
                            </label>
                        </div>
                        <div class="col-md-6 col-6">
                            <label class="route-card" data-route="摆烂求生路线" onclick="selectRoute(this)">
                                <div style="font-size: 1.5rem;">🛋️</div>
                                <div class="handwrite" style="font-size: 0.95rem; margin: 4px 0;">摆烂求生</div>
                                <div style="font-size: 0.72rem; color: var(--ink-muted);">压力 20 健康 80</div>
                            </label>
                        </div>
                    </div>
                </div>

                <button type="submit" class="paper-btn paper-btn--primary w-100" style="text-align: center;">进入校园 →</button>
            </form>
        </section>
    </div>
</main>

<script>
function selectRoute(el) {
    document.querySelectorAll('.route-card').forEach(c => c.classList.remove('route-card--selected'));
    el.classList.add('route-card--selected');
    var route = el.getAttribute('data-route');
    var select = document.getElementById('growthRoute');
    select.value = route;
    // 触发 required 验证
    select.setCustomValidity('');
}
// 页面加载时如果已有选中值，高亮对应卡片
document.addEventListener('DOMContentLoaded', function() {
    var selected = document.getElementById('growthRoute').value;
    if (selected) {
        document.querySelectorAll('.route-card').forEach(function(c) {
            if (c.getAttribute('data-route') === selected) {
                c.classList.add('route-card--selected');
            }
        });
    }
});
</script>
</body>
```

- [ ] **Step 3: 在 app.css 追加 route-card 样式**

```css
/* ---------- 角色创建路线卡片 ---------- */

.route-card {
    display: block;
    background: #fff;
    border: 2px solid var(--border-color);
    border-radius: 12px 6px 10px 8px;
    box-shadow: 2px 2px 0 var(--shadow-color);
    padding: 14px 10px;
    text-align: center;
    cursor: pointer;
    transition: all 0.15s ease;
    opacity: 0.7;
}

.route-card:hover {
    opacity: 1;
    transform: translate(-1px, -1px);
    box-shadow: 3px 3px 0 var(--shadow-color);
}

.route-card--selected {
    opacity: 1;
    background: var(--yellow-light);
    border-width: 3px;
    transform: scale(1.03);
    box-shadow: 4px 4px 0 var(--shadow-color);
}
```

- [ ] **Step 4: 验证角色创建页**

Run: `.\mvnw.cmd spring-boot:run`
访问 http://localhost:8080/player/create，确认 5 张路线卡片可点击选中，表单可提交。

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/player/create.html src/main/resources/static/css/app.css
git commit -m "style: 重写角色创建页（手绘卡片单选成长路线）"
```

---

## Task 5: 仪表盘 dashboard/index

**Files:**
- Modify: `src/main/resources/templates/dashboard/index.html`

- [ ] **Step 1: 将 body class 从 game-shell 改为普通**

将 `<body class="game-shell">` 改为 `<body>`。

- [ ] **Step 2: 逐 section 替换 class**

对 dashboard/index.html 中的每个 `<section>` 进行 class 替换：

| 旧 class | 新 class + tilt |
|---|---|
| `.dorm-semester-end` | `.paper-card .tilt-left`（保留内联样式适配） |
| `.dorm-profile` | `.paper-card .tilt-right` |
| `.stage-progress` | `.paper-card .tilt-left` |
| `.route-tendency` | `.paper-card .tilt-right` |
| `.pressure-alert` | `.paper-card .tilt-left`（粉色边框） |
| `.week-theme-card` | `.paper-card .tilt-right` |
| `.weekly-goal-card` | `.paper-card .tilt-left` |
| `.phone-home` | 保留 grid，内部 `.phone-home__app` 加 paper-card 样式 |
| `.stat-bar` | `.paper-card .tilt-right` |
| `.sticky-note` | `.paper-card .tilt-left` |
| `.dorm-week-control` | `.paper-card` |
| 熟人列表区 | `.paper-card .tilt-right` |
| 成就架 | `.paper-card .tilt-left` |

**操作方式**：用 edit 工具逐个替换 class。保留所有 `th:` 属性和内部结构不变。

- [ ] **Step 3: 替换属性条内部样式**

将 `.stat-bar__item-fill--*` 类改为 `.stat-fill--*`，轨道改为 `.stat-track`。保留 Thymeleaf 的 `th:style` 宽度绑定。

- [ ] **Step 4: 替换标签/按钮 class**

将 `.dorm-profile__tag` → `.paper-chip paper-chip--yellow`
将 `.sticky-note__btn` → `.paper-btn`
将 `.sticky-note__btn--primary` → `.paper-btn paper-btn--primary`

- [ ] **Step 5: 在 app.css 追加仪表盘专用样式**

补充少量布局样式（保留语义 class 名作为布局辅助，但视觉用 paper-card）：

```css
/* ---------- 仪表盘布局辅助 ---------- */

.phone-home {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
    margin-bottom: 16px;
}

.phone-home__app {
    background: #fff;
    border: 2px solid var(--border-color);
    border-radius: 12px 6px 10px 8px;
    box-shadow: 2px 2px 0 var(--shadow-color);
    padding: 12px 6px;
    text-align: center;
    text-decoration: none;
    color: var(--ink);
    transition: all 0.15s;
}

.phone-home__app:hover {
    transform: translate(-1px, -1px);
    box-shadow: 3px 3px 0 var(--shadow-color);
}

@media (max-width: 640px) {
    .phone-home { grid-template-columns: repeat(4, 1fr); gap: 8px; }
}
```

- [ ] **Step 6: 验证仪表盘**

Run: `.\mvnw.cmd spring-boot:run`
登录 `student/student123`，访问 http://localhost:8080/dashboard，确认所有区块手绘风渲染正常。

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/templates/dashboard/index.html src/main/resources/static/css/app.css
git commit -m "style: 重写仪表盘（手绘笔记本风）"
```

---

## Task 6: 校园地图 map/index

**Files:**
- Modify: `src/main/resources/templates/map/index.html`

- [ ] **Step 1: 替换 body class 和容器**

`<body class="game-shell">` → `<body>`
`.campus-map` 容器保留 max-width 布局。

- [ ] **Step 2: 替换标题区和信息区 class**

`.campus-map__header` → 保留结构，标题用 `.handwrite`
`.campus-map__msg` → `.paper-card .tilt-left`（蓝色 chip 风格）

- [ ] **Step 3: 替换校区照片容器**

`.campus-map-photo` → 加 `.paper-card` 样式（黑描边 + 偏移阴影）
`.campus-map-hotspot` → 改为手绘图钉样式：

```css
.campus-map-hotspot {
    background: var(--yellow-light);
    border: 2px solid var(--border-color);
    border-radius: 50% 45% 50% 40%;
    box-shadow: 2px 2px 0 var(--shadow-color);
    /* 保留绝对定位逻辑 */
}
.campus-map-hotspot:hover {
    background: var(--green-light);
    transform: translate(-50%, -50%) scale(1.1);
}
```

- [ ] **Step 4: 替换地点卡片网格**

`.campus-node` → `.paper-card` 样式，保留 `--node-color` CSS 变量用于装饰光斑
`.campus-node__go` → `.paper-btn`
探索度条用 `.stat-track` + `.stat-fill`

- [ ] **Step 5: 替换副本入口和导航**

`.campus-map__dungeon-hook` → `.paper-card .tilt-right`（橙色调）
`.campus-map__back` 内的链接 → `.paper-btn`

- [ ] **Step 6: 在 app.css 追加地图专用样式**

保留必要的布局 class（grid、hotspot 定位），视觉统一到 paper-card。

- [ ] **Step 7: 验证地图页**

访问 http://localhost:8080/map，确认热点可点击，地点卡片正常。

- [ ] **Step 8: Commit**

```bash
git add src/main/resources/templates/map/index.html src/main/resources/static/css/app.css
git commit -m "style: 重写校园地图页（手绘图钉热点 + 卡片）"
```

---

## Task 7: 事件页 map/event

**Files:**
- Modify: `src/main/resources/templates/map/event.html`

- [ ] **Step 1: 替换事件舞台背景**

`.vn-stage` → 移除深色背景，改为纸张底 + 顶部彩色条带表示地点类型：

```css
.vn-stage {
    background: var(--paper-bg);
    background-image: radial-gradient(circle, rgba(45,52,54,0.04) 1px, transparent 1px);
    background-size: 20px 20px;
    padding: 24px 16px;
    min-height: auto;
}
```

场景类型用顶部条带颜色区分（替换旧 scene-* 背景）：
```css
.vn-stage.scene-lab      .vn-scene { border-top: 6px solid var(--purple-deep); }
.vn-stage.scene-library  .vn-scene { border-top: 6px solid var(--green-deep); }
.vn-stage.scene-classroom .vn-scene { border-top: 6px solid var(--blue-deep); }
.vn-stage.scene-dorm     .vn-scene { border-top: 6px solid var(--purple-deep); }
.vn-stage.scene-canteen  .vn-scene { border-top: 6px solid var(--orange-deep); }
.vn-stage.scene-track    .vn-scene { border-top: 6px solid var(--green-deep); }
.vn-stage.scene-club     .vn-scene { border-top: 6px solid var(--orange-deep); }
.vn-stage.scene-package  .vn-scene { border-top: 6px solid var(--blue-deep); }
```

- [ ] **Step 2: 替换事件场景卡**

`.vn-scene` → `.paper-card` 样式（白底黑描边）
`.vn-scene__meta` 内的标签 → `.paper-chip`
标题用 `.handwrite`

- [ ] **Step 3: 替换选项按钮**

`.event-option` → `.paper-btn` 样式，保留左侧风险色条：
```css
.event-option {
    background: #fff;
    border: 2.5px solid var(--border-color);
    border-left: 6px solid var(--ink-muted);
    border-radius: 10px 6px 8px 12px;
    box-shadow: 3px 3px 0 var(--shadow-color);
    text-align: left;
    width: 100%;
    cursor: pointer;
    transition: all 0.1s;
}
.event-option.risk-low    { border-left-color: var(--green-deep); }
.event-option.risk-medium { border-left-color: var(--yellow-deep); }
.event-option.risk-high   { border-left-color: var(--pink-deep); }
```

- [ ] **Step 4: 替换结果展示**

属性变化标签 → `.paper-chip--green`（上升）/ `.paper-chip--pink`（下降）
玩家状态条 → `.paper-chip` 横排

- [ ] **Step 5: 验证事件页**

访问 http://localhost:8080/map，点击地点触发事件，确认选项可点击、结果正常展示。

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/templates/map/event.html src/main/resources/static/css/app.css
git commit -m "style: 重写事件页（纸张底 + 手绘场景卡 + 风险色条选项）"
```

---

## Task 8: 探索页 exploration/index + result

**Files:**
- Modify: `src/main/resources/templates/exploration/index.html`
- Modify: `src/main/resources/templates/exploration/result.html`

- [ ] **Step 1: 重写 exploration/index**

`<body class="game-shell">` → `<body>`
标题用 `.handwrite`
8 张地点卡片 → `.paper-card` + `.tilt-*` 交替
探索度条 → `.stat-track` + `.stat-fill`
「踩点探索」按钮 → `.paper-btn .paper-btn--primary`

- [ ] **Step 2: 重写 exploration/result**

结果卡 → `.paper-card .tilt-right`
结果类型 → 大 emoji + `.paper-chip`（按类型着色）
属性变化 → `.paper-chip` 组
影响来源 → 小 `.paper-card` 列表

- [ ] **Step 3: 验证探索页**

访问 http://localhost:8080/exploration，点击探索，确认结果页正常。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/exploration/index.html src/main/resources/templates/exploration/result.html src/main/resources/static/css/app.css
git commit -m "style: 重写探索页和结果页（手绘笔记本风）"
```

---

## Task 9: NPC 详情页 + 结果页

**Files:**
- Modify: `src/main/resources/templates/npc/detail.html`
- Modify: `src/main/resources/templates/npc/result.html`

- [ ] **Step 1: 重写 npc/detail**

头像 → `.paper-avatar`（保留 emoji 内容）
NPC 名字 → `.handwrite`
类型/性格标签 → `.paper-chip`
关系阶段进度条 → `.stat-track` + `.stat-fill`
互动选项 → 每项一张小 `.paper-card`
「设为本周搭子」按钮 → `.paper-btn .paper-btn--primary`
「互动」按钮 → `.paper-btn`

- [ ] **Step 2: 重写 npc/result**

结果卡 → `.paper-card .tilt-left`
互动结果文案 + 属性变化 `.paper-chip` + 熟悉度变化

- [ ] **Step 3: 验证 NPC 页**

从仪表盘熟人列表点击 NPC，确认详情页和互动结果正常。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/npc/detail.html src/main/resources/templates/npc/result.html
git commit -m "style: 重写 NPC 详情页和结果页（手绘笔记本风）"
```

---

## Task 10: 组织页 index + detail

**Files:**
- Modify: `src/main/resources/templates/organization/index.html`
- Modify: `src/main/resources/templates/organization/detail.html`

- [ ] **Step 1: 重写 organization/index**

标题 → `.handwrite`
8 张组织摊位卡片 → `.paper-card` + `.tilt-*` 交替（像贴在公告栏）
类型标签 → `.paper-chip`（按类型着色）
状态标记 → `.paper-chip`
「去摊位看看」链接 → `.paper-btn`

- [ ] **Step 2: 重写 organization/detail**

三态展示用 `.paper-card`：
- 未发现：锁图标 + 探索度说明 + 「发现」按钮（禁用态 `.paper-btn--disabled`）
- 已发现：「申请加入」`.paper-btn .paper-btn--primary`
- 已加入：职位/贡献/声望 `.paper-chip` 横排 + 「参加本周活动」`.paper-btn .paper-btn--success`
活动结果 → `.paper-chip` 属性变化

- [ ] **Step 3: 验证组织页**

访问 http://localhost:8080/organizations，确认列表和详情页正常。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/organization/index.html src/main/resources/templates/organization/detail.html
git commit -m "style: 重写组织广场和详情页（手绘摊位卡片）"
```

---

## Task 11: 副本页 index + detail

**Files:**
- Modify: `src/main/resources/templates/dungeon/index.html`
- Modify: Modify: `src/main/resources/templates/dungeon/detail.html`

- [ ] **Step 1: 重写 dungeon/index**

标题 → `.handwrite`
5 张副本海报卡片 → `.paper-card` + `.tilt-strong-*`（倾斜幅度加大，像贴在墙上）
每张：emoji + 名称（`.handwrite`）+ 类型 `.paper-chip` + 难度 `.paper-chip` + 时间 + 奖励 + 「查看挑战」`.paper-btn`

- [ ] **Step 2: 重写 dungeon/detail**

大标题 → `.handwrite` 超大字
副本信息卡 → `.paper-card`
信息项 → `.paper-chip` 排列
阶段路线 → 手绘时间线（编号黑描边不规则圆 + 虚线连接）
「开始挑战」→ `.paper-btn .paper-btn--primary`

- [ ] **Step 3: 在 app.css 追加时间线样式**

```css
/* ---------- 副本阶段时间线 ---------- */

.dungeon-timeline-step {
    display: flex;
    align-items: flex-start;
    gap: 14px;
    margin-bottom: 14px;
}

.dungeon-timeline-step__num {
    background: var(--yellow-light);
    border: 2px solid var(--border-color);
    border-radius: 50% 45% 50% 40%;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    flex-shrink: 0;
}

.dungeon-timeline-connector {
    border-left: 2px dashed var(--border-color);
    margin-left: 17px;
    height: 20px;
}
```

- [ ] **Step 4: 验证副本列表和封面**

访问 http://localhost:8080/dungeons，点击查看副本详情。

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/dungeon/index.html src/main/resources/templates/dungeon/detail.html src/main/resources/static/css/app.css
git commit -m "style: 重写副本列表和封面页（手绘海报墙 + 时间线）"
```

---

## Task 12: 副本进行页 + 结果页

**Files:**
- Modify: `src/main/resources/templates/dungeon/play.html`
- Modify: `src/main/resources/templates/dungeon/result.html`

- [ ] **Step 1: 重写 dungeon/play**

场景叙事 → 大 `.paper-card`（信纸风格，顶部彩色条带）
任务标题 → `.handwrite`
目标文本 → 引用块（左侧粗黑竖线）
倒计时 → `.paper-chip .paper-chip--pink`（手写体数字）

数据库拼图区域：
- `.minigame-board` → `.paper-card` 黑描边框
- 表名 → `.paper-chip` 等宽字体
- `.relation-choice` → `.paper-card` 小卡 + 复选框

Bug 定位区域：
- `.bug-question` → 小 `.paper-card`
- 症状 → `.paper-chip .paper-chip--pink`
- `.bug-option` → `.paper-card` 小卡 + 单选按钮

玩家状态条 → `.paper-chip` 横排

- [ ] **Step 2: 重写 dungeon/result**

评价标题 → `.handwrite` 大字
结果叙事 → 正文
属性变化 → `.paper-chip` 组
分数汇总 → 横线本表格（虚线分隔行）
最终评价 → `.paper-card .tilt-right`（突出显示）
风险标记 → `.paper-chip .paper-chip--pink`
「进入下一阶段」/「返回副本列表」→ `.paper-btn`

- [ ] **Step 3: 在 app.css 追加横线本表格样式**

```css
/* ---------- 横线本表格 ---------- */

.paper-table {
    width: 100%;
    border-collapse: collapse;
}

.paper-table th {
    border-bottom: 2px solid var(--border-color);
    padding: 8px 12px;
    text-align: left;
    font-weight: 800;
    font-size: 0.85rem;
}

.paper-table td {
    border-bottom: 2px dashed var(--border-color);
    padding: 8px 12px;
    font-size: 0.88rem;
}
```

- [ ] **Step 4: 验证副本进行和结果**

开始一个副本，完成阶段，确认进行页和结果页正常。

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/dungeon/play.html src/main/resources/templates/dungeon/result.html src/main/resources/static/css/app.css
git commit -m "style: 重写副本进行页和结果页（手绘信纸 + 横线本表格）"
```

---

## Task 13: 周总结 + 历史周报

**Files:**
- Modify: `src/main/resources/templates/week/summary.html`
- Modify: `src/main/resources/templates/week/history.html`

- [ ] **Step 1: 重写 week/summary**

`<body class="game-shell">` → `<body>`
英雄区 → `.paper-card .tilt-right`（周次 `.handwrite` 大字 + 主题 + 评级 `.paper-chip`）
周目标卡 → `.paper-card`
属性快照 → `.paper-table`（横线本风格，变化用绿↑/红↓）
影响回放 → 每条一张小 `.paper-card`（来源 `.paper-chip` + 描述 + 变化）
NPC 熟人区 → `.paper-card` 横向头像列表
成就区 → `.paper-card` 成就标签
操作按钮 → `.paper-btn .paper-btn--primary`（进入下一周）/ `.paper-btn`

- [ ] **Step 2: 重写 week/history**

学期档案卡 → `.paper-card .tilt-left`（顶部）
关键词标签 → `.paper-chip`
每周回放 → 每周一张 `.paper-card`，`.tilt-left` / `.tilt-right` 交替

- [ ] **Step 3: 验证周总结和历史周报**

推进周次进入周总结页，确认正常。访问 /week/history 确认正常。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/week/summary.html src/main/resources/templates/week/history.html
git commit -m "style: 重写周总结和历史周报页（手绘日记 + 横线本）"
```

---

## Task 14: 学期结局页

**Files:**
- Modify: `src/main/resources/templates/ending/index.html`

- [ ] **Step 1: 重写 ending/index**

`<body class="game-shell">` → `<body>`

三态展示：
- **学期未结束**：`.paper-card` 提示信息 + 返回链接 `.paper-btn`
- **已结束未结算**：`.paper-card` 属性快照 + 「揭晓结局」`.paper-btn .paper-btn--primary`
- **已结算**：
  - 结局卡 → `.paper-card .tilt-right`，双层描边模拟奖状框：
    ```css
    .ending-certificate {
        border: 2.5px solid var(--border-color);
        outline: 2px solid var(--border-color);
        outline-offset: 4px;
        border-radius: 16px 8px 14px 10px;
        box-shadow: 6px 6px 0 var(--shadow-color);
    }
    ```
  - 结局图标大字 + 类型 `.paper-chip` + 结局名 `.handwrite` 超大字
  - 路线评分卡 → `.paper-card`：5 维 `.stat-track` + 评级 `.paper-chip` + 关键证据列表（✓ 手绘对勾）
  - 成长画像 → `.paper-card`：关键词 `.paper-chip` + 描述
  - 「再来一学期」→ `.paper-btn .paper-btn--primary`
  - 历史结局画廊 → 小 `.paper-card` 横排

- [ ] **Step 2: 在 app.css 追加结局动画**

```css
.ending-certificate {
    animation: endingReveal 0.6s ease-out;
}
```

- [ ] **Step 3: 验证结局页**

推进到第 16 周后结算，确认结局页正常展示。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/ending/index.html src/main/resources/static/css/app.css
git commit -m "style: 重写学期结局页（手绘奖状风格）"
```

---

## Task 15: 任务页 + 管理端页面

**Files:**
- Modify: `src/main/resources/templates/task/index.html`
- Modify: `src/main/resources/templates/admin/event-list.html`
- Modify: `src/main/resources/templates/admin/event-form.html`

- [ ] **Step 1: 重写 task/index**

`<body class="bg-light">` → `<body>`
导航 → 使用 `fragments/layout :: nav`
创建任务表单 → `.paper-card .tilt-right`
输入框 → `.paper-input`
难度选择 → `.paper-chip` 单选组
任务列表 → `.paper-table`（横线本风格）
「完成」按钮 → `.paper-btn .paper-btn--success`

- [ ] **Step 2: 重写 admin/event-list**

`<body class="bg-light">` → `<body>`
导航 → 使用 `fragments/layout :: admin-nav`
「新增事件」→ `.paper-btn .paper-btn--primary`
事件表格 → `.paper-table`
编辑/禁用操作 → 小 `.paper-btn`

- [ ] **Step 3: 重写 admin/event-form**

导航 → `admin-nav`
表单 → `.paper-card .tilt-left`
所有输入 → `.paper-input`
地点选择 → `.paper-input`（select 样式适配）
提交 → `.paper-btn .paper-btn--primary`

- [ ] **Step 4: 验证任务和管理端页**

访问 http://localhost:8080/tasks 和 http://localhost:8080/admin/events（用 admin 登录），确认正常。

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/task/index.html src/main/resources/templates/admin/event-list.html src/main/resources/templates/admin/event-form.html
git commit -m "style: 重写任务页和管理端页面（手绘笔记本风）"
```

---

## Task 16: 全量验证与清理

**Files:**
- Modify: `src/main/resources/static/css/app.css`（清理无用旧样式）

- [ ] **Step 1: 全量功能验证**

Run: `.\mvnw.cmd spring-boot:run`

按以下流程完整走一遍：
1. 注册新账号 → 创建角色 → 仪表盘
2. 探索校园（消耗 AP）→ 查看探索结果
3. 校园地图 → 触发事件 → 选择选项 → 查看结果
4. NPC 详情 → 互动 → 查看结果 → 设搭子
5. 组织广场 → 查看详情 → 加入/参加活动
6. 副本列表 → 查看详情 → 开始挑战 → 完成阶段 → 查看结果
7. 周总结 → 推进周次
8. 历史周报
9. 任务页创建/完成任务
10. admin 登录 → 事件管理 CRUD

检查每个页面：
- 无白屏、无控制台报错
- 手绘风视觉统一
- 移动端和桌面端响应式正常
- 所有按钮和表单可正常操作

- [ ] **Step 2: 清理 app.css 中的无用旧样式**

搜索 app.css 中所有未被任何模板引用的旧 class（如 `.survivor-body`、`.campus-hero`、`.event-stage` 深色背景等），确认无引用后删除。

Run: 用 grep 工具搜索模板中是否还引用了旧 class 名，无引用的从 CSS 中删除。

- [ ] **Step 3: 删除备份文件**

Run: `Remove-Item "src\main\resources\static\css\app.css.bak"`

- [ ] **Step 4: 运行测试确认无回归**

Run: `.\mvnw.cmd clean test`

确认 408 个测试通过，0 失败。

- [ ] **Step 5: Final Commit**

```bash
git add -A
git commit -m "style: 前端手绘笔记本风重做完成（全 23 页面）"
```

---

## Self-Review

### Spec coverage 检查

| Spec 章节 | 对应 Task |
|---|---|
| 二、设计系统 | Task 1 |
| 3.1 认证与角色创建 | Task 3 + Task 4 |
| 3.2 核心游戏页面（layout） | Task 2 |
| 3.2 核心游戏页面（dashboard） | Task 5 |
| 3.2 核心游戏页面（map/index） | Task 6 |
| 3.2 核心游戏页面（map/event） | Task 7 |
| 3.2 核心游戏页面（exploration） | Task 8 |
| 3.3 社交与组织（NPC） | Task 9 |
| 3.3 社交与组织（organization） | Task 10 |
| 3.4 副本（index+detail） | Task 11 |
| 3.4 副本（play+result） | Task 12 |
| 3.5 周总结与结局（summary+history） | Task 13 |
| 3.5 周总结与结局（ending） | Task 14 |
| 3.6 管理端与任务 | Task 15 |
| 四、改造顺序 | Task 1→2→3→4→5→...→16 遵循 spec 顺序 |
| 六、验证标准 | Task 16 |

### Placeholder scan

无 TBD/TODO，每个 step 都有具体代码或操作命令。

### Type consistency

`.paper-card` / `.paper-chip` / `.paper-btn` / `.paper-input` / `.paper-avatar` / `.stat-track` / `.stat-fill` 在所有 task 中使用一致。颜色变量 `--yellow-light` 等在 Task 1 定义，后续 task 引用一致。
