/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   Mar 19, 2012 (hornm): created
 */
package org.knime.core.node;

import java.io.InputStream;
import java.util.Properties;

import org.apache.xmlbeans.XmlDocumentProperties;
import org.knime.node2012.KnimeNodeDocument;

/**
 * A node factory to create nodes dynamically. It essentially creates the node
 * description (usually given in the XXXNodeFactory.xml) dynamically.
 *
 * @author Dominik Morent, KNIME.com AG
 * @author Martin Horn, University of Konstanz
 * @param <T> the node model of the factory
 * @since 2.6
 */
public abstract class DynamicNodeFactory<T extends NodeModel> extends
        NodeFactory<T> {

    /**
     * Creates a new dynamic node factory. Additional properties should be set
     * later by invoking {@link #addAdditionalProperties(Properties)}.
     */
    public DynamicNodeFactory() {
        super(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final InputStream getPropertiesInputStream() {
        KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
        XmlDocumentProperties properties = doc.documentProperties();
        properties.setStandalone(true);
        properties.setEncoding("UTF-8");
        properties.setVersion("1.0");
        properties.setDoctypeName("knimeNode");
        properties.setDoctypePublicId("-//UNIKN//DTD KNIME Node 2.0//EN");
        properties.setDoctypeSystemId("http://www.knime.org/Node.dtd");
        addNodeDescription(doc);
        return doc.newInputStream();
    }

    /**
     * Subclasses should add the node description elements. The
     * {@link KnimeNodeDocument} reflects the structure given in
     * http://www.knime.org/Node.dtd and allows one to create the node
     * description document more easily (generated by XMLBeans (see
     * http://xmlbeans.apache.org/)
     *
     * @param doc the document to add the description to
     */
    protected abstract void addNodeDescription(final KnimeNodeDocument doc);
}
