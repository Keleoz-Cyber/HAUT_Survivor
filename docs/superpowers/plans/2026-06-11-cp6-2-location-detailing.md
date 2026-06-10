# CP6.2 Location Detailing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 用现有地点系统补强莲花街校区的地点细节，让图书馆、惟学楼、博闻楼营业厅、韶华楼、知味/知雅餐厅在事件、传闻和探索奇遇里变得更具体。

**Architecture:** 这是 seed-heavy 内容包，不新增表、不新增地图子地点系统、不改热点坐标模型。复用 `campus_location` 的现有 location id，向 `event`、`event_option`、`rumor`、`exploration_story_chain`、`weekly_goal`、`achievement` 追加 CP6.2 数据，并用测试约束内容数量、ID 范围、效果类型和阶段链路。

**Tech Stack:** Spring Boot 3.3.5, Java 17, MyBatis-Plus, MySQL/H2-compatible SQL seed files, Thymeleaf, JUnit 5, AssertJ.

---

## Scope And Rules

- 只做 CP6.2 地点细分内容扩展。
- 不新增数据库表或字段。
- 不修改 `application.yml`，继续复用已加载的 `src/main/resources/data-content-pack-6.sql`。
- 不改 `MapController`、`map/index.html`、`app.css`，除非现有页面因为新增 seed 数据实际报错。
- 不移动或替换 `src/main/resources/static/images/lianhuajie-campus-map.jpeg`。
- 继续复用现有地点 ID：
  - `1` 教学楼/课堂
  - `2` 图书馆
  - `3` 宿舍
  - `4` 食堂
  - `5` 操场
  - `6` 实验室/惟学楼
  - `7` 社团活动区/韶华楼前
  - `8` 快递站/生活服务点；CP6.2 可把博闻楼营业厅作为同一生活服务点复用
- 新增 ID 范围：
  - 事件：`6201` 到 `6210`
  - 事件选项：`620101` 到 `621003`
  - 传闻：`6201` 到 `6206`
  - 探索奇遇链：`6201` 到 `6212`
  - 周目标：`6201` 到 `6202`
  - 成就：`6201` 到 `6202`
- 传闻 `effect_type` 只能使用现有类型：`attr_bonus`、`npc_boost`、`explore_bonus`、`safe_zone`、`event_hint`。
- 传闻 `effect_target` 只使用现有目标：`academic`、`health`、`money`、`social`、`skill`、`pressure`、`discipline`、`explore`、`npc`。
- 周目标 `goal_type` 只使用现有类型：`explore_count`、`rumor_effect_used`、`exploration_story_step`、`weekly_modifier_used`、`buddy_assist`、`pressure_keep` 等已在 `WeeklyGoalServiceTests` 覆盖过的类型。
- 成就 `condition_type` 只使用现有类型：本计划使用 `exploration_story_step` 和 `rumor_effect_used`。

## File Map

- Modify `src/main/resources/data-content-pack-6.sql`
  追加 CP6.2 地点细分事件、选项、传闻、探索奇遇链、周目标、成就。
- Create `src/test/java/cn/haut/survivor/service/ContentPack6LocationDetailTests.java`
  验证 CP6.2 seed 加载、ID 范围、地点覆盖、选项数量、传闻效果类型、奇遇链步骤、周目标和成就类型。
- Modify `docs/PROJECT_COMPLETION_STATUS.md`
  编码完成并验证通过后，记录 CP6.2 完成状态。
- Modify `docs/NEXT_AI_HANDOFF.md`
  更新交接信息，把 CP6.2 从计划变成已完成，并写入下一步建议。
- Optional modify `docs/AI_CONTINUATION_PROMPT.md`
  只有当它还把 CP6.2 写成下一步待做时才更新。

## Task 1: Add Failing CP6.2 Seed Tests

**Files:**
- Create: `src/test/java/cn/haut/survivor/service/ContentPack6LocationDetailTests.java`

- [ ] **Step 1: Create the failing test class**

