package com.uniswap.gui;

import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Marketplace product card: image area, name, category, price, condition,
 * favourite heart and a "View Details" button. All values come from the
 * ListingResponse loaded from the backend.
 */
public class ItemCard extends RoundedPanel {

    /** Fixed card height (used by CardGrid). */
    public static final int HEIGHT = 352;
    public static final int MIN_WIDTH = 236;

    private final HeartButton heart;

    /**
     * @param showHeart show the favourite heart (hidden on your own listings)
     * @param onView    called for "View Details" / clicking the image
     * @param onHeart   called when the heart is clicked (does the real API call)
     */
    public ItemCard(ListingResponse item, boolean showHeart, Runnable onView, Consumer<HeartButton> onHeart) {
        super(20, 6);
        setLayout(new BorderLayout());

        ItemImage image = new ItemImage(item.getName(), item.getCategoryName(), item.getImageUrl(), 19, false);
        image.setPreferredSize(new Dimension(10, 150));
        image.setTopLeft(Badge.type(item.getTransactionType()));
        image.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onView.run();
            }
        });
        if (showHeart) {
            heart = new HeartButton(item.isFavourite());
            heart.addActionListener(e -> onHeart.accept(heart));
            image.setTopRight(heart);
        } else {
            heart = null;
        }
        JPanel imageWrap = new JPanel(new BorderLayout());
        imageWrap.setOpaque(false);
        imageWrap.setBorder(UiTheme.pad(1, 1, 0, 1));
        imageWrap.add(image, BorderLayout.CENTER);
        add(imageWrap, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(UiTheme.pad(14, 16, 16, 16));

        JLabel name = UiTheme.cardTitle(item.getName());
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setToolTipText(item.getName());
        body.add(name);
        body.add(Box.createVerticalStrut(6));

        body.add(UiTheme.hbox(0, Badge.category(item.getCategoryName())));
        body.add(Box.createVerticalStrut(8));

        JLabel price = UiTheme.label(ListingRow.priceLabel(item), UiTheme.font(Font.BOLD, 20), UiTheme.BROWN);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(price);
        body.add(Box.createVerticalStrut(2));

        JPanel condRow = "ACTIVE".equals(item.getStatus())
                ? UiTheme.hbox(0, UiTheme.muted(ListingRow.pretty(item.getCondition()) + " condition"))
                : UiTheme.hbox(8, UiTheme.muted(ListingRow.pretty(item.getCondition()) + " condition"),
                Badge.status(item.getStatus()));
        body.add(condRow);
        body.add(Box.createVerticalGlue());
        body.add(Box.createVerticalStrut(12));

        RoundedButton view = UiTheme.primaryButton("View Details");
        view.addActionListener(e -> onView.run());
        JPanel btnWrap = new JPanel(new GridLayout(1, 1));
        btnWrap.setOpaque(false);
        btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnWrap.add(view);
        body.add(btnWrap);

        add(body, BorderLayout.CENTER);
        enableHover();
    }

    public HeartButton getHeart() {
        return heart;
    }
}
