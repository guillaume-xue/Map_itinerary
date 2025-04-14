package fr.u_paris.gla.project.controllers;

import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.u_paris.gla.project.astar.AStar;
import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.utils.CSVExtractor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import fr.u_paris.gla.project.views.Gui;

public class GUIController {

  private Gui gui;
  private Graph graph;

  /**
   * Constructor for GUIController.
   * Initializes the GUI and sets up event listeners.
   */
  public GUIController() {
    SwingUtilities.invokeLater(() -> {
      // Create the main window
      this.gui = new Gui();
      new KeyboardController(gui.getTextStart());
      new KeyboardController(gui.getTextEnd());
      new MouseController(gui.getMapViewer(), gui.getTextStart(), gui.getTextEnd());
      String[] args = { "--parse", "mapData.csv", "junctionsData.csv" };
      this.graph = CSVExtractor.makeOjectsFromCSV(args);
      initFocusListenerToTextArea();
      initActionListenner();
      initMenuBar();
      this.gui.launch();
    });
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

  /**
   * Initializes the focus listener for the text areas.
   * Clears the default text when the text area gains focus.
   */
  private void initFocusListenerToTextArea() {
    gui.getTextStart().addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        JTextArea textStartArea = gui.getTextStart();
        JTextArea textEndArea = gui.getTextEnd();
        if (textStartArea.getText().equals("From")) {
          textStartArea.setText("");
        }
        if (textEndArea.getText().equals("")) {
          textEndArea.setText("To");
        }
      }
    });

    gui.getTextEnd().addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        JTextArea textStartArea = gui.getTextStart();
        JTextArea textEndArea = gui.getTextEnd();
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
   * Initializes the action listener for the research button.
   * When clicked, it retrieves coordinates from the text areas and displays the
   * path on the map.
   */
  private void initActionListenner() {
    gui.getResearchButton().addActionListener(e -> {
      if (gui.getTextStart().getText().equals("From") || gui.getTextEnd().getText().equals("To")) {
        JOptionPane.showMessageDialog(this.gui, "Veuillez entrer une adresse de départ et d'arrivée.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
        return;
      } else if (gui.getTextStart().getText().equals("From")) {
        JOptionPane.showMessageDialog(this.gui, "Veuillez entrer une adresse de départ.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
        return;
      } else if (gui.getTextEnd().getText().equals("To")) {
        JOptionPane.showMessageDialog(this.gui, "Veuillez entrer une adresse d'arrivée.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
        return;
      } else if (gui.getTextStart().getText().equals(gui.getTextEnd().getText())) {
        JOptionPane.showMessageDialog(this.gui, "Les adresses de départ et d'arrivée sont identiques.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
        return;
      } else if (gui.getTextStart().getText().equals("") || gui.getTextEnd().getText().equals("")) {
        JOptionPane.showMessageDialog(this.gui, "Veuillez entrer une adresse de départ et d'arrivée.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      AStar astar = new AStar(graph);

      gui.getContentPanel().removeAll();

      // Remove all markers and polygons from the map
      gui.getMapViewer().removeAllMapMarkers();
      gui.getMapViewer().removeAllMapPolygons();

      // Get the addresses from the text areas
      String startAddress = gui.getTextStart().getText();
      String endAddress = gui.getTextEnd().getText();

      // Get the coordinates from the addresses
      // Use the Nominatim API to get the coordinates

      double[] startCoordinates = getCoordinatesFromAddress(startAddress);
      double[] endCoordinates = getCoordinatesFromAddress(endAddress);

      String[] p1 = startAddress.split(",\\s*");
      /*
       * double[] startCoordinates = new double[] {
       * Double.parseDouble(p1[0]),
       * Double.parseDouble(p1[1])
       * };
       */

      String[] p2 = endAddress.split(",\\s*");
      /*
       * double[] endCoordinates = new double[] {
       * Double.parseDouble(p2[0]),
       * Double.parseDouble(p2[1])
       * };
       */

      if (startCoordinates != null && endCoordinates != null) {
        // Create stops with coordinates and addresses
        // ArrayList<Stop> stops = new ArrayList<>();

        try {
          Stop stopA = graph.getClosestStop(startCoordinates[0], startCoordinates[1]);
          Stop stopB = graph.getClosestStop(endCoordinates[0], endCoordinates[1]);

          astar.setDepartStop(stopA);
          astar.setFinishStop(stopB);

          ArrayList<Stop> stops = astar.findPath();

          // Display the path on the map
          gui.getContentPanel().add(gui.displayPath(stops));
          gui.getContentPanel().add(new JPanel());
          gui.getContentPanel().revalidate();
          gui.getContentPanel().repaint();
        } catch (Exception except) {
          System.out.println("No path was found between these two points");
        }
      } else {
        // Show an error message if the coordinates are not found
        JOptionPane.showMessageDialog(this.gui, "Impossible de trouver les coordonnées pour l'une des adresses.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  private void initMenuBar() {
    // Add action listeners to toggle checkmark icons
    JMenuItem metroMenuItem = gui.getMenuItem(0, 0);
    metroMenuItem.addActionListener(e -> {
      gui.toggleCheckmark(metroMenuItem);
      if (gui.isCheckmarkEnabled(metroMenuItem)) {
        gui.viewAllMetro(graph);
      } else {
        gui.cleanMap();
      }
    });
    JMenuItem busMenuItem = gui.getMenuItem(0, 1);
    busMenuItem.addActionListener(e -> {
      gui.toggleCheckmark(busMenuItem);
      if (gui.isCheckmarkEnabled(busMenuItem)) {
        gui.viewAllBus(graph);
      } else {
        gui.cleanMap();
      }
    });

  }

}
