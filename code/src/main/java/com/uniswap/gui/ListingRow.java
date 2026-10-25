package com.uniswap.gui;

import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Horizontal listing card used on the Dashboard ("Recent items") and on
 * My Listings. Thumbnail on the left, details in the middle, actions on the
 * right (compact) or in a row underneath (when there are several actions).
 */
public final class ListingRow {

    /** Card sizes used when rows are laid out in a CardGrid (Dashboard / My Listings). */
    public static final int MIN_WIDTH = 400;
    public static final int COMPACT_HEIGHT = 108;
    public static final int ACTIONS_HEIGHT = 172;

    private ListingRow() {
    }

    /** Compact row with a single "View" button on the right. */
    public static JPanel build(ListingResponse item, Runnable onView) {
        return build(item, onView, List.of());
    }

    /** Row with "View" plus extra action buttons (Edit, Mark as Sold, Delete ...) underneath the details. */
    public static JPanel build(ListingResponse item, Runnable onView, List<? extends JComponent> extraActions) {
        RoundedPanel row = UiTheme.card();
        row.setLayout(new BorderLayout(16, 0));
        row.pad(14, 14, 14, 16);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        ItemImage thumb = new ItemImage(item.getName(), item.getCategoryName(), item.getImageUrl(), 16, true);
        thumb.setPreferredSize(new Dimension(76, 76));
        JPanel thumbWrap = new JPanel(new BorderLayout());
        thumbWrap.setOpaque(false);
        thumbWrap.add(thumb, BorderLayout.NORTH);
        row.add(thumbWrap, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JPanel top = UiTheme.hbox(10, UiTheme.cardTitle(item.getName()), Badge.status(item.getStatus()));

        JLabel price = UiTheme.label(priceLabel(item), UiTheme.font(Font.BOLD, 14), UiTheme.BROWN);
        JPanel meta = UiTheme.hbox(10, Badge.category(item.getCategoryName()), price,
                UiTheme.muted(pretty(item.getCondition())));
        meta.setBorder(UiTheme.pad(8, 0, 0, 0));

        info.add(top);
        info.add(meta);

        if (!extraActions.isEmpty()) {
            RoundedButton view = UiTheme.secondaryButton("View");
            view.addActionListener(e -> onView.run());
            java.util.List<Component> all = new java.util.ArrayList<>();
            all.add(view);
            all.addAll(extraActions);
            JPanel actions = UiTheme.hbox(8, all.toArray(new Component[0]));
            actions.setBorder(UiTheme.pad(12, 0, 0, 0));
            // Under the whole card (not squeezed beside the thumbnail) so every button fits
            // even when cards sit side by side.
            row.add(info, BorderLayout.CENTER);
            row.add(actions, BorderLayout.SOUTH);
        } else {
            row.add(info, BorderLayout.CENTER);
            RoundedButton view = UiTheme.secondaryButton("View");
            view.addActionListener(e -> onView.run());
            JPanel right = new JPanel(new GridBagLayout());
            right.setOpaque(false);
            right.add(view);
            row.add(right, BorderLayout.EAST);
        }
        row.enableHover();
        return row;
    }

    /** "1,500" style price with the rupee sign, or Free / Exchange / Borrow for non-sale listings. */
    public static String priceLabel(ListingResponse item) {
        return switch (item.getTransactionType()) {
            case "DONATE" -> "Free";
            case "EXCHANGE" -> "Exchange";
            case "BORROW" -> "Borrow";
            default -> item.getPrice() == null ? "-" : UiTheme.money(item.getPrice());
        };
    }

    /** LIKE_NEW -> "Like new". */
    public static String pretty(String enumName) {
        if (enumName == null || enumName.isEmpty()) return "";
        String s = enumName.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