Create `src/test/java/cn/haut/survivor/service/ContentPack6LocationDetailTests.java` with this complete content:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.EventOption;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.EventOptionMapper;
import cn.haut.survivor.mapper.ExplorationStoryChainMapper;
import cn.haut.survivor.mapper.RumorMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack6LocationDetailTests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventOptionMapper eventOptionMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private ExplorationStoryChainMapper storyChainMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void cp62LocationEventsAreSeededWithThreeOptionsEach() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .ge(Event::getId, 6201L)
                .le(Event::getId, 6210L)
                .orderByAsc(Event::getId));

        assertThat(events).hasSize(10);
        assertThat(events).extracting(Event::getEventName)
                .containsExactly(
                        "图书馆电梯口排队",
                        "八楼 TP 区空位",
                        "惟学楼实验课临时换机房",
                        "惟学楼报告厅临时讲座",
                        "博闻楼营业厅补卡",
                        "博闻楼网络账号申诉",
                        "韶华楼前社团摊位",
                        "韶华楼走廊展示板",
                        "知味餐厅饭点错峰",
                        "知雅餐厅拼桌");
        assertThat(events).extracting(Event::getLocationId)
                .contains(2L, 6L, 8L, 7L, 4L);
        assertThat(events).allMatch(event -> event.getProbability() >= 25 && event.getProbability() <= 75);
        assertThat(events).allMatch(event -> event.getMinWeek() >= 1 && event.getMaxWeek() <= 20);
        assertThat(events).allMatch(event -> event.getStatus() == 1);

        List<EventOption> options = eventOptionMapper.selectList(new LambdaQueryWrapper<EventOption>()
                .ge(EventOption::getEventId, 6201L)
                .le(EventOption::getEventId, 6210L));

        Map<Long, Long> optionCountByEvent = options.stream()
                .collect(Collectors.groupingBy(EventOption::getEventId, Collectors.counting()));
        assertThat(optionCountByEvent).hasSize(10);
        assertThat(optionCountByEvent.values()).allMatch(count -> count >= 3);
        assertThat(options).allMatch(option -> option.getPreviewText() != null && !option.getPreviewText().isBlank());
        assertThat(options).allMatch(option -> Set.of("low", "medium", "high").contains(option.getRiskLevel()));
        assertThat(options).anyMatch(option -> option.getAcademicChange() > 0);
        assertThat(options).anyMatch(option -> option.getHealthChange() > 0);
        assertThat(options).anyMatch(option -> option.getSocialChange() > 0);
        assertThat(options).anyMatch(option -> option.getSkillChange() > 0);
        assertThat(options).anyMatch(option -> option.getPressureChange() < 0);
    }

    @Test
    void cp62RumorsUseExistingEffectTypesAndTargets() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 6201L)
                .le(Rumor::getId, 6206L)
                .orderByAsc(Rumor::getId));

        assertThat(rumors).hasSize(6);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .contains(
                        "图书馆八楼上午空位多",
                        "惟学楼报告厅有临时讲座",
                        "博闻楼营业厅午后人少",
                        "知味餐厅二楼错峰更稳");
        assertThat(rumors).extracting(Rumor::getEffectType)
                .contains("explore_bonus", "event_hint", "safe_zone", "npc_boost", "attr_bonus");
        assertThat(rumors).allMatch(rumor -> Set.of(
                "attr_bonus", "npc_boost", "explore_bonus", "safe_zone", "event_hint"
        ).contains(rumor.getEffectType()));
        assertThat(rumors).allMatch(rumor -> Set.of(
                "academic", "health", "money", "social", "skill", "pressure", "discipline", "explore", "npc"
        ).contains(rumor.getEffectTarget()));
        assertThat(rumors).allMatch(rumor -> rumor.getEffectValue() != null && rumor.getEffectValue() > 0);
        assertThat(rumors).allMatch(rumor -> rumor.getActive() == 1);
    }

    @Test
    void cp62ExplorationStoryChainsHaveOrderedStepsAndValidNextSteps() {
        List<ExplorationStoryChain> chains = storyChainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .ge(ExplorationStoryChain::getId, 6201L)
                .le(ExplorationStoryChain::getId, 6212L)
                .orderByAsc(ExplorationStoryChain::getId));

        assertThat(chains).hasSize(12);
        assertThat(chains).extracting(ExplorationStoryChain::getChainKey)
                .contains(
                        "library_floor_trace",
                        "weixue_lab_route",
                        "bowen_service_window",
                        "canteen_peak_shift");
        assertThat(chains).allMatch(chain -> chain.getActive() == 1);
        assertThat(chains).allMatch(chain -> chain.getLocationId() >= 1L && chain.getLocationId() <= 8L);
        assertThat(chains).allMatch(chain -> chain.getRequiredExploreLevel() >= 0);

        Map<String, List<ExplorationStoryChain>> byChainKey = chains.stream()
                .collect(Collectors.groupingBy(ExplorationStoryChain::getChainKey));
        assertThat(byChainKey).hasSize(4);
        byChainKey.values().forEach(steps -> {
            List<Integer> stepNumbers = steps.stream()
                    .map(ExplorationStoryChain::getStepNumber)
                    .sorted()
                    .toList();
            assertThat(stepNumbers).containsExactly(1, 2, 3);
            assertThat(steps).anyMatch(step -> Integer.valueOf(2).equals(step.getNextStepNumber()));
            assertThat(steps).anyMatch(step -> Integer.valueOf(3).equals(step.getNextStepNumber()));
            assertThat(steps).anyMatch(step -> step.getNextStepNumber() == null);
        });

        assertThat(chains).anyMatch(chain -> chain.getAcademicChange() > 0);
        assertThat(chains).anyMatch(chain -> chain.getSkillChange() > 0);
        assertThat(chains).anyMatch(chain -> chain.getSocialChange() > 0);
        assertThat(chains).anyMatch(chain -> chain.getPressureChange() < 0);
    }

    @Test
    void cp62WeeklyGoalsAndAchievementsUseExistingConditionTypes() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "cp62_location_story_chaser", "cp62_rumor_route_planner")
                .orderByAsc(WeeklyGoal::getId));

        assertThat(goals).hasSize(2);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .containsExactly("exploration_story_step", "rumor_effect_used");
        assertThat(goals).allMatch(goal -> goal.getTargetValue() != null && goal.getTargetValue() > 0);
        assertThat(goals).allMatch(goal -> goal.getRewardExp() != null && goal.getRewardExp() > 0);
        assertThat(goals).allMatch(goal -> goal.getActive() == 1);

        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey, "cp62_location_mapper", "cp62_rumor_commuter")
                .orderByAsc(Achievement::getId));

        assertThat(achievements).hasSize(2);
        assertThat(achievements).extracting(Achievement::getConditionType)
                .containsExactly("exploration_story_step", "rumor_effect_used");
        assertThat(achievements).allMatch(achievement -> achievement.getConditionValue() != null
                && achievement.getConditionValue() > 0);
        assertThat(achievements).allMatch(achievement -> achievement.getRewardTitle() != null
                && !achievement.getRewardTitle().isBlank());
        assertThat(achievements).allMatch(achievement -> achievement.getActive() == 1);
    }
}
```

- [ ] **Step 2: Run the focused failing test**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6LocationDetailTests test
```

