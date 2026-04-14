/**
 * This file Copyright (c) 2026 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.demo.travel.chatbot.ratelimit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class SessionRateLimiterTest {

    @Test
    public void allowsUpToLimit() {
        SessionRateLimiter rl = new SessionRateLimiter(3, () -> 0L);
        assertTrue(rl.tryAcquire("s"));
        assertTrue(rl.tryAcquire("s"));
        assertTrue(rl.tryAcquire("s"));
        assertFalse(rl.tryAcquire("s"));
    }

    @Test
    public void perSessionIsolated() {
        SessionRateLimiter rl = new SessionRateLimiter(1, () -> 0L);
        assertTrue(rl.tryAcquire("a"));
        assertTrue(rl.tryAcquire("b"));
        assertFalse(rl.tryAcquire("a"));
    }

    @Test
    public void windowSlides() {
        AtomicLong now = new AtomicLong(0);
        SessionRateLimiter rl = new SessionRateLimiter(2, now::get);
        assertTrue(rl.tryAcquire("s"));
        assertTrue(rl.tryAcquire("s"));
        assertFalse(rl.tryAcquire("s"));
        now.addAndGet(Duration.ofSeconds(61).toNanos());
        assertTrue(rl.tryAcquire("s"));
    }
}
