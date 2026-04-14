# Travel Chatbot — Design Spec

**Date:** 2026-04-13
**Status:** Approved (design phase)
**Audience:** Implementers of the `travel-demo-chatbot` Magnolia module

## 1. Summary

Add an AI-powered travel-advice chatbot to the Magnolia Travel Demo. The
chatbot is a **demo feature** that showcases two things at once: integrating
Magnolia content (tours, destinations, editorial pages) into a live
LLM-powered experience via tool-calling, and wiring that experience into
Magnolia's existing **personalization** and **i18n** capabilities.

The bot uses Google's Gemini (`gemini-3-flash-preview` by default) over its
public REST API. It is grounded in JCR content via tool-calling — no vector
store, no embedding pipeline. Conversations are session-scoped (in-memory,
cookie-keyed). Visitors can interact with it through a floating widget
present on every page **and** through a placeable "Chatbot" component that
editors drop into any page area.

## 2. Goals & non-goals

### Goals

- Demonstrate Magnolia content powering an AI experience via tool-calling.
- Demonstrate the MTE personalization module shaping AI responses (visitor
  segments/traits flow into the system prompt).
- Demonstrate Magnolia i18n: the bot answers in the visitor's site language.
- Editor-controllable: model, system prompt, enabled tools are configurable
  through a Magnolia admin App without redeploy.
- Self-contained module that can be installed/uninstalled cleanly, matching
  the convention of sibling demo modules (`travel-demo-tours`,
  `travel-demo-content-tags`, etc.).

### Non-goals

- Authenticated visitor identity, account history, persistent
  cross-visit memory.
- Admin moderation UI / message review.
- Streaming responses (deferred to a possible v2).
- Vector-store / embeddings RAG.
- Vendor abstraction layer over multiple LLM providers (Gemini only;
  swappable later via a focused refactor of `GeminiClient` /
  `GeminiToolAdapter`).
- Production-grade abuse detection beyond rate limiting + system-prompt
  scoping.

## 3. Personalization model

"Personalization" in this design means two layered things:

1. **Conversational personalization** — the bot remembers what the visitor
   said earlier in the session (budget, region preference, travel dates)
   and tailors recommendations accordingly. State lives in
   `ChatSessionStore`, keyed by an `MGNL_CHAT_SID` cookie, scoped to the
   browser session.
2. **Visitor-trait personalization** — when the MTE personalization module
   is active (it ships with `travel-demo-webapp` but not
   `travel-demo-community-webapp`), the bot reads visitor traits
   (segment, country, language, returning-visitor flag, etc.) and
   interpolates them into the system prompt. The bot can therefore
   tailor recommendations to "Adventure Seeker / DE" differently than to
   "Family Traveler / US" without the visitor saying anything explicit.

Account-based personalization (logged-in users with stored profiles) is
out of scope.

## 4. Architecture

### 4.1 Module layout

A new Maven module **`travel-demo-chatbot`** is added to the parent reactor
in `pom.xml`, sibling to `travel-demo-tours`,
`travel-demo-component-personalization`, etc. It is added as a dependency
to both `travel-demo-webapp` and `travel-demo-community-webapp`.

