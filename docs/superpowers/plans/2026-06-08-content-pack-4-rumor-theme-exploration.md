# Content Pack 4 Rumor Theme Exploration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Mechanize rumors, weekly themes, exploration story chains, and light buddy assists so exploration feels like a living campus system instead of isolated random rolls.

**Architecture:** Extend the existing exploration flow instead of creating a parallel gameplay loop. Add lightweight seed/schema support, then layer deterministic services (`WeeklyModifierService`, `RumorEffectService`, `ExplorationStoryService`) into `ExplorationService.explore()` and expose their results through the existing exploration result page and week summary.

**Tech Stack:** Java 17, Spring Boot 3.3.5, MyBatis-Plus, MySQL seed data, Thymeleaf, JUnit 5, AssertJ, MockMvc.

---

## File Structure

Create:

- `src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryChain.java`
- `src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryProgress.java`
- `src/main/java/cn/haut/survivor/domain/entity/ExplorationInfluence.java`
- `src/main/java/cn/haut/survivor/mapper/ExplorationStoryChainMapper.java`
- `src/main/java/cn/haut/survivor/mapper/ExplorationStoryProgressMapper.java`
- `src/main/java/cn/haut/survivor/service/WeeklyModifierService.java`
- `src/main/java/cn/haut/survivor/service/RumorEffectService.java`
- `src/main/java/cn/haut/survivor/service/ExplorationStoryService.java`
- `src/main/java/cn/haut/survivor/service/impl/RumorEffectServiceImpl.java`
- `src/main/java/cn/haut/survivor/service/impl/ExplorationStoryServiceImpl.java`
- `src/main/resources/data-content-pack-4.sql`
- `src/test/java/cn/haut/survivor/service/ContentPack4Tests.java`
- `src/test/java/cn/haut/survivor/service/WeeklyModifierServiceTests.java`
- `src/test/java/cn/haut/survivor/service/RumorEffectServiceTests.java`
- `src/test/java/cn/haut/survivor/service/ExplorationStoryServiceTests.java`

Modify:

- `src/main/resources/schema.sql`
- `src/main/resources/application.yml`
- `src/main/java/cn/haut/survivor/domain/entity/Rumor.java`
- `src/main/java/cn/haut/survivor/service/RumorService.java`
- `src/main/java/cn/haut/survivor/service/ExplorationService.java`
- `src/main/java/cn/haut/survivor/service/impl/ExplorationServiceImpl.java`
- `src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`
- `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`
- `src/main/java/cn/haut/survivor/controller/ExplorationController.java`
- `src/main/java/cn/haut/survivor/controller/MapController.java`
- `src/main/resources/templates/exploration/index.html`
- `src/main/resources/templates/exploration/result.html`
- `src/main/resources/templates/map/index.html`
- `src/main/resources/templates/week/summary.html`
- `src/main/resources/static/css/app.css`
- `src/test/java/cn/haut/survivor/service/ExplorationServiceTests.java`
- `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`
- `src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java`
- `src/test/java/cn/haut/survivor/controller/DashboardControllerTests.java`
- `src/test/java/cn/haut/survivor/controller/WeekSummaryControllerTests.java`

---

### Task 1: Add Schema, Entities, Mappers, and Seed Smoke Tests

**Files:**
- Modify: `src/main/resources/schema.sql`
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/cn/haut/survivor/domain/entity/Rumor.java`
- Create: `src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryChain.java`
- Create: `src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryProgress.java`
- Create: `src/main/java/cn/haut/survivor/mapper/ExplorationStoryChainMapper.java`
- Create: `src/main/java/cn/haut/survivor/mapper/ExplorationStoryProgressMapper.java`
- Create: `src/main/resources/data-content-pack-4.sql`
- Create: `src/test/java/cn/haut/survivor/service/ContentPack4Tests.java`

- [ ] **Step 1: Write failing seed smoke tests**

Create `src/test/java/cn/haut/survivor/service/ContentPack4Tests.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.Achievement;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.domain.entity.WeeklyGoal;
import cn.haut.survivor.mapper.AchievementMapper;
import cn.haut.survivor.mapper.ExplorationStoryChainMapper;
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
class ContentPack4Tests {

    @Autowired
    private RumorMapper rumorMapper;

    @Autowired
    private ExplorationStoryChainMapper storyChainMapper;

    @Autowired
    private WeeklyGoalMapper weeklyGoalMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Test
    void mechanicRumorsAreSeeded() {
        List<Rumor> rumors = rumorMapper.selectList(new LambdaQueryWrapper<Rumor>()
                .ge(Rumor::getId, 4001L)
                .le(Rumor::getId, 4016L));

        assertThat(rumors).hasSize(16);
        assertThat(rumors).extracting(Rumor::getEffectType)
                .contains("explore_bonus", "attr_bonus", "npc_boost", "safe_zone");
        assertThat(rumors).allMatch(r -> r.getEffectValue() != null);
    }

    @Test
    void explorationStoryChainsAreSeeded() {
        List<ExplorationStoryChain> chains = storyChainMapper.selectList(
                new LambdaQueryWrapper<ExplorationStoryChain>().eq(ExplorationStoryChain::getActive, 1));

        assertThat(chains).hasSizeGreaterThanOrEqualTo(12);
        assertThat(chains).extracting(ExplorationStoryChain::getChainKey)
                .contains("library_seat", "lab_whiteboard", "track_night_run", "canteen_gossip", "dorm_lights_out");
    }

    @Test
    void contentPack4WeeklyGoalsAreSeeded() {
        List<WeeklyGoal> goals = weeklyGoalMapper.selectList(new LambdaQueryWrapper<WeeklyGoal>()
                .in(WeeklyGoal::getGoalKey,
                        "rumor_hunter", "story_chaser", "theme_survivor", "buddy_rescue"));

        assertThat(goals).hasSize(4);
        assertThat(goals).extracting(WeeklyGoal::getGoalType)
                .contains("rumor_effect_used", "exploration_story_step", "weekly_modifier_used", "buddy_assist");
    }

    @Test
    void contentPack4AchievementsAreSeeded() {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .in(Achievement::getAchievementKey,
                        "first_rumor_effect", "story_first_step", "story_completed", "theme_master", "buddy_saved_me"));

        assertThat(achievements).hasSize(5);
        assertThat(achievements).extracting(Achievement::getRewardTitle)
                .contains("情报新生", "校园目击者", "支线清理大师", "节奏感选手", "有人罩着");
    }
}
```

- [ ] **Step 2: Run the failing test**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack4Tests test
```

Expected: FAIL because `ExplorationStoryChain` and its mapper do not exist yet.

- [ ] **Step 3: Update schema drops**

In `src/main/resources/schema.sql`, add these drops before `DROP TABLE IF EXISTS user_location_exploration;` or before other exploration-related user tables:

```sql
DROP TABLE IF EXISTS exploration_story_progress;
DROP TABLE IF EXISTS exploration_story_chain;
```

- [ ] **Step 4: Extend the `rumor` table**

In `src/main/resources/schema.sql`, update `CREATE TABLE IF NOT EXISTS rumor` to:

