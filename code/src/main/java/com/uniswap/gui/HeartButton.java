package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/** Round favourite button drawn as a heart (outline = not saved, filled = saved). */
public class HeartButton extends JButton {

    private boolean favourite;

    public HeartButton(boolean favourite) {
        this.favourite = favourite;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateTip();
    }

    public void setFavourite(boolean f) {
        this.favourite = f;
        updateTip();
        repaint();
    }

    public boolean isFavourite() {
        return favourite;
    }

    private void updateTip() {
        setToolTipText(favourite ? "Remove from favourites" : "Add to favourites");
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(36, 36);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hover = getModel().isRollover();
        int s = Math.min(getWidth(), getHeight());
        g2.setColor(new Color(UiTheme.CHARCOAL_OLIVE.getRed(), UiTheme.CHARCOAL_OLIVE.getGreen(), UiTheme.CHARCOAL_OLIVE.getBlue(), 25));
        g2.fill(new Ellipse2D.Float(1, 2, s - 2, s - 2));
        g2.setColor(hover ? UiTheme.PEACH_HOVER : Color.WHITE);
        g2.fill(new Ellipse2D.Float(0, 0, s - 1, s - 1));
        Color c = favourite ? UiTheme.ACCENT_DARK : hover ? UiTheme.ACCENT : UiTheme.BROWN_SOFT;
        Icons.paint(g2, Icons.Kind.HEART, (s - 18) / 2f, (s - 17) / 2f, 18, c, favourite);
        g2.dispose();
    }
}
