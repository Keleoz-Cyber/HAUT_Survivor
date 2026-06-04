package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.PlayerAttribute;
import cn.haut.survivor.domain.entity.PlayerProfile;
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
class PlayerServiceTests {

    @Autowired
    private PlayerService playerService;

    @Test
    void creatingProfileInitializesDefaultAttributes() {
        playerService.createProfile(2L, "莲花街新生", "大一", "计算机类", "考研路线");

        PlayerProfile profile = playerService.findProfileByUserId(2L);
        PlayerAttribute attribute = playerService.findAttributeByUserId(2L);

        assertThat(profile.getPlayerName()).isEqualTo("莲花街新生");
        assertThat(profile.getLevel()).isEqualTo(1);
        assertThat(profile.getExp()).isZero();
        assertThat(attribute.getAcademic()).isEqualTo(70);
        assertThat(attribute.getHealth()).isEqualTo(70);
        assertThat(attribute.getMoney()).isEqualTo(80);
        assertThat(attribute.getSocial()).isEqualTo(50);
        assertThat(attribute.getSkill()).isEqualTo(40);
        assertThat(attribute.getPressure()).isEqualTo(35);
        assertThat(attribute.getDiscipline()).isEqualTo(55);
    }

    @Test
    void growthRouteAdjustsAttributes() {
        playerService.createProfile(2L, "就业玩家", "大三", "软件工程", "就业路线");

        PlayerAttribute attribute = playerService.findAttributeByUserId(2L);

        assertThat(attribute.getAcademic()).isEqualTo(60);
        assertThat(attribute.getSkill()).isEqualTo(50);
        assertThat(attribute.getSocial()).isEqualTo(55);
        assertThat(attribute.getPressure()).isEqualTo(30);
    }

    @Test
    void userWithProfileCanBeDetected() {
        assertThat(playerService.hasProfile(2L)).isFalse();

        playerService.createProfile(2L, "已建档玩家", "大二", "自动化", "六边形路线");

        assertThat(playerService.hasProfile(2L)).isTrue();
    }
}
