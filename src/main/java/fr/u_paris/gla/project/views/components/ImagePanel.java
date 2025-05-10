package fr.u_paris.gla.project.views.components;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Custom JPanel that holds an image.
 */
public class ImagePanel extends JPanel {
  private BufferedImage image;

  public ImagePanel(String imagePath) {
      try {
          image = ImageIO.read(new File(imagePath));
      } catch (IOException e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(this, "Image could not be loaded: " + imagePath);
      }
      this.setPreferredSize(new Dimension(15, 15));

  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (image != null) {
        // Scale image to fit the panel
        this.setBackground(Color.WHITE);
        this.setOpaque(true);

        int width = getWidth();
        int height = getHeight();
        int lessOf = height < width ? height : width;

        g.drawImage(image, 0, 0, lessOf, lessOf, this);
    }
  }
}