# Playable Demo Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Build a short, playable HAUT Survivor demo loop with a more visual campus map, richer event choices, and a "Java 课设：DDL 前夜" dungeon slice.

**Architecture:** Keep the current Spring Boot + Thymeleaf architecture. Add lightweight metadata fields to existing location/event tables, introduce focused dungeon tables/services/controllers, and render the first playable dungeon as a small stateful web flow backed by MySQL.

**Tech Stack:** Java 17, Spring Boot 3.3.5, MyBatis-Plus, MySQL, Thymeleaf, Bootstrap, plain CSS/JavaScript.

---

### Task 1: Visual Event Metadata

**Files:**
- Modify: `src/main/resources/schema.sql`
- Modify: `src/main/resources/data.sql`
- Modify: `src/main/java/cn/haut/survivor/domain/entity/CampusLocation.java`
- Modify: `src/main/java/cn/haut/survivor/domain/entity/Event.java`
- Modify: `src/main/java/cn/haut/survivor/domain/entity/EventOption.java`
- Test: `src/test/java/cn/haut/survivor/service/EventServiceTests.java`

- [x] **Step 1: Write failing tests for visual metadata**

Add assertions to `locationsLoadFromSeedData` that the seed locations expose `iconKey`, `backgroundImage`, and `themeColor`.

Add a new test:

```java
@Test
void eventOptionsExposePreviewAndRiskMetadata() {
    List<EventOption> options = eventService.listOptions(7L);

    assertThat(options).extracting(EventOption::getPreviewText)
            .contains("稳妥定位，技能收益高");
    assertThat(options).extracting(EventOption::getRiskLevel)
            .contains("low", "medium", "high");
}
```

- [x] **Step 2: Run tests to verify they fail**

Run: `.\mvnw.cmd -Dtest=EventServiceTests test`

Expected: FAIL because entity getters or SQL columns are missing.

- [x] **Step 3: Implement metadata fields**

Add fields to `CampusLocation`, `Event`, and `EventOption`, update `schema.sql` columns, and update seed inserts with values for all locations/events/options.

- [x] **Step 4: Run tests to verify they pass**

Run: `.\mvnw.cmd -Dtest=EventServiceTests test`

Expected: PASS.

### Task 2: Action Map And Event Presentation

**Files:**
- Modify: `src/main/java/cn/haut/survivor/controller/MapController.java`
- Modify: `src/main/resources/templates/map/index.html`
- Modify: `src/main/resources/templates/map/event.html`
- Modify: `src/main/resources/static/css/app.css`
- Test: `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`

- [x] **Step 1: Write failing controller tests**

Add to `mapPageShowsCampusLocations`:

```java
.andExpect(model().attributeExists("profile", "attribute", "statusLines"))
```

Add to `locationClickTriggersEventPage`:

```java
.andExpect(model().attributeExists("event", "options", "attribute"));
```

- [x] **Step 2: Run tests to verify they fail**

Run: `.\mvnw.cmd -Dtest=MapControllerTests test`

Expected: FAIL because the model attributes are missing.

- [x] **Step 3: Populate model and upgrade templates**

Update `MapController` to add `profile`, `attribute`, and `statusLines` to the map page, and `attribute` to the event page. Rewrite map/event templates to use action-map classes, visible status bars, risk chips, and richer result panels.

- [x] **Step 4: Run tests to verify they pass**

Run: `.\mvnw.cmd -Dtest=MapControllerTests test`

Expected: PASS.

### Task 3: DDL Dungeon Backend

**Files:**
- Modify: `src/main/resources/schema.sql`
- Modify: `src/main/resources/data.sql`
- Create: `src/main/java/cn/haut/survivor/domain/entity/Dungeon.java`
- Create: `src/main/java/cn/haut/survivor/domain/entity/DungeonTask.java`
- Create: `src/main/java/cn/haut/survivor/domain/entity/DungeonTaskOption.java`
- Create: `src/main/java/cn/haut/survivor/domain/entity/UserDungeonRecord.java`
- Create: `src/main/java/cn/haut/survivor/domain/entity/UserDungeonTaskRecord.java`
- Create: `src/main/java/cn/haut/survivor/mapper/DungeonMapper.java`
- Create: `src/main/java/cn/haut/survivor/mapper/DungeonTaskMapper.java`
- Create: `src/main/java/cn/haut/survivor/mapper/DungeonTaskOptionMapper.java`
- Create: `src/main/java/cn/haut/survivor/mapper/UserDungeonRecordMapper.java`
- Create: `src/main/java/cn/haut/survivor/mapper/UserDungeonTaskRecordMapper.java`
- Create: `src/main/java/cn/haut/survivor/service/DungeonService.java`
- Create: `src/main/java/cn/haut/survivor/service/impl/DungeonServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/DungeonServiceTests.java`

