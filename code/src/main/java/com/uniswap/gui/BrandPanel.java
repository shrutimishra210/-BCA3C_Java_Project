package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Brand panel on the left of the Login and Sign Up screens.
 *
 * It doubles as a tiny game, "Pop the Swap": campus items (bags, books, cards...)
 * float up the panel and you pop them with the mouse. Streaks give bonus points,
 * the rare golden item is worth 5, and clicking the logo spins it and releases a
 * swarm of items. The best score is kept while the app is open.
 */
public class BrandPanel extends JPanel {

    // ---------------------------------------------------------------- game model
    private static final Icons.Kind[] KINDS = {Icons.Kind.BAG, Icons.Kind.BOX, Icons.Kind.HEART,
            Icons.Kind.CARD, Icons.Kind.LIST, Icons.Kind.SWAP, Icons.Kind.IMAGE, Icons.Kind.CHECK};
    private static final Color[] COLORS = {UiTheme.CHERRY_BURGUNDY, UiTheme.TERRACOTTA, UiTheme.OLIVE,
            UiTheme.FOREST, UiTheme.TAUPE};
    private static final String[] CHEERS = {"Nice swaps!", "Campus legend!", "Swap master!", "Unstoppable!", "Trade champion!"};
    private static final Color GOLD = new Color(0xE3B04B);
    private static int best = 0;          // survives switching between Login and Sign Up

    private static final class Bubble {
        float x, y, r, vy, phase, sway;
        Icons.Kind kind;
        Color color;
        boolean golden;
        float alpha = 1f;
    }

    private static final class Particle {
        float x, y, vx, vy, life;
        Color c;
    }

    private static final class FloatText {
        float x, y, age;
        String text;
        Color c;
    }

    private final List<Bubble> bubbles = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatText> texts = new ArrayList<>();
    private final Random rnd = new Random();
    private final JComponent header;
    private final LogoMark logo;
    private Timer timer;
    private long lastNanos;
    private float spawnIn = 0.2f;
    private int score = 0;
    private int combo = 0;
    private String toast;
    private float toastAge = 99f;
    private float time = 0f;

