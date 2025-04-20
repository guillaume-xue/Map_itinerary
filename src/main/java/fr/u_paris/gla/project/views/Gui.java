package fr.u_paris.gla.project.views;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import java.awt.*;
import java.time.LocalTime;

import javax.swing.*;
import javax.swing.border.AbstractBorder;

import java.util.ArrayList;
import java.util.HashMap;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import fr.u_paris.gla.project.utils.Pair;

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
  private JButton researchButton;
  private static final Color textColor = new Color(11, 22, 44);
  private static final Color bordeColor = new Color(88, 88, 88);
  private static final Color primaryBackgroundColor = new Color(240, 240, 240);
  private static final Color accentColor = new Color(76, 175, 80);

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
    JMenu viewMenu = new JMenu("View");
    JMenuItem busMenu = new JMenuItem("Bus");
    JMenuItem metroMenu = new JMenuItem("Metro");
    viewMenu.add(busMenu);
    viewMenu.add(metroMenu);
    menuBar.add(viewMenu);
    this.setJMenuBar(menuBar);

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
    buttonPanel.add(this.researchButton); // Add the search button to this centered panel

    // Add the button panel to the research panel
    researchPanel.add(buttonPanel);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 5)));

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

    // Add the split pane and the map panel to the main content panel
    JPanel mainContentPanel = new JPanel();
    mainContentPanel.setLayout(new BorderLayout());
    mainContentPanel.add(splitPane, BorderLayout.WEST);
    mainContentPanel.add(mapPanel, BorderLayout.CENTER);

    add(mainContentPanel);
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
    JButton buttonSearch = new JButton("Search");
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
   * Displays all bus stops on the map.
   * 
   * @param graph the graph containing the bus stops
   */
  public void viewLine(Graph graph, String type) {
    mapViewer.removeAllMapMarkers();
    mapViewer.removeAllMapPolygons();
    ArrayList<Line> tmpLine = new ArrayList<>();
    ArrayList<Subline> tmpLineSub = new ArrayList<>();
    ArrayList<Stop> tmpLineStop = new ArrayList<>();
    for (Line line : graph.getListOfLines()) {
      if (tmpLine.contains(line) || !line.getType().equals(type)) {
        continue;
      }
      tmpLine.add(line);
      for (Subline subline : line.getListOfSublines()) {
        if (tmpLineSub.contains(subline)) {
          continue;
        }
        for (Stop stop : subline.getListOfStops()) {
          if (tmpLineStop.contains(stop)) {
            continue;
          }
          tmpLineStop.add(stop);
          Coordinate coord = new Coordinate(stop.getLongitude(), stop.getLatitude());
          MapMarkerDot marker = new MapMarkerDot(coord);
          mapViewer.addMapMarker(marker);
        }
      }
    }
    for (Stop stop : tmpLineStop) {
      for (Stop adjacentStop : stop.getAdjacentStops()) {
        if (tmpLineStop.contains(adjacentStop)) {
          Coordinate mStart = new Coordinate(stop.getLongitude(), stop.getLatitude());
          Coordinate mEnd = new Coordinate(adjacentStop.getLongitude(), adjacentStop.getLatitude());
          MapPolygon mLine = new MapPolygonImpl(mStart, mEnd, mStart);
          mapViewer.addMapPolygon(mLine);
        }
      }
    }
    mapViewer.setDisplayPosition(new Coordinate(48.8566, 2.3522), 10);
    mapViewer.repaint();
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
  public JPanel displayPath(ArrayList<Pair<Stop, LocalTime>> stopsAndTimes) {
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
    pathPanel.setBackground(primaryBackgroundColor); // Background color of paths panel
    pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel
    // initialize the first stop
    Stop stop = stopsAndTimes.get(0).getKey();
    JTextArea stopTextArea = createTextAreaOutput(stop.getNameOfAssociatedStation());
    pathPanel.add(stopTextArea);
    Coordinate mStart = new Coordinate(stop.getLongitude(), stop.getLatitude());
    mapViewer.setDisplayPosition(mStart, 12);
    MapMarkerDot parisMarker = new MapMarkerDot(mStart);
    mapViewer.addMapMarker(parisMarker);
    // draw the path and add TextAreas for each stop
    for (int i = 1; i < stopsAndTimes.size(); i++) {
      stop = stopsAndTimes.get(i).getKey();
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

  public JPanel displayListOfStopDeparture(HashMap<Subline, ArrayList<LocalTime>> departures) {
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
    pathPanel.setBackground(primaryBackgroundColor); // Background color of paths panel
    pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel
    System.out.println(departures);
    for (Subline subline : departures.keySet()) {
      ArrayList<LocalTime> times = departures.get(subline);
      for (LocalTime time : times) {
        JTextArea timeTextArea = createTextAreaOutput(time.toString());
        pathPanel.add(timeTextArea);
      }
    }
    return pathPanel;
  }

  /**
   * Launches the application.
   */
  public void launch() {
    setVisible(true);
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
}