```
travel-demo-chatbot/
  pom.xml
  src/main/java/info/magnolia/demo/travel/chatbot/
      ChatbotModule.java
      setup/ChatbotModuleVersionHandler.java
      rest/ChatEndpoint.java
      rest/ChatTurnRequest.java
      rest/ChatTurnResponse.java
      llm/GeminiClient.java
      llm/GeminiToolAdapter.java
      llm/LlmException.java
      tools/Tool.java                       (interface)
      tools/ToolRegistry.java
      tools/ToursTool.java
      tools/DestinationsTool.java
      tools/EditorialTool.java
      session/ChatSessionStore.java
      session/ConversationHistory.java
      i18n/LanguageResolver.java
      personalization/VisitorTraitsResolver.java
      ratelimit/SessionRateLimiter.java
      env/EnvLoader.java
  src/main/resources/
      META-INF/magnolia/travel-demo-chatbot.xml
      mgnl-bootstrap/travel-demo-chatbot/config/
          modules/travel-demo-chatbot/config.yaml
          modules/travel-demo-chatbot/apps/chatbot-config-app.yaml
          modules/rest-endpoints/chatbot.yaml
      travel-demo-chatbot/templates/components/chatbot.yaml
      travel-demo-chatbot/templates/components/chatbot.ftl
      travel-demo-chatbot/templates/snippets/chatbot-floating.ftl
      travel-demo-chatbot/dialogs/chatbotComponent.yaml
      travel-demo-chatbot/i18n/chatbot_en.properties
      travel-demo-chatbot/i18n/chatbot_de.properties
      travel-demo-chatbot/i18n/chatbot_fr.properties
      travel-demo-chatbot-theme/js/chatbot.js
      travel-demo-chatbot-theme/css/chatbot.css
  src/test/java/info/magnolia/demo/travel/chatbot/...
```

