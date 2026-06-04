# Demo Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the HAUT Survivor Demo foundation: login, player creation, attribute dashboard, Lotus Street campus map, random event choices, basic task management, and admin event management.

**Architecture:** Use a Spring Boot monolith with server-rendered Thymeleaf pages. Keep business logic in services, persistence in MyBatis-Plus mappers, and page routes in MVC controllers. Store seed content in SQL so the demo can run consistently after database initialization.

**Tech Stack:** Java 17, Spring Boot 3.x, Maven, MyBatis-Plus, MySQL 8, Thymeleaf, Bootstrap 5, JUnit 5.

---

## Current Context

- Repository: `Keleoz-Cyber/HAUT_Survivor`
- Main requirements file: `HAUT_Survivor_Requirements.md`
- Current implementation directory: `src/` exists but is empty except for `.gitkeep`.
- Recommended stack from requirements: Spring Boot + MyBatis-Plus + MySQL + Thymeleaf + Bootstrap.
- Demo scope: implement the `14.1 Demo 基础版` flow from the requirements.

## Target Package And File Structure

Create a Maven Spring Boot project under the existing repository root.

```text
pom.xml
src/main/java/cn/haut/survivor/HautSurvivorApplication.java
src/main/java/cn/haut/survivor/config/WebConfig.java
src/main/java/cn/haut/survivor/config/LoginInterceptor.java
src/main/java/cn/haut/survivor/controller/AuthController.java
src/main/java/cn/haut/survivor/controller/DashboardController.java
src/main/java/cn/haut/survivor/controller/MapController.java
src/main/java/cn/haut/survivor/controller/TaskController.java
src/main/java/cn/haut/survivor/controller/admin/AdminEventController.java
src/main/java/cn/haut/survivor/domain/entity/User.java
src/main/java/cn/haut/survivor/domain/entity/PlayerProfile.java
src/main/java/cn/haut/survivor/domain/entity/PlayerAttribute.java
src/main/java/cn/haut/survivor/domain/entity/CampusLocation.java
src/main/java/cn/haut/survivor/domain/entity/Event.java
src/main/java/cn/haut/survivor/domain/entity/EventOption.java
src/main/java/cn/haut/survivor/domain/entity/EventRecord.java
src/main/java/cn/haut/survivor/domain/entity/Task.java
src/main/java/cn/haut/survivor/domain/enums/UserRole.java
src/main/java/cn/haut/survivor/domain/enums/TaskStatus.java
src/main/java/cn/haut/survivor/mapper/*.java
src/main/java/cn/haut/survivor/service/*.java
src/main/java/cn/haut/survivor/service/impl/*.java
src/main/resources/application.yml
src/main/resources/schema.sql
src/main/resources/data.sql
src/main/resources/templates/auth/login.html
src/main/resources/templates/auth/register.html
src/main/resources/templates/player/create.html
src/main/resources/templates/dashboard/index.html
src/main/resources/templates/map/index.html
src/main/resources/templates/map/event.html
src/main/resources/templates/task/index.html
src/main/resources/templates/admin/event-list.html
src/main/resources/templates/admin/event-form.html
src/main/resources/static/css/app.css
src/test/java/cn/haut/survivor/service/*.java
```

## Demo Acceptance Flow

The final demo must support this path:

```text
register/login -> create player -> view dashboard -> open Lotus Street map
-> choose location -> trigger event -> choose event option -> attributes change
-> create task -> complete task -> admin maintains event data
```

---

### Task 1: Scaffold Spring Boot Project

**Files:**
- Create: `pom.xml`
- Create: `mvnw`
- Create: `mvnw.cmd`
- Create: `.mvn/wrapper/maven-wrapper.properties`
- Create: `src/main/java/cn/haut/survivor/HautSurvivorApplication.java`
- Create: `src/main/resources/application.yml`

- [x] **Step 1: Create Maven project descriptor**

Create `pom.xml` with Spring Boot web, Thymeleaf, validation, MyBatis-Plus, MySQL, Lombok, and test dependencies.

- [x] **Step 2: Create application entry point**

Create `HautSurvivorApplication.java` in package `cn.haut.survivor` with `@SpringBootApplication` and a standard `main` method.

