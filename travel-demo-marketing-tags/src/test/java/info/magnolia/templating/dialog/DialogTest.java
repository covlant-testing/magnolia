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
package info.magnolia.templating.dialog;

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DialogTest extends AbstractDialogTest {

    @Override
    String getModuleName() {
        return "travel-demo-marketing-tags";
    }

    @Test
    public void pageProperties() throws Exception {
        // GIVEN
        formView = createForm("/decorations/travel-demo/dialogs/pages/pageProperties.yaml");

        // WHEN
        formView.applyDefaults();
        formView.write(destination);

        // THEN
        assertThat(destination, allOf(
                hasProperty("complianceType", "info"),
                hasProperty("header"), hasProperty("message"), hasProperty("dismiss"),
                hasProperty("allow"), hasProperty("deny"), hasProperty("link"),
                hasProperty("position", "bottom"),
                hasProperty("layout", "block"),
                hasProperty("readMoreLink", "external"),
                hasProperty("readMoreLinkexternal"),
                hasProperty("bannerbackground"),
                hasProperty("buttonbackground")
        ));
    }
}
