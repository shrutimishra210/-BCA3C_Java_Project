package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern flat button with rounded corners, a hover effect and a hand cursor.
 * Replaces the default Swing look. It is still a normal JButton, so
 * addActionListener(), setEnabled(), keyboard focus (Tab / Space) and
 * getRootPane().setDefaultButton(...) all work exactly as before.
 */
public class RoundedButton extends JButton {

    public enum Style {
        /** Terracotta - default call to action. */
        PRIMARY,
        /** Cherry burgundy - main action of a page. */
        ACCENT,
        /** White with subtle border. */
        SECONDARY,
        /** Soft red. */
        DANGER,
        /** Sidebar navigation item. */
        NAV,
        /** Small category filter chip. */
        CHIP,
        /** Selected category filter chip. */
        CHIP_ACTIVE
    }

    private final Style style;
    private Icons.Kind iconKind;
    private boolean active = false;         // used by NAV
    private boolean dangerText = false;     // NAV variant for "Log Out"
    private int arc = 12;

    public RoundedButton(String text, Style style) {
        super(text);
        this.style = style == Style.CHIP_ACTIVE ? Style.CHIP : style;
        if (style == Style.CHIP_ACTIVE) this.active = true;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(style == Style.CHIP || style == Style.CHIP_ACTIVE ? UiTheme.font(Font.BOLD, 12) : UiTheme.BUTTON);
        if (style == Style.CHIP || style == Style.CHIP_ACTIVE) {
            arc = 999;
        }
        if (style == Style.NAV) {
            setHorizontalAlignment(SwingConstants.LEFT);
        }
    }

    public RoundedButton icon(Icons.Kind kind) {
        this.iconKind = kind;
        return this;
    }

    public RoundedButton arc(int arc) {
        this.arc = arc;
        return this;
    }

    /** NAV buttons: mark as the currently selected page. */
    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public RoundedButton dangerText() {
        this.dangerText = true;
        return this;
    }

    private int hPad() {
        return switch (style) {
            case NAV -> 16;
            case CHIP, CHIP_ACTIVE -> 14;
            default -> 20;
        };
    }

    private int vPad() {
        return switch (style) {
            case NAV -> 12;
            case CHIP, CHIP_ACTIVE -> 6;
            default -> 10;
        };
    }

    private int iconSize() {
        return style == Style.NAV ? 20 : 16;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(getText() == null ? "" : getText()) + hPad() * 2;
        if (iconKind != null) w += iconSize() + 10;
        int h = fm.getHeight() + vPad() * 2;
        return new Dimension(w, Math.max(h, style == Style.NAV ? 44 : 36));
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

        boolean hover = getModel().isRollover();
        boolean pressed = getModel().isPressed();
        boolean enabled = isEnabled();

        Color bg;
        Color fg;
        Color border = null;
        Color iconColor;

        switch (style) {
            case PRIMARY -> {
                bg = pressed ? UiTheme.mix(UiTheme.TERRACOTTA, UiTheme.CHARCOAL_OLIVE, 0.2)
                        : hover ? UiTheme.mix(UiTheme.TERRACOTTA, UiTheme.WHITE, 0.14) : UiTheme.PEACH;
                fg = UiTheme.BROWN;
                iconColor = fg;
            }
            case ACCENT -> {
                bg = pressed ? UiTheme.mix(UiTheme.CHERRY_BURGUNDY, UiTheme.CHARCOAL_OLIVE, 0.45)
                        : hover ? UiTheme.MAROON_HOVER : UiTheme.ACCENT;
                fg = Color.WHITE;
                iconColor = fg;
            }
            case SECONDARY -> {
                bg = pressed ? UiTheme.PEACH_LIGHT : hover ? UiTheme.PEACH_HOVER : Color.WHITE;
                fg = UiTheme.BROWN;
                border = hover ? UiTheme.OLIVE : UiTheme.BORDER_INPUT;
                iconColor = fg;
            }
            case DANGER -> {
                bg = pressed ? new Color(0xF2CCC7) : hover ? new Color(0xF5D6D2) : UiTheme.DANGER_BG;
                fg = UiTheme.DANGER;
                border = new Color(0xEFC6C1);
                iconColor = fg;
            }
            case NAV -> {
                // lives on the dark charcoal sidebar: burgundy when active, light text
                bg = active ? UiTheme.CHERRY_BURGUNDY : hover ? UiTheme.SIDEBAR_HOVER : new Color(0, 0, 0, 0);
                fg = dangerText ? UiTheme.TERRACOTTA : active ? UiTheme.WHITE : UiTheme.SIDEBAR_TEXT;
                iconColor = dangerText ? UiTheme.TERRACOTTA : active ? UiTheme.WHITE : UiTheme.SIDEBAR_MUTED;
            }
            case CHIP -> {
                bg = active ? UiTheme.OLIVE : hover ? UiTheme.PEACH_LIGHT : UiTheme.PEACH_HOVER;
                fg = active ? UiTheme.WHITE : UiTheme.BROWN;
                border = active ? null : UiTheme.BORDER;
                iconColor = fg;
            }
            default -> { // CHIP_ACTIVE
                bg = UiTheme.OLIVE;
                fg = UiTheme.WHITE;
                iconColor = fg;
            }
        }

        if (!enabled) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        }

        int w = getWidth();
        int h = getHeight();
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, Math.min(arc, h), Math.min(arc, h)));
        if (border != null) {
            g2.setColor(border);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, Math.min(arc, h), Math.min(arc, h)));
        }

        // keyboard focus ring
        if (isFocusOwner() && enabled) {
            g2.setColor(style == Style.NAV ? UiTheme.TERRACOTTA : UiTheme.ACCENT);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, Math.min(arc, h), Math.min(arc, h)));
        }

        // content: [icon] text
        g2.setFont(style == Style.NAV && active ? UiTheme.font(Font.BOLD, 13) : getFont());
        FontMetrics fm = g2.getFontMetrics();
        String text = getText() == null ? "" : getText();
        int textW = fm.stringWidth(text);
        int isz = iconKind != null ? iconSize() : 0;
        int gap = iconKind != null && !text.isEmpty() ? 10 : 0;
        int contentW = isz + gap + textW;

        int startX;
        if (getHorizontalAlignment() == SwingConstants.LEFT) {
            startX = hPad();
        } else {
            startX = (w - contentW) / 2;
        }

        if (iconKind != null) {
            Icons.paint(g2, iconKind, startX, (h - isz) / 2f, isz, iconColor, false);
        }
        g2.setColor(fg);
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, startX + isz + gap, ty);
        g2.dispose();
    }
}
