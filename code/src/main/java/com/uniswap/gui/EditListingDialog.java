package com.uniswap.gui;

import com.uniswap.dto.CategoryResponse;
import com.uniswap.dto.ListingRequest;
import com.uniswap.dto.ListingResponse;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Screen: Edit Listing (Module 2 / Module 5 - "Edit own listing"). */
public class EditListingDialog extends BaseDialog {

    private final ListingResponse original;
    private boolean saved = false;

    private final RoundedTextField nameField = new RoundedTextField();
    private final RoundedTextArea descArea = new RoundedTextArea(3);
    private final RoundedComboBox<CategoryResponse> categoryCombo = new RoundedComboBox<>();
    private final RoundedComboBox<String> conditionCombo =
            new RoundedComboBox<>(new String[]{"NEW", "LIKE_NEW", "GOOD", "FAIR"});
    private final RoundedComboBox<String> typeCombo =
            new RoundedComboBox<>(new String[]{"SELL", "EXCHANGE", "BORROW", "DONATE"});
    private final RoundedTextField priceField = new RoundedTextField();
    private final RoundedTextField locationField = new RoundedTextField();
    private JPanel priceFieldWrap;
    private ImagePicker imagePicker;

    public EditListingDialog(JFrame owner, ListingResponse original, List<CategoryResponse> categories) {
        super(owner, "Edit Listing", 480, 720, "Edit Listing", "Update the details of your listing.");
        this.original = original;
        build(categories);
        prefill(categories);
    }

    private void build(List<CategoryResponse> categories) {
        for (CategoryResponse c : categories) categoryCombo.addItem(c);
        typeCombo.addActionListener(e -> updatePriceVisibility());
        descArea.setPreferredSize(new Dimension(10, 80));
        descArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        imagePicker = new ImagePicker(original.getName(), original.getCategoryName(), original.getImageUrl());
        content.add(Forms.field("Photo", imagePicker));
        content.add(Forms.field("Item Name", nameField));
        content.add(Forms.field("Description", descArea));
        content.add(Forms.field("Category", categoryCombo));
        content.add(Forms.row(Forms.field("Condition", conditionCombo), Forms.field("Transaction Type", typeCombo)));
        priceFieldWrap = Forms.field("Price (\u20B9)", priceField);
        content.add(priceFieldWrap);
        content.add(Forms.field("Campus Location", locationField));

        RoundedButton cancel = UiTheme.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        RoundedButton save = UiTheme.accentButton("Save Changes");
        save.addActionListener(e -> onSubmit());
        setFooter(Forms.buttonBar(cancel, save));
        getRootPane().setDefaultButton(save);
    }

    private void prefill(List<CategoryResponse> categories) {
        nameField.setText(original.getName());
        descArea.setText(original.getDescription());
        locationField.setText(original.getLocation());
        conditionCombo.setSelectedItem(original.getCondition());
        typeCombo.setSelectedItem(original.getTransactionType());
        if (original.getPrice() != null) {
            priceField.setText(String.valueOf(original.getPrice().intValue()));
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(original.getCategoryId())) {
                categoryCombo.setSelectedIndex(i);
                break;
            }
        }
        updatePriceVisibility();
    }

    private void updatePriceVisibility() {
        boolean isSell = "SELL".equals(typeCombo.getSelectedItem());
        priceFieldWrap.setVisible(isSell);
    }

    private void onSubmit() {
        if (nameField.getText().isBlank() || descArea.getText().isBlank() || locationField.getText().isBlank()) {
            Dialogs.warn(this, "Please fill in all required fields.", "Missing information");
            return;
        }
        CategoryResponse category = (CategoryResponse) categoryCombo.getSelectedItem();
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
                            + "\nYour changes were not saved - fix or remove the photo and try again.", "Image upload failed");
                    return;
                }
            }
            ApiClient.put("/listings/" + original.getId(), req, ListingResponse.class);
            saved = true;
            Dialogs.info(this, "Listing updated successfully!", "Success");
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't save changes");
        }
    }

    public boolean wasSaved() {
        return saved;
    }
}
