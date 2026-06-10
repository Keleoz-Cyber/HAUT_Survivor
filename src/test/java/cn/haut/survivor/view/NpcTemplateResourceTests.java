package cn.haut.survivor.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NpcTemplateResourceTests {

    @Test
    void npcPlayerTemplatesDoNotReferenceMissingWebjarAssets() throws Exception {
        List<Path> templates = List.of(
                Path.of("src/main/resources/templates/npc/detail.html"),
                Path.of("src/main/resources/templates/npc/result.html")
        );

        for (Path template : templates) {
            assertThat(Files.readString(template))
                    .as(template.toString())
                    .doesNotContain("/webjars/");
        }
    }
}
