package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** The SwapUni logo mark: a rounded peach square with a "swap" arrows icon. */
public class LogoMark extends JComponent {

    private final int size;
    private double angle = 0;           // current spin, radians
    private javax.swing.Timer spinTimer;

    public LogoMark(int size) {
        this.size = size;
        setOpaque(false);
    }

    /** Spins the mark one full turn (used by the login screen's click action). */
    public void spin() {
        if (spinTimer != null && spinTimer.isRunning()) return;
        final long start = System.nanoTime();
        spinTimer = new javax.swing.Timer(16, null);
        spinTimer.addActionListener(e -> {
            double t = Math.min(1.0, (System.nanoTime() - start) / 700_000_000.0);
            double eased = 1 - Math.pow(1 - t, 3);                 // ease-out
            angle = eased * Math.PI * 2;
            repaint();
            if (t >= 1.0) {
                angle = 0;
                spinTimer.stop();
            }
        });
        spinTimer.start();
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
        g2.rotate(angle, size / 2.0, size / 2.0);
        g2.setPaint(new GradientPaint(0, 0, UiTheme.TERRACOTTA, size, size, UiTheme.CHERRY_BURGUNDY));
        g2.fill(new RoundRectangle2D.Float(0, 0, size, size, size * 0.32f, size * 0.32f));
        float pad = size * 0.2f;
        Icons.paint(g2, Icons.Kind.SWAP, pad, pad, size - pad * 2, Color.WHITE, false);
        g2.dispose();
    }
}
