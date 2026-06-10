# CP6.1 NPC Prototypes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the CP6.1 campus NPC prototypes around the Lianhuajie content pack: 富少、小鱼、阿杰、柳如烟.

**Architecture:** Keep this as a seed-heavy expansion. Reuse existing `npc`, `npc_interaction`, `user_npc_relation`, `user_npc_story_progress`, and `user_influence_log`; do not add tables or backend mechanics in CP6.1. 阿杰 already exists as NPC id `1`, so CP6.1 must enrich the existing 阿杰 with new normal interactions instead of inserting a duplicate 阿杰.

**Tech Stack:** Spring Boot 3.3.5, Java 17, MyBatis-Plus, MySQL/H2-compatible SQL seed files, Thymeleaf, JUnit 5, AssertJ, MockMvc.

---

## Scope And ID Rules

- New NPC rows: `6101` 富少, `6102` 小鱼, `6103` 柳如烟.
- Existing NPC row reused: `1` 室友阿杰. Do not insert another 阿杰 row.
- New normal interaction rows: `610001` through `610011`.
- Do not use `900000` through `900999`; that range is reserved for Java-layer virtual NPC branch interactions in `NpcStoryBranchCatalog`.
- No schema changes.
- No new story branch mechanics in this pass.
- No template or CSS changes unless an existing page fails to render the new seed data.

## File Map

- Modify `src/main/resources/data-content-pack-6.sql`
  Add CP6.1 NPC seed rows and normal NPC interaction rows.
- Create `src/test/java/cn/haut/survivor/service/ContentPack6NpcTests.java`
  Verify seed loading, no duplicate 阿杰, interaction IDs, normal interaction availability, and interaction settlement.
- Modify `src/test/java/cn/haut/survivor/service/ContentPack1Tests.java`
  Change `seedNpcsCountIs5` to an at-least assertion because CP6.1 raises active NPC count.
- Modify `src/test/java/cn/haut/survivor/controller/NpcControllerTests.java`
  Add one controller smoke test for a new CP6.1 NPC detail page and one POST interaction result.
- Modify `docs/PROJECT_COMPLETION_STATUS.md`
  Record CP6.1 as completed only after tests and smoke pass.
- Modify `docs/NEXT_AI_HANDOFF.md`
  Update the handoff with CP6.1 details, verification result, and remaining risk.
- Optional modify `docs/AI_CONTINUATION_PROMPT.md`
  Only if it still lists CP6.1 as the immediate next task after implementation.

## Task 1: Add Failing Seed Tests

**Files:**
- Create: `src/test/java/cn/haut/survivor/service/ContentPack6NpcTests.java`

- [ ] **Step 1: Create the failing test class**