Expected: test class compiles, then fails because CP6.2 seed rows do not exist yet. The failure should be an assertion such as expected `10` events but found `0`.

## Task 2: Append CP6.2 Seed Data

**Files:**
- Modify: `src/main/resources/data-content-pack-6.sql`

- [ ] **Step 1: Append this CP6.2 SQL section**

Append the following SQL to the end of `src/main/resources/data-content-pack-6.sql`. Keep the file UTF-8 encoded.

```sql
-- ============================================================
-- Content Pack 6.2: 莲花街校区地点细分
-- ============================================================

INSERT INTO `event`
(id, event_name, event_type, location_id, description, scene_image, mood_tag, probability, min_week, max_week, min_explore_level, status) VALUES
(6201, '图书馆电梯口排队', '学习', 2, '图书馆电梯口排起长队，大家都想赶在空位被占完前上楼。楼梯就在旁边，但背包里还塞着电脑和教材。', 'scene-library', '抢位', 55, 1, 20, 0, 1),
(6202, '八楼 TP 区空位', '学习', 2, '八楼 TP 区今天少见地空出一排座位，插座也还能用。旁边同学正在安静查资料，气氛适合推进困难任务。', 'scene-library', '专注', 40, 1, 20, 10, 1),
(6203, '惟学楼实验课临时换机房', '学习', 6, '惟学楼实验课临时换到另一间机房，群消息刷得很快，还有同学没看到通知。老师已经开始点名。', 'scene-lab', '临时通知', 50, 1, 18, 0, 1),
(6204, '惟学楼报告厅临时讲座', '学习', 6, '惟学楼报告厅临时开放一场技术讲座，主题和课程项目有点关系，但你原本打算回宿舍补觉。', 'scene-campus', '讲座', 35, 2, 18, 5, 1),
(6205, '博闻楼营业厅补卡', '生活', 8, '校园卡刷不出门禁，博闻楼营业厅窗口还有二十分钟下班。排队的人不多，但每个人的问题都不一样。', 'scene-campus', '事务', 45, 1, 20, 0, 1),
(6206, '博闻楼网络账号申诉', '生活', 8, '校园网账号被限速，营业厅工作人员让你补一份说明。旁边的同学说可以先按模板写，别在窗口前硬想。', 'scene-dorm', '网络', 45, 1, 20, 0, 1),
(6207, '韶华楼前社团摊位', '组织', 7, '韶华楼前的摊位突然热闹起来，有社团招新、有活动报名，也有人只是来蹭一张宣传贴纸。', 'scene-campus', '社团', 60, 1, 6, 0, 1),
(6208, '韶华楼走廊展示板', '社交', 7, '韶华楼走廊展示板贴满了近期活动照片，你在角落看到一个和专业竞赛相关的小公告。', 'scene-campus', '公告', 35, 1, 16, 5, 1),
(6209, '知味餐厅饭点错峰', '健康', 4, '知味餐厅饭点人流刚散，几个窗口还没收摊。你可以快速吃完，也可以多花一点时间挑个状态更稳的选择。', 'scene-canteen', '错峰', 55, 1, 20, 0, 1),
(6210, '知雅餐厅拼桌', '社交', 4, '知雅餐厅只剩拼桌位，对面同学正在整理课堂笔记。你们的餐盘同时落桌，气氛有一点尴尬。', 'scene-canteen', '拼桌', 40, 1, 20, 0, 1);

INSERT INTO event_option
(id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
(620101, 6201, '背包抱稳，直接爬楼梯', '消耗体力，抢位成功率高', 'medium', '你喘着气爬上楼，刚好赶在靠窗位置被拿走前坐下。腿有点酸，但学习节奏保住了。', 2, -1, 0, 0, 0, 1, 2, 22),
(620102, 6201, '继续排队，顺手整理待办', '稳妥等待，纪律小升', 'low', '队伍移动得很慢，你把今天要做的事拆成三项。等到电梯时，焦虑反而降了一点。', 1, 0, 0, 0, 0, -1, 2, 18),
(620103, 6201, '放弃上楼，去低层找角落', '收益较低但省力', 'low', '低层座位吵了一些，但你很快坐下开始处理简单任务。效率一般，至少没有被排队消磨完。', 1, 1, 0, 0, 0, -1, 0, 14),
(620201, 6202, '立刻占位开始深度学习', '学业收益高，压力略升', 'medium', '你把电脑接上电源，强迫自己先啃最难的一节内容。过程不轻松，但进度明显推进。', 5, 0, 0, 0, 1, 2, 3, 30),
(620202, 6202, '邀请同学一起查资料', '学业和社交均衡', 'low', '你发消息叫来同学分头查资料，最后合成了一份比单人更完整的提纲。', 3, 0, 0, 2, 1, 0, 1, 28),
(620203, 6202, '只处理轻任务，保留精力', '压力下降，进度有限', 'low', '你整理了引用和文件夹，没有碰最难的部分。离目标还有距离，但脑子清爽了一些。', 1, 1, 0, 0, 0, -2, 1, 16),
(620301, 6203, '马上转发定位并赶过去', '纪律和社交收益稳定', 'low', '你把新机房位置转到群里，顺手提醒了两个还在原教室的同学。老师点名时你们刚好赶上。', 2, 0, 0, 2, 1, 0, 2, 28),
(620302, 6203, '先确认设备环境再入座', '技能收益高，时间紧', 'medium', '你快速检查电脑环境，发现缺少插件并提前解决。虽然点名时有点狼狈，实验后半段顺了很多。', 2, 0, 0, 0, 4, 2, 1, 30),
(620303, 6203, '跟着人流走，不再确认', '省心但有走错风险', 'high', '你跟着人群走到隔壁机房，结果发现是别的班。折返后赶上了课，但开局已经乱了。', -1, 0, 0, 0, 0, 3, -1, 10),
(620401, 6204, '认真听讲并记下项目灵感', '技能和学业提升', 'low', '讲座里有一段案例正好能用在课程项目里，你记下框架，回去后能少走一截弯路。', 3, 0, 0, 0, 3, 0, 1, 30),
(620402, 6204, '只听前半场，保留休息时间', '收益中等，压力下降', 'low', '你听完最核心的部分就离开，回宿舍补了半小时觉。信息拿到了，状态也没崩。', 1, 2, 0, 0, 1, -2, 0, 20),
(620403, 6204, '帮同学占座并交流信息', '社交收益高，节奏变慢', 'medium', '你帮同学占了座，顺便交换了课程项目消息。讲座内容没听全，但关系网多了一条线。', 1, 0, 0, 3, 1, 1, 0, 24),
(620501, 6205, '提前整理材料再排队', '事务稳妥，纪律提升', 'low', '你先把证件和截图准备好，窗口办理时几乎没有返工。工作人员甚至提醒你顺便改了绑定信息。', 0, 0, -20, 0, 1, -1, 2, 22),
(620502, 6205, '直接冲到窗口说明情况', '快速但容易返工', 'medium', '你抢在下班前到了窗口，但材料缺一项，只能现场补截图。事情办完了，心跳也上来了。', 0, -1, -20, 0, 0, 2, 0, 16),
(620503, 6205, '请同学先帮忙刷门禁', '社交救场，问题延后', 'medium', '同学先帮你进了楼，你把补卡推到明天。今天省下了时间，但待办列表又多了一条。', 0, 0, 0, 2, 0, 1, -1, 16),
(620601, 6206, '按模板写申诉说明', '技能小升，解决概率高', 'low', '你照着模板把问题描述清楚，工作人员很快定位到账号状态。网络恢复后，作业上传顺了。', 2, 0, 0, 0, 2, -1, 1, 26),
(620602, 6206, '询问排队同学的处理经验', '社交换效率', 'low', '旁边同学提醒你先截图错误页面，少跑了一趟。你们顺便互加了联系方式。', 1, 0, 0, 3, 1, -1, 0, 24),
(620603, 6206, '先用热点撑过今晚', '短期省事，金钱受损', 'medium', '热点勉强撑住了提交，但流量提醒不断弹出。问题没有消失，只是被你挪到了明天。', 1, 0, -15, 0, 0, 1, -1, 12),
(620701, 6207, '认真逛摊并问清活动节奏', '社交收益高', 'low', '你问清了活动频率和报名要求，发现其中一个社团并没有想象中那么占时间。', 0, 0, 0, 4, 0, -1, 1, 26),
(620702, 6207, '只拿资料，不立刻报名', '信息增加，压力较低', 'low', '你把几张传单夹进书里，决定晚上再比较。摊位很热闹，但你没有被气氛推着走。', 0, 0, 0, 1, 0, -1, 1, 16),
(620703, 6207, '现场报名一个感兴趣的组织', '机会增加，时间压力上升', 'medium', '你填了报名表，也收到第一次活动通知。新机会很诱人，但日程表明显更满了。', 0, 0, 0, 3, 1, 2, 0, 24),
(620801, 6208, '拍下竞赛公告并查资料', '技能和学业收益', 'low', '公告上的比赛方向和专业课有关，你顺手查了往届作品，突然有了一个可做的小点子。', 2, 0, 0, 0, 3, 0, 1, 28),
(620802, 6208, '转给可能感兴趣的同学', '社交收益稳定', 'low', '你把公告发给同学，对方很快回了一个组队邀请。事情还没定，但连接已经建立。', 1, 0, 0, 3, 1, 0, 0, 24),
(620803, 6208, '路过看看，不增加计划', '压力下降，收益较少', 'low', '你看完展示板就离开，没有再给本周塞新任务。错过一点机会，但脑袋轻了一些。', 0, 1, 0, 0, 0, -2, 1, 14),
(620901, 6209, '错峰吃一顿正餐', '健康恢复稳定', 'low', '你没有排太久队，也终于吃上热饭。下午的困意没有完全消失，但身体明显稳了一点。', 0, 4, -18, 0, 0, -2, 1, 24),
(620902, 6209, '顺手给室友带饭', '社交提升，金钱流出', 'low', '室友收到饭时像看见救命消息，你们顺便互相同步了今天的课程情报。', 0, 2, -35, 3, 0, -1, 0, 24),
(620903, 6209, '随便买点继续赶路', '省时间但健康收益低', 'medium', '你买了最快的套餐边走边吃，时间省下来了，胃却没有完全同意这个方案。', 1, -1, -12, 0, 0, 1, 0, 12),
(621001, 6210, '主动寒暄，聊聊课堂笔记', '社交和学业均衡', 'low', '对方刚好也在整理同一门课，你们交换了笔记重点。拼桌的尴尬变成了意外收获。', 2, 0, 0, 3, 0, -1, 0, 28),
(621002, 6210, '安静吃饭，保持边界', '稳定恢复', 'low', '你们互相点头后各自吃饭。没有新关系，也没有额外消耗，一顿饭安稳结束。', 0, 2, -15, 0, 0, -1, 1, 16),
(621003, 6210, '边吃边继续刷任务', '进度推进但恢复不足', 'medium', '你在餐盘旁打开作业文档，效率不算低，但休息时间被切得很碎。', 2, -1, -15, 0, 1, 2, 0, 16);

INSERT INTO rumor
(id, week_number, location_id, rumor_title, rumor_text, effect_hint, effect_type, effect_value, effect_target, rarity, active) VALUES
(6201, 1, 2, '图书馆八楼上午空位多', '有人说上午第二节课前，图书馆八楼 TP 区经常还能找到带插座的位置。', '图书馆探索收益提高', 'explore_bonus', 2, 'explore', 'common', 1),
(6202, 1, 6, '惟学楼报告厅有临时讲座', '惟学楼报告厅这周可能有技术讲座，听说内容和课程项目能搭上边。', '惟学楼事件更偏向技能', 'event_hint', 1, 'skill', 'common', 1),
(6203, 2, 8, '博闻楼营业厅午后人少', '午饭后去博闻楼营业厅，窗口排队可能比临下班时稳定得多。', '生活服务点压力风险降低', 'safe_zone', 1, 'pressure', 'common', 1),
(6204, 2, 7, '韶华楼前摊位容易遇到熟人', '韶华楼前社团摊位换班时，很多学长学姐会顺手聊两句。', '社团区 NPC 遇见概率提高', 'npc_boost', 10, 'npc', 'rare', 1),
(6205, 3, 4, '知味餐厅二楼错峰更稳', '知味餐厅二楼错峰时窗口选择更多，不用在队伍里消耗太久。', '食堂健康收益提高', 'attr_bonus', 2, 'health', 'common', 1),
(6206, 3, 1, '教学楼早八前排更容易被提问', '这几天老师好像更喜欢点前排同学复盘上节课内容。', '教学楼事件更偏向学业', 'event_hint', 1, 'academic', 'common', 1);

INSERT INTO exploration_story_chain
(id, chain_key, chain_name, location_id, week_number, required_explore_level, step_number, scenario_text, result_text,
 academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_step_number, active) VALUES
(6201, 'library_floor_trace', '图书馆楼层路线', 2, 1, 0, 1, '你第一次认真观察图书馆各楼层的人流，发现电梯、楼梯和空位之间其实有一套隐形路线。', '你记下了几个相对稳定的自习点，之后来图书馆不再完全靠运气。', 1, 0, 0, 0, 1, -1, 1, 18, 2, 1),
(6202, 'library_floor_trace', '图书馆楼层路线', 2, 1, 10, 2, '你带着电脑去了八楼 TP 区，发现插座、书架和安静程度刚好适合查资料。', '你把资料检索流程顺了一遍，复杂任务被拆得清楚了一些。', 3, 0, 0, 0, 2, 0, 1, 24, 3, 1),
(6203, 'library_floor_trace', '图书馆楼层路线', 2, 1, 20, 3, '你已经能根据时间判断去哪层更稳，甚至顺手提醒了一个找座位的新生。', '图书馆从随机碰运气的地方，变成了你能主动规划的学习据点。', 4, 0, 0, 2, 1, -2, 2, 34, NULL, 1),
(6204, 'weixue_lab_route', '惟学楼机房路线', 6, 1, 0, 1, '你在惟学楼第一次因为换机房差点迟到，楼层指示牌和群消息看得人头大。', '你把常用机房位置记进备忘录，下次至少不会在楼里绕圈。', 1, 0, 0, 0, 2, -1, 1, 18, 2, 1),
(6205, 'weixue_lab_route', '惟学楼机房路线', 6, 1, 10, 2, '你提前到机房检查环境，发现软件版本和老师演示用的不太一样。', '你提前处理了插件问题，还把解决方法发到小群里。', 2, 0, 0, 1, 3, 0, 1, 26, 3, 1),
(6206, 'weixue_lab_route', '惟学楼机房路线', 6, 1, 20, 3, '你已经熟悉惟学楼几间常用机房，临时通知再出现时不再慌张。', '实验课节奏稳定下来，你甚至能把多余精力用来理解原理。', 3, 0, 0, 1, 4, -1, 2, 36, NULL, 1),
(6207, 'bowen_service_window', '博闻楼事务窗口', 8, 1, 0, 1, '你第一次去博闻楼营业厅办事，被窗口、表格和下班时间压得有点懵。', '你知道了先准备截图和证件，事务处理不再完全靠临场反应。', 0, 0, 0, 0, 1, -1, 2, 18, 2, 1),
(6208, 'bowen_service_window', '博闻楼事务窗口', 8, 1, 10, 2, '你带着完整材料再去窗口，发现很多问题其实能在排队前先自查。', '你少跑了一趟，也帮后面的同学提醒了材料清单。', 0, 0, 5, 2, 1, -2, 2, 26, 3, 1),
(6209, 'bowen_service_window', '博闻楼事务窗口', 8, 1, 20, 3, '你开始把校园卡、网络和证明办理归进同一个生活事务清单。', '杂事没有消失，但你已经知道什么时候办、带什么、找谁问。', 0, 1, 8, 1, 1, -3, 3, 34, NULL, 1),
(6210, 'canteen_peak_shift', '食堂错峰路线', 4, 1, 0, 1, '你在饭点挤进知味餐厅，意识到排队本身正在吞掉你的休息时间。', '你记住了几个错峰窗口，至少不用每顿饭都像打仗。', 0, 2, 0, 0, 0, -1, 1, 18, 2, 1),
(6211, 'canteen_peak_shift', '食堂错峰路线', 4, 1, 10, 2, '你尝试在知味和知雅之间切换，发现不同楼层的人流节奏差异很大。', '吃饭变成了能恢复状态的安排，而不是单纯填饱肚子。', 0, 3, -5, 1, 0, -2, 1, 24, 3, 1),
(6212, 'canteen_peak_shift', '食堂错峰路线', 4, 1, 20, 3, '你带室友一起按错峰路线吃饭，顺便交换了课程、社团和作业信息。', '食堂成了低成本的信息交换点，生活节奏也更稳。', 1, 3, -8, 3, 0, -2, 2, 34, NULL, 1);

INSERT INTO weekly_goal
(id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, active) VALUES
(6201, 'cp62_location_story_chaser', '地点路线追踪者', '推进 2 次莲花街地点细分奇遇，逐渐摸清校区生活路线。', 'exploration_story_step', 2, 45, 'discipline', 2, 1),
(6202, 'cp62_rumor_route_planner', '听传闻走捷径', '本周使用 1 次传闻效果，把校园消息转化成实际行动。', 'rumor_effect_used', 1, 35, 'social', 1, 1);

INSERT INTO achievement
(id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, active) VALUES
(6201, 'cp62_location_mapper', '校区路线熟了点', '累计推进 3 次地点细分奇遇，开始知道什么时候该去哪里。', '🗺️', 'exploration_story_step', 3, '路线感新生', 1),
(6202, 'cp62_rumor_commuter', '消息灵通的赶路人', '累计使用 2 次传闻效果，把传闻变成节省时间的小优势。', '📌', 'rumor_effect_used', 2, '消息路标', 1);
```

