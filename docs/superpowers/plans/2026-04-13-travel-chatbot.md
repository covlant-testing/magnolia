# Travel Chatbot Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a `travel-demo-chatbot` Magnolia module that adds a Gemini-powered travel-advice chatbot to the Magnolia Travel Demo, grounded in JCR content via tool-calling, with a floating widget plus a placeable component, session-scoped memory, visitor-trait personalization, i18n, and editor-facing configuration.

**Architecture:** A single new Maven module under the `travel-demo-parent` reactor, packaged as a Magnolia module JAR. Server-side `ChatEndpoint` (REST-EASY) orchestrates per-turn flow: resolves locale + visitor traits, loads conversation history from an in-memory cache, calls Gemini's REST API with tool declarations, dispatches tool calls back to in-process tools that read JCR, and returns the final assistant message to the browser. Front-end is vanilla JS rendered by FreeMarker — one floating-widget snippet (injected via template decoration) and one placeable Magnolia component, both calling the same endpoint and sharing a session cookie.

**Tech Stack:** Java 17, Magnolia 6.3, Maven, JCR, REST-EASY, SLF4J/Logback, Caffeine (in-process cache for sessions and rate-limiting), Apache HttpClient (already on the Magnolia classpath, used to call Gemini), Jackson (already present), JUnit 4 + Mockito + WireMock for tests, vanilla JS + FreeMarker for the front-end.

**Spec:** `docs/superpowers/specs/2026-04-13-travel-chatbot-design.md`

---

## File map

Lays out every file created or modified, grouped by responsibility. Tasks below reference these paths exactly.

### New module `travel-demo-chatbot/`

**Maven & module descriptor**
- `travel-demo-chatbot/pom.xml` — module POM (mirrors `travel-demo-tours/pom.xml`)
- `travel-demo-chatbot/src/main/resources/META-INF/magnolia/travel-demo-chatbot.xml` — Magnolia module descriptor
- `travel-demo-chatbot/src/main/resources/info/magnolia/demo/travel/chatbot/Messages.properties` — backend i18n (English defaults for error messages)
- `travel-demo-chatbot/src/main/resources/info/magnolia/demo/travel/chatbot/Messages_de.properties`
- `travel-demo-chatbot/src/main/resources/info/magnolia/demo/travel/chatbot/Messages_fr.properties`

**Java — module config & lifecycle**
- `src/main/java/info/magnolia/demo/travel/chatbot/ChatbotModule.java` — config bean (model, prompt, enabled tools, limits)
- `src/main/java/info/magnolia/demo/travel/chatbot/setup/ChatbotModuleVersionHandler.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/env/EnvLoader.java` — loads `.env` into a `Map<String,String>` at startup

**Java — i18n / personalization**
- `src/main/java/info/magnolia/demo/travel/chatbot/i18n/LanguageResolver.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/personalization/VisitorTraitsResolver.java`

**Java — session & rate-limit**
- `src/main/java/info/magnolia/demo/travel/chatbot/session/ConversationHistory.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/session/Turn.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/session/ChatSessionStore.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/ratelimit/SessionRateLimiter.java`

**Java — tools**
- `src/main/java/info/magnolia/demo/travel/chatbot/tools/Tool.java` — interface
- `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToolContext.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToolException.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToolRegistry.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToursTool.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/tools/DestinationsTool.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/tools/EditorialTool.java`

**Java — LLM client**
- `src/main/java/info/magnolia/demo/travel/chatbot/llm/LlmException.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/llm/GeminiClient.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/llm/GeminiToolAdapter.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/llm/LlmRequest.java` (system, tools, history, userMessage)
- `src/main/java/info/magnolia/demo/travel/chatbot/llm/LlmResponse.java`

**Java — REST endpoint**
- `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatEndpoint.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatEndpointDefinition.java` — Magnolia REST endpoint definition
- `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatTurnRequest.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatTurnResponse.java`
- `src/main/java/info/magnolia/demo/travel/chatbot/rest/TourSummary.java`

**JCR bootstrap (YAML decorations and module config)**
- `src/main/resources/travel-demo-chatbot/restEndpoints/chatbot/chatbot_v1.yaml`
- `src/main/resources/travel-demo-chatbot/decorations/admincentral/config.yaml` — registers the Chatbot Configuration App in the Tools group
- `src/main/resources/travel-demo-chatbot/apps/chatbot-config-app.yaml` — the App descriptor
- `src/main/resources/travel-demo-chatbot/decorations/travel-demo/sites/travel-demo-site.yaml` — adds the floating widget include to the existing site template's footer area (path confirmed at Task 17)

**Bootstrap config nodes (XML, picked up by version handler)**
- `src/main/resources/mgnl-bootstrap/travel-demo-chatbot/config.modules.travel-demo-chatbot.config.xml` — default `ChatbotModule` config

**Templates & front-end**
- `src/main/resources/travel-demo-chatbot/templates/components/chatbot.yaml` — placeable component definition
- `src/main/resources/travel-demo-chatbot/templates/components/chatbot.ftl`
- `src/main/resources/travel-demo-chatbot/templates/snippets/chatbot-floating.ftl`
- `src/main/resources/travel-demo-chatbot/dialogs/chatbotComponent.yaml` — empty dialog (no per-instance settings in MVP, but dialog file required by Magnolia)
- `src/main/resources/travel-demo-chatbot/i18n/chatbot_en.properties` — UI strings
- `src/main/resources/travel-demo-chatbot/i18n/chatbot_de.properties`
- `src/main/resources/travel-demo-chatbot/i18n/chatbot_fr.properties`
- `src/main/resources/travel-demo-chatbot-theme/js/chatbot.js`
- `src/main/resources/travel-demo-chatbot-theme/css/chatbot.css`

**Tests (mirrors source tree)**
- All `*Test.java` files colocated under `src/test/java/...` matching the production class.
- One integration test: `src/test/java/info/magnolia/demo/travel/chatbot/integration/ChatbotIntegrationIT.java`

### Modified existing files

- `pom.xml` (parent reactor) — add `<module>travel-demo-chatbot</module>` and `<dependency>` entry in `<dependencyManagement>`.
- `travel-demo-webapp/pom.xml` — add `<dependency>` on `magnolia-travel-demo-chatbot`.
- `travel-demo-community-webapp/pom.xml` — same.

---

## Conventions used throughout

- Every Java class begins with the standard Magnolia copyright header, copied verbatim from `travel-demo-tours/src/main/java/info/magnolia/demo/travel/tours/ToursModule.java` (lines 1–33), with the year range updated to `2026`.
- All loggers: `private static final Logger log = LoggerFactory.getLogger(<Class>.class);`. SLF4J only; no `System.out`.
- `@Singleton` on stateful service classes (we use `javax.inject.Singleton`, matching the existing `TourServices`).
- TDD: every task writes a failing test first, runs it, implements, runs it again, then commits. Keep commits small; one commit per task unless noted.
- Tests use JUnit 4 (matches sibling modules), Mockito for mocks, WireMock for HTTP stubbing.
- Build/test command from repo root: `mvn -pl travel-demo-chatbot -am test`. Linting is enforced by the parent POM's standard checkstyle profile.
- All commit messages follow Conventional Commits (CLAUDE.md GH-1) and never reference Claude/Anthropic.

---

## Task 1: Create empty module skeleton and reactor wiring

**Files:**
- Create: `travel-demo-chatbot/pom.xml`
- Create: `travel-demo-chatbot/src/main/resources/META-INF/magnolia/travel-demo-chatbot.xml`
- Create: `travel-demo-chatbot/src/main/java/info/magnolia/demo/travel/chatbot/ChatbotModule.java`
- Create: `travel-demo-chatbot/src/main/java/info/magnolia/demo/travel/chatbot/setup/ChatbotModuleVersionHandler.java`
- Create: `travel-demo-chatbot/src/test/java/info/magnolia/demo/travel/chatbot/setup/ChatbotModuleVersionHandlerTest.java`
- Modify: `pom.xml` (parent reactor)

- [ ] **Step 1: Write the failing version-handler test**

`travel-demo-chatbot/src/test/java/info/magnolia/demo/travel/chatbot/setup/ChatbotModuleVersionHandlerTest.java`:

```java
package info.magnolia.demo.travel.chatbot.setup;

import static org.junit.Assert.assertNotNull;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ChatbotModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/travel-demo-chatbot.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new ChatbotModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml",
                "/META-INF/magnolia/travel-demo-chatbot.xml");
    }

    @Test
    public void freshInstallSucceeds() throws Exception {
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);
        assertNotNull(MgnlContext.getJCRSession("config")
                .getNode("/modules/travel-demo-chatbot/config"));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
mvn -pl travel-demo-chatbot -am -q test \
    -Dtest=ChatbotModuleVersionHandlerTest#freshInstallSucceeds
```

Expected: compilation failure (`ChatbotModuleVersionHandler` does not exist).

- [ ] **Step 3: Create `pom.xml` for the new module**

`travel-demo-chatbot/pom.xml` — copy `travel-demo-tours/pom.xml` and change `artifactId` to `magnolia-travel-demo-chatbot`, `name` to `Magnolia Travel Chatbot Module`, `description` to `Magnolia module providing a Gemini-powered travel advice chatbot for the travel demo.`. Drop the categorization, dam, and tourTours-only test deps that we don't need yet — we will add WireMock and Caffeine in later tasks.

Initial dependencies block (subset of tours/pom):

```xml
<dependencies>
  <dependency><groupId>info.magnolia</groupId><artifactId>magnolia-core</artifactId></dependency>
  <dependency><groupId>info.magnolia</groupId><artifactId>magnolia-rendering</artifactId></dependency>
  <dependency><groupId>info.magnolia</groupId><artifactId>magnolia-templating</artifactId></dependency>
  <dependency><groupId>info.magnolia.core</groupId><artifactId>magnolia-configuration</artifactId></dependency>
  <dependency><groupId>info.magnolia.demo</groupId><artifactId>magnolia-travel-demo</artifactId></dependency>
  <dependency><groupId>org.slf4j</groupId><artifactId>slf4j-api</artifactId></dependency>
  <dependency><groupId>org.apache.commons</groupId><artifactId>commons-lang3</artifactId></dependency>
  <dependency><groupId>com.google.guava</groupId><artifactId>guava</artifactId></dependency>
  <dependency><groupId>javax.jcr</groupId><artifactId>jcr</artifactId></dependency>
  <dependency><groupId>jakarta.inject</groupId><artifactId>jakarta.inject-api</artifactId></dependency>

  <!-- Tests -->
  <dependency><groupId>info.magnolia</groupId><artifactId>magnolia-core</artifactId>
              <type>test-jar</type><scope>test</scope></dependency>
  <dependency><groupId>junit</groupId><artifactId>junit</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.mockito</groupId><artifactId>mockito-core</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.hamcrest</groupId><artifactId>hamcrest</artifactId><scope>test</scope></dependency>
</dependencies>
```

- [ ] **Step 4: Create the Magnolia module descriptor**

`travel-demo-chatbot/src/main/resources/META-INF/magnolia/travel-demo-chatbot.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module SYSTEM "module.dtd" >
<module>
  <name>travel-demo-chatbot</name>
  <displayName>${project.name}</displayName>
  <description>${project.description}</description>
  <class>info.magnolia.demo.travel.chatbot.ChatbotModule</class>
  <versionHandler>info.magnolia.demo.travel.chatbot.setup.ChatbotModuleVersionHandler</versionHandler>
  <version>${project.version}</version>

  <dependencies>
    <dependency><name>core</name><version>6.3/*</version></dependency>
    <dependency><name>rest-services</name><version>3.0/*</version></dependency>
    <dependency><name>travel-demo</name><version>${project.version}</version></dependency>
    <dependency><name>ui-admincentral</name><version>6.3/*</version></dependency>
    <dependency><name>ui-framework</name><version>6.3/*</version></dependency>
    <dependency><name>site</name><version>2.0/*</version></dependency>
  </dependencies>
</module>
```

- [ ] **Step 5: Create the empty `ChatbotModule` config bean**

`travel-demo-chatbot/src/main/java/info/magnolia/demo/travel/chatbot/ChatbotModule.java` — POJO with no fields yet (fields added in Task 2):

```java
package info.magnolia.demo.travel.chatbot;

/** Configuration bean for the travel-demo-chatbot module. */
public class ChatbotModule {
}
```

(Standard Magnolia copyright header above the package line — copy from `travel-demo-tours/.../ToursModule.java`.)

- [ ] **Step 6: Create the version handler**

`travel-demo-chatbot/src/main/java/info/magnolia/demo/travel/chatbot/setup/ChatbotModuleVersionHandler.java`:

```java
package info.magnolia.demo.travel.chatbot.setup;

import info.magnolia.module.DefaultModuleVersionHandler;

/** Module-version handler for travel-demo-chatbot. */
public class ChatbotModuleVersionHandler extends DefaultModuleVersionHandler {
}
```

- [ ] **Step 7: Wire the new module into the parent reactor**

Edit the top-level `pom.xml`. Add `<module>travel-demo-chatbot</module>` to the `<modules>` list (alphabetical order) and add a managed dependency in `<dependencyManagement>`:

```xml
<dependency>
  <groupId>info.magnolia.demo</groupId>
  <artifactId>magnolia-travel-demo-chatbot</artifactId>
  <version>${project.version}</version>
</dependency>
```

- [ ] **Step 8: Run the test, expect PASS**

```bash
mvn -pl travel-demo-chatbot -am -q test \
    -Dtest=ChatbotModuleVersionHandlerTest#freshInstallSucceeds
```

