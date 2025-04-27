package fr.u_paris.gla.project.controllers;

import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.u_paris.gla.project.astar.CostFunction;
import fr.u_paris.gla.project.astar.CostFunctionFactory;
import fr.u_paris.gla.project.astar.AStarBis;
import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.astar.SegmentItineraire;
import fr.u_paris.gla.project.utils.CSVExtractor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import fr.u_paris.gla.project.views.Gui;
import fr.u_paris.gla.project.utils.Pair;
import java.time.LocalTime;

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
      // init Graph class
      String[] args = { "--parse", "mapData.csv", "junctionsData.csv", "Schedules/" };
      this.graph = CSVExtractor.makeObjectsFromCSV(args);
      // init Controllers
      new KeyboardController(gui.getTextStart());
      new KeyboardController(gui.getTextEnd());
      new MouseController(gui.getMapViewer(), gui.getTextStart(), gui.getTextEnd(), this.graph, this.gui);
      // init Listenners
      initFocusListenerToTextArea();
      initActionListenner();
      // init Menu bar
      initMenuBar();
      // Set the visibility to true
      this.gui.launch();
      // Check the internet connection.
      if (!isInternetAvailable()) {
        JOptionPane.showMessageDialog(this.gui,
            "Aucune connexion Internet détectée. Veuillez vérifier votre connexion.",
            "Erreur de connexion",
            JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  /**
   * Checks if there is an active internet connection.
   * 
   * @return true if connected, false otherwise
   */
  private boolean isInternetAvailable() {
    try {
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder()
          .url("http://www.google.com")
          .build();
      try (Response response = client.newCall(request).execute()) {
        return response.isSuccessful();
      }
    } catch (Exception e) {
      return false;
    }
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

      // avec version astar2 où ou peut choisir si on veut le chemin le plus court en
      // duree ou distance
      // faudra appeler qu'un des choix en fonction de ce que l'utilisateur demande
      // CostFunction costFunction =
      // CostFunctionFactory.getCostFunction(CostFunctionFactory.Mode.DISTANCE);
      CostFunction costFunction = CostFunctionFactory.getCostFunction(CostFunctionFactory.Mode.DURATION);
      AStarBis astar = new AStarBis(costFunction);

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

      if (startCoordinates != null && endCoordinates != null) {
        try {
          Stop stopA = graph.getClosestStop(startCoordinates[0], startCoordinates[1]);
          Stop stopB = graph.getClosestStop(endCoordinates[0], endCoordinates[1]);

          // v2 astar
          LocalTime heureDepart = LocalTime.of(19, 38);
          // 3 prochaines lignes à jeter qd c'est adapté au nv format
          System.out.println("recherche du chemin avec algo obsolete");
          ArrayList<Pair<Stop, LocalTime>> stopsAndTimes = astar.findShortestPath(stopA, stopB, heureDepart);
          printPathWithTimes(stopsAndTimes);

          // pour le nouveau format
          System.out.println("recherche du chemin avec nouvel algo");
          ArrayList<SegmentItineraire> itinerary = astar.findShortestPath2(stopA, stopB, heureDepart);
          displayItinerary(itinerary);

          // Display the path on the map
          gui.getContentPanel().add(gui.displayPath(itinerary));
          gui.getContentPanel().add(new JPanel());
          gui.getContentPanel().revalidate();
          gui.getContentPanel().repaint();
        } catch (Exception except) {
          // Show an error message if the path is not found
          except.printStackTrace();
          JOptionPane.showMessageDialog(this.gui, "No path was found between these two points",
              "Erreur",
              JOptionPane.ERROR_MESSAGE);
        }
      } else {
        // Show an error message if the coordinates are not found
        JOptionPane.showMessageDialog(this.gui, "Impossible de trouver les coordonnées pour l'une des adresses.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  // TBD
  // la fonction c'est juste en attendant qu'on ait les affichages avec horaire ds
  // l'app et c'est pour debugger
  public static void printPathWithTimes(ArrayList<Pair<Stop, LocalTime>> stopsAndTimes) {
    if (stopsAndTimes == null || stopsAndTimes.isEmpty()) {
      System.out.println("Aucun chemin trouvé.");
      return;
    }
    System.out.println("Chemin trouvé :");
    for (Pair<Stop, LocalTime> pair : stopsAndTimes) {
      Stop stop = pair.getKey();
      LocalTime time = pair.getValue();
      System.out.println(" -> " + stop.getNameOfAssociatedStation() + " à " + time);
    }
  }

  // TBD
  // pr debugger
  public void displayItinerary(ArrayList<SegmentItineraire> itinerary) {
    if (itinerary == null || itinerary.isEmpty()) {
      System.out.println("L'itinéraire est vide.");
      return;
    }

    System.out.println("Itinéraire :");
    for (SegmentItineraire segment : itinerary) {
      System.out.println(segment);
    }
  }

  /**
   * Initializes the menu bar with action listeners for metro and bus options.
   * Toggles the checkmark icons when selected.
   */
  private void initMenuBar() {
    // Add action listeners to toggle checkmark icons
    JMenuItem busMenuItem = gui.getMenuItem(0, 0);
    busMenuItem.addActionListener(e -> {
      gui.toggleCheckmark(busMenuItem);
      if (gui.isCheckmarkEnabled(busMenuItem)) {
        gui.viewLine(graph, "Bus");
      } else {
        gui.cleanMap();
      }
    });
    JMenuItem metroMenuItem = gui.getMenuItem(0, 1);
    metroMenuItem.addActionListener(e -> {
      gui.toggleCheckmark(metroMenuItem);
      if (gui.isCheckmarkEnabled(metroMenuItem)) {
        gui.viewLine(graph, "Subway");
      } else {
        gui.cleanMap();
      }
    });
  }

}