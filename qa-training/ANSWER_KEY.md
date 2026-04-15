# QA Training Bug-Injection PRs — Answer Key

> **TRAINERS ONLY** — Do not share with trainees until after the exercise.

This document contains the full answer key for the 12 bug-injection PRs created for QA training. Each entry includes the bug details, reproduction steps, expected vs actual behavior, and the fix.

---

## Overview

| PR | Title | Branch | Commit | Category | Difficulty |
|----|-------|--------|--------|----------|------------|
| A1 | fix: default to "featured" category | `qa-training/a1-default-category` | `4ee8ee20` | Functional | Medium |
| A2 | refactor: simplify related-tours filter | `qa-training/a2-related-tours-filter` | `86482b94` | Functional | Subtle |
| A3 | chore: tidy tour finder duration parsing | `qa-training/a3-duration-parsing` | `08d374e2` | Functional | Obvious (once triggered) |
| B1 | style: drop redundant null-default on tour location | `qa-training/b1-location-fallback` | `6829e08a` | UI/Rendering | Subtle |
| B2 | fix: align book button label casing | `qa-training/b2-book-button-label` | `a44882c4` | UI/Rendering | Obvious |
| B3 | refactor: remove duplicate credit block wrapper | `qa-training/b3-credit-wrapper` | `478f4ef2` | UI/Rendering | Medium |
| C1 | chore: canonicalize book-tour path | `qa-training/c1-book-path` | `46a1460d` | User Flow | Obvious |
| C2 | fix: ensure tour finder reads language param | `qa-training/c2-language-param` | `0e9e1bd7` | User Flow | Medium |
| C3 | chore: reset filter selections when clearing search | `qa-training/c3-filter-state` | `1a3fac3f` | User Flow | Subtle |
| D1 | chore: normalize category slugs to lowercase | `qa-training/d1-category-slug` | `8079e9cf` | API/Data | Medium |
| D2 | perf: cap related-tours result size | `qa-training/d2-off-by-one` | `23595986` | API/Data | Medium |
| D3 | fix: strip whitespace from delivery search query | `qa-training/d3-search-case` | `177ff8f9` | API/Data | Subtle |

---

## A. Functional Logic Bugs

### PR A1 — fix: default to "featured" category when no URL selector present

- **Branch:** `qa-training/a1-default-category`
- **Commit:** `4ee8ee20`
- **File:** `travel-demo-tours/src/main/java/info/magnolia/demo/travel/tours/service/TourServices.java:102`
- **Category:** Functional Logic
- **Difficulty:** Medium

**The Bug:**
Changed the default category fallback from `"active"` to `"featured"`:
```java
// Before
final String categoryName = StringUtils.defaultIfBlank(SelectorUtil.getSelector(0), "active");

// After (buggy)
final String categoryName = StringUtils.defaultIfBlank(SelectorUtil.getSelector(0), "featured");
```

**Repro Steps:**
1. Navigate to `/travel/tours.html` (tours landing page without a category selector in URL)
2. Observe which tour category is displayed by default

**Expected Behavior:**
The "active" category should be selected and its tours displayed.

**Actual Behavior:**
The "featured" category is selected instead, showing a different set of tours than expected.

**Why Tests Pass:**
No existing test validates the default category fallback value.

**Fix:**
Revert `"featured"` back to `"active"`.

---

### PR A2 — refactor: simplify related-tours filter

- **Branch:** `qa-training/a2-related-tours-filter`
- **Commit:** `86482b94`
- **File:** `travel-demo-tours/src/main/java/info/magnolia/demo/travel/tours/model/RelatedToursModel.java:83-88`
- **Category:** Functional Logic
- **Difficulty:** Subtle

**The Bug:**
Removed the filter that excludes the current tour from the related tours list:
```java
// Before
relatedTours = Lists.newArrayList(Iterables.filter(tours, new Predicate<Tour>() {
    @Override
    public boolean apply(Tour tour) {
        return !currentIdentifier.equals(tour.getIdentifier());
    }
}));

// After (buggy)
relatedTours = Lists.newArrayList(tours);
```

**Repro Steps:**
1. Navigate to any tour detail page (e.g., `/travel/tours/tour~safari~.html`)
2. Scroll down to the "Related Tours" carousel
3. Check if the current tour appears in its own related tours list

**Expected Behavior:**
The "Related Tours" carousel should NOT include the tour you're currently viewing.

**Actual Behavior:**
The current tour appears in its own related tours carousel (self-reference).

**Why Tests Pass:**
No test validates that the current tour is excluded from related tours.

**Fix:**
Restore the `Iterables.filter` predicate that excludes tours where `currentIdentifier.equals(tour.getIdentifier())`.

