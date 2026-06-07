# Content Pack 3 NPC Buddy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the NPC buddy content pack: active NPC interactions, weekly buddy selection, relationship stages, seed content, achievements, weekly goals, and week-summary feedback.

**Architecture:** Add two lightweight tables: `npc_interaction` for interaction definitions and `user_npc_weekly_action` for per-week interaction/buddy state. Extend the existing `NpcService` instead of creating a separate subsystem, reuse `PlayerService.consumeActionPoint()`, `WeeklyGoalService.updateProgress()`, and `AchievementService` for side effects. Add a focused `NpcController` and small Thymeleaf pages that reuse the game UI shell.

**Tech Stack:** Java 17, Spring Boot 3.3.5, MyBatis-Plus, MySQL seed data, Thymeleaf, JUnit 5, AssertJ, MockMvc.

---

## File Structure

Create:

- `src/main/java/cn/haut/survivor/domain/entity/NpcInteraction.java`
- `src/main/java/cn/haut/survivor/domain/entity/UserNpcWeeklyAction.java`
- `src/main/java/cn/haut/survivor/mapper/NpcInteractionMapper.java`
- `src/main/java/cn/haut/survivor/mapper/UserNpcWeeklyActionMapper.java`
- `src/main/java/cn/haut/survivor/controller/NpcController.java`
- `src/main/resources/templates/npc/detail.html`
- `src/main/resources/templates/npc/result.html`
- `src/main/resources/data-content-pack-3.sql`
- `src/test/java/cn/haut/survivor/service/ContentPack3Tests.java`
- `src/test/java/cn/haut/survivor/controller/NpcControllerTests.java`

Modify:

- `src/main/resources/schema.sql`
  - Add `npc_interaction` and `user_npc_weekly_action`.
- `src/main/resources/application.yml`
  - Add `classpath:data-content-pack-3.sql` to data locations.
- `src/main/java/cn/haut/survivor/service/NpcService.java`
  - Add interaction, stage, and buddy APIs.
- `src/main/java/cn/haut/survivor/service/impl/NpcServiceImpl.java`
  - Implement active interactions, weekly action state, attributes, AP consumption, weekly goals, and achievements.
- `src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`
  - Add count-based goal types `npc_interaction`, `buddy_selected`, and `familiarity_gain`.
- `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`
  - Add NPC buddy summary text.
- `src/main/java/cn/haut/survivor/controller/DashboardController.java`
  - Add current buddy model attribute.
- `src/main/resources/templates/dashboard/index.html`
  - Upgrade the NPC strip with relationship stage, detail links, and buddy marker.
- `src/main/resources/static/css/app.css`
  - Add NPC detail/result styles.
- `src/test/java/cn/haut/survivor/service/NpcServiceTests.java`
  - Add interaction and buddy service tests.
- `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`
  - Add start-value/progress tests for new goal types.
- `src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java`
  - Add buddy-related summary tests.
- `src/test/java/cn/haut/survivor/controller/DashboardControllerTests.java`
  - Add model assertion for current buddy if needed.

---

### Task 1: Add Failing Seed Smoke Tests

**Files:**
- Create: `src/test/java/cn/haut/survivor/service/ContentPack3Tests.java`

- [ ] **Step 1: Create seed smoke tests**

Create `src/test/java/cn/haut/survivor/service/ContentPack3Tests.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.NpcInteractionMapper;
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
class ContentPack3Tests {

    @Autowired
    private NpcInteractionMapper npcInteractionMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private RumorMapper rumorMapper;

    @Test
    void npcInteractionsSeeded() {
        List<NpcInteraction> interactions = npcInteractionMapper.selectList(
                new LambdaQueryWrapper<NpcInteraction>().eq(NpcInteraction::getActive, 1));

        assertThat(interactions).hasSizeGreaterThanOrEqualTo(15);
        assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
                .contains(
                        "ajie_night_talk",
                        "linran_study",
                        "zhouyu_news",
                        "laozheng_debug",
                        "xiaoma_jog");
    }

    @Test
    void npcBuddyWeeklyGoalsSeeded() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey,
                        "buddy_chat",
                        "buddy_week",
                        "relationship_builder"));

        assertThat(goals).hasSize(3);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .contains("npc_interaction", "buddy_selected", "familiarity_gain");
    }

    @Test
    void npcBuddyAchievementsSeeded() {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey,
                        "first_buddy",
                        "iron_buddy",
                        "social_web",
                        "study_partner",
                        "lab_apprentice"));

        assertThat(achievements).hasSize(5);
        assertThat(achievements).extracting(Achievement::getRewardTitle)
                .contains("有搭子的人", "默契满分", "到哪都有熟人", "自习室常驻队友", "师兄认证");
    }

    @Test
    void npcBuddyRumorsSeeded() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 3001L)
                .le(Rumor::getId, 3010L));

        assertThat(rumors).hasSizeGreaterThanOrEqualTo(8);
        assertThat(rumors).extracting(Rumor::getRumorTitle)
                .contains("阿杰的拼单邀请", "林然的固定座位", "老郑的白板建议");
    }
}
```