```sql
CREATE TABLE IF NOT EXISTS rumor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_number INT NOT NULL,
    location_id BIGINT,
    rumor_title VARCHAR(100) NOT NULL,
    rumor_text TEXT NOT NULL,
    effect_hint VARCHAR(100),
    effect_type VARCHAR(50),
    effect_value INT NOT NULL DEFAULT 0,
    effect_target VARCHAR(50),
    rarity VARCHAR(20) NOT NULL DEFAULT 'common',
    active INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 5: Add exploration story tables**

In `src/main/resources/schema.sql`, place these after `user_location_exploration` and before `rumor`:

```sql
CREATE TABLE IF NOT EXISTS exploration_story_chain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_key VARCHAR(80) NOT NULL,
    chain_name VARCHAR(100) NOT NULL,
    location_id BIGINT NOT NULL,
    week_number INT NOT NULL DEFAULT 0,
    required_explore_level INT NOT NULL DEFAULT 0,
    step_number INT NOT NULL,
    scenario_text TEXT,
    result_text TEXT,
    academic_change INT NOT NULL DEFAULT 0,
    health_change INT NOT NULL DEFAULT 0,
    money_change INT NOT NULL DEFAULT 0,
    social_change INT NOT NULL DEFAULT 0,
    skill_change INT NOT NULL DEFAULT 0,
    pressure_change INT NOT NULL DEFAULT 0,
    discipline_change INT NOT NULL DEFAULT 0,
    exp_change INT NOT NULL DEFAULT 0,
    next_step_number INT,
    active INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_chain_step (chain_key, step_number),
    CONSTRAINT fk_esc_location FOREIGN KEY (location_id) REFERENCES campus_location(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exploration_story_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    chain_key VARCHAR(80) NOT NULL,
    current_step INT NOT NULL DEFAULT 1,
    completed INT NOT NULL DEFAULT 0,
    last_trigger_week INT,
    update_time DATETIME,
    UNIQUE KEY uk_user_chain (user_id, chain_key),
    CONSTRAINT fk_esp_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 6: Extend `Rumor` entity**

In `src/main/java/cn/haut/survivor/domain/entity/Rumor.java`, add fields after `effectHint`:

```java
private String effectType;
private Integer effectValue;
private String effectTarget;
```

- [ ] **Step 7: Create story entities**

Create `src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryChain.java`:

```java
package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exploration_story_chain")
public class ExplorationStoryChain {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String chainKey;
    private String chainName;
    private Long locationId;
    private Integer weekNumber;
    private Integer requiredExploreLevel;
    private Integer stepNumber;
    private String scenarioText;
    private String resultText;
    private Integer academicChange;
    private Integer healthChange;
    private Integer moneyChange;
    private Integer socialChange;
    private Integer skillChange;
    private Integer pressureChange;
    private Integer disciplineChange;
    private Integer expChange;
    private Integer nextStepNumber;
    private Integer active;
}
```

Create `src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryProgress.java`:

```java
package cn.haut.survivor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exploration_story_progress")
public class ExplorationStoryProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String chainKey;
    private Integer currentStep;
    private Integer completed;
    private Integer lastTriggerWeek;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 8: Create mappers**

Create `src/main/java/cn/haut/survivor/mapper/ExplorationStoryChainMapper.java`:

```java
package cn.haut.survivor.mapper;

import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExplorationStoryChainMapper extends BaseMapper<ExplorationStoryChain> {
}
```

Create `src/main/java/cn/haut/survivor/mapper/ExplorationStoryProgressMapper.java`:

```java
package cn.haut.survivor.mapper;

import cn.haut.survivor.domain.entity.ExplorationStoryProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExplorationStoryProgressMapper extends BaseMapper<ExplorationStoryProgress> {
}
```

- [ ] **Step 9: Add data file to application.yml**

In `src/main/resources/application.yml`, update `spring.sql.init.data-locations`:

```yaml
data-locations: classpath:data.sql,classpath:data-content-pack-2.sql,classpath:data-content-pack-3.sql,classpath:data-content-pack-4.sql
```

- [ ] **Step 10: Create `data-content-pack-4.sql`**

Create `src/main/resources/data-content-pack-4.sql` with the following seed file:

```sql
-- ============================================================
-- Content Pack 4: 传闻、周主题与探索奇遇机制化
-- ============================================================

INSERT INTO rumor
(id, week_number, location_id, rumor_title, rumor_text, effect_hint, effect_type, effect_value, effect_target, rarity, active) VALUES
(4001, 1, 2, '图书馆二楼突然空了一排座', '听说图书馆二楼靠窗位置今天没人抢，适合悄悄开始学习。', '图书馆探索度更容易增加', 'explore_bonus', 2, 'explore', 'common', 1),
(4002, 1, 3, '宿舍今晚可能晚点断电', '宿舍群里有人说今晚断电会推迟，虽然消息来源是“隔壁说的”。', '宿舍压力风险降低', 'safe_zone', 1, 'pressure', 'rare', 1),
(4003, 1, 4, '食堂新窗口排队很短', '三楼新窗口刚开，人还不多，拼桌聊天机会不少。', '食堂更容易遇见 NPC', 'npc_boost', 10, 'npc', 'common', 1),
(4004, 1, 1, '教学楼有人问路', '新生还在到处找教室，主动帮忙能混个脸熟。', '教学楼社交收益提高', 'attr_bonus', 1, 'social', 'common', 1),

(4005, 2, 7, '社团区今晚有联合招新', '几个社团临时决定一起摆摊，路过的人都会被热情拦住。', '社团区社交收益提高', 'attr_bonus', 2, 'social', 'common', 1),
(4006, 2, 8, '篮球社夜训缺人', '篮球社今晚训练缺人，去操场可能被拉去凑队。', '操场健康收益提高', 'attr_bonus', 2, 'health', 'common', 1),
(4007, 2, 6, '实验室开放日', '老郑说今晚实验室门没锁，想看项目结构可以来。', '实验室技能收益提高', 'attr_bonus', 2, 'skill', 'rare', 1),
(4008, 2, 4, '食堂拼桌情报局', '有人在食堂聊社团报名和课设队友，消息很杂但可能有用。', '食堂传闻效果更明显', 'npc_boost', 10, 'npc', 'rare', 1),

(4009, 3, 6, '老郑的白板还没擦', '实验室白板上还有昨晚留下的报错分析，像一张救命地图。', '实验室技能收益提高', 'attr_bonus', 2, 'skill', 'rare', 1),
(4010, 3, 2, '图书馆今晚有人通宵', '有人说图书馆今晚气氛像战场，坐下就不好意思摸鱼。', '图书馆学业收益提高但压力上升', 'attr_bonus', 2, 'academic', 'common', 1),
(4011, 3, 3, '宿舍开黑局变赶工局', '阿杰说今晚不打了，大家都在救自己的 DDL。', '宿舍压力风险降低', 'safe_zone', 1, 'pressure', 'common', 1),
(4012, 3, 1, '教学楼小测风声', '课代表突然问大家复习没，空气里有小测的味道。', '教学楼事件更偏学业', 'event_hint', 1, 'academic', 'common', 1),

(4013, 4, 8, '操场今晚有人组体测冲刺', '小马说最后一周再不练就只能靠玄学。', '操场健康收益提高', 'attr_bonus', 3, 'health', 'rare', 1),
(4014, 4, 2, '考前资料在图书馆流动', '据说有人整理了考试重点，但只在图书馆附近传。', '图书馆学业收益提高', 'attr_bonus', 2, 'academic', 'common', 1),
(4015, 4, 4, '食堂补给窗口加量', '期末周食堂某窗口突然多给一点，像在给学生续命。', '食堂健康收益提高', 'attr_bonus', 1, 'health', 'common', 1),
(4016, 4, 3, '宿舍早睡联盟成立', '隔壁寝室决定一起早睡，虽然听起来很不现实。', '宿舍自律收益提高', 'attr_bonus', 2, 'discipline', 'rare', 1);

INSERT INTO exploration_story_chain
(id, chain_key, chain_name, location_id, week_number, required_explore_level, step_number, scenario_text, result_text,
 academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_step_number, active) VALUES
(4001, 'library_seat', '被占的自习座', 2, 0, 10, 1, '你发现图书馆靠窗有个座位总被书包占着，但人一直没出现。', '你记住了这个座位，也开始观察图书馆真正的座位生态。', 2, 0, 0, 0, 0, 1, 1, 18, 2, 1),
(4002, 'library_seat', '被占的自习座', 2, 0, 10, 2, '你终于遇见了座位主人，对方也在赶 DDL。', '你们短暂交流了一下复习范围，发现彼此都很惨。', 3, 0, 0, 1, 0, 1, 2, 24, 3, 1),
(4003, 'library_seat', '被占的自习座', 2, 0, 10, 3, '你们形成了“谁先到谁占座”的默契。', '图书馆突然多了一个固定学习角，学业节奏稳了一点。', 5, 0, 0, 2, 0, -1, 3, 35, NULL, 1),

(4004, 'lab_whiteboard', '白板上的神秘报错', 6, 0, 20, 1, '实验室白板上留下了一串报错和箭头。', '你看不全懂，但至少知道这不是普通报错。', 0, 0, 0, 0, 3, 2, 1, 20, 2, 1),
(4005, 'lab_whiteboard', '白板上的神秘报错', 6, 0, 20, 2, '你根据白板线索定位到一个项目结构问题。', '你第一次觉得错误日志像线索，不只是精神攻击。', 1, 0, 0, 0, 5, 2, 2, 30, 3, 1),
(4006, 'lab_whiteboard', '白板上的神秘报错', 6, 0, 20, 3, '老郑发现你看懂了白板，开始把你当半个自己人。', '师兄点了点头，这种认可比一杯咖啡还提神。', 2, 0, 0, 1, 6, 1, 2, 40, NULL, 1),

(4007, 'track_night_run', '夜跑打卡局', 8, 0, 10, 1, '你看到一群人在操场边喊“今天第三天”。', '你被气氛带着走了一圈，身体比脑子先醒了。', 0, 4, 0, 1, 0, -2, 1, 18, 2, 1),
(4008, 'track_night_run', '夜跑打卡局', 8, 0, 10, 2, '小马邀请你一起跑一圈，说跑完再焦虑也不迟。', '你跑得不快，但压力真的松了一点。', 0, 5, 0, 2, 0, -3, 2, 28, 3, 1),
(4009, 'track_night_run', '夜跑打卡局', 8, 0, 10, 3, '你开始把夜跑当成减压手段。', '操场从路过的地方变成了你的回血点。', 0, 7, 0, 2, 0, -4, 3, 38, NULL, 1),

(4010, 'canteen_gossip', '拼桌情报局', 4, 0, 0, 1, '你被迫和陌生同学拼桌。', '对方聊起社团和课设队友，消息杂但有用。', 0, 0, 0, 3, 0, -1, 0, 18, 2, 1),
(4011, 'canteen_gossip', '拼桌情报局', 4, 0, 0, 2, '你听到一个关于社团和课程安排的真实情报。', '你发现食堂不只是吃饭的地方，也是校园信息中转站。', 1, 0, 0, 5, 1, 0, 1, 30, NULL, 1),

(4012, 'dorm_lights_out', '熄灯后的寝室会议', 3, 0, 0, 1, '熄灯后大家开始聊最近谁最惨。', '你发现不是只有自己在硬撑，压力稍微散了一点。', 0, 1, 0, 3, 0, -4, -1, 18, 2, 1),
(4013, 'dorm_lights_out', '熄灯后的寝室会议', 3, 0, 0, 2, '阿杰提出一个离谱但有用的减压方案。', '方案不一定科学，但寝室笑成一团，至少今晚没那么窒息。', 0, 1, 0, 4, 0, -5, -1, 30, NULL, 1);

INSERT INTO weekly_goal
(id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, active) VALUES
(4001, 'rumor_hunter', '情报猎人', '本周触发 2 次传闻效果。', 'rumor_effect_used', 2, 35, 'social', 2, 1),
(4002, 'story_chaser', '校园奇遇追踪者', '本周推进 2 次探索奇遇。', 'exploration_story_step', 2, 40, 'skill', 2, 1),
(4003, 'theme_survivor', '顺势而为', '本周触发 2 次周主题修正。', 'weekly_modifier_used', 2, 30, 'discipline', 2, 1),
(4004, 'buddy_rescue', '搭子救场', '本周触发 1 次搭子外溢加成。', 'buddy_assist', 1, 35, 'pressure', 3, 1);

INSERT INTO achievement
(id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, active) VALUES
(4001, 'first_rumor_effect', '听劝一次', '第一次让校园传闻真正帮上忙。', '📰', 'rumor_effect_used', 1, '情报新生', 1),
(4002, 'story_first_step', '奇遇开端', '第一次触发探索奇遇链。', '🧩', 'exploration_story_step', 1, '校园目击者', 1),
(4003, 'story_completed', '有始有终', '完成 1 条探索奇遇链。', '✅', 'exploration_story_completed', 1, '支线清理大师', 1),
(4004, 'theme_master', '看懂周节奏', '第一次触发周主题修正。', '📆', 'weekly_modifier_used', 1, '节奏感选手', 1),
(4005, 'buddy_saved_me', '搭子救我', '触发 1 次本周搭子外溢加成。', '🤝', 'buddy_assist', 1, '有人罩着', 1);
```

- [ ] **Step 11: Run seed smoke tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ContentPack4Tests test
```

Expected: PASS.

- [ ] **Step 12: Commit**

```powershell
git add src/main/resources/schema.sql src/main/resources/application.yml src/main/resources/data-content-pack-4.sql src/main/java/cn/haut/survivor/domain/entity/Rumor.java src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryChain.java src/main/java/cn/haut/survivor/domain/entity/ExplorationStoryProgress.java src/main/java/cn/haut/survivor/mapper/ExplorationStoryChainMapper.java src/main/java/cn/haut/survivor/mapper/ExplorationStoryProgressMapper.java src/test/java/cn/haut/survivor/service/ContentPack4Tests.java
git commit -m "feat: seed rumor theme exploration content pack"
```

---

### Task 2: Add Weekly Modifier Service

**Files:**
- Create: `src/main/java/cn/haut/survivor/domain/entity/ExplorationInfluence.java`
- Create: `src/main/java/cn/haut/survivor/service/WeeklyModifierService.java`
- Test: `src/test/java/cn/haut/survivor/service/WeeklyModifierServiceTests.java`

- [ ] **Step 1: Create `ExplorationInfluence` record**

Create `src/main/java/cn/haut/survivor/domain/entity/ExplorationInfluence.java`:

```java
package cn.haut.survivor.domain.entity;

public record ExplorationInfluence(
        String sourceType,
        String sourceName,
        String description,
        AttributeChange attributeChange,
        int exploreBonus
) {
    public boolean hasEffect() {
        return exploreBonus != 0 || (attributeChange != null && attributeChange.hasAnyChange());
    }
}
```

- [ ] **Step 2: Write WeeklyModifier tests**

Create `src/test/java/cn/haut/survivor/service/WeeklyModifierServiceTests.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class WeeklyModifierServiceTests {

    @Autowired
    private WeeklyModifierService weeklyModifierService;

    @Test
    void weekOneAddsSmallExploreBonus() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(1, 2L);

        assertThat(influence.sourceType()).isEqualTo("weekly_theme");
        assertThat(influence.exploreBonus()).isEqualTo(1);
        assertThat(influence.description()).contains("开学适应周");
    }

    @Test
    void weekThreeLabAddsSkillAndPressure() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(3, 6L);

        assertThat(influence.attributeChange().skillChange()).isEqualTo(1);
        assertThat(influence.attributeChange().pressureChange()).isEqualTo(1);
        assertThat(influence.description()).contains("DDL");
    }

    @Test
    void unknownWeekReturnsEmptyInfluence() {
        ExplorationInfluence influence = weeklyModifierService.getExplorationInfluence(9, 2L);

        assertThat(influence.hasEffect()).isFalse();
        assertThat(influence.description()).isBlank();
    }
}
```

- [ ] **Step 3: Run failing tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyModifierServiceTests test
```

Expected: FAIL because `WeeklyModifierService` does not exist.

- [ ] **Step 4: Implement `WeeklyModifierService`**

Create `src/main/java/cn/haut/survivor/service/WeeklyModifierService.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import org.springframework.stereotype.Service;

@Service
public class WeeklyModifierService {

    public ExplorationInfluence getExplorationInfluence(int weekNumber, Long locationId) {
        if (weekNumber == 1) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "开学适应周",
                    "开学适应周：你对校园还新鲜，探索度额外 +1。",
                    AttributeChange.EMPTY,
                    1);
        }
        if (weekNumber == 2 && (locationId == 4L || locationId == 7L || locationId == 8L)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "社团招新周",
                    "社团招新周：人多消息多，社交收益 +1。",
                    new AttributeChange(0, 0, 0, 1, 0, 0, 0, 0),
                    0);
        }
        if (weekNumber == 3 && (locationId == 2L || locationId == 6L || locationId == 1L)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "DDL 高压周",
                    "DDL 高压周：学习和技能收益 +1，但压力也更容易上升。",
                    new AttributeChange(0, 0, 0, 0, 1, 1, 0, 0),
                    0);
        }
        if (weekNumber == 4 && (locationId == 2L || locationId == 8L)) {
            return new ExplorationInfluence(
                    "weekly_theme",
                    "期末与体测周",
                    "期末与体测周：关键地点收益更明显，探索度额外 +1。",
                    AttributeChange.EMPTY,
                    1);
        }
        return new ExplorationInfluence("weekly_theme", "", "", AttributeChange.EMPTY, 0);
    }
}
```

- [ ] **Step 5: Run tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyModifierServiceTests test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/domain/entity/ExplorationInfluence.java src/main/java/cn/haut/survivor/service/WeeklyModifierService.java src/test/java/cn/haut/survivor/service/WeeklyModifierServiceTests.java
git commit -m "feat: add weekly exploration modifiers"
```

