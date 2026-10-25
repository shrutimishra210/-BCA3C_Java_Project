package com.uniswap.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.uniswap.dto.CategoryResponse;
import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Screen: Marketplace / Listings (Module 2). */
public class MarketplacePanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final RoundedTextField searchField = new RoundedTextField();
    private final RoundedComboBox<ComboItem> categoryCombo = new RoundedComboBox<>();
    private final RoundedComboBox<String> typeCombo =
            new RoundedComboBox<>(new String[]{"ALL", "SELL", "EXCHANGE", "BORROW", "DONATE"});
    private final CardGrid results = new CardGrid(ItemCard.MIN_WIDTH, 18, ItemCard.HEIGHT);
    private final JPanel resultsContainer = new JPanel(new BorderLayout());
    private final JLabel resultsCount = UiTheme.muted("");

    private List<CategoryResponse> categories = List.of();

    public MarketplacePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD - 6));

        add(buildHeader(), BorderLayout.NORTH);

        ScrollPanel wrap = ScrollPanel.column();
        resultsContainer.setOpaque(false);
        resultsContainer.add(results, BorderLayout.CENTER);
        wrap.add(resultsContainer);
        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(wrap));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        RoundedButton addBtn = UiTheme.accentButton("List an Item").icon(Icons.Kind.PLUS);
        addBtn.addActionListener(e -> {
            AddListingDialog dialog = new AddListingDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), categories);
            dialog.setVisible(true);
            if (dialog.wasSaved()) {
                refresh();
            }
        });
        header.add(PageHeader.build("Marketplace", "Discover what your campus is buying, selling and swapping.", addBtn));
        header.add(Box.createVerticalStrut(18));

        searchField.placeholder("Search books, electronics, calculators...");
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.setMaximumSize(new Dimension(280, 40));
        searchField.addActionListener(e -> loadListings());
        RoundedButton searchBtn = UiTheme.secondaryButton("Search").icon(Icons.Kind.SEARCH);
        searchBtn.addActionListener(e -> loadListings());

        categoryCombo.setPreferredSize(new Dimension(180, 40));
        categoryCombo.setMaximumSize(new Dimension(180, 40));
        categoryCombo.addActionListener(e -> loadListings());
        typeCombo.setPreferredSize(new Dimension(140, 40));
        typeCombo.setMaximumSize(new Dimension(140, 40));
        typeCombo.addActionListener(e -> loadListings());

        JPanel filterRow = UiTheme.hbox(10, searchField, searchBtn,
                UiTheme.formLabel("Category"), categoryCombo, UiTheme.formLabel("Type"), typeCombo);
        header.add(filterRow);
        header.add(Box.createVerticalStrut(14));
        header.add(resultsCount);
        header.add(Box.createVerticalStrut(4));

        return header;
    }

    @Override
    public void refresh() {
        loadCategories();
        loadListings();
    }

    public void filterByCategory(Long categoryId) {
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            ComboItem item = categoryCombo.getItemAt(i);
            if (item.id != null && item.id.equals(categoryId)) {
                categoryCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadCategories() {
        try {
            categories = ApiClient.getList("/categories", new TypeReference<List<CategoryResponse>>() {
            });
            categoryCombo.removeAllItems();
            categoryCombo.addItem(new ComboItem(null, "All Categories"));
            for (CategoryResponse c : categories) {
                categoryCombo.addItem(new ComboItem(c.getId(), c.getName()));
            }
        } catch (Exception ex) {
            // Categories rarely change; a silent failure here just means the
            // filter dropdown stays empty, search itself still works.
        }
    }

    private void loadListings() {
        results.removeAll();
        resultsContainer.removeAll();
        resultsContainer.add(results, BorderLayout.CENTER);
        try {
            StringBuilder path = new StringBuilder("/listings?");
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                path.append("keyword=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8)).append("&");
            }
            ComboItem selectedCategory = (ComboItem) categoryCombo.getSelectedItem();
            if (selectedCategory != null && selectedCategory.id != null) {
                path.append("categoryId=").append(selectedCategory.id).append("&");
            }
            String type = (String) typeCombo.getSelectedItem();
            if (type != null && !type.equals("ALL")) {
                path.append("type=").append(type).append("&");
            }

            List<ListingResponse> items = ApiClient.getList(path.toString(),
                    new TypeReference<List<ListingResponse>>() {
                    });

            resultsCount.setText(items.size() + " result" + (items.size() == 1 ? "" : "s") + " found");

            if (items.isEmpty()) {
                resultsContainer.removeAll();
                resultsContainer.add(PageHeader.emptyState(Icons.Kind.SEARCH, "No items match your search",
                        "Try different keywords or filters.", null), BorderLayout.NORTH);
            } else {
                for (ListingResponse item : items) {
                    boolean own = Session.getUserId() != null && Session.getUserId().equals(item.getOwnerId());
                    ItemCard card = new ItemCard(item, !own, () -> openDetails(item.getId()),
                            heart -> toggleFavourite(item, heart));
                    results.add(card);
                }
            }
        } catch (Exception ex) {
            resultsContainer.removeAll();
            resultsContainer.add(UiTheme.body("Couldn't load listings: " + ex.getMessage()), BorderLayout.NORTH);
        }
        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    private void toggleFavourite(ListingResponse item, HeartButton heart) {
        try {
            if (heart.isFavourite()) {
                ApiClient.delete("/favourites/" + item.getId());
                heart.setFavourite(false);
                item.setFavourite(false);
            } else {
                ApiClient.post("/favourites/" + item.getId(), null);
                heart.setFavourite(true);
                item.setFavourite(true);
            }
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't update favourites");
        }
    }

    private void openDetails(Long listingId) {
        ItemDetailsDialog dialog = new ItemDetailsDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), listingId);
        dialog.setVisible(true);
        if (dialog.needsRefresh()) {
            loadListings();
        }
    }

    /** Small wrapper so the category JComboBox can show a name but carry an id. */
    static class ComboItem {
        final Long id;
        final String label;

        ComboItem(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
