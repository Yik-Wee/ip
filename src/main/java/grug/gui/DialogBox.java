package grug.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * Represents a dialog box consisting of a label containing text from the
 * speaker.
 */
public class DialogBox extends HBox {
    private static final Image USER_IMAGE = new Image("/images/user.jpg");
    private static final Image GRUG_IMAGE = new Image("/images/grug.png");

    @FXML
    private Label dialog;

    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image image) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(image);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on
     * the right.
     */
    private void flip() {
        setAlignment(Pos.TOP_LEFT);
        this.dialog.getStyleClass().add("reply-label");

        // flip the children
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(tmp);
        getChildren().setAll(tmp);
    }

    /**
     * Creates a {@link DialogBox} representing the user's dialog, on the right of
     * the screen.
     *
     * @param text The user's dialog text.
     * @return The {@link DialogBox} containing the user's dialog text.
     */
    public static DialogBox createFromUserDialog(String text) {
        return new DialogBox(text, USER_IMAGE);
    }

    /**
     * Creates a {@link DialogBox} representing the chatbot grug's dialog, on the
     * left of the screen.
     *
     * @param response Grug's dialog text/response.
     * @return The {@link DialogBox} containing Grug's dialog text.
     */
    public static DialogBox createFromGrugDialog(String response) {
        var db = new DialogBox(response, GRUG_IMAGE);
        db.dialog.setFont(Font.font("Monospaced"));
        db.flip();
        return db;
    }
}
