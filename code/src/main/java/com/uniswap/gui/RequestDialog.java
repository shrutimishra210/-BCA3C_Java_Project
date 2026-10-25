package com.uniswap.gui;

import com.uniswap.dto.ListingResponse;
import com.uniswap.dto.RequestCreateRequest;

import javax.swing.*;
import java.awt.*;

/** Small dialog for sending a Buy/Exchange/Borrow/Donate request on a listing. */
public class RequestDialog extends BaseDialog {

    private final ListingResponse item;
    private boolean sent = false;

    private final RoundedTextArea messageArea = new RoundedTextArea(4);
    private final RoundedTextField extraField = new RoundedTextField();

    public RequestDialog(JFrame owner, ListingResponse item) {
        super(owner, "Send Request", 440, 460, "Send Request",
                item.getName() + "  \u2014  " + ListingRow.priceLabel(item));
        this.item = item;
        build();
    }

    private void build() {
        String type = item.getTransactionType();

        RoundedPanel typeCard = new RoundedPanel(14, 0).fill(UiTheme.PEACH_HOVER);
        typeCard.pad(10, 14, 10, 14);
        typeCard.setLayout(new BorderLayout());
        typeCard.add(UiTheme.label("Request type: " + type, UiTheme.LABEL, UiTheme.BROWN), BorderLayout.WEST);
        typeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        content.add(typeCard);
        content.add(Box.createVerticalStrut(16));

        if (type.equals("EXCHANGE") || type.equals("BORROW")) {
            String label = type.equals("EXCHANGE")
                    ? "What would you like to offer?"
                    : "How long would you like to borrow it?";
            extraField.placeholder(type.equals("EXCHANGE") ? "e.g. My old calculator" : "e.g. 2 weeks");
            content.add(Forms.field(label, extraField));
        }

        messageArea.setText("I am interested in this item...");
        messageArea.setPreferredSize(new Dimension(10, 90));
        messageArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(Forms.field("Message", messageArea));

        RoundedButton cancel = UiTheme.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        RoundedButton send = UiTheme.accentButton("Send Request");
        send.addActionListener(e -> doSend(type));
        setFooter(Forms.buttonBar(cancel, send));
        getRootPane().setDefaultButton(send);
    }

    private void doSend(String type) {
        RequestCreateRequest req = new RequestCreateRequest();
        req.setListingId(item.getId());
        req.setType(type);
        req.setMessage(messageArea.getText().trim());
        req.setExtraDetail(extraField.getText().trim());

        try {
            ApiClient.post("/requests", req, com.uniswap.dto.RequestResponse.class);
            sent = true;
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't send request");
        }
    }

    public boolean wasSent() {
        return sent;
    }
}