- [ ] **Step 2: Run the focused test again**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack6LocationDetailTests test
```

Expected:

```text
BUILD SUCCESS
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 3: Commit seed and test**

Run:

```powershell
git add src/main/resources/data-content-pack-6.sql src/test/java/cn/haut/survivor/service/ContentPack6LocationDetailTests.java
git commit -m "feat: add CP6.2 location detailing seeds"
```

Expected: commit succeeds.

## Task 3: Regression Verification

**Files:**
- No code files.

- [ ] **Step 1: Run all tests**

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

Record the exact final test count in the implementation feedback. The count should be higher than the CP6.1 baseline of `313`.

- [ ] **Step 2: Start the app**

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected: app starts on `http://localhost:8080`.

- [ ] **Step 3: HTTP smoke with login/profile setup**

Use the repo's established smoke flow. Verify:

```text
GET  /dashboard
GET  /map
GET  /map/location/2/event
GET  /map/location/4/event
GET  /map/location/6/event
GET  /map/location/8/event
GET  /exploration
POST /exploration/4
GET  /week/summary
GET  /npcs/6101
GET  /dungeons
GET  /organizations
```

Expected:

```text
All checked pages return 200 after login/profile setup.
No Whitelabel error page.
/map/location/{id}/event can select normal events from the expanded seed pool.
POST /exploration/4 still renders the exploration result page with influence feedback.
```

