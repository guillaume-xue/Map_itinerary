package fr.u_paris.gla.project.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Gui extends JFrame {

  private static int SCREEN_WIDTH = 800;
  private static int SCREEN_HEIGHT = 600;
  private static int RESEARCH_PANEL_WIDTH = 400;
  private static final int MIN_SCREEN_WIDTH = 600;
  private static final int MIN_SCREEN_HEIGHT = 300;
  private static final String PATH_FILE = "/paths.json";

  /**
   * Constructor.
   */
  public Gui() {
    // Initialize the frame
    super("Map");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
    setMinimumSize(new java.awt.Dimension(MIN_SCREEN_WIDTH, MIN_SCREEN_HEIGHT)); // Set minimum size
    setLocationRelativeTo(null);

    // Create text areas for the start and end points
    JTextArea textStart = new JTextArea();
    textStart.setText("From");
    textStart.setPreferredSize(new Dimension(200, 80));

    JTextArea textEnd = new JTextArea();
    textEnd.setText("To");
    textEnd.setPreferredSize(new Dimension(200, 80));

    // Create a search button
    JButton buttonSearch = new JButton("Search");
    buttonSearch.setPreferredSize(new Dimension(200, 80));

    // Create a panel for the research
    JPanel researchPanel = new JPanel();
    researchPanel.setBackground(java.awt.Color.LIGHT_GRAY);
    researchPanel.setPreferredSize(new java.awt.Dimension(RESEARCH_PANEL_WIDTH, SCREEN_HEIGHT));
    researchPanel.setLayout(new BoxLayout(researchPanel, BoxLayout.Y_AXIS));
    researchPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    researchPanel.add(textStart);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    researchPanel.add(textEnd);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    researchPanel.add(buttonSearch);
    researchPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    // Add the JSON content display panel
    researchPanel.add(displayJsonContent());

    // Add the research panel to a scroll pane
    JScrollPane scrollPane = new JScrollPane(researchPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    // Create a panel for the map
    JPanel mapPanel = new JPanel();
    mapPanel.setBackground(java.awt.Color.GRAY);

    // Add the research panel and the map panel to the content panel
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(scrollPane, BorderLayout.WEST);
    contentPanel.add(mapPanel, BorderLayout.CENTER);

    add(contentPanel);
  }

  /**
   * Reads and displays the contents of a JSON file.
   */
  private JPanel displayJsonContent() {
    JPanel pathPanel = new JPanel(); // Panel to display the paths
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS)); // Set layout
    try (InputStream inputStream = getClass().getResourceAsStream(PATH_FILE);
        InputStreamReader reader = new InputStreamReader(inputStream)) { // Read the JSON file
      JSONTokener tokener = new JSONTokener(reader); // Create a JSON tokener
      JSONObject jsonObject = new JSONObject(tokener); // Create a JSON object
      JSONArray jsonArray = jsonObject.getJSONArray("paths"); // Get the paths array

      for (int i = 0; i < jsonArray.length(); i++) { // Loop through the paths
        String path = jsonArray.getString(i);
        JTextField textArea = new JTextField(path);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        pathPanel.add(textArea);
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
