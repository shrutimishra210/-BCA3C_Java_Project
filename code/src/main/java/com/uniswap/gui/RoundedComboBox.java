package com.uniswap.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;

/** Drop-down with rounded corners, a chevron arrow and a peach highlight. */
public class RoundedComboBox<E> extends JComboBox<E> {

    public RoundedComboBox() {
        this(null);
    }

    public RoundedComboBox(E[] items) {
        super();
        if (items != null) {
            for (E item : items) addItem(item);
        }
        setOpaque(false);
        setFont(UiTheme.BODY);
        setForeground(UiTheme.BROWN);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setUI(new ComboUI());
        setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
                l.setFont(UiTheme.BODY);
                l.setForeground(UiTheme.BROWN);
                l.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                l.setOpaque(true);
                l.setBackground(isSelected ? UiTheme.PEACH_LIGHT : Color.WHITE);
                return l;
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 120), 40);
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

    private static class ComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UiTheme.BROWN_SOFT);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            b.setPreferredSize(new Dimension(30, 30));
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            return b;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // background is painted by RoundedComboBox itself
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            ListCellRenderer<Object> r = comboBox.getRenderer();
            Object value = comboBox.getSelectedItem();
            Component c = r.getListCellRendererComponent(listBox, value, -1, false, false);
            c.setFont(comboBox.getFont());
            c.setForeground(UiTheme.BROWN);
            if (c instanceof JComponent jc) jc.setOpaque(false);
            currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, false);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup p = (BasicComboPopup) super.createPopup();
            p.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_INPUT));
            return p;
        }
    }
}
