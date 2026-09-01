package grug.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The main GUI for the Grug chatbot.
 */
public class GrugApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GrugApp.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap;
            ap = fxmlLoader.load();
            Scene scene = new Scene(ap);
            stage.setScene(scene);

            stage.setMinHeight(220);
            stage.setMinWidth(417);

            MainWindow mainWindowController = fxmlLoader.<MainWindow>getController();
            mainWindowController.initialiseGrugBackend();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
