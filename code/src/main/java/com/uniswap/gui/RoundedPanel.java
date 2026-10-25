package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A panel with rounded corners, a soft border and an optional very subtle
 * shadow. Used for every card in the app so they all look the same.
 *
 * The shadow is painted INSIDE the panel's bounds (in the padding area), so
 * layouts don't need any special handling - just add it like a normal panel.
 */
public class RoundedPanel extends JPanel {

    private int arc;
    private int shadow;
    private Color fill = UiTheme.CARD;
    private Color borderColor = UiTheme.BORDER;
    private Color hoverBorderColor = UiTheme.OLIVE;
    private boolean hoverEnabled = false;
    private boolean hover = false;
    private int padTop = 0, padLeft = 0, padBottom = 0, padRight = 0;

    public RoundedPanel() {
        this(20, 0);
    }

    public RoundedPanel(int arc, int shadow) {
        this.arc = arc;
        this.shadow = shadow;
        setOpaque(false);
        applyBorder();
    }

    public RoundedPanel fill(Color c) {
        this.fill = c;
        return this;
    }

    public RoundedPanel borderColor(Color c) {
        this.borderColor = c;
        return this;
    }

    /** Same padding on all four sides (in addition to the shadow space). */
    public RoundedPanel pad(int all) {
        return pad(all, all, all, all);
    }

    public RoundedPanel pad(int top, int left, int bottom, int right) {
        padTop = top;
        padLeft = left;
        padBottom = bottom;
        padRight = right;
        applyBorder();
        return this;
    }

    private void applyBorder() {
        setBorder(BorderFactory.createEmptyBorder(
                padTop + shadow, padLeft + shadow, padBottom + shadow, padRight + shadow));
    }

    /**
     * Turns on the hover effect (border turns peach, shadow gets a touch
     * stronger). Call this AFTER all children have been added, because the
     * listener is attached to the panel and each of its children so the
     * effect doesn't flicker when the mouse moves over a button inside it.
     */
    public RoundedPanel enableHover() {
        hoverEnabled = true;
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                update(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                update(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                update(e);
            }

            private void update(MouseEvent e) {
                Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), RoundedPanel.this);
                boolean inside = RoundedPanel.this.contains(p);
                if (inside != hover) {
                    hover = inside;
                    repaint();
                }
            }
        };
        attach(this, ma);
        return this;
    }

    private void attach(Component c, MouseAdapter ma) {
        c.addMouseListener(ma);
        c.addMouseMotionListener(ma);
        if (c instanceof Container cont) {
            for (Component child : cont.getComponents()) {
                attach(child, ma);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int x = shadow;
        int y = shadow;
        int cw = w - shadow * 2;
        int ch = h - shadow * 2;

        // very soft, warm-tinted shadow: a few translucent layers
        for (int i = shadow; i >= 1; i--) {
            float t = 1f - (i / (float) (shadow + 1));
            int alpha = (int) ((hover ? 20 : 12) * t * t) + 1;
            g2.setColor(new Color(UiTheme.CHARCOAL_OLIVE.getRed(), UiTheme.CHARCOAL_OLIVE.getGreen(), UiTheme.CHARCOAL_OLIVE.getBlue(), Math.min(alpha, 255)));
            g2.fill(new RoundRectangle2D.Float(x - i, y - i + 2, cw + i * 2, ch + i * 2, arc + i * 2, arc + i * 2));
        }

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(x, y, cw, ch, arc, arc));

        g2.setColor(hover && hoverEnabled ? hoverBorderColor : borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, cw - 1, ch - 1, arc, arc));
        g2.dispose();
        super.paintComponent(g);
    }
}
