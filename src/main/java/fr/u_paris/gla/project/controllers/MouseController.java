package fr.u_paris.gla.project.controllers;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.views.Gui;

public class MouseController {

  private JMapViewer mapViewer;
  private Point lastDragPoint;
  private JTextArea startTextArea;
  private JTextArea endTextArea;
  private Graph graph;
  private Gui gui;

  /**
   * Constructor for MouseController.
   *
   * @param mapViewer the JMapViewer to control
   */
  MouseController(JMapViewer mapViewer, JTextArea startTextArea, JTextArea endTextArea, Graph graph, Gui gui) {
    this.gui = gui;
    this.graph = graph;
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

    private int leftClickCount = 0;
    private Timer singleClickTimer;

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
      if (SwingUtilities.isRightMouseButton(e)) {
        if (e.getClickCount() == 2) {
          mapViewer.zoomOut();
        }
      } else if (SwingUtilities.isLeftMouseButton(e)) {
        if (e.getClickCount() == 2) {
          if (singleClickTimer != null) {
            singleClickTimer.stop(); // Cancel the single-click action
          }
          mapViewer.zoomIn();
        } else if (e.getClickCount() == 1) {
          singleClickTimer = new Timer(200, actionEvent -> {
            // Handle single click only if no double click occurs
            if (!startTextArea.isFocusOwner() && !endTextArea.isFocusOwner()) {
              mapViewer.removeAllMapMarkers();
              mapViewer.removeAllMapPolygons();
              gui.getContentPanel().removeAll();
              gui.getContentPanel().revalidate();
              gui.getContentPanel().repaint();

              if (leftClickCount == 0) {
                Point clickPoint = e.getPoint();
                Coordinate coord = new Coordinate(mapViewer.getPosition(clickPoint).getLat(),
                    mapViewer.getPosition(clickPoint).getLon());
                mapViewer.addMapMarker(new MapMarkerDot(coord));
                try {
                  Stop stop = graph.getClosestStop(coord.getLat(), coord.getLon());
                  gui.getTextItineraryPanel().add(gui.displayListOfStopDeparture(stop));

                  

                  gui.getTextItineraryPanel().revalidate();
                  gui.getTextItineraryPanel().repaint();
                } catch (Exception e1) {
                  e1.printStackTrace();
                }
                leftClickCount++;
              } else {
                leftClickCount = 0;
              }
            } else if (startTextArea.isFocusOwner() || endTextArea.isFocusOwner()) {
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
              gui.requestFocusInWindow();
            }
          });
          singleClickTimer.setRepeats(false);
          singleClickTimer.start();
        }
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
