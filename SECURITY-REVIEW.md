# Security & Bug Review — Magnolia Travel Demo

**Date:** 2026-04-09
**Scope:** All custom Java code (56 files), FreeMarker templates (40+), YAML/XML configs, GraphQL/REST API layer

---

## Critical (3)

| # | Location | Issue |
|---|----------|-------|
| **C1** | `TourServices.java:320` | **JCR-SQL2 Injection** — `getContentNodeByName` interpolates user input (`?tour=` param, URL selectors) directly into a SQL2 query via `String.format`. An attacker can inject arbitrary JCR predicates (e.g., `?tour=' OR 1=1 OR name(content)='`). |
| **C2** | `graphql-tour-queries-provider.js:53-63` | **JCR Query Injection (client-side)** — `buildFTSQuery` interpolates search box input into LIKE clauses without sanitization. Attacker types a crafted string in the search box to alter query logic. |
| **C3** | `storyDisplayArea.ftl:42,85` | **Stored XSS via embedsource** — `${story.embedsource!}` is rendered raw into HTML *and* injected into a JS `innerHTML` call. Any CMS editor can inject arbitrary `<script>` tags. The JS string context (`'${story.embedsource!}'`) also breaks on single quotes. |

---

## High (8)

| # | Location | Issue |
|---|----------|-------|
| **H1** | `graphql-tour-queries-provider.js:41-51` | **Filter injection** — `@duration` values inserted raw without numeric validation; other filter values trivially escape single-quote wrapping. |
| **H2** | `TourServices.java:361` | **Second JCR-SQL2 injection** — `getToursByCategory` interpolates `categoryPropertyName` and `identifier` into a LIKE clause. |
| **H3** | `storyDisplayArea.ftl:28` | **Path traversal** — `state.selector` from URL concatenated into JCR path (`getStoriesFolder() + "/" + selector`). `../` sequences could traverse the content tree. |
| **H4** | `carousel.ftl:15,17` | **XSS in JS context** — `slideShowId` and `slickConfig` CMS fields rendered raw into JavaScript without `?js_string` escaping. |
| **H5** | `htmlHeader.ftl:9,12,13` | **XSS in meta tags** — `windowTitle`, `description`, `keywords` rendered without `?html` in `<title>` and `content=""` attributes. `</title><script>...` escapes the tag. |
| **H6** | `htmlHeader-marketing-tags.ftl:76-106` | **XSS in cookie consent JS** — 10+ CMS properties injected into a JS object literal without `?js_string` escaping. |
| **H7** | `users.admin.*.yaml` | **Password hashes in VCS** — Bcrypt hashes for 4 admin users (eric, peter, tina) committed to source. Demo passwords are likely weak/guessable. |
| **H8** | `tour-service.js:123` | **Constructor bug** — `new GraphqlTourQueriesProvider()` instantiated without required `(httpClient, baseContext)` args. Works by accident today but fragile. |

---

## Medium (14)

| # | Location | Issue |
|---|----------|-------|
| **M1** | `DefaultUserLinksResolver.java:86-101` | **NPE chain** — `getLoginPageLink`, `getLogoutLink`, `getProfilePageLink`, `getRegistrationPageLink` all pass `findPage()` result to `templatingFunctions.link()` without null checks. Produces `null?mgnlLogout=true` URLs or throws NPE. |
| **M2** | `tourFinder.ftl:28-33` (both modules) | **XSS in i18n JS** — i18n values and `ctx.contextPath` rendered into JS strings without `?js_string`. |
| **M3** | `functions.ftl:20,50` | **JCR injection in FTL** — SQL2 queries built via string interpolation with `${storiesPage.@path}` and `${blockType}`. Current call-sites safe but the pattern is dangerous. |
| **M4** | `tour-service.js:72-83` | **REST param injection** — URL query params built without `encodeURIComponent`. Attacker can inject `&`/`=` to alter query semantics. |
| **M5** | `tour-data-mapper.js:40-53` | **TypeError risk** — `image.renditions.reduce()` and `tourTypes.map()` throw if properties are null/undefined. |
| **M6** | `tourDetail.ftl:36,116` + others | **Missing `!` defaults** — `${tour.description}`, `${tour.name}`, `${story.title}` without FreeMarker `!` operator throw on null. |
| **M7** | `travel.yaml:36` | **Wildcard CORS** — `all: '*'` allows any origin to make cross-origin API requests. |
| **M8** | `AddMixinTask.java:49-54` | **Silent task failure** — Catches `RepositoryException` and logs error but returns success. Version handler reports the delta as complete even if the mixin was not applied. Same in `RemoveMixinTask`. |
| **M9** | `tours-enterprise.xml` | **No GraphQL depth/complexity limits** — No query depth, complexity, or cost configuration. Susceptible to DoS via deeply nested queries. |
| **M10** | `tourfinder.js:5` | **AngularJS EOL** — Angular 1.x reached EOL Dec 2021. Known unpatched sandbox escapes and prototype pollution. |
| **M11** | `travel-demo-theme.yaml:38` | **jQuery 1.10.2** — Multiple known XSS CVEs (CVE-2015-9251, CVE-2019-11358, CVE-2020-11022/11023). |
| **M12** | `pom.xml:22-23` | **SNAPSHOT dependencies** — `magnolia.version=6.3-SNAPSHOT` and `mte.version=3.0-SNAPSHOT`. Mutable artifacts = non-reproducible builds and supply chain risk. |
| **M13** | `travel-demo-multisite/pom.xml:50-66` | **Test deps in compile scope** — JUnit, Mockito, Hamcrest declared without `<scope>test</scope>`. Bundled into production WAR. Same in component-personalization and personalization modules. |
| **M14** | No `web.xml` in either webapp | **Missing security descriptors** — No custom error pages (leak stack traces), no session cookie hardening (`HttpOnly`/`Secure`/`SameSite`), no security headers. |