---

### Task 3: Add Rumor Effect Service

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/RumorService.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/RumorServiceImpl.java`
- Create: `src/main/java/cn/haut/survivor/service/RumorEffectService.java`
- Create: `src/main/java/cn/haut/survivor/service/impl/RumorEffectServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/RumorEffectServiceTests.java`

- [ ] **Step 1: Extend RumorService with visible rumor method**

In `src/main/java/cn/haut/survivor/service/RumorService.java`, add:

```java
List<Rumor> pickVisibleRumorsForUser(Long userId, int weekNumber);
```

In `src/main/java/cn/haut/survivor/service/impl/RumorServiceImpl.java`, add:

```java
@Override
public List<Rumor> pickVisibleRumorsForUser(Long userId, int weekNumber) {
    return pickRumorsForUser(userId, weekNumber, 3);
}
```

- [ ] **Step 2: Write RumorEffect tests**

Create `src/test/java/cn/haut/survivor/service/RumorEffectServiceTests.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.Rumor;
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
class RumorEffectServiceTests {

    @Autowired
    private RumorEffectService rumorEffectService;

    @Autowired
    private RumorService rumorService;

    @Test
    void locationRumorProducesInfluence() {
        Long userId = userSeeingRumor(1, "图书馆二楼");
        List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, 1, 2L);

