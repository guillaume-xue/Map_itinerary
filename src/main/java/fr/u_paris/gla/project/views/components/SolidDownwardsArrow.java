package fr.u_paris.gla.project.views.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Custom JPanel that represents a solid downward arrow
 */
public class SolidDownwardsArrow extends JPanel {
  private final Color color;

  public SolidDownwardsArrow(Color color) {
    super();
    this.color = color;
    this.setBackground(Color.WHITE);
    this.setOpaque(true);
  }

  @Override
  protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      Graphics2D g2d = (Graphics2D) g;

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int width = getWidth();
      int height = getHeight();

      int[] xPoints = { width / 2, width, 0 };
      int[] yPoints = { height, 0, 0 };

      g2d.setColor(color);
      g2d.fillPolygon(xPoints, yPoints, 3);
  }
}