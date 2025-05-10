package fr.u_paris.gla.project.views.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

/**
 * Custom JPanel that represents a solid colored line.
 */
public class SolidColoredLine extends JPanel {
  private final Color color;

  public SolidColoredLine(Color color) {
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


    int middleX = width / 2;


    g2d.setColor(color);
    g2d.setStroke(new BasicStroke(4)); 
    g2d.drawLine(middleX, 0, middleX, height);
  }
}