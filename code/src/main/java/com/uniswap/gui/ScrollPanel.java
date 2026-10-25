package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel to use as the view of a JScrollPane: it always matches the width of
 * the viewport (so cards stretch and wrap properly when the window is resized)
 * and only scrolls vertically.
 */
public class ScrollPanel extends JPanel implements Scrollable {

    public ScrollPanel() {
        setOpaque(false);
    }

    /** Vertical stack (BoxLayout) - the common case for list screens. */
    public static ScrollPanel column() {
        ScrollPanel p = new ScrollPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(60, visibleRect.height - 60);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
