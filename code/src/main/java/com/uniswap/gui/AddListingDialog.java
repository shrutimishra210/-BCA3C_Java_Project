package com.uniswap.gui;

import com.uniswap.dto.CategoryResponse;
import com.uniswap.dto.ListingRequest;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Screen: Add Listing (Module 2). */
public class AddListingDialog extends BaseDialog {

    private final List<CategoryResponse> categories;
    private boolean saved = false;

    private final RoundedTextField nameField = new RoundedTextField();
    private final RoundedTextArea descArea = new RoundedTextArea(3);
    private final RoundedComboBox<CategoryResponse> categoryCombo = new RoundedComboBox<>();
    private final RoundedComboBox<String> conditionCombo =
            new RoundedComboBox<>(new String[]{"NEW", "LIKE_NEW", "GOOD", "FAIR"});
    private final RoundedComboBox<String> typeCombo =
            new RoundedComboBox<>(new String[]{"SELL", "EXCHANGE", "BORROW", "DONATE"});
    private final RoundedTextField priceField = new RoundedTextField();
    private JPanel priceFieldWrap;
    private final ImagePicker imagePicker = new ImagePicker(null, null, null);

    public AddListingDialog(JFrame owner, List<CategoryResponse> categories) {
        super(owner, "List an Item", 480, 720, "List an Item", "Share something a fellow student could use.");
        this.categories = categories;
        build("Save Listing");
    }

    private void build(String submitLabel) {
        nameField.placeholder("e.g. Scientific Calculator");
        descArea.getTextArea().setText("");
        descArea.setPreferredSize(new Dimension(10, 80));
        descArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (CategoryResponse c : categories) categoryCombo.addItem(c);
        typeCombo.addActionListener(e -> updatePriceVisibility());
        priceField.placeholder("e.g. 500");

        content.add(Forms.field("Photo (optional)", imagePicker));
        content.add(Forms.field("Item Name", nameField));
        content.add(Forms.field("Description", descArea));
        content.add(Forms.field("Category", categoryCombo));
        content.add(Forms.row(Forms.field("Condition", conditionCombo), Forms.field("Transaction Type", typeCombo)));
        priceFieldWrap = Forms.field("Price (\u20B9)", priceField);
        content.add(priceFieldWrap);
        content.add(Forms.field("Campus Location", locationFieldWithPlaceholder()));

        RoundedButton cancel = UiTheme.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        RoundedButton submit = UiTheme.accentButton(submitLabel);
        submit.addActionListener(e -> onSubmit());
        setFooter(Forms.buttonBar(cancel, submit));
        getRootPane().setDefaultButton(submit);

        updatePriceVisibility();
    }

    private final RoundedTextField locationField = new RoundedTextField();

    private RoundedTextField locationFieldWithPlaceholder() {
        locationField.placeholder("e.g. Block A Hostel");
        return locationField;
    }

    private void updatePriceVisibility() {
        boolean isSell = "SELL".equals(typeCombo.getSelectedItem());
        priceFieldWrap.setVisible(isSell);
    }

    protected void onSubmit() {
        if (nameField.getText().isBlank() || descArea.getText().isBlank() || locationField.getText().isBlank()) {
            Dialogs.warn(this, "Please fill in all required fields.", "Missing information");
            return;
        }
        CategoryResponse category = (CategoryResponse) categoryCombo.getSelectedItem();
        if (category == null) {
            Dialogs.warn(this, "Please choose a category.", "Missing information");
            return;
        }

        String type = (String) typeCombo.getSelectedItem();
        Double price = null;
        if ("SELL".equals(type)) {
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                Dialogs.warn(this, "Please enter a valid price greater than zero.", "Invalid price");
                return;
            }
        }

        ListingRequest req = new ListingRequest();
        req.setName(nameField.getText().trim());
        req.setDescription(descArea.getText().trim());
        req.setCategoryId(category.getId());
        req.setCondition((String) conditionCombo.getSelectedItem());
        req.setTransactionType(type);
        req.setPrice(price);
        req.setLocation(locationField.getText().trim());

        try {
            if (imagePicker.getSelectedFile() != null) {
                try {
                    req.setImageUrl(ApiClient.uploadImage(imagePicker.getSelectedFile()));
                } catch (Exception upEx) {
                    Dialogs.error(this, "The image couldn't be uploaded: " + upEx.getMessage()
                            + "\nYour listing was not saved - fix or remove the photo and try again.", "Image upload failed");
                    return;
                }
            }
            submit(req);
            saved = true;
            Dialogs.info(this, successMessage(), "Success");
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't save listing");
        }
    }

    /** Overridden by EditListingDialog to PUT instead of POST. */
    protected void submit(ListingRequest req) {
        ApiClient.post("/listings", req, com.uniswap.dto.ListingResponse.class);
    }

    protected String successMessage() {
        return "Your item has been listed successfully!";
    }

    public boolean wasSaved() {
        return saved;
    }
}
