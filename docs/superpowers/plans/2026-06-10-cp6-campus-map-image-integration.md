# CP6 Campus Map Image Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Insert the real Lianhuajie campus map image from `docs/补充信息.docx` into the game `/map` page as an interactive visual layer.

**Architecture:** Keep the existing map gameplay unchanged: `/map` still uses current locations, events, rumors, and action-point flow. Add the docx map image as a static asset, add controller-provided hotspot metadata, render clickable percentage-positioned hotspots over the image, and keep the current location cards below as the reliable fallback entry.

**Tech Stack:** Spring Boot 3.3.5, Java 17, Thymeleaf, CSS, JUnit 5, AssertJ, MockMvc, Windows PowerShell.

---

## Scope

Implement this as a CP6 UI/content companion, not a new map engine.

In scope:

- Extract `word/media/image1.jpeg` from `docs/补充信息.docx`.
- Save it as `src/main/resources/static/images/lianhuajie-campus-map.jpeg`.
- Add 8 hotspot markers matching existing location ids.
- Render the image and hotspots on `src/main/resources/templates/map/index.html`.
- Preserve the existing `.campus-map__grid` location cards.
- Add CSS for desktop and mobile.
- Add tests for controller model data and asset/template references.
- Run clean tests, HTTP smoke, and browser visual checks.

Out of scope:

- No new database table for map coordinates.
- No schema changes.
- No map editor.
- No zoom/pan library.
- No replacement of existing location cards.
- No new event or exploration mechanics.

## Location Hotspot Mapping

Use one hotspot per existing location id.

| location id | Hotspot label | Approximate x/y percent | Notes |
|---:|---|---:|---|
| `1` | 教学楼群 | `75, 66` | 博识/博学/博闻教学楼群 |
| `2` | 图书馆 | `55, 51` | 图书馆中部湖边 |
| `3` | 宿舍区 | `62, 18` | 德园 A/B/C/D 主宿舍区 |
| `4` | 食堂 | `35, 43` | 知味餐厅附近，知雅餐厅由卡片文案覆盖 |
| `5` | 操场 | `31, 16` | 东西操场区域 |
| `6` | 实验楼 | `50, 69` | 惟学楼/粮食科创实验中心一带 |
| `7` | 社团区 | `68, 35` | 韶华楼/钟楼广场/社团活动区 |
| `8` | 快递站 | `39, 65` | 31 号楼北侧驿站近似位置 |

Coordinates are allowed to be adjusted by up to 5 percentage points during browser visual QA if the marker visibly misses the intended area.

## File Map

- Create: `src/main/resources/static/images/lianhuajie-campus-map.jpeg`
  - Static asset extracted from user-provided docx.
- Modify: `src/main/java/cn/haut/survivor/controller/MapController.java`
  - Adds a small `CampusMapHotspot` record and model attribute.
- Modify: `src/main/resources/templates/map/index.html`
  - Renders image map before the existing card grid.
- Modify: `src/main/resources/static/css/app.css`
  - Adds `.campus-map-photo` styles and mobile adjustments.
- Modify: `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`
  - Verifies `/map` provides hotspot model data.
- Create: `src/test/java/cn/haut/survivor/view/MapTemplateResourceTests.java`
  - Verifies the image asset exists and template references expected classes/attributes.
- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
  - Records map image integration and verification.
- Modify: `docs/NEXT_AI_HANDOFF.md`
  - Adds handoff notes and remaining map risks.

## Task 1: Preflight

**Files:**
- Read: `docs/补充信息.docx`
- Read: `src/main/resources/templates/map/index.html`
- Read: `src/main/resources/static/css/app.css`
- Read: `src/main/java/cn/haut/survivor/controller/MapController.java`
- Read: `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`

- [ ] **Step 1: Inspect worktree**

Run:

```powershell
git status --short
```

Expected:

```text
May show existing modified/untracked CP4/CP5/CP6 files. Do not revert unrelated changes.
```

- [ ] **Step 2: Confirm docx contains the map image**

