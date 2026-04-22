import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright config for the travel-demo regression suite.
 *
 * Magnolia author/public instances default to http://localhost:8080 for
 * developer runs. CI can override the target via the MAGNOLIA_BASE_URL env
 * var.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.MAGNOLIA_BASE_URL ?? 'http://localhost:8080',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
