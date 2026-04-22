/**
 * Regression guard for PR `refactor/chatbot-endpoint-v2`.
 *
 * Handler: info.magnolia.demo.travel.chatbot.rest.ChatEndpoint
 *          (@Path("/chatbot/v1"))
 *
 * Master exposes the chat turn endpoint at `/.rest/chatbot/v1/turn`. The
 * refactor PR bumps the path to `/chatbot/v2`, so a client (and this test)
 * still pointing at `/v1` will hit a 404 — breaking the assertion below.
 *
 * The body is intentionally minimal — we only care that the endpoint
 * accepts POSTs and returns a JSON payload on master, proving the v1 path
 * is live.
 */
import { test, expect } from '@playwright/test';

test('POST /.rest/chatbot/v1/turn responds successfully on master', async ({ request }) => {
  const response = await request.post('/.rest/chatbot/v1/turn', {
    headers: { 'Content-Type': 'application/json' },
    data: {
      sessionId: 'e2e-regression',
      message: 'hello',
    },
  });

  expect(
    response.status(),
    'v1 chat-turn endpoint must be live on master',
  ).toBe(200);

  const body = await response.json();
  expect(body, 'chat response body must be JSON').toBeDefined();
});
