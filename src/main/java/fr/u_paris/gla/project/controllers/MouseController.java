package fr.u_paris.gla.project.controllers;

import java.awt.Point;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class MouseController {

  private JMapViewer mapViewer;
  private Point lastDragPoint;
  private JTextArea startTextArea;
  private JTextArea endTextArea;

  /**
   * Constructor for MouseController.
   *
   * @param mapViewer the JMapViewer to control
   */
  MouseController(JMapViewer mapViewer, JTextArea startTextArea, JTextArea endTextArea) {
    this.mapViewer = mapViewer;
    this.startTextArea = startTextArea;
    this.endTextArea = endTextArea;
    this.lastDragPoint = new Point();
    addMouseController();
  }

  /**
   * Adds markers to the map if startTextArea and endTextArea contain valid
   * coordinates.
   */
  private void addInitialMarkers() {
    try {
      if (isValidCoordinate(startTextArea.getText())) {
        String[] startCoords = startTextArea.getText().split(",\\s*");
        Coordinate startCoord = new Coordinate(Double.parseDouble(startCoords[0]), Double.parseDouble(startCoords[1]));
        mapViewer.addMapMarker(new MapMarkerDot(startCoord));
      }
      if (isValidCoordinate(endTextArea.getText())) {
        String[] endCoords = endTextArea.getText().split(",\\s*");
        Coordinate endCoord = new Coordinate(Double.parseDouble(endCoords[0]), Double.parseDouble(endCoords[1]));
        mapViewer.addMapMarker(new MapMarkerDot(endCoord));
      }
    } catch (Exception e) {
      System.err.println("Error parsing coordinates: " + e.getMessage());
    }
  }

  /**
   * Validates if a string represents valid coordinates in the format "latitude,
   * longitude".
   *
   * @param text the string to validate
   * @return true if valid, false otherwise
   */
  private boolean isValidCoordinate(String text) {
    if (text == null || !text.matches("-?\\d+(\\.\\d+)?\\s*,\\s*-?\\d+(\\.\\d+)?")) {
      return false;
    }
    return true;
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

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && (startTextArea.isFocusOwner() || endTextArea.isFocusOwner())) {
        mapViewer.removeAllMapMarkers();
        mapViewer.removeAllMapPolygons();
        Point clickPoint = e.getPoint();
        Coordinate coord = new Coordinate(mapViewer.getPosition(clickPoint).getLat(),
            mapViewer.getPosition(clickPoint).getLon());
        mapViewer.addMapMarker(new MapMarkerDot(coord));
        if (startTextArea.isFocusOwner()) {
          startTextArea.setText(coord.getLat() + ", " + coord.getLon());
        } else if (endTextArea.isFocusOwner()) {
          endTextArea.setText(coord.getLat() + ", " + coord.getLon());
        }
        addInitialMarkers();
      }
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
