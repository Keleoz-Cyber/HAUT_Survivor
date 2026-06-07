# Content Pack 2 Academic Crisis Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Content Pack 2, a playable exam-week and DDL pressure arc that adds academic-crisis events, one database-course-design dungeon, weekly goals, achievements, rumors, NPC-flavored writing, and week-summary feedback.

**Architecture:** Reuse the existing Spring Boot + MyBatis-Plus + Thymeleaf architecture. Most content lands in `data.sql`; only light controller/service changes are needed for `academic_event` weekly goal progress, achievement unlocks, the new dungeon completion achievement, and week-summary academic crisis text.

**Tech Stack:** Java 17, Spring Boot 3.3.5, MyBatis-Plus, MySQL seed data via `schema.sql`/`data.sql`, Thymeleaf, JUnit 5, AssertJ, MockMvc.

---

## File Structure

Create:

- `src/test/java/cn/haut/survivor/service/ContentPack2Tests.java`

Modify:

- `src/main/resources/data.sql`
  - Append seed rows for academic-crisis events/options, one dungeon, dungeon tasks/options, weekly goals, achievements, and rumors.
- `src/main/java/cn/haut/survivor/controller/MapController.java`
  - Inject `WeeklyGoalService` and `AchievementService`.
  - After choosing an event option, if the event is an academic-crisis event, update `academic_event` weekly goal progress and unlock direct event achievements.
- `src/main/java/cn/haut/survivor/controller/DungeonController.java`
  - Unlock the new database-course-design achievement when dungeon id `3` completes.
- `src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`
  - Treat `academic_event` as a count-based weekly goal starting from zero.
- `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`
  - Make current-week summary text aware of week number and add academic-crisis feedback for high-pressure academic weeks.
- `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`
  - Verify academic-crisis event option increments `academic_event` weekly goal progress.
- `src/test/java/cn/haut/survivor/controller/DungeonControllerTests.java`
  - Verify completing dungeon `3` unlocks the new achievement.
- `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`
  - Verify `academic_event` starts at zero and progresses.
- `src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java`
  - Verify DDL/high-pressure week summary text uses academic-crisis feedback.

No new tables are planned.

---

### Task 1: Add Failing Seed Smoke Tests

**Files:**
- Create: `src/test/java/cn/haut/survivor/service/ContentPack2Tests.java`

- [ ] **Step 1: Create the failing seed smoke test file**

Create `src/test/java/cn/haut/survivor/service/ContentPack2Tests.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.Dungeon;
import cn.haut.survivor.domain.entity.DungeonTask;
import cn.haut.survivor.domain.entity.Event;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.DungeonMapper;
import cn.haut.survivor.mapper.DungeonTaskMapper;
import cn.haut.survivor.mapper.EventMapper;
import cn.haut.survivor.mapper.RumorMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ContentPack2Tests {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private DungeonMapper dungeonMapper;

    @Autowired
    private DungeonTaskMapper dungeonTaskMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Test
    void academicCrisisEventsSeeded() {
        List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .eq(Event::getEventType, "academic_crisis")
                .eq(Event::getStatus, 1));

        assertThat(events).hasSizeGreaterThanOrEqualTo(20);
        assertThat(events).extracting(Event::getEventName)
                .contains("早八点名危机", "Git 合并地狱", "考前抱佛脚");
    }

    @Test
    void databaseDefenseDungeonSeededWithFourStages() {
        Dungeon dungeon = dungeonMapper.selectOne(new LambdaQueryWrapper<Dungeon>()
                .eq(Dungeon::getDungeonName, "数据库课设答辩夜")
                .last("LIMIT 1"));

        assertThat(dungeon).isNotNull();
        assertThat(dungeon.getStatus()).isEqualTo(1);

        List<DungeonTask> tasks = dungeonTaskMapper.selectList(new LambdaQueryWrapper<DungeonTask>()
                .eq(DungeonTask::getDungeonId, dungeon.getId())
                .eq(DungeonTask::getStatus, 1)
                .orderByAsc(DungeonTask::getTaskOrder));

        assertThat(tasks).hasSize(4);
        assertThat(tasks).extracting(DungeonTask::getTaskName)
                .containsExactly("需求梳理", "ER 图连线", "SQL 暴走", "答辩现场");
    }

    @Test
    void academicCrisisWeeklyGoalsSeeded() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey, "study_twice", "ddl_survivor", "keep_calm_exam", "ask_for_help"));

        assertThat(goals).hasSize(4);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .contains("academic_event", "dungeon_stage", "pressure_keep", "npc_meet");
    }

    @Test
    void academicCrisisAchievementsSeeded() {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey,
                        "early_class_warrior",
                        "ddl_survivor_plus",
                        "last_minute_master",
                        "calm_under_pressure",
                        "help_seeker"));

        assertThat(achievements).hasSize(5);
        assertThat(achievements).extracting(Achievement::getRewardTitle)
                .contains("早八幸存者", "表结构守夜人", "考前冲刺型选手", "情绪稳定大师", "不单打独斗");
    }

    @Test
    void academicCrisisRumorsSeeded() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 2001L)
                .le(Rumor::getId, 2012L));

        assertThat(rumors).hasSizeGreaterThanOrEqualTo(8);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .contains("三楼靠窗复习位", "老郑的合并忠告", "阿杰的开黑陷阱");
    }
}
```

- [ ] **Step 2: Run the new test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack2Tests test
```

Expected: FAIL because the new seed rows do not exist yet.

- [ ] **Step 3: Commit the failing tests only if your workflow requires red commits**

If working in strict red-green commits, commit the failing test:

```powershell
git add src/test/java/cn/haut/survivor/service/ContentPack2Tests.java
git commit -m "test: add content pack 2 seed smoke tests"
```

If not committing red tests, keep the file staged for the next green commit.

---

### Task 2: Add Content Pack 2 Seed Data

**Files:**
- Modify: `src/main/resources/data.sql`

- [ ] **Step 1: Append academic-crisis events**

Append after the existing event and event option seed blocks in `src/main/resources/data.sql`. Use IDs above `2000` to avoid collisions.

```sql
-- ============================================================
-- Content Pack 2: 考试周与 DDL 生存线事件
-- ============================================================

