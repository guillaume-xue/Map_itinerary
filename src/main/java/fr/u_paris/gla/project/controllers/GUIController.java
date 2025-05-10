package fr.u_paris.gla.project.controllers;

import java.time.LocalTime;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.MutablePair;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.u_paris.gla.project.astar.AStar;
import fr.u_paris.gla.project.astar.CostFunction;
import fr.u_paris.gla.project.astar.CostFunctionFactory;
import fr.u_paris.gla.project.astar.SegmentItineraire;
import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.idfm.IDFMNetworkExtractor;
import fr.u_paris.gla.project.utils.CSVExtractor;
import fr.u_paris.gla.project.utils.Pair;
import fr.u_paris.gla.project.utils.TransportTypes;
import fr.u_paris.gla.project.views.Gui;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GUIController {

  private Gui gui;
  private Graph graph;

  private boolean areStopsConnected = false;

  /**
   * Constructor for GUIController.
   * Initializes the GUI and sets up event listeners.
   */
  public GUIController(String[] args, Boolean isCSVCreate) {
    SwingUtilities.invokeLater(() -> {

      if (isCSVCreate) {
        IDFMNetworkExtractor.parse(args);
      }
      // init Graph class
      this.graph = CSVExtractor.makeObjectsFromCSV(args);
      if (graph == null)
        System.exit(0);

      // Create the main window
      this.gui = new Gui();
      // init Controllers
      new KeyboardController(gui.getTextStart(), gui.getTextEnd(), gui.getResearchButton());
      new KeyboardController(gui.getnumLine());
      new MouseController(gui.getMapViewer(), gui.getTextStart(), gui.getTextEnd(), this.graph, this.gui);
      // init Listenners
      initFocusListenerToTextArea();
      initActionListenner();
      initActionListennerCheckBox();
      // init Menu bar
      initMenuBar();
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
        JTextArea viewLineNumLine = gui.getnumLine();
        if (textStartArea.getText().equals("De")) {
          textStartArea.setText("");
        }
        if (textEndArea.getText().equals("")) {
          textEndArea.setText("À");
        }
        if (viewLineNumLine.getText().equals("")) {
          viewLineNumLine.setText("Line number");
        }
      }
    });

    gui.getTextEnd().addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        JTextArea textStartArea = gui.getTextStart();
        JTextArea textEndArea = gui.getTextEnd();
        JTextArea viewLineNumLine = gui.getnumLine();
        if (textEndArea.getText().equals("À")) {
          textEndArea.setText("");
        }
        if (textStartArea.getText().equals("")) {
          textStartArea.setText("De");
        }
        if (viewLineNumLine.getText().equals("")) {
          viewLineNumLine.setText("Line number");
        }
      }
    });

    gui.getnumLine().addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        JTextArea viewLineNumLine = gui.getnumLine();
        JTextArea textStartArea = gui.getTextStart();
        JTextArea textEndArea = gui.getTextEnd();
        if (viewLineNumLine.getText().equals("Line number")) {
          viewLineNumLine.setText("");
        }
        if (textStartArea.getText().equals("")) {
          textStartArea.setText("De");
        }
        if (textEndArea.getText().equals("")) {
          textEndArea.setText("À");
        }
      }
    });
  }

  private void initActionListennerCheckBox() {
    gui.getDistCheckBox().addActionListener(e -> {
      if (gui.getDistCheckBox().isSelected()) {
        gui.getTimeCheckBox().setSelected(false);
      } else {
        gui.getTimeCheckBox().setSelected(true);
      }
    });
    gui.getTimeCheckBox().addActionListener(e -> {
      if (gui.getTimeCheckBox().isSelected()) {
        gui.getDistCheckBox().setSelected(false);
      } else {
        gui.getDistCheckBox().setSelected(true);
      }
    });
  }

  /**
   * Initializes the action listener for the research button.
   * When clicked, it retrieves coordinates from the text areas and displays the
   * path on the map.
   */
  private void initActionListenner() {
    gui.getViewLineButton().addActionListener(e -> {
      TransportTypes type = TransportTypes.valueOf(gui.getLineTypeDropdown().getSelectedItem().toString());

      if (gui.isShowAllLinesSelected()){
        gui.displayTransportType(graph.getListOfLines(), type);
      } else {
        if ( gui.getnumLine().getText().equals("N° ligne") || gui.getnumLine().getText().equals("") ){
          JOptionPane.showMessageDialog(this.gui, "Veuillez entrer un numéro de ligne.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
          return;
        }

        String lineNumber = gui.getnumLine().getText();
        gui.displayLine(graph.getListOfLines(), type, lineNumber);
      }
    });

    gui.getResearchButton().addActionListener(e -> {
      if (gui.getTextStart().getText().equals("De") || gui.getTextEnd().getText().equals("À")) {
        JOptionPane.showMessageDialog(this.gui, "Veuillez entrer une adresse de départ et d'arrivée.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
        return;
      } else if (gui.getTextStart().getText().equals("De")) {
        JOptionPane.showMessageDialog(this.gui, "Veuillez entrer une adresse de départ.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
        return;
      } else if (gui.getTextEnd().getText().equals("À")) {
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
      } else if (gui.getComboBoxHours().getSelectedItem() == "Now"
          && gui.getComboBoxMinutes().getSelectedItem() != "Now") {
        JOptionPane.showMessageDialog(this.gui, "Veuillez choisir l'heure.", "Erreur", JOptionPane.ERROR_MESSAGE);
        return;
      } else if (gui.getComboBoxHours().getSelectedItem() != "Now"
          && gui.getComboBoxMinutes().getSelectedItem() == "Now") {
        JOptionPane.showMessageDialog(this.gui, "Veuillez choisir les minutes.", "Erreur", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Choose the cost function based on the selected checkbox
      CostFunction costFunction;
      if (gui.getDistCheckBox().isSelected()) {
        costFunction = CostFunctionFactory.getCostFunction(CostFunctionFactory.Mode.DISTANCE);
      } else {
        costFunction = CostFunctionFactory.getCostFunction(CostFunctionFactory.Mode.DURATION);
      }
      AStar astar = new AStar(costFunction);

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

          // Create and get Start and Finish Stops
          MutablePair<Stop, Stop> startFinish = graph.addStartAndFinish(startAddress, endAddress, startCoordinates[0], startCoordinates[1],
              endCoordinates[0], endCoordinates[1]);

          LocalTime heureDepart;
          if (gui.getComboBoxHours().getSelectedItem().toString().equals("Now") &&
              gui.getComboBoxMinutes().getSelectedItem().toString().equals("Now")) {
            heureDepart = LocalTime.now();
          } else {
            heureDepart = LocalTime.of(Integer.parseInt(gui.getComboBoxHours().getSelectedItem().toString()),
                Integer.parseInt(gui.getComboBoxMinutes().getSelectedItem().toString()));
          }

          System.out.println("Recherche de trajet en cours...");
          // pour le nouveau format
          ArrayList<SegmentItineraire> itinerary = astar.findShortestPath(startFinish.getLeft(), startFinish.getRight(),
              heureDepart);

          printItinerary(itinerary);

          // Display the path on the map
          gui.getTextItineraryPanel().add(gui.displayTextItinerary(itinerary));

          
          //Scroll the JScrollPane used to display the textual itinerary to the top

          gui.getContentPanel().add(gui.displayPath(itinerary));
          gui.getContentPanel().add(new JPanel());
          

          gui.getTextItineraryPanel().revalidate();
          gui.getTextItineraryPanel().repaint();

          gui.getContentPane().revalidate();
          gui.getContentPane().repaint();

        } catch (Exception except) {
          // Show an error message if the path is not found
          except.printStackTrace();
          JOptionPane.showMessageDialog(this.gui, "Pas de chemins trouvés entre ces deux points.",
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

  /**
   * Debug function to print out the stops and their corresponding departure times.
   *
   * @param      stopsAndTimes  The stops and corresponding times
   */
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


  /**
   * Debug function to print an itinerary in the terminal.
   *
   * @param      itinerary  The itinerary
   */
  public void printItinerary(ArrayList<SegmentItineraire> itinerary) {
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
    JMenuItem networkMenuItem = gui.getMenuItem(0, 0);
    networkMenuItem.addActionListener(e -> {
      gui.toggleCheckmark(networkMenuItem);
      gui.toggleFloatingWindow(gui.isCheckmarkEnabled(networkMenuItem));
    });

    JMenuItem connectStopsMenuItem = gui.getMenuItem(1,0);
    connectStopsMenuItem.addActionListener(e -> {
      if ( !areStopsConnected ){
        int confirm = JOptionPane.showConfirmDialog(
        this.gui, 
        "Attention option expérimentale. Connecter les stations entre-elles (100 mètres par défaut) pour les trajets à pied\n" + 
        " prend du temps et rend l'algorithme de recherche plus lent. Si vous confirmez l'action\n" + 
        " garder un oeil sur le terminal pour savoir quand les connections ont fini d'être créées.\n"
        ,"Confirmation requise"
        , JOptionPane.YES_NO_OPTION);
        if ( confirm == 0 ){
          graph.connectStopsByWalkingV2();
          this.areStopsConnected = true;
        } 
      } else {
        JOptionPane.showMessageDialog(this.gui, "Stations déjà connectées. Relancer l'application pour enlever cette option.",
            "Info",
            JOptionPane.ERROR_MESSAGE);
      }
    }); 
  }

  /**
   * Launches the gui
   */
  public void launch() {
    gui.launch();
  }

}