    public BrandPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 10));

        logo = new LogoMark(68);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logo.setToolTipText("Click me!");
        logo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                logo.spin();
                for (int i = 0; i < 6; i++) spawn(false);
            }
        });
        JLabel name = UiTheme.label("SwapUni", UiTheme.serif(Font.BOLD, 40), UiTheme.TEXT_LIGHT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tag = UiTheme.label("Your Campus. Your Marketplace.", UiTheme.font(Font.PLAIN, 14), UiTheme.mix(UiTheme.TEXT_LIGHT, UiTheme.CHARCOAL_OLIVE, 0.25));
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        chips.setOpaque(false);
        for (String s : new String[]{"Buy", "Sell", "Swap", "Borrow", "Donate"}) {
            chips.add(new Badge(s, UiTheme.BROWN, UiTheme.EGGSHELL, false));
        }
        chips.setAlignmentX(Component.CENTER_ALIGNMENT);
        chips.setMaximumSize(new Dimension(360, 64));

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(UiTheme.pad(40, 20, 10, 20));
        col.add(logo);
        col.add(Box.createVerticalStrut(14));
        col.add(name);
        col.add(Box.createVerticalStrut(4));
        col.add(tag);
        col.add(Box.createVerticalStrut(14));
        col.add(chips);
        header = col;
        add(col, BorderLayout.NORTH);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(bubbleAt(e.getX(), e.getY()) != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /** Lays Login/Sign Up out as brand | content, brand taking ~42% of the width (never under 380px). */
    public static JPanel split(JComponent brand, JComponent content) {
        JPanel p = new JPanel(null) {
            @Override
            public void doLayout() {
                int w = getWidth(), h = getHeight();
                int bw = Math.max(380, Math.min(640, (int) (w * 0.42)));
                brand.setBounds(0, 0, bw, h);
                content.setBounds(bw, 0, Math.max(0, w - bw), h);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(900, 600);
            }
        };
        p.setBackground(UiTheme.BG);
        p.add(brand);
        p.add(content);
        return p;
    }

    // ---------------------------------------------------------------- lifecycle
    @Override
    public void addNotify() {
        super.addNotify();
        lastNanos = System.nanoTime();
        timer = new Timer(16, e -> tick());
        timer.start();
    }

    @Override
    public void removeNotify() {
        if (timer != null) timer.stop();
        super.removeNotify();
    }

    // ---------------------------------------------------------------- game logic
    private float headerBottom() {
        return header.getY() + header.getHeight() + 8;
    }

    private void spawn(boolean allowGolden) {
        int w = getWidth(), h = getHeight();
        if (w < 100 || h < 200) return;
        Bubble b = new Bubble();
        b.golden = allowGolden && rnd.nextFloat() < 0.09f;
        b.r = b.golden ? 24 : 24 + rnd.nextInt(11);
        b.x = b.r + 12 + rnd.nextFloat() * (w - 2 * b.r - 24);
        b.y = h + b.r;
        b.vy = -(46 + rnd.nextFloat() * 36) * (b.golden ? 1.35f : 1f);
        b.phase = rnd.nextFloat() * 6.28f;
        b.sway = 10 + rnd.nextFloat() * 18;
        b.kind = b.golden ? Icons.Kind.SPARK : KINDS[rnd.nextInt(KINDS.length)];
        b.color = b.golden ? GOLD : COLORS[rnd.nextInt(COLORS.length)];
        bubbles.add(b);
    }

    private void tick() {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastNanos) / 1e9f);
        lastNanos = now;
        time += dt;
        float top = headerBottom();

        spawnIn -= dt;
        if (spawnIn <= 0 && bubbles.size() < 9) {
            spawn(true);
            spawnIn = 0.55f + rnd.nextFloat() * 0.6f;
        }
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            Bubble b = bubbles.get(i);
            b.y += b.vy * dt;
            float sx = b.x + (float) Math.sin(time * 1.4f + b.phase) * b.sway;
            b.alpha = Math.max(0f, Math.min(1f, (b.y - top) / 70f));
            if (b.y < top) {                 // escaped: streak ends
                bubbles.remove(i);
                combo = 0;
            }
        }
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= dt * 1.6f;
            p.vy += 260 * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if (p.life <= 0) particles.remove(i);
        }
        for (int i = texts.size() - 1; i >= 0; i--) {
            FloatText t = texts.get(i);
            t.age += dt;
            t.y -= 38 * dt;
            if (t.age > 1.0f) texts.remove(i);
        }
        toastAge += dt;
        repaint();
    }

    private float swayX(Bubble b) {
        return b.x + (float) Math.sin(time * 1.4f + b.phase) * b.sway;
    }

    private Bubble bubbleAt(int mx, int my) {
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            Bubble b = bubbles.get(i);
            double dx = mx - swayX(b), dy = my - b.y;
            if (b.alpha > 0.3f && dx * dx + dy * dy <= (b.r + 5) * (b.r + 5)) return b;
        }
        return null;
    }

    private void handleClick(int mx, int my) {
        Bubble b = bubbleAt(mx, my);
        if (b == null) {                       // empty click: tiny ripple only
            burst(mx, my, UiTheme.mix(UiTheme.TEXT_LIGHT, UiTheme.CHARCOAL_OLIVE, 0.4), 5);
            return;
        }
        bubbles.remove(b);
        combo++;
        int points = b.golden ? 5 : 1;
        if (combo >= 3) points += 1;           // streak bonus
        int before = score;
        score += points;
        best = Math.max(best, score);
        burst(swayX(b), b.y, b.color, b.golden ? 26 : 14);

        FloatText t = new FloatText();
        t.x = swayX(b);
        t.y = b.y - b.r;
        t.text = "+" + points + (combo >= 3 ? "  x" + combo : "");
        t.c = b.golden ? GOLD : UiTheme.TEXT_LIGHT;
        texts.add(t);

        if (score / 10 > before / 10) {        // milestone every 10 points
            toast = CHEERS[(score / 10 - 1) % CHEERS.length];
            toastAge = 0f;
            for (int i = 0; i < 4; i++) burst(getWidth() * (0.2f + 0.2f * i), getHeight() * 0.45f, COLORS[i % COLORS.length], 18);
        }
    }

    private void burst(float x, float y, Color c, int n) {
        for (int i = 0; i < n; i++) {
            Particle p = new Particle();
            double a = rnd.nextDouble() * Math.PI * 2;
            float sp = 60 + rnd.nextFloat() * 150;
            p.x = x;
            p.y = y;
            p.vx = (float) Math.cos(a) * sp;
            p.vy = (float) Math.sin(a) * sp - 70;
            p.life = 0.7f + rnd.nextFloat() * 0.5f;
            p.c = c;
            particles.add(p);
        }
    }

    // ---------------------------------------------------------------- painting
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, UiTheme.CHARCOAL_OLIVE, w, h, UiTheme.mix(UiTheme.CHARCOAL_OLIVE, UiTheme.FOREST, 0.75)));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(255, 255, 255, 14));
        g2.fill(new Ellipse2D.Float(-w * 0.35f, -w * 0.3f, w * 0.95f, w * 0.95f));
        g2.setColor(new Color(UiTheme.TERRACOTTA.getRed(), UiTheme.TERRACOTTA.getGreen(), UiTheme.TERRACOTTA.getBlue(), 28));
        g2.fill(new Ellipse2D.Float(w * 0.3f, h - w * 0.5f, w * 0.95f, w * 0.95f));

        // bubbles
        for (Bubble b : bubbles) {
            float x = swayX(b), r = b.r;
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, b.alpha))));
            g2.setColor(new Color(0, 0, 0, 55));
            g2.fill(new Ellipse2D.Float(x - r + 3, b.y - r + 6, r * 2, r * 2));
            if (b.golden) {
                float pulse = 1f + 0.06f * (float) Math.sin(time * 6);
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 70));
                g2.fill(new Ellipse2D.Float(x - r * 1.35f * pulse, b.y - r * 1.35f * pulse, r * 2.7f * pulse, r * 2.7f * pulse));
            }
            g2.setPaint(new GradientPaint(x - r, b.y - r, UiTheme.mix(b.color, Color.WHITE, 0.18), x + r, b.y + r, b.color));
            g2.fill(new Ellipse2D.Float(x - r, b.y - r, r * 2, r * 2));
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fill(new Ellipse2D.Float(x - r * 0.62f, b.y - r * 0.78f, r * 0.9f, r * 0.5f));
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new Ellipse2D.Float(x - r, b.y - r, r * 2, r * 2));
            float s = r * 1.0f;
            Icons.paint(g2, b.kind, x - s / 2, b.y - s / 2, s, b.golden ? UiTheme.CHARCOAL_OLIVE : Color.WHITE, false);
            g2.setComposite(old);
        }

        // particles
        for (Particle p : particles) {
            int a = (int) (255 * Math.max(0, Math.min(1, p.life)));
            g2.setColor(new Color(p.c.getRed(), p.c.getGreen(), p.c.getBlue(), a));
            g2.fill(new Ellipse2D.Float(p.x - 3, p.y - 3, 6, 6));
        }
        // floating "+1" texts
        g2.setFont(UiTheme.font(Font.BOLD, 15));
        for (FloatText t : texts) {
            int a = (int) (255 * Math.max(0, 1 - t.age));
            g2.setColor(new Color(t.c.getRed(), t.c.getGreen(), t.c.getBlue(), a));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t.text, t.x - fm.stringWidth(t.text) / 2f, t.y);
        }

        // milestone toast
        if (toastAge < 1.8f && toast != null) {
            float a = toastAge < 0.2f ? toastAge / 0.2f : toastAge > 1.4f ? Math.max(0, (1.8f - toastAge) / 0.4f) : 1f;
            g2.setFont(UiTheme.serif(Font.BOLD, 26));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(toast);
            float ty = h * 0.42f - toastAge * 14;
            g2.setColor(new Color(0, 0, 0, (int) (90 * a)));
            g2.drawString(toast, (w - tw) / 2f + 2, ty + 2);
            g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), (int) (255 * a)));
            g2.drawString(toast, (w - tw) / 2f, ty);
        }

        // score pill
        String line = score == 0 ? "Pop the items to swap them!"
                : "Swaps " + score + "   •   Best " + best + (combo >= 3 ? "   •   Streak x" + combo : "");
        g2.setFont(UiTheme.font(Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int pw = fm.stringWidth(line) + 36, ph = 32;
        float px = (w - pw) / 2f, py = h - ph - 18;
        g2.setColor(new Color(255, 255, 255, 28));
        g2.fill(new RoundRectangle2D.Float(px, py, pw, ph, ph, ph));
        g2.setColor(new Color(255, 255, 255, 60));
        g2.draw(new RoundRectangle2D.Float(px + 0.5f, py + 0.5f, pw - 1, ph - 1, ph, ph));
        g2.setColor(UiTheme.TEXT_LIGHT);
        g2.drawString(line, px + 18, py + (ph - fm.getHeight()) / 2f + fm.getAscent());
        g2.dispose();
    }
}
