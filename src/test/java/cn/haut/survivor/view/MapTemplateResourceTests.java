package cn.haut.survivor.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MapTemplateResourceTests {

    @Test
    void mapTemplateReferencesRealCampusMapAssetAndHotspots() throws Exception {
        Path image = Path.of("src/main/resources/static/images/lianhuajie-campus-map.jpeg");
        Path template = Path.of("src/main/resources/templates/map/index.html");

        assertThat(image).exists();
        assertThat(Files.size(image)).isGreaterThan(100_000);

        String html = Files.readString(template);
        assertThat(html)
                .contains("/images/lianhuajie-campus-map.jpeg")
                .contains("campus-map-photo")
                .contains("campusMapHotspots")
                .contains("campus-map-hotspot");
    }
}
