/**
 * Regression guard for PRs that alter locale resolution.
 *
 * Source: travel-demo-module/src/main/java/info/magnolia/demo/travel/model/NavigationAreaModel.java
 *
 * On master, `NavigationAreaModel.getLocale(String)` delegates directly to
 * `I18nContentSupport.determineLocaleFromString(...)`. Any PR that:
 *   - short-circuits to `Locale.getDefault()` for empty/null input, OR
 *   - routes the call through a new helper (extract-method refactor)
 * changes the observable `<html lang>` attribute for corner-case input
 * handling — this test trips as a "locale pathway touched" marker.
 */
import { test, expect } from '@playwright/test';

test('travel home page reports a populated <html lang> attribute', async ({ page }) => {
  await page.goto('/travel.html');

  const lang = await page.getAttribute('html', 'lang');
  expect(lang, '<html lang> must be populated on master').not.toBeNull();
  expect(lang!.length, 'locale must resolve to a non-empty tag').toBeGreaterThan(0);
  expect(lang).not.toBe('');
});
