package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/** Round avatar with the user's initials on a peach gradient (no photos are stored, so this is the profile picture). */
public class Avatar extends JComponent {

    private final int size;
    private final String initials;

    public Avatar(String fullName, int size) {
        this.size = size;
        this.initials = initialsOf(fullName);
        setOpaque(false);
    }

    public static String initialsOf(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        String r = String.valueOf(parts[0].charAt(0));
        if (parts.length > 1) r += parts[parts.length - 1].charAt(0);
        return r.toUpperCase();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, UiTheme.PEACH, size, size, UiTheme.ACCENT));
        g2.fill(new Ellipse2D.Float(0, 0, size, size));
        g2.setColor(Color.WHITE);
        g2.setFont(UiTheme.font(Font.BOLD, Math.max(11, size * 38 / 100)));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initials, (size - fm.stringWidth(initials)) / 2f, (size - fm.getHeight()) / 2f + fm.getAscent());
        g2.dispose();
    }
}
