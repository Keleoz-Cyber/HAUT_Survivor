# CP6 Lianhuajie Campus Content Pack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a seed-heavy CP6 content pack based on `docs/补充信息.docx`, making the demo feel more like HAUT Lianhuajie Campus without rewriting the game loop.

**Architecture:** Keep CP6 as a low-risk content pack: one new SQL seed file, one config change to load it, one focused seed test class, and small documentation updates. Reuse existing location ids, dungeon tables, organization tables, event tables, rumor effect fields, and existing player UI pages.

**Tech Stack:** Spring Boot 3.3.5, Java 17, MyBatis-Plus, MySQL, Thymeleaf, JUnit 5, AssertJ, Maven Wrapper on Windows PowerShell.

---

## Execution Rules

- Work in `D:\study\code\java\classlearn\HAUT_Survivor`.
- Do not rewrite backend mechanisms.
- Do not replace the current player UI system.
- Do not delete existing features, seed data, tests, or docs.
- Existing worktree may be dirty. Read `git status --short` first and preserve unrelated changes.
- Use CP6 id range `6001+`.
- Use UTF-8 for Chinese seed text.
- After code/data changes, run `.\mvnw.cmd clean test`.
- If any page or content surface changes, run HTTP smoke tests.
- If templates/CSS are changed, also run browser checks at `1366x768` and `375x812`.
- Do not use insulting or defamatory wording copied directly from `docs/补充信息.docx`. Student-union content must be toned down into campus satire or ordinary bureaucracy.

## Must-Read Files

- `README.md`
- `docs/PROJECT_COMPLETION_STATUS.md`
- `docs/NEXT_AI_HANDOFF.md`
- `docs/补充信息.docx`
- `src/main/resources/schema.sql`
- `src/main/resources/application.yml`
- `src/main/resources/data.sql`
- `src/main/resources/data-content-pack-4.sql`
- `src/test/java/cn/haut/survivor/service/ContentPack4Tests.java`
- `src/main/java/cn/haut/survivor/controller/DungeonController.java`
- `src/main/java/cn/haut/survivor/controller/OrganizationController.java`
- `src/main/java/cn/haut/survivor/controller/MapController.java`
- `src/main/java/cn/haut/survivor/controller/ExplorationController.java`

## File Map

- Create `src/main/resources/data-content-pack-6.sql`
  - Owns all CP6 seed data.
  - Includes organizations, events, event options, rumors, and one dungeon.
  - Uses existing tables only.
- Modify `src/main/resources/application.yml`
  - Adds `classpath:data-content-pack-6.sql` to `spring.sql.init.data-locations`.
- Create `src/test/java/cn/haut/survivor/service/ContentPack6Tests.java`
  - Verifies CP6 seed rows load and remain internally consistent.
- Modify `docs/PROJECT_COMPLETION_STATUS.md`
  - Records CP6 scope and verification result.
- Modify `docs/NEXT_AI_HANDOFF.md`
  - Adds handoff notes for the next AI.
- Do not modify `schema.sql` for CP6 first batch.
- Do not modify templates or CSS unless smoke/visual checks reveal a real display regression.

## Existing Location Mapping

Reuse the current eight base locations instead of adding a large map taxonomy in CP6.

| Existing location id | CP6 localized meaning |
|---:|---|
| `1` | 博识/博学/博闻教学楼群 |
| `2` | 莲花街校区图书馆 |
| `3` | 德园/勤园宿舍区 |
| `4` | 知味/知雅餐厅 |
| `5` | 东西操场/体育训练中心 |
| `6` | 惟学楼三楼实验室/粮食科创实验中心 |
| `7` | 韶华楼前广场/钟楼广场/社团活动区 |
| `8` | 31号楼北侧/洗浴中心北侧驿站 |

## CP6 Content Scope

Implement these in the first CP6 batch:

- Five organizations:
  - `6001` 计算机协会
  - `6002` 信息学院学生会
  - `6003` 信息学院辩论队
  - `6004` 轮滑社
  - `6005` 校合唱团
- Eight campus events:
  - `6001` 实验数据“蒸发”
  - `6002` 突发的实训任务
  - `6003` 大佬来校讲座
  - `6004` 断网危机
  - `6005` 健康餐挑战
  - `6006` 通宵自习室的陌生人
  - `6007` 路过的社团招新
  - `6008` 座位之战
- Six rumors:
  - `6001` 食堂三楼新开减脂轻食窗口
  - `6002` 操场合唱社团露天表演
  - `6003` 教学楼课堂临时点名风声
  - `6004` 图书馆八楼 TP 区空位多
  - `6005` 韶华楼前社团摊位送文创
  - `6006` 快递站雨后错峰取件
- One dungeon:
  - `6001` 小组作业
  - Five stages: 公布名单、线上开会、各自开荒、整合攻坚、卡点提交
- Defer NPC seed expansion to a later CP unless the user asks for CP6-NPC:
  - Reason: current project already has NPC relationship/story changes in dirty worktree; CP6 first batch should avoid entangling narrative relationship systems.