Run:

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$docx = Resolve-Path 'docs\补充信息.docx'
$zip = [System.IO.Compression.ZipFile]::OpenRead($docx)
$zip.Entries | Where-Object { $_.FullName -like 'word/media/*' } | Select-Object FullName, Length
$zip.Dispose()
```

Expected:

```text
FullName               Length
--------               ------
word/media/image1.jpeg 174826
```

The exact length may differ slightly if the docx was edited, but the file must be a JPEG campus map.

## Task 2: Extract Static Map Asset

**Files:**
- Create: `src/main/resources/static/images/lianhuajie-campus-map.jpeg`

- [ ] **Step 1: Extract the JPEG from docx**

Run:

```powershell
New-Item -ItemType Directory -Force src\main\resources\static\images | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$docx = Resolve-Path 'docs\补充信息.docx'
$out = Resolve-Path 'src\main\resources\static\images'
$target = Join-Path $out 'lianhuajie-campus-map.jpeg'
$zip = [System.IO.Compression.ZipFile]::OpenRead($docx)
$entry = $zip.GetEntry('word/media/image1.jpeg')
$stream = $entry.Open()
$file = [System.IO.File]::Create($target)
$stream.CopyTo($file)
$file.Close()
$stream.Close()
$zip.Dispose()
Get-Item $target | Select-Object FullName, Length
```

Expected:

```text
FullName                                                                                                      Length
--------                                                                                                      ------
D:\study\code\java\classlearn\HAUT_Survivor\src\main\resources\static\images\lianhuajie-campus-map.jpeg      174826
```

- [ ] **Step 2: Verify dimensions**

Run:

```powershell
Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Image]::FromFile((Resolve-Path 'src\main\resources\static\images\lianhuajie-campus-map.jpeg'))
"width=$($img.Width) height=$($img.Height)"
$img.Dispose()
```

Expected:

```text
width=1267 height=679
```

If dimensions differ slightly, keep the file and continue. The CSS uses responsive image sizing and percentage hotspots.

## Task 3: Add Hotspot Model To MapController

**Files:**
- Modify: `src/main/java/cn/haut/survivor/controller/MapController.java`
- Test: `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`

- [ ] **Step 1: Add the failing controller test**

In `src/test/java/cn/haut/survivor/controller/MapControllerTests.java`, add this import if it is not already present:

```java
import java.util.List;
```

Add this test method inside `class MapControllerTests`:

```java
@Test
@SuppressWarnings("unchecked")
void mapPageProvidesRealCampusMapHotspots() throws Exception {
    var result = mockMvc.perform(get("/map")
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ID, 2L)
                    .sessionAttr(LoginInterceptor.LOGIN_USER_ROLE, "USER"))
            .andExpect(status().isOk())
            .andExpect(view().name("map/index"))
            .andExpect(model().attributeExists("campusMapHotspots"))
            .andReturn();

    List<MapController.CampusMapHotspot> hotspots =
            (List<MapController.CampusMapHotspot>) result.getModelAndView()
                    .getModel()
                    .get("campusMapHotspots");

    assertThat(hotspots).hasSize(8);
    assertThat(hotspots).extracting(MapController.CampusMapHotspot::locationId)
            .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
    assertThat(hotspots).extracting(MapController.CampusMapHotspot::label)
            .containsExactly("教学楼群", "图书馆", "宿舍区", "食堂", "操场", "实验楼", "社团区", "快递站");
    assertThat(hotspots).allMatch(hotspot -> hotspot.x() >= 0 && hotspot.x() <= 100);
    assertThat(hotspots).allMatch(hotspot -> hotspot.y() >= 0 && hotspot.y() <= 100);
}
```

- [ ] **Step 2: Run the failing test**

Run:

```powershell
.\mvnw.cmd -Dtest=MapControllerTests#mapPageProvidesRealCampusMapHotspots test
```

Expected:

```text
Failure because model attribute "campusMapHotspots" does not exist.
```

- [ ] **Step 3: Add hotspot record and builder**

In `src/main/java/cn/haut/survivor/controller/MapController.java`, add this nested record inside `public class MapController`, before the constructor:

```java
    public record CampusMapHotspot(Long locationId, String label, String shortLabel, int x, int y) {
    }
