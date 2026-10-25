package com.uniswap.gui;

import com.uniswap.dto.AuthResponse;
import com.uniswap.dto.SignUpRequest;

import javax.swing.*;
import java.awt.*;

/** Screen 3: Sign Up. */
public class SignUpFrame extends JFrame {

    private final RoundedTextField fullNameField = new RoundedTextField();
    private final RoundedTextField emailField = new RoundedTextField();
    private final RoundedTextField usernameField = new RoundedTextField();
    private final RoundedPasswordField passwordField = new RoundedPasswordField();
    private final RoundedTextField courseField = new RoundedTextField();
    private final RoundedTextField yearField = new RoundedTextField();
    private final RoundedTextField campusField = new RoundedTextField();

    public SignUpFrame() {
        super("SwapUni - Create Account");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);      // resizable + maximizable

        JPanel form = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(480, super.getPreferredSize().height);   // fixed readable width
            }
        };
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(UiTheme.pad(30, 44, 26, 44));

        JLabel title = UiTheme.label("Create your account", UiTheme.serif(Font.BOLD, 30), UiTheme.BROWN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UiTheme.label("Join your campus marketplace.", UiTheme.BODY, UiTheme.BROWN_SOFT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(sub);
        form.add(Box.createVerticalStrut(22));

        emailField.placeholder("you@college.edu");
        form.add(Forms.row(Forms.field("Full Name *", fullNameField), Forms.field("Username *", usernameField)));
        form.add(Forms.field("College Email *", emailField));
        form.add(Forms.field("Password * (min 6 characters)", passwordField));
        form.add(Forms.row(Forms.field("Course", courseField), Forms.field("Year", yearField)));
        form.add(Forms.field("Campus / Hostel Block", campusField));
        form.add(Box.createVerticalStrut(8));

        RoundedButton createBtn = UiTheme.accentButton("Create Account");
        createBtn.addActionListener(e -> doSignUp());
        form.add(fullWidth(createBtn));
        form.add(Box.createVerticalStrut(10));

        RoundedButton backBtn = UiTheme.secondaryButton("Already have an account? Log In");
        backBtn.addActionListener(e -> {
            LoginFrame next = new LoginFrame();
            next.setBounds(getBounds());
            next.setExtendedState(getExtendedState());
            next.setVisible(true);
            dispose();
        });
        form.add(fullWidth(backBtn));

        JPanel centred = new JPanel(new GridBagLayout());   // keeps the form centred when maximized
        centred.setOpaque(false);
        centred.add(form);
        JScrollPane scroll = UiTheme.styleScroll(new JScrollPane(centred));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setOpaque(false);
        JPanel formSide = new JPanel(new BorderLayout());
        formSide.setBackground(UiTheme.BG);
        formSide.add(scroll, BorderLayout.CENTER);

        getRootPane().setDefaultButton(createBtn);
        setContentPane(BrandPanel.split(new BrandPanel(), formSide));
    }

    private JComponent fullWidth(JComponent c) {
        JPanel p = new JPanel(new GridLayout(1, 1));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.add(c);
        return p;
    }

    private void doSignUp() {
        if (fullNameField.getText().isBlank() || emailField.getText().isBlank()
                || usernameField.getText().isBlank() || passwordField.getPassword().length == 0) {
            Dialogs.warn(this, "Please fill in all required fields (*).", "Missing information");
            return;
        }

        SignUpRequest req = new SignUpRequest();
        req.setFullName(fullNameField.getText().trim());
        req.setEmail(emailField.getText().trim());
        req.setUsername(usernameField.getText().trim());
        req.setPassword(new String(passwordField.getPassword()));
        req.setCourse(courseField.getText().trim());
        req.setYear(yearField.getText().trim());
        req.setCampus(campusField.getText().trim());

        try {
            AuthResponse resp = ApiClient.post("/auth/signup", req, AuthResponse.class);
            ApiClient.setAuthToken(resp.getToken());
            Session.set(resp.getUserId(), resp.getFullName());

            Dialogs.info(this, "Account created! Welcome to SwapUni.", "Success");

            MainFrame main = new MainFrame();
            main.setExtendedState(getExtendedState());
            main.setVisible(true);
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Sign up failed");
        }
    }
}
