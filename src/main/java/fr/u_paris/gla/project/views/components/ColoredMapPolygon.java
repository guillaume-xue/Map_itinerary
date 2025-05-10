package fr.u_paris.gla.project.views.components;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

/**
 * Custom MapPolygon to make a line between two stops.
 */
public class ColoredMapPolygon extends MapPolygonImpl {
  private final Color color;

  public ColoredMapPolygon(Coordinate start, Coordinate end, Coordinate middle, Color color) {
    super(start, end, middle);
    this.color = color;
  }

  @Override
  public void paint(Graphics g, List<Point> points) {
    if (points == null || points.size() < 2) {
      return;
    }
    
    Graphics2D g2d = (Graphics2D) g;

    Stroke originalStroke = g2d.getStroke();
    Paint originalPaint = g2d.getPaint();
    Color originalColor = g2d.getColor();
    RenderingHints originalHints = g2d.getRenderingHints();

    g2d.setColor(Color.BLACK);
    g2d.setStroke( new BasicStroke(6.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
    drawLines(g2d, points);

    g2d.setColor(color);
    g2d.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
    drawLines(g2d, points);

    g2d.setStroke(originalStroke);
    g2d.setPaint(originalPaint);
    g2d.setColor(originalColor);
    g2d.setRenderingHints(originalHints);
  }

  /**
   * Helper function a draw a line between points.
   *
   * @param      g2d     The 2d graphics object
   * @param      points  The list of points
   */
  private void drawLines(Graphics2D g2d, List<Point> points) {
    for (int i = 1; i < points.size(); i++) {
      Point p1 = points.get(i - 1);
      Point p2 = points.get(i);
      g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
  }
  
}