INSERT INTO event (id, event_name, event_type, location_id, description, scene_image, mood_tag, probability, min_week, max_week, min_explore_level, status) VALUES
(2001, '早八点名危机', 'academic_crisis', 1, '闹钟响了三次，你终于在距离上课还有 12 分钟时醒来。群里有人说老师今天可能点名。', 'scene-classroom', '早八预警', 70, 1, 1, 0, 1),
(2002, '选课群消息轰炸', 'academic_crisis', 1, '刚下课，课程群突然刷出几十条消息：调课、作业、实验分组同时出现。', 'scene-classroom', '消息爆炸', 55, 1, 1, 0, 1),
(2003, '寝室作息磨合', 'academic_crisis', 3, '宿舍灯还亮着，阿杰正在开黑，明天早八的你陷入沉思。', 'scene-dorm', '寝室夜谈', 55, 1, 1, 0, 1),
(2004, '图书馆座位初体验', 'academic_crisis', 2, '你第一次认真找自习座位，发现插座、空调、同桌状态都像隐藏属性。', 'scene-library', '自习试炼', 50, 1, 1, 0, 1),
(2005, '课代表发来复习范围', 'academic_crisis', 2, '课代表突然在群里发了 8 页 PDF，说老师说这些都可能考。', 'scene-library', '小测预警', 70, 2, 2, 0, 1),
(2006, '林然的复习提纲', 'academic_crisis', 2, '学霸林然把一份排版工整到离谱的复习提纲放在桌上，你忍不住多看了两眼。', 'scene-library', '学霸光环', 55, 2, 2, 0, 1),
(2007, '社团招新撞上小测', 'academic_crisis', 7, '社团摊位正热闹，手机却弹出明天小测的提醒。热闹和复习开始抢你的行动点。', 'scene-club', '时间冲突', 60, 2, 2, 0, 1),
(2008, '老郑提醒课设别拖', 'academic_crisis', 6, '实验室师兄老郑看了一眼你的进度，说：课设这种东西，最后一天才开始会变成恐怖片。', 'scene-lab', '师兄警告', 60, 2, 2, 0, 1),
(2009, '第一次课程小测', 'academic_crisis', 1, '老师把小测卷子发下来，你发现题目都很眼熟，只是你和它们不太熟。', 'scene-classroom', '随堂小测', 65, 2, 2, 0, 1),
(2010, '食堂复习搭子局', 'academic_crisis', 4, '饭点的食堂很吵，但周予说这里最适合交换情报：谁点名，谁划重点，谁作业查重。', 'scene-canteen', '饭桌情报', 45, 2, 2, 0, 1),
(2011, '组队分工失控', 'academic_crisis', 6, '课设小组群里沉默了十分钟，最后只有你发了句：那我先建个仓库？', 'scene-lab', '组队危机', 75, 3, 3, 0, 1),
(2012, 'Git 合并地狱', 'academic_crisis', 6, '你拉取队友代码后，项目出现 37 个冲突。控制台红得像期末成绩单。', 'scene-lab', '合并冲突', 85, 3, 3, 0, 1),
(2013, '数据库设计返工', 'academic_crisis', 6, '老师一句“这个表是不是太万能了”，让你意识到数据库设计可能要推倒重来。', 'scene-lab', '结构返工', 80, 3, 3, 0, 1),
(2014, '宿舍通宵赶工', 'academic_crisis', 3, '凌晨一点，寝室只剩键盘声和泡面味。你看着未完成的课设，感觉 DDL 正坐在床边。', 'scene-dorm', '通宵边缘', 75, 3, 3, 0, 1),
(2015, '图书馆抢座失败', 'academic_crisis', 2, '你到图书馆时，好位置已经被书包占领。复习计划还没开始，心态先被考验。', 'scene-library', '座位战争', 60, 3, 3, 0, 1),
(2016, 'Bug 暴走排序', 'academic_crisis', 6, 'Bug 列表越修越长，你决定先给它们排个优先级，不然今晚谁也别想睡。', 'scene-lab', 'Bug 暴走', 80, 3, 3, 0, 1),
(2017, '考前抱佛脚', 'academic_crisis', 2, '距离考试还有一晚，你终于翻开了那本像新买的一样的教材。', 'scene-library', '最后冲刺', 80, 4, 4, 0, 1),
(2018, '体测前的操场夜跑', 'academic_crisis', 5, '小马在操场挥手：来都来了，跑一圈再回去复习。你的腿和大脑同时沉默。', 'scene-track', '体测冲刺', 60, 4, 4, 0, 1),
(2019, '课程报告最后修改', 'academic_crisis', 6, '报告封面、目录、截图、参考文献同时出问题，你开始怀疑 Word 也是副本 Boss。', 'scene-lab', '报告收尾', 65, 4, 4, 0, 1),
(2020, '期末前最后一节课', 'academic_crisis', 1, '老师说“我再强调最后一次”，全班突然坐直。你知道重点来了。', 'scene-classroom', '重点捕捉', 70, 4, 4, 0, 1);
```

- [ ] **Step 2: Append options for the new events**

Append this block immediately after the new events:

```sql
INSERT INTO event_option (id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
(5001, 2001, '直接冲刺去教室', '硬冲早八', 'medium', '你一路冲到教室，刚坐下老师就开始点名。心跳很快，但名字保住了。', 2, -1, 0, 0, 0, 3, 2, 18),
(5002, 2001, '让室友帮忙占座', '求助室友', 'low', '阿杰嘴上嫌麻烦，还是给你占了个后排。你赶到时还能喘口气。', 1, 0, 0, 1, 0, 1, 0, 12),
(5003, 2001, '继续睡，赌一手不点名', '摆烂续命', 'high', '这一觉很香，群里的点名消息也很刺眼。你获得了休息，也欠下了学业债。', -2, 2, 0, 0, 0, -1, -3, 6),
(5004, 2002, '立刻整理待办', '控住信息流', 'low', '你把群消息拆成了作业、调课和实验三类，混乱暂时被按进表格里。', 2, 0, 0, 0, 1, 1, 3, 20),
(5005, 2002, '问周予有没有简版', '找人类摘要', 'medium', '周予三句话讲清了重点，还顺手告诉你哪个老师最爱突然点名。', 1, 0, 0, 3, 0, 0, 0, 16),
(5006, 2002, '先静音群聊', '眼不见为净', 'high', '世界安静了，任务也没有消失。你短暂回血，但晚上会重新面对它们。', -1, 1, 0, 0, 0, -2, -2, 8),
(5007, 2003, '戴耳机继续预习', '寝室修行', 'medium', '阿杰的团战声从耳机缝里钻进来，但你还是看完了一章。', 3, 0, 0, 0, 0, 2, 2, 20),
(5008, 2003, '跟阿杰开一把再睡', '放松一局', 'medium', '一把之后又一把。你很快乐，也很清楚明天早八会发生什么。', -1, -1, 0, 2, 0, -2, -2, 10),
(5009, 2003, '约定宿舍熄灯时间', '协商作息', 'low', '你们认真谈了十分钟，宿舍第一次像个能长期运行的系统。', 0, 1, 0, 2, 0, -1, 2, 16),
(5010, 2004, '找靠窗插座位', '搜索自习 buff', 'low', '你找到了一个稳定座位，甚至有插座。学习体验突然高级了起来。', 3, 0, 0, 0, 1, 0, 2, 20),
(5011, 2004, '随便坐下先开始', '降低启动成本', 'low', '座位一般，但你没有把时间浪费在寻找完美条件上。', 2, 0, 0, 0, 0, -1, 1, 16),
(5012, 2004, '刷手机等好位置', '等待奇迹', 'high', '好位置没等来，短视频倒是刷了不少。', -1, 0, 0, 0, 0, 1, -2, 6),
(5013, 2005, '立刻整理复习计划', '计划先行', 'medium', '你把 8 页 PDF 拆成今晚能完成的三段，恐惧变成了进度条。', 4, 0, 0, 0, 1, 2, 3, 28),
(5014, 2005, '找林然借资料', '学霸支援', 'low', '林然把重点圈给你，还提醒你第二章例题最容易变形。', 3, 0, 0, 2, 0, -1, 1, 24),
(5015, 2005, '先收藏，晚上再看', '经典收藏', 'high', '你点了收藏，心里获得一种虚假的完成感。', 1, 0, 0, 0, 0, -1, -2, 8),
(5016, 2006, '认真请教林然', '借脑学习', 'low', '林然讲得很快，但你抓住了主线。你第一次觉得这门课还有救。', 4, 0, 0, 2, 0, 1, 1, 26),
(5017, 2006, '只拍照不提问', '资料囤积', 'medium', '照片很清晰，理解很模糊。资料躺进相册，等待一个更自律的你。', 1, 0, 0, 0, 0, 0, -1, 10),
(5018, 2006, '交换你的课堂笔记', '互相拯救', 'low', '你的笔记不算完美，但林然补上了缺口。你们像临时学习小队。', 3, 0, 0, 3, 0, 0, 1, 24),
(5019, 2007, '先去复习再逛摊', '稳妥安排', 'low', '你把复习放在前面，逛摊时心态明显轻了一点。', 3, 0, 0, 1, 0, -1, 2, 22),
(5020, 2007, '参加招新现场', '抓住热闹', 'medium', '你认识了几个人，也错过了一点复习时间。大学生活确实不是单线程。', -1, 0, 0, 4, 0, 2, -1, 18),
(5021, 2007, '让周予帮你打听重点', '社交换情报', 'low', '周予从三个群里拼出一份重点清单，社交网络开始发挥作用。', 2, 0, 0, 3, 0, 0, 0, 22),
(5022, 2008, '听老郑的先建最小版本', '控制范围', 'low', '老郑点了点头：先能跑，再好看。你把课设从幻想拉回现实。', 2, 0, 0, 1, 3, -1, 2, 28),
(5023, 2008, '继续设计大而全系统', '野心拉满', 'high', '功能越列越多，你感到一种危险的兴奋。', 1, -1, 0, 0, 2, 4, -1, 18),
(5024, 2008, '先假装没听见', '拖延一下', 'high', '你决定明天再开始。DDL 在远处笑了一声。', -1, 1, 0, 0, 0, -1, -2, 8),
(5025, 2009, '先做会的题', '稳住分数', 'low', '你把能拿的分先拿到手，卷面终于不再那么吓人。', 3, 0, 0, 0, 0, 1, 1, 24),
(5026, 2009, '硬啃最后大题', '冒险冲刺', 'high', '你盯着大题看了很久，最后写出一个不完整但有希望的思路。', 2, -1, 0, 0, 1, 3, 0, 20),
(5027, 2009, '交卷后立刻复盘', '把亏吃明白', 'medium', '你把错题记下来，痛苦没有消失，但变成了经验。', 3, 0, 0, 0, 1, 1, 2, 26),
(5028, 2010, '听周予讲情报', '饭桌情报局', 'low', '周予的信息密度比食堂窗口还高，你记下了几个关键提醒。', 1, 1, 0, 3, 0, -1, 0, 18),
(5029, 2010, '边吃边刷题', '效率混合餐', 'medium', '你在饭点刷了两道题，也差点把汤洒到本子上。', 2, 0, 0, 0, 0, 1, 1, 18),
(5030, 2010, '彻底放空吃顿好的', '回血优先', 'low', '热饭让你短暂想起自己是个人，不只是作业处理器。', 0, 3, -2, 1, 0, -3, 0, 14),
(5031, 2011, '主动拆分任务', '接管节奏', 'medium', '你把需求、数据库、页面和测试拆开，群里终于有人开始认领。', 3, 0, 0, 2, 2, 3, 3, 32),
(5032, 2011, '等队友先说话', '沉默观察', 'high', '大家都在等别人先动。十分钟后，群还是那个群。', -1, 0, 0, 0, 0, 2, -2, 8),
(5033, 2011, '找老郑看分工', '师兄介入', 'low', '老郑看完分工表，删掉了三个不现实的功能。你们终于有了主线。', 2, 0, 0, 1, 3, -1, 1, 30),
(5034, 2012, '冷静逐个处理冲突', '手动解冲突', 'high', '你一个文件一个文件处理，最后项目重新启动。你像刚从火场里走出来。', 2, -1, 0, 0, 5, 4, 2, 36),
(5035, 2012, '找老郑救场', '请求支援', 'low', '老郑让你先看冲突标记，再看业务意图。十分钟后你知道该删哪半边了。', 1, 0, 0, 1, 3, -2, 1, 30),
(5036, 2012, '复制旧版本覆盖', '危险回滚', 'high', '冲突没了，新功能也没了。你获得了安静，也获得了技术债。', -2, 0, 0, 0, -2, -3, -2, 8),
(5037, 2013, '重新梳理实体关系', '推倒重来', 'medium', '你把万能表拆开，结构突然清爽了。返工很痛，但方向对了。', 4, -1, 0, 0, 4, 3, 2, 34),
(5038, 2013, '给字段继续打补丁', '补丁续命', 'high', '字段越来越多，表名越来越抽象。你感觉自己在养一团数据迷雾。', 0, 0, 0, 0, 1, 3, -1, 12),
(5039, 2013, '问林然怎么解释', '表达优先', 'low', '林然帮你把“为什么拆表”讲成人话。答辩风险降了一些。', 3, 0, 0, 2, 1, -1, 1, 26),
(5040, 2014, '通宵赶完核心功能', '燃烧健康', 'high', '功能跑起来了，你也快不太跑得动了。凌晨的寝室像临时战场。', 3, -4, 0, 0, 4, 5, -1, 34),
(5041, 2014, '先睡四小时再继续', '保命策略', 'medium', '你睡得不多，但醒来后至少能看懂自己写的代码。', 1, 3, 0, 0, 1, -2, 1, 20),
(5042, 2014, '让阿杰陪你熬一会儿', '寝室互助', 'low', '阿杰递来泡面，还帮你吐槽了三分钟需求。你没那么孤单了。', 1, -1, -3, 2, 1, -2, 0, 20),
(5043, 2015, '换楼层继续找', '不轻易放弃', 'medium', '你绕了两层楼，终于找到一个角落座位。它不完美，但够用了。', 2, -1, 0, 0, 0, 2, 2, 20),
(5044, 2015, '直接回宿舍学', '改变战场', 'low', '宿舍诱惑很多，但你至少不用继续和书包争座位。', 1, 0, 0, 0, 0, 0, 1, 14),
(5045, 2015, '去食堂补充能量', '先救身体', 'low', '你承认自己饿了。吃完饭后，世界稍微正常了一点。', 0, 3, -2, 0, 0, -2, 0, 12),
(5046, 2016, '先修阻塞启动的 Bug', '抓主线', 'medium', '你没有被红色列表吓住，先处理了最影响演示的问题。', 2, 0, 0, 0, 5, 2, 2, 34),
(5047, 2016, '从最简单的开始修', '建立手感', 'low', '你修掉几个小问题，信心回来了，但核心 Bug 还在。', 1, 0, 0, 0, 3, 0, 1, 22),
(5048, 2016, '把 Bug 列表发给队友', '团队分摊', 'medium', '队友终于知道哪里爆了。虽然不一定都能修，但至少不是你一个人扛。', 1, 0, 0, 2, 2, -1, 1, 24),
(5049, 2017, '只看高频题型', '精准抱佛脚', 'medium', '你放弃全面复习，开始抓最可能出现的题。效率很高，心跳也很快。', 4, -1, 0, 0, 0, 3, 1, 28),
(5050, 2017, '跟林然一起过重点', '学霸带飞', 'low', '林然把重点讲得很清楚，你第一次觉得抱佛脚也能有章法。', 5, 0, 0, 2, 0, 1, 1, 32),
(5051, 2017, '放弃挣扎早点睡', '保存人类状态', 'medium', '你关上书，决定至少以清醒的大脑进入考场。', -2, 3, 0, 0, 0, -3, 0, 12),
(5052, 2018, '跟小马跑两圈', '体测预热', 'medium', '两圈下来你喘得厉害，但身体像被重新接上电源。', 0, 4, 0, 1, 0, -2, 1, 22),
(5053, 2018, '只拉伸不跑', '温和保养', 'low', '你没有硬冲，但至少认真照顾了一下身体。', 0, 2, 0, 0, 0, -1, 1, 14),
(5054, 2018, '继续回去复习', '学业优先', 'medium', '你把时间留给复习，腿部状态决定先欠着。', 3, -1, 0, 0, 0, 2, 1, 20),
(5055, 2019, '先修正文档结构', '报告也要架构', 'low', '目录和截图终于对齐，报告看起来像个正经项目了。', 3, 0, 0, 0, 2, 1, 2, 26),
(5056, 2019, '疯狂补截图', '视觉补救', 'medium', '截图很多，逻辑一般。至少老师翻页时不会太空。', 1, -1, 0, 0, 1, 2, 0, 16),
(5057, 2019, '找老郑看一眼', '答辩预演', 'low', '老郑指出了三个会被追问的地方，你提前补上了解释。', 3, 0, 0, 1, 3, -1, 1, 30),
(5058, 2020, '立刻记下老师强调的点', '捕捉重点', 'low', '你把“最后一次强调”全部写下，感觉考前雾气散了一点。', 4, 0, 0, 0, 0, 1, 2, 26),
(5059, 2020, '拍照发给宿舍群', '共享情报', 'low', '阿杰回了一个表情包，但十分钟后也开始问你重点在哪。', 2, 0, 0, 2, 0, 0, 1, 20),
(5060, 2020, '相信自己已经会了', '危险自信', 'high', '你合上本子，获得了短暂的自信和长期的不确定。', -1, 0, 0, 0, 0, -1, -2, 8);
```

- [ ] **Step 3: Append the database defense dungeon**

Append after existing dungeon seed blocks:

```sql
-- ============================================================
-- Content Pack 2: 数据库课设答辩夜副本
-- ============================================================

INSERT INTO dungeon (id, dungeon_name, dungeon_type, description, cover_image, theme_style, estimated_minutes, difficulty_label, reward_exp, reward_title, status) VALUES
(3, '数据库课设答辩夜', 'DDL', '答辩前夜，你要把需求、ER 图、SQL 报错和老师追问一起压进一个还能运行的系统里。', 'dungeon-db-defense', 'DDL', 10, '中等偏高', 90, '表结构守夜人', 1);

INSERT INTO dungeon_task (id, dungeon_id, task_name, task_type, task_order, scene_text, target_text, background_image, minigame_type, minigame_config, timer_seconds, settlement_rule, random_enabled, attribute_check_rule, pass_condition, required, status) VALUES
(3001, 3, '需求梳理', 'choice', 1, '老师要求系统必须有用户、任务、记录、统计四类核心数据。队友已经开始问能不能直接建表。', '先把实体和关系想清楚，避免后面返工。', 'scene-lab', NULL, NULL, NULL, 'option_score', 0, NULL, 'score>=50', 1, 1),
(3002, 3, 'ER 图连线', 'minigame', 2, '你需要把用户、任务、副本记录、属性变化连起来。每一条关系都可能决定后面 Bug 的数量。', '选择正确的数据关系。', 'scene-lab', 'db_link', 'user->player_attribute,event->event_option,dungeon->dungeon_task', 45, 'db_link_score', 0, NULL, 'score>=50', 1, 1),
(3003, 3, 'SQL 暴走', 'minigame', 3, '答辩前一晚，外键、字段名、时间类型同时报错。控制台像在开红色演唱会。', '定位最关键的 Bug，让项目重新跑起来。', 'scene-lab', 'bug_hunt', NULL, 60, 'bug_hunt_score', 1, 'skill>=50', 'score>=50', 1, 1),
(3004, 3, '答辩现场', 'choice', 4, '老师问：为什么这里要拆表？你看了一眼队友，发现大家都在看你。', '解释你的数据库设计，让答辩稳住。', 'scene-classroom', NULL, NULL, NULL, 'option_score', 0, 'academic>=55 OR skill>=55', 'score>=50', 1, 1);

INSERT INTO dungeon_task_option (id, dungeon_task_id, option_type, option_text, is_correct, trigger_probability, result_text, evaluation, score, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_task_id, status) VALUES
(7001, 3001, 'strategy', '先列实体和关系，再决定表结构', 1, 100, '你把用户、任务、记录、统计拆成清晰模块。后面的表结构终于有了骨架。', '需求控场', 90, 4, 0, 0, 0, 5, -2, 3, 35, 3002, 1),
(7002, 3001, 'strategy', '直接开始建表，边做边改', 0, 100, '你很快写出了第一版表，但字段越加越多，万能表的气息开始蔓延。', '边跑边补', 55, 1, 0, 0, 0, 2, 3, 0, 18, 3002, 1),
(7003, 3001, 'strategy', '先做页面，数据库最后再说', 0, 100, '页面看起来有了，数据从哪里来暂时没人知道。你获得了进度错觉。', '范围失控', 35, -1, 0, 0, 0, 1, 5, -2, 8, 3002, 1),
(7004, 3004, 'defense', '解释拆表是为了降低重复和方便扩展', 1, 100, '老师点了点头，又追问了一个细节。你接住了，队友终于敢呼吸。', '答辩稳定', 90, 5, 0, 0, 1, 4, -3, 2, 40, NULL, 1),
(7005, 3004, 'defense', '承认有些设计是为了 Demo 先跑起来', 1, 100, '你的回答不算完美，但足够真实。老师没有继续深挖最危险的地方。', '惊险通过', 65, 2, 0, 0, 0, 2, 1, 0, 24, NULL, 1),
(7006, 3004, 'defense', '把问题转给队友补充', 0, 100, '队友愣了一秒，你们在沉默中完成了一次无声的责任交接。', '现场摇晃', 40, 0, -1, 0, 1, -1, 5, -1, 10, NULL, 1);
```

- [ ] **Step 4: Append weekly goals, achievements, and rumors**

Append after existing weekly goal, achievement, and rumor seed blocks:

```sql
-- ============================================================
-- Content Pack 2: 周目标、成就、传闻
-- ============================================================

INSERT INTO weekly_goal (id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, active) VALUES
(2001, 'study_twice', '复习两小时', '本周完成 2 次学业危机事件，把复习从口号变成行动。', 'academic_event', 2, 35, 'academic', 3, 1),
(2002, 'ddl_survivor', 'DDL 幸存', '本周完成 2 个副本阶段，在压力线里活下来。', 'dungeon_stage', 2, 45, 'skill', 4, 1),
(2003, 'keep_calm_exam', '稳住别炸', '本周结束时压力不超过 55。', 'pressure_keep', 55, 40, 'pressure', 6, 1),
(2004, 'ask_for_help', '会求助也是本事', '本周至少遇见 1 位 NPC，大学不是单机游戏。', 'npc_meet', 1, 30, 'social', 3, 1);

INSERT INTO achievement (id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, active) VALUES
(2001, 'early_class_warrior', '早八战士', '在早八危机里选择认真应对。', '⏰', 'academic_event', 1, '早八幸存者', 1),
(2002, 'ddl_survivor_plus', 'DDL 幸存者', '完成数据库课设答辩夜副本。', '🗄️', 'dungeon_completed_db', 1, '表结构守夜人', 1),
(2003, 'last_minute_master', '抱佛脚大师', '在第 4 周完成考前复习事件。', '📚', 'academic_event', 1, '考前冲刺型选手', 1),
(2004, 'calm_under_pressure', '高压稳定器', '高压周仍然控制住压力。', '🧘', 'pressure_keep', 1, '情绪稳定大师', 1),
(2005, 'help_seeker', '会求助的人', '通过 NPC 帮助解决学业危机。', '🤝', 'academic_event', 1, '不单打独斗', 1);

INSERT INTO rumor (id, week_number, location_id, rumor_title, rumor_text, effect_hint, rarity, active) VALUES
(2001, 1, 1, '点名雷达启动', '有人说开学第一周老师最喜欢突然点名，因为大家还没进入状态。', '教学楼事件更容易出现早八和点名压力。', 'common', 1),
(2002, 1, 3, '阿杰的开黑陷阱', '阿杰说只打一把，但熟人都知道这一把通常有复数含义。', '宿舍可能出现放松与自律的取舍。', 'common', 1),
(2003, 2, 2, '三楼靠窗复习位', '图书馆三楼靠窗位置据说复习效率特别高，前提是你抢得到。', '图书馆更适合复习类事件。', 'rare', 1),
(2004, 2, 7, '招新和小测撞车', '社团区很热闹，但课程群里的小测提醒也是真的。', '社团和学业开始争夺行动点。', 'common', 1),
(2005, 2, 6, '老郑的合并忠告', '老郑说课设不要最后一天才合并代码，他说这话时眼神很有故事。', '实验室事件可能提前提示课设风险。', 'rare', 1),
(2006, 3, 6, '控制台红色预警', '实验室今晚有人通宵改 Bug，控制台红得照亮了半张脸。', '第 3 周实验室 DDL 事件概率提高。', 'common', 1),
(2007, 3, 3, '泡面救不了所有 Bug', '宿舍里泡面味越浓，说明大家离 DDL 越近。', '宿舍可能出现通宵赶工事件。', 'common', 1),
(2008, 3, 2, '座位战争升级', '图书馆的书包占座行为进入期末前形态。', '图书馆可能触发抢座和复习冲突。', 'common', 1),
(2009, 4, 2, '林然的最后重点', '林然整理了一份考前重点，但据说只会发给认真问的人。', '第 4 周图书馆适合抱佛脚。', 'rare', 1),
(2010, 4, 5, '体测队伍会变短', '有人说下午晚一点操场排队会短，但那时候腿也更不想动。', '操场体测事件出现。', 'common', 1),
(2011, 4, 6, '答辩老师爱问拆表', '数据库老师最近很爱问：为什么这里要拆表？', '数据库课设答辩夜需要解释能力。', 'rare', 1),
(2012, 4, 4, '热汤回血传说', '食堂二楼的热汤能救一半熬夜灵魂，另一半要靠睡觉。', '食堂仍然是降压和回血地点。', 'common', 1);
```

- [ ] **Step 5: Run seed smoke tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack2Tests test
```

Expected: PASS.

- [ ] **Step 6: Commit seed data and smoke tests**

```powershell
git add src/main/resources/data.sql src/test/java/cn/haut/survivor/service/ContentPack2Tests.java
git commit -m "feat: seed academic crisis content pack"
```

---

### Task 3: Add Academic Event Weekly Goal Progress and Event Achievements

**Files:**
- Modify: `src/main/java/cn/haut/survivor/controller/MapController.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`
- Modify: `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`
- Modify: `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`

- [ ] **Step 1: Write a WeeklyGoalService test for academic_event start and progress**

Add to `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`:

```java
@Test
void academicEventGoalStartsFromZeroAndProgresses() {
    playerService.createProfile(2L, "学业危机目标测试", "大二", "计算机类", "就业路线");

    WeeklyGoal goal = weeklyGoalMapper.selectOne(new LambdaQueryWrapper<WeeklyGoal>()
            .eq(WeeklyGoal::getGoalKey, "study_twice")
            .last("LIMIT 1"));
    assertThat(goal).isNotNull();

    UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, goal.getId());
    assertThat(userGoal.getStartValue()).isZero();
    assertThat(userGoal.getCurrentValue()).isZero();

    weeklyGoalService.updateProgress(2L, 1, "academic_event", 1);
    UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
    assertThat(updated.getCurrentValue()).isEqualTo(1);
    assertThat(updated.getCompleted()).isEqualTo(0);

    weeklyGoalService.updateProgress(2L, 1, "academic_event", 1);
    UserWeeklyGoal completed = weeklyGoalService.getCurrentGoal(2L, 1);
    assertThat(completed.getCurrentValue()).isEqualTo(2);
    assertThat(completed.getCompleted()).isEqualTo(1);
}
```

Imports needed if missing:

```java
import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
```

- [ ] **Step 2: Run the service test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyGoalServiceTests#academicEventGoalStartsFromZeroAndProgresses test
```

Expected before implementation: FAIL if `academic_event` does not start as a count-based goal.

- [ ] **Step 3: Treat academic_event as a count-based goal**

Modify `getStartValueForGoalType()` in `src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`:

```java
case "academic_event" -> {
    return 0; // 学业危机事件次数从 0 开始计数
}
```

Place it next to the existing count-based cases:

```java
case "explore_count" -> {
    return 0;
}
case "npc_meet" -> {
    return 0;
}
case "org_activity" -> {
    return 0;
}
case "dungeon_stage" -> {
    return 0;
}
case "academic_event" -> {
    return 0;
}
```

- [ ] **Step 4: Run the service test and verify it passes**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyGoalServiceTests#academicEventGoalStartsFromZeroAndProgresses test
```

Expected: PASS.

- [ ] **Step 5: Write a controller test for academic-crisis event progress**

Add to `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`:

```java
@Autowired
private WeeklyGoalMapper weeklyGoalMapper;

@Autowired
private UserWeeklyGoalMapper userWeeklyGoalMapper;

@Test
void choosingAcademicCrisisOptionUpdatesAcademicEventGoal() throws Exception {
    userService.register("academic_event_user", "password123");
    User user = userService.login("academic_event_user", "password123");
    playerService.createProfile(user.getId(), "学业事件进度测试", "大二", "计算机类", "就业路线");

    WeeklyGoal goal = weeklyGoalMapper.selectOne(new LambdaQueryWrapper<WeeklyGoal>()
            .eq(WeeklyGoal::getGoalKey, "study_twice")
            .last("LIMIT 1"));
    weeklyGoalService.chooseGoal(user.getId(), 1, goal.getId());

    mockMvc.perform(post("/map/event/2001/option/5001")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, user.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("map/event"));

    UserWeeklyGoal updated = userWeeklyGoalMapper.selectOne(new LambdaQueryWrapper<UserWeeklyGoal>()
            .eq(UserWeeklyGoal::getUserId, user.getId())
            .eq(UserWeeklyGoal::getWeekNumber, 1)
            .last("LIMIT 1"));

    assertThat(updated.getCurrentValue()).isEqualTo(1);
}
```

Imports needed if missing:

```java
import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.User;
import cn.haut.survivor.domain.entity.UserWeeklyGoal;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.UserWeeklyGoalMapper;
import cn.haut.survivor.mapper.WeeklyGoalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
```

Use existing fields from `MapControllerTests` if already present instead of duplicating them.

- [ ] **Step 6: Run the controller test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=MapControllerTests#choosingAcademicCrisisOptionUpdatesAcademicEventGoal test
```

Expected before implementation: FAIL because `MapController` does not update `academic_event`.

- [ ] **Step 7: Inject goal and achievement services into MapController**

Modify fields and constructor in `src/main/java/cn/haut/survivor/controller/MapController.java`:

```java
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.WeeklyGoalService;
```

Add fields:

```java
private final WeeklyGoalService weeklyGoalService;
private final AchievementService achievementService;
```

Update constructor:

```java
public MapController(EventService eventService, PlayerService playerService,
                     ExplorationService explorationService,
                     WeeklyThemeService weeklyThemeService, RumorService rumorService,
                     WeeklyGoalService weeklyGoalService, AchievementService achievementService) {
    this.eventService = eventService;
    this.playerService = playerService;
    this.explorationService = explorationService;
    this.weeklyThemeService = weeklyThemeService;
    this.rumorService = rumorService;
    this.weeklyGoalService = weeklyGoalService;
    this.achievementService = achievementService;
}
```

- [ ] **Step 8: Update academic-crisis progress after event option selection**

Modify `chooseOption()` in `MapController` after `EventRecord record = eventService.chooseOption(...)`:

```java
Event event = eventService.findEventById(eventId);
PlayerProfile profile = playerService.findProfileByUserId(userId);
if (event != null && "academic_crisis".equals(event.getEventType()) && profile != null) {
    weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "academic_event", 1);
    unlockAcademicCrisisAchievements(userId, event, record);
}
```

Then add this private method to `MapController`:

```java
private void unlockAcademicCrisisAchievements(Long userId, Event event, EventRecord record) {
    String name = event.getEventName() == null ? "" : event.getEventName();
    String result = record.getResultText() == null ? "" : record.getResultText();

    if (name.contains("早八") && record.getAttributeChange() != null
            && record.getAttributeChange().discipline() > 0) {
        achievementService.unlockAchievement(userId, "early_class_warrior");
    }

    if (name.contains("考前") || name.contains("抱佛脚")) {
        achievementService.unlockAchievement(userId, "last_minute_master");
    }

    if (result.contains("林然") || result.contains("老郑") || result.contains("周予") || result.contains("阿杰")) {
        achievementService.unlockAchievement(userId, "help_seeker");
    }
}
```

- [ ] **Step 9: Run affected tests**

Run:

```powershell
.\mvnw.cmd -Dtest=MapControllerTests,WeeklyGoalServiceTests test
```

Expected: PASS.

- [ ] **Step 10: Commit**

```powershell
git add src/main/java/cn/haut/survivor/controller/MapController.java src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java src/test/java/cn/haut/survivor/controller/MapControllerTests.java src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java
git commit -m "feat: track academic crisis event progress"
```

---

### Task 4: Unlock New Dungeon Achievement

**Files:**
- Modify: `src/main/java/cn/haut/survivor/controller/DungeonController.java`
- Modify: `src/test/java/cn/haut/survivor/controller/DungeonControllerTests.java`

- [ ] **Step 1: Write a controller test for completing the database defense dungeon**

Add to `src/test/java/cn/haut/survivor/controller/DungeonControllerTests.java`:

```java
@Test
void completingDatabaseDefenseDungeonUnlocksAchievement() throws Exception {
    userService.register("db_dungeon_user", "password123");
    User user = userService.login("db_dungeon_user", "password123");
    playerService.createProfile(user.getId(), "数据库答辩测试", "大二", "计算机类", "就业路线");

    mockMvc.perform(get("/dungeons/3/start")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, user.getId()))
            .andExpect(status().is3xxRedirection());

    mockMvc.perform(post("/dungeons/3/task/3001/option/7001")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, user.getId()))
            .andExpect(status().isOk());

    mockMvc.perform(post("/dungeons/3/task/3002/minigame")
                    .param("selectedRelations", "user->player_attribute", "event->event_option", "dungeon->dungeon_task")
                    .param("elapsedSeconds", "20")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, user.getId()))
            .andExpect(status().isOk());

    mockMvc.perform(post("/dungeons/3/task/3003/bughunt")
                    .param("questionIds", "0", "1", "2")
                    .param("answers", "1", "0", "0")
                    .param("elapsedSeconds", "30")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, user.getId()))
            .andExpect(status().isOk());

    mockMvc.perform(post("/dungeons/3/task/3004/option/7004")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, user.getId()))
            .andExpect(status().isOk());

    assertThat(achievementService.hasUnlocked(user.getId(), "ddl_survivor_plus")).isTrue();
}
```

Use existing test helper imports and fields if already available.

- [ ] **Step 2: Run the test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=DungeonControllerTests#completingDatabaseDefenseDungeonUnlocksAchievement test
```