- [ ] **Step 2: Run the new test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack3Tests test
```

Expected: FAIL because `NpcInteractionMapper` and `NpcInteraction` do not exist yet.

- [ ] **Step 3: Commit is not needed yet**

Do not commit the failing test by itself unless using strict red-green commits. Keep it for Task 2.

---

### Task 2: Add Schema, Entities, Mappers, and Seed Data

**Files:**
- Modify: `src/main/resources/schema.sql`
- Modify: `src/main/resources/application.yml`
- Create: `src/main/resources/data-content-pack-3.sql`
- Create: `src/main/java/cn/haut/survivor/domain/entity/NpcInteraction.java`
- Create: `src/main/java/cn/haut/survivor/domain/entity/UserNpcWeeklyAction.java`
- Create: `src/main/java/cn/haut/survivor/mapper/NpcInteractionMapper.java`
- Create: `src/main/java/cn/haut/survivor/mapper/UserNpcWeeklyActionMapper.java`
- Test: `src/test/java/cn/haut/survivor/service/ContentPack3Tests.java`

- [ ] **Step 1: Add schema tables**

In `src/main/resources/schema.sql`, add these drops near the existing NPC/user relation drops:

```sql
DROP TABLE IF EXISTS user_npc_weekly_action;
DROP TABLE IF EXISTS npc_interaction;
```

Add these tables immediately after `user_npc_relation`:

```sql
CREATE TABLE IF NOT EXISTS npc_interaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    npc_id BIGINT NOT NULL,
    interaction_key VARCHAR(50) NOT NULL UNIQUE,
    interaction_name VARCHAR(100) NOT NULL,
    required_familiarity INT NOT NULL DEFAULT 0,
    description TEXT,
    result_text TEXT,
    academic_change INT NOT NULL DEFAULT 0,
    health_change INT NOT NULL DEFAULT 0,
    money_change INT NOT NULL DEFAULT 0,
    social_change INT NOT NULL DEFAULT 0,
    skill_change INT NOT NULL DEFAULT 0,
    pressure_change INT NOT NULL DEFAULT 0,
    discipline_change INT NOT NULL DEFAULT 0,
    familiarity_change INT NOT NULL DEFAULT 0,
    exp_change INT NOT NULL DEFAULT 0,
    active INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_ni_npc FOREIGN KEY (npc_id) REFERENCES npc(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_npc_weekly_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    npc_id BIGINT NOT NULL,
    week_number INT NOT NULL,
    interacted INT NOT NULL DEFAULT 0,
    buddy_selected INT NOT NULL DEFAULT 0,
    selected_at DATETIME,
    interacted_at DATETIME,
    CONSTRAINT fk_unwa_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_unwa_npc FOREIGN KEY (npc_id) REFERENCES npc(id),
    UNIQUE KEY uk_user_npc_week (user_id, npc_id, week_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Create `NpcInteraction` entity**

Create `src/main/java/cn/haut/survivor/domain/entity/NpcInteraction.java`:

```java
package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("npc_interaction")
public class NpcInteraction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long npcId;
    private String interactionKey;
    private String interactionName;
    private Integer requiredFamiliarity;
    private String description;
    private String resultText;
    private Integer academicChange;
    private Integer healthChange;
    private Integer moneyChange;
    private Integer socialChange;
    private Integer skillChange;
    private Integer pressureChange;
    private Integer disciplineChange;
    private Integer familiarityChange;
    private Integer expChange;
    private Integer active;
}
```

- [ ] **Step 3: Create `UserNpcWeeklyAction` entity**

Create `src/main/java/cn/haut/survivor/domain/entity/UserNpcWeeklyAction.java`:

```java
package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_npc_weekly_action")
public class UserNpcWeeklyAction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long npcId;
    private Integer weekNumber;
    private Integer interacted;
    private Integer buddySelected;
    private LocalDateTime selectedAt;
    private LocalDateTime interactedAt;
}
```

- [ ] **Step 4: Create mappers**

Create `src/main/java/cn/haut/survivor/mapper/NpcInteractionMapper.java`:

```java
package cn.haut.survivor.mapper;

import cn.haut.survivor.domain.entity.NpcInteraction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NpcInteractionMapper extends BaseMapper<NpcInteraction> {
}
```

Create `src/main/java/cn/haut/survivor/mapper/UserNpcWeeklyActionMapper.java`:

```java
package cn.haut.survivor.mapper;

import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserNpcWeeklyActionMapper extends BaseMapper<UserNpcWeeklyAction> {
}
```

- [ ] **Step 5: Add data-content-pack-3 to application.yml**

In `src/main/resources/application.yml`, set `spring.sql.init.data-locations` to include all seed files:

```yaml
spring:
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql,classpath:data-content-pack-2.sql,classpath:data-content-pack-3.sql
```

Keep existing datasource and other settings unchanged.

- [ ] **Step 6: Create Content Pack 3 seed file**

Create `src/main/resources/data-content-pack-3.sql`:

```sql
-- ============================================================
-- Content Pack 3: 校园搭子与人际关系线
-- ============================================================

INSERT INTO npc_interaction
(id, npc_id, interaction_key, interaction_name, required_familiarity, description, result_text,
 academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change,
 familiarity_change, exp_change, active) VALUES
(3001, 1, 'ajie_night_talk', '夜聊一下', 0,
 '阿杰正瘫在椅子上刷视频，看到你进来，顺手把另一只耳机递了过来。',
 '你们从课设聊到食堂哪家窗口最稳。压力确实松了一点，寝室像一个临时避难所。',
 0, 0, 0, 2, 0, -4, 0, 4, 18, 1),
(3002, 1, 'ajie_game_break', '开黑放松', 0,
 '阿杰说只打一把。你知道这句话在寝室里的可信度大概等于“我马上睡”。',
 '这一把打得很爽，压力降了不少。代价是你原本准备复习的半小时消失了。',
 -1, 0, 0, 3, 0, -6, -2, 5, 16, 1),
(3003, 1, 'ajie_morning_call', '早八互相叫醒', 50,
 '你和阿杰约定明天谁先醒谁就把另一个从床上拽起来。',
 '第二天你们都很痛苦，但都到了教室。某种意义上，这就是寝室战友情。',
 2, -1, 0, 1, 0, 2, 4, 6, 26, 1),

(3004, 2, 'linran_notes', '借复习提纲', 0,
 '林然把一份整理到近乎艺术品的复习提纲递给你，上面连易错点都有颜色标记。',
 '资料很清楚，也让你意识到自己欠账不少。至少现在知道该从哪里开始补。',
 4, 0, 0, 1, 0, 2, 2, 4, 24, 1),
(3005, 2, 'linran_study', '一起自习', 20,
 '你坐到林然旁边，手机刚亮一下，她就用余光看了你一眼。',
 '这段自习效率高得离谱。你第一次觉得坐对位置也是一种策略。',
 5, 0, 0, 1, 0, 1, 4, 5, 30, 1),
(3006, 2, 'linran_quiz', '互相抽题', 50,
 '林然说“我随便问几个”。你听到“随便”两个字时已经开始紧张了。',
 '你被问住了几次，但每次都记得更牢。痛苦是真的，有效也是真的。',
 7, 0, 0, 1, 0, 4, 3, 7, 38, 1),

(3007, 3, 'zhouyu_news', '打听校园消息', 0,
 '周予刚从一个社团摊位赶到食堂，手里还攥着三张不同颜色的传单。',
 '她用五分钟告诉你谁在招人、谁在点名、哪里有活动。信息量大到像开了校园雷达。',
 0, 0, 0, 5, 0, 1, 0, 4, 22, 1),
(3008, 3, 'zhouyu_activity', '一起参加活动', 20,
 '周予说“来都来了，认识几个人再走”。你被她拉进了一个小型活动现场。',
 '你认识了不少人，也消耗了不少社交电量。大学生活突然变得很热闹。',
 0, -1, 0, 7, 0, 2, 0, 5, 28, 1),
(3009, 3, 'zhouyu_teammate', '牵线找队友', 50,
 '周予翻了翻联系人，说她刚好知道一个也在找课设队友的人。',
 '她牵线的效率高得可怕。你忽然理解了人脉为什么也是战斗力。',
 1, 0, 0, 5, 3, 0, 1, 7, 36, 1),

(3010, 4, 'laozheng_debug', '请教报错', 0,
 '老郑看了一眼控制台，说第一步不是乱改，是先看日志。',
 '他没有直接替你写代码，但你终于知道错误从哪里冒出来了。',
 1, 0, 0, 1, 5, -2, 1, 4, 26, 1),
(3011, 4, 'laozheng_review', '看代码结构', 20,
 '老郑把你的项目结构从头到尾扫了一遍，沉默三秒后开始画图。',
 '被指出问题很扎心，但结构清楚之后，项目突然不像一团毛线了。',
 3, 0, 0, 0, 6, 2, 2, 5, 34, 1),
(3012, 4, 'laozheng_overtime', '一起熬课设', 50,
 '实验室灯亮到很晚，老郑说“今晚先把能跑的版本救出来”。',
 '你们把关键问题一个个压下去。健康掉了一点，但技能涨得很实在。',
 3, -2, 0, 1, 8, 3, 3, 7, 42, 1),

(3013, 5, 'xiaoma_jog', '跑一圈', 0,
 '小马在操场冲你招手，说来都来了，跑一圈再走。',
 '你本来只是路过，结果真的跑了一圈。身体累了，脑子倒是清醒不少。',
 0, 5, 0, 1, 0, -4, 1, 4, 20, 1),
(3014, 5, 'xiaoma_stretch', '拉伸放松', 20,
 '小马认真教你拉伸，还说久坐之后直接回去学习效率很低。',
 '身体像重新开机，压力也松了一点。你开始怀疑运动搭子真的有用。',
 0, 6, 0, 1, 0, -5, 2, 5, 28, 1),
(3015, 5, 'xiaoma_training', '体测特训', 50,
 '小马给你安排了一套体测前训练节奏，听起来不难，做起来另说。',
 '训练结束后你累得不想说话，但健康和自律都实打实地涨了。',
 0, 8, 0, 2, 0, -2, 4, 7, 36, 1);

INSERT INTO weekly_goal
(id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, active) VALUES
(3001, 'buddy_chat', '找搭子聊聊', '本周主动和任意 NPC 互动 1 次。', 'npc_interaction', 1, 30, 'social', 2, 1),
(3002, 'buddy_week', '本周不单机', '本周选择 1 位校园搭子。', 'buddy_selected', 1, 25, 'pressure', 3, 1),
(3003, 'relationship_builder', '关系升温', '本周累计获得 10 点熟悉度。', 'familiarity_gain', 10, 35, 'social', 3, 1);

INSERT INTO achievement
(id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, active) VALUES
(3001, 'first_buddy', '第一个搭子', '任一 NPC 熟悉度达到 50。', '🤝', 'npc_familiarity', 50, '有搭子的人', 1),
(3002, 'iron_buddy', '铁搭子认证', '任一 NPC 熟悉度达到 80。', '🫡', 'npc_familiarity', 80, '默契满分', 1),
(3003, 'social_web', '校园人脉网', '认识 5 个 NPC。', '🕸️', 'npc_count', 5, '到哪都有熟人', 1),
(3004, 'study_partner', '复习搭子', '和林然互动 3 次。', '📚', 'npc_interaction_linran', 3, '自习室常驻队友', 1),
(3005, 'lab_apprentice', '实验室半个自己人', '和老郑互动 3 次。', '🔬', 'npc_interaction_laozheng', 3, '师兄认证', 1);

INSERT INTO rumor
(id, week_number, location_id, rumor_title, rumor_text, effect_hint, rarity, active) VALUES
(3001, 1, 3, '阿杰的拼单邀请', '阿杰说今晚宿舍有人拼外卖，适合缓一口气。', '宿舍互动可能降低压力。', 'common', 1),
(3002, 1, 2, '林然的固定座位', '林然最近在图书馆固定位置复习，去那里可能遇见她。', '图书馆适合发展学习搭子。', 'common', 1),
(3003, 2, 7, '周予的人脉雷达', '周予知道哪个社团正在找人，但她一般不会只说一遍。', '社团区可能触发社交机会。', 'common', 1),
(3004, 2, 6, '老郑今晚在实验室', '老郑今晚在实验室，带着电脑过去可能能救一个 Bug。', '实验室适合提升技能。', 'rare', 1),
(3005, 3, 5, '小马的夜跑建议', '小马说操场晚上风不大，跑完再复习会清醒一点。', '操场适合降压和恢复健康。', 'common', 1),
(3006, 3, 2, '熟人复习不孤独', '据说和熟人一起复习，压力不会凭空消失，但至少不会那么孤独。', '高压周适合找搭子。', 'common', 1),
(3007, 3, 4, '食堂二楼情报局', '食堂二楼是交换情报的地方，周予经常在那里出现。', '食堂可能带来人脉消息。', 'common', 1),
(3008, 3, 6, '老郑的白板建议', '实验室的白板上有老郑留下的数据库设计建议。', '实验室传闻可能帮助课设。', 'rare', 1),
(3009, 4, 5, '体测前的小马提醒', '小马说体测前别只抱佛脚，腿也需要临时抢救。', '期末周健康仍然重要。', 'common', 1),
(3010, 4, 2, '林然的考前抽题', '林然说互相抽题比单纯看书更容易发现漏洞。', '期末前学习搭子很有价值。', 'rare', 1);
```

- [ ] **Step 7: Run seed smoke tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack3Tests test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```powershell
git add src/main/resources/schema.sql src/main/resources/application.yml src/main/resources/data-content-pack-3.sql src/main/java/cn/haut/survivor/domain/entity/NpcInteraction.java src/main/java/cn/haut/survivor/domain/entity/UserNpcWeeklyAction.java src/main/java/cn/haut/survivor/mapper/NpcInteractionMapper.java src/main/java/cn/haut/survivor/mapper/UserNpcWeeklyActionMapper.java src/test/java/cn/haut/survivor/service/ContentPack3Tests.java
git commit -m "feat: seed npc buddy content pack"
```

---

### Task 3: Extend NpcService for Interactions and Weekly Buddy

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/NpcService.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/NpcServiceImpl.java`
- Modify: `src/test/java/cn/haut/survivor/service/NpcServiceTests.java`

- [ ] **Step 1: Add failing service tests**

Append these tests to `src/test/java/cn/haut/survivor/service/NpcServiceTests.java`. Add missing imports shown in the snippet.

```java
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.service.NpcService.NpcInteractionResult;
import java.util.List;
import java.util.Optional;
```

Autowire:

```java
@Autowired
private PlayerService playerService;

@Autowired
private PlayerAttributeMapper playerAttributeMapper;
```

Add tests:

```java
@Test
void listAvailableInteractionsIncludesUnlockedOnly() {
    playerService.createProfile(2L, "NPC互动测试", "大二", "计算机类", "就业路线");
    npcService.increaseFamiliarity(2L, 2L, 25);

    List<NpcInteraction> interactions = npcService.listAvailableInteractions(2L, 2L, 1);

    assertThat(interactions).extracting(NpcInteraction::getInteractionKey)
            .contains("linran_notes", "linran_study")
            .doesNotContain("linran_quiz");
}

@Test
void interactingConsumesActionPointAndAppliesActualAttributeChange() {
    playerService.createProfile(2L, "NPC互动测试", "大二", "计算机类", "就业路线");
    PlayerProfile beforeProfile = playerService.findProfileByUserId(2L);
    PlayerAttribute before = playerService.findAttributeByUserId(2L);

    NpcInteractionResult result = npcService.interact(2L, 2L, 3004L, 1);

    PlayerProfile afterProfile = playerService.findProfileByUserId(2L);
    PlayerAttribute after = playerService.findAttributeByUserId(2L);
    assertThat(afterProfile.getActionPoints()).isEqualTo(beforeProfile.getActionPoints() - 1);
    assertThat(after.getAcademic()).isEqualTo(before.getAcademic() + result.attributeChange().academicChange());
    assertThat(result.familiarityGain()).isGreaterThan(0);
}

@Test
void sameNpcCannotBeInteractedTwiceInOneWeek() {
    playerService.createProfile(2L, "NPC互动测试", "大二", "计算机类", "就业路线");
    npcService.interact(2L, 2L, 3004L, 1);

    assertThatThrownBy(() -> npcService.interact(2L, 2L, 3004L, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("本周已经互动过");
}

@Test
void chooseWeeklyBuddyRequiresFamiliarity50() {
    playerService.createProfile(2L, "NPC互动测试", "大二", "计算机类", "就业路线");

    assertThatThrownBy(() -> npcService.chooseWeeklyBuddy(2L, 2L, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("熟悉度达到 50");
}

@Test
void chooseWeeklyBuddyAllowsOnlyOneBuddyPerWeek() {
    playerService.createProfile(2L, "NPC互动测试", "大二", "计算机类", "就业路线");
    npcService.increaseFamiliarity(2L, 2L, 55);
    npcService.increaseFamiliarity(2L, 4L, 55);

    npcService.chooseWeeklyBuddy(2L, 2L, 1);

    assertThatThrownBy(() -> npcService.chooseWeeklyBuddy(2L, 4L, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("本周已经选择搭子");
}

@Test
void weeklyBuddyBonusAffectsInteractionResult() {
    playerService.createProfile(2L, "NPC互动测试", "大二", "计算机类", "就业路线");
    npcService.increaseFamiliarity(2L, 2L, 55);
    npcService.chooseWeeklyBuddy(2L, 2L, 1);

    NpcInteractionResult result = npcService.interact(2L, 2L, 3004L, 1);

    assertThat(result.attributeChange().academicChange()).isEqualTo(5);
}

@Test
void relationStageDerivedFromFamiliarity() {
    assertThat(npcService.getRelationStage(0)).isEqualTo("认识");
    assertThat(npcService.getRelationStage(20)).isEqualTo("熟人");
    assertThat(npcService.getRelationStage(50)).isEqualTo("搭子");
    assertThat(npcService.getRelationStage(80)).isEqualTo("铁搭子");
}
```

Ensure this static import exists:

```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

- [ ] **Step 2: Run service tests and verify they fail**

Run:

```powershell
.\mvnw.cmd -Dtest=NpcServiceTests test
```

Expected: FAIL because the new API does not exist.

- [ ] **Step 3: Extend `NpcService` interface**

Modify `src/main/java/cn/haut/survivor/service/NpcService.java`:

```java
import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
```

Add methods:

```java
List<NpcInteraction> listAvailableInteractions(Long userId, Long npcId, int weekNumber);

NpcInteractionResult interact(Long userId, Long npcId, Long interactionId, int weekNumber);

Optional<UserNpcWeeklyAction> getCurrentBuddy(Long userId, int weekNumber);

void chooseWeeklyBuddy(Long userId, Long npcId, int weekNumber);

String getRelationStage(Integer familiarity);
```

Add record:

```java
record NpcInteractionResult(
        Npc npc,
        UserNpcRelation relation,
        NpcInteraction interaction,
        AttributeChange attributeChange,
        int familiarityGain,
        String relationStage,
        String resultText
) {}
```

- [ ] **Step 4: Update `NpcServiceImpl` constructor and fields**

Modify imports in `src/main/java/cn/haut/survivor/service/impl/NpcServiceImpl.java`:

```java
import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.NpcInteraction;
import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import cn.haut.survivor.mapper.NpcInteractionMapper;
import cn.haut.survivor.mapper.PlayerAttributeMapper;
import cn.haut.survivor.mapper.UserNpcWeeklyActionMapper;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.PlayerService;
import cn.haut.survivor.service.WeeklyGoalService;
import java.time.LocalDateTime;
```

Add fields:

```java
private final NpcInteractionMapper interactionMapper;
private final UserNpcWeeklyActionMapper weeklyActionMapper;
private final PlayerAttributeMapper playerAttributeMapper;
private final PlayerService playerService;
private final WeeklyGoalService weeklyGoalService;
private final AchievementService achievementService;
```

Update constructor:

```java
public NpcServiceImpl(NpcMapper npcMapper,
                      UserNpcRelationMapper relationMapper,
                      NpcInteractionMapper interactionMapper,
                      UserNpcWeeklyActionMapper weeklyActionMapper,
                      PlayerAttributeMapper playerAttributeMapper,
                      PlayerService playerService,
                      WeeklyGoalService weeklyGoalService,
                      AchievementService achievementService) {
    this.npcMapper = npcMapper;
    this.relationMapper = relationMapper;
    this.interactionMapper = interactionMapper;
    this.weeklyActionMapper = weeklyActionMapper;
    this.playerAttributeMapper = playerAttributeMapper;
    this.playerService = playerService;
    this.weeklyGoalService = weeklyGoalService;
    this.achievementService = achievementService;
}
```

- [ ] **Step 5: Implement relation helpers in `NpcServiceImpl`**

Add these private/public helpers:

```java
@Override
public String getRelationStage(Integer familiarity) {
    int value = familiarity == null ? 0 : familiarity;
    if (value >= 80) return "铁搭子";
    if (value >= 50) return "搭子";
    if (value >= 20) return "熟人";
    return "认识";
}

private UserNpcRelation findRelation(Long userId, Long npcId) {
    return relationMapper.selectOne(new LambdaQueryWrapper<UserNpcRelation>()
            .eq(UserNpcRelation::getUserId, userId)
            .eq(UserNpcRelation::getNpcId, npcId)
            .last("LIMIT 1"));
}

private UserNpcRelation requireRelation(Long userId, Long npcId) {
    UserNpcRelation relation = findRelation(userId, npcId);
    if (relation == null) {
        relation = new UserNpcRelation();
        relation.setUserId(userId);
        relation.setNpcId(npcId);
        relation.setFamiliarity(0);
        relation.setMetCount(0);
        relation.setLastMetWeek(0);
        relationMapper.insert(relation);
    }
    Npc npc = npcMapper.selectById(npcId);
    if (npc != null) {
        relation.setNpc(npc);
    }
    return relation;
}

private UserNpcWeeklyAction findWeeklyAction(Long userId, Long npcId, int weekNumber) {
    return weeklyActionMapper.selectOne(new LambdaQueryWrapper<UserNpcWeeklyAction>()
            .eq(UserNpcWeeklyAction::getUserId, userId)
            .eq(UserNpcWeeklyAction::getNpcId, npcId)
            .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
            .last("LIMIT 1"));
}

private UserNpcWeeklyAction getOrCreateWeeklyAction(Long userId, Long npcId, int weekNumber) {
    UserNpcWeeklyAction action = findWeeklyAction(userId, npcId, weekNumber);
    if (action != null) return action;
    action = new UserNpcWeeklyAction();
    action.setUserId(userId);
    action.setNpcId(npcId);
    action.setWeekNumber(weekNumber);
    action.setInteracted(0);
    action.setBuddySelected(0);
    weeklyActionMapper.insert(action);
    return action;
}

private int clamp(int value) {
    return Math.max(0, Math.min(100, value));
}
```

- [ ] **Step 6: Implement available interactions and buddy methods**

Add:

```java
@Override
public List<NpcInteraction> listAvailableInteractions(Long userId, Long npcId, int weekNumber) {
    UserNpcRelation relation = requireRelation(userId, npcId);
    int familiarity = relation.getFamiliarity() == null ? 0 : relation.getFamiliarity();
    return interactionMapper.selectList(new LambdaQueryWrapper<NpcInteraction>()
            .eq(NpcInteraction::getNpcId, npcId)
            .eq(NpcInteraction::getActive, 1)
            .le(NpcInteraction::getRequiredFamiliarity, familiarity)
            .orderByAsc(NpcInteraction::getRequiredFamiliarity)
            .orderByAsc(NpcInteraction::getId));
}

@Override
public Optional<UserNpcWeeklyAction> getCurrentBuddy(Long userId, int weekNumber) {
    UserNpcWeeklyAction action = weeklyActionMapper.selectOne(new LambdaQueryWrapper<UserNpcWeeklyAction>()
            .eq(UserNpcWeeklyAction::getUserId, userId)
            .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
            .eq(UserNpcWeeklyAction::getBuddySelected, 1)
            .last("LIMIT 1"));
    return Optional.ofNullable(action);
}

@Override
@Transactional
public void chooseWeeklyBuddy(Long userId, Long npcId, int weekNumber) {
    UserNpcRelation relation = requireRelation(userId, npcId);
    if ((relation.getFamiliarity() == null ? 0 : relation.getFamiliarity()) < 50) {
        throw new IllegalArgumentException("熟悉度达到 50 才能设为本周搭子");
    }
    if (getCurrentBuddy(userId, weekNumber).isPresent()) {
        throw new IllegalArgumentException("本周已经选择搭子");
    }
    UserNpcWeeklyAction action = getOrCreateWeeklyAction(userId, npcId, weekNumber);
    action.setBuddySelected(1);
    action.setSelectedAt(LocalDateTime.now());
    weeklyActionMapper.updateById(action);
    weeklyGoalService.updateProgress(userId, weekNumber, "buddy_selected", 1);
}
```

- [ ] **Step 7: Implement `interact()` with actual delta**

Add:

```java
@Override
@Transactional
public NpcInteractionResult interact(Long userId, Long npcId, Long interactionId, int weekNumber) {
    Npc npc = npcMapper.selectById(npcId);
    if (npc == null || npc.getActive() == null || npc.getActive() != 1) {
        throw new IllegalArgumentException("NPC 不存在或已不可用");
    }

    NpcInteraction interaction = interactionMapper.selectById(interactionId);
    if (interaction == null || interaction.getActive() == null || interaction.getActive() != 1
            || !npcId.equals(interaction.getNpcId())) {
        throw new IllegalArgumentException("互动不存在或不属于该 NPC");
    }

    UserNpcRelation relation = requireRelation(userId, npcId);
    int familiarity = relation.getFamiliarity() == null ? 0 : relation.getFamiliarity();
    if (familiarity < interaction.getRequiredFamiliarity()) {
        throw new IllegalArgumentException("熟悉度不足，暂未解锁该互动");
    }

    UserNpcWeeklyAction action = getOrCreateWeeklyAction(userId, npcId, weekNumber);
    if (action.getInteracted() != null && action.getInteracted() == 1) {
        throw new IllegalArgumentException("本周已经互动过");
    }

    playerService.consumeActionPoint(userId);

    PlayerAttribute before = playerAttributeMapper.selectOne(new LambdaQueryWrapper<PlayerAttribute>()
            .eq(PlayerAttribute::getUserId, userId)
            .last("LIMIT 1"));
    if (before == null) {
        throw new IllegalArgumentException("玩家属性不存在");
    }

    int academicDelta = value(interaction.getAcademicChange());
    int healthDelta = value(interaction.getHealthChange());
    int moneyDelta = value(interaction.getMoneyChange());
    int socialDelta = value(interaction.getSocialChange());
    int skillDelta = value(interaction.getSkillChange());
    int pressureDelta = value(interaction.getPressureChange());
    int disciplineDelta = value(interaction.getDisciplineChange());

    Optional<UserNpcWeeklyAction> buddy = getCurrentBuddy(userId, weekNumber);
    if (buddy.isPresent() && npcId.equals(buddy.get().getNpcId())) {
        switch (npc.getFavoriteAttribute()) {
            case "academic" -> academicDelta += 1;
            case "health" -> healthDelta += 1;
            case "social" -> socialDelta += 1;
            case "skill" -> skillDelta += 1;
            case "pressure" -> {
                pressureDelta -= 1;
                if (disciplineDelta > 0) disciplineDelta -= 1;
            }
            case "discipline" -> disciplineDelta += 1;
            default -> { }
        }
    }

    int oldAcademic = before.getAcademic();
    int oldHealth = before.getHealth();
    int oldMoney = before.getMoney();
    int oldSocial = before.getSocial();
    int oldSkill = before.getSkill();
    int oldPressure = before.getPressure();
    int oldDiscipline = before.getDiscipline();

    before.setAcademic(clamp(oldAcademic + academicDelta));
    before.setHealth(clamp(oldHealth + healthDelta));
    before.setMoney(clamp(oldMoney + moneyDelta));
    before.setSocial(clamp(oldSocial + socialDelta));
    before.setSkill(clamp(oldSkill + skillDelta));
    before.setPressure(clamp(oldPressure + pressureDelta));
    before.setDiscipline(clamp(oldDiscipline + disciplineDelta));
    before.setUpdateTime(LocalDateTime.now());
    playerAttributeMapper.updateById(before);

    AttributeChange change = new AttributeChange(
            before.getAcademic() - oldAcademic,
            before.getHealth() - oldHealth,
            before.getMoney() - oldMoney,
            before.getSocial() - oldSocial,
            before.getSkill() - oldSkill,
            before.getPressure() - oldPressure,
            before.getDiscipline() - oldDiscipline,
            value(interaction.getExpChange())
    );

    int familiarityGain = value(interaction.getFamiliarityChange());
    relation.setFamiliarity(clamp((relation.getFamiliarity() == null ? 0 : relation.getFamiliarity()) + familiarityGain));
    relation.setMetCount((relation.getMetCount() == null ? 0 : relation.getMetCount()) + 1);
    relation.setLastMetWeek(weekNumber);
    relationMapper.updateById(relation);
    relation.setNpc(npc);

    action.setInteracted(1);
    action.setInteractedAt(LocalDateTime.now());
    weeklyActionMapper.updateById(action);

    weeklyGoalService.updateProgress(userId, weekNumber, "npc_interaction", 1);
    weeklyGoalService.updateProgress(userId, weekNumber, "familiarity_gain", familiarityGain);
    unlockNpcAchievements(userId, npc, relation);

    return new NpcInteractionResult(
            npc,
            relation,
            interaction,
            change,
            familiarityGain,
            getRelationStage(relation.getFamiliarity()),
            interaction.getResultText()
    );
}

private int value(Integer value) {
    return value == null ? 0 : value;
}
```

- [ ] **Step 8: Update `increaseFamiliarity()` to create missing relations**

Replace the existing `increaseFamiliarity()` method in `NpcServiceImpl` with:

```java
@Override
@Transactional
public void increaseFamiliarity(Long userId, Long npcId, int amount) {
    UserNpcRelation relation = requireRelation(userId, npcId);
    relation.setFamiliarity(clamp((relation.getFamiliarity() == null ? 0 : relation.getFamiliarity()) + amount));
    relationMapper.updateById(relation);
}
```

This keeps old callers working and allows tests to set up relationship stages without relying on random NPC encounters.

- [ ] **Step 9: Implement achievement unlock helper**

Add:

```java
private void unlockNpcAchievements(Long userId, Npc npc, UserNpcRelation relation) {
    int familiarity = relation.getFamiliarity() == null ? 0 : relation.getFamiliarity();
    if (familiarity >= 50) {
        achievementService.unlockAchievement(userId, "first_buddy");
    }
    if (familiarity >= 80) {
        achievementService.unlockAchievement(userId, "iron_buddy");
    }
    long knownCount = relationMapper.selectCount(new LambdaQueryWrapper<UserNpcRelation>()
            .eq(UserNpcRelation::getUserId, userId));
    if (knownCount >= 5) {
        achievementService.unlockAchievement(userId, "social_web");
    }
    if (npc.getId() == 2L && relation.getMetCount() != null && relation.getMetCount() >= 3) {
        achievementService.unlockAchievement(userId, "study_partner");
    }
    if (npc.getId() == 4L && relation.getMetCount() != null && relation.getMetCount() >= 3) {
        achievementService.unlockAchievement(userId, "lab_apprentice");
    }
}
```

- [ ] **Step 10: Run service tests**

Run:

```powershell
.\mvnw.cmd -Dtest=NpcServiceTests test
```

Expected: PASS.

- [ ] **Step 11: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/NpcService.java src/main/java/cn/haut/survivor/service/impl/NpcServiceImpl.java src/test/java/cn/haut/survivor/service/NpcServiceTests.java
git commit -m "feat: add npc interactions and weekly buddy service"
```

---

### Task 4: Add Weekly Goal Support for NPC Buddy Types

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`
- Modify: `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`

- [ ] **Step 1: Add failing goal tests**

Append to `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`:

```java
@Test
void npcInteractionGoalStartsFromZeroAndCompletes() {
    playerService.createProfile(2L, "NPC目标测试", "大二", "计算机类", "就业路线");
    WeeklyGoal goal = weeklyGoalMapper.selectOne(new LambdaQueryWrapper<WeeklyGoal>()
            .eq(WeeklyGoal::getGoalKey, "buddy_chat")
            .last("LIMIT 1"));

    UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, goal.getId());
    assertThat(userGoal.getStartValue()).isZero();
    assertThat(userGoal.getCurrentValue()).isZero();

    weeklyGoalService.updateProgress(2L, 1, "npc_interaction", 1);
    UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
    assertThat(updated.getCurrentValue()).isEqualTo(1);
    assertThat(updated.getCompleted()).isEqualTo(1);
}

@Test
void buddySelectedGoalStartsFromZeroAndCompletes() {
    playerService.createProfile(2L, "搭子目标测试", "大二", "计算机类", "就业路线");
    WeeklyGoal goal = weeklyGoalMapper.selectOne(new LambdaQueryWrapper<WeeklyGoal>()
            .eq(WeeklyGoal::getGoalKey, "buddy_week")
            .last("LIMIT 1"));

    weeklyGoalService.chooseGoal(2L, 1, goal.getId());
    weeklyGoalService.updateProgress(2L, 1, "buddy_selected", 1);

    UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
    assertThat(updated.getCurrentValue()).isEqualTo(1);
    assertThat(updated.getCompleted()).isEqualTo(1);
}

@Test
void familiarityGainGoalStartsFromZeroAndAccumulates() {
    playerService.createProfile(2L, "关系目标测试", "大二", "计算机类", "就业路线");
    WeeklyGoal goal = weeklyGoalMapper.selectOne(new LambdaQueryWrapper<WeeklyGoal>()
            .eq(WeeklyGoal::getGoalKey, "relationship_builder")
            .last("LIMIT 1"));

    weeklyGoalService.chooseGoal(2L, 1, goal.getId());
    weeklyGoalService.updateProgress(2L, 1, "familiarity_gain", 4);
    weeklyGoalService.updateProgress(2L, 1, "familiarity_gain", 6);

    UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
    assertThat(updated.getCurrentValue()).isEqualTo(10);
    assertThat(updated.getCompleted()).isEqualTo(1);
}
```

- [ ] **Step 2: Run the new tests and verify failure**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyGoalServiceTests#npcInteractionGoalStartsFromZeroAndCompletes,WeeklyGoalServiceTests#buddySelectedGoalStartsFromZeroAndCompletes,WeeklyGoalServiceTests#familiarityGainGoalStartsFromZeroAndAccumulates test
```

Expected before implementation: FAIL if these goal types do not start from zero.

- [ ] **Step 3: Add count-based goal types**

In `WeeklyGoalServiceImpl.getStartValueForGoalType()`, add these cases next to existing count-based cases:

```java
case "npc_interaction" -> {
    return 0;
}
case "buddy_selected" -> {
    return 0;
}
case "familiarity_gain" -> {
    return 0;
}
```

- [ ] **Step 4: Run goal tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyGoalServiceTests test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java
git commit -m "feat: support npc buddy weekly goals"
```

---

### Task 5: Add NPC Controller and Pages

**Files:**
- Create: `src/main/java/cn/haut/survivor/controller/NpcController.java`
- Create: `src/main/resources/templates/npc/detail.html`
- Create: `src/main/resources/templates/npc/result.html`
- Create: `src/test/java/cn/haut/survivor/controller/NpcControllerTests.java`

- [ ] **Step 1: Add controller tests**

Create `src/test/java/cn/haut/survivor/controller/NpcControllerTests.java`:

```java
package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class NpcControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private NpcService npcService;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "NPC页面玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void npcDetailPageShowsNpcAndInteractions() throws Exception {
        mockMvc.perform(get("/npcs/2")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("npc/detail"))
                .andExpect(model().attributeExists("npc", "relation", "relationStage", "interactions", "currentBuddy"));
    }

    @Test
    void npcDetailRedirectsWithoutProfile() throws Exception {
        mockMvc.perform(get("/npcs/2")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 99L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/player/create"));
    }

    @Test
    void interactingShowsResultPage() throws Exception {
        mockMvc.perform(post("/npcs/2/interactions/3004")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("npc/result"))
                .andExpect(model().attributeExists("result", "attribute", "profile"));
    }

    @Test
    void chooseBuddyRedirectsToNpcDetail() throws Exception {
        npcService.increaseFamiliarity(2L, 2L, 55);

        mockMvc.perform(post("/npcs/2/buddy")
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                        .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/npcs/2"));
    }
}
```

- [ ] **Step 2: Run controller tests and verify failure**

Run:

```powershell
.\mvnw.cmd -Dtest=NpcControllerTests test
```

Expected: FAIL because `NpcController` and templates do not exist.

- [ ] **Step 3: Create `NpcController`**

Create `src/main/java/cn/haut/survivor/controller/NpcController.java`:

```java
package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.Npc;
import cn.haut.survivor.domain.entity.UserNpcRelation;
import cn.haut.survivor.service.NpcService;
import cn.haut.survivor.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NpcController {

    private final NpcService npcService;
    private final PlayerService playerService;

    public NpcController(NpcService npcService, PlayerService playerService) {
        this.npcService = npcService;
        this.playerService = playerService;
    }

    @GetMapping("/npcs/{npcId}")
    public String detail(@PathVariable Long npcId, HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        Npc npc = npcService.listActiveNpcs().stream()
                .filter(n -> n.getId().equals(npcId))
                .findFirst()
                .orElse(null);
        if (npc == null) {
            return "redirect:/dashboard";
        }

        UserNpcRelation relation = npcService.listKnownNpcs(userId).stream()
                .filter(r -> r.getNpcId().equals(npcId))
                .findFirst()
                .orElseGet(() -> {
                    UserNpcRelation r = new UserNpcRelation();
                    r.setUserId(userId);
                    r.setNpcId(npcId);
                    r.setFamiliarity(0);
                    r.setMetCount(0);
                    r.setLastMetWeek(0);
                    r.setNpc(npc);
                    return r;
                });

        int week = playerService.findProfileByUserId(userId).getCurrentWeek();
        model.addAttribute("npc", npc);
        model.addAttribute("relation", relation);
        model.addAttribute("relationStage", npcService.getRelationStage(relation.getFamiliarity()));
        model.addAttribute("interactions", npcService.listAvailableInteractions(userId, npcId, week));
        model.addAttribute("currentBuddy", npcService.getCurrentBuddy(userId, week).orElse(null));
        model.addAttribute("profile", playerService.findProfileByUserId(userId));
        model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
        return "npc/detail";
    }

    @PostMapping("/npcs/{npcId}/interactions/{interactionId}")
    public String interact(@PathVariable Long npcId, @PathVariable Long interactionId,
                           HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        int week = playerService.findProfileByUserId(userId).getCurrentWeek();
        try {
            var result = npcService.interact(userId, npcId, interactionId, week);
            model.addAttribute("result", result);
            model.addAttribute("attribute", playerService.findAttributeByUserId(userId));
            model.addAttribute("profile", playerService.findProfileByUserId(userId));
            return "npc/result";
        } catch (IllegalArgumentException e) {
            return "redirect:/npcs/" + npcId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/npcs/{npcId}/buddy")
    public String chooseBuddy(@PathVariable Long npcId, HttpSession session) {
        Long userId = currentUserId(session);
        if (!playerService.hasProfile(userId)) {
            return "redirect:/player/create";
        }

        int week = playerService.findProfileByUserId(userId).getCurrentWeek();
        try {
            npcService.chooseWeeklyBuddy(userId, npcId, week);
        } catch (IllegalArgumentException ignored) {
            // detail page will still show the current state
        }
        return "redirect:/npcs/" + npcId;
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(LoginInterceptor.LOGIN_USER_ID);
    }
}
```

- [ ] **Step 4: Create NPC detail template**

Create `src/main/resources/templates/npc/detail.html`:

```html
<!doctype html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title th:text="${npc.npcName} + ' - 校园搭子'">校园搭子</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/app.css" rel="stylesheet">
</head>
<body class="game-shell">
<div th:replace="~{layout :: game-hud}"></div>

<main class="npc-page">
    <section class="npc-profile-card">
        <div class="npc-profile-card__avatar" th:text="${npc.avatarIcon}">👤</div>
        <div class="npc-profile-card__body">
            <p class="npc-profile-card__type" th:text="${npc.npcType}">类型</p>
            <h1 class="npc-profile-card__name" th:text="${npc.npcName}">NPC</h1>
            <p class="npc-profile-card__desc" th:text="${npc.description}">描述</p>
            <div class="npc-profile-card__meta">
                <span th:text="'熟悉度 ' + ${relation.familiarity}">熟悉度 0</span>
                <span th:text="${relationStage}">认识</span>
                <span th:if="${currentBuddy != null and currentBuddy.npcId == npc.id}">本周搭子</span>
            </div>
        </div>
    </section>

    <section class="npc-actions-panel">
        <div class="npc-actions-panel__header">
            <h2>找 TA 做点什么</h2>
            <form method="post" th:action="@{/npcs/{id}/buddy(id=${npc.id})}">
                <button type="submit" class="sticky-note__btn"
                        th:disabled="${relation.familiarity < 50 or currentBuddy != null}">
                    设为本周搭子
                </button>
            </form>
        </div>

        <div class="npc-interaction-list">
            <article class="npc-interaction-card" th:each="interaction : ${interactions}">
                <div>
                    <h3 th:text="${interaction.interactionName}">互动</h3>
                    <p th:text="${interaction.description}">描述</p>
                    <small th:text="'需要熟悉度 ' + ${interaction.requiredFamiliarity}">需要熟悉度 0</small>
                </div>
                <form method="post" th:action="@{/npcs/{npcId}/interactions/{interactionId}(npcId=${npc.id}, interactionId=${interaction.id})}">
                    <button type="submit" class="sticky-note__btn sticky-note__btn--primary">开始</button>
                </form>
            </article>
        </div>
    </section>
</main>

<div th:replace="~{layout :: game-dock}"></div>
</body>
</html>
```

- [ ] **Step 5: Create NPC result template**

Create `src/main/resources/templates/npc/result.html`:

```html
<!doctype html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>搭子互动 - HAUT Survivor</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/app.css" rel="stylesheet">
</head>
<body class="game-shell">
<div th:replace="~{layout :: game-hud}"></div>

<main class="vn-stage">
    <section class="vn-result">
        <div class="vn-result__header">
            <span class="vn-result__eyebrow">校园搭子</span>
            <h1 class="vn-result__title" th:text="${result.interaction.interactionName}">互动结果</h1>
        </div>

        <p class="vn-result__story" th:text="${result.resultText}">互动结果文案</p>

        <div class="vn-result__changes">
            <p class="vn-result__changes-label">状态变化</p>
            <div class="vn-result__changes-list">
                <span th:if="${result.attributeChange.academicChange != 0}" class="vn-result__change"
                      th:classappend="${result.attributeChange.academicChange > 0} ? ' vn-up' : ' vn-down'">
                    学业 <em th:text="${result.attributeChange.academicChange > 0 ? '+' + result.attributeChange.academicChange : result.attributeChange.academicChange}">+1</em>
                </span>
                <span th:if="${result.attributeChange.healthChange != 0}" class="vn-result__change"
                      th:classappend="${result.attributeChange.healthChange > 0} ? ' vn-up' : ' vn-down'">
                    健康 <em th:text="${result.attributeChange.healthChange > 0 ? '+' + result.attributeChange.healthChange : result.attributeChange.healthChange}">+1</em>
                </span>
                <span th:if="${result.attributeChange.socialChange != 0}" class="vn-result__change"
                      th:classappend="${result.attributeChange.socialChange > 0} ? ' vn-up' : ' vn-down'">
                    社交 <em th:text="${result.attributeChange.socialChange > 0 ? '+' + result.attributeChange.socialChange : result.attributeChange.socialChange}">+1</em>
                </span>
                <span th:if="${result.attributeChange.skillChange != 0}" class="vn-result__change"
                      th:classappend="${result.attributeChange.skillChange > 0} ? ' vn-up' : ' vn-down'">
                    技能 <em th:text="${result.attributeChange.skillChange > 0 ? '+' + result.attributeChange.skillChange : result.attributeChange.skillChange}">+1</em>
                </span>
                <span th:if="${result.attributeChange.pressureChange != 0}" class="vn-result__change"
                      th:classappend="${result.attributeChange.pressureChange < 0} ? ' vn-up' : ' vn-down'">
                    压力 <em th:text="${result.attributeChange.pressureChange > 0 ? '+' + result.attributeChange.pressureChange : result.attributeChange.pressureChange}">-1</em>
                </span>
                <span th:if="${result.attributeChange.disciplineChange != 0}" class="vn-result__change"
                      th:classappend="${result.attributeChange.disciplineChange > 0} ? ' vn-up' : ' vn-down'">
                    自律 <em th:text="${result.attributeChange.disciplineChange > 0 ? '+' + result.attributeChange.disciplineChange : result.attributeChange.disciplineChange}">+1</em>
                </span>
                <span th:if="${result.familiarityGain > 0}" class="vn-result__change vn-up">
                    熟悉度 <em th:text="'+' + ${result.familiarityGain}">+4</em>
                </span>
            </div>
        </div>

        <div class="vn-result__stats">
            <div><span>关系阶段</span><strong th:text="${result.relationStage}">认识</strong></div>
            <div><span>熟悉度</span><strong th:text="${result.relation.familiarity}">0</strong></div>
            <div><span>行动点</span><strong th:text="${profile.actionPoints}">3</strong></div>
        </div>

        <div class="vn-result__actions">
            <a th:href="@{/npcs/{id}(id=${result.npc.id})}" class="sticky-note__btn sticky-note__btn--primary">继续找 TA</a>
            <a href="/dashboard" class="sticky-note__btn">回寝室</a>
        </div>
    </section>
</main>

<div th:replace="~{layout :: game-dock}"></div>
</body>
</html>
```

- [ ] **Step 6: Run controller tests**

Run:

```powershell
.\mvnw.cmd -Dtest=NpcControllerTests test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/cn/haut/survivor/controller/NpcController.java src/main/resources/templates/npc/detail.html src/main/resources/templates/npc/result.html src/test/java/cn/haut/survivor/controller/NpcControllerTests.java
git commit -m "feat: add npc buddy pages"
```

---

### Task 6: Upgrade Dashboard NPC Strip and Styles

**Files:**
- Modify: `src/main/java/cn/haut/survivor/controller/DashboardController.java`
- Modify: `src/main/resources/templates/dashboard/index.html`
- Modify: `src/main/resources/static/css/app.css`
- Modify: `src/test/java/cn/haut/survivor/controller/DashboardControllerTests.java`

- [ ] **Step 1: Add current buddy to DashboardController**

In `DashboardController.dashboard()`, after `knownNpcs` model attribute, add:

```java
model.addAttribute("currentBuddy", npcService.getCurrentBuddy(userId, profile.getCurrentWeek()).orElse(null));
```

- [ ] **Step 2: Update dashboard NPC strip**

In `src/main/resources/templates/dashboard/index.html`, replace the existing `npc-strip` article body with this structure:

```html
<section th:if="${knownNpcs != null and !knownNpcs.isEmpty()}" class="npc-strip">
    <p class="npc-strip__title">👥 校园熟人</p>
    <div class="npc-strip__list">
        <a class="npc-chip npc-chip--link" th:each="rel : ${knownNpcs}" th:if="${rel.npc != null}"
           th:href="@{/npcs/{id}(id=${rel.npc.id})}">
            <span class="npc-chip__avatar" th:text="${rel.npc.avatarIcon}">👤</span>
            <div class="npc-chip__info">
                <span class="npc-chip__name" th:text="${rel.npc.npcName}">NPC名</span>
                <span class="npc-chip__familiarity" th:text="'熟悉 ' + ${rel.familiarity}">熟悉 10</span>
                <span class="npc-chip__stage"
                      th:text="${rel.familiarity >= 80 ? '铁搭子' : (rel.familiarity >= 50 ? '搭子' : (rel.familiarity >= 20 ? '熟人' : '认识'))}">
                    认识
                </span>
                <span class="npc-chip__buddy"
                      th:if="${currentBuddy != null and currentBuddy.npcId == rel.npcId}">本周搭子</span>
            </div>
        </a>
    </div>
</section>
```

- [ ] **Step 3: Add CSS**

Append to `src/main/resources/static/css/app.css`:

```css
.npc-chip--link {
    text-decoration: none;
    color: inherit;
    transition: transform 160ms ease, border-color 160ms ease;
}

.npc-chip--link:hover {
    transform: translateY(-2px);
    border-color: rgba(255, 255, 255, 0.22);
}

.npc-chip__stage,
.npc-chip__buddy {
    display: inline-flex;
    width: fit-content;
    padding: 2px 7px;
    border-radius: 999px;
    font-size: 0.72rem;
    background: rgba(255, 255, 255, 0.1);
    color: rgba(255, 255, 255, 0.78);
}

.npc-chip__buddy {
    background: rgba(34, 197, 94, 0.2);
    color: #bbf7d0;
}

.npc-page {
    width: min(960px, calc(100% - 32px));
    margin: 0 auto;
    padding: 24px 0 96px;
}

.npc-profile-card,
.npc-actions-panel {
    border: 1px solid rgba(255, 255, 255, 0.12);
    background: rgba(15, 23, 42, 0.72);
    border-radius: 8px;
    padding: 18px;
}

.npc-profile-card {
    display: grid;
    grid-template-columns: 96px 1fr;
    gap: 18px;
    align-items: center;
    margin-bottom: 16px;
}

.npc-profile-card__avatar {
    width: 88px;
    height: 88px;
    display: grid;
    place-items: center;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.1);
    font-size: 2.4rem;
}

.npc-profile-card__type,
.npc-profile-card__desc {
    color: rgba(255, 255, 255, 0.72);
    margin: 0;
}

.npc-profile-card__name {
    color: #fff;
    font-size: 1.8rem;
    margin: 4px 0 8px;
}

.npc-profile-card__meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 12px;
}

.npc-profile-card__meta span {
    padding: 4px 9px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.1);
    color: rgba(255, 255, 255, 0.82);
    font-size: 0.82rem;
}

.npc-actions-panel__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;
}

.npc-actions-panel__header h2 {
    color: #fff;
    font-size: 1.2rem;
    margin: 0;
}

.npc-interaction-list {
    display: grid;
    gap: 12px;
}

.npc-interaction-card {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 12px;
    align-items: center;
    padding: 14px;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.08);
}

.npc-interaction-card h3 {
    color: #fff;
    font-size: 1rem;
    margin: 0 0 6px;
}

.npc-interaction-card p,
.npc-interaction-card small {
    color: rgba(255, 255, 255, 0.7);
    margin: 0;
}

@media (max-width: 640px) {
    .npc-page {
        width: min(100% - 20px, 560px);
        padding-bottom: 128px;
    }

    .npc-profile-card,
    .npc-interaction-card,
    .npc-actions-panel__header {
        grid-template-columns: 1fr;
    }

    .npc-profile-card__avatar {
        width: 72px;
        height: 72px;
        font-size: 2rem;
    }
}
```

- [ ] **Step 4: Update dashboard controller tests**

In `DashboardControllerTests`, add assertion to the test that checks dashboard model:

```java
.andExpect(model().attributeExists("currentBuddy"))
```

- [ ] **Step 5: Run dashboard tests**

Run:

```powershell
.\mvnw.cmd -Dtest=DashboardControllerTests,NpcControllerTests test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/controller/DashboardController.java src/main/resources/templates/dashboard/index.html src/main/resources/static/css/app.css src/test/java/cn/haut/survivor/controller/DashboardControllerTests.java
git commit -m "feat: show npc buddy state on dashboard"
```

---

### Task 7: Add Week Summary Buddy Feedback

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`
- Modify: `src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java`

- [ ] **Step 1: Add failing summary tests**

Append to `WeekSummaryServiceTests`:

```java
@Autowired
private NpcService npcService;
```

If already present, do not duplicate it.

Add tests:

```java
@Test
void summaryMentionsWeeklyBuddyWhenSelected() {
    playerService.createProfile(2L, "搭子总结测试", "大二", "计算机类", "就业路线");
    npcService.increaseFamiliarity(2L, 2L, 55);
    npcService.chooseWeeklyBuddy(2L, 2L, 1);

    WeekSummaryView view = weekSummaryService.buildCurrentWeekSummary(2L, 1);

    assertThat(view.summaryText()).contains("搭子");
}

@Test
void summaryWarnsWhenHighPressureAndNoNpcInteraction() {
    playerService.createProfile(2L, "单机总结测试", "大二", "计算机类", "就业路线");
    var attr = playerService.findAttributeByUserId(2L);
    attr.setPressure(82);
    playerAttributeMapper.updateById(attr);

    WeekSummaryView view = weekSummaryService.buildCurrentWeekSummary(2L, 1);

    assertThat(view.summaryText()).contains("硬撑");
}
```

Use the existing `playerAttributeMapper` field from current tests.

- [ ] **Step 2: Run summary tests and verify failure**

Run:

```powershell
.\mvnw.cmd -Dtest=WeekSummaryServiceTests#summaryMentionsWeeklyBuddyWhenSelected,WeekSummaryServiceTests#summaryWarnsWhenHighPressureAndNoNpcInteraction test
```

Expected: FAIL because the summary does not check weekly buddy state yet.

- [ ] **Step 3: Inject weekly action mapper and NPC mapper if needed**

In `WeekSummaryServiceImpl`, add field:

```java
private final UserNpcWeeklyActionMapper userNpcWeeklyActionMapper;
```

Add constructor parameter before `WeeklyThemeService weeklyThemeService`:

```java
UserNpcWeeklyActionMapper userNpcWeeklyActionMapper,
```

Assign:

```java
this.userNpcWeeklyActionMapper = userNpcWeeklyActionMapper;
```

Add import:

```java
import cn.haut.survivor.domain.entity.UserNpcWeeklyAction;
import cn.haut.survivor.mapper.UserNpcWeeklyActionMapper;
```

- [ ] **Step 4: Pass buddy context into summary generation**

In `buildCurrentWeekSummary()`, after `recentNpcNames` is built, add:

```java
UserNpcWeeklyAction currentBuddy = userNpcWeeklyActionMapper.selectOne(
        new LambdaQueryWrapper<UserNpcWeeklyAction>()
                .eq(UserNpcWeeklyAction::getUserId, userId)
                .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
                .eq(UserNpcWeeklyAction::getBuddySelected, 1)
                .last("LIMIT 1"));

boolean hasNpcInteractionThisWeek = userNpcWeeklyActionMapper.selectCount(
        new LambdaQueryWrapper<UserNpcWeeklyAction>()
                .eq(UserNpcWeeklyAction::getUserId, userId)
                .eq(UserNpcWeeklyAction::getWeekNumber, weekNumber)
                .eq(UserNpcWeeklyAction::getInteracted, 1)) > 0;
```

Replace:

```java
String summaryText = generateSummaryText(attribute, goalCompleted, goalClaimed, knownNpcCount, weekNumber);
```

with:

```java
String summaryText = generateSummaryText(attribute, goalCompleted, goalClaimed, knownNpcCount,
        weekNumber, currentBuddy != null, hasNpcInteractionThisWeek);
```

Change signature:

```java
private String generateSummaryText(PlayerAttribute attr, boolean goalCompleted, boolean goalClaimed, int npcCount,
                                   int weekNumber, boolean hasWeeklyBuddy, boolean hasNpcInteractionThisWeek) {
```

At the top after local variables, before existing academic-crisis branches, add:

```java
if (hasWeeklyBuddy && goalCompleted) {
    return "这周你不是一个人硬扛，校园搭子把你的节奏拉住了。";
}
if (hasWeeklyBuddy) {
    return "这周你选了一个搭子，虽然计划未必完美，但至少不是单机求生。";
}
if (highPressure && !hasNpcInteractionThisWeek) {
    return "这周你几乎全靠自己硬撑。下周也许该找个人一起扛。";
}
if (hasNpcInteractionThisWeek && !goalCompleted) {
    return "你这周和校园里的人有了更多连接，虽然目标没完全拿下，但已经不算单机。";
}
```

- [ ] **Step 5: Run summary tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeekSummaryServiceTests test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java
git commit -m "feat: add npc buddy week summaries"
```

---

### Task 8: Final Verification and Smoke Test

**Files:**
- No planned edits unless verification finds issues.

- [ ] **Step 1: Run full test suite**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
Failures: 0
Errors: 0
BUILD SUCCESS
```

- [ ] **Step 2: Start current code on a clean port**

Use port `8081` to avoid accidentally testing an old app on `8080`:

```powershell
$out = Join-Path $PWD 'target\spring-boot-8081.out.log'
$err = Join-Path $PWD 'target\spring-boot-8081.err.log'
if (Test-Path $out) { Remove-Item -LiteralPath $out -Force }
if (Test-Path $err) { Remove-Item -LiteralPath $err -Force }
Start-Process -FilePath '.\mvnw.cmd' `
  -ArgumentList @('spring-boot:run','-Dspring-boot.run.arguments=--server.port=8081') `
  -WorkingDirectory $PWD `
  -RedirectStandardOutput $out `
  -RedirectStandardError $err `
  -WindowStyle Hidden
```

Poll:

```powershell
for ($i=0; $i -lt 30; $i++) {
  try {
    $r = Invoke-WebRequest -Uri http://localhost:8081/login -UseBasicParsing -TimeoutSec 2
    if ($r.StatusCode -eq 200) { 'UP'; break }
  } catch { Start-Sleep -Seconds 1 }
}
```

- [ ] **Step 3: HTTP smoke pages**

Run:

```powershell
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-WebRequest -Uri http://localhost:8081/login -Method Post -Body @{username='student'; password='student123'} -WebSession $session -UseBasicParsing -MaximumRedirection 5 | Out-Null
Invoke-WebRequest -Uri http://localhost:8081/player/create -Method Post -Body @{playerName='Buddy Smoke'; grade='大二'; majorType='计算机类'; growthRoute='就业路线'} -WebSession $session -UseBasicParsing -MaximumRedirection 5 | Out-Null

$pages = @(
  'http://localhost:8081/dashboard',
  'http://localhost:8081/npcs/1',
  'http://localhost:8081/npcs/2',
  'http://localhost:8081/week/summary'
)

foreach ($url in $pages) {
  $r = Invoke-WebRequest -Uri $url -WebSession $session -UseBasicParsing -MaximumRedirection 5
  $body = [string]$r.Content
  Write-Output ("{0}: status={1}; whitelabel={2}; dock={3}" -f $url, $r.StatusCode, $body.Contains('Whitelabel'), $body.Contains('game-dock'))
}
```

Expected:

- Each page status is `200`.
- `whitelabel=False`.
- `dock=True`.

- [ ] **Step 4: HTTP smoke one interaction**

Run:

```powershell
$r = Invoke-WebRequest -Uri http://localhost:8081/npcs/2/interactions/3004 -Method Post -WebSession $session -UseBasicParsing -MaximumRedirection 5
$body = [string]$r.Content
Write-Output ("interaction: status={0}; whitelabel={1}; result={2}" -f $r.StatusCode, $body.Contains('Whitelabel'), $body.Contains('搭子互动'))
```

Expected:

```text
interaction: status=200; whitelabel=False; result=True
```

- [ ] **Step 5: Stop the temporary server**

Run:

```powershell
$lines = netstat -ano | Select-String ':8081\s'
$pids = @()
foreach ($line in $lines) {
  $parts = ($line.ToString() -split '\s+') | Where-Object { $_ -ne '' }
  if ($parts.Length -ge 5) { $pids += $parts[-1] }
}
$pids = $pids | Sort-Object -Unique | Where-Object { $_ -ne '0' }
foreach ($processId in $pids) {
  Stop-Process -Id ([int]$processId) -Force
}
```

- [ ] **Step 6: Commit fixes if smoke found issues**

If verification required fixes:

```powershell
git status --short
git add src/main/java src/main/resources src/test/java
git commit -m "fix: stabilize npc buddy content pack"
```

- [ ] **Step 7: Final report**

Report:

- Modified files.
- Schema changes.
- Seed data counts.
- New tests.
- Full test result.
- Smoke result.
- Remaining risks.

Do not claim completion unless `.\mvnw.cmd clean test` was run after the final code change and showed zero failures/errors.

---

## Plan Self-Review

Spec coverage:

- Active NPC interaction: Task 3 service and Task 5 controller/pages.
- Weekly buddy: Task 3 service, Task 6 dashboard, Task 7 summary.
- Relationship stages: Task 3 `getRelationStage()`, Task 6 dashboard display.
- Seed interactions: Task 2 creates 15 interactions.
- Weekly goals: Task 2 seeds goals, Task 4 wires goal types, Task 3 updates progress.
- Achievements: Task 2 seeds achievements, Task 3 unlocks them.
- Rumors: Task 2 seeds 10 rumors.
- Week summary: Task 7.
- Tests: Tasks 1, 3, 4, 5, 6, 7, 8.

Scope decisions:

- No love system, no free chat, no gifts, no large story tree.
- Buddy bonuses only apply to NPC interactions in MVP.
- Service-level validation enforces one weekly buddy instead of a complex database partial unique index.