        assertThat(influences).anyMatch(i ->
                "rumor".equals(i.sourceType())
                        && i.sourceName().contains("图书馆")
                        && i.exploreBonus() > 0);
    }

    @Test
    void nonMatchingLocationDoesNotUseLocationRumor() {
        Long userId = userSeeingRumor(1, "图书馆二楼");
        List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, 1, 6L);

        assertThat(influences).noneMatch(i -> i.sourceName().contains("图书馆二楼"));
    }

    @Test
    void attrBonusRumorProducesAttributeChange() {
        Long userId = userSeeingRumor(3, "白板");
        List<ExplorationInfluence> influences = rumorEffectService.getExplorationInfluences(userId, 3, 6L);

        assertThat(influences).anyMatch(i ->
                i.attributeChange() != null && i.attributeChange().skillChange() > 0);
    }

    private Long userSeeingRumor(int weekNumber, String titlePart) {
        for (long userId = 1L; userId <= 80L; userId++) {
            List<Rumor> visible = rumorService.pickVisibleRumorsForUser(userId, weekNumber);
            boolean found = visible.stream()
                    .map(Rumor::getRumorTitle)
                    .anyMatch(title -> title != null && title.contains(titlePart));
            if (found) {
                return userId;
            }
        }
        throw new AssertionError("No deterministic user sees rumor title containing: " + titlePart);
    }
}
```

- [ ] **Step 3: Run failing tests**

Run:

```powershell
.\mvnw.cmd -Dtest=RumorEffectServiceTests test
```

Expected: FAIL because `RumorEffectService` does not exist.

- [ ] **Step 4: Create RumorEffectService interface**

Create `src/main/java/cn/haut/survivor/service/RumorEffectService.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.ExplorationInfluence;

import java.util.List;

public interface RumorEffectService {
    List<ExplorationInfluence> getExplorationInfluences(Long userId, int weekNumber, Long locationId);
}
```

- [ ] **Step 5: Implement RumorEffectServiceImpl**

Create `src/main/java/cn/haut/survivor/service/impl/RumorEffectServiceImpl.java`:

```java
package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.service.RumorEffectService;
import cn.haut.survivor.service.RumorService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RumorEffectServiceImpl implements RumorEffectService {

    private final RumorService rumorService;

    public RumorEffectServiceImpl(RumorService rumorService) {
        this.rumorService = rumorService;
    }

    @Override
    public List<ExplorationInfluence> getExplorationInfluences(Long userId, int weekNumber, Long locationId) {
        List<Rumor> visibleRumors = rumorService.pickVisibleRumorsForUser(userId, weekNumber);
        List<ExplorationInfluence> influences = new ArrayList<>();
        for (Rumor rumor : visibleRumors) {
            if (rumor.getLocationId() != null && !rumor.getLocationId().equals(locationId)) {
                continue;
            }
            ExplorationInfluence influence = toInfluence(rumor);
            if (influence.hasEffect()) {
                influences.add(influence);
            }
        }
        return influences;
    }

    private ExplorationInfluence toInfluence(Rumor rumor) {
        String type = rumor.getEffectType();
        int amount = rumor.getEffectValue() != null ? rumor.getEffectValue() : 0;
        String target = rumor.getEffectTarget();
        String description = "传闻生效：" + rumor.getRumorTitle() + "，" + rumor.getEffectHint();

        if ("explore_bonus".equals(type)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description, AttributeChange.EMPTY, amount);
        }
        if ("safe_zone".equals(type) && "pressure".equals(target)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description,
                    new AttributeChange(0, 0, 0, 0, 0, -amount, 0, 0), 0);
        }
        if ("attr_bonus".equals(type)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description, attrChange(target, amount), 0);
        }
        if ("npc_boost".equals(type)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description, AttributeChange.EMPTY, 0);
        }
        if ("event_hint".equals(type)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description, AttributeChange.EMPTY, 0);
        }
        return new ExplorationInfluence("rumor", rumor.getRumorTitle(), "", AttributeChange.EMPTY, 0);
    }

    private AttributeChange attrChange(String target, int amount) {
        return switch (target != null ? target : "") {
            case "academic" -> new AttributeChange(amount, 0, 0, 0, 0, 0, 0, 0);
            case "health" -> new AttributeChange(0, amount, 0, 0, 0, 0, 0, 0);
            case "money" -> new AttributeChange(0, 0, amount, 0, 0, 0, 0, 0);
            case "social" -> new AttributeChange(0, 0, 0, amount, 0, 0, 0, 0);
            case "skill" -> new AttributeChange(0, 0, 0, 0, amount, 0, 0, 0);
            case "pressure" -> new AttributeChange(0, 0, 0, 0, 0, amount, 0, 0);
            case "discipline" -> new AttributeChange(0, 0, 0, 0, 0, 0, amount, 0);
            default -> AttributeChange.EMPTY;
        };
    }
}
```

- [ ] **Step 6: Run tests**

Run:

```powershell
.\mvnw.cmd -Dtest=RumorEffectServiceTests test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/RumorService.java src/main/java/cn/haut/survivor/service/impl/RumorServiceImpl.java src/main/java/cn/haut/survivor/service/RumorEffectService.java src/main/java/cn/haut/survivor/service/impl/RumorEffectServiceImpl.java src/test/java/cn/haut/survivor/service/RumorEffectServiceTests.java
git commit -m "feat: apply visible rumor effects"
```

---

### Task 4: Add Exploration Story Service

**Files:**
- Create: `src/main/java/cn/haut/survivor/service/ExplorationStoryService.java`
- Create: `src/main/java/cn/haut/survivor/service/impl/ExplorationStoryServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/ExplorationStoryServiceTests.java`

- [ ] **Step 1: Write story service tests**

Create `src/test/java/cn/haut/survivor/service/ExplorationStoryServiceTests.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.mapper.ExplorationStoryProgressMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class ExplorationStoryServiceTests {

    @Autowired
    private ExplorationStoryService explorationStoryService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ExplorationStoryProgressMapper progressMapper;

    @BeforeEach
    void setUp() {
        playerService.createProfile(2L, "story test", "大二", "计算机类", "就业路线");
    }

    @Test
    void triggerSpecificChainStepCreatesProgress() {
        var result = explorationStoryService.triggerSpecificStep(2L, "library_seat", 1, 1);

        assertThat(result).isPresent();
        assertThat(result.get().chain().getChainKey()).isEqualTo("library_seat");
        assertThat(result.get().completed()).isFalse();
        assertThat(progressMapper.selectById(result.get().progress().getId()).getCurrentStep()).isEqualTo(2);
    }

    @Test
    void finalStepMarksChainCompleted() {
        explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 1, 1);
        var result = explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 2, 1);

        assertThat(result).isPresent();
        assertThat(result.get().completed()).isTrue();
        assertThat(result.get().progress().getCompleted()).isEqualTo(1);
    }

    @Test
    void completedChainDoesNotTriggerAgain() {
        explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 1, 1);
        explorationStoryService.triggerSpecificStep(2L, "canteen_gossip", 2, 1);

        var result = explorationStoryService.maybeTrigger(2L, 4L, 1, 0);

        assertThat(result).isEmpty();
    }
}
```

- [ ] **Step 2: Run failing tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ExplorationStoryServiceTests test
```

