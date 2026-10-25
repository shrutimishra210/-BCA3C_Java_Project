package com.uniswap.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.uniswap.dto.RequestResponse;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Screen: Requests (Module 3) - Incoming and Outgoing, switched with a segmented control. */
public class RequestsPanel extends JPanel implements Refreshable {

    private final ScrollPanel incomingList = ScrollPanel.column();
    private final ScrollPanel outgoingList = ScrollPanel.column();
    private final CardLayout tabsLayout = new CardLayout();
    private final JPanel tabsContent = new JPanel(tabsLayout);
    private RoundedButton incomingTab;
    private RoundedButton outgoingTab;

    public RequestsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD - 6));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(PageHeader.build("Requests", "Track requests you've received and sent.", null));
        top.add(Box.createVerticalStrut(18));

        incomingTab = new RoundedButton("Incoming Requests", RoundedButton.Style.CHIP);
        outgoingTab = new RoundedButton("Sent Requests", RoundedButton.Style.CHIP);
        incomingTab.setActive(true);
        incomingTab.addActionListener(e -> selectTab(true));
        outgoingTab.addActionListener(e -> selectTab(false));
        RoundedPanel segment = new RoundedPanel(14, 0).fill(UiTheme.PEACH_HOVER);
        segment.pad(4, 4, 4, 4);
        segment.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        segment.add(incomingTab);
        segment.add(outgoingTab);
        segment.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(wrapLeft(segment));
        top.add(Box.createVerticalStrut(18));
        add(top, BorderLayout.NORTH);

        tabsContent.setOpaque(false);
        tabsContent.add(scrollWrap(incomingList), "incoming");
        tabsContent.add(scrollWrap(outgoingList), "outgoing");
        add(tabsContent, BorderLayout.CENTER);
    }

    private JPanel wrapLeft(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(c);
        return p;
    }

    private void selectTab(boolean incoming) {
        incomingTab.setActive(incoming);
        outgoingTab.setActive(!incoming);
        tabsLayout.show(tabsContent, incoming ? "incoming" : "outgoing");
    }

    private JScrollPane scrollWrap(JPanel content) {
        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(content));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    @Override
    public void refresh() {
        loadIncoming();
        loadOutgoing();
    }

    private void loadIncoming() {
        incomingList.removeAll();
        try {
            List<RequestResponse> requests = ApiClient.getList("/requests/incoming",
                    new TypeReference<List<RequestResponse>>() {
                    });
            if (requests.isEmpty()) {
                incomingList.add(emptyState("No incoming requests", "You're all caught up!"));
            } else {
                for (RequestResponse r : requests) {
                    JPanel row = buildIncomingRow(r);
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);
                    incomingList.add(row);
                    incomingList.add(Box.createVerticalStrut(10));
                }
            }
        } catch (Exception ex) {
            incomingList.add(UiTheme.body("Couldn't load requests: " + ex.getMessage()));
        }
        incomingList.revalidate();
        incomingList.repaint();
    }

    private void loadOutgoing() {
        outgoingList.removeAll();
        try {
            List<RequestResponse> requests = ApiClient.getList("/requests/outgoing",
                    new TypeReference<List<RequestResponse>>() {
                    });
            if (requests.isEmpty()) {
                outgoingList.add(emptyState("No sent requests", "You haven't sent any requests yet."));
            } else {
                for (RequestResponse r : requests) {
                    JPanel row = buildOutgoingRow(r);
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);
                    outgoingList.add(row);
                    outgoingList.add(Box.createVerticalStrut(10));
                }
            }
        } catch (Exception ex) {
            outgoingList.add(UiTheme.body("Couldn't load requests: " + ex.getMessage()));
        }
        outgoingList.revalidate();
        outgoingList.repaint();
    }

    private JPanel emptyState(String headline, String text) {
        JPanel p = PageHeader.emptyState(Icons.Kind.BOX, headline, text, null);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JPanel buildIncomingRow(RequestResponse r) {
        JPanel row = baseRow(r, "From " + r.getRequesterName());
        if ("PENDING".equals(r.getStatus())) {
            RoundedButton accept = UiTheme.primaryButton("Accept").icon(Icons.Kind.CHECK);
            accept.addActionListener(e -> act(() -> ApiClient.put("/requests/" + r.getId() + "/accept")));
            RoundedButton reject = UiTheme.dangerButton("Reject");
            reject.addActionListener(e -> act(() -> ApiClient.put("/requests/" + r.getId() + "/reject")));
            JPanel actions = (JPanel) row.getClientProperty("actions");
            actions.add(accept);
            actions.add(reject);
        }
        return row;
    }

    private JPanel buildOutgoingRow(RequestResponse r) {
        JPanel row = baseRow(r, "To " + r.getOwnerName());
        if ("PENDING".equals(r.getStatus())) {
            RoundedButton cancel = UiTheme.secondaryButton("Cancel Request");
            cancel.addActionListener(e -> act(() -> ApiClient.put("/requests/" + r.getId() + "/cancel")));
            JPanel actions = (JPanel) row.getClientProperty("actions");
            actions.add(cancel);
        }
        return row;
    }

    private JPanel baseRow(RequestResponse r, String personLine) {
        RoundedPanel row = UiTheme.card();
        row.setLayout(new BorderLayout(16, 4));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        String priceSuffix = r.getListingPrice() != null && "SELL".equals(r.getType())
                ? " (" + UiTheme.money(r.getListingPrice()) + ")" : "";
        JPanel itemLine = UiTheme.hbox(10,
                UiTheme.label(r.getListingName(), UiTheme.font(Font.BOLD, 14), UiTheme.BROWN),
                Badge.type(r.getType() + priceSuffix));

        JLabel person = UiTheme.muted(personLine);
        JLabel message = UiTheme.muted("\u201C" + safe(r.getMessage()) + "\u201D"
                + (r.getExtraDetail() != null && !r.getExtraDetail().isBlank() ? "  \u2022  " + r.getExtraDetail() : ""));

        info.add(itemLine);
        info.add(Box.createVerticalStrut(4));
        info.add(person);
        info.add(message);
        info.add(Box.createVerticalStrut(6));
        info.add(Badge.status(r.getStatus()));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        row.putClientProperty("actions", actions);

        row.add(info, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void act(Runnable action) {
        try {
            action.run();
            refresh();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Action failed");
        }
    }
}
