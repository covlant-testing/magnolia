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
package info.magnolia.demo.travel.chatbot.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EnvLoaderTest {

    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void parsesSimpleAssignmentsIgnoringCommentsAndBlanks() throws Exception {
        File env = tmp.newFile(".env");
        Files.writeString(env.toPath(),
                "# comment\n\nGEMINI_API_KEY=abc123\nFOO=\"hello world\"\n");
        Map<String, String> result = EnvLoader.loadFile(env);
        assertEquals("abc123", result.get("GEMINI_API_KEY"));
        assertEquals("hello world", result.get("FOO"));
    }

    @Test
    public void missingFileReturnsEmptyMap() {
        File env = new File(tmp.getRoot(), "does-not-exist");
        assertEquals(0, EnvLoader.loadFile(env).size());
    }

    @Test
    public void mergedSystemEnvTakesPrecedence() throws Exception {
        File env = tmp.newFile(".env");
        Files.writeString(env.toPath(), "PATH=should-be-overridden\n");
        Map<String, String> merged = EnvLoader.merge(EnvLoader.loadFile(env), System.getenv());
        assertEquals(System.getenv("PATH"), merged.get("PATH"));
    }

    @Test
    public void missingKeyReturnsNull() throws Exception {
        File env = tmp.newFile(".env");
        Files.writeString(env.toPath(), "");
        assertNull(EnvLoader.loadFile(env).get("ABSENT"));
    }
}
