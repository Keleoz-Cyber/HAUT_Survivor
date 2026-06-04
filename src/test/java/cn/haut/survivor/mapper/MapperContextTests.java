package cn.haut.survivor.mapper;

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
class MapperContextTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void userMapperLoadsSeedUsers() {
        assertThat(userMapper.selectCount(null)).isEqualTo(2);
    }
}