- [ ] **Step 4: Browser visual smoke only if UI changed**

Expected for this CP6.2 plan: no UI files changed, so browser visual smoke is not required. If the coding AI changed any template or CSS file, check:

```text
1366x768: /map, /exploration, /week/summary
375x812:  /map, /exploration, /week/summary
```

Expected:

```text
No horizontal scroll.
Mobile dock does not cover final buttons.
Map image and existing hotspot layer still render.
New content text wraps inside existing cards.
```

## Task 4: Update Documentation

**Files:**
- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
- Modify: `docs/NEXT_AI_HANDOFF.md`
- Optional modify: `docs/AI_CONTINUATION_PROMPT.md`

- [ ] **Step 1: Update `docs/PROJECT_COMPLETION_STATUS.md`**

Add a CP6.2 entry after the CP6/CP6.1 status content:

```markdown
## CP6.2 莲花街校区地点细分

状态：已完成

本批次复用现有地点、事件、传闻、探索奇遇链、周目标和成就系统，不新增数据库表。

新增内容：
- 事件：10 条，ID 6201-6210，覆盖图书馆、惟学楼、博闻楼营业厅/生活服务点、韶华楼、知味/知雅餐厅。
- 事件选项：30 条，ID 620101-621003，每个事件 3 个选项。
- 传闻：6 条，ID 6201-6206，继续使用现有 effect_type。
- 探索奇遇链：4 条链、12 个阶段，ID 6201-6212。
- 周目标：2 个，ID 6201-6202。
- 成就：2 个，ID 6201-6202。

验证：
- `.\mvnw.cmd clean test`
- HTTP smoke 覆盖 `/dashboard`、`/map`、`/map/location/2/event`、`/map/location/4/event`、`/map/location/6/event`、`/map/location/8/event`、`/exploration`、`POST /exploration/4`、`/week/summary`。
```

