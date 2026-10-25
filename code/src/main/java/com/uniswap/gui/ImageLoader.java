package com.uniswap.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Loads product photos off the UI thread and caches them, so scrolling the
 * Marketplace never freezes. A replaced photo gets a new URL, so the cache
 * can never show a stale picture.
 */
public final class ImageLoader {

    private static final int MAX_DIM = 900;   // downscale big photos once, paint cheaply afterwards
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "swapuni-image-loader");
        t.setDaemon(true);
        return t;
    });
    private static final Map<String, BufferedImage> CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) {
            return size() > 80;
        }
    };

    private ImageLoader() {
    }

    /** Calls onLoaded on the Swing thread with the image, or never if it is missing / unreadable. */
    public static void load(String relativeUrl, Consumer<BufferedImage> onLoaded) {
        if (relativeUrl == null || relativeUrl.isBlank()) return;
        BufferedImage cached;
        synchronized (CACHE) {
            cached = CACHE.get(relativeUrl);
        }
        if (cached != null) {
            SwingUtilities.invokeLater(() -> onLoaded.accept(cached));
            return;
        }
        POOL.execute(() -> {
            byte[] bytes = ApiClient.fetchBytes(ApiClient.resolveUrl(relativeUrl));
            BufferedImage img = decode(bytes);
            if (img == null) return;
            synchronized (CACHE) {
                CACHE.put(relativeUrl, img);
            }
            SwingUtilities.invokeLater(() -> onLoaded.accept(img));
        });
    }

    /** Reads an image from disk (for the live preview). Returns null if it can't be decoded. */
    public static BufferedImage readFile(File f) {
        try {
            return shrink(ImageIO.read(f));
        } catch (Throwable e) {
            return null;
        }
    }

    private static BufferedImage decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return shrink(ImageIO.read(new ByteArrayInputStream(bytes)));
        } catch (Throwable e) {   // includes missing WEBP native libs -> placeholder is used
            return null;
        }
    }

    private static BufferedImage shrink(BufferedImage src) {
        if (src == null) return null;
        int w = src.getWidth(), h = src.getHeight();
        double scale = Math.min(1.0, (double) MAX_DIM / Math.max(w, h));
        if (scale >= 1.0 && src.getType() != BufferedImage.TYPE_CUSTOM) return src;
        int nw = Math.max(1, (int) Math.round(w * scale)), nh = Math.max(1, (int) Math.round(h * scale));
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }
}