Expected before implementation: FAIL because dungeon id `3` completion does not unlock `ddl_survivor_plus`.

- [ ] **Step 3: Update all dungeon completion branches**

In `src/main/java/cn/haut/survivor/controller/DungeonController.java`, find the three places that unlock dungeon completion achievements:

```java
if ("COMPLETED".equals(updatedRecord.getStatus())) {
    if (dungeonId == 1L) {
        achievementService.unlockAchievement(userId, "java_survivor");
    } else if (dungeonId == 2L) {
        achievementService.unlockAchievement(userId, "fitness_survivor");
    }
}
```

Replace with:

```java
unlockDungeonCompletionAchievement(userId, dungeonId, updatedRecord);
```

Add this private method:

```java
private void unlockDungeonCompletionAchievement(Long userId, Long dungeonId, UserDungeonRecord updatedRecord) {
    if (!"COMPLETED".equals(updatedRecord.getStatus())) {
        return;
    }
    if (dungeonId == 1L) {
        achievementService.unlockAchievement(userId, "java_survivor");
    } else if (dungeonId == 2L) {
        achievementService.unlockAchievement(userId, "fitness_survivor");
    } else if (dungeonId == 3L) {
        achievementService.unlockAchievement(userId, "ddl_survivor_plus");
    }
}
```

