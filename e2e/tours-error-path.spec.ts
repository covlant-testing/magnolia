/**
 * Regression guard for PR `hotfix/locale-null-safe`.
 *
 * Handler: info.magnolia.virtualuri.mapping.RegexpVirtualUriMapping
 *          (travel-demo-tours/src/main/resources/tours/virtualUriMappings/travelToursMapping.yaml)
 *
 * Master's fromUri is `^/(?!\.\w+/)(.*/)?tours/(.*).html`, so
 * `/_error/tours/paris.html` is accepted and forwarded to
 * `/travel/tour?tour=/paris`.
 *
 * The hotfix PR adds `_error/` to the negative lookahead
 * (`^/(?!\.\w+/|_error/)...`). After that lands, the URL no longer matches
 * the regex and no forward happens — so the `waitForURL` below never fires
 * and the test fails.
 */
import { test, expect } from '@playwright/test';

test('legacy /_error/tours/{slug}.html still forwards to /travel/tour', async ({ page }) => {
  const response = await page.goto('/_error/tours/paris.html');
  expect(response, 'navigation response must exist').not.toBeNull();

  await page.waitForURL(/\/travel\/tour\?tour=\/paris\b/);
  expect(page.url()).toContain('/travel/tour?tour=/paris');
});
