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

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionRateLimiter {

    private static final long WINDOW_NANOS = Duration.ofSeconds(60).toNanos();
    private final int permitsPerMinute;
    private final LongSupplier clockNanos;
    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    @Inject
    public SessionRateLimiter() {
        this(30, System::nanoTime);
    }

    public SessionRateLimiter(int permitsPerMinute, LongSupplier clockNanos) {
        this.permitsPerMinute = permitsPerMinute;
        this.clockNanos = clockNanos;
    }

    public boolean tryAcquire(String sessionId) {
        long now = clockNanos.getAsLong();
        long cutoff = now - WINDOW_NANOS;
        Deque<Long> q = windows.computeIfAbsent(sessionId, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
            if (q.size() >= permitsPerMinute) return false;
            q.addLast(now);
            return true;
        }
    }
}