## Task 1: Preflight And Baseline

**Files:**
- Read: files listed in "Must-Read Files"
- No modification in this task

- [ ] **Step 1: Inspect current worktree**

Run:

```powershell
git status --short
```

Expected:

```text
May show existing modified/untracked files from CP4/CP5. Do not revert them.
```

- [ ] **Step 2: Confirm CP6 seed file does not already exist**

Run:

```powershell
Test-Path src\main\resources\data-content-pack-6.sql
```

Expected:

```text
False
```

If it prints `True`, open the file and merge this plan with existing CP6 work instead of overwriting it.

- [ ] **Step 3: Confirm SQL init list currently omits CP6**

Run:

```powershell
Select-String -Path src\main\resources\application.yml -Pattern "data-content-pack-6"
```

Expected:

```text
No output
```

## Task 2: Write Failing CP6 Seed Tests

**Files:**
- Create: `src/test/java/cn/haut/survivor/service/ContentPack6Tests.java`

- [ ] **Step 1: Create the test class**

Create `src/test/java/cn/haut/survivor/service/ContentPack6Tests.java` with this complete content:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.DungeonTaskOption;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.Organization;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.mapper.DungeonMapper;
import cn.haut.survivor.mapper.DungeonTaskMapper;
import cn.haut.survivor.mapper.DungeonTaskOptionMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.OrganizationMapper;
import cn.haut.survivor.mapper.RumorMapper;
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
class ContentPack6Tests {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private DungeonMapper dungeonMapper;

    @Autowired
    private DungeonTaskMapper dungeonTaskMapper;

    @Autowired
    private DungeonTaskOptionMapper dungeonTaskOptionMapper;

    @Test
    void lianhuajieOrganizationsAreSeededWithSanitizedCopy() {
        List<Organization> organizations = organizationMapper.selectList(new LambdaQueryWrapper<Organization>()
                .ge(Organization::getId, 6001L)
                .le(Organization::getId, 6005L)
                .orderByAsc(Organization::getId));

        assertThat(organizations).hasSize(5);
        assertThat(organizations).extracting(Organization::getOrgName)
                .containsExactly("计算机协会", "信息学院学生会", "信息学院辩论队", "轮滑社", "校合唱团");
        assertThat(organizations).allMatch(org -> org.getUnlockLocationId() != null);
        assertThat(organizations).allMatch(org -> org.getWeeklyApCost() >= 1);

        Organization studentUnion = organizations.stream()
                .filter(org -> "信息学院学生会".equals(org.getOrgName()))
                .findFirst()
                .orElseThrow();
        assertThat(studentUnion.getDescription())
                .doesNotContain("官僚主义")
                .doesNotContain("虚荣心")
                .doesNotContain("闲着没事");
    }

    @Test
    void lianhuajieCampusEventsHaveThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6001L)
                .le(Event::getId, 6008L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(8);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "实验数据“蒸发”",
                        "突发的实训任务",
                        "大佬来校讲座",
                        "断网危机",
                        "健康餐挑战",
                        "通宵自习室的陌生人",
                        "路过的社团招新",
                        "座位之战"
                );
        assertThat(events).allMatch(event -> event.getLocationId() >= 1L && event.getLocationId() <= 8L);
        assertThat(events).allMatch(event -> event.getProbability() >= 25 && event.getProbability() <= 80);

        List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                .ge(EventOption::getEventId, 6001L)
                .le(EventOption::getEventId, 6008L));

        Map<Long, Long> optionCountByEvent = options.stream()
                .collect(Collectors.groupingBy(EventOption::getEventId, Collectors.counting()));
        assertThat(optionCountByEvent).hasSize(8);
        assertThat(optionCountByEvent.values()).allMatch(count -> count >= 3);
    }

    @Test
    void lianhuajieRumorsUseExistingEffectTypes() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6001L)
                .le(Rumor::getId, 6006L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(6);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .contains(
                        "食堂三楼新开减脂轻食窗口",
                        "操场合唱社团露天表演",
                        "图书馆八楼 TP 区空位多"
                );
        assertThat(rumors).extracting(Rumor::getEffectType)
                .contains("attr_bonus", "npc_boost", "explore_bonus", "safe_zone", "event_hint");
        assertThat(rumors).allMatch(rumor -> rumor.getEffectValue() != null);
        assertThat(rumors).allMatch(rumor -> rumor.getEffectTarget() != null && !rumor.getEffectTarget().isBlank());
    }

    @Test
    void groupAssignmentDungeonIsSeededWithOrderedStages() {
        Dungeon dungeon = dungeonMapper.selectById(6001L);

        assertThat(dungeon).isNotNull();
        assertThat(dungeon.getDungeonName()).isEqualTo("小组作业");
        assertThat(dungeon.getDungeonType()).isEqualTo("academic");
        assertThat(dungeon.getRewardExp()).isGreaterThanOrEqualTo(80);

        List<DungeonTask> tasks = dungeonTaskMapper.selectList(new LambdaQueryWrapper<DungeonTask>()
                .eq(DungeonTask::getDungeonId, 6001L)
                .orderByAsc(DungeonTask::getTaskOrder));

        assertThat(tasks).hasSize(5);
        assertThat(tasks).extracting(DungeonTask::getTaskName)
                .containsExactly("公布小组名单", "线上开会分工", "各自开荒", "整合攻坚", "卡点提交");
        assertThat(tasks).extracting(DungeonTask::getTaskOrder)
                .containsExactly(1, 2, 3, 4, 5);

        List<Long> taskIds = tasks.stream().map(DungeonTask::getId).toList();
        List<DungeonTaskOption> options = dungeonTaskOptionMapper.selectList(new LambdaQueryWrapper<DungeonTaskOption>()
                .in(DungeonTaskOption::getDungeonTaskId, taskIds));

        Map<Long, Long> optionCountByTask = options.stream()
                .collect(Collectors.groupingBy(DungeonTaskOption::getDungeonTaskId, Collectors.counting()));
        assertThat(optionCountByTask).hasSize(5);
        assertThat(optionCountByTask.values()).allMatch(count -> count >= 3);
        assertThat(options).anyMatch(option -> option.getPressureChange() < 0);
        assertThat(options).anyMatch(option -> option.getSocialChange() > 0);
        assertThat(options).anyMatch(option -> option.getSkillChange() > 0);
    }
}
```

- [ ] **Step 2: Run the new test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6Tests test
```

