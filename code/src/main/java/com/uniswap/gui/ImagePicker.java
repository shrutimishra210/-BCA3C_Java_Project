package com.uniswap.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

/**
 * Photo field for the Add / Edit listing forms: live preview, "Choose Image"
 * (native file picker) and "Remove" / "Use current photo". The file is only
 * uploaded when the form is submitted - this component just validates the
 * choice (type, 5 MB limit, readable) and shows it immediately.
 */
public class ImagePicker extends JPanel {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final ItemImage preview;
    private final RoundedButton chooseBtn;
    private final RoundedButton clearBtn;
    private final JLabel hint = UiTheme.muted("JPG, PNG, WEBP or GIF • up to 5 MB");
    private final String itemName;
    private final String category;
    private final String existingUrl;     // photo the listing already has (edit mode), may be null
    private File selected;

    public ImagePicker(String itemName, String category, String existingUrl) {
        this.itemName = itemName == null || itemName.isBlank() ? "+" : itemName;
        this.category = category;
        this.existingUrl = existingUrl;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        preview = new ItemImage(this.itemName, category, existingUrl, 16, true);
        preview.setPreferredSize(new Dimension(10, 150));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(preview);
        add(Box.createVerticalStrut(8));

        chooseBtn = UiTheme.secondaryButton(existingUrl == null ? "Choose Image" : "Replace Image");
        chooseBtn.addActionListener(e -> chooseFile());
        clearBtn = UiTheme.secondaryButton(existingUrl == null ? "Remove" : "Keep Current");
        clearBtn.setEnabled(false);
        clearBtn.addActionListener(e -> clearSelection());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(chooseBtn);
        row.add(Box.createHorizontalStrut(8));
        row.add(clearBtn);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        add(row);
        add(Box.createVerticalStrut(6));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(hint);
    }

    /** The newly chosen file, or null if the user didn't pick one. */
    public File getSelectedFile() {
        return selected;
    }

    private void chooseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose a photo for your listing");
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, WEBP, GIF)",
                "jpg", "jpeg", "png", "webp", "gif"));
        if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            selectFile(fc.getSelectedFile());
        }
    }

    /** Validates and previews a file. Returns an error message, or null when accepted. */
    public String selectFile(File f) {
        String error = validate(f);
        if (error != null) {
            Dialogs.warn(this, error, "Can't use that image");
            return error;
        }
        BufferedImage img = ImageLoader.readFile(f);
        selected = f;
        preview.setPhoto(img);
        clearBtn.setEnabled(true);
        hint.setText(f.getName());
        return null;
    }

    static String validate(File f) {
        if (f == null || !f.isFile()) return "That file could not be found.";
        String n = f.getName().toLowerCase();
        int dot = n.lastIndexOf('.');
        if (dot < 0 || !EXT.contains(n.substring(dot + 1))) {
            return "Only JPG, JPEG, PNG, WEBP and GIF images are allowed.";
        }
        if (f.length() > MAX_BYTES) {
            return "That image is too large (" + String.format("%.1f", f.length() / 1048576.0)
                    + " MB). The maximum size is 5 MB.";
        }
        if (f.length() == 0 || ImageLoader.readFile(f) == null) {
            return "That image couldn't be read. It may be corrupted or in an unsupported format.";
        }
        return null;
    }

    private void clearSelection() {
        selected = null;
        clearBtn.setEnabled(false);
        hint.setText("JPG, PNG, WEBP or GIF • up to 5 MB");
        preview.setPhoto(null);
        if (existingUrl != null) {
            ImageLoader.load(existingUrl, preview::setPhoto);   // back to the saved photo
        }
    }
}
