package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;

/** Page title + subtitle (left) with an optional action button (right). Same on every screen. */
public final class PageHeader {

    private PageHeader() {
    }

    public static JPanel build(String title, String subtitle, JComponent action) {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = UiTheme.title(title);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(t);
        if (subtitle != null) {
            JLabel s = UiTheme.label(subtitle, UiTheme.BODY, UiTheme.BROWN_SOFT);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(Box.createVerticalStrut(4));
            text.add(s);
        }
        p.add(text, BorderLayout.CENTER);
        if (action != null) {
            JPanel a = new JPanel(new GridBagLayout());
            a.setOpaque(false);
            a.add(action);
            p.add(a, BorderLayout.EAST);
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 4));
        return p;
    }

    /** Standard outer padding of every page. */
    public static JPanel page() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UiTheme.BG);
        p.setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, 0, UiTheme.PAGE_PAD - 6));
        return p;
    }

    /** Empty-state block: big soft icon, headline, sentence, optional button. */
    public static JPanel emptyState(Icons.Kind icon, String headline, String text, JComponent action) {
        RoundedPanel card = new RoundedPanel(24, 0).fill(Color.WHITE);
        card.pad(40, 30, 40, 30);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JComponent ic = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(64, 64);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.PEACH_LIGHT);
                g2.fillOval(0, 0, 63, 63);
                Icons.paint(g2, icon, 17, 17, 30, UiTheme.ACCENT_DARK, false);
                g2.dispose();
            }
        };
        ic.setAlignmentX(Component.CENTER_ALIGNMENT);
        ic.setMaximumSize(new Dimension(64, 64));
        JLabel h = UiTheme.label(headline, UiTheme.font(Font.BOLD, 18), UiTheme.BROWN);
        h.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel t = UiTheme.label(text, UiTheme.BODY, UiTheme.BROWN_SOFT);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(ic);
        card.add(Box.createVerticalStrut(16));
        card.add(h);
        card.add(Box.createVerticalStrut(6));
        card.add(t);
        if (action != null) {
            action.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createVerticalStrut(20));
            card.add(action);
        }
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    /** Small stat card: drawn icon in a peach circle, big number, caption. */
    public static RoundedPanel statCard(Icons.Kind icon, String number, String caption) {
        RoundedPanel card = UiTheme.card();
        card.pad(18, 20, 18, 20);
        card.setLayout(new BorderLayout(16, 0));
        JComponent ic = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(52, 52);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.PEACH_LIGHT);
                g2.fillRoundRect(0, 0, 51, 51, 18, 18);
                Icons.paint(g2, icon, 14, 14, 24, UiTheme.ACCENT_DARK, false);
                g2.dispose();
            }
        };
        JPanel wrapIc = new JPanel(new GridBagLayout());
        wrapIc.setOpaque(false);
        wrapIc.add(ic);
        card.add(wrapIc, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel n = UiTheme.label(number, UiTheme.STAT, UiTheme.BROWN);
        JLabel c = UiTheme.muted(caption);
        text.add(n);
        text.add(c);
        card.add(text, BorderLayout.CENTER);
        card.enableHover();
        return card;
    }
}