Expected:

```text
Tests run: 4, Failures: 4
```

The exact assertion messages may differ. The important signal is that CP6 rows are absent before the seed file and config are added.

## Task 3: Add CP6 Seed SQL

**Files:**
- Create: `src/main/resources/data-content-pack-6.sql`

- [ ] **Step 1: Create `data-content-pack-6.sql`**

Create `src/main/resources/data-content-pack-6.sql` with this complete content:

```sql
-- ============================================================
-- Content Pack 6: 莲花街校区真实校园内容包
-- ============================================================

INSERT INTO organization
(id, org_name, org_type, description, unlock_location_id, unlock_explore_level, recommended_attribute, weekly_ap_cost, theme_color, status) VALUES
(6001, '计算机协会', '技术社团', '技术味比较重的社团。有人认真分享项目经验，也有人来蹭资料和认识师兄师姐；适合想做项目、打比赛、补 Git 基础的新生。', 6, 15, 'skill', 1, '#2563eb', 1),
(6002, '信息学院学生会', '学生组织', '活动、通知、物资和现场执行都不少，能刷到存在感和综合评价，也会遇到临时任务、反复改材料和会议拉扯。', 7, 20, 'social', 2, '#7c3aed', 1),
(6003, '信息学院辩论队', '竞赛队', '训练气氛认真，重视资料、逻辑和临场表达。备赛会带来压力，但也能明显提升表达、检索和抗压能力。', 7, 25, 'social', 2, '#dc2626', 1),
(6004, '轮滑社', '兴趣社团', '氛围随性，常见活动是操场练习、社团聚餐和带新手找平衡。适合把运动当成减压出口。', 5, 10, 'health', 1, '#16a34a', 1),
(6005, '校合唱团', '文艺社团', '常为晚会、毕业季和校园活动准备节目。排练会占时间，但站上舞台时压力会被掌声冲淡一截。', 7, 20, 'social', 2, '#db2777', 1);

INSERT INTO `event`
(id, event_name, event_type, location_id, description, scene_image, mood_tag, probability, min_week, max_week, min_explore_level, status) VALUES
(6001, '实验数据“蒸发”', '学习', 6, '你正在惟学楼三楼实验室做课设关键实验，电脑突然蓝屏，辛苦跑了一下午的数据还没保存。指导老师一会儿要来看进度。', 'scene-lab', 'DDL', 55, 2, 20, 10, 1),
(6002, '突发的实训任务', '学习', 6, '班级群突然通知下午在粮食科创实验中心增加一次实训，参与可加实践分，不去会影响平时成绩。', 'scene-lab', '临时通知', 45, 1, 18, 0, 1),
(6003, '大佬来校讲座', '学习', 6, '惟实楼报告厅有一场关于粮食未来科技的讲座，主讲人是行业专家。你有点累，但室友已经把报名链接甩了过来。', 'scene-campus', '讲座', 35, 2, 18, 0, 1),
(6004, '断网危机', '生活', 3, '你正在宿舍赶论文，校园网突然断开。重启无效，博闻楼一楼校园网营业厅可能快下班了。', 'scene-dorm', '危机', 50, 1, 20, 0, 1),
(6005, '健康餐挑战', '健康', 4, '知味餐厅三楼上新减脂轻食窗口，价格略高，但听说连续打卡一周能抽奖。朋友们正在犹豫要不要组队。', 'scene-canteen', '轻食', 45, 1, 20, 0, 1),
(6006, '通宵自习室的陌生人', '社交', 2, '期末前夜，你在通宵自习区复习，对面的同学不小心打翻水杯。尴尬之后，你们发现彼此都很焦虑。', 'scene-library', '深夜', 35, 8, 20, 15, 1),
(6007, '路过的社团招新', '组织', 7, '你下课经过韶华楼前广场，十几个社团正在快闪招新。有人唱歌，有人展示机器人，你手里还有没写完的作业。', 'scene-campus', '热闹', 60, 1, 4, 0, 1),
(6008, '座位之战', '课堂', 1, '你提前半小时到教室占座，发现常坐的位置上已经放了一本书，但人一直没来，马上就要上课。', 'scene-classroom', '微妙', 45, 1, 20, 0, 1);

INSERT INTO event_option
(id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
(600101, 6001, '立刻重做，争取补回数据', '压力上升，但进度有机会补回来', 'medium', '你把实验步骤重新拆开，虽然手忙脚乱，还是补回了关键数据。老师看到你的记录后点了点头。', 2, 0, 0, 0, 2, 3, 2, 28),
(600102, 6001, '找队友合作，分开重做', '社交和效率更稳，压力略升', 'low', '队友接过一部分重复操作，你负责整理异常记录。两个人都累，但进度明显回来了。', 1, 0, 0, 3, 2, 1, 1, 26),
(600103, 6001, '向老师说明情况，请求延期', '压力下降，但自律评价受损', 'high', '老师同意你补交一版说明，但提醒你以后实验记录必须实时保存。你松了口气，也记住了这个教训。', -1, 0, 0, 0, 1, -3, -1, 16),

(600201, 6002, '立刻收拾出发', '实践分稳了，但午饭可能泡汤', 'medium', '你赶到 2 号楼时刚好点名。实训做得不算优雅，但老师记住了你的准时。', 2, -1, 0, 0, 1, 1, 2, 26),
(600202, 6002, '叫室友一起去，路上买饭', '状态更好，也能拉近关系', 'low', '你和室友边走边啃面包，到场后互相提醒流程，实训完成得比预想顺。', 1, 1, -10, 2, 1, 0, 1, 24),
(600203, 6002, '找借口请假留在宿舍', '短期轻松，长期有代价', 'high', '你确实休息了一下午，但群里开始发实训照片和签到名单，压力又慢慢爬了回来。', -2, 1, 0, 0, 0, -1, -2, 10),

(600301, 6003, '认真听讲并提问', '学业和社交都有收益', 'low', '你的问题被主讲人认真回应，旁边同学也主动和你交换了联系方式。', 3, 0, 0, 2, 1, -1, 1, 30),
(600302, 6003, '边听边摸鱼，混个签到', '收益有限，但消耗较低', 'medium', '你拿到了签到，却只记住了几个关键词。回去后发现讲座资料其实挺有用。', 0, 0, 0, 0, 0, -1, -1, 12),
(600303, 6003, '回宿舍休息', '压力下降，错过信息', 'medium', '你睡了一觉，精神恢复不少。第二天听同学聊讲座彩蛋时，还是有一点后悔。', -1, 2, 0, 0, 0, -3, -1, 10),

(600401, 6004, '飞奔去营业厅解决', '学业保住，体力和钱包受损', 'medium', '你赶在关门前处理好账号问题，作业顺利提交，只是一路小跑让你气喘得厉害。', 2, -2, -30, 0, 1, -1, 1, 26),
(600402, 6004, '先用手机热点顶上', '能交作业，但压力上升', 'medium', '热点勉强撑住了提交，流量提醒也准时弹了出来。你决定明天再去营业厅。', 1, 0, -20, 0, 0, 2, 0, 18),
(600403, 6004, '去隔壁宿舍蹭网', '靠社交救场', 'low', '隔壁同学把密码发给你，还顺手帮你看了一眼格式。你们关系熟了一点。', 1, 0, 0, 3, 0, -1, 0, 22),

(600501, 6005, '自己尝试一周', '健康和自律提升，花费更高', 'medium', '轻食没有想象中难吃，你连续打卡后感觉下午犯困少了一些。', 0, 3, -80, 0, 0, -1, 2, 24),
(600502, 6005, '拉朋友组队打卡', '社交更强，还有团购价', 'low', '几个人互相监督，偶尔吐槽菜叶子，但确实比单打独斗容易坚持。', 0, 2, -60, 3, 0, -1, 1, 26),
(600503, 6005, '继续吃重油盖饭', '省钱又解压，健康小亏', 'medium', '盖饭很香，压力暂时消失。只是下午上课时，你又开始困了。', 0, -1, -25, 0, 0, -2, -1, 12),

(600601, 6006, '主动聊天，互相加油', '社交和复习资料都有收获', 'low', '你们交换了复习重点，对方还提醒你一个常考概念。深夜自习室没那么孤独了。', 1, 0, 0, 3, 0, -2, 0, 28),
(600602, 6006, '保持沉默，各学各的', '自律稳定，没有额外波动', 'low', '你重新低头复习，耳边只剩翻书声和笔尖划过纸面的声音。', 1, 0, 0, 0, 0, 0, 2, 18),
(600603, 6006, '换个位置继续学', '减少干扰，但略显生硬', 'medium', '你换到角落位置，效率回来了，但对方似乎有点尴尬。', 1, 0, 0, -1, 0, 1, 1, 14),

(600701, 6007, '围观并报名一个社团', '社交上升，作业被挤压', 'medium', '你被摊位氛围感染，当场填了报名表。回宿舍时才想起作业还没动。', -1, 0, 0, 3, 0, -2, -1, 24),
(600702, 6007, '只看不报名，拍几个视频', '轻量社交，压力下降', 'low', '你拍下热闹的摊位，顺手转给室友。虽然没报名，心情明显轻松了。', 0, 0, 0, 1, 0, -1, 0, 16),
(600703, 6007, '直接回去写作业', '自律和学业稳定，错过热闹', 'low', '你穿过人群回到教室，把作业推进了一大段。广场的歌声还隐约传来。', 1, 0, 0, -1, 0, 0, 3, 20),

(600801, 6008, '把书挪到旁边，自己坐下', '学业小升，社交风险高', 'high', '你坐下后原主人赶来，气氛一度凝固。课是听完了，但心情不太好。', 1, 0, 0, -2, 0, 2, 1, 14),
(600802, 6008, '坐到旁边空位', '稳妥但没有额外收益', 'low', '你避开冲突，虽然位置不算完美，整节课还算安稳。', 0, 0, 0, 0, 0, 0, 0, 12),
(600803, 6008, '写纸条礼貌沟通', '可能交到新朋友', 'medium', '你留下纸条后坐到旁边。下课时对方来道谢，还解释自己只是去打印资料。', 0, 0, 0, 2, 0, -1, 1, 20);

INSERT INTO rumor
(id, week_number, location_id, rumor_title, rumor_text, effect_hint, effect_type, effect_value, effect_target, rarity, active) VALUES
(6001, 1, 4, '食堂三楼新开减脂轻食窗口', '听说知味餐厅三楼低油轻食窗口排队不长，组队打卡还能便宜一点。', '食堂健康收益提高', 'attr_bonus', 2, 'health', 'common', 1),
(6002, 1, 5, '操场合唱社团露天表演', '周三晚操场边可能有合唱团露天排练，路过的人还能领水。', '操场压力下降更明显', 'attr_bonus', 3, 'pressure', 'rare', 1),
(6003, 2, 1, '教学楼课堂临时点名风声', '有人说博学楼和博识楼这两天课堂提问变多，老师喜欢临时抽人复盘上节课。', '教学楼事件更偏向学业危机', 'event_hint', 1, 'academic', 'common', 1),
(6004, 2, 2, '图书馆八楼 TP 区空位多', '八楼 TP 计算机书架附近今天人不多，适合带电脑查资料。', '图书馆探索收益提高', 'explore_bonus', 2, 'explore', 'common', 1),
(6005, 3, 7, '韶华楼前社团摊位送文创', '社团摊位临时加了文创小礼物，愿意聊天的人更容易被记住。', '社团区更容易遇见 NPC', 'npc_boost', 12, 'npc', 'rare', 1),
(6006, 3, 8, '快递站雨后错峰取件', '雨停后半小时去 31 号楼北侧驿站，排队可能刚好断档。', '快递站压力风险降低', 'safe_zone', 1, 'pressure', 'common', 1);

INSERT INTO dungeon
(id, dungeon_name, dungeon_type, description, cover_image, theme_style, estimated_minutes, difficulty_label, reward_exp, reward_title, status) VALUES
(6001, '小组作业', 'academic', '随机队友、线上会议、潜水成员、格式统一和卡点提交组成的大学常驻副本。你需要在有限时间内把一组人的混乱输出变成能交的成果。', 'scene-classroom', 'DDL', 12, '中等', 90, '小组协调员', 1);

INSERT INTO dungeon_task
(id, dungeon_id, task_name, task_type, task_order, scene_text, target_text, background_image, minigame_type, minigame_config, timer_seconds, settlement_rule, random_enabled, attribute_check_rule, pass_condition, required, status) VALUES
(600101, 6001, '公布小组名单', 'choice', 1, '任课老师公布小组名单，你被分到一个完全陌生的小组。群已经建好，但头像亮着的人不多。', '先稳住开局，让小组愿意动起来。', 'scene-classroom', 'none', NULL, NULL, '根据选择获得开局分数和属性变化。', 0, NULL, 'score >= 40', 1, 1),
(600102, 6001, '线上开会分工', 'choice', 2, '第一次线上会议开始，大家对选题都没有强烈意见，但都希望自己少做一点。', '确定题目、分工和时间节点。', 'scene-dorm', 'none', NULL, NULL, '分工越清晰，后续压力越低。', 0, NULL, 'score >= 40', 1, 1),
(600103, 6001, '各自开荒', 'choice', 3, '资料查找和内容撰写阶段到了。有人认真推进，也有人头像安静得像背景板。', '让每个模块至少有东西能交。', 'scene-library', 'none', NULL, NULL, '处理潜水队友和资料质量。', 1, 'skill>=45', 'score >= 50', 1, 1),
(600104, 6001, '整合攻坚', 'choice', 4, '临近 DDL，所有人的内容终于交上来：字体、引用、图表风格各有各的想法。', '统一格式，修掉明显问题。', 'scene-lab', 'none', NULL, NULL, '整合策略影响最终评分和压力。', 0, 'discipline>=45', 'score >= 50', 1, 1),
(600105, 6001, '卡点提交', 'choice', 5, '提交入口即将关闭，PPT 和报告都在最后一轮导出。群里终于热闹了起来。', '在截止前交出能看的版本。', 'scene-classroom', 'none', NULL, 90, '最终选择决定评价标签。', 0, NULL, 'score >= 60', 1, 1);

INSERT INTO dungeon_task_option
(id, dungeon_task_id, option_type, option_text, is_correct, trigger_probability, result_text, evaluation, score, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_task_id, status) VALUES
(60010101, 600101, 'strategy', '主动打招呼并整理需求', 1, 100, '你把作业要求、截止时间和评分标准贴进群里，沉默的队友也开始回应。', '开局稳定', 75, 1, 0, 0, 2, 1, -1, 1, 15, 600102, 1),
(60010102, 600101, 'strategy', '先观察，不急着发言', 0, 100, '群安静了一整晚，你意识到没人开头就真的没人开头。', '节奏偏慢', 45, 0, 0, 0, 0, 0, 1, 0, 8, 600102, 1),
(60010103, 600101, 'strategy', '直接表示自己只做一小块', 0, 100, '你抢先划定边界，但其他人也学会了这招，开局变成互相试探。', '气氛微妙', 35, 0, 0, 0, -1, 0, 2, -1, 6, 600102, 1),

(60010201, 600102, 'strategy', '按模块拆分并写清交付标准', 1, 100, '你把分工表写清楚，每个人都知道自己要交什么。后续返工风险下降。', '分工清晰', 80, 1, 0, 0, 2, 1, -1, 2, 16, 600103, 1),
(60010202, 600102, 'strategy', '让大家自由认领', 0, 100, '热门部分很快被认领，麻烦部分无人回应，会议拖得更久了。', '分工不均', 50, 0, 0, 0, 1, 0, 1, 0, 9, 600103, 1),
(60010203, 600102, 'strategy', '自己包下最难部分', 0, 100, '你拿到了核心模块，质量可控了，但压力也直接翻倍。', '个人负担重', 60, 1, -1, 0, 0, 2, 3, 1, 12, 600103, 1),

(60010301, 600103, 'strategy', '催进度并提供参考资料', 1, 100, '你把资料链接和格式样例发到群里，潜水队友终于交出第一版内容。', '推进有效', 75, 1, 0, 0, 2, 2, 0, 1, 16, 600104, 1),
(60010302, 600103, 'strategy', '默默补上缺口', 0, 100, '你把空缺部分补齐了，但越写越觉得这不是小组作业，是个人挑战赛。', '能交但累', 65, 1, -1, 0, 0, 2, 3, 1, 13, 600104, 1),
(60010303, 600103, 'strategy', '放任大家自由发挥', 0, 100, '每个人都交了一点东西，但主题开始向四个方向漂移。', '质量发散', 40, 0, 0, 0, 0, 0, 2, -1, 8, 600104, 1),

(60010401, 600104, 'strategy', '统一模板并逐段修订', 1, 100, '你把报告和 PPT 都套进统一模板，引用和图表终于像同一个小组做的。', '整合优秀', 85, 2, 0, 0, 1, 2, 1, 2, 18, 600105, 1),
(60010402, 600104, 'strategy', '只修最明显的问题', 0, 100, '你修掉了错别字和乱格式，大问题勉强压住，小问题只能随缘。', '基本能看', 60, 1, 0, 0, 0, 1, 0, 1, 12, 600105, 1),
(60010403, 600104, 'strategy', '临时要求队友返工', 0, 100, '队友们开始返工，但时间已经很紧，群里气氛越来越紧张。', '返工拉扯', 50, 1, 0, 0, -1, 1, 3, 0, 10, 600105, 1),

(60010501, 600105, 'strategy', '提前十分钟提交稳定版', 1, 100, '你们交上了稳定版本。虽然还想再润色，但至少没有卡在系统关闭前。', '稳定通关', 80, 2, 0, 0, 1, 1, -2, 2, 20, NULL, 1),
(60010502, 600105, 'strategy', '最后一分钟追求完美', 0, 100, '你们赶在最后几十秒提交，版本确实更好，但所有人都被吓出一身汗。', '极限提交', 70, 2, -1, 0, 1, 2, 3, 1, 18, NULL, 1),
(60010503, 600105, 'strategy', '让组长代交，自己下线', 0, 100, '你把文件丢给组长后下线。作业交上去了，但你对最终版本完全没底。', '侥幸过关', 45, 0, 1, 0, -1, 0, -1, -1, 10, NULL, 1);
```