Replace the verification bullet with the exact test count and HTTP result after running Task 3.

- [ ] **Step 2: Update `docs/NEXT_AI_HANDOFF.md`**

Move CP6.2 into completed work and set the next recommendation to:

```markdown
建议下一步：
- CP6.3 开学迎新周机制化：把迎新、选课、社团招新、宿舍适应和校园卡/网络事务组织成一个更明确的新学期前两周节奏。
- 或先排查预存 500：如果 `/organizations/{id}`、`/dungeons/{id}/start`、`/dungeons/{id}/play` 仍然异常，优先修这些流程稳定性问题。
```

Also record:

```markdown
最近验证：
- `.\mvnw.cmd clean test`：填写实际 Tests run / Failures / Errors / Skipped。
- HTTP smoke：填写实际页面状态。
- UI/template/CSS：CP6.2 未修改则写“未修改 UI 文件，未执行视觉检查”。
```

- [ ] **Step 3: Update `docs/AI_CONTINUATION_PROMPT.md` only if stale**

If it still says CP6.2 is the immediate next task, replace that section with:

```markdown
当前最建议做的任务：
1. 如果发现 `/organizations/{id}` 或 `/dungeons/{id}/start|play` 仍有 500，先做稳定性排查。
2. 否则进入 CP6.3 开学迎新周机制化。
```

