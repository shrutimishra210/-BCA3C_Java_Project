package com.uniswap.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.uniswap.dto.TransactionResponse;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Screen: Transactions (Module 5 - transaction/request history). */
public class TransactionsPanel extends JPanel implements Refreshable {

    private final ScrollPanel list = ScrollPanel.column();

    public TransactionsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD - 6));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(PageHeader.build("Transactions", "Everything you've completed on SwapUni.", null));
        top.add(Box.createVerticalStrut(18));
        add(top, BorderLayout.NORTH);

        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(list));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        list.removeAll();
        try {
            List<TransactionResponse> txs = ApiClient.getList("/transactions", new TypeReference<List<TransactionResponse>>() {
            });
            if (txs.isEmpty()) {
                JPanel empty = PageHeader.emptyState(Icons.Kind.CARD, "No completed transactions yet",
                        "Accepted requests will show up here.", null);
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                list.add(empty);
            } else {
                for (TransactionResponse t : txs) {
                    JPanel row = buildRow(t);
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);
                    list.add(row);
                    list.add(Box.createVerticalStrut(10));
                }
            }
        } catch (Exception ex) {
            list.add(UiTheme.body("Couldn't load transactions: " + ex.getMessage()));
        }
        list.revalidate();
        list.repaint();
    }

    private JPanel buildRow(TransactionResponse t) {
        RoundedPanel row = UiTheme.card();
        row.setLayout(new BorderLayout(16, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JComponent icon = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(44, 44);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.PEACH_LIGHT);
                g2.fillRoundRect(0, 0, 43, 43, 14, 14);
                Icons.paint(g2, Icons.Kind.CARD, 11, 11, 22, UiTheme.ACCENT_DARK, false);
                g2.dispose();
            }
        };
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(icon);
        row.add(iconWrap, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        String amountText = t.getAmount() != null ? "  \u2022  " + UiTheme.money(t.getAmount()) : "";
        JPanel top = UiTheme.hbox(10,
                UiTheme.label(t.getListingName(), UiTheme.font(Font.BOLD, 14), UiTheme.BROWN),
                Badge.type(t.getType() + amountText));
        JLabel bottom = UiTheme.muted(t.getRole() + "  \u2022  with " + t.getOtherPartyName() + "  \u2022  " + t.getCompletedAt());

        info.add(top);
        info.add(Box.createVerticalStrut(4));
        info.add(bottom);
        row.add(info, BorderLayout.CENTER);

        JPanel statusWrap = new JPanel(new GridBagLayout());
        statusWrap.setOpaque(false);
        statusWrap.add(Badge.status(t.getStatus()));
        row.add(statusWrap, BorderLayout.EAST);
        return row;
    }
}
