/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   06.07.2014 (koetter): created
 */
package org.knime.base.data.aggregation.dialogutil;

import java.awt.Component;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.base.data.aggregation.AggregationMethod;
import org.knime.base.data.aggregation.AggregationMethods;
import org.knime.core.data.DataType;




/**
 * {@link AbstractAggregationTableModel} that stores {@link DataType}s and the {@link AggregationMethod} to use.
 * @author Tobias Koetter, KNIME.com, Zurich, Switzerland
 * @since 2.11
 */
public class RegexAggregationTableModel extends AbstractAggregationTableModel<RegexAggregator> {

    private static final long serialVersionUID = 1L;
    private JPanel m_panel;

    /**
     * Constructor.
     */
    RegexAggregationTableModel() {
        super(new String[] {"Regular expression (double click to change)", "Aggregation (click to change)"},
            new Class[] {RegexAggregator.class, RegexAggregator.class}, true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setValue(final Object aValue, final int row, final int columnIdx) {
        if (aValue == null) {
            return;
        }
        if (columnIdx == 0) {
            updateRegex(row, aValue.toString());
        }
        if (columnIdx == 1) {
            if (aValue instanceof AggregationMethod) {
                assert columnIdx == 1;
                final AggregationMethod newMethod = (AggregationMethod)aValue;
                updateMethod(row, newMethod);
            }
        }
    }

    /**
     * @param row the row index
     * @param regex the new regular expression to use
     */
    private void updateRegex(final int row, final String regex) {
        final RegexAggregator old = getRow(row);
        if (old.getRegex().equals(regex)) {
            //check if the method has changed
            return;
        }
        //create a new operator each time it is updated to guarantee that
        //each column has its own operator instance
        final AggregationMethod methodClone = AggregationMethods.getMethod4Id(old.getMethodTemplate().getId());
        final RegexAggregator regexAggregator = new RegexAggregator(regex, methodClone, old.inclMissingCells());
        if (!regexAggregator.isValid()) {
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                final Component root = SwingUtilities.getRoot(m_panel);
                JOptionPane.showMessageDialog(root, "<html><body><p>Invalid regular expression:</p><p>"
                        + regex + "</p><p>" + e.getDescription() + " at position " + e.getIndex() + "</p>",
                    "Invalid regular expression", JOptionPane.ERROR_MESSAGE);
            }
        }
        updateRow(row, regexAggregator);
    }

    /**
     * @param row the row to update
     * @param method the {@link AggregationMethod} to use
     */
    private void updateMethod(final int row, final AggregationMethod method) {
        final RegexAggregator old = getRow(row);
        if (old.getMethodTemplate().equals(method)) {
            //check if the method has changed
            return;
        }
        //create a new operator each time it is updated to guarantee that
        //each column has its own operator instance
        final AggregationMethod methodClone = AggregationMethods.getMethod4Id(method.getId());
        updateRow(row, new RegexAggregator(old.getRegex(), methodClone, old.inclMissingCells()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(final int row, final int columnIdx) {
        final RegexAggregator operator = getRow(row);
        if (!operator.isValid()) {
            //the row is not editable if the operator is invalid
            return columnIdx == 0;
        }
        return super.isCellEditable(row, columnIdx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEditable(final int row, final int columnIdx) {
        switch (columnIdx) {
            case 0:
                return true;
            case 1:
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getValueAtRow(final int row, final int columnIndex) {
        final RegexAggregator aggregator = getRow(row);
        switch (columnIndex) {
        case 0:
            return aggregator;
        case 1:
            return aggregator;

        default:
            break;
        }
        return null;
    }


    /**
     * @param panel the {@link JPanel} this model is used in
     */
    public void setRootPanel(final JPanel panel) {
        m_panel = panel;
    }
}
