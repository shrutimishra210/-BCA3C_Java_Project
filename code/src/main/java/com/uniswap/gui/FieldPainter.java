package com.uniswap.gui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Shared painting for the rounded text inputs (background, border, placeholder). */
final class FieldPainter {

    private FieldPainter() {
    }

    static void paintBackground(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = c.getWidth();
        int h = c.getHeight();
        g2.setColor(c.isEnabled() ? Color.WHITE : UiTheme.NEUTRAL_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
        boolean focus = c.hasFocus() || (c instanceof JComboBox<?> cb && cb.isPopupVisible());
        g2.setColor(focus ? UiTheme.ACCENT : UiTheme.BORDER_INPUT);
        g2.setStroke(new BasicStroke(focus ? 1.6f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.8f, 0.8f, w - 1.6f, h - 1.6f, 12, 12));
        g2.dispose();
    }

    static void paintPlaceholder(Graphics g, JTextComponent c, String placeholder) {
        if (placeholder == null || placeholder.isEmpty() || c.getDocument().getLength() > 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(UiTheme.mix(UiTheme.CHARCOAL_OLIVE, UiTheme.WHITE, 0.55));
        g2.setFont(c.getFont());
        Insets in = c.getInsets();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(placeholder, in.left, (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
