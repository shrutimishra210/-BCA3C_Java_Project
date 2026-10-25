package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Text field with rounded corners, warm border and an optional placeholder. */
public class RoundedTextField extends JTextField {

    private String placeholder;

    public RoundedTextField() {
        this(null);
    }

    public RoundedTextField(String text) {
        super(text);
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

    public RoundedTextField placeholder(String p) {
        this.placeholder = p;
        return this;
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
        FieldPainter.paintPlaceholder(g, this, placeholder);
    }
}
