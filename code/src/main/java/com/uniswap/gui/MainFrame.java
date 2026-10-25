package com.uniswap.gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The post-login shell: a left navigation sidebar plus a CardLayout content
 * area. Every major screen (Dashboard, Marketplace, Favourites, Requests,
 * My Listings, Transactions, Profile) lives inside this one resizable window
 * and is swapped in and out.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JComponent> panels = new LinkedHashMap<>();
    private final Map<String, RoundedButton> navButtons = new LinkedHashMap<>();

    private MarketplacePanel marketplacePanel;

    public MainFrame() {
        super("SwapUni - " + Session.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(940, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        showCard("Dashboard");
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UiTheme.CHARCOAL_OLIVE);
        sidebar.setPreferredSize(new Dimension(248, 10));
        
        // --- logo -------------------------------------------------
        JPanel logo = new JPanel();
        logo.setOpaque(false);
        logo.setLayout(new BoxLayout(logo, BoxLayout.X_AXIS));
        logo.setBorder(UiTheme.pad(26, 22, 22, 22));
        LogoMark mark = new LogoMark(40);
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel brand = UiTheme.label("SwapUni", UiTheme.font(Font.BOLD, 21), UiTheme.SIDEBAR_TEXT);
        JLabel tag = UiTheme.label("Campus marketplace", UiTheme.font(Font.PLAIN, 11), UiTheme.SIDEBAR_MUTED);
        names.add(brand);
        names.add(tag);
        logo.add(mark);
        logo.add(Box.createHorizontalStrut(12));
        logo.add(names);
        sidebar.add(logo, BorderLayout.NORTH);

        // --- navigation ---------------------------------------------
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(UiTheme.pad(4, 14, 4, 14));
        addNav(nav, "Dashboard", Icons.Kind.HOME);
        addNav(nav, "Marketplace", Icons.Kind.BAG);
        addNav(nav, "Favourites", Icons.Kind.HEART);
        addNav(nav, "Requests", Icons.Kind.BOX);
        addNav(nav, "My Listings", Icons.Kind.LIST);
        addNav(nav, "Transactions", Icons.Kind.CARD);
        addNav(nav, "Profile", Icons.Kind.USER);
        sidebar.add(nav, BorderLayout.CENTER);

        // --- bottom: user + logout ------------------------------------
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(UiTheme.pad(10, 14, 20, 14));

        RoundedPanel user = new RoundedPanel(16, 0).fill(UiTheme.SIDEBAR_HOVER)
                .borderColor(UiTheme.mix(UiTheme.CHARCOAL_OLIVE, UiTheme.WHITE, 0.18));
        user.pad(10, 12, 10, 12);
        user.setLayout(new BorderLayout(10, 0));
        user.add(new Avatar(Session.getFullName(), 36), BorderLayout.WEST);
        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        JLabel nm = UiTheme.label(Session.getFullName(), UiTheme.font(Font.BOLD, 13), UiTheme.SIDEBAR_TEXT);
        JLabel st = UiTheme.label("Signed in", UiTheme.SMALL, UiTheme.SIDEBAR_MUTED);
        who.add(Box.createVerticalGlue());
        who.add(nm);
        who.add(st);
        who.add(Box.createVerticalGlue());
        user.add(who, BorderLayout.CENTER);
        user.setAlignmentX(Component.LEFT_ALIGNMENT);
        user.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        bottom.add(user);
        bottom.add(Box.createVerticalStrut(10));

        RoundedButton logout = new RoundedButton("Log Out", RoundedButton.Style.NAV)
                .icon(Icons.Kind.LOGOUT).dangerText();
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        logout.addActionListener(e -> doLogout());
        bottom.add(logout);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNav(JPanel nav, String label, Icons.Kind icon) {
        RoundedButton b = new RoundedButton(label, RoundedButton.Style.NAV).icon(icon);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.addActionListener(e -> showCard(label));
        navButtons.put(label, b);
        nav.add(b);
        nav.add(Box.createVerticalStrut(4));
    }

    private JComponent buildContent() {
        content.setBackground(UiTheme.BG);

        DashboardPanel dashboardPanel = new DashboardPanel(this);
        marketplacePanel = new MarketplacePanel(this);
        FavouritesPanel favouritesPanel = new FavouritesPanel(this);
        RequestsPanel requestsPanel = new RequestsPanel();
        MyListingsPanel myListingsPanel = new MyListingsPanel(this);
        TransactionsPanel transactionsPanel = new TransactionsPanel();
        ProfilePanel profilePanel = new ProfilePanel();

        panels.put("Dashboard", dashboardPanel);
        panels.put("Marketplace", marketplacePanel);
        panels.put("Favourites", favouritesPanel);
        panels.put("Requests", requestsPanel);
        panels.put("My Listings", myListingsPanel);
        panels.put("Transactions", transactionsPanel);
        panels.put("Profile", profilePanel);

        panels.forEach((name, panel) -> content.add(panel, name));
        return content;
    }

    /** Used by panels (e.g. Dashboard's Quick Actions) to jump between screens. */
    public void showCard(String name) {
        cardLayout.show(content, name);
        navButtons.forEach((n, b) -> b.setActive(n.equals(name)));
        JComponent panel = panels.get(name);
        if (panel instanceof Refreshable r) {
            r.refresh();
        }
    }

    public void showMarketplaceFilteredByCategory(Long categoryId) {
        showCard("Marketplace");
        marketplacePanel.filterByCategory(categoryId);
    }

    private void doLogout() {
        if (Dialogs.confirm(this, "Are you sure you want to log out of SwapUni?",
                "Confirm Logout", "Log Out", false)) {
            Session.clear();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
