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
 * Custom MapPolygon to make a "walking" line between two stops.
 */
public class GrayDashedMapPolygon extends MapPolygonImpl {
  private static final Color GRAY_COLOR = Color.GRAY;

  public GrayDashedMapPolygon(Coordinate start, Coordinate end, Coordinate middle) {
    super(start, end, middle);
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

    g2d.setColor(GRAY_COLOR); // Set the color to gray
    float[] dashPattern = { 10, 10 }; // Define dash pattern (10 pixels on, 10 pixels off)
    g2d.setStroke( new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
    for (int i = 1; i < points.size(); i++) {
      Point p1 = points.get(i - 1);
      Point p2 = points.get(i);
      g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    g2d.setStroke(originalStroke);
    g2d.setPaint(originalPaint);
    g2d.setColor(originalColor);
    g2d.setRenderingHints(originalHints);
  }
}
