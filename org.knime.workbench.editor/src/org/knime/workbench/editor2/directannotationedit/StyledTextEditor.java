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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *
 * History
 *   2010 10 25 (ohl): created
 */
package org.knime.workbench.editor2.directannotationedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.Annotation;
import org.knime.core.node.workflow.AnnotationData;
import org.knime.core.node.workflow.NodeAnnotation;
import org.knime.workbench.KNIMEEditorPlugin;
import org.knime.workbench.core.util.ImageRepository;
import org.knime.workbench.editor2.editparts.AnnotationEditPart;
import org.knime.workbench.editor2.editparts.FontStore;

/**
 *
 * @author ohl, KNIME AG, Zurich, Switzerland
 */
public class StyledTextEditor extends CellEditor {
    private static final int TAB_SIZE;

    static {
        // set tab size for win and linux and mac differently (it even depends on the zoom level, yuk!)
        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            TAB_SIZE = 8;
        } else if (Platform.OS_LINUX.equals(Platform.getOS())) {
            TAB_SIZE = 8;
        } else {
            TAB_SIZE = 16;
        }
    }

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StyledTextEditor.class);

    private static final RGB[] DEFAULT_COLORS = new RGB[]{//
        fromHex("CDE280"), fromHex("D8D37B"), //
        fromHex("93DDD2"), fromHex("D0D2B5"), //
        fromHex("ADDF9E"), fromHex("E8AFA7"), //
        fromHex("C4CBE0"), fromHex("E3B67D")};

    private static RGB[] lastColors = null;

    private StyledText m_styledText;

    /**
     * instance used to get layout info in a non-word-wrapping editor (the foreground text editor must be auto-wrapped
     * otherwise the alignment is ignored!).
     */
    private StyledText m_shadowStyledText;

    /**
     * List of menu items that are disabled/enabled with text selection, e.g. copy or font selection.
     */
    private List<MenuItem> m_enableOnSelectedTextMenuItems;

    /**
     * Whether the text shall be selected when the editor is activated. It's true if the annotation contains the default
     * text ("Double-Click to edit" or "Node x").
     */
    private boolean m_selectAllUponFocusGain;

    private Composite m_panel;

    private Color m_backgroundColor = null;

    private final AtomicBoolean m_allowFocusLost = new AtomicBoolean(true);

    private MenuItem m_rightAlignMenuItem;

    private MenuItem m_centerAlignMenuItem;

    private MenuItem m_leftAlignMenuItem;

    /**
     * Creates a workflow annotation editor (with the font set to workflow annotations default font - see
     * #setDefaultFont(Font)).
     */
    public StyledTextEditor() {
        super();
    }

    /**
     * @param parent
     */
    public StyledTextEditor(final Composite parent) {
        super(parent);
    }

    /**
     * @param parent
     * @param style
     */
    public StyledTextEditor(final Composite parent, final int style) {
        super(parent, style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createControl(final Composite parent) {
        m_panel = new Composite(parent, SWT.NONE);
        StackLayout layout = new StackLayout();
        m_panel.setLayout(layout);
        layout.topControl = createStyledText(m_panel);
        createShadowText(m_panel);
        applyBackgroundColor();
        return m_panel;
    }

    private Control createShadowText(final Composite parent) {
        m_shadowStyledText = new StyledText(parent, SWT.MULTI | SWT.FULL_SELECTION);
        m_shadowStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        syncShadowWithEditor();
        return m_shadowStyledText;
    }

    private void syncShadowWithEditor() {
        m_shadowStyledText.setFont(m_styledText.getFont());
        m_shadowStyledText.setVisible(false);
        m_shadowStyledText.setBounds(m_styledText.getBounds());
        m_shadowStyledText.setText(m_styledText.getText());
        m_shadowStyledText.setStyleRanges(m_styledText.getStyleRanges());
        m_shadowStyledText.setAlignment(m_styledText.getAlignment());
        m_shadowStyledText.setBackground(m_styledText.getBackground());
        int m = m_styledText.getRightMargin();
        m_shadowStyledText.setMargins(m, m, m, m);
        m_shadowStyledText.setMarginColor(m_styledText.getMarginColor());
    }

    private Control createStyledText(final Composite parent) {
        m_styledText = new StyledText(parent, SWT.MULTI | SWT.WRAP | SWT.FULL_SELECTION);
        // by default we are a workflow annotation editor
        // can be changed by changing the default font (setDefaultFont(Font))
        m_styledText.setFont(AnnotationEditPart.getWorkflowAnnotationDefaultFont());
        m_styledText.setAlignment(SWT.LEFT);
        m_styledText.setText("");
        m_styledText.setTabs(TAB_SIZE);
        m_styledText.addVerifyKeyListener(new VerifyKeyListener() {
            @Override
            public void verifyKey(final VerifyEvent event) {
                if (event.character == SWT.CR && (event.stateMask & SWT.MOD1) != 0) {
                    event.doit = false;
                }
            }
        });
        // forward some events to the cell editor
        m_styledText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                keyReleaseOccured(e);
            }
        });
        m_styledText.addFocusListener(new FocusAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void focusLost(final org.eclipse.swt.events.FocusEvent e) {
                // close the editor only if called directly (not as a side
                // effect of an opening font editor, for instance)
                if (m_allowFocusLost.get()) {
                    lostFocus();
                }
            }
        });
        m_styledText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                // super marks it dirty (otherwise no commit at the end)
                fireEditorValueChanged(true, true);
            }
        });
        m_styledText.addExtendedModifyListener(new ExtendedModifyListener() {
            @Override
            public void modifyText(final ExtendedModifyEvent event) {
                if (event.length > 0) {
                    textInserted(event.start, event.length);
                }
            }
        });
        m_styledText.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                selectionChanged();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                selectionChanged();
            }
        });
        m_styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        addMenu(m_styledText);
        // toolbar gets created first - enable its style buttons!
        selectionChanged();
        return m_styledText;
    }

    /**
     * Changes the font of unformatted text ranges.
     *
     * @param newDefaultFont The font to use, not null
     */
    public void setDefaultFont(final Font newDefaultFont) {
        if (newDefaultFont != null && !newDefaultFont.equals(m_styledText.getFont())) {
            m_styledText.setFont(newDefaultFont);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fireEditorValueChanged(final boolean oldValidState, final boolean newValidState) {
        syncShadowWithEditor();
        super.fireEditorValueChanged(oldValidState, newValidState);
    }

    /**
     * Sets the style range for the new text. Copies it from the left neighbor (or from the right neighbor, if there is
     * no left neighbor).
     *
     * @param startIdx
     * @param length
     */
    private void textInserted(final int startIdx, final int length) {
        if (m_styledText.getCharCount() <= length) {
            // no left nor right neighbor
            return;
        }
        StyleRange[] newStyles = m_styledText.getStyleRanges(startIdx, length);
        if (newStyles != null && newStyles.length > 0 && newStyles[0] != null) {
            // inserted text already has a style (shouldn't really happen)
            return;
        }
        StyleRange[] extStyles;
        if (startIdx == 0) {
            extStyles = m_styledText.getStyleRanges(length, 1);
        } else {
            extStyles = m_styledText.getStyleRanges(startIdx - 1, 1);
        }
        if (extStyles == null || extStyles.length != 1 || extStyles[0] == null) {
            // no style to extend over inserted text
            return;
        }
        if (startIdx == 0) {
            extStyles[0].start = 0;
        }
        extStyles[0].length += length;
        m_styledText.setStyleRange(extStyles[0]);
    }

    private void selectionChanged() {
        boolean enabled = true;
        int[] sel = m_styledText.getSelectionRanges();
        if (sel == null || sel.length != 2) {
            enabled = false;
        } else {
            int length = sel[1];
            enabled = (length > 0);
        }
        fireEnablementChanged(COPY);
        fireEnablementChanged(CUT);
        enableStyleButtons(enabled);
    }

    private void enableStyleButtons(final boolean enableThem) {
        if (m_enableOnSelectedTextMenuItems != null) {
            for (MenuItem action : m_enableOnSelectedTextMenuItems) {
                action.setEnabled(enableThem);
            }
        }
    }

    private void addMenu(final Composite parent) {
        Menu menu = new Menu(parent);
        // On some Linux systems the right click triggers focus loss, we need to disable this while the menu is open
        menu.addListener(SWT.Show, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                m_allowFocusLost.set(false);
            }
        });
        menu.addListener(SWT.Hide, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                m_allowFocusLost.set(true);
            }
        });
        Image img;
        MenuItem action;

        // background color
        img = ImageRepository.getImage(KNIMEEditorPlugin.PLUGIN_ID, "icons/annotations/bgcolor_10.png");
        action = addMenuItem(menu, "bg", SWT.PUSH, "Background", img);

        // alignment
        img = ImageRepository.getImage(KNIMEEditorPlugin.PLUGIN_ID, "icons/annotations/alignment_10.png");

        MenuItem alignmentMenuItem = addMenuItem(menu, "alignment", SWT.CASCADE, "Alignment", img);

        final Menu alignMenu = new Menu(alignmentMenuItem);
        alignmentMenuItem.setMenu(alignMenu);

        m_leftAlignMenuItem = addMenuItem(alignMenu, "alignment_left", SWT.RADIO, "Left", null);

        m_centerAlignMenuItem = addMenuItem(alignMenu, "alignment_center", SWT.RADIO, "Center", null);

        m_rightAlignMenuItem = addMenuItem(alignMenu, "alignment_right", SWT.RADIO, "Right", null);

        new MenuItem(menu, SWT.SEPARATOR);
        // contains buttons being en/disabled with selection
        m_enableOnSelectedTextMenuItems = new ArrayList<MenuItem>();

        // font/style button
        img = ImageRepository.getImage(KNIMEEditorPlugin.PLUGIN_ID, "icons/annotations/font_10.png");
        action = addMenuItem(menu, "style", SWT.PUSH, "Font Style...", img);
        m_enableOnSelectedTextMenuItems.add(action);

        new MenuItem(menu, SWT.SEPARATOR);

        // border style
        img = ImageRepository.getImage(KNIMEEditorPlugin.PLUGIN_ID, "icons/annotations/border_10.png");
        action = addMenuItem(menu, "border", SWT.PUSH, "Border...", img);

        new MenuItem(menu, SWT.SEPARATOR);

        // ok button
        img = ImageRepository.getImage(KNIMEEditorPlugin.PLUGIN_ID, "icons/annotations/ok_10.png");
        addMenuItem(menu, "ok", SWT.PUSH, "OK (commit)", img);

        // cancel button
        img = ImageRepository.getImage(KNIMEEditorPlugin.PLUGIN_ID, "icons/annotations/cancel_10.png");
        addMenuItem(menu, "cancel", SWT.PUSH, "Cancel (discard)", img);

        parent.setMenu(menu);
    }

    private MenuItem addMenuItem(final Menu menuMgr, final String id, final int style, final String text,
        final Image img) {
        MenuItem menuItem = new MenuItem(menuMgr, style);
        SelectionAdapter selListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                m_allowFocusLost.set(false);
                try {
                    buttonClick(id);
                } finally {
                    m_allowFocusLost.set(true);
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                super.widgetSelected(e);
            }
        };
        menuItem.addSelectionListener(selListener);
        menuItem.setText(text);
        menuItem.setImage(img);
        return menuItem;
    }

    private void buttonClick(final String src) {
        if (src.equals("style")) {
            font();
            fireEditorValueChanged(true, true);
        } else if (src.equals("color")) {
            fontColor();
            fireEditorValueChanged(true, true);
        } else if (src.equals("bold")) {
            bold();
            fireEditorValueChanged(true, true);
        } else if (src.equals("italic")) {
            italic();
            fireEditorValueChanged(true, true);
        } else if (src.equals("bg")) {
            bgColor();
            fireEditorValueChanged(true, true);
        } else if (src.equals("alignment_left")) {
            alignment(SWT.LEFT);
            fireEditorValueChanged(true, true);
        } else if (src.equals("alignment_center")) {
            alignment(SWT.CENTER);
            fireEditorValueChanged(true, true);
        } else if (src.equals("alignment_right")) {
            alignment(SWT.RIGHT);
            fireEditorValueChanged(true, true);
        } else if (src.equals("border")) {
            borderStyle();
            fireEditorValueChanged(true, true);
        } else if (src.equals("ok")) {
            ok();
        } else if (src.equals("cancel")) {
            cancel();
        } else {
            LOGGER.coding("IMPLEMENTATION ERROR: Wrong button ID");
        }

        // set the focus back to the editor after the buttons finish
        if (!src.equals("ok") && !src.equals("cancel")) {
            m_styledText.setFocus();
        }

    }

    private void applyBackgroundColor() {
        if (m_backgroundColor != null && m_panel != null) {
            LinkedList<Composite> comps = new LinkedList<Composite>();
            comps.add(m_panel);
            while (!comps.isEmpty()) {
                // set the composite's bg
                Composite c = comps.pollFirst();
                c.setBackgroundMode(SWT.INHERIT_NONE);
                c.setBackground(m_backgroundColor);
                // and the bg all of its children
                Control[] children = c.getChildren();
                for (Control child : children) {
                    if (child instanceof Composite) {
                        comps.add((Composite)child);
                    } else {
                        child.setBackground(m_backgroundColor);
                    }
                }
            }
        }
    }

    /**
     * @param bg
     */
    public void setBackgroundColor(final Color bg) {
        m_backgroundColor = bg;
        applyBackgroundColor();
    }

    /**
     *
     */
    protected void lostFocus() {
        super.focusLost();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void keyReleaseOccured(final KeyEvent keyEvent) {
        if (keyEvent.character == SWT.CR) { // Return key
            // don't let super close the editor on CR
            if ((keyEvent.stateMask & SWT.MOD1) != 0) {
                // closing the editor with Ctrl/Command-CR.
                keyEvent.doit = false;
                fireApplyEditorValue();
                deactivate();
                return;
            }
        } else {
            super.keyReleaseOccured(keyEvent);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return a {@link AnnotationData} with the new text and style ranges - and with the same ID as the original
     *         annotation (the one the editor was initialized with) - but in a new object.
     */
    @Override
    protected Object doGetValue() {
        assert m_styledText != null : "Control not created!";
        return AnnotationEditPart.toAnnotationData(m_styledText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetFocus() {
        assert m_styledText != null : "Control not created!";
        String text = m_styledText.getText();
        if (m_selectAllUponFocusGain) {
            performSelectAll();
        }
        m_styledText.setFocus();
        m_styledText.setCaretOffset(text.length());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetValue(final Object value) {
        assert value instanceof Annotation : "Wrong value object!";
        final Annotation wa = (Annotation)value;
        final int alignment;
        switch (wa.getAlignment()) {
            case CENTER:
                alignment = SWT.CENTER;
                break;
            case RIGHT:
                alignment = SWT.RIGHT;
                break;
            default:
                alignment = SWT.LEFT;
        }
        checkSelectionOfAlignmentMenuItems(alignment);
        m_selectAllUponFocusGain = false;
        final String text;
        if (wa instanceof NodeAnnotation) {
            if (AnnotationEditPart.isDefaultNodeAnnotation(wa)) {
                text = AnnotationEditPart.getAnnotationText(wa);
                m_selectAllUponFocusGain = true;
            } else {
                text = wa.getText();
            }
        } else {
            text = wa.getText();

            final int annotationBorderSize = wa.getBorderSize();
            // set margins as borders
            m_styledText.setMarginColor(AnnotationEditPart.RGBintToColor(wa.getBorderColor()));
            if (annotationBorderSize > 0) {
                m_styledText.setMargins(annotationBorderSize, annotationBorderSize, annotationBorderSize,
                    annotationBorderSize);
            }
            // for workflow annotations set the default font to the size stored in the annotation
            final Font defFont;
            final int defFontSize = wa.getDefaultFontSize();
            if (defFontSize < 0) {
                defFont = AnnotationEditPart.getWorkflowAnnotationDefaultFont(); // uses the size from the pref page
            } else {
                defFont = AnnotationEditPart.getWorkflowAnnotationDefaultFont(defFontSize);
            }
            setDefaultFont(defFont);
        }
        m_styledText.setAlignment(alignment);
        m_styledText.setText(text);
        m_styledText.setStyleRanges(AnnotationEditPart.toSWTStyleRanges(wa.getData(), m_styledText.getFont()));
        setBackgroundColor(AnnotationEditPart.RGBintToColor(wa.getBgColor()));
        syncShadowWithEditor();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCopyEnabled() {
        return !m_styledText.isDisposed() && m_styledText.getSelectionCount() > 0;
    }

    /** {@inheritDoc} */
    @Override
    public void performCopy() {
        m_styledText.copy();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPasteEnabled() {
        return !m_styledText.isDisposed();
    }

    /** {@inheritDoc} */
    @Override
    public void performPaste() {
        m_styledText.paste();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCutEnabled() {
        return !m_styledText.isDisposed() && m_styledText.getSelectionCount() > 0;
    }

    /** {@inheritDoc} */
    @Override
    public void performCut() {
        m_styledText.cut();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSelectAllEnabled() {
        return !m_styledText.isDisposed();
    }

    /** {@inheritDoc} */
    @Override
    public void performSelectAll() {
        m_styledText.selectAll();
        selectionChanged();
    }

    private void bold() {
        setSWTStyle(SWT.BOLD);
    }

    /**
     * Update selection state of alignment buttons in menu.
     *
     * @param swtAlignment SWT.LEFT, CENTER, or RIGHT
     */
    private void checkSelectionOfAlignmentMenuItems(final int swtAlignment) {
        MenuItem[] alignmentMenuItems =
            new MenuItem[]{m_leftAlignMenuItem, m_centerAlignMenuItem, m_rightAlignMenuItem};
        MenuItem activeMenuItem;
        switch (swtAlignment) {
            case SWT.LEFT:
                activeMenuItem = m_leftAlignMenuItem;
                break;
            case SWT.CENTER:
                activeMenuItem = m_centerAlignMenuItem;
                break;
            case SWT.RIGHT:
                activeMenuItem = m_rightAlignMenuItem;
                break;
            default:
                LOGGER.coding("Invalid alignment (ignored): " + swtAlignment);
                return;
        }
        for (MenuItem m : alignmentMenuItems) {
            m.setSelection(m == activeMenuItem);
        }
    }

    private void setSWTStyle(final int swtStyle) {
        List<StyleRange> styles = getStylesInSelection();
        boolean setAttr = true;
        for (StyleRange s : styles) {
            if (s.font != null && (s.font.getFontData()[0].getStyle() & swtStyle) != 0) {
                setAttr = false;
                break;
            }
        }
        for (StyleRange s : styles) {
            if (setAttr) {
                s.font = FontStore.INSTANCE.addStyleToFont(s.font, swtStyle);
            } else {
                s.font =FontStore.INSTANCE.removeStyleFromFont(s.font, swtStyle);
            }
            m_styledText.setStyleRange(s);
        }
    }

    /**
     * Returns a list of ordered styles in the selected range. For regions in the selection that do not have a style
     * yet, it inserts a new (empty) style. The styles are ordered and not overlapping. If there is no selection in the
     * control, an empty list is returned, never null. Contained styles should be applied individually (after possible
     * modification) with setStyleRange().
     *
     * @return styles for the entire selected range, ordered and not overlapping. Empty list, if no selection exists,
     *         never null.
     */
    private List<StyleRange> getStylesInSelection() {
        int[] sel = m_styledText.getSelectionRanges();
        if (sel == null || sel.length != 2) {
            return Collections.emptyList();
        }
        int start = sel[0];
        int length = sel[1];
        StyleRange[] styles = m_styledText.getStyleRanges(start, length);
        if (styles == null || styles.length == 0) {
            // no existing styles in selection
            StyleRange newStyle = new StyleRange();
            newStyle.font = m_styledText.getFont();
            newStyle.start = start;
            newStyle.length = length;
            return Collections.singletonList(newStyle);
        } else {
            LinkedList<StyleRange> result = new LinkedList<StyleRange>();
            int lastEnd = start; // not yet covered index
            for (StyleRange s : styles) {
                if (s.start < lastEnd) {
                    LOGGER.error("StyleRanges not ordered! Style might be messed up");
                }
                if (lastEnd < s.start) {
                    // create style for range not covered by next exiting style
                    StyleRange newRange = new StyleRange();
                    newRange.font = m_styledText.getFont();
                    newRange.start = lastEnd;
                    newRange.length = s.start - lastEnd;
                    lastEnd = s.start;
                    result.add(newRange);
                }
                result.add(s);
                lastEnd = s.start + s.length;
            }
            if (lastEnd < start + length) {
                // create new style for the part at the end, not covered
                StyleRange newRange = new StyleRange();
                newRange.font = m_styledText.getFont();
                newRange.start = lastEnd;
                newRange.length = start + length - lastEnd;
                result.add(newRange);
            }
            return result;
        }
    }

    private void italic() {
        setSWTStyle(SWT.ITALIC);
    }

    private void bgColor() {
        ColorDialog colDlg = new ColorDialog(m_styledText.getShell());
        RGB[] toSet = lastColors == null ? DEFAULT_COLORS : lastColors;
        colDlg.setText("Change the Background Color");
        colDlg.setRGBs(toSet);
        if (m_backgroundColor != null) {
            colDlg.setRGB(m_backgroundColor.getRGB());
        }
        RGB newBGCol = colDlg.open();
        if (newBGCol == null) {
            // user canceled
            return;
        }
        lastColors = colDlg.getRGBs();
        m_backgroundColor = new Color(null, newBGCol);
        applyBackgroundColor();
    }


    private void borderStyle() {
        BorderStyleDialog dlg = new BorderStyleDialog(m_styledText.getShell(), m_styledText.getMarginColor(),
            m_styledText.getRightMargin());
        if (dlg.open() == Window.OK) {
            m_styledText.setMarginColor(AnnotationEditPart.RGBtoColor(dlg.getColor()));
            m_styledText.redraw();
            int s = dlg.getSize();
            m_styledText.setMargins(s, s, s, s);
        }
    }

    /**
     * @return
     */
    private static RGB fromHex(final String hex) {
        int color = Integer.parseInt(hex, 16);
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        return new RGB(r, g, b);
    }

    /**
     * Change alignment.
     *
     * @param alignment SWT.LEFT|CENTER|RIGHT.
     */
    private void alignment(final int alignment) {
        int newAlignment;
        switch (alignment) {
            case SWT.CENTER:
                newAlignment = alignment;
                break;
            case SWT.RIGHT:
                newAlignment = alignment;
                break;
            default:
                newAlignment = SWT.LEFT;
        }
        checkSelectionOfAlignmentMenuItems(newAlignment);
        m_styledText.setAlignment(newAlignment);
    }

    private void fontColor() {
        Color col = AnnotationEditPart.getAnnotationDefaultForegroundColor();
        List<StyleRange> sel = getStylesInSelection();
        // set the color of the first selection style
        for (StyleRange style : sel) {
            if (style.foreground != null) {
                col = style.foreground;
                break;
            }
        }
        ColorDialog colDlg = new ColorDialog(m_styledText.getShell());
        colDlg.setText("Change Font Color in Selection");
        colDlg.setRGB(col.getRGB());
        RGB newRGB = colDlg.open();
        if (newRGB == null) {
            // user canceled
            return;
        }
        Color newCol = AnnotationEditPart.RGBtoColor(newRGB);
        for (StyleRange style : sel) {
            style.foreground = newCol;
            m_styledText.setStyleRange(style);
        }
    }

    private void font() {
        List<StyleRange> sel = getStylesInSelection();
        Font f = m_styledText.getFont();
        Color c = null;
        // set the first font style in the selection
        for (StyleRange style : sel) {
            if (style.font != null) {
                f = style.font;
                c = style.foreground;
                break;
            }
        }
        FontData fd = f.getFontData()[0];
        FontStyleDialog dlg = new FontStyleDialog(m_styledText.getShell(), c, fd.getHeight(),
            (fd.getStyle() & SWT.BOLD) != 0, (fd.getStyle() & SWT.ITALIC) != 0);
        m_allowFocusLost.set(false);
        try {
            if (dlg.open() != Window.OK) {
                // user canceled.
                return;
            }
        } finally {
            m_allowFocusLost.set(true);
        }
        RGB newRGB = dlg.getColor();
        Integer newSize = dlg.getSize();
        Boolean newBold = dlg.getBold();
        Boolean newItalic = dlg.getItalic();
        Color newCol = newRGB == null ? null : AnnotationEditPart.RGBtoColor(newRGB);
        for (StyleRange style : sel) {
            if (newSize != null || newBold != null || newItalic != null) {
                FontData stylefd = style.font.getFontData()[0];
                boolean b = (stylefd.getStyle() & SWT.BOLD) != 0;
                if (newBold != null) {
                    b = newBold.booleanValue();
                }
                boolean i = (stylefd.getStyle() & SWT.ITALIC) != 0;
                if (newItalic != null) {
                    i = newItalic.booleanValue();
                }
                int s = stylefd.getHeight();
                if (newSize != null) {
                    s = newSize.intValue();
                }
                style.font = FontStore.INSTANCE.getDefaultFont(s, b, i);
            }
            if (newCol != null) {
                style.foreground = newCol;
            }
            m_styledText.setStyleRange(style);
        }
    }

    private void ok() {
        fireApplyEditorValue();
        deactivate();
        return;
    }

    private void cancel() {
        fireCancelEditor();
        deactivate();
        return;
    }

    /**
     * @return the bounds needed to display the current text
     */
    Rectangle getTextBounds() {
        // use the shadow instance to get the size of the not auto-wrapped text
        int charCount = m_shadowStyledText.getCharCount();
        if (charCount < 1) {
            Rectangle b = m_shadowStyledText.getBounds();
            return new Rectangle(b.x, b.y, 0, 0);
        } else {
            Rectangle r = m_shadowStyledText.getTextBounds(0, charCount - 1);
            if (m_shadowStyledText.getText(charCount - 1, charCount - 1).charAt(0) == '\n') {
                r.height += m_shadowStyledText.getLineHeight();
            }
            return r;
        }
    }

}
