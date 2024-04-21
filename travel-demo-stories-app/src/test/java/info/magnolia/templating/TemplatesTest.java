/**
 * This file Copyright (c) 2021-2024 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating;

import static org.mockito.Mockito.when;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.jcr.MockSession;

import java.nio.charset.Charset;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TemplatesTest extends AbstractRenderingTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        final Node area = renderingCtx.getCurrentContent().addNode("area", NodeTypes.Area.NAME);
        area.setProperty("date", Calendar.getInstance()); //date.ftl
        area.setProperty("title", "aTitle"); //storiesLead.ftl
        when(renderingCtx.getCurrentContent()).thenReturn(area);

        final WebContext webContext = MgnlContext.getWebContext();
        final Session stories = new MockSession("stories") {
            @Override
            public Node getNode(String absPath) throws RepositoryException {
                // storyDisplayArea.ftl
                final Node story = getRootNode().addNode("story");
                story.setProperty("title", "aTitle");
                story.setProperty("created", Calendar.getInstance());
                story.setProperty("authorImage", "authorImage");
                story.setProperty("imageCredit", "imageCredit");
                return story;
            }
        };
        when(webContext.getJCRSession("stories")).thenReturn(stories); // storiesImageUniverse.ftl
        when(webContext.getContextPath()).thenReturn("/magnoliaAuthor");

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setCharacterEncoding(Charset.defaultCharset().name());
        aggregationState.setCurrentContentNode(area);
        aggregationState.setSelector("story"); //storyDisplayArea.ftl
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/travel-demo-stories-app/templates/pages/areas/storyDisplayArea.ftl",
            "/travel-demo-stories-app/templates/components/externalLink.ftl",
            "/travel-demo-stories-app/templates/components/storiesImageUniverse.ftl",
            "/travel-demo-stories-app/templates/components/storiesLead.ftl",
            "/travel-demo-stories-app/templates/blocks/date.ftl",
            "/travel-demo-stories-app/templates/blocks/tour.ftl",
    })
    public void render(String templateScript) throws Exception {
        // GIVEN
        when(renderableDefinition.getTemplateScript()).thenReturn(templateScript);

        // WHEN
        freemarkerHelper.render(renderingCtx, contextObjects);
        // THEN no exceptions
    }
}
