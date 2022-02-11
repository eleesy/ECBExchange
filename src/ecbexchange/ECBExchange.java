/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecbexchange;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Eleesy
 * 
 * ECBExchange 
 * 
 * This tool olny an assignment for querying 
 * European Central Bank Euro reference daily exchange rate
 * This application user interface use Hungarian language.
 * 
 * Data sources:
 * 
 *   1. eurofxref-daily.xml local file
 * 
 *   2. Web access for the following link
 *      https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
 * 
 * Used  techniques:
 * 
 *   JavaFX, MVC, XML
 * 
 */
public class ECBExchange extends Application {
  public static final String DATA_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
  public static final String DATA_FILENAME = "eurofxref-daily.xml";
  public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
  private UpdateCheckService service;
  private MainFormController controller; 
  
  
    @Override
    public void start(Stage stage) throws Exception {

        //Load MainForm FXML resource / MainForm FXML betöltése
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainForm.fxml"));
        Parent root = loader.load();

        controller = loader.<MainFormController>getController();

        //Rates autoupdate / Árfolyamok automatikus frissítése
        service = new UpdateCheckService();
        service.setPeriod(Duration.seconds(60));    //1 minute / 1 perc
        service.setOnSucceeded(e -> {
          if (service.getValue()) {
                controller.onUpdateAction();
          } 
        });

        Scene scene = new Scene(root);
        stage.setTitle("Devizaváltó");
        stage.setWidth(336);
        stage.setHeight(213);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        service.start();
    }
    
    /** AutoUpdate task / Automatikus frissítését kezdeményező taszk */
    private static class UpdateCheckService extends ScheduledService<Boolean> {

      @Override
      protected Task<Boolean> createTask() {
        return new Task<Boolean>() {

          @Override
          protected Boolean call() throws Exception {
               updateMessage("Checking for updates...");
               return true;
          }

        };
      }
    }        
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
