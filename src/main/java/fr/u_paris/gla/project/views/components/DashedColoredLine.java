package fr.u_paris.gla.project.views.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

/**
 * Custom JPanel that represents a dashed colored line.
 */
public class DashedColoredLine extends JPanel {
  private final Color color;

  public DashedColoredLine(Color color) {
    super();
    this.color = color;
    this.setBackground(Color.WHITE);
    this.setOpaque(true);
  }
  
  @Override
  protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Graphics2D g2d = (Graphics2D) g.create();

      // Calculate dynamic dash size based on height
      int panelHeight = getHeight();
      float dashLength = Math.max(2f, panelHeight / 50f); // 2% of height or minimum 2px
      float gapLength = dashLength; // Make dashes and gaps equal

      // Create dynamic dash pattern
      float[] dashPattern = {dashLength, gapLength};

      // Set stroke with dynamic dash
      g2d.setStroke(new BasicStroke(
          2f,                      // Line thickness
          BasicStroke.CAP_BUTT, 
          BasicStroke.JOIN_MITER, 
          10f,                     // Miter limit
          dashPattern,            // Dash pattern
          0f                      // Dash phase
      ));

      g2d.setColor(color);

      // Draw vertical dashed line down the center
      int x = getWidth() / 2;
      g2d.drawLine(x, 0, x, getHeight());

      g2d.dispose();
  }
}