- [x] **Step 1: Write failing service tests**

Create tests that verify:

```java
@Test
void demoDungeonLoadsSeededStages() {
    Dungeon dungeon = dungeonService.findDemoDungeon();
    assertThat(dungeon.getDungeonName()).isEqualTo("Java 课设：DDL 前夜");
    assertThat(dungeonService.listTasks(dungeon.getId())).hasSizeGreaterThanOrEqualTo(3);
}

@Test
void choosingDungeonOptionUpdatesProgressAndAttributes() {
    UserDungeonRecord record = dungeonService.startOrResumeDemoDungeon(2L);
    DungeonTask task = dungeonService.findCurrentTask(record);
    DungeonTaskOption option = dungeonService.listOptions(task.getId()).get(0);

    UserDungeonTaskRecord taskRecord = dungeonService.chooseOption(2L, record.getId(), task.getId(), option.getId(), null);

    assertThat(taskRecord.getEvaluation()).isNotBlank();
    assertThat(dungeonService.startOrResumeDemoDungeon(2L).getCurrentTaskId()).isNotEqualTo(task.getId());
}
```

- [x] **Step 2: Run tests to verify they fail**

Run: `.\mvnw.cmd -Dtest=DungeonServiceTests test`

Expected: FAIL because dungeon classes do not exist.

- [x] **Step 3: Implement minimal dungeon service**

Add dungeon schema/seed data, entity/mapper classes, and service methods for finding the demo dungeon, listing tasks/options, starting/resuming, choosing an option, applying attribute/experience changes, and advancing to the next task.

- [x] **Step 4: Run tests to verify they pass**

Run: `.\mvnw.cmd -Dtest=DungeonServiceTests test`

Expected: PASS.

### Task 4: DDL Dungeon Pages And Navigation

**Files:**
- Create: `src/main/java/cn/haut/survivor/controller/DungeonController.java`
- Create: `src/main/resources/templates/dungeon/index.html`
- Create: `src/main/resources/templates/dungeon/play.html`
- Create: `src/main/resources/templates/dungeon/result.html`
- Modify: `src/main/resources/templates/fragments/layout.html`
- Modify: `src/main/resources/templates/dashboard/index.html`
- Modify: `src/main/resources/templates/map/index.html`
- Modify: `src/main/resources/static/css/app.css`
- Test: `src/test/java/cn/haut/survivor/controller/DungeonControllerTests.java`

- [x] **Step 1: Write failing controller tests**

Create tests that verify `/dungeons`, `/dungeons/demo/start`, and option submission render the expected views and model attributes.

- [x] **Step 2: Run tests to verify they fail**

Run: `.\mvnw.cmd -Dtest=DungeonControllerTests test`

Expected: FAIL because routes do not exist.

- [x] **Step 3: Implement dungeon pages**

Add navigation link, dungeon landing page, play page with task scene/options/minigame UI, and result page. The first minigame stage can be represented as selectable database relation choices with a timer-style visual, not a full canvas game.

- [x] **Step 4: Run tests to verify they pass**

Run: `.\mvnw.cmd -Dtest=DungeonControllerTests test`

Expected: PASS.

### Task 5: Full Verification

**Files:**
- Modify: `README.md`

- [x] **Step 1: Update README demo flow**

Document the new playable flow:

```text
登录 student/student123 -> 创建角色 -> 校园地图 -> 实验室事件 -> Java 课设：DDL 前夜 -> 数据库拼图 -> 结算
```

- [x] **Step 2: Run full test suite**

Run: `.\mvnw.cmd test`

Expected: BUILD SUCCESS.

- [x] **Step 3: Run app smoke check**

Run: `.\mvnw.cmd spring-boot:run`, then verify these pages return 200 or expected redirects:

```text
/login
/dashboard
/map
/dungeons
```

Expected: app starts and routes render after login.

---

## Self-Review

- Spec coverage: covers visual map, event choice metadata, one playable DDL dungeon, a lightweight minigame-like task, and updated demo flow.
- Intentional gaps: full admin CRUD for dungeon configuration is not included in this slice; this version focuses on player-facing demo playability.
- Placeholder scan: no TBD/TODO placeholders.
- Type consistency: dungeon service/entity names are used consistently across tasks.

