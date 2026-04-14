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
package info.magnolia.demo.travel.chatbot.personalization;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves visitor traits from the MTE personalization module when available.
 * Uses a reflective lookup so the chatbot module does not have a hard compile-time
 * dependency on the enterprise personalization module. When the class is absent
 * or any error occurs during lookup, an empty map is returned.
 */
@Singleton
public class VisitorTraitsResolver {

    private static final Logger log = LoggerFactory.getLogger(VisitorTraitsResolver.class);

    private final Supplier<Map<String, String>> traitsSupplier;

    @Inject
    public VisitorTraitsResolver() {
        this(buildSupplier());
    }

    VisitorTraitsResolver(Supplier<Map<String, String>> traitsSupplier) {
        this.traitsSupplier = traitsSupplier;
    }

    /**
     * Returns the current visitor's traits, or an empty map when personalization
     * is unavailable or any error occurs.
     */
    public Map<String, String> resolve() {
        try {
            Map<String, String> traits = traitsSupplier.get();
            return traits != null ? traits : Collections.emptyMap();
        } catch (Exception e) {
            log.debug("Visitor traits unavailable: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private static Supplier<Map<String, String>> buildSupplier() {
        try {
            Class.forName("info.magnolia.personalization.visitor.VisitorContext");
            return () -> {
                try {
                    Class<?> ctxClass = Class.forName("info.magnolia.personalization.visitor.VisitorContext");
                    Object ctx = ctxClass.getMethod("getCurrent").invoke(null);
                    if (ctx == null) {
                        return null;
                    }
                    return (Map<String, String>) ctxClass.getMethod("getTraits").invoke(ctx);
                } catch (Exception e) {
                    log.debug("Could not read VisitorContext traits: {}", e.getMessage());
                    return null;
                }
            };
        } catch (ClassNotFoundException e) {
            log.debug("Personalization module not on classpath; visitor traits disabled");
            return () -> null;
        }
    }
}