```

Add this private method near `buildExploreLevelMap`:

```java
    private List<CampusMapHotspot> buildCampusMapHotspots() {
        return List.of(
                new CampusMapHotspot(1L, "教学楼群", "教学", 75, 66),
                new CampusMapHotspot(2L, "图书馆", "图书馆", 55, 51),
                new CampusMapHotspot(3L, "宿舍区", "宿舍", 62, 18),
                new CampusMapHotspot(4L, "食堂", "食堂", 35, 43),
                new CampusMapHotspot(5L, "操场", "操场", 31, 16),
                new CampusMapHotspot(6L, "实验楼", "实验", 50, 69),
                new CampusMapHotspot(7L, "社团区", "社团", 68, 35),
                new CampusMapHotspot(8L, "快递站", "快递", 39, 65)
        );
    }
```

- [ ] **Step 4: Add model attribute in both map rendering paths**

In `map(...)`, after `model.addAttribute("exploreLevels", exploreLevels);`, add:

```java
        model.addAttribute("campusMapHotspots", buildCampusMapHotspots());
```

In `buildMapRedirect(...)`, after `model.addAttribute("exploreLevels", buildExploreLevelMap(userId));`, add:

```java
        model.addAttribute("campusMapHotspots", buildCampusMapHotspots());
```

- [ ] **Step 5: Run the controller test**

Run:

```powershell
.\mvnw.cmd -Dtest=MapControllerTests#mapPageProvidesRealCampusMapHotspots test
```

Expected:

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Task 4: Render Image Map In Thymeleaf

**Files:**
- Modify: `src/main/resources/templates/map/index.html`
- Test: `src/test/java/cn/haut/survivor/view/MapTemplateResourceTests.java`

- [ ] **Step 1: Add the resource/template test**

Create `src/test/java/cn/haut/survivor/view/MapTemplateResourceTests.java`:

```java
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
```

- [ ] **Step 2: Run the failing resource/template test**

Run:

```powershell
.\mvnw.cmd -Dtest=MapTemplateResourceTests test
```

Expected:

```text
Failure because map/index.html does not yet reference the image and hotspot classes.
```

- [ ] **Step 3: Insert the map image section**

In `src/main/resources/templates/map/index.html`, insert this block after the message block:

```html
    <!-- ========== 莲花街校区真实地图 ========== -->
    <section class="campus-map-photo" th:if="${!semesterOver}">
        <div class="campus-map-photo__header">
            <p class="campus-map-photo__eyebrow">真实校区图</p>
            <h2 class="campus-map-photo__title">莲花街校区一览</h2>
        </div>
        <div class="campus-map-photo__frame" aria-label="莲花街校区真实地图">
            <img src="/images/lianhuajie-campus-map.jpeg"
                 alt="河南工业大学莲花街校区地图"
                 class="campus-map-photo__image">

            <a th:if="${profile.actionPoints > 0}"
               th:each="hotspot : ${campusMapHotspots}"
               class="campus-map-hotspot"
               th:href="@{/map/location/{id}/event(id=${hotspot.locationId})}"
               th:style="|--hotspot-x:${hotspot.x}%;--hotspot-y:${hotspot.y}%;|"
               th:attr="aria-label=|前往${hotspot.label}|">
                <span class="campus-map-hotspot__dot"></span>
                <span class="campus-map-hotspot__label" th:text="${hotspot.shortLabel}">地点</span>
            </a>

            <span th:if="${profile.actionPoints == 0}"
                  th:each="hotspot : ${campusMapHotspots}"
                  class="campus-map-hotspot campus-map-hotspot--disabled"
                  th:style="|--hotspot-x:${hotspot.x}%;--hotspot-y:${hotspot.y}%;|"
                  th:attr="aria-label=|行动点不足，无法前往${hotspot.label}|">
                <span class="campus-map-hotspot__dot"></span>
                <span class="campus-map-hotspot__label" th:text="${hotspot.shortLabel}">地点</span>
            </span>
        </div>
    </section>
```

Keep the existing `.campus-map__grid` section directly below this new section.

- [ ] **Step 4: Run the template resource test**

Run:

```powershell
.\mvnw.cmd -Dtest=MapTemplateResourceTests test
```

Expected:

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Task 5: Add CSS For Image Map And Hotspots

**Files:**
- Modify: `src/main/resources/static/css/app.css`

- [ ] **Step 1: Add desktop CSS near existing campus map CSS**

In `src/main/resources/static/css/app.css`, add this block after `.campus-map__msg` and before `.campus-map__grid`:

```css
.campus-map-photo {
    background: rgba(15, 23, 42, 0.78);
    border: 1px solid rgba(148, 163, 184, 0.18);
    border-radius: 12px;
    margin-bottom: 18px;
    overflow: hidden;
}