If it already reflects this, leave the file unchanged.

- [ ] **Step 4: Commit documentation**

Run:

```powershell
git add docs/PROJECT_COMPLETION_STATUS.md docs/NEXT_AI_HANDOFF.md docs/AI_CONTINUATION_PROMPT.md
git commit -m "docs: update handoff for CP6.2 location detailing"
```

If `docs/AI_CONTINUATION_PROMPT.md` was unchanged, remove it from the `git add` command.

Expected: commit succeeds.

## Final Implementation Feedback Required

The coding AI must report:

1. 修改文件。
2. 新增表/字段：预期为无。
3. 新增 seed 数据：按事件、事件选项、传闻、奇遇链、周目标、成就分别列数量和 ID 范围。
4. 新增/修改测试。
5. `.\mvnw.cmd clean test` 结果，必须包含精确 test count。
6. HTTP smoke 结果表。
7. 浏览器视觉检查结果：若未改 UI 文件，说明未执行及理由。
8. Git commit hashes。
9. 剩余风险。

## Self-Review Notes

- Spec coverage: 覆盖地点细分、真实校区地点语义、事件/传闻/奇遇链/目标/成就五个内容层。
- Placeholder scan: no unfinished implementation markers remain.
- Type consistency: tests use existing `Event`、`EventOption`、`Rumor`、`ExplorationStoryChain`、`WeeklyGoal`、`Achievement` entities and mappers.
- Scope safety: no schema, map subsystem, controller, template, or CSS change is required.
- Verification boundary: seed-only CP6.2 still requires `.\mvnw.cmd clean test` and HTTP smoke because content can affect map event and exploration flows.
