package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Common shell for SwapUni form dialogs: cream background, a heading, and a
 * scrollable content column. Subclasses add their fields to {@link #content}.
 */
public class BaseDialog extends JDialog {

    protected final ScrollPanel content = ScrollPanel.column();
    private final JPanel root = new JPanel(new BorderLayout());

    public BaseDialog(Window owner, String title, int width, int height, String heading, String subtitle) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        root.setBackground(UiTheme.BG);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(UiTheme.pad(24, 28, 6, 28));
        JLabel h = UiTheme.label(heading, UiTheme.font(Font.BOLD, 22), UiTheme.BROWN);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(h);
        if (subtitle != null) {
            JLabel s = UiTheme.muted(subtitle);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(Box.createVerticalStrut(4));
            header.add(s);
        }
        root.add(header, BorderLayout.NORTH);

        content.setBorder(UiTheme.pad(12, 28, 24, 28));
        JScrollPane sp = UiTheme.styleScroll(new JScrollPane(content));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setBackground(UiTheme.BG);
        sp.getViewport().setOpaque(true);
        root.add(sp, BorderLayout.CENTER);

        setContentPane(root);
        setSize(width, height);
        setLocationRelativeTo(owner);
        getRootPane().registerKeyboardAction((ActionEvent e) -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Fixed footer (buttons) that stays visible while the content scrolls. */
    protected void setFooter(JComponent footer) {
        footer.setBorder(UiTheme.pad(12, 28, 18, 28));
        root.add(footer, BorderLayout.SOUTH);
    }
}
