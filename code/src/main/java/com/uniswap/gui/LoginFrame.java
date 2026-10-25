package com.uniswap.gui;

import com.uniswap.dto.AuthResponse;
import com.uniswap.dto.LoginRequest;

import javax.swing.*;
import java.awt.*;

/** Screen 2: Login. */
public class LoginFrame extends JFrame {

    private final RoundedTextField emailField = new RoundedTextField();
    private final RoundedPasswordField passwordField = new RoundedPasswordField();

    public LoginFrame() {
        super("SwapUni - Log In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);      // resizable + maximizable (maximize button works)

        JPanel formSide = new JPanel(new GridBagLayout());
        formSide.setBackground(UiTheme.BG);

        // Eggshell card holding the form, centred however large the window gets.
        RoundedPanel card = new RoundedPanel(22, 6);
        card.pad(34, 38, 30, 38);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 500));

        JLabel eyebrow = UiTheme.label("WELCOME BACK", UiTheme.font(Font.BOLD, 11), UiTheme.TERRACOTTA);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = UiTheme.label("Log in to SwapUni", UiTheme.serif(Font.BOLD, 30), UiTheme.BROWN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UiTheme.label("Swap, sell and share with students on your campus.", UiTheme.BODY, UiTheme.BROWN_SOFT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(eyebrow);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        emailField.placeholder("you@college.edu");
        card.add(Forms.field("Email or Username", emailField));
        card.add(Forms.field("Password", passwordField));
        card.add(Box.createVerticalStrut(4));

        RoundedButton loginBtn = UiTheme.accentButton("Log In");
        loginBtn.addActionListener(e -> doLogin());
        card.add(fullWidth(loginBtn));
        card.add(Box.createVerticalStrut(10));

        RoundedButton signUpBtn = UiTheme.secondaryButton("Create an Account");
        signUpBtn.addActionListener(e -> {
            SignUpFrame next = new SignUpFrame();
            next.setBounds(getBounds());
            next.setExtendedState(getExtendedState());
            next.setVisible(true);
            dispose();
        });
        card.add(fullWidth(signUpBtn));
        card.add(Box.createVerticalStrut(20));

        RoundedPanel hint = new RoundedPanel(14, 0).fill(UiTheme.WARM_CREAM).borderColor(UiTheme.BORDER);
        hint.pad(10, 14, 10, 14);
        hint.setLayout(new BoxLayout(hint, BoxLayout.Y_AXIS));
        JLabel h1 = UiTheme.label("Sample login", UiTheme.LABEL, UiTheme.BROWN_SOFT);
        JLabel h2 = UiTheme.label("amit.sharma@cgc.edu.in / Password@123", UiTheme.SMALL, UiTheme.BROWN);
        hint.add(h1);
        hint.add(Box.createVerticalStrut(2));
        hint.add(h2);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hint);

        JLabel psst = UiTheme.label("Psst... try popping the items on the left.", UiTheme.SMALL, UiTheme.TEXT_PLACEHOLDER);
        psst.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(14));
        card.add(psst);

        formSide.add(card);
        setContentPane(BrandPanel.split(new BrandPanel(), formSide));

        // Pressing Enter in the password field logs in too.
        getRootPane().setDefaultButton(loginBtn);
    }

    private JComponent fullWidth(JComponent c) {
        JPanel p = new JPanel(new GridLayout(1, 1));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.add(c);
        return p;
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            Dialogs.warn(this, "Please enter both fields.", "Missing information");
            return;
        }

        LoginRequest req = new LoginRequest();
        req.setEmailOrUsername(email);
        req.setPassword(password);

        try {
            AuthResponse resp = ApiClient.post("/auth/login", req, AuthResponse.class);
            ApiClient.setAuthToken(resp.getToken());
            Session.set(resp.getUserId(), resp.getFullName());

            MainFrame main = new MainFrame();
            main.setExtendedState(getExtendedState());   // stay maximized if the login window was
            main.setVisible(true);
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage(), "Login failed");
        }
    }
}
