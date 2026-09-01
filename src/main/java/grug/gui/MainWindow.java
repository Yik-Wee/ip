package grug.gui;

import grug.ui.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Grug grug;

    /**
     * Initialises the Grug backend API, loads the saved tasks and greets the user.
     */
    public void initialiseGrugBackend() {
        Ui ui = new GraphicalUi(this.userInput, this.dialogContainer);
        this.grug = new Grug(Grug.DEFAULT_STORAGE_FILE, ui);

        // show startup dialog and load the tasks
        this.grug.loadTasks();
        this.grug.greet();
    }

    /**
     * Sets the behavior of the {@link #dialogContainer} to scroll to the bottom
     * when its height changes (i.e. when a new dialog is added).
     */
    @FXML
    public void initialize() {
        // scroll to the bottom when vbox height changes
        this.dialogContainer.heightProperty().addListener((observable, oldHeight, newHeight) -> {
            this.scrollPane.setVvalue(this.scrollPane.getVmax());
        });
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing
     * Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        String input = this.grug.readUserInput("").get();

        // only display the user's dialog box if its not blank
        // but don't return since blank could be a valid command
        if (!input.isBlank()) {
            DialogBox userDialogBox = DialogBox.fromUserDialog(input);
            this.dialogContainer.getChildren().add(userDialogBox);
        }
        this.userInput.clear();

        this.grug.handleUserInput(input);
    }
}
