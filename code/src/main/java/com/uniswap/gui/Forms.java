package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;

/** Tiny helpers so every form in the app lays out its labelled fields the same way. */
public final class Forms {

    private Forms() {
    }

    /** Label above an input, left aligned, with a small gap below. */
    public static JPanel field(String label, JComponent input) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = UiTheme.formLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(input);
        p.setBorder(UiTheme.pad(0, 0, 14, 0));
        return p;
    }

    /** Places fields side by side with an even gap. */
    public static JPanel row(JComponent... cols) {
        JPanel p = new JPanel(new GridLayout(1, cols.length, 14, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent c : cols) p.add(c);
        return p;
    }

    /** Footer row with right-aligned buttons. */
    public static JPanel buttonBar(JComponent... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        for (JComponent b : buttons) p.add(b);
        return p;
    }
}