Apply this method in:

- `chooseOption()`
- `submitMinigame()`
- `submitBugHunt()`

- [ ] **Step 4: Run dungeon controller tests**

Run:

```powershell
.\mvnw.cmd -Dtest=DungeonControllerTests test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/cn/haut/survivor/controller/DungeonController.java src/test/java/cn/haut/survivor/controller/DungeonControllerTests.java
git commit -m "feat: unlock database defense dungeon achievement"
```

---

### Task 5: Add Academic Crisis Week Summary Feedback

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`
- Modify: `src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java`

- [ ] **Step 1: Write summary tests**

Add to `src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java`:

```java
@Test
void ddlWeekHighPressureUsesAcademicCrisisSummary() {
    playerService.createProfile(2L, "DDL总结测试", "大二", "计算机类", "就业路线");
    PlayerProfile profile = playerService.findProfileByUserId(2L);
    profile.setCurrentWeek(3);
    playerProfileMapper.updateById(profile);

    PlayerAttribute attr = playerService.findAttributeByUserId(2L);
    attr.setAcademic(68);
    attr.setSkill(72);
    attr.setPressure(78);
    playerAttributeMapper.updateById(attr);

    WeekSummaryService.WeekSummaryView view = weekSummaryService.buildCurrentWeekSummary(2L, 3);

    assertThat(view.summaryText()).contains("DDL");
}