Create `src/test/java/cn/haut/survivor/service/ContentPack6NpcTests.java` with this complete content:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.mapper.NpcInteractionMapper;
import cn.haut.survivor.mapper.NpcMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack6NpcTests {

    @Autowired
    private NpcMapper npcMapper;

    @Autowired
    private NpcInteractionMapper npcInteractionMapper;

    @Autowired
    private NpcService npcService;

    @Autowired
    private PlayerService playerService;

    @Test
    void cp6NpcPrototypesAreSeededWithoutDuplicatingAjie() {
        List<Npc> cp6Npcs = npcMapper.selectList(new LambdaQueryWrapper<Npc>()
                .in(Npc::getId, 6101L, 6102L, 6103L)
                .orderByAsc(Npc::getId));

        assertThat(cp6Npcs).hasSize(3);
        assertThat(cp6Npcs).extracting(Npc::getNpcName)
                .containsExactly("富少", "小鱼", "柳如烟");
        assertThat(cp6Npcs).allMatch(npc -> npc.getActive() == 1);
        assertThat(cp6Npcs).allMatch(npc -> npc.getHomeLocationId() != null
                && npc.getHomeLocationId() >= 1L
                && npc.getHomeLocationId() <= 8L);
        assertThat(cp6Npcs).extracting(Npc::getFavoriteAttribute)
                .contains("money", "health", "academic");

        long ajieCount = npcMapper.selectCount(new LambdaQueryWrapper<Npc>()
                .like(Npc::getNpcName, "阿杰")
                .eq(Npc::getActive, 1));
        assertThat(ajieCount).isEqualTo(1);

        Npc ajie = npcMapper.selectById(1L);
        assertThat(ajie).isNotNull();
        assertThat(ajie.getNpcName()).contains("阿杰");
    }

    @Test
    void cp6NpcInteractionsAreNormalSeedRowsOutsideVirtualBranchRange() {
        List<NpcInteraction> interactions = npcInteractionMapper.selectList(new LambdaQueryWrapper<NpcInteraction>()
                .ge(NpcInteraction::getId, 610001L)
                .le(NpcInteraction::getId, 610011L)
                .orderByAsc(NpcInteraction::getId));

        assertThat(interactions).hasSize(11);
        assertThat(interactions).allMatch(interaction -> interaction.getActive() == 1);
        assertThat(interactions).allMatch(interaction -> interaction.getId() < 900000L
                || interaction.getId() > 900999L);
        assertThat(interactions).allMatch(interaction -> interaction.getInteractionKey().startsWith("cp6_"));
        assertThat(interactions).allMatch(interaction -> interaction.getFamiliarityChange() != null
                && interaction.getFamiliarityChange() > 0);
        assertThat(interactions).allMatch(interaction -> interaction.getExpChange() != null
                && interaction.getExpChange() > 0);

        Map<Long, Long> countByNpc = interactions.stream()
                .collect(Collectors.groupingBy(NpcInteraction::getNpcId, Collectors.counting()));
        assertThat(countByNpc).containsEntry(1L, 2L);
        assertThat(countByNpc).containsEntry(6101L, 3L);
        assertThat(countByNpc).containsEntry(6102L, 3L);
        assertThat(countByNpc).containsEntry(6103L, 3L);

        assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
                .contains(
                        "cp6_ajie_lianhuajie_walk",
                        "cp6_fushao_canteen_tip",
                        "cp6_xiaoyu_light_meal",
                        "cp6_liuruyan_library_focus");
    }

    @Test
    void cp6NpcInteractionsUnlockByFamiliarity() {
        playerService.createProfile(2L, "cp6 npc unlock test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 6101L, 25);

        List<NpcInteraction> interactions = npcService.listAvailableInteractions(2L, 6101L, 1);

        assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
                .contains("cp6_fushao_canteen_tip", "cp6_fushao_budget_chat")
                .doesNotContain("cp6_fushao_resource_trade");
    }

    @Test
    void cp6NpcInteractionSettlesAttributesAndConsumesActionPoint() {
        playerService.createProfile(2L, "cp6 npc interaction test", "大二", "计算机类", "就业路线");
        npcService.increaseFamiliarity(2L, 6103L, 25);
        int beforeAp = playerService.findProfileByUserId(2L).getActionPoints();
        PlayerAttribute before = playerService.findAttributeByUserId(2L);

        NpcService.NpcInteractionResult result = npcService.interact(2L, 6103L, 610009L, 1);

        PlayerAttribute after = playerService.findAttributeByUserId(2L);
        assertThat(playerService.findProfileByUserId(2L).getActionPoints()).isEqualTo(beforeAp - 1);
        assertThat(result.interaction().getInteractionKey()).isEqualTo("cp6_liuruyan_library_focus");
        assertThat(result.attributeChange().academicChange()).isPositive();
        assertThat(result.attributeChange().pressureChange()).isGreaterThanOrEqualTo(0);
        assertThat(after.getAcademic()).isEqualTo(before.getAcademic() + result.attributeChange().academicChange());
        assertThat(result.familiarityGain()).isPositive();
        assertThat(result.storyResult()).isNull();
    }
}
```

- [ ] **Step 2: Run the focused failing tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6NpcTests test
```

Expected: fail because `ContentPack6NpcTests` compiles but CP6.1 seed rows do not exist yet. The failure should mention missing NPCs or missing interactions.

- [ ] **Step 3: Commit the failing tests only if your workflow allows red commits**

Preferred for this project: do not commit the failing state. Keep the test file unstaged until Task 2 passes.

## Task 2: Add CP6.1 NPC Seed Rows

**Files:**
- Modify: `src/main/resources/data-content-pack-6.sql`

- [ ] **Step 1: Append CP6.1 seed data**

Append this section to the end of `src/main/resources/data-content-pack-6.sql`. Keep the file encoded as UTF-8.

```sql
-- ============================================================
-- Content Pack 6.1: 莲花街校区 NPC 原型
-- ============================================================

INSERT INTO npc
(id, npc_name, npc_type, home_location_id, personality, description, avatar_icon, favorite_attribute, active) VALUES
(6101, '富少', '同学', 4, '消息灵、出手大方、爱把小事说得很夸张',
 '经常在食堂和社团摊位附近出现的同学，知道不少校内资源和省钱门路；他说话浮夸，但偶尔真能帮你绕开麻烦。',
 '💳', 'money', 1),
(6102, '小鱼', '同学', 4, '温和、会照顾人、对吃饭和作息很有研究',
 '总能找到食堂里相对靠谱的窗口，也会提醒你别靠奶茶和熬夜硬撑。和她相处时，校园生活会变得没那么慌。',
 '🐟', 'health', 1),
(6103, '柳如烟', '同学', 2, '清醒、克制、目标感强，说话偶尔有点刺',
 '图书馆常驻的高压自律型同学。她不太会安慰人，但很擅长把混乱的复习和作业拆成能执行的计划。',
 '🪶', 'academic', 1);

INSERT INTO npc_interaction
(id, npc_id, interaction_key, interaction_name, required_familiarity, description, result_text,
 academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change,
 familiarity_change, exp_change, active) VALUES
(610001, 1, 'cp6_ajie_lianhuajie_walk', '莲花街夜路搭话', 0,
 '阿杰说宿舍待久了脑子会糊，拉你去楼下走一圈，顺便吐槽今天校园网又抽风。',
 '你们沿着宿舍区慢慢走，聊了食堂、作业和这周的烦心事。问题没有立刻解决，但心里松了一点。',
 0, 1, 0, 2, 0, -4, 0, 4, 18, 1),
(610002, 1, 'cp6_ajie_router_rescue', '宿舍网络救场', 25,
 '阿杰蹲在路由器旁边研究指示灯，嘴上说自己也不懂，手却已经开始排查网线。',
 '网络恢复了，作业终于能上传。阿杰得意地说这叫宿舍生存技能，你决定先不反驳。',
 1, 0, 0, 1, 2, -2, 1, 5, 24, 1),

(610003, 6101, 'cp6_fushao_canteen_tip', '食堂窗口情报', 0,
 '富少神神秘秘地告诉你，今天三楼有个窗口排队短、分量稳，还能避开饭点高峰。',
 '你按他的情报少排了很久的队，顺便听他讲了一堆校园资源传闻。真假先不说，省下的时间是真的。',
 0, 1, 5, 2, 0, -1, 0, 4, 20, 1),
(610004, 6101, 'cp6_fushao_budget_chat', '预算复盘', 20,
 '富少看了看你的消费记录，表情严肃得像在审项目预算。',
 '他帮你把这周花销拆了一遍，虽然语气浮夸，但你确实发现了几个不必要的开支。',
 0, 0, 12, 1, 1, 1, 2, 5, 26, 1),
(610005, 6101, 'cp6_fushao_resource_trade', '资源互换', 55,
 '富少说他认识一个刚好需要组队的人，但你也得拿出一点靠谱的资料交换。',
 '你们交换了课程资料和小组消息。社交成本不低，但信息差被补上后，事情推进得快了不少。',
 2, 0, 6, 4, 2, 1, 1, 7, 36, 1),

(610006, 6102, 'cp6_xiaoyu_light_meal', '轻食窗口打卡', 0,
 '小鱼指了指知味餐厅的窗口，说今天这家不油，适合在连轴转之前先把胃稳住。',
 '你终于吃了一顿像样的饭。下午的状态没有奇迹般爆发，但至少没有继续下滑。',
 0, 5, -6, 1, 0, -2, 1, 4, 20, 1),
(610007, 6102, 'cp6_xiaoyu_schedule_check', '作息提醒', 20,
 '小鱼问你昨晚几点睡，你迟疑了两秒，她已经懂了。',
 '她帮你把今晚的任务压缩到最关键的两项。少做一点不代表摆烂，有时候是为了明天还能继续。',
 1, 4, 0, 1, 0, -3, 3, 5, 28, 1),
(610008, 6102, 'cp6_xiaoyu_recovery_walk', '饭后散步', 50,
 '小鱼说吃完饭别立刻回去坐着，绕操场慢慢走一圈就好。',
 '你们没有聊太重的话题，只是沿着操场走了一圈。身体缓过来后，脑子也清楚了一点。',
 0, 6, 0, 2, 0, -4, 2, 7, 34, 1),

(610009, 6103, 'cp6_liuruyan_library_focus', '图书馆专注位', 0,
 '柳如烟把你带到图书馆一个相对安静的位置，然后让你先写下今天必须完成的三件事。',
 '被她盯着学习有点压力，但你确实把最难开始的部分推进了。计划一旦落到纸上，就没那么吓人。',
 5, 0, 0, 1, 1, 2, 3, 4, 26, 1),
(610010, 6103, 'cp6_liuruyan_review_plan', '复习计划切片', 25,
 '柳如烟看完你的复习安排，直接划掉了三项看起来很努力但收益很低的内容。',
 '她的建议不算温柔，却很有效。你把复习拆成几段之后，终于知道下一小时该做什么。',
 6, 0, 0, 0, 2, 1, 4, 5, 32, 1),
(610011, 6103, 'cp6_liuruyan_hard_question', '难题互问', 55,
 '柳如烟说光看懂不算会，让你现场讲一遍最容易混的知识点。',
 '你讲得磕磕绊绊，但漏洞暴露得很彻底。压力上来了，掌握度也真的上来了。',
 8, 0, 0, 1, 3, 4, 3, 7, 40, 1);
```

- [ ] **Step 2: Run the focused seed tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6NpcTests test
```

Expected: pass all four tests in `ContentPack6NpcTests`.

- [ ] **Step 3: Commit seed and focused tests**

Run:

```powershell
git add src/main/resources/data-content-pack-6.sql src/test/java/cn/haut/survivor/service/ContentPack6NpcTests.java
git commit -m "feat: add CP6.1 NPC prototypes"
```

Expected: commit succeeds.

## Task 3: Update Existing Count And Controller Tests

**Files:**
- Modify: `src/test/java/cn/haut/survivor/service/ContentPack1Tests.java`
- Modify: `src/test/java/cn/haut/survivor/controller/NpcControllerTests.java`

- [ ] **Step 1: Update active NPC count assertion**

In `ContentPack1Tests`, replace the existing method:

```java
@Test
void seedNpcsCountIs5() {
    long count = npcMapper.selectCount(new LambdaQueryWrapper<Npc>().eq(Npc::getActive, 1));
    assertThat(count).isEqualTo(5);
}
```

with:

```java
@Test
void seedNpcsCountIsAtLeast5() {
    long count = npcMapper.selectCount(new LambdaQueryWrapper<Npc>().eq(Npc::getActive, 1));
    assertThat(count).isGreaterThanOrEqualTo(8);
}
```

- [ ] **Step 2: Add CP6.1 NPC controller smoke tests**

Append these tests inside `NpcControllerTests`:

```java
@Test
void cp6NpcDetailPageRenders() throws Exception {
    npcService.increaseFamiliarity(2L, 6101L, 25);

    mockMvc.perform(get("/npcs/6101")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
            .andExpect(status().isOk())
            .andExpect(view().name("npc/detail"))
            .andExpect(model().attributeExists("npc", "relation", "relationSummary", "interactions"))
            .andExpect(model().attribute("interactions", hasItem(
                    hasProperty("interactionKey", equalTo("cp6_fushao_canteen_tip")))));
}

@Test
void cp6NpcInteractionShowsResultPage() throws Exception {
    npcService.increaseFamiliarity(2L, 6101L, 25);

    mockMvc.perform(post("/npcs/6101/interactions/610003")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
            .andExpect(status().isOk())
            .andExpect(view().name("npc/result"))
            .andExpect(model().attributeExists("result", "relationSummary", "profile", "attribute"));
}
```

The imports already present in `NpcControllerTests` cover `get`, `post`, `model`, `status`, `view`, `equalTo`, `hasItem`, and `hasProperty`.

- [ ] **Step 3: Run focused regression tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack1Tests,ContentPack6NpcTests,NpcControllerTests test
```

Expected: all selected tests pass.

- [ ] **Step 4: Commit test updates**

Run:

```powershell
git add src/test/java/cn/haut/survivor/service/ContentPack1Tests.java src/test/java/cn/haut/survivor/controller/NpcControllerTests.java
git commit -m "test: cover CP6.1 NPC pages"
```

Expected: commit succeeds.

## Task 4: Full Verification And HTTP Smoke

**Files:**
- No code files.

- [ ] **Step 1: Run full clean test**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
Failures: 0
Errors: 0
```

Record the final test count in the implementation feedback.

- [ ] **Step 2: Start the app**

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected: app starts on `http://localhost:8080`.

- [ ] **Step 3: HTTP smoke with a valid user session**

Use the repo's established smoke method or a browser session. Verify these routes:

```text
GET  /dashboard
GET  /map
GET  /exploration
POST /exploration/4
GET  /week/summary
GET  /npcs/1
GET  /npcs/6101
GET  /npcs/6102
GET  /npcs/6103
POST /npcs/6101/interactions/610003
GET  /dungeons
GET  /organizations
```

Expected:

```text
All checked pages return 200 after login/profile setup.
No Whitelabel error page.
NPC pages include game-dock.
POST /npcs/6101/interactions/610003 renders npc/result or redirects only if AP/profile/session precondition is not set correctly.
```

- [ ] **Step 4: Browser visual smoke if any template/CSS changed**

If Task 1 through Task 3 only changed SQL/tests/docs, browser visual smoke is optional. If any template/CSS was changed, check:

```text
1366x768: /npcs/6101, /npcs/6102, /npcs/6103
375x812:  /npcs/6101, /npcs/6102, /npcs/6103
```

Expected:

```text
No horizontal scrolling.
Dock does not cover the final action buttons.
Long NPC descriptions wrap within the card.
Interaction cards keep their buttons visible.
```

## Task 5: Update Documentation

**Files:**
- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
- Modify: `docs/NEXT_AI_HANDOFF.md`
- Optional modify: `docs/AI_CONTINUATION_PROMPT.md`

- [ ] **Step 1: Update project completion status**

Add a CP6.1 section to `docs/PROJECT_COMPLETION_STATUS.md` with these facts after verification:

```markdown
## CP6.1 NPC 原型接入

状态：已完成

本批次复用现有 NPC 系统和 CP6 莲花街内容包，不新增数据库表。

新增/调整内容：
- 新增 NPC：富少（6101）、小鱼（6102）、柳如烟（6103）。
- 复用已有 NPC：室友阿杰（1），追加莲花街校区相关普通互动，不新增重复阿杰。
- 新增普通 NPC 互动：610001-610011。
- 未使用 900000-900999 虚拟分支互动保留段。

验证：
- `.\mvnw.cmd clean test`
- HTTP smoke 覆盖 `/npcs/1`、`/npcs/6101`、`/npcs/6102`、`/npcs/6103` 和至少一次 CP6.1 互动 POST。
```

- [ ] **Step 2: Update next AI handoff**

In `docs/NEXT_AI_HANDOFF.md`, move CP6.1 from "recommended next step" to "completed", and update the recommended next step to:

```markdown
建议下一步：
- CP6.2 地点细分：图书馆楼层、惟学楼、博闻楼营业厅、韶华楼、知味/知雅餐厅。
- CP6.3 开学迎新周机制化。
```

Also include the final clean test count and HTTP smoke result from Task 4.

- [ ] **Step 3: Update AI continuation prompt only if stale**

If `docs/AI_CONTINUATION_PROMPT.md` still says CP6.1 is the next recommended task, change it to CP6.2/CP6.3. If it does not mention CP6.1 as next, leave it alone.

- [ ] **Step 4: Commit docs**

Run:

```powershell
git add docs/PROJECT_COMPLETION_STATUS.md docs/NEXT_AI_HANDOFF.md docs/AI_CONTINUATION_PROMPT.md
git commit -m "docs: update handoff for CP6.1"
```

If `docs/AI_CONTINUATION_PROMPT.md` was not changed, remove it from the `git add` command.

Expected: commit succeeds.

## Final Implementation Feedback Required

The coding AI must report these items:

1. Modified files.
2. Added tables/fields: expected answer is none.
3. Added seed data: NPC IDs and interaction IDs.
4. Added/modified tests.
5. `.\mvnw.cmd clean test` result with exact test count.
6. HTTP smoke result table.
7. Browser visual smoke result if any UI file changed.
8. Git commit hashes.
9. Remaining risks.

## Self-Review Notes

- Spec coverage: covers all four requested prototypes while avoiding duplicate 阿杰.
- Placeholder scan: no unfinished placeholder markers remain.
- Type consistency: tests use existing `Npc`, `NpcInteraction`, `NpcMapper`, `NpcInteractionMapper`, `NpcService`, and `PlayerService` APIs.
- ID safety: normal seed IDs stay in `610xxx`; virtual story branch IDs stay untouched in `900xxx`.
- Scope safety: no schema, backend mechanism, or UI system rewrite is required.
