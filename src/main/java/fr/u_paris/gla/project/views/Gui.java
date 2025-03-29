package fr.u_paris.gla.project.views;

import org.json.JSONArray;
import org.json.JSONObject;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.*;
import javax.swing.border.Border;

import java.util.ArrayList;

import fr.u_paris.gla.project.graph.Stop;

public class Gui extends JFrame {

  private static final int SCREEN_WIDTH = 900;
  private static final int SCREEN_HEIGHT = 600;
  private static final int RESEARCH_PANEL_WIDTH = 350;
  private static final int MIN_SCREEN_WIDTH = 600;
  private static final int MIN_SCREEN_HEIGHT = 300;
  private JTextArea textStart;
  private JTextArea textEnd;
  private JPanel contentPanel;
  private JMapViewer mapViewer;
  private static final Color textColor = new Color(11, 22, 44);
  private static final Color bordeColor = new Color(88, 88, 88);
  private static final Color primaryBackgroundColor = new Color(240, 240, 240);
  private static final Color accentColor = new Color(76, 175, 80);
  private Point lastDragPoint; // Add this variable to track the last drag position

  /**
   * Constructor.
   */
  public Gui() {
    // Initialize the frame
    super("Map");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
    setMinimumSize(new Dimension(MIN_SCREEN_WIDTH, MIN_SCREEN_HEIGHT)); // Set minimum size
    setLocationRelativeTo(null);

    this.lastDragPoint = new Point();

    // Create text areas for the start and end
    this.textStart = createTextArea("From");
    JPanel startPanel = new JPanel();
    startPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    startPanel.setBackground(primaryBackgroundColor);
    startPanel.add(textStart);

    this.textEnd = createTextArea("To");
    JPanel endPanel = new JPanel();
    endPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    endPanel.setBackground(primaryBackgroundColor);
    endPanel.add(textEnd);

    addFocusListenerToTextArea();

    // Create a search button with hover effects
    JButton buttonSearch = createSearchButton();

    // Create a panel for the research area
    JPanel researchPanel = new JPanel();
    researchPanel.setBackground(primaryBackgroundColor);
    researchPanel.setLayout(new BoxLayout(researchPanel, BoxLayout.Y_AXIS));
    researchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel
    researchPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    researchPanel.add(startPanel);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    researchPanel.add(endPanel);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    // Create a container for the button to center it
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the button within the panel
    buttonPanel.setBackground(primaryBackgroundColor); // Ensure the background matches
    buttonPanel.add(buttonSearch); // Add the search button to this centered panel

    // Add the button panel to the research panel
    researchPanel.add(buttonPanel);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Create a content panel for displaying JSON content
    contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(primaryBackgroundColor);

    // Add the research panel to a scroll pane
    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling

    // Create a split pane to separate the research panel and the content panel
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setDividerLocation(250);
    splitPane.setEnabled(false);
    splitPane.add(researchPanel, JSplitPane.TOP);
    splitPane.add(scrollPane, JSplitPane.BOTTOM);

    // Create a map panel
    this.mapViewer = createMapViewer();
    addMouseController();
    JPanel mapPanel = new JPanel();
    mapPanel.setLayout(new java.awt.BorderLayout());
    mapPanel.add(this.mapViewer, java.awt.BorderLayout.CENTER);

    // Add the split pane and the map panel to the main content panel
    JPanel mainContentPanel = new JPanel();
    mainContentPanel.setLayout(new BorderLayout());
    mainContentPanel.add(splitPane, BorderLayout.WEST);
    mainContentPanel.add(mapPanel, BorderLayout.CENTER);

    add(mainContentPanel);
  }

  /**
   * Creates a map viewer panel with a tile source and a default location.
   * 
   * @return the map viewer panel
   */
  private JMapViewer createMapViewer() {
    JMapViewer mapViewer = new JMapViewer();
    mapViewer.setTileSource(new OsmTileSource.Mapnik());

    // Center the map on Paris (latitude: 48.8566, longitude: 2.3522)
    mapViewer.setDisplayPosition(
        new org.openstreetmap.gui.jmapviewer.Coordinate(48.8566, 2.3522), 10);

    return mapViewer;
  }

