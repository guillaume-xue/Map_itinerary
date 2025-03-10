package fr.u_paris.gla.project.views;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Gui extends JFrame {

  private static final int SCREEN_WIDTH = 900;
  private static final int SCREEN_HEIGHT = 600;
  private static final int RESEARCH_PANEL_WIDTH = 350;
  private static final int MIN_SCREEN_WIDTH = 600;
  private static final int MIN_SCREEN_HEIGHT = 300;
  private static final String PATH_FILE = "/paths.json";
  private JTextArea textStart;
  private JTextArea textEnd;
  private JPanel contentPanel;

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

    // Set a modern font and color scheme
    Color primaryColor = new Color(34, 40, 49);
    Color secondaryColor = new Color(45, 52, 54);
    Color accentColor = new Color(76, 175, 80);

    // Create text areas for the start and end
    this.textStart = createTextArea("From");
    JPanel startPanel = new JPanel();
    startPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    startPanel.setBackground(secondaryColor);
    startPanel.add(textStart);

    this.textEnd = createTextArea("To");
    JPanel endPanel = new JPanel();
    endPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    endPanel.setBackground(secondaryColor);
    endPanel.add(textEnd);

    addFocusListenerToTextArea();

    // Create a search button with hover effects
    JButton buttonSearch = createSearchButton(accentColor);

    // Create a panel for the research area
    JPanel researchPanel = new JPanel();
    researchPanel.setBackground(secondaryColor);
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
    buttonPanel.setBackground(secondaryColor); // Ensure the background matches
    buttonPanel.add(buttonSearch); // Add the search button to this centered panel

    // Add the button panel to the research panel
    researchPanel.add(buttonPanel);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Create a content panel for displaying JSON content
    contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(secondaryColor);

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
    JPanel mapPanel = new JPanel();
    mapPanel.setBackground(primaryColor);

    // Add the split pane and the map panel to the main content panel
    JPanel mainContentPanel = new JPanel();
    mainContentPanel.setLayout(new BorderLayout());
    mainContentPanel.add(splitPane, BorderLayout.WEST);
    mainContentPanel.add(mapPanel, BorderLayout.CENTER);

    add(mainContentPanel);
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
   */
  private JTextArea createTextArea(String text) {
    JTextArea textArea = new JTextArea(text);
    textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    textArea.setForeground(Color.WHITE);
    textArea.setBackground(new Color(48, 54, 63));
    textArea.setPreferredSize(new Dimension(RESEARCH_PANEL_WIDTH, 52));
    textArea.setCaretColor(Color.WHITE);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(true);
    textArea.setBorder(createRoundedBorder(15)); // Rounded border
    textArea.setAlignmentX(Component.CENTER_ALIGNMENT); // Align text to center
    return textArea;
  }

  /**
   * Creates a styled JButton with rounded edges and hover effects.
   */
  private JButton createSearchButton(Color accentColor) {
    JButton buttonSearch = new JButton("Search");
    buttonSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
    buttonSearch.setBackground(accentColor);
    buttonSearch.setForeground(Color.WHITE);
    buttonSearch.setFocusPainted(false);
    buttonSearch.setBorder(createRoundedBorder(20)); // Rounded border
    buttonSearch.setPreferredSize(new Dimension(200, 50));
    buttonSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Add action listener to add displayJsonContent to contentPanel
    buttonSearch.addActionListener(e -> {
      contentPanel.removeAll();
      contentPanel.add(displayJsonContent());
      contentPanel.revalidate();
      contentPanel.repaint();
    });

    return buttonSearch;
  }

  /**
   * Creates a rounded border with the given radius.
   */
  private Border createRoundedBorder(int radius) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
        BorderFactory.createEmptyBorder(radius, radius, radius, radius));
  }

  /**
   * Reads and displays the contents of a JSON file in a formatted panel.
   */
  private JPanel displayJsonContent() {
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
    pathPanel.setBackground(new Color(48, 54, 63)); // Background color of paths panel
    pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the panel

    try (InputStream inputStream = getClass().getResourceAsStream(PATH_FILE);
        InputStreamReader reader = new InputStreamReader(inputStream)) {
      JSONTokener tokener = new JSONTokener(reader);
      JSONObject jsonObject = new JSONObject(tokener);
      JSONArray jsonArray = jsonObject.getJSONArray("paths");

      for (int i = 0; i < jsonArray.length(); i++) {
        String path = jsonArray.getString(i);
        JTextField pathField = new JTextField(path);
        pathField.setEditable(false);
        pathField.setFocusable(false);
        pathField.setForeground(Color.WHITE);

        // Skip if the path is a number
        if (path.matches("\\d+")) {
          pathField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
          pathField.setBackground(new Color(45, 52, 54));
          pathField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding for the number
        } else {
          pathField.setBackground(new Color(48, 54, 63));
          pathField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
          pathField.setBorder(createRoundedBorder(15)); // Rounded border
        }
        pathPanel.add(pathField);
        pathPanel.add(Box.createRigidArea(new Dimension(0, 5)));
      }
    } catch (IOException e) {
      e.printStackTrace();
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