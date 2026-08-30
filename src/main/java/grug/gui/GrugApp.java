package grug.gui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import grug.ui.Ui;
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

            MainWindow mainWindowController = fxmlLoader.<MainWindow>getController();

            StringWriter messageOutput = new StringWriter();
            // null since we are reading input directly from the text box
            Ui messageUi = new Ui(null, new PrintWriter(messageOutput));
            Grug grug = new Grug(Grug.DEFAULT_STORAGE_FILE, messageUi);
            mainWindowController.setGrug(grug);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