  /**
   * Retrieves the coordinates of an address using the Nominatim API.
   * 
   * @param address the address to search for
   * @return the coordinates as an array [latitude, longitude]
   */
  private double[] getCoordinatesFromAddress(String address) {
    String url = "https://nominatim.openstreetmap.org/search?q=" + address.replace(" ", "+")
        + "&format=json&addressdetails=1&limit=1";
    OkHttpClient client = new OkHttpClient();

    Request request = new Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 (compatible; MyApp/1.0)")
        .build();

    // Execute the request and parse the response
    try (Response response = client.newCall(request).execute()) {
      if (response.isSuccessful()) {
        String responseBody = response.body().string();
        JSONArray jsonArray = new JSONArray(responseBody);

        if (jsonArray.length() > 0) {
          // Get the first result
          JSONObject location = jsonArray.getJSONObject(0);
          JSONObject addressDetails = location.optJSONObject("address");

          // Verify if the address is in France
          if (addressDetails != null && "France".equalsIgnoreCase(addressDetails.optString("country"))) {
            double latitude = location.getDouble("lat");
            double longitude = location.getDouble("lon");
            return new double[] { latitude, longitude };
          } else {
            System.out.println("Adresse hors de France : " + address);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null; // Return null if the address is not found
  }

  private void addMouseController() {
    // Add mouse listener for panning with left click
    mapViewer.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          Gui.this.lastDragPoint = e.getPoint(); // Use Gui.this to access the enclosing class field
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        Gui.this.lastDragPoint = null; // Use Gui.this to access the enclosing class field
      }
    });

    mapViewer.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && Gui.this.lastDragPoint != null) {
          Point currentPoint = e.getPoint();
          int dx = Gui.this.lastDragPoint.x - currentPoint.x; // Reverse direction
          int dy = Gui.this.lastDragPoint.y - currentPoint.y; // Reverse direction

          // Move the map
          mapViewer.moveMap(dx, dy);

          // Update the last drag position
          Gui.this.lastDragPoint = currentPoint; // Use Gui.this to access the enclosing class field
        }
      }
    });
  }

  /**
   * Adds focus listeners to text areas to clear default text on focus.
   */
  private void addFocusListenerToTextArea() {
    textStart.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        if (textStart.getText().equals("From")) {
          textStart.setText("");
        }
        if (textEnd.getText().equals("")) {
          textEnd.setText("To");
        }
      }
    });

    textEnd.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        if (textEnd.getText().equals("To")) {
          textEnd.setText("");
        }
        if (textStart.getText().equals("")) {
          textStart.setText("From");
        }
      }
    });
  }

  /**
   * Creates a styled JTextArea with rounded edges.
   * 
   * @return the text area
   */
  private JTextArea createTextArea(String text) {
    JTextArea textArea = new JTextArea(text);
    textArea.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Set text to bold
    textArea.setForeground(textColor);
    textArea.setBackground(primaryBackgroundColor);
    textArea.setPreferredSize(new Dimension(RESEARCH_PANEL_WIDTH, 52));
    textArea.setCaretColor(Color.BLACK);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(true);
    textArea.setBorder(createBorder(15)); // Rounded border
    textArea.setAlignmentX(Component.CENTER_ALIGNMENT); // Align text to center
    return textArea;
  }

  /**
   * Creates a styled JButton with rounded edges and hover effects.
   * 
   * @return the search button
   */
  private JButton createSearchButton() {
    JButton buttonSearch = new JButton("Search");
    buttonSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
    buttonSearch.setBackground(accentColor);
    buttonSearch.setForeground(textColor);
    buttonSearch.setFocusPainted(false);
    buttonSearch.setBorder(createBorder(20)); // Rounded border
    buttonSearch.setPreferredSize(new Dimension(200, 50));
    buttonSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Add action listener to add displayJsonContent to contentPanel
    buttonSearch.addActionListener(e -> {

      contentPanel.removeAll();

      // Remove all markers and polygons from the map
      mapViewer.removeAllMapMarkers();
      mapViewer.removeAllMapPolygons();

      // Get the addresses from the text areas
      String startAddress = textStart.getText();
      String endAddress = textEnd.getText();

      // Get the coordinates from the addresses
      // Use the Nominatim API to get the coordinates
      double[] startCoordinates = getCoordinatesFromAddress(startAddress);
      double[] endCoordinates = getCoordinatesFromAddress(endAddress);

      if (startCoordinates != null && endCoordinates != null) {
        // Create stops with coordinates and addresses
        ArrayList<Stop> stops = new ArrayList<>();
        stops.add(new Stop(startCoordinates[0], startCoordinates[1], startAddress));
        stops.add(new Stop(endCoordinates[0], endCoordinates[1], endAddress));

        // Display the path on the map
        contentPanel.add(displayPath(stops));
        contentPanel.revalidate();
        contentPanel.repaint();
      } else {
        // Show an error message if the coordinates are not found
        JOptionPane.showMessageDialog(this, "Impossible de trouver les coordonnées pour l'une des adresses.", "Erreur",
            JOptionPane.ERROR_MESSAGE);
      }
    });

    return buttonSearch;
  }

  /**
   * Creates a rounded border with the given radius.
   * 
   * @return the border
   */
  private Border createBorder(int radius) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(bordeColor, 2),
        BorderFactory.createEmptyBorder(radius, radius, radius, radius));
  }

  /**
   * Reads and displays the contents of a .txt file in a formatted panel.
   * 
   * @return the panel containing the text content
   */
  private JPanel displayPath(ArrayList<Stop> stops) {
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
    pathPanel.setBackground(primaryBackgroundColor); // Background color of paths panel
    pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel

    // initialize the first stop
    Stop stop = stops.get(0);
    JTextArea textArea = createTextArea(stop.getNameOfAssociatedStation());
    pathPanel.add(textArea);
    Coordinate mStart = new Coordinate(stop.getLongitude(), stop.getLatitude());
    mapViewer.setDisplayPosition(mStart, 12);
    MapMarkerDot parisMarker = new MapMarkerDot(mStart);
    mapViewer.addMapMarker(parisMarker);

    // draw the path and add TextAreas for each stop
    for (int i = 1; i < stops.size(); i++) {
      stop = stops.get(i);
      // add TextArea for the stop
      textArea = createTextArea(stop.getNameOfAssociatedStation());
      pathPanel.add(textArea);
      // draw the path
      Coordinate mEnd = new Coordinate(stop.getLongitude(), stop.getLatitude()); // Paris center
      MapPolygon mLine = new MapPolygonImpl(mStart, mEnd, mStart);
      MapMarkerDot markerDot = new MapMarkerDot(mEnd);
      mapViewer.addMapPolygon(mLine);
      mapViewer.addMapMarker(markerDot);
      // update the start
      mStart = mEnd;
    }
    return pathPanel;
  }

  /**
   * Launches the application.
   */
  public void launch() {
    setVisible(true);
  }
}