- [x] **Step 3: Create local configuration**

Create `application.yml` with:

```yaml
spring:
  application:
    name: HAUT Survivor
  datasource:
    url: jdbc:mysql://localhost:3306/haut_survivor?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  sql:
    init:
      mode: always
  thymeleaf:
    cache: false
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
server:
  port: 8080
```

- [x] **Step 4: Add Maven Wrapper**

Add Maven Wrapper files so the project can build with `.\mvnw.cmd test` even when global Maven is not installed.

- [x] **Step 5: Verify scaffold**

Run: `.\mvnw.cmd test`

Expected: Maven resolves dependencies and the test phase exits with code 0.

- [x] **Step 6: Commit**

```bash
git add pom.xml mvnw mvnw.cmd .mvn/wrapper/maven-wrapper.properties src/main/java/cn/haut/survivor/HautSurvivorApplication.java src/main/resources/application.yml src/test/java/cn/haut/survivor/HautSurvivorApplicationTests.java docs/superpowers/plans/2026-06-04-demo-foundation.md
git commit -m "Add Spring Boot project scaffold"
```

---

### Task 2: Create Demo Database Schema And Seed Data

**Files:**
- Create: `src/main/resources/schema.sql`
- Create: `src/main/resources/data.sql`

- [x] **Step 1: Define demo tables**

Create SQL tables for:

- `user`
- `player_profile`
- `player_attribute`
- `campus_location`
- `event`
- `event_option`
- `event_record`
- `task`

Use the field names already defined in `HAUT_Survivor_Requirements.md`.

- [x] **Step 2: Add seed users**

Seed:

```text
admin / admin123 / role admin
student / student123 / role user
```

Use a simple SHA-256 password hash or clearly named demo-only hash helper. Do not store plain text passwords in Java code.

- [x] **Step 3: Add seed campus locations**

Seed Lotus Street campus locations:

```text
教学楼, 图书馆, 宿舍, 食堂, 操场, 实验室, 社团活动区, 快递站
```

- [x] **Step 4: Add seed events and options**

Add at least 12 event records and 3 options per event. Include:

```text
早八点名危机
图书馆抢座
Java 代码报错
宿舍 DDL 突袭
食堂夜宵诱惑
体测通知
快递到了但下雨
社团招新
生活费余额不足
实验数据异常
课堂突然提问
粮食守护者挑战
```

- [x] **Step 5: Verify SQL loads**

Run the app once after Task 1 is complete:

```bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-application-type=none"
```

Expected: application starts without SQL initialization errors and exits because web mode is disabled.

- [x] **Step 6: Commit**

```bash
git add src/main/resources/schema.sql src/main/resources/data.sql
git commit -m "Add demo database schema and seed data"
```

---

### Task 3: Implement Domain Entities And Mappers

**Files:**
- Create: `src/main/java/cn/haut/survivor/domain/entity/*.java`
- Create: `src/main/java/cn/haut/survivor/domain/enums/*.java`
- Create: `src/main/java/cn/haut/survivor/mapper/*.java`

- [x] **Step 1: Create entity classes**

Create one entity class per demo table. Use Lombok `@Data`, MyBatis-Plus `@TableName`, and `@TableId(type = IdType.AUTO)`.

- [x] **Step 2: Create enum classes**

Create:

```java
public enum UserRole {
    USER, ADMIN
}
```

```java
public enum TaskStatus {
    PENDING, DONE, OVERDUE, ABANDONED
}
```

- [x] **Step 3: Create mapper interfaces**

Each mapper extends `BaseMapper<EntityName>`.

- [x] **Step 4: Add mapper scanning**

Add `@MapperScan("cn.haut.survivor.mapper")` to `HautSurvivorApplication`.

- [x] **Step 5: Verify mapper context**

Run: `mvn test`

Expected: Spring context loads without mapper bean errors.

- [x] **Step 6: Commit**

```bash
git add src/main/java/cn/haut/survivor
git commit -m "Add domain entities and mappers"
```

---

### Task 4: Implement Registration And Login

