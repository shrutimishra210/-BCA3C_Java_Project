package com.uniswap.gui;

import com.uniswap.dto.DashboardResponse;
import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;

/** Screen: Main Dashboard (Module 6). */
public class DashboardPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final ScrollPanel body = ScrollPanel.column();

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD - 6));

        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(body));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        body.removeAll();
        try {
            DashboardResponse dto = ApiClient.get("/dashboard", DashboardResponse.class);
            buildBody(dto);
        } catch (Exception ex) {
            body.add(UiTheme.body("Couldn't load dashboard: " + ex.getMessage()));
        }
        body.revalidate();
        body.repaint();
    }

    private void buildBody(DashboardResponse dto) {
        JLabel welcome = UiTheme.label("Good " + greeting() + ", " + firstName(dto.getWelcomeName()) + " \uD83D\uDC4B",
                UiTheme.font(Font.BOLD, 27), UiTheme.BROWN);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(welcome);

        JLabel sub = UiTheme.label("Here's what's happening on your campus marketplace.", UiTheme.BODY, UiTheme.BROWN_SOFT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sub);
        body.add(Box.createVerticalStrut(22));

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        stats.add(PageHeader.statCard(Icons.Kind.BAG, String.valueOf(dto.getTotalActiveListings()), "Active Listings"));
        stats.add(PageHeader.statCard(Icons.Kind.HEART, String.valueOf(dto.getTotalFavourites()), "Favourite Items"));
        stats.add(PageHeader.statCard(Icons.Kind.BOX, String.valueOf(dto.getPendingRequests()), "Pending Requests"));
        body.add(stats);
        body.add(Box.createVerticalStrut(28));

        JLabel quickTitle = UiTheme.heading("Quick Actions");
        quickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(quickTitle);
        body.add(Box.createVerticalStrut(10));

        JPanel actions = new JPanel(new GridLayout(1, 4, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        actions.add(quickAction("List an Item", Icons.Kind.PLUS, "Marketplace"));
        actions.add(quickAction("Browse Marketplace", Icons.Kind.SEARCH, "Marketplace"));
        actions.add(quickAction("View Favourites", Icons.Kind.HEART, "Favourites"));
        actions.add(quickAction("My Requests", Icons.Kind.BOX, "Requests"));
        body.add(actions);
        body.add(Box.createVerticalStrut(28));

        if (dto.getPopularCategories() != null && !dto.getPopularCategories().isEmpty()) {
            JLabel popTitle = UiTheme.heading("Popular Categories");
            popTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(popTitle);
            body.add(Box.createVerticalStrut(10));

            JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            chipRow.setOpaque(false);
            chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String cat : dto.getPopularCategories()) {
                RoundedButton chip = new RoundedButton(cat, RoundedButton.Style.CHIP);
                chip.addActionListener(e -> mainFrame.showCard("Marketplace"));
                chipRow.add(chip);
            }
            body.add(chipRow);
            body.add(Box.createVerticalStrut(28));
        }

        JLabel recentTitle = UiTheme.heading("Recent Marketplace Items");
        recentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(recentTitle);
        body.add(Box.createVerticalStrut(12));

        if (dto.getRecentListings() == null || dto.getRecentListings().isEmpty()) {
            JPanel empty = PageHeader.emptyState(Icons.Kind.BAG, "No listings yet",
                    "Be the first to list something on your campus.", null);
            body.add(empty);
        } else {
            // Side-by-side cards that use the full width instead of one long stack.
            CardGrid recent = new CardGrid(ListingRow.MIN_WIDTH, 14, ListingRow.COMPACT_HEIGHT);
            recent.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (ListingResponse item : dto.getRecentListings()) {
                recent.add(ListingRow.build(item, () -> mainFrame.showCard("Marketplace")));
            }
            body.add(recent);
        }
    }

    private RoundedButton quickAction(String label, Icons.Kind icon, String target) {
        RoundedButton b = UiTheme.secondaryButton(label).icon(icon);
        b.addActionListener(e -> mainFrame.showCard(target));
        return b;
    }

    private String greeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "morning";
        if (hour < 17) return "afternoon";
        return "evening";
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "there";
        return fullName.split(" ")[0];
    }
}
