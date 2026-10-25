package com.uniswap;

import com.uniswap.gui.LoginFrame;
import com.uniswap.gui.SplashScreen;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

/**
 * Entry point for the whole desktop application.
 *
 * Two things happen here, in order:
 *   1. The Spring Boot backend starts (REST API + JPA + MySQL), exactly
 *      like a normal Spring Boot web app, just with no browser involved.
 *   2. Once the backend is ready, the Java Swing desktop window opens.
 *      From here on, the Swing GUI talks to the backend purely over
 *      HTTP calls to http://localhost:8080/api/... (see gui.ApiClient).
 */
@SpringBootApplication
public class UniSwapApplication {

    public static void main(String[] args) {
        // Keep Spring Boot's own console banner off - a student demoing a
        // *desktop app* shouldn't have to explain a Spring banner first.
        System.setProperty("spring.main.banner-mode", "off");

        // IMPORTANT: Spring Boot assumes every app is a headless server and
        // sets java.awt.headless=true automatically, very early in startup -
        // before application.properties is even read, so a property setting
        // won't undo it in time. Left alone, this makes AWT/Swing think
        // there is no display, even on a normal desktop with a screen, and
        // throws java.awt.HeadlessException the moment any Swing window
        // (JFrame/JWindow) is created. Explicitly telling the SpringApplication
        // instance not to do this - before calling run() - fixes it.
        SpringApplication app = new SpringApplication(UniSwapApplication.class);
        app.setHeadless(false);
        ConfigurableApplicationContext context = app.run(args);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default cross-platform look and feel.
            }

            SplashScreen splash = new SplashScreen();
            splash.showThenRun(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        });

        // Make sure closing the Swing window also shuts down the backend
        // cleanly instead of leaving a Spring Boot process running in the
        // background.
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }
}
