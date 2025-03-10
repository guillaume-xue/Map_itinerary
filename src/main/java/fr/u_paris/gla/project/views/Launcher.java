package fr.u_paris.gla.project.views;

import javax.swing.BorderFactory;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Timer;
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
  public Launcher() {
    // Create and display the splash screen
    JWindow splashScreen = createSplashScreen();
    splashScreen.setVisible(true);

    // Schedule the launch of the main window after 3 seconds
    Timer timer = new Timer(1000, e -> {
      // Add fade-out effect before closing
      fadeOut(splashScreen);
      splashScreen.dispose();
      Gui gui = new Gui();
      gui.launch();
    });
    timer.setRepeats(false);
    timer.start();
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
    contentPane.setBackground(new Color(45, 52, 54));
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

  /**
   * Adds a fade-out effect when closing the window.
   *
   * @param window the window to close
   */
  private static void fadeOut(JWindow window) {
    float opacity = 1.0f;
    while (opacity > 0.0f) {
      window.setOpacity(opacity);
      opacity -= 0.1f;
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