Expected: FAIL because `ExplorationStoryService` does not exist.

- [ ] **Step 3: Create ExplorationStoryService interface**

Create `src/main/java/cn/haut/survivor/service/ExplorationStoryService.java`:

```java
package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.ExplorationStoryProgress;

import java.util.Optional;

public interface ExplorationStoryService {

    Optional<ExplorationStoryResult> maybeTrigger(Long userId, Long locationId, int weekNumber, int exploreLevel);

    Optional<ExplorationStoryResult> triggerSpecificStep(Long userId, String chainKey, int stepNumber, int weekNumber);

    record ExplorationStoryResult(
            ExplorationStoryChain chain,
            ExplorationStoryProgress progress,
            AttributeChange attributeChange,
            String storyText,
            boolean completed
    ) {}
}
```

- [ ] **Step 4: Implement ExplorationStoryServiceImpl**

Create `src/main/java/cn/haut/survivor/service/impl/ExplorationStoryServiceImpl.java`:

```java
package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationStoryChain;
import cn.haut.survivor.domain.entity.ExplorationStoryProgress;
import cn.haut.survivor.mapper.ExplorationStoryChainMapper;
import cn.haut.survivor.mapper.ExplorationStoryProgressMapper;
import cn.haut.survivor.service.ExplorationStoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExplorationStoryServiceImpl implements ExplorationStoryService {

    private static final int TRIGGER_PROBABILITY = 25;

    private final ExplorationStoryChainMapper chainMapper;
    private final ExplorationStoryProgressMapper progressMapper;

    public ExplorationStoryServiceImpl(ExplorationStoryChainMapper chainMapper,
                                       ExplorationStoryProgressMapper progressMapper) {
        this.chainMapper = chainMapper;
        this.progressMapper = progressMapper;
    }

    @Override
    @Transactional
    public Optional<ExplorationStoryResult> maybeTrigger(Long userId, Long locationId, int weekNumber, int exploreLevel) {
        if (ThreadLocalRandom.current().nextInt(100) >= TRIGGER_PROBABILITY) {
            return Optional.empty();
        }

        List<ExplorationStoryChain> candidates = chainMapper.selectList(new LambdaQueryWrapper<ExplorationStoryChain>()
                .eq(ExplorationStoryChain::getLocationId, locationId)
                .eq(ExplorationStoryChain::getActive, 1)
                .le(ExplorationStoryChain::getRequiredExploreLevel, exploreLevel)
                .and(q -> q.eq(ExplorationStoryChain::getWeekNumber, 0).or().eq(ExplorationStoryChain::getWeekNumber, weekNumber))
                .orderByAsc(ExplorationStoryChain::getId));

        for (ExplorationStoryChain firstStep : candidates) {
            ExplorationStoryProgress progress = findProgress(userId, firstStep.getChainKey());
            if (progress != null && value(progress.getCompleted()) == 1) {
                continue;
            }
            int step = progress == null ? 1 : value(progress.getCurrentStep());
            return triggerSpecificStep(userId, firstStep.getChainKey(), step, weekNumber);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<ExplorationStoryResult> triggerSpecificStep(Long userId, String chainKey, int stepNumber, int weekNumber) {
        ExplorationStoryProgress progress = findProgress(userId, chainKey);
        if (progress != null && value(progress.getCompleted()) == 1) {
            return Optional.empty();
        }

        ExplorationStoryChain chain = chainMapper.selectOne(new LambdaQueryWrapper<ExplorationStoryChain>()
                .eq(ExplorationStoryChain::getChainKey, chainKey)
                .eq(ExplorationStoryChain::getStepNumber, stepNumber)
                .eq(ExplorationStoryChain::getActive, 1)
                .last("LIMIT 1"));

        if (chain == null) {
            return Optional.empty();
        }

        if (progress == null) {
            progress = new ExplorationStoryProgress();
            progress.setUserId(userId);
            progress.setChainKey(chainKey);
            progress.setCurrentStep(stepNumber);
            progress.setCompleted(0);
            progress.setLastTriggerWeek(weekNumber);
            progress.setUpdateTime(LocalDateTime.now());
            progressMapper.insert(progress);
        }

        boolean completed = chain.getNextStepNumber() == null;
        progress.setCurrentStep(completed ? stepNumber : chain.getNextStepNumber());
        progress.setCompleted(completed ? 1 : 0);
        progress.setLastTriggerWeek(weekNumber);
        progress.setUpdateTime(LocalDateTime.now());
        progressMapper.updateById(progress);

        AttributeChange change = new AttributeChange(
                value(chain.getAcademicChange()),
                value(chain.getHealthChange()),
                value(chain.getMoneyChange()),
                value(chain.getSocialChange()),
                value(chain.getSkillChange()),
                value(chain.getPressureChange()),
                value(chain.getDisciplineChange()),
                value(chain.getExpChange()));

        String storyText = chain.getScenarioText() + " " + chain.getResultText();
        return Optional.of(new ExplorationStoryResult(chain, progress, change, storyText, completed));
    }

    private ExplorationStoryProgress findProgress(Long userId, String chainKey) {
        return progressMapper.selectOne(new LambdaQueryWrapper<ExplorationStoryProgress>()
                .eq(ExplorationStoryProgress::getUserId, userId)
                .eq(ExplorationStoryProgress::getChainKey, chainKey)
                .last("LIMIT 1"));
    }

    private int value(Integer value) {
        return value != null ? value : 0;
    }
}
```

- [ ] **Step 5: Run tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ExplorationStoryServiceTests test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/ExplorationStoryService.java src/main/java/cn/haut/survivor/service/impl/ExplorationStoryServiceImpl.java src/test/java/cn/haut/survivor/service/ExplorationStoryServiceTests.java
git commit -m "feat: add exploration story chains"
```

---

### Task 5: Integrate Influences into ExplorationService

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/ExplorationService.java`
- Modify: `src/main/java/cn/haut/survivor/service/impl/ExplorationServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/ExplorationServiceTests.java`

- [ ] **Step 1: Add failing exploration influence tests**

Append to `src/test/java/cn/haut/survivor/service/ExplorationServiceTests.java`:

```java
@Test
void explorationResultContainsInfluences() {
    playerService.createProfile(2L, "influence test", "大二", "计算机类", "就业路线");

    ExplorationService.ExplorationResult result = explorationService.explore(2L, 2L);

    assertThat(result.influences()).isNotNull();
    assertThat(result.influences()).anyMatch(i -> "weekly_theme".equals(i.sourceType()) || "rumor".equals(i.sourceType()));
}

@Test
void explorationAppliesInfluenceDeltasToReturnedChange() {
    playerService.createProfile(2L, "delta test", "大二", "计算机类", "就业路线");
    int beforeAcademic = playerService.findAttributeByUserId(2L).getAcademic();

    ExplorationService.ExplorationResult result = explorationService.explore(2L, 2L);
    int afterAcademic = playerService.findAttributeByUserId(2L).getAcademic();

    assertThat(afterAcademic).isEqualTo(beforeAcademic + result.academicChange());
}
```

