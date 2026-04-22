/**
 * Regression guard for PR `feature/tour-api-endpoint` — route absence.
 *
 * Master does not expose `/api/tours/{id}.json`. The feature PR introduces a
 * new virtualUriMapping (travel-demo-tours/.../toursApiMapping.yaml) that
 * forwards `^/api/tours/(.*)\.json$` to `/travel/tour-api`.
 *
 * This test asserts that on master the route responds with 404. Once the
 * feature PR lands, the forward succeeds and the HTTP status becomes 200
 * (or whatever the tour-api handler returns), failing this assertion.
 */
import { test, expect } from '@playwright/test';

test('GET /api/tours/{id}.json is not exposed on master (404)', async ({ request }) => {
  const response = await request.get('/api/tours/123.json');
  expect(response.status(), 'master should not expose a tours API').toBe(404);
});
