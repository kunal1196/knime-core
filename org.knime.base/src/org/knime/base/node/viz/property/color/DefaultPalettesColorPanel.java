/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 * ------------------------------------------------------------------------
 */
package org.knime.base.node.viz.property.color;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import org.knime.base.node.viz.property.color.ColorManager2NodeModel.NewValueOption;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

/**
 * A default panel to show two color palettes.
 *
 * @author Johannes Schweig, KNIME AG
 */
final class DefaultPalettesColorPanel extends AbstractColorChooserPanel {

    private final JButton m_set1Button = new JButton("Apply to columns");

    private final JButton m_set2Button = new JButton("Apply to columns");

    private final JButton m_set3Button = new JButton("Apply to columns");

    private JPanel m_newValuesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PALETTE_ELEMENT_SPACING, 0));

    private final String[] m_paletteSet1;

    private final String[] m_paletteSet2;

    private final String[] m_paletteSet3;

    /** Size of the individual elements of a palette. */
    private static final int PALETTE_ELEMENT_SIZE = 20;

    /** Spacing between the individual elements of a palette. */
    private static final int PALETTE_ELEMENT_SPACING = 4;

    private JComboBox<NewValueOption> m_handleNewValues;

    /**
     * @param paletteSet1 the first, default color palette
     * @param paletteSet2 the second color palette
     */
    DefaultPalettesColorPanel(final String[] paletteSet1, final String[] paletteSet2, final String[] paletteSet3) {
        m_paletteSet1 = paletteSet1;
        m_paletteSet2 = paletteSet2;
        m_paletteSet3 = paletteSet3;
        // set the button fonts
        m_set1Button.setFont(new Font(m_set1Button.getFont().getName(), Font.PLAIN, m_set1Button.getFont().getSize()));
        m_set2Button.setFont(new Font(m_set2Button.getFont().getName(), Font.PLAIN, m_set2Button.getFont().getSize()));
        m_set3Button.setFont(new Font(m_set3Button.getFont().getName(), Font.PLAIN, m_set3Button.getFont().getSize()));
    }

    /**
     * Overwrites the default JPanel to notify the ColorSelectionModel of changes.
     */
    private class PaletteElement extends JPanel {

        private Color m_color;

        PaletteElement(final String c, final int size) {
            m_color = Color.decode(c);
            setPreferredSize(new Dimension(size, size));
            setBackground(m_color);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    getColorSelectionModel().setSelectedColor(m_color);
                }

                @Override
                public void mousePressed(final MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(Color.gray));
                }

                @Override
                public void mouseReleased(final MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(Color.black));
                }

                @Override
                public void mouseEntered(final MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(Color.black));
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    setBorder(null);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildChooser() {
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 10, 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;

        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Set 1:"),gbc);
        ++gbc.gridx;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(createColorPanel(m_paletteSet1), gbc);
        ++gbc.gridx;
        add(m_set1Button, gbc);
        gbc.gridx = 0;
        ++gbc.gridy;

        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Set 2:"),gbc);
        ++gbc.gridx;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(createColorPanel(m_paletteSet2), gbc);
        ++gbc.gridx;
        add(m_set2Button, gbc);
        gbc.gridx = 0;
        ++gbc.gridy;

        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Set 3 (colorblind safe):"),gbc);
        ++gbc.gridx;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(createColorPanel(m_paletteSet3), gbc);
        ++gbc.gridx;
        add(m_set3Button, gbc);
        gbc.gridx = 0;
        ++gbc.gridy;

        // Panel for new values
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("On different table:"), gbc);
        ++gbc.gridx;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 10, 10, 20);
        m_handleNewValues = new JComboBox<NewValueOption>(NewValueOption.values());
        add(m_handleNewValues, gbc);
    }

    /**
     * @param palette the color palette
     * @return panel containing the color palette.
     */
    private JPanel createColorPanel(final String[] palette) {
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PALETTE_ELEMENT_SPACING, 0));
        colorPanel.setAlignmentX(LEFT_ALIGNMENT);
        for (String s : palette) {
            colorPanel.add(new PaletteElement(s, PALETTE_ELEMENT_SIZE));
        }
        return colorPanel;
    }

    /**
     * @param al1 the action listener for the first button
     * @param al2 the action listener for the second button
     * @param al3 the action listener for the third button
     */
    void addActionListeners(final ActionListener al1, final ActionListener al2, final ActionListener al3) {
        m_set1Button.addActionListener(al1);
        m_set2Button.addActionListener(al2);
        m_set3Button.addActionListener(al3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        // hide buttons and handling of new values for range columns
        m_set1Button.setVisible(enabled);
        m_set2Button.setVisible(enabled);
        m_set3Button.setVisible(enabled);
        m_newValuesPanel.setVisible(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Palettes";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMnemonic() {
        return KeyEvent.VK_P;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDisplayedMnemonicIndex() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Icon getLargeDisplayIcon() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Icon getSmallDisplayIcon() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChooser() {

    }

    /**
     * Saves the settings.
     *
     * @param settings the settings storage
     */
    void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addString(ColorManager2NodeModel.CFG_NEW_VALUES,
            ((NewValueOption)m_handleNewValues.getSelectedItem()).getSettingsName());
    }

    /**
     * Loads the settings.
     *
     * @param settings the settings provider
     * @param specs the data table spec
     * @throws NotConfigurableException if the stored new values option is not associated with a
     *             <code>NewValueOption</code>
     */
    public void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        try {
            // get color mapping for unassigned colors
            final NewValueOption newValueOption = NewValueOption.getEnum(
                settings.getString(ColorManager2NodeModel.CFG_NEW_VALUES, NewValueOption.FAIL.getSettingsName()));
            m_handleNewValues.setSelectedItem(newValueOption);
        } catch (InvalidSettingsException e) {
            throw new NotConfigurableException(e.getMessage());
        }
    }
}
