package fr.u_paris.gla.project.views.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

/**
 * A custom JPanel that represents a circle.
 */
public class SolidCircle extends JPanel {
  Color color;

  public SolidCircle(Color color){
    this.color = color;
    this.setBackground(Color.WHITE);
    this.setOpaque(true);
  }
   @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

      // Cast to Graphics2D for better control
    Graphics2D g2d = (Graphics2D) g;

      // Enable anti-aliasing for smooth edges
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Define the diameter of the circle
    int diameter = Math.min(getWidth(), getHeight()) / 2;

      // Calculate top-left corner to center the circle
    int x = (getWidth() - diameter) / 2;
    int y = (getHeight() - diameter) / 2;

    // Draw filled circle
    g2d.setColor(color);
    g2d.fillOval(x, y, diameter, diameter);

    g2d.setColor(Color.BLACK);
    g2d.setStroke(new BasicStroke(1)); 
    g2d.drawOval(x, y, diameter, diameter);
  }
}