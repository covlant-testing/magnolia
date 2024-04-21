/**
 * This file Copyright (c) 2015-2024 Magnolia International
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
import static org.hamcrest.MatcherAssert.assertThat;

import info.magnolia.ui.VaadinLookup;
import info.magnolia.ui.field.LinkField;

import javax.jcr.Node;

import org.junit.Test;

import com.vaadin.ui.Component;

public class DialogTest extends AbstractDialogTest {

    @Override
    public String getModuleName() {
        return "travel-demo-content-tags";
    }

    @Test
    public void tourDetail() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/tourDetail-content-tags.yaml");
        final Component form = formView.asVaadinComponent();

        // WHEN
        final LinkField<Node> tourListLink = VaadinLookup.findByType(form, LinkField.class);
        tourListLink.setValue(destination);
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("tourListLink", destination.getIdentifier()));
    }

    @Test
    public void tourList() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/tourList-content-tags.yaml");

        // WHEN
        formView.applyDefaults();
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("logicalOperand", "OR"));
    }
}
