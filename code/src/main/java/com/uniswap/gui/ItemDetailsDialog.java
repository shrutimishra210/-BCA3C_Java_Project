package com.uniswap.gui;

import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;

/** Screen: Item Details. */
public class ItemDetailsDialog extends JDialog {

    private final Long listingId;
    private boolean needsRefresh = false;
    private ListingResponse item;

    public ItemDetailsDialog(JFrame owner, Long listingId) {
        super(owner, "Item Details", true);
        this.listingId = listingId;
        setSize(500, 640);
        setLocationRelativeTo(owner);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        load();
    }

    private void load() {
        try {
            item = ApiClient.get("/listings/" + listingId, ListingResponse.class);
            build();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't load item");
            dispose();
        }
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BG);

        ScrollPanel col = ScrollPanel.column();
        col.setBorder(UiTheme.pad(0, 0, 24, 0));

        ItemImage image = new ItemImage(item.getName(), item.getCategoryName(), item.getImageUrl(), 0, true);
        image.setPreferredSize(new Dimension(10, 220));
        image.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        image.setAlignmentX(Component.LEFT_ALIGNMENT);
        image.setTopLeft(Badge.type(item.getTransactionType()));
        col.add(image);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(UiTheme.pad(22, 26, 0, 26));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel nameRow = new JPanel(new BorderLayout(10, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = UiTheme.label(item.getName(), UiTheme.font(Font.BOLD, 22), UiTheme.BROWN);
        nameRow.add(name, BorderLayout.CENTER);
        boolean isOwnListing = Session.getUserId() != null && Session.getUserId().equals(item.getOwnerId());
        HeartButton heart = null;
        if (!isOwnListing) {
            final HeartButton h = new HeartButton(item.isFavourite());
            h.addActionListener(e -> toggleFavourite(h));
            nameRow.add(h, BorderLayout.EAST);
            heart = h;
        }
        body.add(nameRow);
        body.add(Box.createVerticalStrut(6));

        JLabel price = UiTheme.label(ListingRow.priceLabel(item), UiTheme.font(Font.BOLD, 22), UiTheme.ACCENT_DARK);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(price);
        body.add(Box.createVerticalStrut(12));

        JPanel badges = UiTheme.hbox(8, Badge.category(item.getCategoryName()),
                new Badge(ListingRow.pretty(item.getCondition()), UiTheme.BROWN, UiTheme.NEUTRAL_BG, false));
        if (!"ACTIVE".equals(item.getStatus())) {
            JPanel extra = UiTheme.hbox(8, badges, Badge.status(item.getStatus()));
            body.add(extra);
        } else {
            body.add(badges);
        }
        body.add(Box.createVerticalStrut(14));

        JPanel metaRow = UiTheme.hbox(18,
                metaItem(Icons.Kind.PIN, safe(item.getLocation())),
                metaItem(Icons.Kind.CLOCK, safe(item.getCreatedAt())));
        body.add(metaRow);
        body.add(Box.createVerticalStrut(18));

        RoundedPanel descCard = new RoundedPanel(16, 0).fill(Color.WHITE).borderColor(UiTheme.BORDER);
        descCard.pad(16, 16, 16, 16);
        descCard.setLayout(new BorderLayout());
        descCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel desc = new JLabel("<html><div style='width:380px'>" + escape(item.getDescription()) + "</div></html>");
        desc.setFont(UiTheme.BODY);
        desc.setForeground(UiTheme.BROWN);
        descCard.add(desc, BorderLayout.CENTER);
        body.add(descCard);
        body.add(Box.createVerticalStrut(18));

        JPanel ownerRow = new JPanel(new BorderLayout(12, 0));
        ownerRow.setOpaque(false);
        ownerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        ownerRow.add(new Avatar(item.getOwnerName(), 38), BorderLayout.WEST);
        JPanel ownerText = new JPanel();
        ownerText.setOpaque(false);
        ownerText.setLayout(new BoxLayout(ownerText, BoxLayout.Y_AXIS));
        ownerText.add(UiTheme.label(item.getOwnerName(), UiTheme.BODY_BOLD, UiTheme.BROWN));
        ownerText.add(UiTheme.muted("Listed this item"));
        ownerRow.add(ownerText, BorderLayout.CENTER);
        body.add(ownerRow);
        body.add(Box.createVerticalStrut(22));

        boolean isActive = "ACTIVE".equals(item.getStatus());
        if (!isOwnListing) {
            RoundedButton requestBtn = UiTheme.accentButton(isActive ? "Send Request" : "No Longer Available");
            requestBtn.setEnabled(isActive);
            requestBtn.addActionListener(e -> {
                RequestDialog dialog = new RequestDialog((JFrame) getOwner(), item);
                dialog.setVisible(true);
                if (dialog.wasSent()) {
                    needsRefresh = true;
                    Dialogs.info(this, "Request sent successfully!", "Success");
                }
            });
            JPanel full = new JPanel(new GridLayout(1, 1));
            full.setOpaque(false);
            full.setAlignmentX(Component.LEFT_ALIGNMENT);
            full.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            full.add(requestBtn);
            body.add(full);
        } else {
            RoundedPanel note = new RoundedPanel(14, 0).fill(UiTheme.PEACH_HOVER);
            note.pad(12, 14, 12, 14);
            note.setLayout(new BorderLayout());
            note.add(UiTheme.label("This is your own listing.", UiTheme.SMALL, UiTheme.BROWN_SOFT), BorderLayout.WEST);
            note.setAlignmentX(Component.LEFT_ALIGNMENT);
            note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            body.add(note);
        }

        col.add(body);
        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(col));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UiTheme.BG);
        footer.setBorder(UiTheme.pad(0, 26, 20, 26));
        RoundedButton close = UiTheme.secondaryButton("Close");
        close.addActionListener(e -> dispose());
        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeWrap.setOpaque(false);
        closeWrap.add(close);
        footer.add(closeWrap, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JComponent metaItem(Icons.Kind icon, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JComponent ic = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 16);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Icons.paint((Graphics2D) g, icon, 0, 0, 16, UiTheme.BROWN_SOFT, false);
            }
        };
        p.add(ic);
        p.add(UiTheme.muted(text));
        return p;
    }

    private void toggleFavourite(HeartButton heart) {
        try {
            if (item.isFavourite()) {
                ApiClient.delete("/favourites/" + item.getId());
                item.setFavourite(false);
                heart.setFavourite(false);
            } else {
                ApiClient.post("/favourites/" + item.getId(), null);
                item.setFavourite(true);
                heart.setFavourite(true);
            }
            needsRefresh = true;
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't update favourites");
        }
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public boolean needsRefresh() {
        return needsRefresh;
    }
}