**Files:**
- Create: `AuthController.java`
- Create: `UserService.java`
- Create: `UserServiceImpl.java`
- Create: `LoginInterceptor.java`
- Create: `WebConfig.java`
- Create: `templates/auth/login.html`
- Create: `templates/auth/register.html`

- [x] **Step 1: Write user service tests**

Create tests for:

- duplicate username registration fails
- password shorter than 6 characters fails
- login succeeds with valid credentials
- login fails with invalid password

- [x] **Step 2: Implement `UserService`**

Methods:

```java
User register(String username, String password, String nickname);
User login(String username, String password);
User findById(Long id);
boolean isAdmin(Long userId);
```

- [x] **Step 3: Implement login session**

Store `LOGIN_USER_ID` and `LOGIN_USER_ROLE` in `HttpSession`.

- [x] **Step 4: Implement auth pages**

Routes:

```text
GET /login
POST /login
GET /register
POST /register
POST /logout
```

- [x] **Step 5: Add login interceptor**

Allow:

```text
/login, /register, /css/**, /js/**, /images/**
```

Protect all demo pages behind login.

- [x] **Step 6: Verify manually**

Run: `mvn spring-boot:run`

Expected:

- `http://localhost:8080/login` opens
- `student / student123` logs in
- invalid credentials show an error message

- [x] **Step 7: Commit**

```bash
git add src/main/java/cn/haut/survivor src/main/resources/templates/auth
git commit -m "Add registration and login"
```

---

### Task 5: Implement Player Creation And Attribute Dashboard

**Files:**
- Create: `DashboardController.java`
- Create: `PlayerService.java`
- Create: `PlayerServiceImpl.java`
- Create: `templates/player/create.html`
- Create: `templates/dashboard/index.html`
- Modify: auth login success redirect

- [x] **Step 1: Write player service tests**

Cover:

- creating a profile initializes default attributes
- growth route adjusts attributes
- user with profile is routed to dashboard
- user without profile is routed to player creation

- [x] **Step 2: Implement profile creation**

Default attributes:

```text
academic=60, health=70, money=80, social=50, skill=40, pressure=30, discipline=50
```

Supported growth routes:

```text
考研路线, 就业路线, 竞赛路线, 六边形路线, 摆烂求生路线
```

- [x] **Step 3: Implement dashboard**

Show:

- nickname
- current title
- current week
- level and exp
- all seven attributes
- risk warnings for low health, low money, low academic, and high pressure

- [x] **Step 4: Verify manually**

Expected:

- first login redirects to `/player/create`
- after creation redirects to `/dashboard`
- dashboard shows initialized attributes

- [x] **Step 5: Commit**

```bash
git add src/main/java/cn/haut/survivor src/main/resources/templates/player src/main/resources/templates/dashboard
git commit -m "Add player profile and dashboard"
```

---

### Task 6: Implement Campus Map And Random Events

**Files:**
- Create: `MapController.java`
- Create: `EventService.java`
- Create: `EventServiceImpl.java`
- Create: `templates/map/index.html`
- Create: `templates/map/event.html`

- [ ] **Step 1: Write event service tests**

Cover:

- locations load from seed data
- enabled events are filtered by location
- choosing an event option updates attributes
- attribute values stay between 0 and 100 except experience
- event record is saved

- [ ] **Step 2: Implement map page**

Route:

```text
GET /map
```

Show location cards for Lotus Street campus.

- [ ] **Step 3: Implement event trigger**

Route:

```text
GET /map/location/{locationId}/event
```

Select a random enabled event for the location.

- [ ] **Step 4: Implement event option selection**

Route:

```text
POST /map/event/{eventId}/option/{optionId}
```

Apply attribute and experience changes, save `event_record`, and show result text.

- [ ] **Step 5: Verify manually**

Expected:

- clicking a location opens an event
- selecting an option changes dashboard attributes
- event results are saved in the database

- [ ] **Step 6: Commit**

```bash
git add src/main/java/cn/haut/survivor src/main/resources/templates/map
git commit -m "Add campus map and random events"
```

---

### Task 7: Implement Basic Task Management

**Files:**
- Create: `TaskController.java`
- Create: `TaskService.java`
- Create: `TaskServiceImpl.java`
- Create: `templates/task/index.html`