Expected: PASS (the default version handler creates the `/modules/travel-demo-chatbot/config` node automatically).

- [ ] **Step 9: Commit**

```bash
git add travel-demo-chatbot pom.xml
git commit -m "feat(chatbot): scaffold travel-demo-chatbot module"
```

---

## Task 2: ChatbotModule config fields and defaults

**Files:**
- Modify: `src/main/java/info/magnolia/demo/travel/chatbot/ChatbotModule.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/ChatbotModuleTest.java`

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ChatbotModuleTest {

    @Test
    public void defaultsMatchSpec() {
        ChatbotModule cfg = new ChatbotModule();
        assertEquals("gemini-3-flash-preview", cfg.getModel());
        assertEquals(List.of("tours", "destinations", "editorial"), cfg.getEnabledTools());
        assertEquals(5, cfg.getMaxToolIterations());
        assertEquals(20, cfg.getHistoryTurnLimit());
        assertEquals(30000, cfg.getRequestTimeoutMs());
        assertEquals(30, cfg.getRateLimitPerMinute());
        assertEquals(50000, cfg.getMaxTokensPerSession());
        assertEquals(4000, cfg.getMaxUserMessageChars());
        assertTrue(cfg.getSystemPromptTemplate().contains("${language}"));
        assertTrue(cfg.getSystemPromptTemplate().contains("${visitorTraits}"));
    }

    @Test
    public void settersWork() {
        ChatbotModule cfg = new ChatbotModule();
        cfg.setModel("gemini-other");
        cfg.setMaxToolIterations(3);
        assertEquals("gemini-other", cfg.getModel());
        assertEquals(3, cfg.getMaxToolIterations());
    }
}
```

- [ ] **Step 2: Run, expect FAIL** — `mvn -pl travel-demo-chatbot test -Dtest=ChatbotModuleTest`

- [ ] **Step 3: Implement the bean**

Replace `ChatbotModule.java` with the field-bearing version. Each field has a getter and a setter so Magnolia's content2bean can populate it from `/modules/travel-demo-chatbot/config`. Default `systemPromptTemplate` is the literal text from the spec §6 — copy verbatim.

```java
package info.magnolia.demo.travel.chatbot;

import java.util.ArrayList;
import java.util.List;

public class ChatbotModule {

    private String model = "gemini-3-flash-preview";
    private String systemPromptTemplate =
            "You are a travel advisor for the Magnolia Travel demo site.\n\n"
          + "Always reply in ${language}. If the visitor writes in a different language,\n"
          + "still reply in ${language} unless they explicitly ask you to switch.\n\n"
          + "You only answer questions about travel, tours, and destinations available\n"
          + "on this site. If asked about anything else, politely redirect.\n\n"
          + "Use the available tools to ground your recommendations in real content.\n"
          + "Prefer one or two strong suggestions over long lists. When you reference\n"
          + "a tour or destination, include its title.\n\n"
          + "Visitor profile (for tailoring; do not mention these traits explicitly\n"
          + "unless asked): ${visitorTraits}\n";
    private List<String> enabledTools = new ArrayList<>(List.of("tours", "destinations", "editorial"));
    private int maxToolIterations = 5;
    private int historyTurnLimit = 20;
    private int requestTimeoutMs = 30000;
    private int rateLimitPerMinute = 30;
    private int maxTokensPerSession = 50000;
    private int maxUserMessageChars = 4000;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSystemPromptTemplate() { return systemPromptTemplate; }
    public void setSystemPromptTemplate(String t) { this.systemPromptTemplate = t; }
    public List<String> getEnabledTools() { return enabledTools; }
    public void setEnabledTools(List<String> t) { this.enabledTools = t; }
    public int getMaxToolIterations() { return maxToolIterations; }
    public void setMaxToolIterations(int n) { this.maxToolIterations = n; }
    public int getHistoryTurnLimit() { return historyTurnLimit; }
    public void setHistoryTurnLimit(int n) { this.historyTurnLimit = n; }
    public int getRequestTimeoutMs() { return requestTimeoutMs; }
    public void setRequestTimeoutMs(int n) { this.requestTimeoutMs = n; }
    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int n) { this.rateLimitPerMinute = n; }
    public int getMaxTokensPerSession() { return maxTokensPerSession; }
    public void setMaxTokensPerSession(int n) { this.maxTokensPerSession = n; }
    public int getMaxUserMessageChars() { return maxUserMessageChars; }
    public void setMaxUserMessageChars(int n) { this.maxUserMessageChars = n; }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add ChatbotModule config bean with defaults"
```

---

## Task 3: EnvLoader for the Gemini API key

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/env/EnvLoader.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/env/EnvLoaderTest.java`

