package com.uniswap.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SwapUni design system - the ONE place that decides how the app looks.
 *
 * Palette: SwapYuni tokens - burgundy, dark slate, sage, forest, terracotta, parchment, eggshell.
 * Every screen pulls its colours, fonts, buttons, cards and headings from
 * here (or from the small reusable components next to it: RoundedPanel,
 * RoundedButton, RoundedTextField, Badge, Icons ...), so the whole
 * application reads as one consistent, intentionally designed product.
 */
public final class UiTheme {

    // ---------------------------------------------------------------
    // Colours - SwapYuni design tokens. Defined ONCE here; every tint and
    // legacy name below is derived from them.
    // ---------------------------------------------------------------
    public static final Color CHERRY_BURGUNDY = new Color(0x68252F); // --color-primary-burgundy (buttons, accents)
    public static final Color MAROON_HOVER    = new Color(0x7A2D37); // --color-maroon-hover
    public static final Color CHARCOAL_OLIVE  = new Color(0x2D3231); // --bg-sidebar (dark slate)
    public static final Color OLIVE           = new Color(0x788277); // --color-sage
    public static final Color FOREST          = new Color(0x3C5042); // --color-forest
    public static final Color TERRACOTTA      = new Color(0xC3775E); // --color-terracotta
    public static final Color TAUPE           = new Color(0x65605C); // --color-gray-circle
    public static final Color WARM_CREAM      = new Color(0xEAE4DA); // --bg-hero (parchment page background)
    public static final Color EGGSHELL        = new Color(0xF8F4EE); // --bg-card
    public static final Color WHITE           = new Color(0xFFFFFF); // --bg-input

    public static final Color TEXT_HEADLINE   = new Color(0x2D2926);
    public static final Color TEXT_BODY       = new Color(0x58534E);
    public static final Color TEXT_MUTED      = new Color(0x8F9794);
    public static final Color TEXT_PLACEHOLDER = new Color(0x9B948C);
    public static final Color TEXT_LIGHT      = new Color(0xF5EBE6);

    /** Blend of a and b; t = 0 gives a, t = 1 gives b. */
    public static Color mix(Color a, Color b, double t) {
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    public static final Color BG          = WARM_CREAM;                              // main background (parchment)
    public static final Color PEACH       = TERRACOTTA;                              // warm fill (complementary buttons, highlights)
    public static final Color PEACH_LIGHT = mix(OLIVE, EGGSHELL, 0.68);              // soft selected / chip / icon-circle tint
    public static final Color PEACH_HOVER = mix(OLIVE, EGGSHELL, 0.88);              // very light hover tint
    public static final Color ACCENT      = CHERRY_BURGUNDY;                         // primary accent
    public static final Color ACCENT_DARK = MAROON_HOVER;                            // accent, hovered / emphasised text
    public static final Color BROWN       = TEXT_HEADLINE;                           // primary text
    public static final Color BROWN_SOFT  = TEXT_BODY;                               // secondary text
    public static final Color CARD        = EGGSHELL;
    public static final Color BORDER      = new Color(0xE2DBD2);                     // --border-divider
    public static final Color BORDER_INPUT = new Color(0xDFD8CE);                    // --border-input

    // Status colours: success uses forest / sage; warning / error keep their own
    // semantic hues so important messages stay unmistakable.
    public static final Color SUCCESS     = FOREST;
    public static final Color SUCCESS_BG  = mix(OLIVE, WHITE, 0.78);
    public static final Color WARN        = new Color(0x9A6A16);
    public static final Color WARN_BG     = new Color(0xFBEFD2);
    public static final Color DANGER      = new Color(0xB04E48);
    public static final Color DANGER_BG   = new Color(0xF9E3E0);
    public static final Color NEUTRAL     = TAUPE;
    public static final Color NEUTRAL_BG  = mix(TAUPE, EGGSHELL, 0.88);

    // Dark sidebar helpers
    public static final Color SIDEBAR_HOVER = mix(CHARCOAL_OLIVE, WHITE, 0.10);
    public static final Color SIDEBAR_TEXT  = TEXT_LIGHT;
    public static final Color SIDEBAR_MUTED = TEXT_MUTED;

    // ---------------------------------------------------------------
    // Typography
    // ---------------------------------------------------------------
    public static final String FONT_FAMILY = pickFamily();

    public static final Font TITLE      = serif(Font.BOLD, 30);  // page title (editorial serif)
    public static final Font HEADING    = font(Font.BOLD, 18);   // section heading
    public static final Font CARD_TITLE = font(Font.BOLD, 15);   // card title
    public static final Font BODY       = font(Font.PLAIN, 13);  // body text
    public static final Font BODY_BOLD  = font(Font.BOLD, 13);
    public static final Font SMALL      = font(Font.PLAIN, 12);  // secondary text
    public static final Font LABEL      = font(Font.BOLD, 12);   // form labels
    public static final Font BUTTON     = font(Font.BOLD, 13);
    public static final Font STAT       = font(Font.BOLD, 30);   // big numbers

    /** Rupee sign if the UI font can draw it, otherwise a safe "Rs." fallback. */
    public static final String RUPEE = BODY.canDisplay('\u20B9') ? "\u20B9" : "Rs.";

    // ---------------------------------------------------------------
    // Spacing (use these instead of magic numbers)
    // ---------------------------------------------------------------
    public static final int GAP_S = 8;
    public static final int GAP_M = 16;
    public static final int GAP_L = 24;
    public static final int PAGE_PAD = 32;

    private UiTheme() {
    }

    private static String pickFamily() {
        String[] preferred = {"Segoe UI", "Helvetica Neue", "SF Pro Text", "Roboto", "Inter", "Ubuntu"};
        try {
            Set<String> available = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String p : preferred) {
                if (available.contains(p)) return p;
            }
        } catch (Throwable ignored) {
            // fall through to the logical font
        }
        return "SansSerif";
    }

