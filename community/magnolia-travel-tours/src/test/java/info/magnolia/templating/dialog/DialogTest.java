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
package info.magnolia.templating.dialog;

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.MatcherAssert.assertThat;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.MultiValueField;
import info.magnolia.ui.VaadinLookup;
import info.magnolia.ui.field.LinkField;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.TextField;

public class DialogTest extends AbstractDialogTest {

    @Override
    String getModuleName() {
        return "tours";
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
        MockContext ctx = (MockContext) MgnlContext.getInstance();
        ctx.addSession("tours", this.session);
        ctx.addSession("category", this.session);
    }

    @Test
    public void tourList() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/tourList.yaml");

        // WHEN
        final TextField title = VaadinLookup.findByType(formView.asVaadinComponent(), TextField.class);
        title.setValue("A title");
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("title", title.getValue()));
    }

    @Test
    public void tourFinderRow() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/tourFinderRow.yaml");

        // WHEN
        final LinkField<Node> tourFinder = VaadinLookup.findByType(formView.asVaadinComponent(), LinkField.class);
        tourFinder.setValue(destination);
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("tourFinder", destination.getIdentifier()));
    }

    @Test
    public void tourCarousel() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/tourCarousel.yaml");
        // WHEN
        formView.applyDefaults();
        formView.write(destination);
        // THEN
        assertThat(destination, hasProperty("showTourTypes", true));

        //GIVEN
        final MultiValueField<Node> tourFinder = new MultiValueField<>(VaadinLookup.findByCaption(formView.asVaadinComponent(), "form.tours.label"));
        VaadinLookup.findByType(formView.asVaadinComponent(), LinkField.class);
        tourFinder.addValues(destination);
        // WHEN
        formView.write(destination);
        // THEN
        assertThat(destination, hasProperty("tours", new String[]{destination.getIdentifier()}));
    }

    @Test
    public void tourTypeTeaserRow() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/tourTypeTeaserRow.yaml");
        final MultiValueField<Node> tourFinder = new MultiValueField<>(VaadinLookup.findByCaption(formView.asVaadinComponent(), "form.tourTypes.label"));
        VaadinLookup.findByType(formView.asVaadinComponent(), LinkField.class);
        tourFinder.addValues(destination);

        // WHEN
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("tourTypes", new String[]{destination.getIdentifier()}));
    }
}