`ExplorationServiceTests` already has `PlayerService` injected in the current repo, so only append the two test methods above.

- [ ] **Step 2: Run failing tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ExplorationServiceTests test
```

Expected: FAIL because `ExplorationResult.influences()` does not exist.

- [ ] **Step 3: Extend ExplorationResult**

In `src/main/java/cn/haut/survivor/service/ExplorationService.java`, import:

```java
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.service.ExplorationStoryService.ExplorationStoryResult;
import java.util.Optional;
```

Change `ExplorationResult` to include two trailing fields:

```java
record ExplorationResult(
        UserLocationExploration exploration,
        String resultType,
        String description,
        int exploreLevelGain,
        int academicChange,
        int healthChange,
        int moneyChange,
        int socialChange,
        int skillChange,
        int pressureChange,
        int disciplineChange,
        List<ExplorationInfluence> influences,
        Optional<ExplorationStoryResult> storyResult
) {}
```

- [ ] **Step 4: Inject services into ExplorationServiceImpl**

In `src/main/java/cn/haut/survivor/service/impl/ExplorationServiceImpl.java`, add fields:

```java
private final WeeklyModifierService weeklyModifierService;
private final RumorEffectService rumorEffectService;
private final ExplorationStoryService explorationStoryService;
private final WeeklyGoalService weeklyGoalService;
private final AchievementService achievementService;
```

Update constructor:

```java
public ExplorationServiceImpl(
        UserLocationExplorationMapper explorationMapper,
        PlayerService playerService,
        PlayerAttributeMapper playerAttributeMapper,
        WeeklyModifierService weeklyModifierService,
        RumorEffectService rumorEffectService,
        ExplorationStoryService explorationStoryService,
        WeeklyGoalService weeklyGoalService,
        AchievementService achievementService) {
    this.explorationMapper = explorationMapper;
    this.playerService = playerService;
    this.playerAttributeMapper = playerAttributeMapper;
    this.weeklyModifierService = weeklyModifierService;
    this.rumorEffectService = rumorEffectService;
    this.explorationStoryService = explorationStoryService;
    this.weeklyGoalService = weeklyGoalService;
    this.achievementService = achievementService;
}
```

Add imports:

```java
import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.service.AchievementService;
import cn.haut.survivor.service.ExplorationStoryService;
import cn.haut.survivor.service.RumorEffectService;
import cn.haut.survivor.service.WeeklyGoalService;
import cn.haut.survivor.service.WeeklyModifierService;
import java.util.ArrayList;
import java.util.Optional;
```

- [ ] **Step 5: Apply influences in explore()**

In `explore()`, after `ExplorationOutcome outcome = rollExplorationOutcome(currentLevel, attribute);`, add:

```java
List<ExplorationInfluence> influences = new ArrayList<>();
ExplorationInfluence weeklyInfluence = weeklyModifierService.getExplorationInfluence(profile.getCurrentWeek(), locationId);
if (weeklyInfluence.hasEffect()) {
    influences.add(weeklyInfluence);
    weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "weekly_modifier_used", 1);
    achievementService.unlockIfEligible(userId, "weekly_modifier_used", 1);
}
List<ExplorationInfluence> rumorInfluences =
        rumorEffectService.getExplorationInfluences(userId, profile.getCurrentWeek(), locationId);
for (ExplorationInfluence influence : rumorInfluences) {
    if (influence.hasEffect()) {
        influences.add(influence);
        weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "rumor_effect_used", 1);
        achievementService.unlockIfEligible(userId, "rumor_effect_used", 1);
    }
}
Optional<ExplorationStoryService.ExplorationStoryResult> storyResult =
        explorationStoryService.maybeTrigger(userId, locationId, profile.getCurrentWeek(), currentLevel);
storyResult.ifPresent(story -> {
    influences.add(new ExplorationInfluence(
            "story",
            story.chain().getChainName(),
            story.storyText(),
            story.attributeChange(),
            0));
    weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "exploration_story_step", 1);
    achievementService.unlockIfEligible(userId, "exploration_story_step", 1);
    if (story.completed()) {
        achievementService.unlockIfEligible(userId, "exploration_story_completed", 1);
    }
});
```

Then replace attribute application with summed deltas:

```java
int academicDelta = outcome.academicChange;
int healthDelta = outcome.healthChange;
int moneyDelta = outcome.moneyChange;
int socialDelta = outcome.socialChange;
int skillDelta = outcome.skillChange;
int pressureDelta = outcome.pressureChange;
int disciplineDelta = outcome.disciplineChange;
int influenceExploreBonus = 0;

for (ExplorationInfluence influence : influences) {
    AttributeChange change = influence.attributeChange();
    if (change != null) {
        academicDelta += change.academicChange();
        healthDelta += change.healthChange();
        moneyDelta += change.moneyChange();
        socialDelta += change.socialChange();
        skillDelta += change.skillChange();
        pressureDelta += change.pressureChange();
        disciplineDelta += change.disciplineChange();
    }
    influenceExploreBonus += influence.exploreBonus();
}
exploreLevelGain = Math.min(exploreLevelGain + influenceExploreBonus, 100 - currentLevel);

attribute.setAcademic(clamp(attribute.getAcademic() + academicDelta));
attribute.setHealth(clamp(attribute.getHealth() + healthDelta));
attribute.setMoney(clamp(attribute.getMoney() + moneyDelta));
attribute.setSocial(clamp(attribute.getSocial() + socialDelta));
attribute.setSkill(clamp(attribute.getSkill() + skillDelta));
attribute.setPressure(clamp(attribute.getPressure() + pressureDelta));
attribute.setDiscipline(clamp(attribute.getDiscipline() + disciplineDelta));
```

Finally update the returned result:

```java
return new ExplorationResult(
        exploration,
        outcome.resultType,
        outcome.description,
        exploreLevelGain,
        academicDelta,
        healthDelta,
        moneyDelta,
        socialDelta,
        skillDelta,
        pressureDelta,
        disciplineDelta,
        influences,
        storyResult
);
```

- [ ] **Step 6: Run tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ExplorationServiceTests,WeeklyModifierServiceTests,RumorEffectServiceTests,ExplorationStoryServiceTests test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/ExplorationService.java src/main/java/cn/haut/survivor/service/impl/ExplorationServiceImpl.java src/test/java/cn/haut/survivor/service/ExplorationServiceTests.java
git commit -m "feat: apply exploration influences"
```

---

