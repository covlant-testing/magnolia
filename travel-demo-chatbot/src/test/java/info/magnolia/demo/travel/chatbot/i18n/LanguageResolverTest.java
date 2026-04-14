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
package info.magnolia.demo.travel.chatbot.i18n;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.Test;

public class LanguageResolverTest {

    @Test
    public void usesAggregationStateLocaleWhenPresent() {
        LanguageResolver r = new LanguageResolver(() -> Locale.GERMAN);
        HttpServletRequest req = mock(HttpServletRequest.class);
        assertEquals("de", r.resolve(req));
    }

    @Test
    public void stripsRegionFromAggregationLocale() {
        LanguageResolver r = new LanguageResolver(() -> new Locale("de", "AT"));
        assertEquals("de", r.resolve(mock(HttpServletRequest.class)));
    }

    @Test
    public void fallsBackToAcceptLanguage() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Accept-Language")).thenReturn("fr-CA,fr;q=0.9");
        LanguageResolver r = new LanguageResolver(() -> null);
        assertEquals("fr", r.resolve(req));
    }

    @Test
    public void fallsBackToEnglish() {
        LanguageResolver r = new LanguageResolver(() -> null);
        assertEquals("en", r.resolve(mock(HttpServletRequest.class)));
    }
}