- [ ] **Step 2: Keep CP6 seed independent**

Run:

```powershell
Select-String -Path src\main\resources\data-content-pack-6.sql -Pattern "ALTER TABLE|CREATE TABLE|DROP TABLE|DELETE FROM|TRUNCATE"
```

Expected:

```text
No output
```

## Task 4: Load CP6 Seed File

**Files:**
- Modify: `src/main/resources/application.yml`

- [ ] **Step 1: Add CP6 to SQL init list**

Change:

```yaml
      data-locations: classpath:data.sql,classpath:data-content-pack-2.sql,classpath:data-content-pack-3.sql,classpath:data-content-pack-4.sql
```

To:

```yaml
      data-locations: classpath:data.sql,classpath:data-content-pack-2.sql,classpath:data-content-pack-3.sql,classpath:data-content-pack-4.sql,classpath:data-content-pack-6.sql
```

- [ ] **Step 2: Run CP6 tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6Tests test
```

Expected:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

If the test fails because an entity method name differs, inspect the entity in `src/main/java/cn/haut/survivor/domain/entity/*.java` and update only the test method reference to match the actual entity field. Do not change schema for that.

## Task 5: Run Full Test Suite

**Files:**
- No source modifications expected in this task

- [ ] **Step 1: Run full clean test**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
```

Record:

- Total tests run
- Failures
- Errors
- Skipped

- [ ] **Step 2: Fix only CP6-caused failures**

If failures occur:

- If failure is a duplicate id, choose a free id in the `6001+` range and update SQL plus tests together.
- If failure is a changed count from seed data, update the relevant old test only when the old test was asserting an exact global count that CP6 legitimately increases.
- If failure is caused by a typo in SQL column names, fix `data-content-pack-6.sql`.
- If failure is unrelated to CP6, report it separately and do not hide it.

Run `.\mvnw.cmd clean test` again after each fix.

## Task 6: HTTP Smoke Test

**Files:**
- No source modifications expected unless smoke reveals a real problem

- [ ] **Step 1: Start the app**

Run in a dedicated terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected:

```text
Started HautSurvivorApplication
```

- [ ] **Step 2: Smoke existing player pages**

Use browser or HTTP client. Confirm status `200` and no Whitelabel page:

```text
GET  /dashboard
GET  /map
GET  /exploration
POST /exploration/4
GET  /week/summary
GET  /organizations
GET  /organizations/6001
GET  /dungeons
GET  /dungeons/6001
GET  /dungeons/6001/start
GET  /dungeons/6001/play
```

Expected:

```text
All checked pages return 200 or intentional redirect within the game flow.
No Whitelabel error page.
No missing template exception.
No SQL exception for CP6 ids.
```

- [ ] **Step 3: Exercise first dungeon choice**

Open `/dungeons/6001/start`, then `/dungeons/6001/play`.

Submit one option from first task:

```text
POST /dungeons/6001/task/600101/option/60010101
```

Expected:

```text
Response is a redirect or 200 according to existing dungeon flow.
The next play page should show stage 2 or a valid dungeon result state.
```

## Task 7: Browser Visual Check

**Files:**
- Modify templates/CSS only if a real regression appears

- [ ] **Step 1: Check desktop**

Viewport:

```text
1366x768
```

Pages:

```text
/dashboard
/map
/exploration
/organizations
/organizations/6001
/dungeons
/dungeons/6001
/dungeons/6001/play
/week/summary
```

Expected:

```text
No horizontal scroll.
CP6 organization/dungeon text fits existing cards.
Game dock does not overlap primary buttons.
Chinese text wraps cleanly.
```

- [ ] **Step 2: Check mobile**

Viewport:

```text
375x812
```

Pages:

```text
/dashboard
/map
/exploration
/organizations
/organizations/6001
/dungeons
/dungeons/6001
/dungeons/6001/play
/week/summary
```

Expected:

```text
No horizontal scroll.
Bottom dock does not cover final action buttons.
Dungeon option buttons remain tappable.
Organization detail actions remain above safe area.
```

- [ ] **Step 3: CSS rule if a fix is needed**

If the mobile dock covers CP6 dungeon or organization actions, patch only `src/main/resources/static/css/app.css` inside the existing mobile breakpoint. Prefer increasing bottom padding for the affected page container. Do not redesign the UI.

After any CSS/template change, rerun:

```powershell
.\mvnw.cmd clean test
```

Then repeat HTTP smoke and visual checks.

## Task 8: Documentation Update

**Files:**
- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
- Modify: `docs/NEXT_AI_HANDOFF.md`

- [ ] **Step 1: Update project completion status**

Add a concise CP6 section to `docs/PROJECT_COMPLETION_STATUS.md`:

```markdown
## CP6 莲花街校区真实校园内容包

状态：已完成

范围：
- 新增 `data-content-pack-6.sql`，使用 6001+ id 段。
- 新增 5 个莲花街校区组织。
- 新增 8 个校园生活事件，每个事件至少 3 个选项。
- 新增 6 条机制型传闻，复用 `attr_bonus`、`npc_boost`、`explore_bonus`、`safe_zone`、`event_hint`。
- 新增副本「小组作业」，包含 5 个阶段和每阶段至少 3 个选项。
- 未新增数据库表，未修改核心机制。

验证：
- `.\mvnw.cmd clean test`：记录实际测试数量和 BUILD SUCCESS。
- HTTP 冒烟：记录实际检查页面。
- 浏览器视觉检查：记录桌面与移动端结果。
```

Replace "记录实际..." with the actual verification numbers and results from this implementation run.

- [ ] **Step 2: Update next handoff**

Add a concise handoff section to `docs/NEXT_AI_HANDOFF.md`:

```markdown
## CP6 莲花街校区内容包交接

已完成：
- `src/main/resources/data-content-pack-6.sql`
- `src/test/java/cn/haut/survivor/service/ContentPack6Tests.java`
- `application.yml` 已加载 CP6 seed 文件。

设计边界：
- CP6 第一批只做 seed-heavy 内容包。
- 未扩展地点表结构。
- 未新增 NPC 原型，避免和当前 NPC 关系/故事线改动交叉。
- 信息学院学生会描述已做中性化处理，未直接使用原始负面表述。

建议下一步：
- CP6.1 再考虑 NPC 原型接入：富少、小鱼、阿杰、柳如烟。
- CP6.2 再考虑地点细分：图书馆楼层、惟学楼、博闻楼营业厅、韶华楼、知味/知雅餐厅。
- CP6.3 再考虑周主题「开学迎新周」的机制化。
```

## Task 9: Final Verification Report From Coding AI

**Files:**
- No source modifications expected

- [ ] **Step 1: Prepare final report in this format**

The coding AI must report back with this exact structure:

```markdown
# CP6 编码执行反馈

## 1. 修改文件
- `src/main/resources/data-content-pack-6.sql`
- `src/main/resources/application.yml`
- `src/test/java/cn/haut/survivor/service/ContentPack6Tests.java`
- `docs/PROJECT_COMPLETION_STATUS.md`
- `docs/NEXT_AI_HANDOFF.md`
- 其他实际修改文件：列出并说明原因

## 2. 新增表/字段
无。

## 3. 新增 Seed 数据
- 组织：实际数量与 id 范围
- 事件：实际数量与 id 范围
- 事件选项：实际数量与 id 范围
- 传闻：实际数量与 id 范围
- 副本：实际数量与 id 范围
- 副本阶段/选项：实际数量与 id 范围

## 4. 新增/修改测试
- `ContentPack6Tests`：列出测试方法名和覆盖点
- 是否修改旧测试：如果有，说明原因

## 5. Clean Test 结果
命令：
`.\mvnw.cmd clean test`

结果：
`Tests run: X, Failures: 0, Errors: 0, Skipped: X`
`BUILD SUCCESS`

## 6. HTTP 冒烟结果
逐项列出：
- `/dashboard`
- `/map`
- `/exploration`
- `POST /exploration/4`
- `/week/summary`
- `/organizations`
- `/organizations/6001`
- `/dungeons`
- `/dungeons/6001`
- `/dungeons/6001/start`
- `/dungeons/6001/play`
- `POST /dungeons/6001/task/600101/option/60010101`

## 7. 浏览器视觉检查
- 1366x768：结果
- 375x812：结果
- 是否发现横向滚动、Dock 遮挡、按钮不可点、文本溢出

## 8. 剩余风险
- 与当前 dirty worktree 的关系
- 文案/真实校园信息准确性风险
- CP6 未包含 NPC 和周主题机制的原因
```

## Self-Review

- Spec coverage: plan covers `docs/补充信息.docx` first-batch content: real campus events, organizations, rumors, and one dungeon.
- Scope control: plan intentionally defers large location expansion, NPC branch expansion, and new weekly theme mechanics.
- Schema fit: plan uses existing `organization`, `event`, `event_option`, `rumor`, `dungeon`, `dungeon_task`, and `dungeon_task_option` tables.
- Config fit: plan explicitly updates `application.yml` so CP6 SQL is loaded.
- Test fit: `ContentPack6Tests` verifies content counts, key names, sanitized organization copy, event option counts, rumor effect types, and dungeon stage/options.
- UI fit: plan does not touch UI by default and only allows small CSS/template fixes if smoke reveals real regressions.
