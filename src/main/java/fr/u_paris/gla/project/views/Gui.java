package fr.u_paris.gla.project.views;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Gui extends JFrame {

  private static final int SCREEN_WIDTH = 900;
  private static final int SCREEN_HEIGHT = 600;
  private static final int RESEARCH_PANEL_WIDTH = 350;
  private static final int MIN_SCREEN_WIDTH = 600;
  private static final int MIN_SCREEN_HEIGHT = 300;
  private static final String TXT_PATH_FILE = "/txt/paths.txt";
  private JTextArea textStart;
  private JTextArea textEnd;
  private JPanel contentPanel;
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
    JPanel mapPanel = createMapViewerPanel();

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
  private static JPanel createMapViewerPanel() {
    JMapViewer mapViewer = new JMapViewer();
    mapViewer.setTileSource(new OsmTileSource.Mapnik());

    // Center the map on Paris (latitude: 48.8566, longitude: 2.3522)
    mapViewer.setDisplayPosition(
        new org.openstreetmap.gui.jmapviewer.Coordinate(48.8566, 2.3522), 10);

    JPanel mapPanel = new JPanel();
    mapPanel.setLayout(new java.awt.BorderLayout());
    mapPanel.add(mapViewer, java.awt.BorderLayout.CENTER);

    return mapPanel;
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
      contentPanel.add(displayTxtContent());
      contentPanel.revalidate();
      contentPanel.repaint();
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
  private JPanel displayTxtContent() {
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
    pathPanel.setBackground(primaryBackgroundColor); // Background color of paths panel
    pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel

    try (InputStream inputStream = Gui.class.getResourceAsStream(TXT_PATH_FILE)) {
      if (inputStream == null) {
        // Handle the case where the file is not found
        JLabel errorLabel = new JLabel("Error: File not found - " + TXT_PATH_FILE);
        errorLabel.setForeground(Color.RED);
        pathPanel.add(errorLabel);
        return pathPanel;
      }
      InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      StringBuilder content = new StringBuilder();
      int c;
      while ((c = reader.read()) != -1) {
        if (c == '\n') {
          String line = content.toString();
          JTextArea lineTextArea = createTextArea(line);
          lineTextArea.setEditable(false); // Make the text area read-only
          lineTextArea.setFocusable(false); // Disable focus to prevent selection
          if (line.matches("\\d+")) {
            // If the line is a number, display it without additional formatting
            lineTextArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
          }
          pathPanel.add(lineTextArea);
          content.setLength(0); // Clear the content for the next line
        } else {
          content.append((char) c);
        }
      }
      // Add the last line if it exists
      if (content.length() > 0) {
        String line = content.toString();
        JTextArea lineTextArea = createTextArea(line);
        lineTextArea.setEditable(false);
        lineTextArea.setFocusable(false); // Disable focus to prevent selection
        if (line.matches("\\d+")) {
          lineTextArea.setFont(new Font("Segoe UI", Font.BOLD, 16));
          lineTextArea.setBackground(primaryBackgroundColor);
        }
        pathPanel.add(lineTextArea);
      }
    } catch (IOException e) {
      e.printStackTrace();
      JLabel errorLabel = new JLabel("Error reading file: " + e.getMessage());
      errorLabel.setForeground(Color.RED);
      pathPanel.add(errorLabel);
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