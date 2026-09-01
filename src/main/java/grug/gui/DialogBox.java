package grug.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * Represents a dialog box consisting of an ImageView to represent the speaker's
 * face and a label containing text from the speaker.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;

    private DialogBox(String text) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on
     * the right.
     */
    private void flip() {
        setAlignment(Pos.TOP_LEFT);
        this.dialog.getStyleClass().add("reply-label");
    }

    /**
     * Creates a {@link DialogBox} representing the user's dialog, on the right of
     * the screen.
     *
     * @param text The user's dialog text.
     * @return The {@link DialogBox} containing the user's dialog text.
     */
    public static DialogBox fromUserDialog(String text) {
        return new DialogBox(text);
    }

    /**
     * Creates a {@link DialogBox} representing the chatbot grug's dialog, on the
     * left of the screen.
     *
     * @param response Grug's dialog text/response.
     * @return The {@link DialogBox} containing Grug's dialog text.
     */
    public static DialogBox fromGrugDialog(String response) {
        var db = new DialogBox(response);
        db.dialog.setFont(Font.font("Monospaced"));
        db.flip();
        return db;
    }
}
