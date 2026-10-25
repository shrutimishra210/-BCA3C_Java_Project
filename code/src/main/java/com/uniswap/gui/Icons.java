package com.uniswap.gui;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.*;

/**
 * Small vector icons drawn with Java2D.
 *
 * Emoji glyphs depend on whichever fonts a machine happens to have, so
 * they render inconsistently between computers. These are drawn as
 * shapes instead, so they look identical everywhere and take their
 * colour from the theme.
 */
public final class Icons {

    public enum Kind {
        HOME, BAG, HEART, BOX, LIST, CARD, USER, LOGOUT, PLUS, SEARCH, SWAP,
        PIN, CLOCK, IMAGE, SPARK, CHECK
    }

    private Icons() {
    }

    public static Icon icon(Kind kind, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                paint(g2, kind, x, y, size, color, false);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /** Draws the icon inside the square (x, y, size, size). */
    public static void paint(Graphics2D g, Kind kind, float x, float y, float size, Color color, boolean filled) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        float s = size / 24f;
        g2.scale(s, s);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (kind) {
            case HOME -> {
                Path2D roof = new Path2D.Float();
                roof.moveTo(3, 11); roof.lineTo(12, 3); roof.lineTo(21, 11);
                g2.draw(roof);
                Path2D body = new Path2D.Float();
                body.moveTo(5.5, 9.5); body.lineTo(5.5, 20); body.lineTo(10, 20);
                body.lineTo(10, 14); body.lineTo(14, 14); body.lineTo(14, 20);
                body.lineTo(18.5, 20); body.lineTo(18.5, 9.5);
                g2.draw(body);
            }
            case BAG -> {
                Path2D bag = new Path2D.Float();
                bag.moveTo(5, 8); bag.lineTo(19, 8); bag.lineTo(20.2, 20.5);
                bag.lineTo(3.8, 20.5); bag.closePath();
                g2.draw(bag);
                Path2D handle = new Path2D.Float();
                handle.moveTo(9, 10.5); handle.lineTo(9, 7);
                handle.quadTo(9, 3, 12, 3); handle.quadTo(15, 3, 15, 7); handle.lineTo(15, 10.5);
                g2.draw(handle);
            }
            case HEART -> {
                Path2D h = new Path2D.Float();
                h.moveTo(12, 20.5);
                h.curveTo(5, 15, 2.5, 11.5, 2.5, 8.2);
                h.curveTo(2.5, 5.3, 4.7, 3.2, 7.4, 3.2);
                h.curveTo(9.4, 3.2, 11.1, 4.3, 12, 6.2);
                h.curveTo(12.9, 4.3, 14.6, 3.2, 16.6, 3.2);
                h.curveTo(19.3, 3.2, 21.5, 5.3, 21.5, 8.2);
                h.curveTo(21.5, 11.5, 19, 15, 12, 20.5);
                h.closePath();
                if (filled) g2.fill(h);
                g2.draw(h);
            }
            case BOX -> {
                Path2D b = new Path2D.Float();
                b.moveTo(3, 7); b.lineTo(12, 3); b.lineTo(21, 7); b.lineTo(21, 17);
                b.lineTo(12, 21); b.lineTo(3, 17); b.closePath();
                g2.draw(b);
                Path2D top = new Path2D.Float();
                top.moveTo(3, 7); top.lineTo(12, 11); top.lineTo(21, 7);
                g2.draw(top);
                g2.draw(new Line2D.Float(12, 11, 12, 21));
            }
            case LIST -> {
                g2.draw(new Line2D.Float(9, 6, 20, 6));
                g2.draw(new Line2D.Float(9, 12, 20, 12));
                g2.draw(new Line2D.Float(9, 18, 20, 18));
                g2.fill(new Ellipse2D.Float(3, 4.6f, 2.8f, 2.8f));
                g2.fill(new Ellipse2D.Float(3, 10.6f, 2.8f, 2.8f));
                g2.fill(new Ellipse2D.Float(3, 16.6f, 2.8f, 2.8f));
            }
            case CARD -> {
                g2.draw(new RoundRectangle2D.Float(2.5f, 5, 19, 14, 3.5f, 3.5f));
                g2.draw(new Line2D.Float(2.5f, 10, 21.5f, 10));
                g2.draw(new Line2D.Float(6, 15, 10, 15));
            }
            case USER -> {
                g2.draw(new Ellipse2D.Float(8, 3.5f, 8, 8));
                Path2D sh = new Path2D.Float();
                sh.moveTo(4, 21); sh.curveTo(4, 16.5, 8, 14.5, 12, 14.5);
                sh.curveTo(16, 14.5, 20, 16.5, 20, 21);
                g2.draw(sh);
            }
            case LOGOUT -> {
                Path2D door = new Path2D.Float();
                door.moveTo(9.5, 4); door.lineTo(6, 4); door.quadTo(4, 4, 4, 6);
                door.lineTo(4, 18); door.quadTo(4, 20, 6, 20); door.lineTo(9.5, 20);
                g2.draw(door);
                g2.draw(new Line2D.Float(10, 12, 21, 12));
                Path2D arrow = new Path2D.Float();
                arrow.moveTo(17, 8); arrow.lineTo(21, 12); arrow.lineTo(17, 16);
                g2.draw(arrow);
            }
            case PLUS -> {
                g2.draw(new Line2D.Float(12, 5, 12, 19));
                g2.draw(new Line2D.Float(5, 12, 19, 12));
            }
            case SEARCH -> {
                g2.draw(new Ellipse2D.Float(4.5f, 4.5f, 12, 12));
                g2.draw(new Line2D.Float(15, 15, 20.5f, 20.5f));
            }
            case SWAP -> {
                g2.draw(new Line2D.Float(4, 8, 19, 8));
                Path2D a1 = new Path2D.Float();
                a1.moveTo(15, 4); a1.lineTo(19, 8); a1.lineTo(15, 12);
                g2.draw(a1);
                g2.draw(new Line2D.Float(20, 16, 5, 16));
                Path2D a2 = new Path2D.Float();
                a2.moveTo(9, 12); a2.lineTo(5, 16); a2.lineTo(9, 20);
                g2.draw(a2);
            }
            case PIN -> {
                Path2D p = new Path2D.Float();
                p.moveTo(12, 21);
                p.curveTo(12, 21, 5, 14.5, 5, 9.5);
                p.curveTo(5, 5.6, 8.1, 2.8, 12, 2.8);
                p.curveTo(15.9, 2.8, 19, 5.6, 19, 9.5);
                p.curveTo(19, 14.5, 12, 21, 12, 21);
                p.closePath();
                g2.draw(p);
                g2.draw(new Ellipse2D.Float(9.5f, 7, 5, 5));
            }
            case CLOCK -> {
                g2.draw(new Ellipse2D.Float(3, 3, 18, 18));
                Path2D hands = new Path2D.Float();
                hands.moveTo(12, 7); hands.lineTo(12, 12); hands.lineTo(15.5, 14);
                g2.draw(hands);
            }
            case IMAGE -> {
                g2.draw(new RoundRectangle2D.Float(3, 4, 18, 16, 3.5f, 3.5f));
                g2.fill(new Ellipse2D.Float(7, 8, 3, 3));
                Path2D m = new Path2D.Float();
                m.moveTo(4, 18); m.lineTo(10, 12.5); m.lineTo(14, 16); m.lineTo(17, 13.5); m.lineTo(20.5, 17);
                g2.draw(m);
            }
            case SPARK -> {
                Path2D sp = new Path2D.Float();
                sp.moveTo(12, 2.5); sp.quadTo(12, 12, 21.5, 12);
                sp.quadTo(12, 12, 12, 21.5); sp.quadTo(12, 12, 2.5, 12);
                sp.quadTo(12, 12, 12, 2.5); sp.closePath();
                g2.fill(sp);
            }
            case CHECK -> {
                Path2D c = new Path2D.Float();
                c.moveTo(4.5, 12.5); c.lineTo(10, 18); c.lineTo(19.5, 6.5);
                g2.draw(c);
            }
        }
        g2.dispose();
    }
}
