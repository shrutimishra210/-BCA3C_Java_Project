package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;

/**
 * Styled replacements for JOptionPane message boxes, so pop-ups follow the
 * SwapUni look (cream background, coloured icon, rounded buttons).
 * Same behaviour as before: info/warn/error just show a message,
 * confirm() returns true only when the user picks the confirm button.
 */
public final class Dialogs {

    private enum Kind { INFO, WARN, ERROR, CONFIRM }

    private Dialogs() {
    }

    public static void info(Component parent, String message, String title) {
        show(parent, Kind.INFO, message, title, "OK", null, false);
    }

    public static void warn(Component parent, String message, String title) {
        show(parent, Kind.WARN, message, title, "OK", null, false);
    }

    public static void error(Component parent, String message, String title) {
        show(parent, Kind.ERROR, message, title, "OK", null, false);
    }

    /** Returns true if the user clicked the confirm button. */
    public static boolean confirm(Component parent, String message, String title, String confirmLabel, boolean danger) {
        return show(parent, Kind.CONFIRM, message, title, confirmLabel, "Cancel", danger);
    }

    private static boolean show(Component parent, Kind kind, String message, String title,
                                String yesLabel, String noLabel, boolean danger) {
        Window owner = parent == null ? null
                : parent instanceof Window w ? w : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        boolean[] result = {false};

        JPanel root = new JPanel(new BorderLayout(18, 0));
        root.setBackground(UiTheme.BG);
        root.setBorder(UiTheme.pad(26, 26, 20, 26));

        root.add(new IconCircle(kind), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel heading = UiTheme.label(title, UiTheme.font(Font.BOLD, 16), UiTheme.BROWN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel msg = new JLabel("<html><div style='width:290px'>" + escape(message) + "</div></html>");
        msg.setFont(UiTheme.BODY);
        msg.setForeground(UiTheme.BROWN_SOFT);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(heading);
        text.add(Box.createVerticalStrut(6));
        text.add(msg);
        root.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(UiTheme.pad(20, 0, 0, 0));
        RoundedButton yes = danger ? UiTheme.dangerButton(yesLabel) : UiTheme.primaryButton(yesLabel);
        yes.addActionListener(e -> {
            result[0] = true;
            d.dispose();
        });
        if (noLabel != null) {
            RoundedButton no = UiTheme.secondaryButton(noLabel);
            no.addActionListener(e -> d.dispose());
            buttons.add(no);
        }
        buttons.add(yes);
        root.add(buttons, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.getRootPane().setDefaultButton(yes);
        d.getRootPane().registerKeyboardAction((ActionEvent e) -> d.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.setResizable(false);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        return result[0];
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    /** Coloured circle with a simple glyph, drawn (no emoji fonts needed). */
    private static class IconCircle extends JComponent {
        private final Kind kind;

        IconCircle(Kind kind) {
            this.kind = kind;
            setPreferredSize(new Dimension(48, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color bg;
            Color fg;
            String glyph = null;
            switch (kind) {
                case INFO -> { bg = UiTheme.SUCCESS_BG; fg = UiTheme.SUCCESS; }
                case WARN -> { bg = UiTheme.WARN_BG; fg = UiTheme.WARN; glyph = "!"; }
                case ERROR -> { bg = UiTheme.DANGER_BG; fg = UiTheme.DANGER; glyph = "!"; }
                default -> { bg = UiTheme.PEACH_LIGHT; fg = UiTheme.ACCENT_DARK; glyph = "?"; }
            }
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Float(0, 0, 47, 47));
            if (glyph == null) {
                Icons.paint(g2, Icons.Kind.CHECK, 12, 12, 24, fg, false);
            } else {
                g2.setColor(fg);
                g2.setFont(UiTheme.font(Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(glyph, (48 - fm.stringWidth(glyph)) / 2f, (48 - fm.getHeight()) / 2f + fm.getAscent());
            }
            g2.dispose();
        }
    }
}