The floating widget is included on every page via a Magnolia template
**decoration** that the chatbot module applies to the existing site
template owned by `travel-demo-module`, injecting an include of
`/travel-demo-chatbot/templates/snippets/chatbot-floating.ftl` into the
footer area. Using a decoration (rather than editing
`travel-demo-module`'s template directly) keeps the chatbot module
self-contained and lets it be installed/uninstalled cleanly. If the
existing site template's structure makes a decoration awkward, a direct
include in `travel-demo-module`'s footer template is an acceptable
fallback — choose during implementation after reading that template.

### 4.2 Request flow

```
Browser (widget or component)
   └─► POST /.rest/chatbot/v1/turn  { userMessage }
        │   Cookie: MGNL_CHAT_SID=<sessionId>
        ▼
   ChatEndpoint
   ├─ rateLimiter.check(sessionId)            -> 429 if exceeded
   ├─ validateLength(userMessage)             -> 413 if oversized
   ├─ language    = LanguageResolver.resolve()
   ├─ traits      = VisitorTraitsResolver.resolve()
   ├─ history     = ChatSessionStore.get(sessionId)
   ├─ cfg         = ChatbotModule.config()
   └─► GeminiClient.generate(systemPrompt(cfg, language, traits),
                             enabledTools(cfg), history, userMessage)
            │
            ▼  (loop, capped at cfg.maxToolIterations)
       Gemini response has functionCall?
         yes: ToolRegistry.invoke(name, args) -> append functionResponse,
              call generate() again
         no:  return text
            │
            ▼
   ChatEndpoint
   ├─ append {user, assistant} turns to history
   ├─ trim to cfg.historyTurnLimit
   ├─ ChatSessionStore.put(sessionId, history)
   └─ return ChatTurnResponse { assistantMessage, suggestedTours? }
        │
        ▼
   Browser renders message + cards; mirrors transcript to sessionStorage
```

### 4.3 Key invariants

- The Gemini API key never leaves the server.
- Tool results are bounded in size (≤10 entries, summary fields only).
- A turn is atomic per user message: success → history appended; failure →
  history unchanged.
- Tool calls are constrained to content the visitor is otherwise allowed
  to see — no auth bypass via the bot.

## 5. Components

### 5.1 `ChatbotModule`

Standard Magnolia `@Singleton` module config bean, configured under
`/modules/travel-demo-chatbot/config` and bootstrapped by
`ChatbotModuleVersionHandler`. Editor-tunable fields:

| Field | Type | Default | Notes |
|---|---|---|---|
| `model` | String | `gemini-3-flash-preview` | |
| `systemPromptTemplate` | String | (see §6) | Supports `${language}` and `${visitorTraits}` placeholders |
| `enabledTools` | List<String> | `[tours, destinations, editorial]` | |
| `maxToolIterations` | int | 5 | Range 1–10 |
| `historyTurnLimit` | int | 20 | Range 4–50 |
| `requestTimeoutMs` | int | 30000 | |
| `rateLimitPerMinute` | int | 30 | |
| `maxTokensPerSession` | int | 50000 | Soft cap; trims older turns when exceeded |
| `maxUserMessageChars` | int | 4000 | |

The Gemini API key is read from the `GEMINI_API_KEY` environment variable.
For local development, `EnvLoader` (a small wrapper, see §5.11) loads
`.env` at module startup if present. The key is **deliberately not
exposed** through the admin App; the App shows only a "Detected" /
"Missing" status badge.

### 5.2 `ChatEndpoint`

REST-EASY endpoint registered at `/modules/rest-endpoints/chatbot/v1`.
Single `POST /turn` operation. Reads/sets `MGNL_CHAT_SID` cookie. Method
body is short — orchestration only; logic lives in collaborators.

Request: `ChatTurnRequest { userMessage: String }`.
Response: `ChatTurnResponse { assistantMessage: String, suggestedTours?: List<TourSummary> }`.

CSRF: requires the standard Magnolia REST CSRF token header used by
existing demo REST endpoints.

### 5.3 `GeminiClient`

Thin wrapper over Gemini's REST endpoint
`https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`.
Uses Magnolia's existing Jackson + Apache HttpClient. No SDK
dependency.

Behaviors:
- Sends `x-goog-api-key` header.
- Honors `requestTimeoutMs`.
- Retries on 429 / 5xx with exponential backoff (3 attempts:
  ~250ms, ~1s, ~4s, with jitter).
- Surfaces `LlmException` with the upstream status code on
  non-recoverable failures.

### 5.4 `GeminiToolAdapter`

Converts the local `Tool` interface (name, description, JSON-schema
parameters, `invoke(JsonNode args) -> JsonNode`) into Gemini's
`function_declarations` shape, and parses Gemini `functionCall` parts back
into `(name, args)` pairs. Handles parallel function calls in one
response. Isolated so a future provider swap is a single-class change.

### 5.5 `Tool` interface and registry

```java
interface Tool {
    String name();
    String description();
    JsonNode parametersSchema();   // JSON Schema
    JsonNode invoke(JsonNode args, ToolContext ctx) throws ToolException;
}
```

`ToolRegistry` holds the configured tools, resolves by name, exposes
those listed in `cfg.enabledTools`. Invocations failing with
`ToolException` are converted to `functionResponse` entries with
`{error: "tool_failed", message}` so Gemini can recover gracefully.

### 5.6 `ToursTool`

Operations:
- `searchTours({region?, maxPriceUsd?, durationDaysMax?, theme?})`
- `getTour({id})`

Delegates to existing `info.magnolia.demo.travel.tours.service.TourServices`
in `magnolia-travel-tours`. Output is trimmed: title, id, region, price,
duration, short summary, URL. Capped at 10 results.

### 5.7 `DestinationsTool`

Operations:
- `searchDestinations({query?, region?, climate?})`
- `getDestination({id})`

Reads from JCR `website` workspace under `/travel/destinations/...`.
Output trimmed to title, id, region, climate, summary, URL. Capped at 10.

### 5.8 `EditorialTool`

Operation:
- `searchEditorial({query, tags?})`

Searches JCR pages tagged via the existing content-tags module. Output
trimmed to title, id, summary, tags, URL. Capped at 10.

### 5.9 `ChatSessionStore`

`Caffeine` cache, `Map<SessionId, ConversationHistory>`. ~30-min idle
TTL, max ~10000 entries. RAM only — no JCR writes, no logs of message
bodies. `Ticker` is injectable so TTL behavior is unit-testable.

`ConversationHistory` is a list of immutable
`Turn { role, content, toolCalls?, toolResults? }` records, capped at
`cfg.historyTurnLimit` (oldest dropped first). When
`cfg.maxTokensPerSession` is exceeded (estimated by char/4 heuristic),
older turns are trimmed more aggressively and a one-time notice flag is
set on the next response.

### 5.10 `LanguageResolver` and `VisitorTraitsResolver`

`LanguageResolver` returns the active locale via:
1. `AggregationState.getLocale()` if non-null,
2. else parse `Accept-Language`,
3. else `en`.

Locales unsupported by the resource bundles fall back to the base
language (e.g., `de-AT` → `de`).

`VisitorTraitsResolver` reads from MTE personalization's
`info.magnolia.personalization.visitor.VisitorContext` if available.
Returns an empty map when the personalization module isn't installed
(so the bot still works in the community webapp). Output is a small
`Map<String, String>` with at most a handful of keys
(e.g., `{segment, country, returningVisitor}`).

### 5.11 `EnvLoader`

Tiny utility (one class, ~30 lines) called from `ChatbotModule.start()`
that reads a `.env` file from the working directory if present and
populates any missing keys into the process environment view consumed
by `ChatbotModule`. Does nothing if `.env` is absent. Never logs values.
Pulls from `io.github.cdimascio:dotenv-java` if dependency is acceptable;
otherwise hand-rolled.

### 5.12 `SessionRateLimiter`

`Caffeine`-backed sliding window per `MGNL_CHAT_SID`. Default
`cfg.rateLimitPerMinute = 30`. Over-limit returns HTTP 429 with a
localized friendly message.

### 5.13 Front-end

Vanilla JS (`chatbot.js`), no framework. Single entry point:

```js
MagnoliaChatbot.init({ mountSelector, mode: "widget" | "component" });
```

- Floating widget: a FreeMarker snippet
  (`chatbot-floating.ftl`) included from the site template's footer
  area injects a `<div id="mgnl-chatbot-floating">` and a `<script>` tag
  near `</body>`. The script mounts the chat panel inside the div, with
  a button that toggles open/closed.
- Placeable component: the `chatbot.ftl` template renders an inline
  `<div data-mgnl-chatbot data-mode="component">`. The script auto-mounts
  on DOMContentLoaded for any matching div on the page.

Both surfaces call `POST /.rest/chatbot/v1/turn`, share the same
`MGNL_CHAT_SID` cookie, and mirror the visible transcript into
`sessionStorage` keyed by sessionId so a refresh restores the rendered
history. CSS is scoped to `.mgnl-chatbot-*` class names.

Suggested-tour cards rendered below the assistant message link to the
tour's page URL using the URL returned by `ToursTool`.

## 6. Default system prompt template

```
You are a travel advisor for the Magnolia Travel demo site.

Always reply in {language}. If the visitor writes in a different language,
still reply in {language} unless they explicitly ask you to switch.

You only answer questions about travel, tours, and destinations available
on this site. If asked about anything else, politely redirect.

Use the available tools to ground your recommendations in real content.
Prefer one or two strong suggestions over long lists. When you reference
a tour or destination, include its title.

Visitor profile (for tailoring; do not mention these traits explicitly
unless asked): {visitorTraits}
```

Editors can override this entire string via the admin App. Placeholders
that don't resolve become empty strings.

## 7. Editor configuration

A new Magnolia App, **"Chatbot Configuration"**, registered under the
`Tools` group in AdminCentral. Built with `ConfiguredContentAppDescriptor`
against the `config` workspace, rooted at
`/modules/travel-demo-chatbot/config`. Single detail form (one config
node, no list view).

| Field | Widget | Notes |
|---|---|---|
| Model | text | |
| System prompt template | textarea (rich-text-disabled) | Multi-line; help text documents `${language}` and `${visitorTraits}` |
| Enabled tools | multi-select checkboxes | `tours`, `destinations`, `editorial` |
| Max tool iterations | number | |
| History turn limit | number | |
| Request timeout (ms) | number | |
| Rate limit / minute | number | |
| Max tokens / session | number | |
| Max user message chars | number | |
| API key status | read-only badge | Shows ✅ "Detected" or ⚠️ "Missing" — value never displayed |

Saves are picked up by `ChatbotModule` on the next request via Magnolia's
standard `Node2BeanProcessor` reload. No restart required.

## 8. i18n / multilingual support

Two layers:

1. **Bot UI strings** — `chatbot_<locale>.properties` resolved through
   Magnolia's `MessagesManager`. Includes button labels, placeholder text,
   thinking indicator, all error-message strings. Ships with `en`, `de`,
   `fr`. The component template emits the locale into a `data-locale`
   attribute so JS can use it for date / number formatting.
2. **Bot answers** — `LanguageResolver` injects the active locale into
   `${language}` in the system prompt template. The default prompt
   instructs the model to respond in that locale. Tool results are
   returned as-is (Magnolia's locale fallback chain handles missing
   translations of source content); the model weaves them into the
   target language.

Edge case: when content for the active locale isn't authored in JCR,
tools return whatever the standard Magnolia fallback returns. The bot
will describe it in the resolved locale even if the source data is in
another. Acceptable for a demo.

## 9. Error handling & guardrails

| Failure | Server response | User sees (localized) |
|---|---|---|
| `GEMINI_API_KEY` missing | 503 `LLM_UNCONFIGURED` | "The travel advisor is not configured. Please contact the site administrator." |
| Gemini 4xx (non-rate-limit) | 502 `LLM_BAD_REQUEST`, history unchanged | "Something went wrong. Try again." |
| Gemini 429 / 5xx after retries | 502 `LLM_UNAVAILABLE` | "The travel advisor is busy. Please try again in a moment." |
| Request timeout exceeded | 504 `LLM_TIMEOUT`, history unchanged | Same as above. |
| Tool throws | Tool error returned to model as `functionResponse`; turn proceeds | Whatever the model says — usually a graceful fallback |
| `maxToolIterations` exceeded | Loop stops, returns last text + appended notice | "(I couldn't fully resolve that — try rephrasing.)" |
| Session cookie absent | New session created | Bot greets as if new |
| `userMessage` length > `maxUserMessageChars` | 413 | "Message too long — please shorten it." |
| Rate limit exceeded | 429 | "Please wait a moment before sending another message." |

Guardrails enforced by code (not just the prompt):

- Server-side per-session sliding-window rate limit
  (`SessionRateLimiter`).
- Soft per-session token budget; older turns trimmed when exceeded;
  one-time notice flagged on the response.
- Topic scoping baked into the default system prompt
  (editor-overridable).
- No PII storage: RAM-only session store with TTL eviction; no JCR
  writes; logs never include message bodies.
- CSRF: standard Magnolia REST CSRF token required.
- Tool argument validation against each tool's JSON Schema before any
  JCR access.

## 10. Logging

Per CLAUDE.md L-1, L-3, L-7:

- One INFO log per turn:
  `{sessionIdHash, language, toolCount, latencyMs, modelTokensIn?, modelTokensOut?, status}`.
  `sessionIdHash` is a SHA-256 prefix of the cookie value, never the raw
  cookie.
- WARN on tool failures: `{toolName, errorClass}`.
- ERROR on LLM failures: `{statusCode, errorClass}`.
- Never logged: full user messages, full assistant messages, API keys,
  raw cookie values.
- All logs go through `createLogger`-equivalent for the Magnolia / Java
  logging stack already in use by sibling modules
  (Magnolia uses SLF4J via Logback in this project; CLAUDE.md's
  `@sentinel/common` helpers do not apply here, but the spirit —
  structured fields, no `System.out`, no PII — does).

## 11. Testing strategy

### 11.1 Unit tests (colocated under `src/test/java/...`)

| Class | Notable cases |
|---|---|
| `LanguageResolver` | AggregationState locale; `Accept-Language` fallback; `en` default; `de-AT` → `de` |
| `VisitorTraitsResolver` | empty map when personalization absent; trait map when present; never throws on missing `VisitorContext` |
| `ChatSessionStore` | new sessionId → empty history; appended turns retrievable; TTL eviction (using injected `Ticker`); `historyTurnLimit` enforced — oldest dropped first; token-budget trim path |
| `GeminiToolAdapter` | round-trip a `Tool` to Gemini `function_declarations`; parse `functionCall` parts back; handle parallel function calls |
| `GeminiClient` | retries 429/503 with capped backoff; gives up after 3; `LlmException` carries status code; honors `requestTimeoutMs`; sends `x-goog-api-key` |
| `ToursTool` / `DestinationsTool` / `EditorialTool` | argument validation rejects out-of-range/unknown enum values with structured tool errors; result trimming caps at 10; output JSON shape stable (snapshot) |
| `ChatEndpoint` (collaborators mocked) | happy path; tool loop iterates and stops at `maxToolIterations`; LLM failure leaves history unchanged; rate-limit returns 429; oversized message returns 413; sets `MGNL_CHAT_SID` when absent |
| `SessionRateLimiter` | within limit; over limit; window slides correctly with injected `Ticker` |
| `ChatbotModuleVersionHandler` | bootstrap installs config node, REST endpoint registration, App registration, default tool list |

Tests follow CLAUDE.md TDD discipline: failing test before
implementation; one assertion when possible; `expect.any(...)`-style
matchers for variable IDs; parameterized inputs over magic literals.

### 11.2 Integration test

`src/test/java/.../integration/ChatbotIntegrationIT.java`:

- Boots an in-memory Magnolia test context using the existing demo
  module pattern (extending `info.magnolia.test.MgnlTestCase` or the
  modules' existing test base).
- Stubs the Gemini HTTP endpoint with a small WireMock server scripted
  to: respond with a `searchTours` function call, then with a final
  text reply once the tool result is supplied.
- Drives `ChatEndpoint` with a real `ToursTool` over a seeded JCR
  `website` workspace containing one tour node.
- Asserts: final assistant message reaches the client; the tour
  appears in `suggestedTours`; conversation history is persisted in
  `ChatSessionStore`; one INFO turn-log line was emitted.

CI runs offline; no real Gemini key required.

### 11.3 Demo-rehearsal checklist (manual, not CI)

1. Set `GEMINI_API_KEY` in `.env`; start `travel-demo-webapp` locally.
2. Visit a destination page; floating widget appears; ask "Where should
   I go in November under $2k?" — verify a tour-grounded reply.
3. Switch the site to German; reload; verify the bot's response
   language follows.
4. In the Chatbot Configuration app, disable the `editorial` tool;
   send a query that would have used editorial; verify it isn't called
   (check logs).
5. Drop a "Chatbot" component into the page editor on a content page;
   verify the in-page surface works and shares state with the floating
   widget on the same page.
6. Send 31 messages within a minute; verify the rate-limit message
   appears.

### 11.4 Out of test scope

- The actual quality of Gemini's responses (non-deterministic).
- Visual regression on the chat UI (manual rehearsal only).
- Cross-browser front-end matrix (latest evergreen browsers only).

## 12. Open items deferred to implementation plan

- Exact dependency coordinates for `Caffeine`, `dotenv-java` (or
  hand-rolled `EnvLoader`), and any Gemini-specific JSON helpers.
- Exact YAML for the App registration (consult an existing demo App
  like `magnolia-travel-tours-app` for the closest pattern).
- Whether the JCR search in `DestinationsTool` / `EditorialTool`
  reuses the existing `magnolia-travel-demo-graphql` query layer or
  hits JCR directly. Pick whichever is shorter/clearer when reading
  the existing modules.
