package com.uniswap.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.uniswap.dto.CategoryResponse;
import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Screen: My Listings (Module 5). */
public class MyListingsPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
    private final ScrollPanel list = ScrollPanel.column();
    private List<CategoryResponse> categories = List.of();

    public MyListingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD - 6));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(PageHeader.build("My Listings", "Manage the items you've shared with campus.", null));
        top.add(Box.createVerticalStrut(20));

        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        top.add(statsRow);
        top.add(Box.createVerticalStrut(20));

        add(top, BorderLayout.NORTH);

        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(list));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            categories = ApiClient.getList("/categories", new TypeReference<List<CategoryResponse>>() {
            });
        } catch (Exception ignored) {
            // Editing will just show an empty category list if this fails; harmless.
        }

        list.removeAll();
        statsRow.removeAll();
        try {
            List<ListingResponse> mine = ApiClient.getList("/listings/mine", new TypeReference<List<ListingResponse>>() {
            });

            long active = mine.stream().filter(l -> "ACTIVE".equals(l.getStatus())).count();
            long completed = mine.size() - active;

            statsRow.add(PageHeader.statCard(Icons.Kind.LIST, String.valueOf(mine.size()), "Total Listings"));
            statsRow.add(PageHeader.statCard(Icons.Kind.SPARK, String.valueOf(active), "Active"));
            statsRow.add(PageHeader.statCard(Icons.Kind.CHECK, String.valueOf(completed), "Completed"));
            statsRow.add(PageHeader.statCard(Icons.Kind.CARD,
                    String.valueOf(mine.stream().filter(l -> "SOLD".equals(l.getStatus())).count()), "Sold"));

            if (mine.isEmpty()) {
                RoundedButton addBtn = UiTheme.accentButton("List an Item");
                addBtn.addActionListener(e -> mainFrame.showCard("Marketplace"));
                JPanel empty = PageHeader.emptyState(Icons.Kind.LIST, "You haven't listed anything yet",
                        "Turn your unused stuff into something useful.", addBtn);
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                list.add(empty);
            } else {
                CardGrid grid = new CardGrid(ListingRow.MIN_WIDTH + 30, 14, ListingRow.ACTIONS_HEIGHT);
                grid.setAlignmentX(Component.LEFT_ALIGNMENT);
                for (ListingResponse item : mine) {
                    grid.add(buildRow(item));
                }
                list.add(grid);
            }
        } catch (Exception ex) {
            list.add(UiTheme.body("Couldn't load your listings: " + ex.getMessage()));
        }
        statsRow.revalidate();
        statsRow.repaint();
        list.revalidate();
        list.repaint();
    }

    private JPanel buildRow(ListingResponse item) {
        RoundedButton edit = UiTheme.secondaryButton("Edit");
        edit.addActionListener(e -> {
            EditListingDialog dialog = new EditListingDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), item, categories);
            dialog.setVisible(true);
            if (dialog.wasSaved()) refresh();
        });

        java.util.List<JComponent> extras = new java.util.ArrayList<>();
        extras.add(edit);

        if ("ACTIVE".equals(item.getStatus())) {
            RoundedButton markSold = UiTheme.secondaryButton(soldLabel(item.getTransactionType()));
            markSold.addActionListener(e -> {
                try {
                    ApiClient.put("/listings/" + item.getId() + "/status?status=" + statusFor(item.getTransactionType()));
                    refresh();
                } catch (Exception ex) {
                    Dialogs.error(this, ex.getMessage(), "Couldn't update");
                }
            });
            extras.add(markSold);
        }

        RoundedButton delete = UiTheme.dangerButton("Delete");
        delete.addActionListener(e -> confirmDelete(item));
        extras.add(delete);

        return ListingRow.build(item, () -> {
            ItemDetailsDialog dialog = new ItemDetailsDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), item.getId());
            dialog.setVisible(true);
            if (dialog.needsRefresh()) refresh();
        }, extras);
    }

    private String soldLabel(String type) {
        return switch (type) {
            case "EXCHANGE" -> "Mark Exchanged";
            case "BORROW" -> "Mark Borrowed";
            case "DONATE" -> "Mark Donated";
            default -> "Mark as Sold";
        };
    }

    private String statusFor(String type) {
        return switch (type) {
            case "EXCHANGE" -> "EXCHANGED";
            case "BORROW" -> "BORROWED";
            case "DONATE" -> "DONATED";
            default -> "SOLD";
        };
    }

    private void confirmDelete(ListingResponse item) {
        boolean ok = Dialogs.confirm(this, "Delete \"" + item.getName() + "\"? This can't be undone.",
                "Delete Listing", "Delete", true);
        if (ok) {
            try {
                ApiClient.delete("/listings/" + item.getId());
                refresh();
            } catch (Exception ex) {
                Dialogs.error(this, ex.getMessage(), "Couldn't delete");
            }
        }
    }
}