@Test
void finalWeekGoodAcademicUsesExamSprintSummary() {
    playerService.createProfile(2L, "期末总结测试", "大二", "计算机类", "就业路线");
    PlayerProfile profile = playerService.findProfileByUserId(2L);
    profile.setCurrentWeek(4);
    playerProfileMapper.updateById(profile);

    PlayerAttribute attr = playerService.findAttributeByUserId(2L);
    attr.setAcademic(82);
    attr.setSkill(70);
    attr.setPressure(45);
    playerAttributeMapper.updateById(attr);

    WeekSummaryService.WeekSummaryView view = weekSummaryService.buildCurrentWeekSummary(2L, 4);

    assertThat(view.summaryText()).contains("复习");
}
```

Use existing autowired mappers/fields if present. If `playerProfileMapper` or `playerAttributeMapper` are not available in the test class, add:

```java
@Autowired
private PlayerProfileMapper playerProfileMapper;

@Autowired
private PlayerAttributeMapper playerAttributeMapper;
```

Imports:

```java
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.PlayerProfileMapper;
```

- [ ] **Step 2: Run the summary tests and verify they fail**

Run:

```powershell
.\mvnw.cmd -Dtest=WeekSummaryServiceTests#ddlWeekHighPressureUsesAcademicCrisisSummary,WeekSummaryServiceTests#finalWeekGoodAcademicUsesExamSprintSummary test
```

Expected before implementation: FAIL because existing summary text does not include DDL/exam-specific wording.

- [ ] **Step 3: Pass week number into summary text generation**

In `WeekSummaryServiceImpl.buildCurrentWeekSummary()`, replace:

```java
String summaryText = generateSummaryText(attribute, goalCompleted, goalClaimed, knownNpcCount);
```

with:

```java
String summaryText = generateSummaryText(attribute, goalCompleted, goalClaimed, knownNpcCount, weekNumber);
```

Change the private method signature:

```java
private String generateSummaryText(PlayerAttribute attr, boolean goalCompleted, boolean goalClaimed, int npcCount, int weekNumber) {
```

- [ ] **Step 4: Add academic crisis branches before generic summary branches**

At the top of `generateSummaryText()`, after null handling and local variables, add:

```java
boolean highPressure = pressure >= 60;
boolean lowHealth = attr.getHealth() < 40;
boolean highAcademic = attr.getAcademic() >= 70;
boolean highSkill = attr.getSkill() >= 70;

if (weekNumber == 3 && highPressure && (highAcademic || highSkill)) {
    return "你把 DDL 压成了可控范围，但精神状态已经像被控制台红字照了一晚上。";
}
if (weekNumber == 3 && highPressure) {
    return "这一周 DDL 明显压上来了。你还在推进，但下周最好别继续硬撑。";
}
if (weekNumber == 3 && (highAcademic || highSkill)) {
    return "你这周认真处理了课设和复习，DDL 没消失，但至少开始听你指挥。";
}
if (weekNumber == 4 && (highAcademic || highSkill) && pressure < 60) {
    return "期末前你稳住了复习节奏，没有把最后一周过成灾难片。";
}
if (weekNumber == 4 && pressure >= 70) {
    return "期末周把你推到了边缘，复习、体测和报告像同时响起的闹钟。";
}
```

Keep the existing generic branches after these academic-crisis branches.

- [ ] **Step 5: Run summary tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeekSummaryServiceTests test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java
git commit -m "feat: add academic crisis week summaries"
```

---

### Task 6: Final Verification and Browser Smoke

**Files:**
- No planned source edits unless verification finds issues.

- [ ] **Step 1: Run full tests**

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

- [ ] **Step 2: Start the app**

If port `8080` is free:

```powershell
.\mvnw.cmd spring-boot:run
```

If `8080` is occupied, stop the stale Java process or set another port:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

- [ ] **Step 3: Browser smoke pages**

Check these pages as `student/student123` after creating or reusing a character:

- `/dashboard`
- `/map`
- `/map/location/1/event`
- `/dungeons`
- `/dungeons/3`
- `/dungeons/3/play`
- `/week/summary`

Expected:

- No Whitelabel page.
- Player pages still use `game-hud` and `game-dock`.
- New dungeon appears in the dungeon list.
- New academic-crisis events can render when triggered directly by URL/form flow.
- Mobile width around `375x812` has no horizontal scroll and no Dock covering primary buttons.

- [ ] **Step 4: Verify seed counts directly if needed**

Optional MySQL check:

```sql
SELECT COUNT(*) FROM event WHERE event_type='academic_crisis';
SELECT COUNT(*) FROM dungeon WHERE dungeon_name='数据库课设答辩夜';
SELECT COUNT(*) FROM weekly_goal WHERE goal_key IN ('study_twice','ddl_survivor','keep_calm_exam','ask_for_help');
SELECT COUNT(*) FROM achievement WHERE achievement_key IN ('early_class_warrior','ddl_survivor_plus','last_minute_master','calm_under_pressure','help_seeker');
SELECT COUNT(*) FROM rumor WHERE id BETWEEN 2001 AND 2012;
```

Expected:

```text
20
1
4
5
12
```

- [ ] **Step 5: Final commit if verification required fixes**

If any small fixes were needed during verification:

```powershell
git add <changed-files>
git commit -m "fix: stabilize academic crisis content pack"
```

- [ ] **Step 6: Final report**

Report:

- Modified files.
- New seed data counts.
- New tests.
- Full test result.
- Browser smoke result.
- Any remaining risk.

Do not report success unless `.\mvnw.cmd clean test` has been run after the final code change and shows zero failures/errors.

---

## Plan Self-Review

Spec coverage:

- Academic-crisis events: Task 2 seeds 20 events and 60 options.
- New dungeon: Task 2 seeds `数据库课设答辩夜` with 4 stages.
- Weekly goals: Task 2 seeds 4 goals; Task 3 wires `academic_event`.
- Achievements: Task 2 seeds 5 achievements; Tasks 3 and 4 wire direct unlock points.
- Rumors: Task 2 seeds 12 rumors.
- NPC participation: event option result text references 阿杰、林然、周予、老郑、小马 across multiple events.
- Week summary feedback: Task 5 adds week-aware academic crisis summary text.
- Testing: Tasks 1, 3, 4, 5, and 6 define targeted and full verification.

No intentional schema changes. No UI rewrite. No complete GPA/course-table system.
