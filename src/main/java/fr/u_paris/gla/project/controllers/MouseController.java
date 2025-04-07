package fr.u_paris.gla.project.controllers;

import java.awt.Point;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class MouseController {

  private JMapViewer mapViewer;
  private Point lastDragPoint;

  /**
   * Constructor for MouseController.
   *
   * @param mapViewer the JMapViewer to control
   */
  MouseController(JMapViewer mapViewer) {
    this.mapViewer = mapViewer;
    this.lastDragPoint = new Point();
    addMouseController();
  }

  /**
   * Adds mouse listeners to the map viewer for drag and drop functionality.
   */
  private void addMouseController() {
    mapViewer.addMouseListener(new CustomMouseAdapter());
    mapViewer.addMouseMotionListener(new CustomMouseMotionAdapter());
  }

  /// Custom mouse adapter to handle mouse events
  private class CustomMouseAdapter extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        lastDragPoint = e.getPoint();
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      lastDragPoint = null;
    }
  }

  /// Custom mouse motion adapter to handle mouse drag events
  private class CustomMouseMotionAdapter extends MouseMotionAdapter {
    @Override
    public void mouseDragged(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e) && lastDragPoint != null) {
        Point currentPoint = e.getPoint();
        int dx = lastDragPoint.x - currentPoint.x;
        int dy = lastDragPoint.y - currentPoint.y;

        mapViewer.moveMap(dx, dy);
        lastDragPoint = currentPoint;
      }
    }
  }
}
