package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerProfile;
import cn.haut.survivor.domain.entity.Task;
import cn.haut.survivor.domain.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class TaskServiceTests {

    @Autowired
    private TaskService taskService;

    @Autowired
    private PlayerService playerService;

    @BeforeEach
    void setUpPlayer() {
        playerService.createProfile(2L, "任务测试玩家", "大二", "计算机类", "就业路线");
    }

    @Test
    void userCanCreateTask() {
        Task task = taskService.createTask(2L, "完成 Java 课设", "课程", "A",
                LocalDateTime.now().plusDays(3), "完成登录模块");

        assertThat(task.getId()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING.name());
        assertThat(task.getRewardExp()).isEqualTo(70);
    }

    @Test
    void userCanCompleteOwnTask() {
        Task task = taskService.createTask(2L, "整理实验报告", "实验", "B",
                LocalDateTime.now().plusDays(1), "补充截图");

        Task completed = taskService.completeTask(2L, task.getId());

        assertThat(completed.getStatus()).isEqualTo(TaskStatus.DONE.name());
        assertThat(completed.getFinishTime()).isNotNull();
    }

    @Test
    void completingTaskGrantsExperience() {
        Task task = taskService.createTask(2L, "冲刺竞赛题", "竞赛", "S",
                LocalDateTime.now().plusDays(7), "刷题训练");

        taskService.completeTask(2L, task.getId());

        PlayerProfile profile = playerService.findProfileByUserId(2L);
        assertThat(profile.getExp()).isEqualTo(120);
    }

    @Test
    void userCannotCompleteAnotherUsersTask() {
        Task task = taskService.createTask(2L, "个人任务", "生活", "C",
                LocalDateTime.now().plusDays(2), "只属于自己");

        assertThatThrownBy(() -> taskService.completeTask(1L, task.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能完成其他用户的任务");
    }
}