### Task 6: Add Buddy Assist to Exploration

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/ExplorationServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/ExplorationServiceTests.java`

- [ ] **Step 1: Add deterministic helper method**

In `ExplorationServiceImpl`, add package-private method for tests:

```java
ExplorationInfluence buildBuddyAssistInfluence(Long buddyNpcId, Long locationId) {
    if (buddyNpcId == null) {
        return new ExplorationInfluence("buddy", "", "", AttributeChange.EMPTY, 0);
    }
    if (buddyNpcId == 1L && locationId == 3L) {
        return new ExplorationInfluence("buddy", "室友阿杰", "搭子加成：阿杰让宿舍探索压力 -1。",
                new AttributeChange(0, 0, 0, 0, 0, -1, 0, 0), 0);
    }
    if (buddyNpcId == 2L && (locationId == 1L || locationId == 2L)) {
        return new ExplorationInfluence("buddy", "学霸林然", "搭子加成：林然让学习地点学业 +1。",
                new AttributeChange(1, 0, 0, 0, 0, 0, 0, 0), 0);
    }
    if (buddyNpcId == 3L && (locationId == 4L || locationId == 7L)) {
        return new ExplorationInfluence("buddy", "社牛周予", "搭子加成：周予让社交场景社交 +1。",
                new AttributeChange(0, 0, 0, 1, 0, 0, 0, 0), 0);
    }
    if (buddyNpcId == 4L && locationId == 6L) {
        return new ExplorationInfluence("buddy", "师兄老郑", "搭子加成：老郑让实验室技能 +1。",
                new AttributeChange(0, 0, 0, 0, 1, 0, 0, 0), 0);
    }
    if (buddyNpcId == 5L && locationId == 8L) {
        return new ExplorationInfluence("buddy", "运动搭子小马", "搭子加成：小马让操场健康 +1。",
                new AttributeChange(0, 1, 0, 0, 0, 0, 0, 0), 0);
    }
    return new ExplorationInfluence("buddy", "", "", AttributeChange.EMPTY, 0);
}
```

- [ ] **Step 2: Inject NpcService**

Add field:

```java
private final NpcService npcService;
```

Add constructor parameter and assignment:

```java
NpcService npcService
```

```java
this.npcService = npcService;
```

Add import:

```java
import cn.haut.survivor.service.NpcService;
```

- [ ] **Step 3: Add buddy influence inside explore()**

After rumor influences and before story trigger:

```java
npcService.getCurrentBuddy(userId, profile.getCurrentWeek()).ifPresent(buddy -> {
    ExplorationInfluence buddyInfluence = buildBuddyAssistInfluence(buddy.getNpcId(), locationId);
    if (buddyInfluence.hasEffect()) {
        influences.add(buddyInfluence);
        weeklyGoalService.updateProgress(userId, profile.getCurrentWeek(), "buddy_assist", 1);
        achievementService.unlockIfEligible(userId, "buddy_assist", 1);
    }
});
```

- [ ] **Step 4: Add test**

Append to `ExplorationServiceTests`:

```java
@Autowired
private NpcService npcService;

@Test
void currentBuddyCanAddExplorationInfluence() {
    playerService.createProfile(2L, "buddy exploration test", "大二", "计算机类", "就业路线");
    npcService.increaseFamiliarity(2L, 2L, 55);
    npcService.chooseWeeklyBuddy(2L, 2L, 1);

    ExplorationService.ExplorationResult result = explorationService.explore(2L, 2L);

    assertThat(result.influences()).anyMatch(i ->
            "buddy".equals(i.sourceType())
                    && i.description().contains("林然")
                    && i.attributeChange().academicChange() > 0);
}
```

- [ ] **Step 5: Run tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ExplorationServiceTests test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/ExplorationServiceImpl.java src/test/java/cn/haut/survivor/service/ExplorationServiceTests.java
git commit -m "feat: add buddy assists to exploration"
```

---

### Task 7: Update Weekly Goals and Achievements

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java`
- Test: `src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java`

- [ ] **Step 1: Add tests for new goal types**

Append to `WeeklyGoalServiceTests`:

```java
@Test
void rumorEffectGoalStartsFromZeroAndProgresses() {
    WeeklyGoal goal = weeklyGoalService.listActiveGoals().stream()
            .filter(g -> "rumor_hunter".equals(g.getGoalKey()))
            .findFirst()
            .orElseThrow();

    UserWeeklyGoal userGoal = weeklyGoalService.chooseGoal(2L, 1, goal.getId());
    assertThat(userGoal.getStartValue()).isZero();

    weeklyGoalService.updateProgress(2L, 1, "rumor_effect_used", 2);

    UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
    assertThat(updated.getCurrentValue()).isEqualTo(2);
    assertThat(updated.getCompleted()).isEqualTo(1);
}

@Test
void storyStepGoalStartsFromZeroAndProgresses() {
    WeeklyGoal goal = weeklyGoalService.listActiveGoals().stream()
            .filter(g -> "story_chaser".equals(g.getGoalKey()))
            .findFirst()
            .orElseThrow();

    weeklyGoalService.chooseGoal(2L, 1, goal.getId());
    weeklyGoalService.updateProgress(2L, 1, "exploration_story_step", 2);

    UserWeeklyGoal updated = weeklyGoalService.getCurrentGoal(2L, 1);
    assertThat(updated.getCompleted()).isEqualTo(1);
}
```

- [ ] **Step 2: Run failing or passing tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyGoalServiceTests test
```

Expected: These may already PASS through the default branch, but the next step makes the count-based intent explicit.

- [ ] **Step 3: Add new goal types to count-based branch**

In `WeeklyGoalServiceImpl.getStartValueForGoalType`, extend:

```java
case "npc_interaction", "buddy_selected", "familiarity_gain" -> {
    return 0;
}
```

to:

```java
case "npc_interaction", "buddy_selected", "familiarity_gain",
     "rumor_effect_used", "exploration_story_step", "weekly_modifier_used", "buddy_assist" -> {
    return 0;
}
```

- [ ] **Step 4: Run tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeeklyGoalServiceTests test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/WeeklyGoalServiceImpl.java src/test/java/cn/haut/survivor/service/WeeklyGoalServiceTests.java
git commit -m "feat: support exploration influence weekly goals"
```

---

### Task 8: Update Exploration and Map UI

**Files:**
- Modify: `src/main/java/cn/haut/survivor/controller/ExplorationController.java`
- Modify: `src/main/java/cn/haut/survivor/controller/MapController.java`
- Modify: `src/main/resources/templates/exploration/index.html`
- Modify: `src/main/resources/templates/exploration/result.html`
- Modify: `src/main/resources/templates/map/index.html`
- Modify: `src/main/resources/static/css/app.css`
- Test: relevant controller tests

- [ ] **Step 1: Add model attributes for visible rumor locations**

In `ExplorationController.explorationPage`, add:

```java
Map<Long, Long> rumorCountsByLocation = rumorService.pickVisibleRumorsForUser(userId, profile.getCurrentWeek()).stream()
        .filter(r -> r.getLocationId() != null)
        .collect(java.util.stream.Collectors.groupingBy(
                cn.haut.survivor.domain.entity.Rumor::getLocationId,
                java.util.stream.Collectors.counting()));
model.addAttribute("rumorCountsByLocation", rumorCountsByLocation);
```

In `MapController`, add the same model attribute on the map page method that currently adds `rumors`.

- [ ] **Step 2: Add influence panel to exploration result template**

In `src/main/resources/templates/exploration/result.html`, after the exploration gain block and before unlock hints, add:

```html
<section th:if="${result.influences != null and !result.influences.isEmpty()}" class="influence-panel">
    <p class="influence-panel__title">本次影响来源</p>
    <div class="influence-panel__list">
        <article class="influence-chip" th:each="influence : ${result.influences}">
            <span class="influence-chip__type" th:text="${influence.sourceType}">source</span>
            <div class="influence-chip__body">
                <strong th:text="${influence.sourceName}">来源名称</strong>
                <p th:text="${influence.description}">影响说明</p>
            </div>
        </article>
    </div>
</section>

<section th:if="${result.storyResult != null and result.storyResult.isPresent()}" class="story-progress-card">
    <span class="story-progress-card__eyebrow">校园奇遇</span>
    <h2 th:text="${result.storyResult.get().chain.chainName}">奇遇链</h2>
    <p th:text="${result.storyResult.get().storyText}">奇遇内容</p>
    <span th:if="${result.storyResult.get().completed}" class="story-progress-card__done">已完成</span>
</section>
```

- [ ] **Step 3: Add rumor badges to exploration cards**

In `src/main/resources/templates/exploration/index.html`, inside each location card, add:

```html
<span th:if="${rumorCountsByLocation != null and rumorCountsByLocation[loc.id] != null}"
      class="explore-card__badge explore-card__badge--rumor">有传闻</span>
```

The current template uses `th:each="loc : ${locations}"`, so keep `loc.id` exactly as shown.

- [ ] **Step 4: Add CSS**

Append to `src/main/resources/static/css/app.css`:

```css
.influence-panel,
.story-progress-card {
    margin: 16px 0;
    padding: 14px;
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.12);
    background: rgba(15, 23, 42, 0.7);
}

.influence-panel__title,
.story-progress-card__eyebrow {
    margin: 0 0 10px;
    color: rgba(255, 255, 255, 0.74);
    font-size: 0.86rem;
}

.influence-panel__list {
    display: grid;
    gap: 8px;
}

.influence-chip {
    display: grid;
    grid-template-columns: auto 1fr;
    gap: 10px;
    align-items: start;
    padding: 10px;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.08);
}

