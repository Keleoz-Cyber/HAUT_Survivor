package cn.haut.survivor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HautSurvivorApplicationTests {

    @Test
    void applicationEntryPointExists() {
        assertThat(HautSurvivorApplication.class).isNotNull();
    }
}
