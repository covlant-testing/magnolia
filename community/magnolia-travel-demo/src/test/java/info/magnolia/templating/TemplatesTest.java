/**
 * This file Copyright (c) 2021-2024 Magnolia International
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
package info.magnolia.templating;

import static org.mockito.Mockito.when;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import javax.jcr.Node;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.collect.ImmutableMap;

public class TemplatesTest extends AbstractRenderingTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        final Node node = renderingCtx.getCurrentContent();
        node.setProperty("linkType", "page"); //link.ftl
        node.setProperty("link", node.getIdentifier()); //link.ftl
        node.setProperty("vertical", "true"); //social.ftl
        node.setProperty("floating", "true"); //social.ftl
        node.setProperty("rounded", "true"); //social.ftl

        final WebContext webContext = MgnlContext.getWebContext();
        when(webContext.getContextPath()).thenReturn("/magnoliaAuthor"); ///travel-demo/templates/pages/*.ftl
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/travel-demo/templates/components/jumbotron.yaml",
            "/travel-demo/templates/components/columnLayout.yaml",
            "/travel-demo/templates/components/link.yaml",
            "/travel-demo/templates/components/linkList.yaml",
            "/travel-demo/templates/components/carousel.yaml",
            "/travel-demo/templates/components/textImage.yaml",
            "/travel-demo/templates/components/teaser.yaml",
            "/travel-demo/templates/components/social.yaml",
            "/travel-demo/templates/components/searchResults.yaml",
            "/travel-demo/templates/pages/aboutDemo.yaml",
            "/travel-demo/templates/pages/home.yaml",
            "/travel-demo/templates/pages/prototype.yaml",
            "/travel-demo/templates/pages/pur.yaml",
            "/travel-demo/templates/pages/standard.yaml",
            "/travel-demo/templates/pages/searchResultPage.yaml"
    })
    public void render(String templateDefinitionPath) throws Exception {
        // GIVEN
        String templateDefinition = IOUtils.toString(getClass().getResourceAsStream(templateDefinitionPath), Charset.defaultCharset());
        String templateScript = StringUtils.substringAfter(templateDefinition, "templateScript: ");
        templateScript = StringUtils.substringBefore(templateScript, "\n");
        when(renderableDefinition.getTemplateScript()).thenReturn(templateScript);
        // WHEN
        freemarkerHelper.render(renderingCtx, ImmutableMap.of(
                "cms", directives,
                "cmsfn", templatingFunctions,
                "components", new ArrayList<>() //pur.ftl
        ));
        // THEN no exceptions
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/travel-demo/themes/travel-demo-theme.yaml",
    })
    public void allLinksExist(String templateDefinitionPath) {
        // GIVEN
        InputStream templateDefinition = getClass().getResourceAsStream(templateDefinitionPath);

        // WHEN
        final Scanner template = new Scanner(templateDefinition);

        // THEN
        while (template.hasNextLine()) {
            final String link = StringUtils.substringAfter(template.nextLine(), "link: /.resources");
            Assertions.assertNotNull(Optional.ofNullable(link)
                            .map(getClass()::getResource)
                            .orElse(null),
                    link + " doesn't exist");
        }
    }
}
