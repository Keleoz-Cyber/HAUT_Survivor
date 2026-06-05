# Next AI Handoff

本文档给下一个接手 HAUT Survivor 的 AI 使用。请先读完，再继续改项目。

## 你接手的项目是什么

这是一个 Java 课程设计项目：

> HAUT Survivor / 河南工业大学生存挑战

项目类型：

- Spring Boot Web 应用；
- MyBatis-Plus + MySQL；
- Thymeleaf 页面；
- 校园生活模拟；
- 游戏化成长管理；
- 可演示课程设计 Demo。

核心目标：

> 让用户以“工大学生角色”的身份，在莲花街校区地图中触发事件、改变属性、完成任务和副本，并获得结局评价。

## 你应先读哪些文件

按顺序阅读：

1. `docs/PROJECT_COMPLETION_STATUS.md`
2. `docs/requirements/README.md`
3. `docs/requirements/01-product-scope.md`
4. `docs/requirements/02-core-gameplay.md`
5. `docs/requirements/03-dungeon-system.md`
6. `docs/requirements/05-data-and-technical-design.md`
7. `docs/requirements/07-roadmap-and-acceptance.md`
8. `README.md`

如果需要更完整背景，再读归档原文：

```text
docs/archive/HAUT_Survivor_Requirements.full.md
```

但根目录大文档很长，不建议作为唯一上下文入口。

## 当前代码状态摘要

当前已经实现：

- 注册登录；
- 角色创建；
- 属性仪表盘；
- 莲花街校区地图；
- 随机事件选择；
- 属性结算；
- 基础任务管理；
- 管理员事件管理；
- Java 课设副本；
- 数据库拼图小游戏；
- 副本过程标签；
- 副本最终评价；
- README 演示路线；
- 多份拆分需求文档。

当前仍未充分实现：

- 地图事件池随机化；
- Bug 定位小游戏；
- 视觉背景图；
- 结局图鉴；
- 成就称号；
- 成长报告；
- 多副本；
- 完整后台 CRUD。

## 当前技术信息

工作目录：

```text
d:\study\code\java\classlearn\HAUT_Survivor
```

启动命令：

```powershell
.\mvnw.cmd spring-boot:run
```

测试命令：

```powershell
.\mvnw.cmd test
```

MySQL：

```text
host: localhost
port: 3306
database: haut_survivor
username: root
password: 123456
```

Demo 账号：

```text
student / student123
admin / admin123
```

访问地址：

```text
http://localhost:8080/login
```

## 重要工作区注意事项

1. 当前工作区有大量未提交文件，接手前先运行：

```powershell
git status -sb
```

2. 不要随意执行破坏性命令，例如：

```powershell
git reset --hard
git checkout -- .
```

3. `.claude/` 是未跟踪目录，不要碰，除非用户明确要求。

4. 如果 8080 端口被占用，先查看进程：

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

5. 如果要重启服务，先确认是否是旧 Spring Boot 进程，再停止。

## 推荐下一步开发

建议下一步不要做后台 CRUD，而是做：

> 地图随机事件 + Bug 定位小游戏 + 基础视觉增强

原因：

- 当前最大短板是整体可玩性仍集中在 Java 课设副本；
- 地图事件还不够随机，系统进入感不足；
- Bug 暴走阶段仍是普通选择，适合升级为小游戏；
- 视觉素材不足，页面仍可能显单调；
- 这些改动能直接提升 Demo 观感。

## 建议下一步任务范围

### 任务 1：地图事件池随机化

目标：

- 同一地点支持多个事件；
- 系统按地点、状态、概率选择事件；
- 实验室、图书馆、食堂、宿舍优先扩充。

建议新增或调整：

- `EventService` 随机选择逻辑；
- 事件 seed data；
- 事件测试；
- 地图页面提示。

### 任务 2：Bug 定位小游戏

目标：

- 把 Java 课设副本第三阶段“Bug 暴走”从普通选择改成小游戏；
- 展示简化 Bug 现象；
- 用户选择可能原因；
- 后端根据选择、技能、压力和过程标签结算。

推荐题库：

| 现象 | 正确原因 |
|---|---|
| 提交后 404 | Controller 路径或表单 action 不一致 |
| 字段显示为空 | 实体字段和数据库列映射不一致 |
| Mapper 无法注入 | Mapper 扫描或注解缺失 |
| 启动提示端口占用 | 旧进程未关闭 |
| 属性没变化 | Service 没有应用选项结算 |

### 任务 3：视觉增强

目标：

- 地图、事件、副本页面增加更明显的氛围；
- 先用 CSS/渐变/图标/轻量背景，不必找真实图片；
- 实验室和图书馆优先。

## 开发原则

1. 不要先大规模重构。
2. 不要删除原始需求文档。
3. 不要把小游戏结果完全交给前端，后端必须结算。
4. 每个业务改动先写测试。
5. 完成后跑：

```powershell
.\mvnw.cmd test
```

6. 重要页面改动后做 HTTP 冒烟：

```text
登录 -> 创建/进入角色 -> 地图 -> 事件 -> 副本 -> 小游戏 -> 结算
```

## 可直接复制给另一个 AI 的提示词

```text
你接手的是 HAUT Survivor，一个 Spring Boot + MyBatis-Plus + MySQL + Thymeleaf 的 Java 课程设计项目，路径是 d:\study\code\java\classlearn\HAUT_Survivor。

请先阅读这些文件：
1. docs/PROJECT_COMPLETION_STATUS.md
2. docs/NEXT_AI_HANDOFF.md
3. docs/requirements/README.md
4. docs/requirements/02-core-gameplay.md
5. docs/requirements/03-dungeon-system.md
6. docs/requirements/05-data-and-technical-design.md
7. README.md

当前项目已经实现登录、角色创建、属性仪表盘、校园地图、随机事件、基础任务、管理员事件管理、Java 课设副本、数据库拼图小游戏、过程标签和最终评价。最近一次全量测试是 .\mvnw.cmd test，结果 55 tests, 0 failures。

请不要重置工作区，不要删除 .claude/，不要覆盖已有未提交改动。先运行 git status -sb 了解当前状态。

下一步请优先增强可玩性：实现地图事件池随机化，并把 Java 课设副本第三阶段“Bug 暴走”升级为 Bug 定位小游戏。请遵循现有 Spring Boot / MyBatis-Plus / Thymeleaf 风格，先写测试，再改代码，最后运行 .\mvnw.cmd test 和本地 HTTP 冒烟流程。
```

## 如果只想让另一个 AI 继续规划

可以这样说：

```text
先不要写代码。请根据 docs/NEXT_AI_HANDOFF.md 和 docs/requirements/ 里的拆分需求，给我制定下一阶段“地图随机事件 + Bug 定位小游戏 + 视觉增强”的详细实现计划。计划要包含涉及文件、数据库改动、测试点、验收流程和风险。
```

## 如果想让另一个 AI 直接开干

可以这样说：

```text
按 docs/NEXT_AI_HANDOFF.md 的建议开始实现下一阶段。优先做地图事件池随机化和 Java 课设副本 Bug 定位小游戏。要求先写失败测试，再改代码；完成后跑 .\mvnw.cmd test，并用 student/student123 走一遍本地 HTTP 演示流程。
```