.influence-chip__type {
    padding: 3px 8px;
    border-radius: 999px;
    background: rgba(34, 197, 94, 0.18);
    color: #bbf7d0;
    font-size: 0.75rem;
}

.influence-chip__body strong,
.story-progress-card h2 {
    color: #fff;
}

.influence-chip__body p,
.story-progress-card p {
    margin: 4px 0 0;
    color: rgba(255, 255, 255, 0.72);
}

.story-progress-card__done {
    display: inline-flex;
    margin-top: 10px;
    padding: 4px 9px;
    border-radius: 999px;
    background: rgba(59, 130, 246, 0.18);
    color: #bfdbfe;
}
```

- [ ] **Step 5: Run controller/template tests**

Run:

```powershell
.\mvnw.cmd "-Dtest=MapControllerTests,DashboardControllerTests,WeekSummaryControllerTests" test
```

Expected: PASS. There is no `ExplorationControllerTests` class in the current repo, so keep the test list exactly as shown.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/controller/ExplorationController.java src/main/java/cn/haut/survivor/controller/MapController.java src/main/resources/templates/exploration/index.html src/main/resources/templates/exploration/result.html src/main/resources/templates/map/index.html src/main/resources/static/css/app.css
git commit -m "feat: show exploration influences"
```

---

### Task 9: Add Week Summary Influence Feedback

**Files:**
- Modify: `src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java`
- Modify: `src/main/resources/templates/week/summary.html`
- Test: `src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java`

- [ ] **Step 1: Add summary tests**

In `WeekSummaryServiceTests`, add this field after `PlayerAttributeMapper playerAttributeMapper`:

```java
@Autowired
private ExplorationStoryService explorationStoryService;
```

Then append this test method:

```java
@Test
void summaryMentionsStoryProgressWhenTriggered() {
    explorationStoryService.triggerSpecificStep(2L, "library_seat", 1, 1);

    WeekSummaryView view = weekSummaryService.buildCurrentWeekSummary(2L, 1);

    assertThat(view.summaryText()).contains("奇遇");
}
```

- [ ] **Step 2: Run failing test**

Run:

```powershell
.\mvnw.cmd -Dtest=WeekSummaryServiceTests#summaryMentionsStoryProgressWhenTriggered test
```

Expected: FAIL because summary does not inspect story progress yet.

- [ ] **Step 3: Inject story progress mapper**

In `WeekSummaryServiceImpl`, add field:

```java
private final ExplorationStoryProgressMapper explorationStoryProgressMapper;
```

Add constructor parameter and assignment:

```java
ExplorationStoryProgressMapper explorationStoryProgressMapper,
```

```java
this.explorationStoryProgressMapper = explorationStoryProgressMapper;
```

Add import:

```java
import cn.haut.survivor.mapper.ExplorationStoryProgressMapper;
```

- [ ] **Step 4: Add story context to summary text**

In `buildCurrentWeekSummary`, before generating `summaryText`, add:

```java
boolean hasStoryProgressThisWeek = explorationStoryProgressMapper.selectCount(
        new LambdaQueryWrapper<ExplorationStoryProgress>()
                .eq(ExplorationStoryProgress::getUserId, userId)
                .eq(ExplorationStoryProgress::getLastTriggerWeek, weekNumber)) > 0;
```

Add import:

```java
import cn.haut.survivor.domain.entity.ExplorationStoryProgress;
```

Extend `generateSummaryText` signature:

```java
private String generateSummaryText(PlayerAttribute attr, boolean goalCompleted, boolean goalClaimed, int npcCount,
                                   int weekNumber, boolean hasWeeklyBuddy, boolean hasNpcInteractionThisWeek,
                                   boolean hasStoryProgressThisWeek)
```

Update the call:

```java
String summaryText = generateSummaryText(attribute, goalCompleted, goalClaimed, knownNpcCount, weekNumber,
        currentBuddy != null, hasNpcInteractionThisWeek, hasStoryProgressThisWeek);
```

Inside `generateSummaryText`, after the week 4 branches and before buddy branches, add:

```java
if (hasStoryProgressThisWeek && goalCompleted) {
    return "这周你不只是完成了计划，还撞见了校园里正在发生的小奇遇。";
}
if (hasStoryProgressThisWeek) {
    return "这周你推进了一段校园奇遇，虽然日程不算完美，但生活感明显变强了。";
}
```

- [ ] **Step 5: Run tests**

Run:

```powershell
.\mvnw.cmd -Dtest=WeekSummaryServiceTests test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/cn/haut/survivor/service/impl/WeekSummaryServiceImpl.java src/test/java/cn/haut/survivor/service/WeekSummaryServiceTests.java
git commit -m "feat: summarize exploration stories"
```

---

### Task 10: Final Verification and Smoke Test

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

- [ ] **Step 2: Start temporary server on port 8081**

Run:

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
for ($i=0; $i -lt 45; $i++) {
  try {
    $r = Invoke-WebRequest -Uri http://localhost:8081/login -UseBasicParsing -TimeoutSec 2
    if ($r.StatusCode -eq 200) { 'UP'; break }
  } catch { Start-Sleep -Seconds 1 }
}
```

- [ ] **Step 3: HTTP smoke**

Run:

```powershell
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-WebRequest -Uri http://localhost:8081/login -Method Post -Body @{username='student'; password='student123'} -WebSession $session -UseBasicParsing -MaximumRedirection 5 | Out-Null
try {
  Invoke-WebRequest -Uri http://localhost:8081/player/create -Method Post -Body @{playerName='CP4 Smoke'; grade='大二'; majorType='计算机类'; growthRoute='就业路线'} -WebSession $session -UseBasicParsing -MaximumRedirection 5 | Out-Null
} catch {}

$pages = @(
  'http://localhost:8081/dashboard',
  'http://localhost:8081/map',
  'http://localhost:8081/exploration',
  'http://localhost:8081/week/summary'
)

foreach ($url in $pages) {
  $r = Invoke-WebRequest -Uri $url -WebSession $session -UseBasicParsing -MaximumRedirection 5
  $body = [string]$r.Content
  Write-Output ("{0}: status={1}; whitelabel={2}; dock={3}" -f $url, $r.StatusCode, $body.Contains('Whitelabel'), $body.Contains('game-dock'))
}

$result = Invoke-WebRequest -Uri http://localhost:8081/exploration/2 -Method Post -WebSession $session -UseBasicParsing -MaximumRedirection 5
$resultBody = [string]$result.Content
Write-Output ("explore-result: status={0}; whitelabel={1}; influence={2}; dock={3}" -f $result.StatusCode, $resultBody.Contains('Whitelabel'), $resultBody.Contains('本次影响来源'), $resultBody.Contains('game-dock'))
```

Expected:

```text
status=200
whitelabel=False
dock=True
explore-result influence=True
```

- [ ] **Step 4: Stop port 8081**

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

- [ ] **Step 5: Inspect and commit verification fixes**

Run `git status --short`. When the output is empty, there is no verification-fix commit to create. When the output lists files changed during smoke/full-test fixes, commit them:

```powershell
git status --short
git add src/main/java src/main/resources src/test/java
git commit -m "fix: stabilize content pack 4"
```

- [ ] **Step 6: Final report**

Report:

- Modified files.
- Schema changes.
- Seed data counts.
- New tests.
- Full test result.
- HTTP smoke result.
- Remaining risks.

Do not claim completion unless `.\mvnw.cmd clean test` was run after the final code change and showed zero failures/errors.

---

## Plan Self-Review

Spec coverage:

- 周主题机制化：Task 2 and Task 5.
- 传闻效果机制化：Task 1, Task 3, Task 5, Task 8.
- 探索奇遇链：Task 1, Task 4, Task 5, Task 8, Task 9.
- NPC 搭子外溢：Task 6.
- UI 展示：Task 8 and Task 9.
- 周目标/成就：Task 1, Task 5, Task 7.
- 测试与验证：Tasks 1-10.

Scope decisions:

- No dynamic simulation engine.
- No admin CRUD.
- No free-form story editor.
- No broad event probability refactor.
- Exploration remains the main integration point.
