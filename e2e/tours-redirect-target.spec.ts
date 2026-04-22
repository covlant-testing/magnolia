/**
 * Optional regression guard — tripwire if the tours toUri rename ever ships.
 *
 * Handler: info.magnolia.virtualuri.mapping.RegexpVirtualUriMapping
 *          (travel-demo-tours/src/main/resources/tours/virtualUriMappings/travelToursMapping.yaml)
 *
 * Master's toUri is `forward:/$1travel/tour?tour=/$2`; this test asserts the
 * redirect target URL contains `/travel/tour?`. If a refactor PR renames
 * that target to `/travel/tour-page?`, this test fails.
 */
import { test, expect } from '@playwright/test';

test('/tours/{slug}.html forwards to /travel/tour (master forward target)', async ({ page }) => {
  await page.goto('/tours/rome.html');

  await page.waitForURL(/\/travel\/tour\?tour=\/rome\b/);
  expect(page.url()).toContain('/travel/tour?tour=/rome');
  expect(page.url()).not.toContain('/travel/tour-page');
});
