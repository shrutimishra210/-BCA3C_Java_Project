package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Responsive grid for product cards: as the window gets wider more cards fit
 * on a row, as it gets narrower they wrap - every card keeps the same height
 * and the row always fills the full width.
 */
public class CardGrid extends JPanel {

    private final int minCardWidth;
    private final int gap;
    private final int cardHeight;
    private int lastCols = -1;

    public CardGrid(int minCardWidth, int gap, int cardHeight) {
        this.minCardWidth = minCardWidth;
        this.gap = gap;
        this.cardHeight = cardHeight;
        setOpaque(false);
        setLayout(new GridLayoutManager());
    }

    private int columns(int availableWidth) {
        return Math.max(1, (availableWidth + gap) / (minCardWidth + gap));
    }

    private class GridLayoutManager implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int width = parent.getWidth() > 0 ? parent.getWidth() : minCardWidth * 3 + gap * 2;
            int cols = columns(width - in.left - in.right);
            int n = parent.getComponentCount();
            int rows = (n + cols - 1) / cols;
            int h = in.top + in.bottom + rows * cardHeight + Math.max(0, rows - 1) * gap;
            return new Dimension(width, h);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(minCardWidth, cardHeight);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int avail = parent.getWidth() - in.left - in.right;
            int cols = columns(avail);
            int cw = (avail - gap * (cols - 1)) / cols;
            int n = parent.getComponentCount();
            // The preferred height depends on the column count, which depends on the
            // width we were just given. If it changed, ask for a fresh layout pass so
            // the scroll area gets the right height (no clipped / blank rows on resize).
            if (cols != lastCols) {
                lastCols = cols;
                SwingUtilities.invokeLater(() -> {
                    parent.invalidate();
                    Container up = parent.getParent();
                    if (up != null) up.revalidate();
                });
            }
            for (int i = 0; i < n; i++) {
                int r = i / cols;
                int c = i % cols;
                parent.getComponent(i).setBounds(in.left + c * (cw + gap), in.top + r * (cardHeight + gap), cw, cardHeight);
            }
        }
    }
}
