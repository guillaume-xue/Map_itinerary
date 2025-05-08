package fr.u_paris.gla.project.views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.AbstractBorder;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import fr.u_paris.gla.project.astar.SegmentItineraire;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import fr.u_paris.gla.project.utils.TransportTypes;
import fr.u_paris.gla.project.views.Gui.ColoredMapPolygon;
import fr.u_paris.gla.project.views.Gui.GrayDashedMapPolygon;


public class Gui extends JFrame {

  private static final int SCREEN_WIDTH = 900;
  private static final int SCREEN_HEIGHT = 600;
  private static final int RESEARCH_PANEL_WIDTH = 350;
  private static final int MIN_SCREEN_WIDTH = 600;
  private static final int MIN_SCREEN_HEIGHT = 300;
  private static final Coordinate PARIS_CENTER = new Coordinate(48.8566, 2.3522);
  private static final int DEFAULT_ZOOM = 10;

  private JScrollPane textStart;
  private JScrollPane textEnd;
  private JPanel contentPanel;
  private JPanel textItineraryPanel;
  private JScrollPane textItineraryScrollPane;
  private JScrollPane contentScrollPane;
  private JMapViewer mapViewer;
  private JButton researchButton;
  private JCheckBox distCheckBox;
  private JCheckBox timeCheckBox;
  private JCheckBox showAllLinesCheckBox;
  private JScrollPane numLine;
  private JComboBox<String> comboBoxHours;
  private JComboBox<String> comboBoxMinutes;
  private JComboBox<String> lineTypeDropdown;
  private JButton viewLineButton;
  private static final Color textColor = new Color(11, 22, 44);
  private static final Color bordeColor = new Color(88, 88, 88);
  private static final Color primaryBackgroundColor = new Color(240, 240, 240);
  private static final Color accentColor = new Color(76, 175, 80);
  private JDialog floatingWindow; // Fenêtre flottante

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
    init();
  }

  /**
   * Initializes the GUI components.
   */
  private void init() {
    // Create a menu bar
    JMenuBar menuBar = new JMenuBar();
    JMenu viewMenu = new JMenu("Vue");
    JMenuItem floatingWindowMenu = new JMenuItem("Réseau");
    viewMenu.add(floatingWindowMenu);
    menuBar.add(viewMenu);

    JMenu devMenu = new JMenu("Dev");
    JMenuItem connectStopsItem = new JMenuItem("Connecter les arrêts");
    devMenu.add(connectStopsItem);
    devMenu.addSeparator();

    menuBar.add(devMenu);
    this.setJMenuBar(menuBar);

    // Create text areas for the start and end
    this.textStart = createTextAreaInput("De");
    JPanel startPanel = new JPanel();
    startPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    startPanel.setBackground(primaryBackgroundColor);
    startPanel.add(textStart);

    this.textEnd = createTextAreaInput("À");
    JPanel endPanel = new JPanel();
    endPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    endPanel.setBackground(primaryBackgroundColor);
    endPanel.add(textEnd);

    // Create a search button with hover effects
    this.researchButton = createSearchButton();

    // Create a map panel
    this.mapViewer = createMapViewer();
    JPanel mapPanel = new JPanel();
    mapPanel.setLayout(new java.awt.BorderLayout());
    mapPanel.add(this.mapViewer, java.awt.BorderLayout.CENTER);

    // Create a content panel for displaying content
    contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(primaryBackgroundColor);

    //Create zone to show textual itinerary
    textItineraryPanel = new JPanel();
    textItineraryPanel.setLayout(new BoxLayout(textItineraryPanel, BoxLayout.Y_AXIS));
    textItineraryPanel.setBackground(primaryBackgroundColor);



    // Create checkboxes for choosing the best route by distance or time
    JLabel label = new JLabel("Meilleur itinéraire en: ");
    this.distCheckBox = new JCheckBox("Distance");
    this.distCheckBox.setSelected(true); // Default to distance
    this.timeCheckBox = new JCheckBox("Temps");
    JPanel checkBoxPanel = new JPanel();
    checkBoxPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

    String[] hours = new String[] { "Now", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
        "23" };

    String[] minutes = new String[] { "Now", "00", "01", "02", "03", "04", "05", "06", "07", "08",
        "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32",
        "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44",
        "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56",
        "57", "58", "59" };

    comboBoxHours = new JComboBox<>(hours);
    comboBoxMinutes = new JComboBox<>(minutes);

    checkBoxPanel.add(label);
    checkBoxPanel.add(distCheckBox);
    checkBoxPanel.add(timeCheckBox);

    JPanel comboBoxPanel = new JPanel();

    comboBoxPanel.add(comboBoxHours);
    comboBoxPanel.add(new JLabel(":")); // Add spacing
    comboBoxPanel.add(comboBoxMinutes);

    // Create a panel for the research area
    JPanel researchPanel = new JPanel();
    researchPanel.setBackground(primaryBackgroundColor);
    researchPanel.setLayout(new BoxLayout(researchPanel, BoxLayout.Y_AXIS));
    researchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel
    researchPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    researchPanel.add(startPanel);
    researchPanel.add(endPanel);
    researchPanel.add(checkBoxPanel);
    researchPanel.add(comboBoxPanel);

    // Create a container for the button to center it
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the button
                                                              // within the panel
    buttonPanel.setBackground(primaryBackgroundColor); // Ensure the background matches
    buttonPanel.add(this.researchButton); // Add the search button to this centered panel

    // Add the button panel to the research panel
    researchPanel.add(buttonPanel);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 5)));

    // Create a floating window
    createFloatingWindow();

    // Add the research panel to a scroll pane
    contentScrollPane = new JScrollPane(contentPanel);
    contentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    contentScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling


    textItineraryScrollPane = new JScrollPane(textItineraryPanel);
    textItineraryScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    textItineraryScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling




    // Create a split pane to separate the research panel and the content panel
    JSplitPane splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT);
    splitPane.setDividerLocation(310);
    splitPane.setEnabled(false);

    splitPane.add(researchPanel, JSplitPane.TOP);
    splitPane.add(textItineraryScrollPane, JSplitPane.BOTTOM);

    // Add the split pane and the map panel to the main content panel
    JPanel mainContentPanel = new JPanel();
    mainContentPanel.setLayout(new BorderLayout());
    mainContentPanel.add(splitPane, BorderLayout.WEST);
    mainContentPanel.add(mapPanel, BorderLayout.CENTER);

    add(mainContentPanel);
  }

  public boolean isShowAllLinesSelected() {
      return showAllLinesCheckBox != null && showAllLinesCheckBox.isSelected();
  }

  private void createFloatingWindow() {
    floatingWindow = new JDialog(this, "Floating Window", false);
    floatingWindow.setSize(200, 200);
    floatingWindow.setLocationRelativeTo(this);
    floatingWindow.setUndecorated(true); // Enlever la barre de titre et de fermeture
    floatingWindow.setAlwaysOnTop(false); // Toujours au-dessus de la fenêtre principale
    floatingWindow.setLayout(new BorderLayout());
    floatingWindow.setBackground(primaryBackgroundColor);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(primaryBackgroundColor);

    JPanel titlePanel = new JPanel();
    JLabel label = new JLabel("Réseau");
    titlePanel.add(label);

    // Set the textArea for the line number

    JPanel textPanel = new JPanel();
    this.numLine = new JScrollPane();
    JTextArea textArea = new JTextArea("N° ligne");
    textArea.setForeground(textColor);
    textArea.setBackground(primaryBackgroundColor);
    textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
    textArea.setAlignmentY(Component.CENTER_ALIGNMENT);
    textArea.setCaretColor(Color.BLACK);
    textArea.setLineWrap(false); // Disable line wrapping
    textArea.setWrapStyleWord(false); // Disable word wrapping
    textArea.setEditable(true);
    textArea.setPreferredSize(new Dimension(100, 18)); // Fixed preferred size
    textArea.setMaximumSize(new Dimension(100, 18)); // Fixed maximum size
    this.numLine.setViewportView(textArea);
    this.numLine.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    this.numLine.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    textPanel.add(numLine);

    JPanel comboBox = new JPanel();
    // Create a dropdown menu (JComboBox) for selecting line types
    lineTypeDropdown = new JComboBox<>(new String[] { "Bus", "Subway", "Tram", "Rail", "Funicular" });
    lineTypeDropdown.setPreferredSize(new Dimension(100, 25));
    lineTypeDropdown.setBackground(primaryBackgroundColor);
    lineTypeDropdown.setForeground(textColor);
    lineTypeDropdown.setFocusable(false);
    comboBox.add(lineTypeDropdown);

    JPanel checkBoxPanel = new JPanel();
    showAllLinesCheckBox = new JCheckBox("Toutes les lignes");
    showAllLinesCheckBox.setBackground(primaryBackgroundColor);
    showAllLinesCheckBox.setForeground(textColor);
    checkBoxPanel.add(showAllLinesCheckBox);

    // Create a button to view the line
    this.viewLineButton = new JButton("Afficher");
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(viewLineButton);

    panel.add(titlePanel);
    panel.add(textPanel);
    panel.add(comboBox);
    panel.add(checkBoxPanel);
    panel.add(buttonPanel);
    this.floatingWindow.add(panel, BorderLayout.CENTER);

    // Ajouter un écouteur pour suivre le déplacement de la fenêtre principale
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
      @Override
      public void componentMoved(java.awt.event.ComponentEvent e) {
        Point mainWindowLocation = Gui.this.getLocation();
        floatingWindow.setLocation(mainWindowLocation.x + (Gui.this.getWidth() - floatingWindow.getWidth() - 50),
            mainWindowLocation.y + (Gui.this.getHeight() - floatingWindow.getHeight() - 50));
      }
    });

    showAllLinesCheckBox.addActionListener(e -> {
        boolean showAll = showAllLinesCheckBox.isSelected();
        textArea.setEnabled(!showAll);
        numLine.setEnabled(!showAll);
        if (showAll) {
            textArea.setBackground(new Color(220, 220, 220));
        } else {
            textArea.setBackground(primaryBackgroundColor);
        }
    });

  }

  public void toggleFloatingWindow(boolean visible) {
    floatingWindow.setVisible(visible);
  }

  /**
   * Toggles the checkmark icon on the given menu item.
   * 
   * @param menuItem the menu item to toggle
   */
  public void toggleCheckmark(JMenuItem menuItem) {
    if (menuItem.getIcon() == null) {
      menuItem.setIcon(UIManager.getIcon("CheckBox.icon"));
    } else {
      menuItem.setIcon(null);
    }
  }

  /**
   * Checks if the given menu item has the checkmark icon enabled.
   * 
   * @param menuItem the menu item to check
   * @return true if the checkmark icon is enabled, false otherwise
   */
  public boolean isCheckmarkEnabled(JMenuItem menuItem) {
    return menuItem.getIcon() != null;
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
   * Creates a map viewer panel with a tile source and a default location.
   * 
   * @return the map viewer panel
   */
  private JMapViewer createMapViewer() {
    JMapViewer mapViewer = new JMapViewer();
    mapViewer.setTileSource(new OsmTileSource.Mapnik());
    // Supprimer tous les écouteurs existants liés à la souris
    for (var listener : mapViewer.getMouseListeners()) {
      mapViewer.removeMouseListener(listener);
    }
    // Center the map on Paris (latitude: 48.8566, longitude: 2.3522)
    mapViewer.setDisplayPosition(
        new org.openstreetmap.gui.jmapviewer.Coordinate(48.8566, 2.3522), 10);

    return mapViewer;
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

  /**
   * Creates a styled JButton with rounded edges and hover effects.
   * 
   * @return the search button
   */
  private JButton createSearchButton() {
    JButton buttonSearch = new JButton("Rechercher");
    buttonSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
    buttonSearch.setBackground(accentColor);
    buttonSearch.setForeground(textColor);
    buttonSearch.setFocusPainted(false);
    buttonSearch.setBorder(new RoundedBorder(10)); // Rounded border
    buttonSearch.setPreferredSize(new Dimension(200, 50));
    buttonSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return buttonSearch;
  }

  /**
   * Adds a marker to the map.
   *
   * @param      latitude   The latitude
   * @param      longitude  The longitude
   * @param      color      The color
   */
  private void addMarker(double latitude, double longitude, Color color){
    MapMarkerDot marker;

    if (color != null) {
        marker = new MapMarkerDot(color, latitude, longitude);
        marker.setBackColor(color);
    } else {
        marker = new MapMarkerDot(latitude, longitude);
    }
    
    mapViewer.addMapMarker(marker);
  }

  /**
   * Displays the specific transport type on the map with all their lines.
   *
   * @param      lines  The list of lines of the graph
   * @param      type   The type to display
   */
  public void displayTransportType(ArrayList<Line> lines, TransportTypes type) {
      cleanMap();

      for (Line line : lines) {
          if (line.getType() != type) {
              continue;
          }
          
          Color lineColor = Color.decode("#" + line.getColor());
          
          for (Subline subline : line.getListOfSublines()) {
              for (int i = 0; i < subline.getListOfStops().size() - 1; i++) {
                  Stop stop = subline.getListOfStops().get(i);
                  Stop nextStop = subline.getListOfStops().get(i + 1);
                  
                  Coordinate start = new Coordinate(stop.getLatitude(), stop.getLongitude());
                  Coordinate end = new Coordinate(nextStop.getLatitude(), nextStop.getLongitude());
                  addPolygonLine(start, end, lineColor, false);
              }
          }
      }
      
      mapViewer.setDisplayPosition(new Coordinate(48.8566, 2.3522), 10);
      mapViewer.repaint();
  }

  /**
   * Adds a polygon line to link stops.
   *
   * @param      start   The start coordinates
   * @param      end     The end coordinates
   * @param      color   The color
   * @param      isDash  isDash for walking
   */
  private void addPolygonLine(Coordinate start, Coordinate end, Color color, boolean isDash){
    MapPolygon line;
    if (isDash) {
        line = new GrayDashedMapPolygon(start, end, start);
    } else if (color != null) {
        line = new ColoredMapPolygon(start, end, start, color);
    } else {
        line = new ColoredMapPolygon(start, end, start, Color.BLACK);
    }
    mapViewer.addMapPolygon(line);
  }

  /**
   * Clears all markers and polygons from the map.
   */
  public void cleanMap() {
    mapViewer.removeAllMapMarkers();
    mapViewer.removeAllMapPolygons();
    mapViewer.repaint();
  }

  /**
   * Reads and displays the contents of a .txt file in a formatted panel.
   * 
   * @return the panel containing the text content
   */
  public JPanel displayPath(ArrayList<SegmentItineraire> segments) {
    // Clear the map and content panel before displaying new paths
    cleanMap();

    // Clear the content panel
    contentPanel.removeAll();
    contentPanel.revalidate();

    // Create a new panel to display the paths
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
    pathPanel.setBackground(primaryBackgroundColor); // Background color of paths panel
    pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel

    // Iterate through the segments and display each one
    for (SegmentItineraire segment : segments) {
      // Point de départ
      if ( segments.indexOf(segment) == 0 ){
        Stop departStop = segment.getStops().get(0);
        MapMarkerDot departDot= new MapMarkerDot(Color.RED, departStop.getLatitude(), departStop.getLongitude());
        departDot.setBackColor(Color.RED);
        mapViewer.addMapMarker(departDot);        
      }

      // Point d'arrivée
      if ( segments.indexOf(segment) == segments.size() - 1 ){
        Stop arriveStop = segment.getStops().get(segment.getStops().size()-1);
        MapMarkerDot arriveDot= new MapMarkerDot(Color.BLUE, arriveStop.getLatitude(), arriveStop.getLongitude());
        arriveDot.setBackColor(Color.BLUE);
        mapViewer.addMapMarker(arriveDot);
      }

      // Print the number of stops
      String timeDepart = segment.getHeureDepart().toString().substring(0, 5);
      String timeArrivee = segment.getHeureArrivee().toString().substring(0, 5);

      if (segment.getSubline().getSublineType() == TransportTypes.Walk) {
        JTextArea sublineTextArea = createTextAreaOutput(segment.getSubline().getAssociatedLine().getName()
            + " - " + timeDepart + " - " + timeArrivee);
        pathPanel.add(sublineTextArea);
        pathPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between segments
        Stop startStop = segment.getStops().get(0);
        Stop endStop = segment.getStops().get(segment.getStops().size() - 1);
        Coordinate mStart = new Coordinate(startStop.getLatitude(), startStop.getLongitude());
        Coordinate mEnd = new Coordinate(endStop.getLatitude(), endStop.getLongitude());
        MapPolygon grayPolygon = new GrayDashedMapPolygon(mStart, mEnd, mStart);
        mapViewer.addMapPolygon(grayPolygon);
        continue; // Skip if the subline type is "Walk"
      }
      // Print the subline name
      JTextArea sublineTextArea = createTextAreaOutput(segment.getSubline().getAssociatedLine().getName() + " - "
          + segment.getSubline().getSublineType() + " - " + timeDepart + " - "
          + timeArrivee);
      pathPanel.add(sublineTextArea);

      // Print the first subline name
      Stop startStop = segment.getStops().get(0);
      Stop finishStop = segment.getStops().get(segment.getStops().size()-1);
      pathPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add spacing between segments

      JTextArea startTextArea = createTextAreaOutput(startStop.getNameOfAssociatedStation());
      startTextArea.setBorder(BorderFactory.createEmptyBorder(0, 20, 60, 0)); // Add bottom margin
      pathPanel.add(startTextArea);

      Coordinate mStart = new Coordinate(startStop.getLatitude(), startStop.getLongitude());
      mapViewer.setDisplayPosition(mStart, 12);
      MapMarkerDot startMarker = new MapMarkerDot(startStop.getLatitude(), startStop.getLongitude());
      mapViewer.addMapMarker(startMarker);
      MapMarkerDot finishMarker = new MapMarkerDot(finishStop.getLatitude(), finishStop.getLongitude());
      mapViewer.addMapMarker(finishMarker);    
      // draw the path and add TextAreas for each stop
      for (int i = 1; i < segment.getStops().size(); i++) {
        Stop stop = segment.getStops().get(i);
        // add TextArea for the stop
        JTextArea stopTextArea = createTextAreaOutput(stop.getNameOfAssociatedStation());
        stopTextArea.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0)); // Add bottom margin
        pathPanel.add(stopTextArea);
        // draw the path
        Coordinate mEnd = new Coordinate(stop.getLatitude(), stop.getLongitude()); // Paris center
        String color = "#" + segment.getSubline().getAssociatedLine().getColor();
        MapPolygon coloredPolygon = new ColoredMapPolygon(mStart, mEnd, mStart, Color.decode(color));
        MapMarkerDot markerDot = new MapMarkerDot(mEnd);
        mapViewer.addMapPolygon(coloredPolygon);
        //mapViewer.addMapMarker(markerDot);
        // update the start
        mStart = mEnd;
      }


    }
    mapViewer.repaint();
    contentPanel.repaint();
    return pathPanel;
  }

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

  public class SolidCircle extends JPanel {
    Color color;
  
    public SolidCircle(Color color){
      this.color = color;
      this.setBackground(Color.WHITE);
      this.setOpaque(true);
    }
     @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

        // Cast to Graphics2D for better control
      Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smooth edges
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Define the diameter of the circle
      int diameter = Math.min(getWidth(), getHeight()) / 2;

        // Calculate top-left corner to center the circle
      int x = (getWidth() - diameter) / 2;
      int y = (getHeight() - diameter) / 2;

      // Draw filled circle
      g2d.setColor(color);
      g2d.fillOval(x, y, diameter, diameter);

      g2d.setColor(Color.BLACK);
      g2d.setStroke(new BasicStroke(1)); 
      g2d.drawOval(x, y, diameter, diameter);
    }
  }
  public class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Image could not be loaded: " + imagePath);
        }
        this.setPreferredSize(new Dimension(15, 15));

    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      if (image != null) {
          // Scale image to fit the panel
          this.setBackground(Color.WHITE);
          this.setOpaque(true);

          int width = getWidth();
          int height = getHeight();
          int lessOf = height < width ? height : width;

          g.drawImage(image, 0, 0, lessOf, lessOf, this);
      }
    }
  }

  private String getResourcePathForTransportTypeIcones (TransportTypes tty) throws Error {
    String ret = "src/main/resources-filtered/fr/u_paris/gla/project";
    //Rail, Subway, Bus, Tram, Walk, Funicular
    switch (tty) {
      case Rail -> {
        return ret + "/icone-paris-rail.png";
      }
      case Subway -> {
        return ret + "/icone-paris-subway.png";
      }
      case Bus -> {
        return ret + "/icone-paris-bus.png";
      }
      case Tram -> {
        return ret + "/icone-paris-tram.png";
      }
      case Walk -> { 
        return ret + "/icone-paris-walk.png";
      } 
      case Funicular -> {
        return ret + "/icone-paris-funicular.png";
      }
      default -> {
        throw new RuntimeException("You tried to match a case of TransportType that does not exist");
      }
    }
  }
  public class TextSquarePanel extends JPanel {
    private String text;
    private Color squareColor;

    public TextSquarePanel(String text, Color squareColor) {
        this.text = text;
        this.squareColor = squareColor;
        this.setPreferredSize(new Dimension(15, 15));
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    public void setSquareColor(Color color) {
        this.squareColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        // Draw background square
        g.setColor(squareColor);
        g.fillRect(0, 0, width, height);

        // Find max font size that fits within the square
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int fontSize = height; // start with maximum font size
        Font font = new Font("SansSerif", Font.BOLD, fontSize);
        FontMetrics fm;

        // Reduce font size until it fits both width and height
        while (fontSize > 5) {
            font = font.deriveFont((float) fontSize);
            fm = g2d.getFontMetrics(font);
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent() + fm.getDescent();

            if (textWidth <= width * 0.9 && textHeight <= height * 0.9) {
                break;
            }

            fontSize--;
        }

        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height + fm.getAscent() - fm.getDescent()) / 2;

        g2d.drawString(text, x, y);
        g2d.dispose();
    }
  }

  public JPanel displayTextItinerary(ArrayList<SegmentItineraire> segments){
    textItineraryPanel.removeAll();
    textItineraryPanel.revalidate();


    JPanel startAndEndingTimesJP = new JPanel();
    startAndEndingTimesJP.setBackground(primaryBackgroundColor);
    startAndEndingTimesJP.setLayout(new BoxLayout(startAndEndingTimesJP, BoxLayout.Y_AXIS));
    
    LocalTime startTimeLT = segments.get(0).getHeureDepart();
    LocalTime arrivalTimeLT = segments.get(segments.size() -1).getHeureArrivee();

    Duration travelTimeD = Duration.between(startTimeLT, arrivalTimeLT);
    //If total time is negative, then modulo 24 hours to get correct difference between arrival and departure. 
    if(travelTimeD.isNegative()){
      travelTimeD = travelTimeD.plusHours(24);
     } 

    JTextArea jl1 = new JTextArea(
      String.format("Départ : %s", 
      startTimeLT.toString().substring(0, 5))
    );
    jl1.setEditable(false);
    jl1.setFocusable(false);
    startAndEndingTimesJP.add(jl1);
    

    JTextArea jl2 = new JTextArea(
      String.format("Arrivée : %s", 
      arrivalTimeLT.toString().substring(0, 5))
    );
    jl2.setEditable(false);
    jl2.setFocusable(false);

    startAndEndingTimesJP.add(jl2);

    JTextArea jl3 = new JTextArea(
      String.format("Temps de trajet estimée: %dh, %dm, %ds",
      travelTimeD.toHours(), 
      travelTimeD.toMinutesPart(),
      travelTimeD.toSecondsPart())
    );
    jl3.setEditable(false);
    jl3.setFocusable(false);


    startAndEndingTimesJP.add(jl3);
  
    JPanel textItinJP = new JPanel();

    textItinJP.setLayout(new GridBagLayout());
    textItinJP.setOpaque(true);
    textItinJP.setBackground(Color.WHITE);
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    int iterations = 0;
    int fourTimeIterations = 0;

    for (SegmentItineraire segment : segments) {
      String timeStart = segment.getHeureDepart().toString().substring(0, 5);
      String placeToStart = segment.getStops().get(0).getNameOfAssociatedStation();
      TransportTypes typeOfLine = segment.getSubline().getAssociatedLine().getType();
      ImagePanel iconeOfTransportType = new ImagePanel(getResourcePathForTransportTypeIcones(typeOfLine));

      //Add Starting time
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 0; 
      c.gridy = fourTimeIterations;
      textItinJP.add(new JLabel(timeStart + "  "), c);

      
      //Determine color of line for this segment
      Color color = Color.decode("#" + segment.getSubline().getAssociatedLine().getColor());

      //First Circle is red, last is reblued, all others are yellow
      if(iterations == 0){
        SolidCircle sc = new SolidCircle(Color.RED);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = fourTimeIterations;
        c.weightx = 0.5;
        textItinJP.add(sc, c);
      }
      else{
        SolidCircle sc = new SolidCircle(Color.YELLOW);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = fourTimeIterations;
        c.weightx = 0.5;
        textItinJP.add(sc, c);
      }
      
      
      //If walking, add dashed lines
      if (segment.getSubline().getSublineType().equals(TransportTypes.Walk)) {
        DashedColoredLine sl1 = new DashedColoredLine(color);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = fourTimeIterations + 1;
        c.weightx = 1;
        textItinJP.add(sl1, c);
  
        DashedColoredLine sl2 = new DashedColoredLine(color);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = fourTimeIterations + 2;
        c.weightx = 0;
        textItinJP.add(sl2, c);
      }
      //else, add solid lines
      else {
        SolidColoredLine sl1 = new SolidColoredLine(color);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = fourTimeIterations + 1;
        c.weightx = 1;
        textItinJP.add(sl1, c);
  
        SolidColoredLine sl2 = new SolidColoredLine(color);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = fourTimeIterations + 2;
        c.weightx = 0;
        textItinJP.add(sl2, c);
      }
      
      
      JPanel totalListOfStopsAndIconeAndDirection = new JPanel();
      totalListOfStopsAndIconeAndDirection.setLayout(new BoxLayout(totalListOfStopsAndIconeAndDirection, BoxLayout.Y_AXIS));
      totalListOfStopsAndIconeAndDirection.setBorder(BorderFactory.createEmptyBorder(0, 05, 0, 0));
      totalListOfStopsAndIconeAndDirection.setBackground(Color.WHITE);
      totalListOfStopsAndIconeAndDirection.setOpaque(true);

      JPanel collapsableListOfStopsInItinerary = new JPanel();
      collapsableListOfStopsInItinerary.setLayout(new BoxLayout(collapsableListOfStopsInItinerary, BoxLayout.Y_AXIS));
      collapsableListOfStopsInItinerary.setBackground(Color.WHITE);
      collapsableListOfStopsInItinerary.setOpaque(true);
      collapsableListOfStopsInItinerary.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
      
      JPanel typeOfTransportAndDirection = new JPanel();
      typeOfTransportAndDirection.setLayout(new FlowLayout(FlowLayout.LEFT));
      typeOfTransportAndDirection.setOpaque(true);
      typeOfTransportAndDirection.setBackground(Color.WHITE);

      if (!segment.getSubline().getSublineType().equals(TransportTypes.Walk)) {
        for(int i = 1; i < segment.getStops().size() - 1; i ++){
          Stop stp = segment.getStops().get(i);
          JPanel stopIconAndStop = new JPanel();
          stopIconAndStop.setLayout(new FlowLayout(FlowLayout.LEFT));
          stopIconAndStop.setOpaque(true);
          stopIconAndStop.setBackground(Color.WHITE);

          stopIconAndStop.add(new SolidCircle(Color.YELLOW));
          stopIconAndStop.add(new JLabel(stp.getNameOfAssociatedStation()));

          collapsableListOfStopsInItinerary.add(stopIconAndStop);
        }
        totalListOfStopsAndIconeAndDirection.add(getIconAndLine(segment.getSubline()));
        totalListOfStopsAndIconeAndDirection.add(collapsableListOfStopsInItinerary);
      }
      else {
        if(segment.getStops().get(0).getNameOfAssociatedStation().equals(segment.getStops().get(1).getNameOfAssociatedStation())){
          typeOfTransportAndDirection.add(iconeOfTransportType);
          typeOfTransportAndDirection.add(new JLabel("Changement de quai"));
        }
        else {
          typeOfTransportAndDirection.add(iconeOfTransportType);
          typeOfTransportAndDirection.add(new JLabel("Marchez jusqu'à destination"));
        }
        totalListOfStopsAndIconeAndDirection.add(typeOfTransportAndDirection);
      }
      
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 2;
      c.gridy = fourTimeIterations + 2;
      c.weightx = 0.5;
      textItinJP.add(totalListOfStopsAndIconeAndDirection, c);


      SolidDownwardsArrow sda = new SolidDownwardsArrow(color);
      c.fill = GridBagConstraints.VERTICAL;
      c.gridx = 1;
      c.gridy = fourTimeIterations + 3;
      c.weightx = 1;
      
      textItinJP.add(sda, c);
      

      //Add place to start 
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 2; 
      c.gridwidth = 1;
      c.gridy = fourTimeIterations; 
      textItinJP.add(new JLabel("  " + placeToStart), c);

      iterations += 1;
      fourTimeIterations = iterations * 4;
      
      if(iterations == segments.size()){
        String timeArrival = segment.getHeureArrivee().toString().substring(0, 5);
        String placeToArrive = segment.getStops().get(segment.getStops().size() - 1).getNameOfAssociatedStation();
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0; 
        c.gridy = fourTimeIterations ;
        //Add Arriving time
        textItinJP.add((new JLabel(timeArrival)), c);


        SolidCircle scIt = new SolidCircle(Color.BLUE);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = fourTimeIterations;
        c.weightx = 0.5;
        textItinJP.add(scIt, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2; 
        c.gridwidth = 2;
        c.gridy = fourTimeIterations; 
        //Add Arrival time
        textItinJP.add((new JLabel(placeToArrive)), c);
      }
    }





    JPanel fullTextItinerary = new JPanel();
    fullTextItinerary.setOpaque(true);
    fullTextItinerary.setBackground(primaryBackgroundColor);
    fullTextItinerary.setLayout(new BoxLayout(fullTextItinerary, BoxLayout.Y_AXIS));

    fullTextItinerary.add(startAndEndingTimesJP);

    textItinJP.setBorder(BorderFactory.createEmptyBorder(20, 00, 0, 0)); // explicitly remove borders
    fullTextItinerary.add(textItinJP);

    SwingUtilities.invokeLater(() -> {
            getTextItineraryScrollPane().getVerticalScrollBar().setValue(0); 
    });

    return fullTextItinerary;
  }

  public JPanel getIconAndLine(Subline subline){

    JPanel typeOfTransportAndDirection = new JPanel();
    typeOfTransportAndDirection.setLayout(new FlowLayout(FlowLayout.LEFT));
    typeOfTransportAndDirection.setBackground(Color.WHITE);
    typeOfTransportAndDirection.setOpaque(true);
    typeOfTransportAndDirection.add(new ImagePanel(getResourcePathForTransportTypeIcones(subline.getSublineType())));
    typeOfTransportAndDirection.add(new TextSquarePanel(subline.getAssociatedLine().getName(), Color.decode("#" + subline.getAssociatedLine().getColor())));
    typeOfTransportAndDirection.add(new JLabel(" Direction : " + subline.getDestination().getNameOfAssociatedStation()));

    return typeOfTransportAndDirection;

  }
  

  /**
   * Calls the displaySpecificLine to display one line on the map.
   *
   * @param      lines    The list of lines
   * @param      type     The type
   * @param      lineNum  The line number
   */
  public void displayLine(ArrayList<Line> lines, TransportTypes type, String lineNum){
      boolean lineFound = displaySpecificLine(lines, type, lineNum);
      if (!lineFound) {
          JOptionPane.showMessageDialog(this, "Pas de ligne trouvée.",
              "Line Not Found", JOptionPane.INFORMATION_MESSAGE);

      }
  }

  /**
   * Adds the specific line to the map.
   *
   * @param      lines    The list of lines
   * @param      type     The type
   * @param      lineNum  The line number
   *
   * @return     Returns if the line was found
   */
  private boolean displaySpecificLine(ArrayList<Line> lines, TransportTypes type, String lineNum) {
      cleanMap();

      boolean lineFound = false;
      
      for (Line line : lines) {
          if (line.getType() != type || !line.getName().equals(lineNum)) {
              continue;
          }
          
          lineFound = true;
          Color lineColor = Color.decode("#" + line.getColor());
          
          for (Subline subline : line.getListOfSublines()) {
              for (Stop stop : subline.getListOfStops()) {
                  addMarker(stop.getLatitude(), stop.getLongitude(), null);
              }
              
              for (int i = 0; i < subline.getListOfStops().size() - 1; i++) {
                  Stop stop = subline.getListOfStops().get(i);
                  Stop nextStop = subline.getListOfStops().get(i + 1);
                  
                  Coordinate start = new Coordinate(stop.getLatitude(), stop.getLongitude());
                  Coordinate end = new Coordinate(nextStop.getLatitude(), nextStop.getLongitude());
                  addPolygonLine(start, end, lineColor, false);
              }
          }
          
          // Center the map
          mapViewer.setDisplayPosition(PARIS_CENTER, DEFAULT_ZOOM);
          mapViewer.repaint();
          break;
      }
      
      return lineFound;
  }

  /**
   * Adds a toggle functionality to expand or collapse the list of times when
   * clicking on a subline text.
   * 
   * @param sublineTextArea the JTextArea representing the subline
   * @param timesPanel      the JPanel containing the list of times
   */
  private void addToggleFunctionality(JPanel jp, JPanel timesPanel) {
    jp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jp.addMouseListener(new java.awt.event.MouseAdapter() {
      private boolean expanded = false;

      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        expanded = !expanded;
        timesPanel.setVisible(expanded);
        timesPanel.getParent().revalidate();
        timesPanel.getParent().repaint();
      }
    });
  }

  /**
   * Makes a JPanel to display the schedules for a stop.
   *
   * @param      stop  The stop
   *
   * @return     The JPanel.
   */
  public JPanel displayListOfStopDeparture(Stop stop) {
    textItineraryPanel.removeAll();
    HashMap<Subline, ArrayList<LocalTime>> departures = stop.getDepartures();
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
    pathPanel.setBackground(primaryBackgroundColor); // Background color of paths panel
    pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel

    JTextArea stopTextArea = createTextAreaOutput(
      "Horaires de départ pour la station : \n" 
      + stop.getNameOfAssociatedStation()
      + "\nCliquez sur la ligne pour afficher ces horaires");    

    stopTextArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add bottom margin
    pathPanel.add(stopTextArea);

    ArrayList<Subline> uniqueSublineList = new ArrayList<>();
    for (Subline subline : departures.keySet()) {
      boolean isUnique = true;
      for (Subline existingSubline : uniqueSublineList) {
        if (existingSubline.getAssociatedLine().getName().equals(subline.getAssociatedLine().getName())
            && existingSubline.getDestination().getNameOfAssociatedStation()
                .equals(subline.getDestination().getNameOfAssociatedStation())) {
          isUnique = false;
          break;
        }
      }
      if (isUnique) {
        uniqueSublineList.add(subline);
      }
    }
    for (Subline subline : uniqueSublineList) {

      //get the subLine's type, the corresponding Icon, and the line number all in a neat JPanel.
      JPanel temp = getIconAndLine(subline);
      pathPanel.add(temp);

      JPanel timesPanel = new JPanel();
      timesPanel.setLayout(new BoxLayout(timesPanel, BoxLayout.Y_AXIS)); // Ensure vertical layout for times
      timesPanel.setVisible(false); // Initially hidden

      ArrayList<LocalTime> times = departures.get(subline);

      if (times == null || times.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No departures available for this subline.",
            "No Departures", JOptionPane.INFORMATION_MESSAGE);
        mapViewer.removeAllMapMarkers();
        mapViewer.removeAllMapPolygons();
        mapViewer.repaint();
        pathPanel.removeAll();
        break;
      }

      for (LocalTime time : times) {
        JTextArea timeTextArea = createTextAreaOutput(time.toString());
        timesPanel.add(timeTextArea);
      }

      pathPanel.add(timesPanel);
      addToggleFunctionality(temp, timesPanel); // Add toggle functionality
    }

    //Makes the scrolling bar go to the top
    SwingUtilities.invokeLater(() -> {
      getTextItineraryScrollPane().getVerticalScrollBar().setValue(0); 
    });

    return pathPanel;
  }

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
    public void paint(Graphics g, java.util.List<Point> points) {
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
  }

  // Helper function to draw a line between points
  private void drawLines(Graphics2D g2d, java.util.List<Point> points) {
    for (int i = 1; i < points.size(); i++) {
      Point p1 = points.get(i - 1);
      Point p2 = points.get(i);
      g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
  }


  /**
   * Custom MapPolygon to make a line between two stops, for walking.
   */
  public class GrayDashedMapPolygon extends MapPolygonImpl {
    private static final Color GRAY_COLOR = Color.GRAY;

    public GrayDashedMapPolygon(Coordinate start, Coordinate end, Coordinate middle) {
      super(start, end, middle);
    }

    @Override
    public void paint(Graphics g, java.util.List<Point> points) {
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

  /**
   * Launches the application.
   */
  public void launch() {
    setVisible(true);
    requestFocusInWindow();
  }

  /**
   * Gets the menu bar item at the specified index.
   * 
   * @return the menu bar item
   */
  public JMenuItem getMenuItem(int menuIndex, int itemIndex) {
    JMenuBar menuBar = this.getJMenuBar();
    if (menuBar == null || menuIndex < 0 || menuIndex >= menuBar.getMenuCount()) {
      System.err.println("Invalid menu index: " + menuIndex);
      return null;
    }

    JMenu menu = menuBar.getMenu(menuIndex);
    if (menu == null || itemIndex < 0 || itemIndex >= menu.getItemCount()) {
      System.err.println("Invalid item index: " + itemIndex + " in menu: " + menuIndex);
      return null;
    }

    return menu.getItem(itemIndex);
  }

  /**
   * Gets the research button.
   * 
   * @return the research button
   */
  public JButton getResearchButton() {
    return researchButton;
  }

  /**
   * Gets the text areas for start and end.
   * 
   * @return the text areas
   */
  public JTextArea getTextStart() {
    return (JTextArea) textStart.getViewport().getView();
  }

  /**
   * Gets the text area for the end.
   * 
   * @return the text area
   */
  public JTextArea getTextEnd() {
    return (JTextArea) textEnd.getViewport().getView();
  }

  /**
   * Gets the map viewer.
   * 
   * @return the map viewer
   */
  public JMapViewer getMapViewer() {
    return mapViewer;
  }

  /**
   * Gets the content panel.
   * 
   * @return the content panel
   */
  public JPanel getContentPanel() {
    return contentPanel;
  }

  /**
   * Gets the text area for the path.
   * 
   * @return the text area
   */
  public JCheckBox getDistCheckBox() {
    return distCheckBox;
  }

  public JPanel getTextItineraryPanel(){
    return textItineraryPanel;
  }

  public JScrollPane getContentScrollPane(){
    return contentScrollPane;
  }
  public JScrollPane getTextItineraryScrollPane(){
    return textItineraryScrollPane;
  }

  /**
   * Gets the text area for the path.
   * 
   * @return the text area
   */
  public JCheckBox getTimeCheckBox() {
    return timeCheckBox;

  }

  public JButton getViewLineButton() {
    return viewLineButton;
  }

  public JTextArea getnumLine() {
    return (JTextArea) numLine.getViewport().getView();
  }

  public JDialog getFloatingWindow() {
    return floatingWindow;
  }

  public JComboBox<String> getLineTypeDropdown() {
    return lineTypeDropdown;
  }

  public JComboBox<String> getComboBoxHours() {
    return comboBoxHours;
  }

  public JComboBox<String> getComboBoxMinutes() {
    return comboBoxMinutes;
  }

}
    
    