`EnvLoader` reads a `.env` file (in the working directory) and exposes a `Map<String,String>` merged with `System.getenv()`. System env wins. Lines starting with `#` and blank lines are skipped. `KEY=VALUE` is parsed; surrounding double quotes on `VALUE` are stripped. Missing file is silently ignored.

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EnvLoaderTest {

    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void parsesSimpleAssignmentsIgnoringCommentsAndBlanks() throws Exception {
        File env = tmp.newFile(".env");
        Files.writeString(env.toPath(),
                "# comment\n\nGEMINI_API_KEY=abc123\nFOO=\"hello world\"\n");

        Map<String, String> result = EnvLoader.loadFile(env);

        assertEquals("abc123", result.get("GEMINI_API_KEY"));
        assertEquals("hello world", result.get("FOO"));
    }

    @Test
    public void missingFileReturnsEmptyMap() {
        File env = new File(tmp.getRoot(), "does-not-exist");
        assertEquals(0, EnvLoader.loadFile(env).size());
    }

    @Test
    public void mergedSystemEnvTakesPrecedence() throws Exception {
        File env = tmp.newFile(".env");
        Files.writeString(env.toPath(), "PATH=should-be-overridden\n");

        Map<String, String> merged = EnvLoader.merge(EnvLoader.loadFile(env), System.getenv());
        // PATH is virtually always set in the test environment; assert the system value wins.
        assertEquals(System.getenv("PATH"), merged.get("PATH"));
    }

    @Test
    public void missingKeyReturnsNull() throws Exception {
        File env = tmp.newFile(".env");
        Files.writeString(env.toPath(), "");
        assertNull(EnvLoader.loadFile(env).get("ABSENT"));
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement**

```java
package info.magnolia.demo.travel.chatbot.env;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EnvLoader {
    private EnvLoader() {}

    public static Map<String, String> loadFile(File file) {
        Map<String, String> out = new LinkedHashMap<>();
        if (file == null || !file.isFile()) return out;
        try {
            for (String raw : Files.readAllLines(file.toPath())) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                if (val.length() >= 2 && val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                out.put(key, val);
            }
        } catch (IOException e) {
            // intentionally silent — .env is best-effort
        }
        return out;
    }

    public static Map<String, String> merge(Map<String, String> dotenv, Map<String, String> systemEnv) {
        Map<String, String> out = new HashMap<>(dotenv);
        out.putAll(systemEnv);
        return out;
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add EnvLoader for .env files"
```

---

## Task 4: LanguageResolver

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/i18n/LanguageResolver.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/i18n/LanguageResolverTest.java`

`LanguageResolver` is a stateless `@Singleton` with a single method
`String resolve(jakarta.servlet.http.HttpServletRequest req)`.
Order: `AggregationState.getLocale()` → parse `Accept-Language` → `"en"`. Returns the language tag's primary subtag (`de-AT` → `de`).

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.i18n;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;
import java.util.function.Supplier;

import org.junit.Test;

public class LanguageResolverTest {

    @Test
    public void usesAggregationStateLocaleWhenPresent() {
        Supplier<Locale> aggLocale = () -> Locale.GERMAN;
        LanguageResolver r = new LanguageResolver(aggLocale);
        HttpServletRequest req = mock(HttpServletRequest.class);
        assertEquals("de", r.resolve(req));
    }

    @Test
    public void stripsRegionFromAggregationLocale() {
        LanguageResolver r = new LanguageResolver(() -> new Locale("de", "AT"));
        assertEquals("de", r.resolve(mock(HttpServletRequest.class)));
    }

    @Test
    public void fallsBackToAcceptLanguage() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Accept-Language")).thenReturn("fr-CA,fr;q=0.9");
        LanguageResolver r = new LanguageResolver(() -> null);
        assertEquals("fr", r.resolve(req));
    }

    @Test
    public void fallsBackToEnglish() {
        LanguageResolver r = new LanguageResolver(() -> null);
        assertEquals("en", r.resolve(mock(HttpServletRequest.class)));
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement**

```java
package info.magnolia.demo.travel.chatbot.i18n;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LanguageResolver {

    private final Supplier<Locale> aggregationLocale;

    @Inject
    public LanguageResolver() {
        this(() -> {
            try {
                AggregationState s = MgnlContext.getAggregationState();
                return s != null ? s.getLocale() : null;
            } catch (IllegalStateException e) {
                return null;
            }
        });
    }

    LanguageResolver(Supplier<Locale> aggregationLocale) {
        this.aggregationLocale = aggregationLocale;
    }

    public String resolve(HttpServletRequest request) {
        Locale agg = aggregationLocale.get();
        if (agg != null && agg.getLanguage() != null && !agg.getLanguage().isEmpty()) {
            return agg.getLanguage();
        }
        String header = request != null ? request.getHeader("Accept-Language") : null;
        if (header != null && !header.isBlank()) {
            String first = header.split(",")[0].trim().split(";")[0].trim();
            if (!first.isEmpty()) {
                return first.split("-")[0].toLowerCase(Locale.ROOT);
            }
        }
        return "en";
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add LanguageResolver"
```

---

## Task 5: VisitorTraitsResolver

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/personalization/VisitorTraitsResolver.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/personalization/VisitorTraitsResolverTest.java`

The MTE personalization module ships a `VisitorContext` we can read via component-provider lookup. To avoid a hard compile-time dependency on the optional personalization module, do the lookup reflectively. If the class isn't on the classpath or the lookup throws, return an empty map.

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.personalization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class VisitorTraitsResolverTest {

    @Test
    public void returnsEmptyMapWhenNoVisitorContextLookup() {
        VisitorTraitsResolver r = new VisitorTraitsResolver(() -> null);
        assertTrue(r.resolve().isEmpty());
    }

    @Test
    public void returnsTraitsWhenSupplierProvidesThem() {
        VisitorTraitsResolver r = new VisitorTraitsResolver(() ->
                Map.of("segment", "Adventure Seeker", "country", "DE"));
        Map<String, String> traits = r.resolve();
        assertEquals("Adventure Seeker", traits.get("segment"));
        assertEquals("DE", traits.get("country"));
    }

    @Test
    public void supplierExceptionsBecomeEmptyMap() {
        VisitorTraitsResolver r = new VisitorTraitsResolver(() -> {
            throw new IllegalStateException("personalization not installed");
        });
        assertTrue(r.resolve().isEmpty());
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement**

```java
package info.magnolia.demo.travel.chatbot.personalization;

import info.magnolia.objectfactory.Components;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VisitorTraitsResolver {

    private static final Logger log = LoggerFactory.getLogger(VisitorTraitsResolver.class);

    private final Supplier<Map<String, String>> traitsSupplier;

    @Inject
    public VisitorTraitsResolver() {
        this(VisitorTraitsResolver::lookupReflectively);
    }

    VisitorTraitsResolver(Supplier<Map<String, String>> traitsSupplier) {
        this.traitsSupplier = traitsSupplier;
    }

    public Map<String, String> resolve() {
        try {
            Map<String, String> v = traitsSupplier.get();
            return v == null ? Collections.emptyMap() : v;
        } catch (Exception e) {
            log.debug("VisitorContext not available, returning empty traits ({})", e.getClass().getSimpleName());
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> lookupReflectively() {
        try {
            Class<?> ctxClass = Class.forName("info.magnolia.personalization.visitor.VisitorContext");
            Object ctx = Components.getComponent(ctxClass);
            Map<String, String> out = new java.util.HashMap<>();
            // Best-effort: pull common trait getters if they exist; ignore the rest.
            for (String getter : new String[] { "getSegment", "getCountry", "getLanguage" }) {
                try {
                    Object value = ctxClass.getMethod(getter).invoke(ctx);
                    if (value != null) {
                        String key = Character.toLowerCase(getter.charAt(3)) + getter.substring(4);
                        out.put(key, value.toString());
                    }
                } catch (NoSuchMethodException ignored) {}
            }
            return out;
        } catch (ClassNotFoundException e) {
            return Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add VisitorTraitsResolver with optional personalization lookup"
```

---

## Task 6: Turn and ConversationHistory value types

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/session/Turn.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/session/ConversationHistory.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/session/ConversationHistoryTest.java`

`Turn` is an immutable record-like class capturing one role/content pair plus optional tool metadata. `ConversationHistory` is a small wrapper holding an ordered list of turns with `append`, `turns()`, `trimTo(int n)`, and `estimatedTokens()` helpers.

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.session;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ConversationHistoryTest {

    @Test
    public void appendAndRetrieveInOrder() {
        ConversationHistory h = new ConversationHistory();
        h.append(Turn.user("hi"));
        h.append(Turn.assistant("hello!"));
        assertEquals(List.of("hi", "hello!"),
                h.turns().stream().map(Turn::content).toList());
    }

    @Test
    public void trimToKeepsMostRecent() {
        ConversationHistory h = new ConversationHistory();
        for (int i = 0; i < 10; i++) h.append(Turn.user("u" + i));
        h.trimTo(3);
        assertEquals(List.of("u7", "u8", "u9"),
                h.turns().stream().map(Turn::content).toList());
    }

    @Test
    public void trimToZeroEmpties() {
        ConversationHistory h = new ConversationHistory();
        h.append(Turn.user("x"));
        h.trimTo(0);
        assertEquals(0, h.turns().size());
    }

    @Test
    public void estimatedTokensApproximatesByCharsOver4() {
        ConversationHistory h = new ConversationHistory();
        h.append(Turn.user("abcd"));   // 4 chars -> 1 token
        h.append(Turn.user("abcdefgh")); // 8 chars -> 2 tokens
        assertEquals(3, h.estimatedTokens());
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement `Turn`**

```java
package info.magnolia.demo.travel.chatbot.session;

public final class Turn {
    public enum Role { USER, ASSISTANT, TOOL }
    private final Role role;
    private final String content;
    private final String toolName; // nullable
    private Turn(Role r, String c, String toolName) { this.role = r; this.content = c; this.toolName = toolName; }
    public static Turn user(String c) { return new Turn(Role.USER, c, null); }
    public static Turn assistant(String c) { return new Turn(Role.ASSISTANT, c, null); }
    public static Turn tool(String toolName, String result) { return new Turn(Role.TOOL, result, toolName); }
    public Role role() { return role; }
    public String content() { return content; }
    public String toolName() { return toolName; }
}
```

- [ ] **Step 4: Implement `ConversationHistory`**

```java
package info.magnolia.demo.travel.chatbot.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationHistory {
    private final List<Turn> turns = new ArrayList<>();

    public void append(Turn t) { turns.add(t); }

    public List<Turn> turns() { return Collections.unmodifiableList(turns); }

    public void trimTo(int n) {
        if (n < 0) n = 0;
        while (turns.size() > n) turns.remove(0);
    }

    public int estimatedTokens() {
        int chars = 0;
        for (Turn t : turns) chars += t.content() == null ? 0 : t.content().length();
        return chars / 4;
    }
}
```

- [ ] **Step 5: Run, expect PASS**

- [ ] **Step 6: Commit**

```bash
git commit -am "feat(chatbot): add Turn and ConversationHistory value types"
```

---

## Task 7: ChatSessionStore (Caffeine-backed, TTL'd, injectable Ticker)

**Files:**
- Modify: `travel-demo-chatbot/pom.xml` — add `com.github.ben-manes.caffeine:caffeine` (compile scope)
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/session/ChatSessionStore.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/session/ChatSessionStoreTest.java`

- [ ] **Step 1: Add Caffeine to pom**

In `travel-demo-chatbot/pom.xml`'s `<dependencies>`:

```xml
<dependency>
  <groupId>com.github.ben-manes.caffeine</groupId>
  <artifactId>caffeine</artifactId>
  <version>3.1.8</version>
</dependency>
```

(Confirm this version works with Java 17 / Magnolia 6.3 by running `mvn -pl travel-demo-chatbot dependency:tree | grep caffeine` after the test passes; bump if conflicts arise.)

- [ ] **Step 2: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import com.github.benmanes.caffeine.cache.Ticker;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class ChatSessionStoreTest {

    @Test
    public void newSessionReturnsEmptyHistory() {
        ChatSessionStore store = new ChatSessionStore(Duration.ofMinutes(30), 100, Ticker.systemTicker());
        ConversationHistory h = store.getOrCreate("sid-1");
        assertEquals(0, h.turns().size());
    }

    @Test
    public void appendsArePersistedAcrossLookups() {
        ChatSessionStore store = new ChatSessionStore(Duration.ofMinutes(30), 100, Ticker.systemTicker());
        store.getOrCreate("sid-1").append(Turn.user("hello"));
        assertEquals("hello", store.getOrCreate("sid-1").turns().get(0).content());
    }

    @Test
    public void evictsAfterIdleTtl() {
        AtomicLong now = new AtomicLong(0);
        ChatSessionStore store = new ChatSessionStore(Duration.ofMinutes(30), 100, now::get);
        ConversationHistory h1 = store.getOrCreate("sid-1");
        h1.append(Turn.user("x"));
        // advance 31 minutes
        now.addAndGet(Duration.ofMinutes(31).toNanos());
        ConversationHistory h2 = store.getOrCreate("sid-1");
        assertNotSame(h1, h2);
        assertEquals(0, h2.turns().size());
    }
}
```

- [ ] **Step 3: Run, expect FAIL**

- [ ] **Step 4: Implement**

```java
package info.magnolia.demo.travel.chatbot.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatSessionStore {

    private final Cache<String, ConversationHistory> cache;

    @Inject
    public ChatSessionStore() {
        this(Duration.ofMinutes(30), 10_000, Ticker.systemTicker());
    }

    public ChatSessionStore(Duration idleTtl, long maxEntries, Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .ticker(ticker)
                .expireAfterAccess(idleTtl)
                .maximumSize(maxEntries)
                .build();
    }

    public ConversationHistory getOrCreate(String sessionId) {
        return cache.get(sessionId, k -> new ConversationHistory());
    }
}
```

- [ ] **Step 5: Run, expect PASS**

- [ ] **Step 6: Commit**

```bash
git commit -am "feat(chatbot): add Caffeine-backed ChatSessionStore"
```

---

## Task 8: SessionRateLimiter (sliding window)

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/ratelimit/SessionRateLimiter.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/ratelimit/SessionRateLimiterTest.java`

A sliding-window limiter with one bucket per session id. Each `tryAcquire(sid)` returns `true` if the call is allowed and records the timestamp, or `false` if `>= permitsPerMinute` calls in the trailing 60 s. Uses an injected `LongSupplier` for current time (nanos) so tests can advance the clock.

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.ratelimit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class SessionRateLimiterTest {

    @Test
    public void allowsUpToLimit() {
        SessionRateLimiter rl = new SessionRateLimiter(3, () -> 0L);
        assertTrue(rl.tryAcquire("s"));
        assertTrue(rl.tryAcquire("s"));
        assertTrue(rl.tryAcquire("s"));
        assertFalse(rl.tryAcquire("s"));
    }

    @Test
    public void perSessionIsolated() {
        SessionRateLimiter rl = new SessionRateLimiter(1, () -> 0L);
        assertTrue(rl.tryAcquire("a"));
        assertTrue(rl.tryAcquire("b"));
        assertFalse(rl.tryAcquire("a"));
    }

    @Test
    public void windowSlides() {
        AtomicLong now = new AtomicLong(0);
        SessionRateLimiter rl = new SessionRateLimiter(2, now::get);
        assertTrue(rl.tryAcquire("s"));
        assertTrue(rl.tryAcquire("s"));
        assertFalse(rl.tryAcquire("s"));
        now.addAndGet(Duration.ofSeconds(61).toNanos());
        assertTrue(rl.tryAcquire("s"));
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement**

```java
package info.magnolia.demo.travel.chatbot.ratelimit;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionRateLimiter {

    private static final long WINDOW_NANOS = Duration.ofSeconds(60).toNanos();

    private final int permitsPerMinute;
    private final LongSupplier clockNanos;
    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    @Inject
    public SessionRateLimiter() {
        this(30, System::nanoTime);
    }

    public SessionRateLimiter(int permitsPerMinute, LongSupplier clockNanos) {
        this.permitsPerMinute = permitsPerMinute;
        this.clockNanos = clockNanos;
    }

    public boolean tryAcquire(String sessionId) {
        long now = clockNanos.getAsLong();
        long cutoff = now - WINDOW_NANOS;
        Deque<Long> q = windows.computeIfAbsent(sessionId, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
            if (q.size() >= permitsPerMinute) return false;
            q.addLast(now);
            return true;
        }
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add SessionRateLimiter"
```

---

## Task 9: Tool interface, ToolContext, ToolException, ToolRegistry

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/tools/Tool.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToolContext.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToolException.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToolRegistry.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/tools/ToolRegistryTest.java`

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.List;

import org.junit.Test;

public class ToolRegistryTest {

    private static Tool toolNamed(String name) {
        return new Tool() {
            @Override public String name() { return name; }
            @Override public String description() { return "desc"; }
            @Override public JsonNode parametersSchema() { return JsonNodeFactory.instance.objectNode(); }
            @Override public JsonNode invoke(JsonNode args, ToolContext ctx) { return JsonNodeFactory.instance.objectNode(); }
        };
    }

    @Test
    public void enabledNamesFilterRegistry() {
        ToolRegistry reg = new ToolRegistry(List.of(toolNamed("a"), toolNamed("b"), toolNamed("c")));
        List<Tool> enabled = reg.enabled(List.of("a", "c"));
        assertEquals(List.of("a", "c"), enabled.stream().map(Tool::name).toList());
    }

    @Test
    public void invokeUnknownThrows() {
        ToolRegistry reg = new ToolRegistry(List.of(toolNamed("a")));
        assertThrows(ToolException.class,
                () -> reg.invoke("missing", JsonNodeFactory.instance.objectNode(), new ToolContext("en")));
    }

    @Test
    public void invokeKnownReturnsResult() throws Exception {
        ToolRegistry reg = new ToolRegistry(List.of(toolNamed("a")));
        JsonNode out = reg.invoke("a", JsonNodeFactory.instance.objectNode(), new ToolContext("en"));
        assertTrue(out.isObject());
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement the four classes**

```java
// Tool.java
package info.magnolia.demo.travel.chatbot.tools;

import com.fasterxml.jackson.databind.JsonNode;

public interface Tool {
    String name();
    String description();
    JsonNode parametersSchema();
    JsonNode invoke(JsonNode args, ToolContext ctx) throws ToolException;
}
```

```java
// ToolContext.java
package info.magnolia.demo.travel.chatbot.tools;

public final class ToolContext {
    private final String language;
    public ToolContext(String language) { this.language = language; }
    public String language() { return language; }
}
```

```java
// ToolException.java
package info.magnolia.demo.travel.chatbot.tools;

public class ToolException extends Exception {
    public ToolException(String msg) { super(msg); }
    public ToolException(String msg, Throwable cause) { super(msg, cause); }
}
```

```java
// ToolRegistry.java
package info.magnolia.demo.travel.chatbot.tools;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ToolRegistry {

    private final Map<String, Tool> byName = new LinkedHashMap<>();

    @Inject
    public ToolRegistry(List<Tool> tools) {
        for (Tool t : tools) byName.put(t.name(), t);
    }

    public List<Tool> enabled(List<String> enabledNames) {
        return enabledNames.stream().map(byName::get).filter(t -> t != null).collect(Collectors.toList());
    }

    public JsonNode invoke(String name, JsonNode args, ToolContext ctx) throws ToolException {
        Tool t = byName.get(name);
        if (t == null) throw new ToolException("unknown tool: " + name);
        return t.invoke(args, ctx);
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add Tool interface and ToolRegistry"
```

---

## Task 10: ToursTool

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/tools/ToursTool.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/tools/ToursToolTest.java`

Operations: `searchTours({region?, maxPriceUsd?, durationDaysMax?, theme?})` and `getTour({id})`.
Implementation reads JCR `tours` workspace via `MgnlContext.getJCRSession("tours")` and a JCR-SQL2 query, returning trimmed JSON. Cap at 10 results. Validate numeric arguments are non-negative; out-of-range values throw `ToolException`.

The tool exposes its name, description, JSON-schema (a hand-written `ObjectNode`), and an `invoke` that branches on `args.path("operation").asText()` — both operations are exposed under one tool to keep the LLM's tool list small.

- [ ] **Step 1: Write the failing test**

The test uses Magnolia's `RepositoryTestCase` (provides an in-memory JCR session). Seed two tour nodes, then exercise the tool.

```java
package info.magnolia.demo.travel.chatbot.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Test;

public class ToursToolTest extends RepositoryTestCase {

    @Override protected String getRepositoryConfigFileName() {
        // use the repository config that includes a "tours" workspace; sibling modules use the
        // default repositories.xml — adjust per existing test fixtures.
        return super.getRepositoryConfigFileName();
    }

    private void seedTour(String name, String region, long price, long days) throws Exception {
        Session s = MgnlContext.getJCRSession("tours");
        Node n = s.getRootNode().addNode(name, "mgnl:content");
        n.setProperty("name", name);
        n.setProperty("region", region);
        n.setProperty("priceUsd", price);
        n.setProperty("durationDays", days);
        s.save();
    }

    @Test
    public void searchToursFiltersByRegionAndPrice() throws Exception {
        seedTour("bali-7day", "Asia", 1500L, 7L);
        seedTour("alps-trek", "Europe", 2500L, 5L);
        ToursTool tool = new ToursTool();

        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchTours").put("region", "Asia").put("maxPriceUsd", 2000);
        JsonNode out = tool.invoke(args, new ToolContext("en"));

        assertEquals(1, out.get("results").size());
        assertEquals("bali-7day", out.get("results").get(0).get("id").asText());
    }

    @Test
    public void searchToursCapsAtTen() throws Exception {
        for (int i = 0; i < 15; i++) seedTour("t" + i, "Asia", 100, 1);
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode().put("operation", "searchTours");
        assertEquals(10, tool.invoke(args, new ToolContext("en")).get("results").size());
    }

    @Test
    public void getTourReturnsDetails() throws Exception {
        seedTour("bali-7day", "Asia", 1500L, 7L);
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "getTour").put("id", "bali-7day");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals("bali-7day", out.get("id").asText());
        assertEquals("Asia", out.get("region").asText());
    }

    @Test
    public void unknownOperationThrows() {
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode().put("operation", "nope");
        assertThrows(ToolException.class, () -> tool.invoke(args, new ToolContext("en")));
    }

    @Test
    public void schemaIsObject() {
        assertTrue(new ToursTool().parametersSchema().isObject());
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement**

```java
package info.magnolia.demo.travel.chatbot.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;

@Singleton
public class ToursTool implements Tool {

    private static final int MAX_RESULTS = 10;
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    @Inject
    public ToursTool() {}

    @Override public String name() { return "tours"; }

    @Override public String description() {
        return "Search and fetch tours from the travel-demo Magnolia content. "
             + "Use operation=searchTours with optional filters {region, maxPriceUsd, durationDaysMax, theme}, "
             + "or operation=getTour with {id}.";
    }

    @Override public JsonNode parametersSchema() {
        ObjectNode schema = JSON.objectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("operation").put("type", "string").putArray("enum").add("searchTours").add("getTour");
        props.putObject("region").put("type", "string");
        props.putObject("maxPriceUsd").put("type", "number");
        props.putObject("durationDaysMax").put("type", "number");
        props.putObject("theme").put("type", "string");
        props.putObject("id").put("type", "string");
        schema.putArray("required").add("operation");
        return schema;
    }

    @Override public JsonNode invoke(JsonNode args, ToolContext ctx) throws ToolException {
        String op = args.path("operation").asText("");
        try {
            Session session = MgnlContext.getJCRSession("tours");
            switch (op) {
                case "searchTours": return searchTours(session, args);
                case "getTour":     return getTour(session, args);
                default:            throw new ToolException("unknown operation: " + op);
            }
        } catch (ToolException te) {
            throw te;
        } catch (Exception e) {
            throw new ToolException("tours tool failed: " + e.getMessage(), e);
        }
    }

    private JsonNode searchTours(Session session, JsonNode args) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT * FROM [mgnl:content] AS t");
        StringBuilder where = new StringBuilder();
        if (args.hasNonNull("region"))
            where.append(" AND LOWER(t.region) = '").append(args.get("region").asText().toLowerCase().replace("'", "''")).append("'");
        if (args.hasNonNull("maxPriceUsd")) {
            double v = args.get("maxPriceUsd").asDouble();
            if (v < 0) throw new ToolException("maxPriceUsd must be non-negative");
            where.append(" AND t.priceUsd <= ").append(v);
        }
        if (args.hasNonNull("durationDaysMax")) {
            double v = args.get("durationDaysMax").asDouble();
            if (v < 0) throw new ToolException("durationDaysMax must be non-negative");
            where.append(" AND t.durationDays <= ").append(v);
        }
        if (where.length() > 0) sql.append(" WHERE ").append(where.substring(5));

        Query q = session.getWorkspace().getQueryManager().createQuery(sql.toString(), Query.JCR_SQL2);
        q.setLimit(MAX_RESULTS);
        ArrayNode results = JSON.arrayNode();
        NodeIterator it = q.execute().getNodes();
        while (it.hasNext() && results.size() < MAX_RESULTS) results.add(summarize(it.nextNode()));

        ObjectNode out = JSON.objectNode();
        out.set("results", results);
        return out;
    }

    private JsonNode getTour(Session session, JsonNode args) throws Exception {
        if (!args.hasNonNull("id")) throw new ToolException("id is required");
        String id = args.get("id").asText();
        if (!session.getRootNode().hasNode(id))
            throw new ToolException("tour not found: " + id);
        return summarize(session.getRootNode().getNode(id));
    }

    private ObjectNode summarize(Node n) throws Exception {
        ObjectNode o = JSON.objectNode();
        o.put("id", n.getName());
        if (n.hasProperty("name"))         o.put("title", n.getProperty("name").getString());
        if (n.hasProperty("region"))       o.put("region", n.getProperty("region").getString());
        if (n.hasProperty("priceUsd"))     o.put("priceUsd", n.getProperty("priceUsd").getLong());
        if (n.hasProperty("durationDays")) o.put("durationDays", n.getProperty("durationDays").getLong());
        if (n.hasProperty("description"))  o.put("summary", truncate(n.getProperty("description").getString()));
        o.put("url", "/travel/tour/" + n.getName());
        return o;
    }

    private static String truncate(String s) {
        return s == null ? "" : (s.length() > 240 ? s.substring(0, 240) + "…" : s);
    }
}
```

- [ ] **Step 4: Run, expect PASS** — `mvn -pl travel-demo-chatbot test -Dtest=ToursToolTest`

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add ToursTool with search and get operations"
```

---

## Task 11: DestinationsTool

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/tools/DestinationsTool.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/tools/DestinationsToolTest.java`

Same shape as `ToursTool` but reads from the `website` workspace under `/travel/destinations`. Operations: `searchDestinations({query?, region?, climate?})` and `getDestination({id})`. Cap 10. URL is `/travel/destinations/<name>`.

- [ ] **Step 1: Write the failing test**

Mirror `ToursToolTest`: seed two destination nodes under `/travel/destinations` in the `website` workspace; assert `query` substring matches title or description (case-insensitive); `getDestination` retrieves details; unknown operation throws.

```java
package info.magnolia.demo.travel.chatbot.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Test;

public class DestinationsToolTest extends RepositoryTestCase {

    private void seedDest(String name, String title, String region, String climate) throws Exception {
        Session s = MgnlContext.getJCRSession("website");
        Node travel = s.getRootNode().hasNode("travel") ? s.getRootNode().getNode("travel") : s.getRootNode().addNode("travel", "mgnl:page");
        Node dests = travel.hasNode("destinations") ? travel.getNode("destinations") : travel.addNode("destinations", "mgnl:page");
        Node n = dests.addNode(name, "mgnl:page");
        n.setProperty("title", title);
        n.setProperty("region", region);
        n.setProperty("climate", climate);
        s.save();
    }

    @Test
    public void searchByRegionAndClimate() throws Exception {
        seedDest("bali", "Bali", "Asia", "tropical");
        seedDest("zurich", "Zurich", "Europe", "alpine");
        DestinationsTool tool = new DestinationsTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchDestinations").put("climate", "tropical");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals(1, out.get("results").size());
        assertEquals("bali", out.get("results").get(0).get("id").asText());
    }

    @Test
    public void getDestinationReturnsDetails() throws Exception {
        seedDest("bali", "Bali", "Asia", "tropical");
        DestinationsTool tool = new DestinationsTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "getDestination").put("id", "bali");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals("Bali", out.get("title").asText());
    }

    @Test
    public void schemaIsObject() {
        assertTrue(new DestinationsTool().parametersSchema().isObject());
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement**

`DestinationsTool.java` mirrors `ToursTool.java` with workspace `"website"`, root path `/travel/destinations`, the JSON-schema accepts `{operation, query, region, climate, id}`, and the JCR-SQL2 query is rooted with `ISDESCENDANTNODE([/travel/destinations])` plus optional case-insensitive substring match on `title`/`description` for the `query` filter:

```java
package info.magnolia.demo.travel.chatbot.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;

@Singleton
public class DestinationsTool implements Tool {

    private static final int MAX_RESULTS = 10;
    private static final String ROOT = "/travel/destinations";
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    @Inject public DestinationsTool() {}

    @Override public String name() { return "destinations"; }

    @Override public String description() {
        return "Search and fetch travel destinations. operation=searchDestinations with "
             + "{query, region, climate} or operation=getDestination with {id}.";
    }

    @Override public JsonNode parametersSchema() {
        ObjectNode s = JSON.objectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("operation").put("type", "string").putArray("enum").add("searchDestinations").add("getDestination");
        p.putObject("query").put("type", "string");
        p.putObject("region").put("type", "string");
        p.putObject("climate").put("type", "string");
        p.putObject("id").put("type", "string");
        s.putArray("required").add("operation");
        return s;
    }

    @Override public JsonNode invoke(JsonNode args, ToolContext ctx) throws ToolException {
        String op = args.path("operation").asText("");
        try {
            Session session = MgnlContext.getJCRSession("website");
            switch (op) {
                case "searchDestinations": return search(session, args);
                case "getDestination":     return get(session, args);
                default:                   throw new ToolException("unknown operation: " + op);
            }
        } catch (ToolException te) {
            throw te;
        } catch (Exception e) {
            throw new ToolException("destinations tool failed: " + e.getMessage(), e);
        }
    }

    private JsonNode search(Session session, JsonNode args) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT * FROM [mgnl:page] AS d WHERE ISDESCENDANTNODE([")
                .append(ROOT).append("])");
        if (args.hasNonNull("region"))
            sql.append(" AND LOWER(d.region) = '").append(args.get("region").asText().toLowerCase().replace("'", "''")).append("'");
        if (args.hasNonNull("climate"))
            sql.append(" AND LOWER(d.climate) = '").append(args.get("climate").asText().toLowerCase().replace("'", "''")).append("'");
        if (args.hasNonNull("query")) {
            String q = args.get("query").asText().toLowerCase().replace("'", "''");
            sql.append(" AND (LOWER(d.title) LIKE '%").append(q).append("%' OR LOWER(d.description) LIKE '%").append(q).append("%')");
        }
        Query q = session.getWorkspace().getQueryManager().createQuery(sql.toString(), Query.JCR_SQL2);
        q.setLimit(MAX_RESULTS);
        ArrayNode results = JSON.arrayNode();
        NodeIterator it = q.execute().getNodes();
        while (it.hasNext() && results.size() < MAX_RESULTS) results.add(summarize(it.nextNode()));
        ObjectNode out = JSON.objectNode();
        out.set("results", results);
        return out;
    }

    private JsonNode get(Session session, JsonNode args) throws Exception {
        if (!args.hasNonNull("id")) throw new ToolException("id is required");
        String id = args.get("id").asText();
        String path = ROOT + "/" + id;
        if (!session.nodeExists(path)) throw new ToolException("destination not found: " + id);
        return summarize(session.getNode(path));
    }

    private ObjectNode summarize(Node n) throws Exception {
        ObjectNode o = JSON.objectNode();
        o.put("id", n.getName());
        if (n.hasProperty("title"))       o.put("title", n.getProperty("title").getString());
        if (n.hasProperty("region"))      o.put("region", n.getProperty("region").getString());
        if (n.hasProperty("climate"))     o.put("climate", n.getProperty("climate").getString());
        if (n.hasProperty("description")) o.put("summary", truncate(n.getProperty("description").getString()));
        o.put("url", n.getPath());
        return o;
    }

    private static String truncate(String s) {
        return s == null ? "" : (s.length() > 240 ? s.substring(0, 240) + "…" : s);
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add DestinationsTool"
```

---

## Task 12: EditorialTool

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/tools/EditorialTool.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/tools/EditorialToolTest.java`

Operation: `searchEditorial({query, tags?})`. Searches `mgnl:page` nodes under the `website` workspace at any path **outside** `/travel/destinations` (so we don't double-count destination pages). Filters by case-insensitive substring on `title`/`abstract`/`text` and, optionally, intersection with the `tags` multi-property.

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.tools;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Test;

public class EditorialToolTest extends RepositoryTestCase {

    private void seed(String parent, String name, String title, String text, String... tags) throws Exception {
        Session s = MgnlContext.getJCRSession("website");
        Node p = s.getRootNode().hasNode(parent) ? s.getRootNode().getNode(parent) : s.getRootNode().addNode(parent, "mgnl:page");
        Node n = p.addNode(name, "mgnl:page");
        n.setProperty("title", title);
        n.setProperty("text", text);
        if (tags.length > 0) n.setProperty("tags", tags);
        s.save();
    }

    @Test
    public void searchByQuerySubstring() throws Exception {
        seed("about", "sustainability", "Sustainable Travel", "We minimize impact in southeast asia.");
        seed("about", "team", "Our Team", "Meet the team.");
        EditorialTool tool = new EditorialTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchEditorial").put("query", "sustainable");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals(1, out.get("results").size());
        assertEquals("sustainability", out.get("results").get(0).get("id").asText());
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement** — same skeleton as `DestinationsTool` but query targets `mgnl:page` nodes under `/`, excludes those under `/travel/destinations`, and supports the `tags` filter:

```java
package info.magnolia.demo.travel.chatbot.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;

@Singleton
public class EditorialTool implements Tool {

    private static final int MAX_RESULTS = 10;
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    @Inject public EditorialTool() {}

    @Override public String name() { return "editorial"; }

    @Override public String description() {
        return "Search editorial pages on the site (excluding destination pages). "
             + "operation=searchEditorial with {query, tags?}.";
    }

    @Override public JsonNode parametersSchema() {
        ObjectNode s = JSON.objectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("operation").put("type", "string").putArray("enum").add("searchEditorial");
        p.putObject("query").put("type", "string");
        ObjectNode tags = p.putObject("tags");
        tags.put("type", "array");
        tags.putObject("items").put("type", "string");
        p.putObject("id").put("type", "string");
        s.putArray("required").add("operation").add("query");
        return s;
    }

    @Override public JsonNode invoke(JsonNode args, ToolContext ctx) throws ToolException {
        String op = args.path("operation").asText("");
        if (!"searchEditorial".equals(op)) throw new ToolException("unknown operation: " + op);
        if (!args.hasNonNull("query")) throw new ToolException("query is required");
        try {
            Session session = MgnlContext.getJCRSession("website");
            String q = args.get("query").asText().toLowerCase().replace("'", "''");
            String sql = "SELECT * FROM [mgnl:page] AS p WHERE NOT ISDESCENDANTNODE(p, [/travel/destinations]) "
                       + "AND (LOWER(p.title) LIKE '%" + q + "%' OR LOWER(p.text) LIKE '%" + q + "%' OR LOWER(p.abstract) LIKE '%" + q + "%')";
            Query query = session.getWorkspace().getQueryManager().createQuery(sql, Query.JCR_SQL2);
            query.setLimit(MAX_RESULTS * 4); // over-fetch to allow tag filtering, then cap
            ArrayNode results = JSON.arrayNode();
            NodeIterator it = query.execute().getNodes();
            JsonNode tagFilter = args.path("tags");
            while (it.hasNext() && results.size() < MAX_RESULTS) {
                Node n = it.nextNode();
                if (tagFilter.isArray() && tagFilter.size() > 0 && !hasAnyTag(n, tagFilter)) continue;
                results.add(summarize(n));
            }
            ObjectNode out = JSON.objectNode();
            out.set("results", results);
            return out;
        } catch (Exception e) {
            throw new ToolException("editorial tool failed: " + e.getMessage(), e);
        }
    }

    private boolean hasAnyTag(Node n, JsonNode wantedTags) throws Exception {
        if (!n.hasProperty("tags")) return false;
        for (Value v : n.getProperty("tags").getValues()) {
            String tag = v.getString();
            for (JsonNode t : wantedTags) if (tag.equalsIgnoreCase(t.asText())) return true;
        }
        return false;
    }

    private ObjectNode summarize(Node n) throws Exception {
        ObjectNode o = JSON.objectNode();
        o.put("id", n.getName());
        if (n.hasProperty("title"))    o.put("title", n.getProperty("title").getString());
        if (n.hasProperty("abstract")) o.put("summary", n.getProperty("abstract").getString());
        else if (n.hasProperty("text")) o.put("summary", truncate(n.getProperty("text").getString()));
        o.put("url", n.getPath());
        return o;
    }

    private static String truncate(String s) {
        return s == null ? "" : (s.length() > 240 ? s.substring(0, 240) + "…" : s);
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add EditorialTool"
```

---

## Task 13: GeminiToolAdapter

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/llm/GeminiToolAdapter.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/llm/GeminiToolAdapterTest.java`

Two static methods:
- `ObjectNode toGeminiTools(List<Tool> tools)` — produces the `{ "function_declarations": [ { name, description, parameters }, ... ] }` shape Gemini expects, wrapped in a top-level `{ "tools": [ ... ] }` array entry. We'll return an `ObjectNode` with one `tools` array and one entry containing `function_declarations` so the caller can drop it into a Gemini request.
- `Optional<FunctionCall> parseFunctionCall(JsonNode geminiResponse)` — walks `candidates[0].content.parts[*].functionCall`, returns the first one. `FunctionCall` is a small record `(String name, JsonNode args)`.

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.llm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.demo.travel.chatbot.tools.Tool;
import info.magnolia.demo.travel.chatbot.tools.ToolContext;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class GeminiToolAdapterTest {

    private static Tool stubTool(String name) {
        return new Tool() {
            @Override public String name() { return name; }
            @Override public String description() { return name + " desc"; }
            @Override public JsonNode parametersSchema() {
                return JsonNodeFactory.instance.objectNode().put("type", "object");
            }
            @Override public JsonNode invoke(JsonNode args, ToolContext ctx) { return null; }
        };
    }

    @Test
    public void toGeminiToolsBuildsFunctionDeclarations() {
        ObjectNode out = GeminiToolAdapter.toGeminiTools(List.of(stubTool("a"), stubTool("b")));
        JsonNode decls = out.get("tools").get(0).get("function_declarations");
        assertEquals(2, decls.size());
        assertEquals("a", decls.get(0).get("name").asText());
        assertEquals("b", decls.get(1).get("name").asText());
        assertEquals("object", decls.get(0).get("parameters").get("type").asText());
    }

    @Test
    public void parsesFunctionCallFromResponse() throws Exception {
        String body = """
            { "candidates": [ { "content": { "parts": [
              { "functionCall": { "name": "tours", "args": { "operation": "searchTours" } } }
            ] } } ] }""";
        Optional<GeminiToolAdapter.FunctionCall> fc =
                GeminiToolAdapter.parseFunctionCall(new ObjectMapper().readTree(body));
        assertTrue(fc.isPresent());
        assertEquals("tours", fc.get().name());
        assertEquals("searchTours", fc.get().args().get("operation").asText());
    }

    @Test
    public void noFunctionCallReturnsEmpty() throws Exception {
        String body = """
            { "candidates": [ { "content": { "parts": [
              { "text": "Bali in November is a great match." }
            ] } } ] }""";
        assertFalse(GeminiToolAdapter.parseFunctionCall(new ObjectMapper().readTree(body)).isPresent());
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement**

```java
package info.magnolia.demo.travel.chatbot.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.demo.travel.chatbot.tools.Tool;

import java.util.List;
import java.util.Optional;

public final class GeminiToolAdapter {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;
    private GeminiToolAdapter() {}

    public record FunctionCall(String name, JsonNode args) {}

    public static ObjectNode toGeminiTools(List<Tool> tools) {
        ObjectNode root = JSON.objectNode();
        ArrayNode toolsArray = root.putArray("tools");
        ObjectNode entry = toolsArray.addObject();
        ArrayNode decls = entry.putArray("function_declarations");
        for (Tool t : tools) {
            ObjectNode d = decls.addObject();
            d.put("name", t.name());
            d.put("description", t.description());
            d.set("parameters", t.parametersSchema());
        }
        return root;
    }

    public static Optional<FunctionCall> parseFunctionCall(JsonNode geminiResponse) {
        JsonNode parts = geminiResponse.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray()) return Optional.empty();
        for (JsonNode part : parts) {
            JsonNode fc = part.path("functionCall");
            if (fc.isObject() && fc.hasNonNull("name")) {
                return Optional.of(new FunctionCall(fc.get("name").asText(), fc.path("args")));
            }
        }
        return Optional.empty();
    }

    public static String parseTextReply(JsonNode geminiResponse) {
        JsonNode parts = geminiResponse.path("candidates").path(0).path("content").path("parts");
        StringBuilder sb = new StringBuilder();
        if (parts.isArray()) for (JsonNode part : parts) {
            if (part.hasNonNull("text")) sb.append(part.get("text").asText());
        }
        return sb.toString();
    }
}
```

- [ ] **Step 4: Run, expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): add GeminiToolAdapter for tool/response conversion"
```

---

## Task 14: GeminiClient (with WireMock-backed test)

**Files:**
- Modify: `travel-demo-chatbot/pom.xml` — add WireMock test dependency
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/llm/LlmException.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/llm/GeminiClient.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/llm/GeminiClientTest.java`

`GeminiClient` exposes one method:

```java
JsonNode generate(String model, ObjectNode requestBody) throws LlmException;
```

It builds the URL `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`, attaches the `x-goog-api-key` header, sends `requestBody` as JSON, and returns the parsed response body. Retries 429/5xx with exponential backoff (250ms, 1s, 4s, with up to 100ms jitter) up to 3 attempts. Honors a configurable per-call timeout (constructor argument). Surfaces `LlmException(int status, String message)` on terminal failures.

For testability, the client takes an injectable base URL (defaulting to the real Gemini endpoint) and an injectable `Sleeper` for jittered backoff (so tests can run without sleeps).

- [ ] **Step 1: Add WireMock**

```xml
<dependency>
  <groupId>com.github.tomakehurst</groupId>
  <artifactId>wiremock-jre8-standalone</artifactId>
  <version>3.0.1</version>
  <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.llm;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Rule;
import org.junit.Test;

public class GeminiClientTest {

    @Rule public WireMockRule wm = new WireMockRule(0); // random port

    private GeminiClient newClient() {
        return new GeminiClient("test-api-key", "http://localhost:" + wm.port() + "/v1beta",
                                5000, durMs -> {/* no-op sleep */});
    }

    private static ObjectNode emptyBody() {
        return JsonNodeFactory.instance.objectNode();
    }

    @Test
    public void successReturnsParsedBody() throws Exception {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .withHeader("x-goog-api-key", equalTo("test-api-key"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"hi\"}]}}]}")));

        JsonNode out = newClient().generate("gemini-3-flash-preview", emptyBody());
        assertEquals("hi", out.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText());
    }

    @Test
    public void retriesOn500ThenSucceeds() throws Exception {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("retry").whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("once"));
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("retry").whenScenarioStateIs("once")
                .willReturn(aResponse().withStatus(200).withBody("{\"ok\":true}")));

        JsonNode out = newClient().generate("m", emptyBody());
        assertEquals(true, out.get("ok").asBoolean());
    }

    @Test
    public void givesUpAfterMaxRetries() {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .willReturn(aResponse().withStatus(503)));
        LlmException ex = assertThrows(LlmException.class,
                () -> newClient().generate("m", emptyBody()));
        assertEquals(503, ex.status());
    }

    @Test
    public void surfaces4xxImmediately() {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .willReturn(aResponse().withStatus(400).withBody("bad")));
        LlmException ex = assertThrows(LlmException.class,
                () -> newClient().generate("m", emptyBody()));
        assertEquals(400, ex.status());
    }
}
```

- [ ] **Step 3: Run, expect FAIL**

- [ ] **Step 4: Implement `LlmException`**

```java
package info.magnolia.demo.travel.chatbot.llm;

public class LlmException extends Exception {
    private final int status;
    public LlmException(int status, String msg) { super(msg); this.status = status; }
    public LlmException(int status, String msg, Throwable cause) { super(msg, cause); this.status = status; }
    public int status() { return status; }
}
```

- [ ] **Step 5: Implement `GeminiClient`**

```java
package info.magnolia.demo.travel.chatbot.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GeminiClient {

    interface Sleeper { void sleep(long millis) throws InterruptedException; }
    private static final Sleeper DEFAULT_SLEEPER = Thread::sleep;

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final long[] BACKOFF_MS = { 250, 1000, 4000 };

    private final String apiKey;
    private final String baseUrl;
    private final int timeoutMs;
    private final Sleeper sleeper;
    private final HttpClient http;

    public GeminiClient(String apiKey, String baseUrl, int timeoutMs, Sleeper sleeper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.sleeper = sleeper;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
    }

    public JsonNode generate(String model, ObjectNode body) throws LlmException {
        String url = baseUrl + "/models/" + model + ":generateContent";
        byte[] payload;
        try {
            payload = JSON.writeValueAsBytes(body);
        } catch (Exception e) {
            throw new LlmException(0, "request serialization failed", e);
        }

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();

        int attempt = 0;
        while (true) {
            try {
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                int status = resp.statusCode();
                if (status >= 200 && status < 300) {
                    return JSON.readTree(resp.body());
                }
                if (isRetryable(status) && attempt < BACKOFF_MS.length) {
                    long wait = BACKOFF_MS[attempt] + ThreadLocalRandom.current().nextLong(0, 100);
                    log.warn("Gemini {} on attempt {}, retrying in {}ms", status, attempt + 1, wait);
                    sleeper.sleep(wait);
                    attempt++;
                    continue;
                }
                throw new LlmException(status, "Gemini call failed: HTTP " + status);
            } catch (IOException | InterruptedException e) {
                if (attempt < BACKOFF_MS.length) {
                    long wait = BACKOFF_MS[attempt] + ThreadLocalRandom.current().nextLong(0, 100);
                    log.warn("Gemini transport error on attempt {} ({}), retrying in {}ms",
                             attempt + 1, e.getClass().getSimpleName(), wait);
                    try { sleeper.sleep(wait); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new LlmException(0, "interrupted", ie); }
                    attempt++;
                    continue;
                }
                throw new LlmException(0, "transport error: " + e.getMessage(), e);
            }
        }
    }

    private static boolean isRetryable(int status) {
        return status == 429 || (status >= 500 && status < 600);
    }
}
```

- [ ] **Step 6: Run, expect PASS** — `mvn -pl travel-demo-chatbot test -Dtest=GeminiClientTest`

- [ ] **Step 7: Commit**

```bash
git commit -am "feat(chatbot): add GeminiClient with retry/backoff"
```

---

## Task 15: ChatEndpoint (orchestration)

**Files:**
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatTurnRequest.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatTurnResponse.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/rest/TourSummary.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatEndpoint.java`
- Create: `src/main/java/info/magnolia/demo/travel/chatbot/rest/ChatEndpointDefinition.java`
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/rest/ChatEndpointTest.java`

`ChatEndpoint` is a JAX-RS resource (Magnolia REST-EASY) annotated with `@Path("/v1")` and a `POST /turn` method.

The endpoint orchestrates: rate-limit check → length check → resolve language + traits → load history → loop calling `GeminiClient` and dispatching tool calls via `ToolRegistry` (capped at `cfg.maxToolIterations`) → append turns → trim to `historyTurnLimit` → return JSON.

Hard guarantees per spec §4.3:
- Failures during the LLM loop leave history unchanged (we mutate a *snapshot* and only commit after success).
- API key check happens at construction time; if missing, the endpoint short-circuits to 503.

The test exercises the orchestration with all collaborators mocked.

- [ ] **Step 1: Write the failing test**

```java
package info.magnolia.demo.travel.chatbot.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.demo.travel.chatbot.ChatbotModule;
import info.magnolia.demo.travel.chatbot.i18n.LanguageResolver;
import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver;
import info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter;
import info.magnolia.demo.travel.chatbot.session.ChatSessionStore;
import info.magnolia.demo.travel.chatbot.tools.ToolRegistry;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ChatEndpointTest {

    private GeminiClient gemini;
    private ToolRegistry registry;
    private ChatSessionStore sessions;
    private SessionRateLimiter limiter;
    private LanguageResolver lang;
    private VisitorTraitsResolver traits;
    private ChatbotModule cfg;
    private ChatEndpoint endpoint;
    private HttpServletRequest req;

    @Before
    public void setup() {
        gemini = mock(GeminiClient.class);
        registry = mock(ToolRegistry.class);
        sessions = new ChatSessionStore();
        limiter = mock(SessionRateLimiter.class);
        lang = mock(LanguageResolver.class);
        traits = mock(VisitorTraitsResolver.class);
        cfg = new ChatbotModule();
        endpoint = new ChatEndpoint("test-key", cfg, gemini, registry, sessions, limiter, lang, traits);
        req = mock(HttpServletRequest.class);
        when(req.getCookies()).thenReturn(new Cookie[0]);
        when(limiter.tryAcquire(any())).thenReturn(true);
        when(lang.resolve(any())).thenReturn("en");
        when(traits.resolve()).thenReturn(Map.of());
    }

    @Test
    public void happyPathReturnsAssistantReply() throws Exception {
        ObjectMapper m = new ObjectMapper();
        JsonNode reply = m.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello!\"}]}}]}");
        when(gemini.generate(eq("gemini-3-flash-preview"), any())).thenReturn(reply);

        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertEquals(200, resp.getStatus());
        ChatTurnResponse body = (ChatTurnResponse) resp.getEntity();
        assertEquals("Hello!", body.assistantMessage());
    }

    @Test
    public void rateLimitReturns429() {
        when(limiter.tryAcquire(any())).thenReturn(false);
        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertEquals(429, resp.getStatus());
    }

    @Test
    public void oversizedMessageReturns413() {
        cfg.setMaxUserMessageChars(5);
        Response resp = endpoint.turn(req, new ChatTurnRequest("123456"));
        assertEquals(413, resp.getStatus());
    }

    @Test
    public void missingApiKeyReturns503() {
        ChatEndpoint noKey = new ChatEndpoint(null, cfg, gemini, registry, sessions, limiter, lang, traits);
        Response resp = noKey.turn(req, new ChatTurnRequest("hi"));
        assertEquals(503, resp.getStatus());
    }

    @Test
    public void llmFailureLeavesHistoryUnchanged() throws Exception {
        when(gemini.generate(any(), any())).thenThrow(new info.magnolia.demo.travel.chatbot.llm.LlmException(503, "down"));
        // Pre-seed sessions.getOrCreate with no entries
        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertEquals(502, resp.getStatus());
        assertEquals(0, sessions.getOrCreate("any").turns().size());
    }

    @Test
    public void responseSetsSessionCookieWhenAbsent() throws Exception {
        ObjectMapper m = new ObjectMapper();
        when(gemini.generate(any(), any())).thenReturn(m.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}"));
        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertNotNull(resp.getCookies().get("MGNL_CHAT_SID"));
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

- [ ] **Step 3: Implement the value types**

```java
// ChatTurnRequest.java
package info.magnolia.demo.travel.chatbot.rest;
public record ChatTurnRequest(String userMessage) {}
```

```java
// TourSummary.java
package info.magnolia.demo.travel.chatbot.rest;
public record TourSummary(String id, String title, String url) {}
```

```java
// ChatTurnResponse.java
package info.magnolia.demo.travel.chatbot.rest;

import java.util.List;

public record ChatTurnResponse(String assistantMessage, List<TourSummary> suggestedTours) {}
```

- [ ] **Step 4: Implement `ChatEndpoint`**

```java
package info.magnolia.demo.travel.chatbot.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.demo.travel.chatbot.ChatbotModule;
import info.magnolia.demo.travel.chatbot.i18n.LanguageResolver;
import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.demo.travel.chatbot.llm.GeminiToolAdapter;
import info.magnolia.demo.travel.chatbot.llm.LlmException;
import info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver;
import info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter;
import info.magnolia.demo.travel.chatbot.session.ChatSessionStore;
import info.magnolia.demo.travel.chatbot.session.ConversationHistory;
import info.magnolia.demo.travel.chatbot.session.Turn;
import info.magnolia.demo.travel.chatbot.tools.Tool;
import info.magnolia.demo.travel.chatbot.tools.ToolContext;
import info.magnolia.demo.travel.chatbot.tools.ToolException;
import info.magnolia.demo.travel.chatbot.tools.ToolRegistry;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1")
public class ChatEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ChatEndpoint.class);
    private static final String COOKIE = "MGNL_CHAT_SID";
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final String apiKey;
    private final ChatbotModule cfg;
    private final GeminiClient gemini;
    private final ToolRegistry registry;
    private final ChatSessionStore sessions;
    private final SessionRateLimiter limiter;
    private final LanguageResolver lang;
    private final VisitorTraitsResolver traits;

    @Inject
    public ChatEndpoint(String apiKey, ChatbotModule cfg, GeminiClient gemini, ToolRegistry registry,
                        ChatSessionStore sessions, SessionRateLimiter limiter,
                        LanguageResolver lang, VisitorTraitsResolver traits) {
        this.apiKey = apiKey;
        this.cfg = cfg;
        this.gemini = gemini;
        this.registry = registry;
        this.sessions = sessions;
        this.limiter = limiter;
        this.lang = lang;
        this.traits = traits;
    }

    @POST @Path("/turn")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response turn(@Context HttpServletRequest request, ChatTurnRequest body) {
        long t0 = System.currentTimeMillis();
        if (apiKey == null || apiKey.isBlank()) {
            return Response.status(503).entity(Map.of("error", "LLM_UNCONFIGURED")).build();
        }
        if (body == null || body.userMessage() == null || body.userMessage().isBlank()) {
            return Response.status(400).entity(Map.of("error", "EMPTY_MESSAGE")).build();
        }
        if (body.userMessage().length() > cfg.getMaxUserMessageChars()) {
            return Response.status(413).entity(Map.of("error", "MESSAGE_TOO_LONG")).build();
        }

        String sid = readOrCreateSessionId(request);
        if (!limiter.tryAcquire(sid)) {
            return Response.status(429).entity(Map.of("error", "RATE_LIMITED")).build();
        }

        String language = lang.resolve(request);
        Map<String, String> traitMap = traits.resolve();
        ConversationHistory authoritative = sessions.getOrCreate(sid);

        // Snapshot history so failures don't dirty state.
        ConversationHistory working = copyOf(authoritative);
        working.append(Turn.user(body.userMessage()));

        String assistantText;
        try {
            assistantText = runToolLoop(language, traitMap, working);
        } catch (LlmException e) {
            log.error("Gemini call failed: {}", e.getMessage());
            return Response.status(502).entity(Map.of("error", "LLM_UNAVAILABLE")).build();
        }

        working.append(Turn.assistant(assistantText));
        working.trimTo(cfg.getHistoryTurnLimit());
        replace(authoritative, working);

        log.info("chat-turn sid={} lang={} latencyMs={} status=ok",
                 sha8(sid), language, System.currentTimeMillis() - t0);

        ChatTurnResponse resp = new ChatTurnResponse(assistantText, List.of());
        Response.ResponseBuilder rb = Response.ok(resp);
        if (!hasCookie(request)) {
            rb.cookie(new NewCookie.Builder(COOKIE).value(sid).path("/").httpOnly(true).build());
        }
        return rb.build();
    }

    private String runToolLoop(String language, Map<String, String> traits, ConversationHistory history)
            throws LlmException {
        String systemPrompt = cfg.getSystemPromptTemplate()
                .replace("${language}", language)
                .replace("${visitorTraits}", traits.isEmpty() ? "(none)" : traits.toString());

        List<Tool> enabled = registry.enabled(cfg.getEnabledTools());

        for (int iter = 0; iter < cfg.getMaxToolIterations(); iter++) {
            ObjectNode req = buildGeminiRequest(systemPrompt, enabled, history);
            JsonNode resp = gemini.generate(cfg.getModel(), req);

            Optional<GeminiToolAdapter.FunctionCall> fc = GeminiToolAdapter.parseFunctionCall(resp);
            if (fc.isEmpty()) {
                return GeminiToolAdapter.parseTextReply(resp);
            }
            JsonNode toolResult;
            try {
                toolResult = registry.invoke(fc.get().name(), fc.get().args(), new ToolContext(language));
            } catch (ToolException te) {
                ObjectNode err = JSON.objectNode();
                err.put("error", "tool_failed");
                err.put("message", te.getMessage());
                toolResult = err;
                log.warn("tool '{}' failed: {}", fc.get().name(), te.getMessage());
            }
            history.append(Turn.tool(fc.get().name(), toolResult.toString()));
        }
        return "(I couldn't fully resolve that — try rephrasing.)";
    }

    private ObjectNode buildGeminiRequest(String systemPrompt, List<Tool> enabled, ConversationHistory history) {
        ObjectNode req = JSON.objectNode();
        ObjectNode sys = req.putObject("systemInstruction");
        ArrayNode sysParts = sys.putArray("parts");
        sysParts.addObject().put("text", systemPrompt);

        ArrayNode contents = req.putArray("contents");
        for (Turn t : history.turns()) {
            ObjectNode c = contents.addObject();
            c.put("role", switch (t.role()) {
                case USER -> "user";
                case ASSISTANT -> "model";
                case TOOL -> "function";
            });
            ArrayNode parts = c.putArray("parts");
            if (t.role() == Turn.Role.TOOL) {
                ObjectNode p = parts.addObject();
                ObjectNode fr = p.putObject("functionResponse");
                fr.put("name", t.toolName());
                fr.putObject("response").put("content", t.content());
            } else {
                parts.addObject().put("text", t.content() == null ? "" : t.content());
            }
        }
        if (!enabled.isEmpty()) {
            req.set("tools", GeminiToolAdapter.toGeminiTools(enabled).get("tools"));
        }
        return req;
    }

    private String readOrCreateSessionId(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) if (COOKIE.equals(c.getName())) return c.getValue();
        }
        return UUID.randomUUID().toString();
    }

    private boolean hasCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return false;
        for (Cookie c : req.getCookies()) if (COOKIE.equals(c.getName())) return true;
        return false;
    }

    private static ConversationHistory copyOf(ConversationHistory src) {
        ConversationHistory out = new ConversationHistory();
        for (Turn t : src.turns()) out.append(t);
        return out;
    }

    private static void replace(ConversationHistory target, ConversationHistory source) {
        target.trimTo(0);
        for (Turn t : source.turns()) target.append(t);
    }

    private static String sha8(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes());
            return HexFormat.of().formatHex(d).substring(0, 8);
        } catch (Exception e) { return "0"; }
    }
}
```

- [ ] **Step 5: Implement `ChatEndpointDefinition`**

Magnolia's REST framework registers endpoints via a `ConfiguredEndpointDefinition`. For our purposes, the YAML descriptor (Task 16) is enough — most REST-EASY endpoints don't need a custom definition class. Skip this file unless Task 16's wiring fails.

- [ ] **Step 6: Run, expect PASS**

- [ ] **Step 7: Commit**

```bash
git commit -am "feat(chatbot): add ChatEndpoint orchestration with rate-limit and tool loop"
```

---

## Task 16: Register the REST endpoint via YAML and bootstrap the module config

**Files:**
- Create: `src/main/resources/travel-demo-chatbot/restEndpoints/chatbot/chatbot_v1.yaml`
- Modify: `setup/ChatbotModuleVersionHandler.java` — bootstrap the config node and the API-key reading

- [ ] **Step 1: Write the YAML endpoint descriptor**

`travel-demo-chatbot/src/main/resources/travel-demo-chatbot/restEndpoints/chatbot/chatbot_v1.yaml`:

```yaml
class: info.magnolia.rest.registry.ConfiguredEndpointDefinition
implementationClass: info.magnolia.demo.travel.chatbot.rest.ChatEndpoint
```

(Magnolia auto-discovers files under `restEndpoints/<group>/` and registers them as JAX-RS resources at `/.rest/<group>/<file-stem>` — so this becomes `/.rest/chatbot/v1` with `POST /turn` reachable at `/.rest/chatbot/v1/turn`.)

- [ ] **Step 2: Wire the API key into the endpoint at construction time**

Update `ChatbotModule` to expose an `apiKey()` accessor that pulls from the merged env (Task 3) once at module start. Add a small init block:

```java
// in ChatbotModule.java
private String apiKey;

public void setApiKey(String k) { this.apiKey = k; }
public String getApiKey() {
    if (apiKey == null) {
        java.util.Map<String,String> merged = info.magnolia.demo.travel.chatbot.env.EnvLoader
                .merge(info.magnolia.demo.travel.chatbot.env.EnvLoader.loadFile(new java.io.File(".env")),
                       System.getenv());
        apiKey = merged.get("GEMINI_API_KEY");
    }
    return apiKey;
}
```

Then, register `ChatEndpoint` as a Magnolia component with a custom provider that calls `cfg.getApiKey()`. Add a `<components>` block in `META-INF/magnolia/travel-demo-chatbot.xml`:

```xml
<components>
  <id>main</id>
  <component>
    <type>info.magnolia.demo.travel.chatbot.tools.ToursTool</type>
    <implementation>info.magnolia.demo.travel.chatbot.tools.ToursTool</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.tools.DestinationsTool</type>
    <implementation>info.magnolia.demo.travel.chatbot.tools.DestinationsTool</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.tools.EditorialTool</type>
    <implementation>info.magnolia.demo.travel.chatbot.tools.EditorialTool</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.tools.ToolRegistry</type>
    <implementation>info.magnolia.demo.travel.chatbot.tools.ToolRegistry</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.session.ChatSessionStore</type>
    <implementation>info.magnolia.demo.travel.chatbot.session.ChatSessionStore</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter</type>
    <implementation>info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.i18n.LanguageResolver</type>
    <implementation>info.magnolia.demo.travel.chatbot.i18n.LanguageResolver</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver</type>
    <implementation>info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver</implementation>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.llm.GeminiClient</type>
    <implementation>info.magnolia.demo.travel.chatbot.llm.GeminiClient</implementation>
    <provider>info.magnolia.demo.travel.chatbot.GeminiClientProvider</provider>
    <scope>singleton</scope>
  </component>
  <component>
    <type>info.magnolia.demo.travel.chatbot.rest.ChatEndpoint</type>
    <implementation>info.magnolia.demo.travel.chatbot.rest.ChatEndpoint</implementation>
    <provider>info.magnolia.demo.travel.chatbot.ChatEndpointProvider</provider>
    <scope>singleton</scope>
  </component>
</components>
```

- [ ] **Step 3: Implement two providers that read from `ChatbotModule`**

Create `src/main/java/info/magnolia/demo/travel/chatbot/GeminiClientProvider.java`:

```java
package info.magnolia.demo.travel.chatbot;

import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.objectfactory.ComponentProvider;

import javax.inject.Inject;
import javax.inject.Provider;

public class GeminiClientProvider implements Provider<GeminiClient> {
    private final ChatbotModule cfg;
    @Inject public GeminiClientProvider(ChatbotModule cfg) { this.cfg = cfg; }
    @Override public GeminiClient get() {
        return new GeminiClient(cfg.getApiKey(), "https://generativelanguage.googleapis.com/v1beta",
                                cfg.getRequestTimeoutMs(), Thread::sleep);
    }
}
```

Create `src/main/java/info/magnolia/demo/travel/chatbot/ChatEndpointProvider.java`:

```java
package info.magnolia.demo.travel.chatbot;

import info.magnolia.demo.travel.chatbot.i18n.LanguageResolver;
import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver;
import info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter;
import info.magnolia.demo.travel.chatbot.rest.ChatEndpoint;
import info.magnolia.demo.travel.chatbot.session.ChatSessionStore;
import info.magnolia.demo.travel.chatbot.tools.ToolRegistry;

import javax.inject.Inject;
import javax.inject.Provider;

public class ChatEndpointProvider implements Provider<ChatEndpoint> {
    private final ChatbotModule cfg;
    private final GeminiClient gemini;
    private final ToolRegistry tools;
    private final ChatSessionStore sessions;
    private final SessionRateLimiter limiter;
    private final LanguageResolver lang;
    private final VisitorTraitsResolver traits;
    @Inject public ChatEndpointProvider(ChatbotModule cfg, GeminiClient gemini, ToolRegistry tools,
                                        ChatSessionStore sessions, SessionRateLimiter limiter,
                                        LanguageResolver lang, VisitorTraitsResolver traits) {
        this.cfg = cfg; this.gemini = gemini; this.tools = tools; this.sessions = sessions;
        this.limiter = limiter; this.lang = lang; this.traits = traits;
    }
    @Override public ChatEndpoint get() {
        return new ChatEndpoint(cfg.getApiKey(), cfg, gemini, tools, sessions, limiter, lang, traits);
    }
}
```

- [ ] **Step 4: Verify build still passes**

```bash
mvn -pl travel-demo-chatbot -am -q test
```

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(chatbot): register REST endpoint and component providers"
```

---

## Task 17: Front-end (vanilla JS + CSS + FreeMarker templates)

**Files:**
- Create: `src/main/resources/travel-demo-chatbot-theme/js/chatbot.js`
- Create: `src/main/resources/travel-demo-chatbot-theme/css/chatbot.css`
- Create: `src/main/resources/travel-demo-chatbot/templates/components/chatbot.yaml`
- Create: `src/main/resources/travel-demo-chatbot/templates/components/chatbot.ftl`
- Create: `src/main/resources/travel-demo-chatbot/templates/snippets/chatbot-floating.ftl`
- Create: `src/main/resources/travel-demo-chatbot/dialogs/chatbotComponent.yaml`

The front-end has no automated tests — verify in the manual rehearsal (Task 21 step 3).

- [ ] **Step 1: Read the existing site template structure**

Inspect `travel-demo-module/src/main/resources/travel-demo/templates/pages/standard.ftl` (or whatever the active page template is) to find the closing `</body>` location. Note the path to the site definition under `travel-demo-module/src/main/resources/travel-demo/sites/` — we'll decorate it from Task 18 to inject the floating-widget include.

If the site template's footer area is a named area, prefer adding the floating-widget include via a decoration on that area. If `</body>` is hard-coded in the template, use a decoration on the template's render path. Capture the chosen path here:

> `travel-demo-module/src/main/resources/travel-demo/templates/pages/standard.ftl` — line near `</body>`.

- [ ] **Step 2: Write `chatbot.js`** (~120 lines, vanilla JS)

```javascript
(function () {
  const ENDPOINT = '/.rest/chatbot/v1/turn';
  const STORAGE_KEY = 'mgnl-chatbot-transcript';

  function el(tag, cls, txt) { const e = document.createElement(tag); if (cls) e.className = cls; if (txt) e.textContent = txt; return e; }

  function loadTranscript() {
    try { return JSON.parse(sessionStorage.getItem(STORAGE_KEY) || '[]'); } catch { return []; }
  }
  function saveTranscript(arr) {
    try { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(arr)); } catch {}
  }

  function appendBubble(panel, role, text) {
    const b = el('div', 'mgnl-chatbot-msg mgnl-chatbot-msg--' + role);
    b.appendChild(el('div', 'mgnl-chatbot-msg__text', text));
    panel.querySelector('.mgnl-chatbot-log').appendChild(b);
    panel.querySelector('.mgnl-chatbot-log').scrollTop = 1e9;
  }

  async function send(panel, message) {
    const log = panel.querySelector('.mgnl-chatbot-log');
    const thinking = el('div', 'mgnl-chatbot-msg mgnl-chatbot-msg--thinking', panel.dataset.thinking || '…');
    log.appendChild(thinking);
    try {
      const r = await fetch(ENDPOINT, {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userMessage: message })
      });
      thinking.remove();
      if (!r.ok) {
        appendBubble(panel, 'error', panel.dataset.errorGeneric || 'Something went wrong.');
        return;
      }
      const data = await r.json();
      appendBubble(panel, 'assistant', data.assistantMessage || '');
      const transcript = loadTranscript();
      transcript.push({ role: 'user', text: message });
      transcript.push({ role: 'assistant', text: data.assistantMessage });
      saveTranscript(transcript);
    } catch (e) {
      thinking.remove();
      appendBubble(panel, 'error', panel.dataset.errorGeneric || 'Something went wrong.');
    }
  }

  function buildPanel(opts) {
    const panel = el('div', 'mgnl-chatbot mgnl-chatbot--' + opts.mode);
    panel.dataset.locale = opts.locale || 'en';
    panel.dataset.thinking = opts.thinkingLabel || '…';
    panel.dataset.errorGeneric = opts.errorLabel || 'Something went wrong.';

    panel.appendChild(el('div', 'mgnl-chatbot-log'));
    const form = el('form', 'mgnl-chatbot-form');
    const input = el('input', 'mgnl-chatbot-input');
    input.type = 'text';
    input.placeholder = opts.placeholder || 'Ask anything…';
    const button = el('button', 'mgnl-chatbot-submit', opts.sendLabel || 'Send');
    button.type = 'submit';
    form.append(input, button);
    panel.appendChild(form);

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const v = input.value.trim();
      if (!v) return;
      input.value = '';
      appendBubble(panel, 'user', v);
      send(panel, v);
    });

    // restore transcript
    for (const t of loadTranscript()) appendBubble(panel, t.role, t.text);

    return panel;
  }

  const MagnoliaChatbot = {
    init(opts) {
      const mount = document.querySelector(opts.mountSelector);
      if (!mount) return;
      const panel = buildPanel(opts);
      if (opts.mode === 'widget') {
        const launcher = el('button', 'mgnl-chatbot-launcher', opts.launcherLabel || '💬');
        launcher.type = 'button';
        const wrap = el('div', 'mgnl-chatbot-widget');
        wrap.style.display = 'none';
        wrap.appendChild(panel);
        mount.append(launcher, wrap);
        launcher.addEventListener('click', () => {
          wrap.style.display = wrap.style.display === 'none' ? 'block' : 'none';
        });
      } else {
        mount.appendChild(panel);
      }
    }
  };
  window.MagnoliaChatbot = MagnoliaChatbot;
})();
```

- [ ] **Step 3: Write `chatbot.css`** — minimal styling, scoped to `.mgnl-chatbot-*`. ~60 lines covering: floating launcher position (`position: fixed; right: 16px; bottom: 16px;`), open panel sizing (`width: 360px; max-height: 480px;`), message bubbles (`.mgnl-chatbot-msg--user` right-aligned, `.mgnl-chatbot-msg--assistant` left-aligned), and form layout. Use existing `travel-demo-theme` font-family / color tokens by referencing `var(--travel-demo-primary)` if present.

```css
.mgnl-chatbot, .mgnl-chatbot-launcher { font-family: inherit; font-size: 14px; }
.mgnl-chatbot-widget { position: fixed; right: 16px; bottom: 72px; width: 360px;
  max-height: 480px; background: #fff; border: 1px solid #ddd; border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15); display: flex; flex-direction: column; z-index: 9999; }
.mgnl-chatbot-launcher { position: fixed; right: 16px; bottom: 16px; width: 48px; height: 48px;
  border-radius: 50%; border: none; background: #1d6b91; color: #fff; cursor: pointer; z-index: 9999; }
.mgnl-chatbot--component { border: 1px solid #ddd; border-radius: 8px; max-width: 480px; }
.mgnl-chatbot-log { flex: 1; overflow-y: auto; padding: 12px; min-height: 240px; max-height: 400px; }
.mgnl-chatbot-msg { display: flex; margin: 4px 0; }
.mgnl-chatbot-msg--user { justify-content: flex-end; }
.mgnl-chatbot-msg--assistant, .mgnl-chatbot-msg--thinking, .mgnl-chatbot-msg--error { justify-content: flex-start; }
.mgnl-chatbot-msg__text { padding: 6px 10px; border-radius: 12px; max-width: 80%; }
.mgnl-chatbot-msg--user .mgnl-chatbot-msg__text { background: #1d6b91; color: #fff; }
.mgnl-chatbot-msg--assistant .mgnl-chatbot-msg__text { background: #f1f1f1; }
.mgnl-chatbot-msg--error .mgnl-chatbot-msg__text { background: #fbeaea; color: #b00020; }
.mgnl-chatbot-form { display: flex; border-top: 1px solid #eee; padding: 8px; gap: 8px; }
.mgnl-chatbot-input { flex: 1; padding: 6px 8px; border: 1px solid #ccc; border-radius: 4px; }
.mgnl-chatbot-submit { padding: 6px 12px; background: #1d6b91; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
```

- [ ] **Step 4: Write the placeable component template**

`templates/components/chatbot.yaml`:

```yaml
templateScript: /travel-demo-chatbot/templates/components/chatbot.ftl
renderType: freemarker
title: Travel Chatbot
visible: true
dialog: travel-demo-chatbot:chatbotComponent
```

`templates/components/chatbot.ftl`:

```freemarker
[#assign locale = ctx.locale!"en"]
[#assign mid = "mgnl-chatbot-component-" + content.@uuid?html]
<link rel="stylesheet" href="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/css/chatbot.css"/>
<script src="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/js/chatbot.js" defer></script>
<div id="${mid}"></div>
<script>
  document.addEventListener('DOMContentLoaded', function () {
    MagnoliaChatbot.init({
      mountSelector: '#${mid}',
      mode: 'component',
      locale: '${locale}',
      placeholder: '${i18n["chatbot.placeholder"]?js_string}',
      sendLabel: '${i18n["chatbot.send"]?js_string}',
      thinkingLabel: '${i18n["chatbot.thinking"]?js_string}',
      errorLabel: '${i18n["chatbot.error"]?js_string}',
      launcherLabel: '💬'
    });
  });
</script>
```

- [ ] **Step 5: Write the floating-widget snippet**

`templates/snippets/chatbot-floating.ftl`:

```freemarker
[#assign locale = ctx.locale!"en"]
<link rel="stylesheet" href="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/css/chatbot.css"/>
<script src="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/js/chatbot.js" defer></script>
<div id="mgnl-chatbot-floating"></div>
<script>
  document.addEventListener('DOMContentLoaded', function () {
    MagnoliaChatbot.init({
      mountSelector: '#mgnl-chatbot-floating',
      mode: 'widget',
      locale: '${locale}',
      placeholder: '${i18n["chatbot.placeholder"]?js_string}',
      sendLabel: '${i18n["chatbot.send"]?js_string}',
      thinkingLabel: '${i18n["chatbot.thinking"]?js_string}',
      errorLabel: '${i18n["chatbot.error"]?js_string}',
      launcherLabel: '💬'
    });
  });
</script>
```

- [ ] **Step 6: Write the empty component dialog**

`dialogs/chatbotComponent.yaml`:

```yaml
form:
  properties: {}
actions:
  commit:
    class: info.magnolia.ui.dialog.actions.SaveDialogActionDefinition
  cancel:
    class: info.magnolia.ui.dialog.actions.CancelDialogActionDefinition
```

- [ ] **Step 7: Commit**

```bash
git commit -am "feat(chatbot): add front-end JS, CSS, templates, and dialog"
```

---

## Task 18: Decoration to inject the floating widget into the site template

**Files:**
- Create: `src/main/resources/travel-demo-chatbot/decorations/travel-demo/templates/pages/standard.ftl`

In Magnolia, FreeMarker template decorations work by placing a file at the same relative path under `decorations/<module-name>/...`. The decoration *replaces* the original file unless using a delta-style merge. Since `standard.ftl` is owned by `travel-demo-module`, the safest demo-friendly approach is:

1. Read the original `travel-demo-module/src/main/resources/travel-demo/templates/pages/standard.ftl` (Task 17 step 1).
2. Copy it verbatim to the decoration path.
3. Add `<#include "/travel-demo-chatbot/templates/snippets/chatbot-floating.ftl">` immediately before `</body>`.

This keeps the chatbot module self-contained (the original `travel-demo-module` is untouched) at the cost of a small duplicated template. Acceptable for a demo. If the original template later changes, the decoration would need to be updated — note this in the README of the chatbot module if any.

- [ ] **Step 1: Copy and modify the template**

Inspect actual contents at `travel-demo-module/src/main/resources/travel-demo/templates/pages/standard.ftl`. If that file does not exist (the demo may use a different template root, e.g. `travel-demo-module/.../travel-demo/templates/pages/standard.yaml` referencing an FTL elsewhere), follow the YAML's `templateScript:` pointer to the real `.ftl` and decorate that path instead.

- [ ] **Step 2: Verify the build still passes**

```bash
mvn -pl travel-demo-chatbot -am -q test
```

- [ ] **Step 3: Commit**

```bash
git commit -am "feat(chatbot): inject floating widget via site-template decoration"
```

---

## Task 19: i18n properties (UI strings)

**Files:**
- Create: `src/main/resources/travel-demo-chatbot/i18n/chatbot_en.properties`
- Create: `src/main/resources/travel-demo-chatbot/i18n/chatbot_de.properties`
- Create: `src/main/resources/travel-demo-chatbot/i18n/chatbot_fr.properties`

- [ ] **Step 1: English defaults**

`chatbot_en.properties`:

```properties
chatbot.placeholder=Ask the travel advisor anything…
chatbot.send=Send
chatbot.thinking=Thinking…
chatbot.error=Something went wrong. Please try again.
chatbot.error.tooLong=Your message is too long. Please shorten it.
chatbot.error.rateLimited=Please wait a moment before sending another message.
chatbot.error.unconfigured=The travel advisor is not configured. Please contact the site administrator.
```

- [ ] **Step 2: German**

`chatbot_de.properties`:

```properties
chatbot.placeholder=Fragen Sie den Reiseberater alles…
chatbot.send=Senden
chatbot.thinking=Denke nach…
chatbot.error=Etwas ist schiefgelaufen. Bitte versuchen Sie es erneut.
chatbot.error.tooLong=Ihre Nachricht ist zu lang. Bitte kürzen Sie sie.
chatbot.error.rateLimited=Bitte warten Sie einen Moment, bevor Sie eine weitere Nachricht senden.
chatbot.error.unconfigured=Der Reiseberater ist nicht konfiguriert. Bitte kontaktieren Sie den Administrator.
```

- [ ] **Step 3: French**

`chatbot_fr.properties`:

```properties
chatbot.placeholder=Demandez n'importe quoi au conseiller voyage…
chatbot.send=Envoyer
chatbot.thinking=Réflexion en cours…
chatbot.error=Une erreur s'est produite. Veuillez réessayer.
chatbot.error.tooLong=Votre message est trop long. Veuillez le raccourcir.
chatbot.error.rateLimited=Veuillez patienter avant d'envoyer un autre message.
chatbot.error.unconfigured=Le conseiller voyage n'est pas configuré. Veuillez contacter l'administrateur.
```

- [ ] **Step 4: Commit**

```bash
git commit -am "feat(chatbot): add i18n properties (en/de/fr)"
```

---

## Task 20: Magnolia App for editor configuration

**Files:**
- Create: `src/main/resources/travel-demo-chatbot/apps/chatbot-config-app.yaml`
- Create: `src/main/resources/travel-demo-chatbot/decorations/admincentral/config.yaml`

The App is a single-detail Configuration app pointing at `/modules/travel-demo-chatbot/config`. Modeled after Magnolia's stock Configuration App but scoped to one node. Editor sees a form with the fields from spec §7. The "API key status" badge is rendered by a custom `info` field that reads `ChatbotModule.getApiKey()` at form-load.

For the demo, a simpler pragmatic alternative is to skip the custom badge and just rely on the standard fields, with a help text on the form: *"Set GEMINI_API_KEY in .env or the environment. Restart not required."* If a custom field is needed, see existing demo examples under `travel-demo-component-personalization/.../apps`.

- [ ] **Step 1: Write the App descriptor**

```yaml
# apps/chatbot-config-app.yaml
class: info.magnolia.ui.contentapp.configuration.ContentAppDescriptor
icon: icon-app
appClass: info.magnolia.ui.contentapp.ContentApp
datasource:
  $type: jcrDatasource
  workspace: config
  rootPath: /modules/travel-demo-chatbot/config
subApps:
  detail:
    $type: detailSubApp
    nodeType: mgnl:contentNode
    form:
      properties:
        model:
          $type: textField
          label: Model
        systemPromptTemplate:
          $type: textField
          label: System prompt template
          rows: 12
        enabledTools:
          $type: textField
          label: Enabled tools (comma-separated)
        maxToolIterations:
          $type: textField
          label: Max tool iterations
        historyTurnLimit:
          $type: textField
          label: History turn limit
        requestTimeoutMs:
          $type: textField
          label: Request timeout (ms)
        rateLimitPerMinute:
          $type: textField
          label: Rate limit per minute
        maxTokensPerSession:
          $type: textField
          label: Max tokens per session
        maxUserMessageChars:
          $type: textField
          label: Max user message chars
    actions:
      commit:
        $type: saveDetailAction
      cancel:
        $type: cancelFormAction
```

- [ ] **Step 2: Register the App in the Tools group via decoration**

`decorations/admincentral/config.yaml`:

```yaml
appLauncherLayout:
  groups:
    tools:
      apps:
        chatbotConfig:
          name: chatbot-config-app
```

(Confirm the exact node names/paths against `travel-demo-tours/src/main/resources/tours/decorations/admincentral/config.yaml` — that file already does this kind of decoration; copy the structure.)

- [ ] **Step 3: Verify build still passes**

```bash
mvn -pl travel-demo-chatbot -am -q test
```

- [ ] **Step 4: Commit**

```bash
git commit -am "feat(chatbot): add Chatbot Configuration App"
```

---

## Task 21: Integration test (WireMock + ChatEndpoint + ToursTool)

**Files:**
- Create: `src/test/java/info/magnolia/demo/travel/chatbot/integration/ChatbotIntegrationIT.java`

Boots an in-memory Magnolia test context with a `tours` workspace, seeds one tour, stubs Gemini via WireMock to (1) emit a `searchTours` function call, then (2) emit a final text reply once the tool response is supplied, drives `ChatEndpoint.turn`, asserts the assistant message is returned and history is persisted.

- [ ] **Step 1: Write the test**

```java
package info.magnolia.demo.travel.chatbot.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import info.magnolia.context.MgnlContext;
import info.magnolia.demo.travel.chatbot.ChatbotModule;
import info.magnolia.demo.travel.chatbot.i18n.LanguageResolver;
import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver;
import info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter;
import info.magnolia.demo.travel.chatbot.rest.ChatEndpoint;
import info.magnolia.demo.travel.chatbot.rest.ChatTurnRequest;
import info.magnolia.demo.travel.chatbot.rest.ChatTurnResponse;
import info.magnolia.demo.travel.chatbot.session.ChatSessionStore;
import info.magnolia.demo.travel.chatbot.tools.ToolRegistry;
import info.magnolia.demo.travel.chatbot.tools.ToursTool;
import info.magnolia.test.RepositoryTestCase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Rule;
import org.junit.Test;

public class ChatbotIntegrationIT extends RepositoryTestCase {

    @Rule public WireMockRule wm = new WireMockRule(0);

    private void seedTour() throws Exception {
        Session s = MgnlContext.getJCRSession("tours");
        Node n = s.getRootNode().addNode("bali-7day", "mgnl:content");
        n.setProperty("name", "bali-7day");
        n.setProperty("region", "Asia");
        n.setProperty("priceUsd", 1500L);
        n.setProperty("durationDays", 7L);
        s.save();
    }

    @Test
    public void roundTripWithToolCallProducesAssistantText() throws Exception {
        seedTour();

        // First call: model asks to search tours.
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("turn").whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("{\"candidates\":[{\"content\":{\"parts\":[{\"functionCall\":"
                                + "{\"name\":\"tours\",\"args\":{\"operation\":\"searchTours\",\"region\":\"Asia\"}}}]}}]}"))
                .willSetStateTo("post-tool"));

        // Second call: model produces a final text reply.
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("turn").whenScenarioStateIs("post-tool")
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Try the bali-7day tour.\"}]}}]}")));

        ChatbotModule cfg = new ChatbotModule();
        GeminiClient gemini = new GeminiClient("test-key", "http://localhost:" + wm.port() + "/v1beta",
                                               5000, durMs -> {});
        ToolRegistry registry = new ToolRegistry(List.of(new ToursTool()));
        ChatSessionStore sessions = new ChatSessionStore();
        SessionRateLimiter limiter = mock(SessionRateLimiter.class);
        when(limiter.tryAcquire(any())).thenReturn(true);
        LanguageResolver lang = mock(LanguageResolver.class);
        when(lang.resolve(any())).thenReturn("en");
        VisitorTraitsResolver traits = mock(VisitorTraitsResolver.class);
        when(traits.resolve()).thenReturn(Map.of());

        ChatEndpoint endpoint = new ChatEndpoint("test-key", cfg, gemini, registry, sessions, limiter, lang, traits);

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getCookies()).thenReturn(null);

        Response resp = endpoint.turn(req, new ChatTurnRequest("Where should I go in Asia?"));
        assertEquals(200, resp.getStatus());
        ChatTurnResponse body = (ChatTurnResponse) resp.getEntity();
        assertEquals("Try the bali-7day tour.", body.assistantMessage());
        assertNotNull(resp.getCookies().get("MGNL_CHAT_SID"));
    }
}
```

- [ ] **Step 2: Run, expect PASS**

```bash
mvn -pl travel-demo-chatbot test -Dtest=ChatbotIntegrationIT
```

- [ ] **Step 3: Commit**

```bash
git commit -am "test(chatbot): add WireMock-driven integration test"
```

---

## Task 22: Wire the chatbot module into the two webapps

**Files:**
- Modify: `travel-demo-webapp/pom.xml`
- Modify: `travel-demo-community-webapp/pom.xml`

- [ ] **Step 1: Add the dependency to `travel-demo-webapp/pom.xml`**

In the `<dependencies>` block, after the other `info.magnolia.demo` deps:

```xml
<dependency>
  <groupId>info.magnolia.demo</groupId>
  <artifactId>magnolia-travel-demo-chatbot</artifactId>
</dependency>
```

- [ ] **Step 2: Add the dependency to `travel-demo-community-webapp/pom.xml`** (same XML).

- [ ] **Step 3: Run a top-level reactor build**

```bash
mvn -q -DskipTests package
```

Expected: builds clean, both WAR artifacts now include the chatbot module.

- [ ] **Step 4: Commit**

```bash
git commit -am "feat(chatbot): include travel-demo-chatbot in DX core and community webapps"
```

---

## Task 23: Final acceptance — run full test suite and prepare for manual rehearsal

- [ ] **Step 1: Run the chatbot module tests**

```bash
mvn -pl travel-demo-chatbot -am test
```

Expected: green.

- [ ] **Step 2: Run the parent reactor's typecheck/lint equivalent** — Magnolia's parent POM enforces checkstyle. Run:

```bash
mvn -pl travel-demo-chatbot -am verify
```

Expected: green; address any checkstyle complaints by following sibling modules' style (header, import order, line length).

- [ ] **Step 3: Document the manual rehearsal checklist**

The spec §11.3 lists 6 manual-rehearsal steps. Run them locally now:

1. Set `GEMINI_API_KEY=...` in `.env` at the repo root.
2. Start `travel-demo-webapp` (`mvn -pl travel-demo-webapp tomcat:run` or similar — confirm by reading the project README).
3. Visit a destination page; floating widget appears; ask "Where should I go in November under $2k?" — expect a tour-grounded reply.
4. Switch the site language to German; reload; expect a German reply.
5. In the Chatbot Configuration app, disable `editorial`; ask a question that would have used editorial; check logs to confirm `searchEditorial` wasn't called.
6. Drop the "Travel Chatbot" component into a content page in the page editor; verify it works inline and shares state with the floating widget on the same page.
7. Send 31 messages within a minute; expect a rate-limit error.

- [ ] **Step 4: Final commit if anything was touched in step 2/3**

```bash
git status
git commit -am "chore(chatbot): pass checkstyle"  # only if needed
```

---

## Done

The module is feature-complete per the design spec. Future enhancements (deferred):
- Streaming responses (replaces the single `generate` call with a stream consumer).
- Vector-RAG over editorial pages.
- Provider abstraction layer if a second LLM is needed.