- [ ] **Step 1: Write task service tests**

Cover:

- user can create a task
- user can complete own task
- completing a task grants experience
- user cannot complete another user's task

- [ ] **Step 2: Implement task list**

Route:

```text
GET /tasks
```

Show current user's tasks grouped by status.

- [ ] **Step 3: Implement task creation**

Route:

```text
POST /tasks
```

Fields:

```text
task_name, task_type, difficulty, deadline, description
```

- [ ] **Step 4: Implement task completion**

Route:

```text
POST /tasks/{taskId}/complete
```

Reward by difficulty:

```text
C=20, B=40, A=70, S=120
```

- [ ] **Step 5: Verify manually**

Expected:

- task appears after creation
- completing task changes status to `DONE`
- dashboard experience increases

- [ ] **Step 6: Commit**

```bash
git add src/main/java/cn/haut/survivor src/main/resources/templates/task
git commit -m "Add basic task management"
```

---

### Task 8: Implement Admin Event Management

**Files:**
- Create: `AdminEventController.java`
- Create: `templates/admin/event-list.html`
- Create: `templates/admin/event-form.html`
- Modify: `EventService.java`

- [ ] **Step 1: Add admin authorization check**

Admin routes must require `LOGIN_USER_ROLE=ADMIN`.

- [ ] **Step 2: Implement event list**

Route:

```text
GET /admin/events
```

Show event name, type, location, probability, week range, and status.

- [ ] **Step 3: Implement event create and edit**

Routes:

```text
GET /admin/events/new
POST /admin/events
GET /admin/events/{eventId}/edit
POST /admin/events/{eventId}
```

- [ ] **Step 4: Implement event disable**

Route:

```text
POST /admin/events/{eventId}/disable
```

Set `status=0`.

- [ ] **Step 5: Verify manually**

Expected:

- admin can open `/admin/events`
- student cannot open `/admin/events`
- admin can add and disable an event

- [ ] **Step 6: Commit**

```bash
git add src/main/java/cn/haut/survivor src/main/resources/templates/admin
git commit -m "Add admin event management"
```

---

### Task 9: Polish Navigation And Demo Readiness

**Files:**
- Create: `src/main/resources/templates/fragments/layout.html`
- Create: `src/main/resources/static/css/app.css`
- Modify: all Thymeleaf pages
- Create: `README.md`

- [ ] **Step 1: Add shared layout fragment**

Include navigation links:

```text
Dashboard, Campus Map, Tasks, Admin Events, Logout
```

Only show Admin Events for admin users.

- [ ] **Step 2: Add app styling**

Use a restrained dashboard style with readable cards, progress bars, and clear buttons. Avoid decorative complexity.

- [ ] **Step 3: Add README run instructions**

Include:

```text
1. Create MySQL database haut_survivor
2. Update src/main/resources/application.yml credentials
3. Run mvn spring-boot:run
4. Open http://localhost:8080/login
5. Demo accounts: admin/admin123, student/student123
```

- [ ] **Step 4: Run final checks**

Run:

```bash
mvn test
mvn spring-boot:run
```

Expected:

- tests pass
- application starts
- demo flow works end to end in browser

- [ ] **Step 5: Commit**

```bash
git add README.md src/main/resources/templates src/main/resources/static/css/app.css
git commit -m "Polish demo navigation and docs"
```

---

## Final Verification Checklist

- [ ] `mvn test` passes.
- [ ] `mvn spring-boot:run` starts the app on port 8080.
- [ ] Login works for `student / student123`.
- [ ] Login works for `admin / admin123`.
- [ ] First-time user can create a player profile.
- [ ] Dashboard shows attributes and warnings.
- [ ] Campus map shows Lotus Street locations.
- [ ] Random event option changes attributes.
- [ ] User can create and complete a task.
- [ ] Admin can create, edit, and disable events.
- [ ] README run instructions match the actual app behavior.

## Scope Deliberately Deferred

These requirements belong to the complete version, not the Demo foundation:

- Dungeon challenge implementation.
- Achievement and title unlocking.
- Ending generation.
- Growth report charts.
- Ranking list.
- Full admin CRUD for all content tables.
- Spring Security integration.
