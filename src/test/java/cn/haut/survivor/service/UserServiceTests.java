package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.thymeleaf.check-template-location=false",
        "debug=false",
        "logging.level.org.springframework=INFO"
})
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Test
    void duplicateUsernameRegistrationFails() {
        assertThatThrownBy(() -> userService.register("student", "student123", "另一个学生"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void passwordShorterThanSixCharactersFails() {
        assertThatThrownBy(() -> userService.register("short-pass-user", "12345", "短密码用户"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码长度不能少于 6 位");
    }

    @Test
    void loginSucceedsWithValidCredentials() {
        User user = userService.login("student", "student123");

        assertThat(user.getUsername()).isEqualTo("student");
        assertThat(user.getRole()).isEqualTo("USER");
    }

    @Test
    void loginFailsWithInvalidPassword() {
        assertThatThrownBy(() -> userService.login("student", "bad-password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名或密码错误");
    }
}
