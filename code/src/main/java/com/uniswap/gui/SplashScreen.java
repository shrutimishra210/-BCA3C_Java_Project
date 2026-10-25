package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Screen 1: short branded splash shown while the app opens. */
public class SplashScreen extends JWindow {

    private final Timer animation;
    private float progress = 0f;

    public SplashScreen() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UiTheme.BG, getWidth(), getHeight(), UiTheme.PEACH_LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UiTheme.PEACH);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 28, 28));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(480, 300));

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        LogoMark logo = new LogoMark(72);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = UiTheme.label("SwapUni", UiTheme.font(Font.BOLD, 40), UiTheme.BROWN);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tag = UiTheme.label("Your Campus. Your Marketplace.", UiTheme.font(Font.PLAIN, 14), UiTheme.BROWN_SOFT);
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel loading = UiTheme.label("Starting backend and connecting to MySQL...", UiTheme.SMALL, UiTheme.BROWN_SOFT);
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComponent bar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                g2.setColor(UiTheme.BORDER);
                g2.fillRoundRect(0, 0, w, 6, 6, 6);
                int seg = (int) (w * 0.4f);
                int x = (int) ((w + seg) * progress) - seg;
                g2.setClip(0, 0, w, 6);
                g2.setColor(UiTheme.ACCENT);
                g2.fillRoundRect(x, 0, seg, 6, 6, 6);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(200, 6));
        bar.setMaximumSize(new Dimension(200, 6));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);

        col.add(logo);
        col.add(Box.createVerticalStrut(16));
        col.add(name);
        col.add(Box.createVerticalStrut(4));
        col.add(tag);
        col.add(Box.createVerticalStrut(30));
        col.add(bar);
        col.add(Box.createVerticalStrut(12));
        col.add(loading);
        panel.add(col);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        try {
            setShape(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 28, 28));
        } catch (Exception ignored) {
            // rounded window shape isn't supported everywhere - a plain rectangle is fine
        }

        // subtle sliding loading bar
        animation = new Timer(30, e -> {
            progress += 0.018f;
            if (progress > 1f) progress = 0f;
            bar.repaint();
        });
    }

    /** Show the splash for a moment, then run the given callback and close it. */
    public void showThenRun(Runnable next) {
        setVisible(true);
        animation.start();
        Timer timer = new Timer(1400, e -> {
            animation.stop();
            setVisible(false);
            dispose();
            next.run();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