---

## Low (10)

| # | Location | Issue |
|---|----------|-------|
| **L1** | `tourList-content-tags.ftl:33` | Stray `}"` in HTML tag (visible rendering bug) |
| **L2** | `detectCookie.ftl:2` | Invalid FreeMarker comment `[!--` leaked to HTML output |
| **L3** | `searchResults.ftl:13,29` | Mismatched `</span>` without opening `<span>` |
| **L4** | `DefaultUserLinksResolver.java:56` | `enabled` field has getter/setter but is never read |
| **L5** | `SetPageAsPublishedTask.java:124` | Hardcoded `"superuser"` falsifies audit trail |
| **L6** | `FolderBootstrapTask.java:60` | `startsWith(folderName)` prefix match could hit unintended resources |
| **L7** | `tour-service.js:99-104` | `language` param silently ignored in GraphQL service (functional bug for multilingual) |
| **L8** | `*.graphql` files served as static resources | Schema structure exposed even if introspection is disabled |
| **L9** | `htmlHeader.ftl` | `html5shiv.googlecode.com` (defunct) loaded via protocol-relative URL |
| **L10** | `.gitignore` | Doesn't exclude `*.env`, `*.pem`, `*.key`, `*.jks` patterns |

---

## Recommendations

### 1. Parameterize JCR queries (addresses C1, C2, H1, H2, M3)

`TourServices.java:320` and `:361` are directly exploitable from HTTP requests. Use `javax.jcr.query.Query` bind variables or at minimum escape single quotes and validate input format. Apply the same discipline in FreeMarker templates that build SQL2 queries via string interpolation.

### 2. Escape all FreeMarker output (addresses C3, H4, H5, H6, M2, M6)

Add `?html` to every plain-text `${...}` in HTML context. Add `?js_string` to every `${...}` inside `<script>` blocks. This is the single highest-leverage change — it addresses 6 findings across both security and bug categories in one systematic pass.

### 3. Sanitize user input from URL selectors (addresses H3, M4)

`SelectorUtil.getSelector()` and `MgnlContext.getParameter()` values are used in paths and queries throughout `TourServices` and templates. Validate against an allowlist or at minimum strip path traversal sequences (`..`). URL-encode values before using them in query string construction.

### 4. Upgrade client-side libraries (addresses M10, M11, L9)

jQuery 1.10.2 and AngularJS 1.x have known, unpatched CVEs. Upgrade to jQuery 3.7+ and migrate off AngularJS. Remove the defunct `html5shiv.googlecode.com` script reference.

### 5. Add webapp security hardening (addresses M14, M7)

Create `web.xml` files with custom error pages, session cookie flags (`HttpOnly`, `Secure`, `SameSite=Lax`), and a security headers filter (`X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`, `Strict-Transport-Security`). Restrict CORS origins in `travel.yaml` to specific trusted domains instead of `*`.

### 6. Remove secrets from version control (addresses H7)

Remove bcrypt password hashes from the `users.admin.*.yaml` bootstrap files. Use environment-specific configuration or Magnolia's built-in user management to provision demo accounts at deploy time rather than baking credentials into source control.

### 7. Fix dependency scoping (addresses M12, M13)

Add `<scope>test</scope>` to JUnit, Mockito, and Hamcrest dependencies in `travel-demo-multisite`, `travel-demo-component-personalization`, and `travel-demo-personalization` modules. Ensure SNAPSHOT dependencies are resolved to release versions before any production deployment.

### 8. Add GraphQL query limits (addresses M9)

Configure query depth limits, complexity limits, and rate limiting for the GraphQL endpoint. This can be done via the `graphql-core` module configuration or by adding a custom `Instrumentation` that enforces max query depth and cost.

### 9. Fix silent setup task failures (addresses M8)

In `AddMixinTask` and `RemoveMixinTask`, re-throw `RepositoryException` as `TaskExecutionException` instead of swallowing it. This ensures the version handler accurately reports whether all install/update tasks succeeded.

### 10. Fix null-safety in link resolution (addresses M1)

Add null checks on `findPage()` return values in `DefaultUserLinksResolver` before passing to `templatingFunctions.link()`. Return a sensible fallback (e.g., `"#"`) or log a warning when a configured page template cannot be found.
