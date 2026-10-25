package com.uniswap.gui;

import com.uniswap.dto.ProfileResponse;
import com.uniswap.dto.ProfileUpdateRequest;

import javax.swing.*;
import java.awt.*;

/** Screen: Profile (Module 1 - profile + edit profile). */
public class ProfilePanel extends JPanel implements Refreshable {

    private Avatar avatar;
    private final JPanel avatarWrap = new JPanel(new BorderLayout());
    private final JLabel nameLabel = UiTheme.label("", UiTheme.font(Font.BOLD, 22), UiTheme.BROWN);
    private final JLabel metaLabel = new JLabel();
    private final JLabel bioLabel = new JLabel();
    private ProfileResponse current;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setBorder(UiTheme.pad(UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD, UiTheme.PAGE_PAD - 6));

        RoundedPanel header = UiTheme.card();
        header.setLayout(new BorderLayout(20, 0));
        header.pad(22, 24, 22, 24);

        avatarWrap.setOpaque(false);
        header.add(avatarWrap, BorderLayout.WEST);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        metaLabel.setForeground(UiTheme.BROWN_SOFT);
        metaLabel.setFont(UiTheme.BODY);
        textCol.add(Box.createVerticalGlue());
        textCol.add(nameLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(metaLabel);
        textCol.add(Box.createVerticalGlue());
        header.add(textCol, BorderLayout.CENTER);

        RoundedButton editBtn = UiTheme.secondaryButton("Edit Profile");
        editBtn.addActionListener(e -> openEditDialog());
        JPanel editWrap = new JPanel(new GridBagLayout());
        editWrap.setOpaque(false);
        editWrap.add(editBtn);
        header.add(editWrap, BorderLayout.EAST);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(PageHeader.build("Profile", null, null));
        top.add(Box.createVerticalStrut(16));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        add(top, BorderLayout.NORTH);

        RoundedPanel bioCard = UiTheme.card();
        bioCard.pad(22, 24, 22, 24);
        bioCard.setLayout(new BoxLayout(bioCard, BoxLayout.Y_AXIS));
        JLabel bioTitle = UiTheme.heading("About");
        bioTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        bioLabel.setForeground(UiTheme.BROWN_SOFT);
        bioLabel.setFont(UiTheme.BODY);
        bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bioCard.add(bioTitle);
        bioCard.add(Box.createVerticalStrut(10));
        bioCard.add(bioLabel);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(UiTheme.pad(20, 0, 0, 0));
        centerWrap.add(bioCard, BorderLayout.NORTH);
        add(centerWrap, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            current = ApiClient.get("/auth/profile", ProfileResponse.class);
            avatarWrap.removeAll();
            avatarWrap.add(new Avatar(current.getFullName(), 76), BorderLayout.CENTER);
            avatarWrap.revalidate();
            avatarWrap.repaint();

            nameLabel.setText(current.getFullName());
            StringBuilder meta = new StringBuilder();
            if (current.getCourse() != null && !current.getCourse().isBlank()) meta.append(current.getCourse());
            if (current.getYear() != null && !current.getYear().isBlank()) meta.append("  \u2022  ").append(current.getYear());
            if (current.getCampus() != null && !current.getCampus().isBlank()) meta.append("  \u2022  ").append(current.getCampus());
            metaLabel.setText("<html>" + meta + "<br>" + current.getEmail() + "</html>");
            bioLabel.setText("<html><div style='width:480px'>"
                    + (current.getBio() == null || current.getBio().isBlank() ? "No bio added yet." : escape(current.getBio()))
                    + "</div></html>");
        } catch (Exception ex) {
            nameLabel.setText("Couldn't load profile");
            metaLabel.setText(ex.getMessage());
        }
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void openEditDialog() {
        if (current == null) return;
        EditProfileDialog dialog = new EditProfileDialog((JFrame) SwingUtilities.getWindowAncestor(this), current);
        dialog.setVisible(true);
        if (dialog.wasSaved()) {
            Dialogs.info(this, "Profile updated.", "Success");
            refresh();
        }
    }
}