.campus-map-photo__header {
    align-items: baseline;
    display: flex;
    gap: 10px;
    justify-content: space-between;
    padding: 14px 16px 10px;
}

.campus-map-photo__eyebrow {
    color: #86efac;
    font-size: 0.76rem;
    font-weight: 800;
    margin: 0;
}

.campus-map-photo__title {
    color: #f8fafc;
    font-size: 1rem;
    font-weight: 800;
    margin: 0;
}

.campus-map-photo__frame {
    aspect-ratio: 1267 / 679;
    background: #0f172a;
    overflow: hidden;
    position: relative;
}

.campus-map-photo__image {
    display: block;
    height: 100%;
    object-fit: cover;
    width: 100%;
}

.campus-map-hotspot {
    align-items: center;
    background: rgba(15, 23, 42, 0.82);
    border: 1px solid rgba(255, 255, 255, 0.72);
    border-radius: 999px;
    color: #f8fafc;
    display: inline-flex;
    gap: 5px;
    left: var(--hotspot-x);
    max-width: 92px;
    padding: 4px 8px 4px 5px;
    position: absolute;
    text-decoration: none;
    top: var(--hotspot-y);
    transform: translate(-50%, -50%);
    transition: transform 0.12s ease, background 0.12s ease, box-shadow 0.12s ease;
    white-space: nowrap;
}

.campus-map-hotspot:hover {
    background: rgba(22, 163, 74, 0.92);
    box-shadow: 0 8px 22px rgba(2, 6, 23, 0.35);
    color: #ffffff;
    transform: translate(-50%, -50%) scale(1.04);
}

.campus-map-hotspot__dot {
    background: #fbbf24;
    border: 2px solid #ffffff;
    border-radius: 999px;
    flex: 0 0 auto;
    height: 12px;
    width: 12px;
}

.campus-map-hotspot__label {
    font-size: 0.72rem;
    font-weight: 800;
    line-height: 1;
    overflow: hidden;
    text-overflow: ellipsis;
}

.campus-map-hotspot--disabled {
    opacity: 0.58;
}
```

- [ ] **Step 2: Add mobile CSS inside existing `@media (max-width: 640px)` block**

Inside the existing mobile block that already contains `.campus-map`, add:

```css
    .campus-map-photo {
        margin-bottom: 14px;
    }

    .campus-map-photo__header {
        padding: 10px 12px 8px;
    }

    .campus-map-photo__title {
        font-size: 0.92rem;
    }

    .campus-map-photo__frame {
        aspect-ratio: 1267 / 679;
    }

    .campus-map-hotspot {
        gap: 3px;
        max-width: 58px;
        padding: 3px 5px 3px 4px;
    }

    .campus-map-hotspot__dot {
        height: 9px;
        width: 9px;
    }

    .campus-map-hotspot__label {
        font-size: 0.62rem;
    }
```

- [ ] **Step 3: Run focused tests**

Run:

```powershell
.\mvnw.cmd -Dtest=MapControllerTests#mapPageProvidesRealCampusMapHotspots,MapTemplateResourceTests test
```

Expected:

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Task 6: Full Verification

**Files:**
- No source modifications expected unless verification reveals a real regression.

- [ ] **Step 1: Run full tests**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
```

Record total tests run, failures, errors, and skipped.

- [ ] **Step 2: Start local app**

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected:

```text
Started HautSurvivorApplication
```

- [ ] **Step 3: HTTP smoke**

Check:

```text
GET /dashboard
GET /map
GET /map/location/2/event
GET /exploration
POST /exploration/4
GET /week/summary
```

Expected:

```text
All return 200 or an existing intentional redirect.
No Whitelabel page.
No missing static image 404 for /images/lianhuajie-campus-map.jpeg.
```

- [ ] **Step 4: Browser visual check**

Desktop viewport:

```text
1366x768
```

Mobile viewport:

```text
375x812
```

Check `/map` for:

- Campus image is visible and not distorted.
- Hotspots sit inside the map image.
- Hotspot labels do not overlap so badly that they block map reading.
- Existing location cards still render below the image.
- "有传闻" badges on cards remain visible.
- Bottom dock does not cover the final visible actions on mobile.
- No horizontal scrolling.

If one or two hotspot positions are visibly off, adjust only the numeric x/y values in `buildCampusMapHotspots()`, rerun `MapControllerTests#mapPageProvidesRealCampusMapHotspots`, and repeat browser visual check.

## Task 7: Documentation Update

**Files:**
- Modify: `docs/PROJECT_COMPLETION_STATUS.md`
- Modify: `docs/NEXT_AI_HANDOFF.md`

- [ ] **Step 1: Update completion status**

Add this section to `docs/PROJECT_COMPLETION_STATUS.md`, with actual verification numbers filled from the run:

```markdown
## CP6 真实校园地图接入

状态：已完成

范围：
- 从 `docs/补充信息.docx` 提取莲花街校区地图图片。
- 新增静态资源 `src/main/resources/static/images/lianhuajie-campus-map.jpeg`。
- `/map` 页面新增真实地图展示层和 8 个地点热点。
- 热点复用现有地点 id 和 `/map/location/{id}/event` 事件流程。
- 保留原有地点卡片作为稳定入口。
- 未新增数据库表，未修改地图核心机制。

验证：
- `.\mvnw.cmd clean test`：Tests run: X, Failures: 0, Errors: 0, Skipped: X，BUILD SUCCESS。
- HTTP 冒烟：`/dashboard`、`/map`、`/map/location/2/event`、`/exploration`、`POST /exploration/4`、`/week/summary`。
- 浏览器视觉检查：1366x768 与 375x812，记录实际结果。
```

- [ ] **Step 2: Update handoff**

Add this section to `docs/NEXT_AI_HANDOFF.md`:

```markdown
## CP6 真实校园地图接入交接

已完成：
- `/map` 页面顶部展示莲花街校区真实地图。
- 地图热点由 `MapController.CampusMapHotspot` 提供，坐标为百分比。
- 热点点击复用现有 `/map/location/{id}/event` 流程。
- 原地点卡片未删除，仍作为主要稳定入口。

剩余风险：
- 热点坐标是按当前图片人工估算的，后续如果替换更高清地图，需要重新微调。
- 移动端热点标签空间有限，目前以短标签为主。
- 这不是完整地图系统，没有缩放、拖拽或楼层切换。
```

## Task 8: Coding AI Feedback Template

**Files:**
- No source modifications expected.

- [ ] **Step 1: Report back in this format**

```markdown
# CP6 真实校园地图接入反馈

## 1. 修改文件
- 列出所有修改/新增文件。

## 2. 图片资源
- 是否从 `docs/补充信息.docx` 提取。
- 最终路径。
- 图片尺寸和大小。

## 3. 实现方式
- Controller 是否新增 `CampusMapHotspot`。
- `/map` 是否保留原地点卡片。
- 热点点击是否复用现有事件流程。

## 4. 测试结果
- `MapControllerTests#mapPageProvidesRealCampusMapHotspots`
- `MapTemplateResourceTests`
- `.\mvnw.cmd clean test`

## 5. HTTP 冒烟
- `/dashboard`
- `/map`
- `/map/location/2/event`
- `/exploration`
- `POST /exploration/4`
- `/week/summary`

## 6. 浏览器视觉检查
- 1366x768：图片、热点、卡片、Dock、横向滚动结果。
- 375x812：图片、热点、卡片、Dock、横向滚动结果。

## 7. 剩余风险
- 热点坐标是否需要人工微调。
- 移动端标签是否拥挤。
- 图片版权/来源说明：用户提供 docx 内图片。
```

## Self-Review

- The plan uses the docx image as a static asset and does not require web downloads.
- The plan preserves current `/map` event flow and existing location cards.
- The plan avoids database and schema changes.
- The plan includes controller, template, CSS, resource, HTTP, and browser verification.
- The plan gives exact hotspot coordinates and permits small visual QA adjustment.