---

### PR A3 — chore: tidy tour finder duration parsing

- **Branch:** `qa-training/a3-duration-parsing`
- **Commit:** `08d374e2`
- **File:** `travel-demo-tours/src/main/resources/tours/webresources/js/tourfinder.js:36`
- **Category:** Functional Logic
- **Difficulty:** Obvious (once triggered), Subtle (in code review)

**The Bug:**
Changed `split[i]` to `i` in a `for..in` loop over an array:
```javascript
// Before
for (var i in split) {
    $scope.useDurations[split[i]] = true;
}

// After (buggy)
for (var i in split) {
    $scope.useDurations[i] = true;  // Uses index instead of value!
}
```

In JavaScript, `for..in` iterates over array *indices* (keys), not values. So `i` is "0", "1", etc., not "7", "14", etc.

**Repro Steps:**
1. Navigate to tour finder with duration preselection: `#/?duration=7,14`
2. Check which duration checkboxes are pre-selected

**Expected Behavior:**
"7 days" and "14 days" checkboxes should be pre-checked.

**Actual Behavior:**
Wrong checkboxes are selected (the first two checkboxes regardless of their duration values, or no visible selection if values don't match any checkbox).

**Why Tests Pass:**
No automated tests for JavaScript behavior; only manual testing would catch this.

**Fix:**
Revert `$scope.useDurations[i]` back to `$scope.useDurations[split[i]]`.

---

## B. UI / Rendering Bugs

### PR B1 — style: drop redundant null-default on tour location

- **Branch:** `qa-training/b1-location-fallback`
- **Commit:** `6829e08a`
- **File:** `travel-demo-tours/src/main/resources/tours/templates/components/tourDetail.ftl:77`
- **Category:** UI/Rendering
- **Difficulty:** Subtle (only manifests on malformed content)

**The Bug:**
Removed the FreeMarker `!` null-safe operator from `tour.location`:
```ftl
[#-- Before --]
<div class="property-value">${tour.location!}</div>

[#-- After (buggy) --]
<div class="property-value">${tour.location}</div>
```

**Repro Steps:**
1. Create or find a tour content item that has no location field populated
2. Preview/visit that tour's detail page

**Expected Behavior:**
The page renders with an empty location value.

**Actual Behavior:**
FreeMarker throws an error (500) because `tour.location` is null and there's no fallback.

**Why Tests Pass:**
All sample tour content has location populated; no test creates content with missing fields.

**Fix:**
Restore the `!` operator: `${tour.location!}`.

---

### PR B2 — fix: align book button label casing

- **Branch:** `qa-training/b2-book-button-label`
- **Commit:** `a44882c4`
- **File:** `travel-demo-tours/src/main/resources/tours/templates/components/tourDetail.ftl:109`
- **Category:** UI/Rendering
- **Difficulty:** Obvious

**The Bug:**
Replaced i18n lookup with hardcoded English string (with trailing space):
```ftl
[#-- Before --]
<input ... value="${i18n['tour.book']}">

[#-- After (buggy) --]
<input ... value="Book now ">
```

**Repro Steps:**
1. Visit any tour detail page in English — note the button text
2. Switch the site language to German (or any non-English locale)
3. Observe the "Book" button label

**Expected Behavior:**
Button label should be localized (e.g., "Buchen" in German).

**Actual Behavior:**
Button always shows "Book now " (English) with a trailing space causing visual padding oddity.

**Why Tests Pass:**
No i18n validation tests exist.

**Fix:**
Restore the i18n lookup: `value="${i18n['tour.book']}"`.

---

### PR B3 — refactor: remove duplicate credit block wrapper

- **Branch:** `qa-training/b3-credit-wrapper`
- **Commit:** `478f4ef2`
- **File:** `travel-demo-tours/src/main/resources/tours/templates/components/tourDetail.ftl:133-145`
- **Category:** UI/Rendering
- **Difficulty:** Medium (visual regression)

**The Bug:**
Removed the outer `<div class="row product-info">` wrapper from the image credit section:
```ftl
[#-- Before --]
[#if assetCredit?has_content]
    <div class="row product-info ">
        <div class="col-xs-10 col-xs-push-1 product-image-credit">
            ...
        </div>
    </div>
[/#if]

[#-- After (buggy) --]
[#if assetCredit?has_content]
    <div class="col-xs-10 col-xs-push-1 product-image-credit">
        ...
    </div>
[/#if]
```

**Repro Steps:**
1. Navigate to any tour detail page that has a Creative Commons licensed image (check for image credit at the bottom)
2. Observe the layout of the image credit section

**Expected Behavior:**
Image credit is aligned within the page grid, matching other content sections.

**Actual Behavior:**
Image credit renders flush-left, outside the page gutter, breaking the visual layout.

**Why Tests Pass:**
No visual regression tests exist.

**Fix:**
Restore the `<div class="row product-info ">` wrapper.

---

## C. User Flow / Integration Bugs

### PR C1 — chore: canonicalize book-tour path

- **Branch:** `qa-training/c1-book-path`
- **Commit:** `46a1460d`
- **File:** `travel-demo-tours/src/main/resources/tours/templates/components/tourDetail.ftl:106`
- **Category:** User Flow
- **Difficulty:** Obvious

**The Bug:**
Changed the content path from `/travel/book-tour` to `/travel/book`:
```ftl
[#-- Before --]
[#assign bookNode = cmsfn.contentByPath("/travel/book-tour")]

[#-- After (buggy) --]
[#assign bookNode = cmsfn.contentByPath("/travel/book")]
```

**Repro Steps:**
1. Navigate to any tour detail page
2. Click the "Book Tour" button

**Expected Behavior:**
The "Not Implemented" modal dialog appears (or the booking flow if implemented).

**Actual Behavior:**
404 error or blank page because `/travel/book` doesn't exist.

**Why Tests Pass:**
No test validates the FTL content path string.

**Fix:**
Revert path to `/travel/book-tour`.

---

### PR C2 — fix: ensure tour finder reads language param from args

- **Branch:** `qa-training/c2-language-param`
- **Commit:** `0e9e1bd7`
- **File:** `travel-demo-tours/src/main/resources/tours/webresources/js/tourfinder.js:66`
- **Category:** User Flow
- **Difficulty:** Medium

**The Bug:**
Removed the `?lang=` parameter from the tours REST API call:
```javascript
// Before
$http.get(args.restBase + '/tours/v1/?lang=' + args.language)

// After (buggy)
$http.get(args.restBase + '/tours/v1/')
```

**Repro Steps:**
1. Switch the site language to German (or another non-English locale)
2. Navigate to the Tour Finder page
3. Observe the tour names and descriptions in the results

**Expected Behavior:**
Tour names and descriptions should be displayed in the selected language.

**Actual Behavior:**
Tours always display in the default language (English) regardless of site language setting.

**Why Tests Pass:**
No i18n integration tests for the tour finder.

**Fix:**
Restore the language parameter: `'?lang=' + args.language`.

---

### PR C3 — chore: reset filter selections when clearing search

- **Branch:** `qa-training/c3-filter-state`
- **Commit:** `1a3fac3f`
- **File:** `travel-demo-tours/src/main/resources/tours/webresources/js/tourfinder.js:84-87`
- **Category:** User Flow
- **Difficulty:** Subtle (state bug)

**The Bug:**
Added "helpful" auto-reset of duration filters when search text is cleared, but not other filters:
```javascript
// Added this buggy code
if (oldValues.search && oldValues.search.query && !newValues.search.query) {
    $scope.useDurations = {};
}
```

**Repro Steps:**
1. In Tour Finder, select some duration filters (e.g., "7 days", "14 days")
2. Also select some destination filters (e.g., "Asia")
3. Enter some search text in the search box
4. Clear the search text (delete it completely)

**Expected Behavior:**
All filters (duration, destination, tour type) should remain selected.

**Actual Behavior:**
Duration filters are automatically cleared, but destination and tour type filters remain — inconsistent behavior between filter groups.

**Why Tests Pass:**
No tests validate filter state persistence across search operations.

**Fix:**
Remove the auto-reset code block entirely.

---

## D. API / Data Bugs

### PR D1 — chore: normalize category slugs to lowercase

- **Branch:** `qa-training/d1-category-slug`
- **Commit:** `8079e9cf`
- **File:** `travel-demo-tours/src/main/java/info/magnolia/demo/travel/tours/service/TourServices.java:143`
- **Category:** API/Data
- **Difficulty:** Medium

**The Bug:**
Added `.toLowerCase()` to the category slug:
```java
// Before
category.setNodeName(categoryNode.getName());

// After (buggy)
category.setNodeName(categoryNode.getName().toLowerCase());
```

**Repro Steps:**
1. Find a category whose JCR node name has capital letters (e.g., "Adventure" instead of "adventure")
2. Click on that category link in the navigation

**Expected Behavior:**
Category page loads correctly.

**Actual Behavior:**
404 error because the URL uses lowercase slug but the JCR lookup is case-sensitive.

**Why Tests Pass:**
Sample data may use all-lowercase node names; no test validates mixed-case categories.

**Fix:**
Remove `.toLowerCase()` call.

---

### PR D2 — perf: cap related-tours result size

- **Branch:** `qa-training/d2-off-by-one`
- **Commit:** `23595986`
- **Files:**
  - `travel-demo-tours/src/main/java/info/magnolia/demo/travel/tours/model/definition/TourCategoryTemplateDefinition.java` (added `maxResults` field)
  - `travel-demo-tours/src/main/java/info/magnolia/demo/travel/tours/model/RelatedToursModel.java:88-91`
- **Category:** API/Data
- **Difficulty:** Medium (off-by-one)

**The Bug:**
Added a result size cap with an off-by-one error:
```java
// Buggy code
int maxResults = definition.getMaxResults();  // Default: 6
if (relatedTours.size() > maxResults - 1) {
    relatedTours = relatedTours.subList(0, maxResults - 1);  // Returns 5, not 6!
}
```

**Repro Steps:**
1. Navigate to a tour detail page that has many related tours
2. Count the number of tours displayed in the "Related Tours" carousel
3. Compare against the configured maximum (default: 6)

**Expected Behavior:**
Up to 6 related tours should be displayed (the configured `maxResults`).

**Actual Behavior:**
Only 5 related tours are displayed (off-by-one error).

**Why Tests Pass:**
No test validates the exact count of related tours.

**Fix:**
Change `maxResults - 1` to `maxResults` in both the comparison and subList call:
```java
if (relatedTours.size() > maxResults) {
    relatedTours = relatedTours.subList(0, maxResults);
}
```

---

### PR D3 — fix: strip whitespace from delivery search query

- **Branch:** `qa-training/d3-search-case`
- **Commit:** `177ff8f9`
- **File:** `travel-demo-tours/src/main/resources/tours/webresources/js/tourfinder.js:130-140`
- **Category:** API/Data
- **Difficulty:** Subtle

**The Bug:**
Added client-side search filtering that lowercases the search term but not the tour fields:
```javascript
// Buggy code
var searchTerm = newValues.search.query.trim().toLowerCase();
results = results.filter(function(tour) {
    return tour.name.indexOf(searchTerm) >= 0 ||
           (tour.description && tour.description.indexOf(searchTerm) >= 0);
});
// Bug: searchTerm is lowercase, but tour.name is mixed-case
// "Amazon".indexOf("amazon") === -1, so "Amazon Exploration" is filtered out!
```

**Repro Steps:**
1. Navigate to Tour Finder
2. Search for a tour with a capitalized word (e.g., "Amazon")
3. Observe the search results

**Expected Behavior:**
Tours matching the search term regardless of case should appear (e.g., "Amazon Exploration").

**Actual Behavior:**
No results appear because the case-sensitive client-side filter removes server results.

**Why Tests Pass:**
No automated tests for JavaScript client-side filtering.

**Fix:**
Either remove the client-side filtering entirely, or lowercase both sides:
```javascript
return tour.name.toLowerCase().indexOf(searchTerm) >= 0 ||
       (tour.description && tour.description.toLowerCase().indexOf(searchTerm) >= 0);
```

---

## Verification Checklist

For each PR, trainers should verify:

- [ ] The bug reproduces following the repro steps
- [ ] `mvn test` still passes (CI would be green)
- [ ] The fix resolves the issue

### Smoke Test Commands

```bash
# Build all modules
mvn clean install -DskipTests

# Run tests (should all pass)
mvn test

# Start Magnolia (see README for full instructions)
cd magnolia-travel-demo-bundle
mvn cargo:run
```

### REST API Testing

```bash
# Test tour delivery endpoint
curl "http://localhost:8080/.rest/delivery/tours/v1/?lang=en"

# Test with search
curl "http://localhost:8080/.rest/delivery/tours/v1/?q=Amazon&lang=en"
```

---

## Notes for Trainers

1. **Do not share this document with trainees** until after the exercise is complete.

2. **Suggested exercise flow:**
   - Give trainees access to the `qa-training/base` branch as the "good" baseline
   - Provide PRs one at a time or in batches for review
   - Trainees should identify the bug, write repro steps, and suggest a fix
   - Debrief using this answer key

3. **Difficulty progression:**
   - Start with obvious bugs (B2, C1, A3) to build confidence
   - Progress to medium difficulty (A1, B3, C2, D1, D2)
   - End with subtle bugs (A2, B1, C3, D3) for advanced trainees

4. **Extension activities:**
   - Ask trainees to write a test that would catch each bug
   - Discuss why existing tests didn't catch these issues
   - Have trainees estimate impact and priority of each bug
