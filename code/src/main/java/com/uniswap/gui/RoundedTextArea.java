package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Multi-line text input with the same rounded look as RoundedTextField. */
public class RoundedTextArea extends JPanel {

    private final JTextArea area = new JTextArea();
    private boolean focused = false;

    public RoundedTextArea(int rows) {
        setOpaque(false);
        setLayout(new BorderLayout());
        area.setRows(rows);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(UiTheme.BODY);
        area.setForeground(UiTheme.BROWN);
        area.setCaretColor(UiTheme.ACCENT_DARK);
        area.setSelectionColor(UiTheme.PEACH);
        area.setSelectedTextColor(UiTheme.BROWN);
        area.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
        JScrollPane sp = UiTheme.styleScroll(new JScrollPane(area));
        sp.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 3));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp, BorderLayout.CENTER);
    }

    public JTextArea getTextArea() {
        return area;
    }

    public String getText() {
        return area.getText();
    }

    public void setText(String t) {
        area.setText(t);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(focused ? UiTheme.ACCENT : UiTheme.BORDER_INPUT);
        g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
