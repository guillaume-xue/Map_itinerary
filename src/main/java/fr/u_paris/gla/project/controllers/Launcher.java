package fr.u_paris.gla.project.controllers;

import javax.swing.BorderFactory;
import javax.swing.JRootPane;
import javax.swing.JWindow;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public class Launcher {

  private static final URL SPLASH_SCREEN_IMAGE = Launcher.class
      .getResource("/fr/u_paris/gla/project/uparis_logo_rvb.png");

  /**
   * Constructor.
   */
  public Launcher(String[] args) {
    // Create and display the splash screen
    JWindow splashScreen = createSplashScreen();
    splashScreen.setVisible(true);
    // Set the system property for the user agent
    System.setProperty("http.agent", "MyCustomApp/1.0 (https://example.com)");
    // Set the system property for the menu bar
    // This is for MacOS to use the menu bar at the top of the screen
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    // Initialize the GUI
    GUIController guiController = new GUIController(args);
    splashScreen.dispose();
    guiController.launch();
  }

  /**
   * Creates a splash screen.
   *
   * @return the splash screen
   */
  private JWindow createSplashScreen() {
    JWindow window = new JWindow();
    JRootPane rootPane = window.getRootPane();

    // Create the content of the splash screen
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    contentPane.setBackground(new Color(240, 240, 240));
    // Resize the image
    ImageIcon icon = new ImageIcon(SPLASH_SCREEN_IMAGE);
    Image image = icon.getImage().getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
    JLabel imageLabel = new JLabel(new ImageIcon(image));
    contentPane.add(imageLabel, BorderLayout.CENTER);

    // Configure the window
    rootPane.setContentPane(contentPane);
    window.setSize(400, 300);
    window.setLocationRelativeTo(null);

    return window;
  }

}
