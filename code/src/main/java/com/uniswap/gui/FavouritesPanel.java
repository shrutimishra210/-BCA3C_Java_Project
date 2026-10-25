package com.uniswap.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Screen: Favourites / Wishlist (Module 4) - uses the same product-card system as the Marketplace. */
public class FavouritesPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final CardGrid grid = new CardGrid(ItemCard.MIN_WIDTH, 18, ItemCard.HEIGHT);
    private final JPanel resultsContainer = new JPanel(new BorderLayout());

    public FavouritesPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD - 6));

        JPanel header = PageHeader.build("Favourites", "Items you're keeping an eye on.", null);
        JPanel headerWrap = new JPanel();
        headerWrap.setOpaque(false);
        headerWrap.setLayout(new BoxLayout(headerWrap, BoxLayout.Y_AXIS));
        headerWrap.add(header);
        headerWrap.add(Box.createVerticalStrut(18));
        add(headerWrap, BorderLayout.NORTH);

        ScrollPanel wrap = ScrollPanel.column();
        resultsContainer.setOpaque(false);
        resultsContainer.add(grid, BorderLayout.CENTER);
        wrap.add(resultsContainer);
        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(wrap));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        grid.removeAll();
        resultsContainer.removeAll();
        resultsContainer.add(grid, BorderLayout.CENTER);
        try {
            List<ListingResponse> items = ApiClient.getList("/favourites", new TypeReference<List<ListingResponse>>() {
            });
            if (items.isEmpty()) {
                RoundedButton browse = UiTheme.accentButton("Explore Marketplace");
                browse.addActionListener(e -> mainFrame.showCard("Marketplace"));
                resultsContainer.removeAll();
                resultsContainer.add(PageHeader.emptyState(Icons.Kind.HEART, "Your wishlist is feeling a little empty",
                        "Find something you'll love on the marketplace.", browse), BorderLayout.NORTH);
            } else {
                for (ListingResponse item : items) {
                    ItemCard card = new ItemCard(item, true,
                            () -> openDetails(item.getId()),
                            heart -> removeFavourite(item));
                    grid.add(card);
                }
            }
        } catch (Exception ex) {
            resultsContainer.removeAll();
            resultsContainer.add(UiTheme.body("Couldn't load favourites: " + ex.getMessage()), BorderLayout.NORTH);
        }
        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    private void removeFavourite(ListingResponse item) {
        try {
            ApiClient.delete("/favourites/" + item.getId());
            refresh();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't remove");
        }
    }

    private void openDetails(Long listingId) {
        ItemDetailsDialog dialog = new ItemDetailsDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), listingId);
        dialog.setVisible(true);
        if (dialog.needsRefresh()) refresh();
    }
}
