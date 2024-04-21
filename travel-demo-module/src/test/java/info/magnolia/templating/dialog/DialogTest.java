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
import static org.hamcrest.core.AllOf.allOf;

import info.magnolia.ui.VaadinLookup;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.field.CheckBoxField;
import info.magnolia.ui.field.LinkField;

import javax.jcr.Node;

import org.junit.Test;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;

public class DialogTest extends AbstractDialogTest {

    @Override
    String getModuleName() {
        return "travel-demo";
    }

    @Test
    public void pageProperties() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/pages/pageProperties.yaml");

        // WHEN
        final LinkField<Node> linkField = VaadinLookup.findByType(formView.asVaadinComponent(), LinkField.class);
        linkField.setValue(destination);
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("searchResultPage", destination.getIdentifier()));
    }

    @Test
    public void link() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/link.yaml");

        // WHEN
        final LinkField<Node> linkField = VaadinLookup.findByType(formView.asVaadinComponent(), LinkField.class);
        linkField.setValue(destination);
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("image", destination.getIdentifier()));
    }

    @Test
    public void teaser() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/teaser.yaml");

        // WHEN
        final CheckBoxField checkBoxField = VaadinLookup.findByType(formView.asVaadinComponent(), CheckBoxField.class);
        checkBoxField.setValue(true);
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("constrainAspectRatio", true));
    }

    @Test
    public void textImage() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/textImage.yaml");

        // WHEN
        formView.applyDefaults();
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("constrainAspectRatio", false));
    }

    @Test
    public void carousel() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/carousel.yaml");

        // WHEN
        formView.applyDefaults();
        formView.write(destination);

        // THEN
        assertThat(destination, allOf(
                hasProperty("dots", true),
                hasProperty("fade", false),
                hasProperty("fade", false),
                hasProperty("variableWidth", false),
                hasProperty("arrows", true),
                hasProperty("slidesToShow", "1"),
                hasProperty("autoplay", true),
                hasProperty("autoplaySeconds", "5")
        ));
    }

    @Test
    public void columnLayout() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/columnLayout.yaml");

        // WHEN
        final ComboBox<Option> comboBox = VaadinLookup.findByType(formView.asVaadinComponent(), ComboBox.class);
        comboBox.setValue(createOption("4x8"));
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("layout", comboBox.getValue().getValue()));
    }

    @Test
    public void searchResults() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/searchResults.yaml");

        // WHEN
        final TextField textField = VaadinLookup.findByType(formView.asVaadinComponent(), TextField.class);
        textField.setValue("A headline");
        formView.write(destination);

        // THEN
        assertThat(destination, hasProperty("headline", textField.getValue()));
    }

    @Test
    public void social() throws Exception {
        // GIVEN
        formView = createForm("/dialogs/components/social.yaml");

        // WHEN
        formView.applyDefaults();
        formView.write(destination);

        // THEN
        assertThat(destination, allOf(
                hasProperty("size", "32"),
                hasProperty("floating", "true"),
                hasProperty("vertical", "true"),
                hasProperty("rounded", "true"),
                hasProperty("services") //, new String[]{"twitter", "facebook", "google_plus"} //HashSet = random order
        ));
    }
}
