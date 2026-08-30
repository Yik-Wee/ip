package grug.gui;

import grug.command.CommandResult;
import grug.command.GrugCommand;
import grug.command.GrugCommandParserException;
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

    public void setGrug(Grug grug) {
        this.grug = grug;
    }

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing
     * Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        DialogBox userDialogBox = DialogBox.fromUserDialog(input);

        String response;
        try {
            GrugCommand command = Grug.parseInput(input);
            CommandResult result = grug.execute(command);
            response = result.message();
        } catch (GrugCommandParserException e) {
            response = e.getMessage();
        }

        DialogBox botDialogBox = DialogBox.fromGrugDialog(response);

        dialogContainer.getChildren().addAll(userDialogBox, botDialogBox);
        userInput.clear();
    }
}
