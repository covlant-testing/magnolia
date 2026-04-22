/**
 * Regression guard for PR `feature/tour-api-endpoint` — home page layout.
 *
 * Source: travel-demo-module/src/main/resources/travel-demo/templates/pages/home.yaml
 *
 * The feature PR introduces a new `featuredToursHero` area wired to
 * `info.magnolia.demo.travel.tours.TourTemplatingFunctions`. On master the
 * home page has no such area, so nothing in the rendered DOM should match.
 *
 * After the PR the area becomes part of the rendered page; the assertion
 * below that the marker is absent will fail.
 */
import { test, expect } from '@playwright/test';

test('home page does not render a featured-tours hero on master', async ({ page }) => {
  await page.goto('/');

  const hero = page.locator('[data-area="featuredToursHero"], .featuredToursHero');
  await expect(hero).toHaveCount(0);
});
