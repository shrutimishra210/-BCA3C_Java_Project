package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Product image area.
 *
 * Draws the listing's uploaded photo (scaled to fill the area, aspect ratio
 * preserved, centre-cropped - never stretched). While the photo loads, or when
 * the listing has none / it can't be read, a soft placeholder is painted
 * (colour from the category, big initial of the item). Optional overlay
 * components (type badge top-left, heart button top-right) sit on top.
 */
public class ItemImage extends JPanel {

    // Placeholder gradients, all derived from the SwapUni palette.
    private static final Color[][] PALETTES = {
            {UiTheme.mix(UiTheme.OLIVE, UiTheme.WARM_CREAM, 0.70), UiTheme.mix(UiTheme.OLIVE, UiTheme.WARM_CREAM, 0.20)},
            {UiTheme.mix(UiTheme.TERRACOTTA, UiTheme.WARM_CREAM, 0.72), UiTheme.mix(UiTheme.TERRACOTTA, UiTheme.WARM_CREAM, 0.25)},
            {UiTheme.mix(UiTheme.CHERRY_BURGUNDY, UiTheme.WARM_CREAM, 0.80), UiTheme.mix(UiTheme.CHERRY_BURGUNDY, UiTheme.WARM_CREAM, 0.40)},
            {UiTheme.mix(UiTheme.OLIVE, UiTheme.WARM_CREAM, 0.55), UiTheme.mix(UiTheme.TERRACOTTA, UiTheme.WARM_CREAM, 0.45)},
            {UiTheme.mix(UiTheme.TERRACOTTA, UiTheme.WARM_CREAM, 0.60), UiTheme.mix(UiTheme.CHERRY_BURGUNDY, UiTheme.WARM_CREAM, 0.55)},
    };

    private final String name;
    private final int palette;
    private final int radiusTop;     // rounded top corners (cards)
    private final boolean roundAll;  // fully rounded (thumbnails)
    private JComponent topLeft;
    private JComponent topRight;
    private BufferedImage photo;

    public ItemImage(String itemName, String category, int radius, boolean roundAll) {
        this.name = itemName == null ? "" : itemName;
        String key = category != null ? category : this.name;
        this.palette = Math.abs(key.hashCode()) % PALETTES.length;
        this.radiusTop = radius;
        this.roundAll = roundAll;
        setOpaque(false);
        setLayout(null);
    }

    /** Photo for a saved listing (server-relative URL, may be null). Loads in the background. */
    public ItemImage(String itemName, String category, String imageUrl, int radius, boolean roundAll) {
        this(itemName, category, radius, roundAll);
        if (imageUrl != null && !imageUrl.isBlank()) {
            ImageLoader.load(imageUrl, img -> setPhoto(img));
        }
    }

    /** Shows this image instead of the placeholder (null = back to the placeholder). */
    public void setPhoto(BufferedImage img) {
        this.photo = img;
        repaint();
    }

    public void setTopLeft(JComponent c) {
        topLeft = c;
        add(c);
    }

    public void setTopRight(JComponent c) {
        topRight = c;
        add(c);
    }

    @Override
    public void doLayout() {
        if (topLeft != null) {
            Dimension d = topLeft.getPreferredSize();
            topLeft.setBounds(12, 12, d.width, d.height);
        }
        if (topRight != null) {
            Dimension d = topRight.getPreferredSize();
            topRight.setBounds(getWidth() - d.width - 12, 10, d.width, d.height);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        Shape shape;
        if (roundAll) {
            shape = new RoundRectangle2D.Float(0, 0, w, h, radiusTop, radiusTop);
        } else {
            // only the top corners are rounded (bottom edge meets the card body)
            Area a = new Area(new RoundRectangle2D.Float(0, 0, w, h + radiusTop, radiusTop, radiusTop));
            a.intersect(new Area(new java.awt.geom.Rectangle2D.Float(0, 0, w, h)));
            shape = a;
        }
        g2.setClip(shape);

        if (photo != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            double s = Math.max((double) w / photo.getWidth(), (double) h / photo.getHeight());
            int dw = (int) Math.ceil(photo.getWidth() * s), dh = (int) Math.ceil(photo.getHeight() * s);
            g2.drawImage(photo, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            g2.dispose();
            return;
        }

        Color[] p = PALETTES[palette];
        g2.setPaint(new GradientPaint(0, 0, p[0], w, h, p[1]));
        g2.fill(shape);

        g2.setColor(new Color(255, 255, 255, 70));
        g2.fill(new Ellipse2D.Float(-h * 0.25f, -h * 0.35f, h * 0.95f, h * 0.95f));
        g2.setColor(new Color(255, 255, 255, 55));
        g2.fill(new Ellipse2D.Float(w - h * 0.55f, h * 0.45f, h * 0.9f, h * 0.9f));

        String initial = name.isBlank() ? "?" : name.trim().substring(0, 1).toUpperCase();
        int fs = Math.max(14, (int) (h * 0.46f));
        g2.setFont(UiTheme.font(Font.BOLD, fs));
        g2.setColor(new Color(UiTheme.CHARCOAL_OLIVE.getRed(), UiTheme.CHARCOAL_OLIVE.getGreen(), UiTheme.CHARCOAL_OLIVE.getBlue(), 80));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initial, (w - fm.stringWidth(initial)) / 2f, (h - fm.getHeight()) / 2f + fm.getAscent());
        g2.dispose();
    }
}
