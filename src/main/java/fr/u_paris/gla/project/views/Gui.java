package fr.u_paris.gla.project.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.AbstractBorder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import fr.u_paris.gla.project.astar.AStar;
import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.utils.CSVExtractor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Gui extends JFrame {

  private static final int SCREEN_WIDTH = 900;
  private static final int SCREEN_HEIGHT = 600;
  private static final int RESEARCH_PANEL_WIDTH = 350;
  private static final int MIN_SCREEN_WIDTH = 600;
  private static final int MIN_SCREEN_HEIGHT = 300;
  private JScrollPane textStart;
  private JScrollPane textEnd;
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
    this.textStart = createTextAreaInput("From");
    JPanel startPanel = new JPanel();
    startPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    startPanel.setBackground(primaryBackgroundColor);
    startPanel.add(textStart);

    this.textEnd = createTextAreaInput("To");
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
    researchPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    researchPanel.add(startPanel);
    researchPanel.add(endPanel);

    // Create a container for the button to center it
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the button within the panel
    buttonPanel.setBackground(primaryBackgroundColor); // Ensure the background matches
    buttonPanel.add(buttonSearch); // Add the search button to this centered panel

    // Add the button panel to the research panel
    researchPanel.add(buttonPanel);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 5)));

    // Create a content panel for displaying content
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
    ((JTextArea) textStart.getViewport().getView()).addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        JTextArea textStartArea = (JTextArea) textStart.getViewport().getView();
        JTextArea textEndArea = (JTextArea) textEnd.getViewport().getView();
        if (textStartArea.getText().equals("From")) {
          textStartArea.setText("");
        }
        if (textEndArea.getText().equals("")) {
          textEndArea.setText("To");
        }
      }
    });

    ((JTextArea) textEnd.getViewport().getView()).addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        JTextArea textStartArea = (JTextArea) textStart.getViewport().getView();
        JTextArea textEndArea = (JTextArea) textEnd.getViewport().getView();
        if (textEndArea.getText().equals("To")) {
          textEndArea.setText("");
        }
        if (textStartArea.getText().equals("")) {
          textStartArea.setText("From");
        }
      }
    });
  }

  /**
   * Creates a styled JTextAreaInput with rounded edges.
   *
   * @return the scroll pane containing the text area
   */
  private JScrollPane createTextAreaInput(String text) {
    JTextArea textArea = new JTextArea(text);
    textArea.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Set text to bold
    textArea.setForeground(textColor);
    textArea.setBackground(primaryBackgroundColor);
    textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
    textArea.setAlignmentY(Component.CENTER_ALIGNMENT);
    textArea.setCaretColor(Color.BLACK);
    textArea.setLineWrap(false); // Disable line wrapping
    textArea.setWrapStyleWord(false); // Disable word wrapping
    textArea.setEditable(true);

    // Prevent newlines by intercepting key events
    // Add a key listener to prevent newlines
    textArea.addKeyListener(new java.awt.event.KeyAdapter() {
      @Override
      public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
          e.consume(); // Prevent the Enter key from inserting a newline
        }
      }
    });

    // Wrap the JTextArea in a JScrollPane for horizontal scrolling
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(RESEARCH_PANEL_WIDTH, 60)); // Fixed preferred size
    scrollPane.setMaximumSize(new Dimension(RESEARCH_PANEL_WIDTH, 60)); // Fixed maximum size
    scrollPane.setBackground(primaryBackgroundColor);
    scrollPane.setBorder(new RoundedBorder(20)); // Use custom rounded border

    return scrollPane;
  }

  /**
   * Creates a styled JTextAreaOutput with rounded edges.
   * 
   * @return the text area
   */
  private JTextArea createTextAreaOutput(String text) {
    JTextArea textArea = new JTextArea(text);
    textArea.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Set text to bold
    textArea.setForeground(textColor);
    textArea.setBackground(primaryBackgroundColor);
    textArea.setCaretColor(Color.BLACK);
    textArea.setLineWrap(false); // Disable line wrapping
    textArea.setWrapStyleWord(false); // Disable word wrapping
    textArea.setEditable(true);
    textArea.setPreferredSize(new Dimension(RESEARCH_PANEL_WIDTH, 50)); // Fixed preferred size
    textArea.setMaximumSize(new Dimension(RESEARCH_PANEL_WIDTH, 50)); // Fixed maximum size
    textArea.setBorder(new RoundedBorder(20)); // Use custom rounded border
    textArea.setFocusable(false); // Make the text area non-focusable
    return textArea;
  }

  // Custom rounded border class
  private static class RoundedBorder extends AbstractBorder {
    private final int radius;

    public RoundedBorder(int radius) {
      this.radius = radius; // Increase this value to make the corners more rounded
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setColor(bordeColor); // Border color
      g2d.drawRoundRect(x + 4, y + 4, width - 8, height - 8, radius * 2, radius * 2); // Larger radius for more
      // rounded corners
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(radius, radius, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
      insets.left = radius;
      insets.right = radius;
      insets.top = radius;
      insets.bottom = radius;
      return insets;
    }
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
    buttonSearch.setBorder(new RoundedBorder(10)); // Rounded border
    buttonSearch.setPreferredSize(new Dimension(200, 50));
    buttonSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Add action listener to add displayJsonContent to contentPanel
    // FIXME ASAP
    String[] args = {"--parse","mapData.csv","junctionsData.csv"};
    Graph graph = CSVExtractor.makeOjectsFromCSV(args);
    AStar astar = new AStar(graph);

    buttonSearch.addActionListener(e -> {

      contentPanel.removeAll();

      // Remove all markers and polygons from the map
      mapViewer.removeAllMapMarkers();
      mapViewer.removeAllMapPolygons();

      // Get the addresses from the text areas
      String startAddress = ((JTextArea) textStart.getViewport().getView()).getText();
      String endAddress = ((JTextArea) textEnd.getViewport().getView()).getText();

      // Get the coordinates from the addresses
      // Use the Nominatim API to get the coordinates

      double[] startCoordinates = getCoordinatesFromAddress(startAddress);
      double[] endCoordinates = getCoordinatesFromAddress(endAddress);

      String[] p1 = startAddress.split(",\\s*"); 
      /*
      double[] startCoordinates = new double[] {
        Double.parseDouble(p1[0]),
        Double.parseDouble(p1[1])
      };
      */
      
      String[] p2 = endAddress.split(",\\s*"); 
      /*
      double[] endCoordinates = new double[] {
        Double.parseDouble(p2[0]),
        Double.parseDouble(p2[1])
      };
      */

      if (startCoordinates != null && endCoordinates != null) {
        // Create stops with coordinates and addresses
        //ArrayList<Stop> stops = new ArrayList<>();
        
        try{
          Stop stopA = graph.getClosestStop(startCoordinates[0], startCoordinates[1]);
          Stop stopB = graph.getClosestStop(endCoordinates[0], endCoordinates[1]);

          astar.setDepartStop(stopA);
          astar.setFinishStop(stopB);

          ArrayList<Stop> stops = astar.findPath();

          

          // Display the path on the map
          contentPanel.add(displayPath(stops));
          contentPanel.add(new JPanel());
          contentPanel.revalidate();
          contentPanel.repaint();
        } catch ( Exception except ){
          System.out.println("No path was found between these two points");
        }
      } else {
        // Show an error message if the coordinates are not found
        JOptionPane.showMessageDialog(this, "Impossible de trouver les coordonnées pour l'une des adresses.", "Erreur",
            JOptionPane.ERROR_MESSAGE);
      }
    });

    return buttonSearch;
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
    JTextArea stopTextArea = createTextAreaOutput(stop.getNameOfAssociatedStation());
    pathPanel.add(stopTextArea);
    Coordinate mStart = new Coordinate(stop.getLongitude(), stop.getLatitude());
    mapViewer.setDisplayPosition(mStart, 12);
    MapMarkerDot parisMarker = new MapMarkerDot(mStart);
    mapViewer.addMapMarker(parisMarker);

    // draw the path and add TextAreas for each stop
    for (int i = 1; i < stops.size(); i++) {
      stop = stops.get(i);
      // add TextArea for the stop
      stopTextArea = createTextAreaOutput(stop.getNameOfAssociatedStation());
      pathPanel.add(stopTextArea);
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