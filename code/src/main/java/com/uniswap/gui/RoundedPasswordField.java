package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Password field styled like RoundedTextField. */
public class RoundedPasswordField extends JPasswordField {

    public RoundedPasswordField() {
        setOpaque(false);
        setFont(UiTheme.BODY);
        setForeground(UiTheme.BROWN);
        setCaretColor(UiTheme.ACCENT_DARK);
        setSelectionColor(UiTheme.PEACH);
        setSelectedTextColor(UiTheme.BROWN);
        setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 40));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        FieldPainter.paintBackground(g, this);
        super.paintComponent(g);
    }
}
