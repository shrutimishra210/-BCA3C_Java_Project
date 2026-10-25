package com.uniswap.gui;

import com.uniswap.dto.ProfileResponse;
import com.uniswap.dto.ProfileUpdateRequest;

import javax.swing.*;
import java.awt.*;

/** Dialog used by the Profile screen's "Edit Profile" button. */
public class EditProfileDialog extends BaseDialog {

    private final RoundedTextField fullNameField;
    private final RoundedTextField courseField;
    private final RoundedTextField yearField;
    private final RoundedTextField campusField;
    private final RoundedTextArea bioArea = new RoundedTextArea(3);
    private boolean saved = false;

    public EditProfileDialog(Window owner, ProfileResponse current) {
        super(owner, "Edit Profile", 460, 520, "Edit Profile", "Update how other students see you.");

        fullNameField = new RoundedTextField(current.getFullName());
        courseField = new RoundedTextField(current.getCourse());
        yearField = new RoundedTextField(current.getYear());
        campusField = new RoundedTextField(current.getCampus());
        bioArea.setText(current.getBio());
        bioArea.setPreferredSize(new Dimension(10, 90));
        bioArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        bioArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(Forms.field("Full Name", fullNameField));
        content.add(Forms.row(Forms.field("Course", courseField), Forms.field("Year", yearField)));
        content.add(Forms.field("Campus", campusField));
        content.add(Forms.field("Bio", bioArea));

        RoundedButton cancel = UiTheme.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        RoundedButton save = UiTheme.accentButton("Save Changes");
        save.addActionListener(e -> onSave());
        setFooter(Forms.buttonBar(cancel, save));
        getRootPane().setDefaultButton(save);
    }

    private void onSave() {
        try {
            ProfileUpdateRequest req = new ProfileUpdateRequest();
            req.setFullName(fullNameField.getText().trim());
            req.setCourse(courseField.getText().trim());
            req.setYear(yearField.getText().trim());
            req.setCampus(campusField.getText().trim());
            req.setBio(bioArea.getText().trim());

            ApiClient.put("/auth/profile", req, ProfileResponse.class);
            Session.set(Session.getUserId(), req.getFullName());
            saved = true;
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Couldn't update profile");
        }
    }

    public boolean wasSaved() {
        return saved;
    }
}
