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
                        "实验数据\"蒸发\"",
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
