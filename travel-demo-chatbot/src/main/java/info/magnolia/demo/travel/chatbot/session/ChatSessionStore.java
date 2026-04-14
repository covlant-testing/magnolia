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
package info.magnolia.demo.travel.chatbot.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import java.time.Duration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ChatSessionStore {

    private final Cache<String, ConversationHistory> cache;

    @Inject
    public ChatSessionStore() {
        this(Duration.ofMinutes(30), 10_000, Ticker.systemTicker());
    }

    public ChatSessionStore(Duration idleTtl, long maxEntries, Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .ticker(ticker)
                .expireAfterAccess(idleTtl)
                .maximumSize(maxEntries)
                .build();
    }

    public ConversationHistory getOrCreate(String sessionId) {
        return cache.get(sessionId, k -> new ConversationHistory());
    }

    public void cleanUp() {
        cache.cleanUp();
    }
}
