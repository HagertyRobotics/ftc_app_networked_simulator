package hagerty.robot.view;

import hagerty.robot.model.Brick;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Dialog to edit details of a Motor Controller.
 *
 * @author Hagerty High
 */
public class BrickEditDialogController {

    @FXML
    private TextField brickNameField;

    @FXML
    private TextField brickIPAddressField;

    @FXML
    private TextField brickPortField;

    private Stage dialogStage;
    private Brick brick;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;

        // Set the dialog icon.
        this.dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));
    }

    /**
     * Sets the brick to be edited.
     *
     * @param brick
     */
    public void setBrick(Brick brick) {
        this.brick = brick;
    }

    /**
     * Sets the brick to be edited.
     *
     * @param brick
     */
    public void fillFieldsWithCurrentValues() {
        brickNameField.setText(brick.getAlias());
        brickIPAddressField.setText(brick.getIPAddress());
        brickPortField.setText(brick.getPort().toString());
    }


    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            brick.setAlias(brickNameField.getText());
            brick.setIPAddress(brickIPAddressField.getText());
            brick.setPort(Integer.parseInt(brickPortField.getText()));

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (brickNameField.getText() == null || brickNameField.getText().length() == 0) {
            errorMessage += "No valid first name!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}