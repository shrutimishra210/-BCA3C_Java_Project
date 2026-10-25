package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/** Small pill label used for statuses ("Pending", "Accepted"), categories and types. */
public class Badge extends JComponent {

    private final String text;
    private final Color fg;
    private final Color bg;
    private final boolean dot;

    public Badge(String text, Color fg, Color bg, boolean dot) {
        this.text = text;
        this.fg = fg;
        this.bg = bg;
        this.dot = dot;
        setFont(UiTheme.font(Font.BOLD, 11));
        setOpaque(false);
    }

    /** Status pill: Pending = amber, Accepted/Completed/Active = green, Rejected = red, etc. */
    public static Badge status(String status) {
        String s = status == null ? "" : status.toUpperCase();
        String label = s.isEmpty() ? "" : s.charAt(0) + s.substring(1).toLowerCase();
        return switch (s) {
            case "ACTIVE", "ACCEPTED", "COMPLETED" -> new Badge(label, UiTheme.SUCCESS, UiTheme.SUCCESS_BG, true);
            case "PENDING" -> new Badge(label, UiTheme.WARN, UiTheme.WARN_BG, true);
            case "REJECTED" -> new Badge(label, UiTheme.DANGER, UiTheme.DANGER_BG, true);
            case "CANCELLED", "INACTIVE" -> new Badge(label, UiTheme.NEUTRAL, UiTheme.NEUTRAL_BG, true);
            default -> new Badge(label, UiTheme.ACCENT, UiTheme.PEACH_LIGHT, true); // sold / exchanged / borrowed / donated
        };
    }

    /** Category / info pill in soft olive. */
    public static Badge category(String text) {
        return new Badge(text, UiTheme.BROWN, UiTheme.PEACH_LIGHT, false);
    }

    /** Transaction type pill (SELL / EXCHANGE / BORROW / DONATE). */
    public static Badge type(String text) {
        return new Badge(text, UiTheme.ACCENT, Color.WHITE, false);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(text) + 20 + (dot ? 12 : 0);
        return new Dimension(w, 22);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = 10;
        if (dot) {
            g2.setColor(fg);
            g2.fill(new Ellipse2D.Float(10, h / 2f - 3, 6, 6));
            x = 20;
        }
        g2.setColor(fg);
        g2.drawString(text, x, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