    /** Georgia-style serif for big editorial headings (falls back to the logical serif font). */
    public static Font serif(int style, int size) {
        try {
            for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                if (f.equals("Georgia")) return new Font("Georgia", style, size);
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return new Font("Serif", style, size);
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    // ---------------------------------------------------------------
    // Buttons
    // ---------------------------------------------------------------
    /** Terracotta button - the default call to action ("View Details", "Send Request"). */
    public static RoundedButton primaryButton(String text) {
        return new RoundedButton(text, RoundedButton.Style.PRIMARY);
    }

    /** Cherry-burgundy primary button - the main action on a page ("List an Item"). */
    public static RoundedButton accentButton(String text) {
        return new RoundedButton(text, RoundedButton.Style.ACCENT);
    }

    /** White / cream button with a subtle border ("Cancel", "Close"). */
    public static RoundedButton secondaryButton(String text) {
        return new RoundedButton(text, RoundedButton.Style.SECONDARY);
    }

    /** Soft red button ("Reject", "Delete"). */
    public static RoundedButton dangerButton(String text) {
        return new RoundedButton(text, RoundedButton.Style.DANGER);
    }

    // ---------------------------------------------------------------
    // Cards, headings, labels
    // ---------------------------------------------------------------
    /** White rounded card with a soft border and a very subtle shadow. */
    public static RoundedPanel card() {
        RoundedPanel p = new RoundedPanel(20, 5);
        p.pad(16);
        return p;
    }

    public static JLabel title(String text) {
        return label(text, TITLE, BROWN);
    }

    public static JLabel heading(String text) {
        return label(text, HEADING, BROWN);
    }

    public static JLabel cardTitle(String text) {
        return label(text, CARD_TITLE, BROWN);
    }

    public static JLabel body(String text) {
        return label(text, BODY, BROWN);
    }

    public static JLabel muted(String text) {
        return label(text, SMALL, BROWN_SOFT);
    }

    public static JLabel formLabel(String text) {
        return label(text, LABEL, BROWN_SOFT);
    }

    public static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    /** Left-aligned horizontal row with an even gap between items (no stray outer margin, unlike FlowLayout). */
    public static JPanel hbox(int gap, Component... items) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < items.length; i++) {
            if (i > 0 && gap > 0) p.add(Box.createHorizontalStrut(gap));
            p.add(items[i]);
        }
        return p;
    }

    public static Border pad(int all) {
        return BorderFactory.createEmptyBorder(all, all, all, all);
    }

    public static Border pad(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    // ---------------------------------------------------------------
    // Scroll panes
    // ---------------------------------------------------------------
    /** Removes the default border and swaps in slim, warm-coloured scroll bars. */
    public static JScrollPane styleScroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        sp.getHorizontalScrollBar().setOpaque(false);
        return sp;
    }

    /** Price text used everywhere prices are shown, e.g. "\u20B9 1,500". */
    public static String money(double amount) {
        return RUPEE + " " + String.format("%,.0f", amount);
    }
}
