# Gameplay Depth Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the current DDL dungeon from a choice chain into a more playable mini-run with a real database relation minigame, carried consequences, and stronger final outcomes.

**Architecture:** Keep the existing dungeon tables and service as the core flow. Add lightweight `risk_flags` state to `user_dungeon_record`, implement a dedicated minigame settlement method, and let earlier choices modify later scores and result text.

**Tech Stack:** Java 17, Spring Boot 3.3.5, MyBatis-Plus, MySQL, Thymeleaf, plain CSS/JavaScript.

---

### Task 1: Consequence State

**Files:**
- Modify: `src/main/resources/schema.sql`
- Modify: `src/main/java/cn/haut/survivor/domain/entity/UserDungeonRecord.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/DungeonServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/DungeonServiceTests.java`

- [x] Write failing tests that assert choosing the best first-stage option stores `scope_controlled` and choosing the risky first-stage option stores `scope_sprawl`.
- [x] Run `.\mvnw.cmd -Dtest=DungeonServiceTests test` and verify RED.
- [x] Add `risk_flags` to `user_dungeon_record` and update service flag handling.
- [x] Run `.\mvnw.cmd -Dtest=DungeonServiceTests test` and verify GREEN.

### Task 2: Database Relation Minigame Settlement

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/DungeonService.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/DungeonServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/DungeonServiceTests.java`

- [x] Write failing tests for `chooseMinigameRelations`: correct relation choices should produce high score, `schema_clear`, and stage advance; poor choices should produce low score, `schema_mist`, higher pressure, and still advance.
- [x] Run `.\mvnw.cmd -Dtest=DungeonServiceTests test` and verify RED.
- [x] Implement score calculation from selected relations, elapsed seconds, current skill, and previous risk flags.
- [x] Run `.\mvnw.cmd -Dtest=DungeonServiceTests test` and verify GREEN.

### Task 3: Minigame UI

**Files:**
- Modify: `src/main/java/cn/haut/survivor/controller/DungeonController.java`
- Modify: `src/main/resources/templates/dungeon/play.html`
- Modify: `src/main/resources/static/css/app.css`
- Test: `src/test/java/cn/haut/survivor/controller/DungeonControllerTests.java`

- [x] Write failing tests that reach task 2, see relation choices, submit selected relations to `/dungeons/demo/task/2/minigame`, and receive a result.
- [x] Run `.\mvnw.cmd -Dtest=DungeonControllerTests test` and verify RED.
- [x] Add the minigame endpoint, checkbox relation UI, timer hidden field, and visual relation board.
- [x] Run `.\mvnw.cmd -Dtest=DungeonControllerTests test` and verify GREEN.

### Task 4: Final Outcome Flavor

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/DungeonServiceImpl.java`
- Modify: `src/main/resources/templates/dungeon/result.html`
- Test: `src/test/java/cn/haut/survivor/service/DungeonServiceTests.java`
- Test: `src/test/java/cn/haut/survivor/controller/DungeonControllerTests.java`

- [x] Write failing tests that a strong full run ends as warrior ending, while a weak run with `schema_mist` ends as silent-defense ending.
- [x] Run `.\mvnw.cmd -Dtest=DungeonServiceTests test` and verify RED.
- [x] Update final evaluation to use total score plus risk flags.
- [x] Ensure the final result page keeps the completed record and displays process tags.
- [x] Run `.\mvnw.cmd -Dtest=DungeonServiceTests,DungeonControllerTests test` and verify GREEN.

### Task 5: Verification

**Files:**
- Modify: `README.md`

- [x] Update README with the deeper gameplay flow.
- [x] Run `.\mvnw.cmd test`.
- [x] Run HTTP smoke check through login, dungeon task 1, database minigame, task 3, and final result.

---

## Self-Review

- Spec coverage: covers interactive minigame, consequence flags, and richer final outcome.
- Scope control: does not add full drag-and-drop or admin CRUD in this pass.
- Placeholder scan: no TBD/TODO placeholders.
