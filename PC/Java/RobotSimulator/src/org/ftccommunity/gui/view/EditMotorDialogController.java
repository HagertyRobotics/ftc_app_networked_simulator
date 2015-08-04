package org.ftccommunity.gui.view;

import org.ftccommunity.simulator.modules.BrickSimulator;
import org.ftccommunity.simulator.modules.LegacyBrickSimulator;
import org.ftccommunity.simulator.modules.MotorBrickSimulator;
import org.ftccommunity.simulator.modules.devices.DeviceType;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Dialog to edit details of a Motor Controller.
 *
 * @author Hagerty High
 */
public class EditMotorDialogController extends EditDialogController {

    @FXML
    private TextField brickNameField;
    @FXML
    private TextField brickIPAddressField;
    @FXML
    private TextField brickPortField;
    @FXML
    private TextField brickSerialField;

    @FXML
    private GridPane portGrid;

    private TextField motorPortName;

    private Stage dialogStage;
    private BrickSimulator brick;
    private boolean okClicked = false;

    public EditMotorDialogController() {
        motorPortName = new TextField();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    		motorPortName = new TextField();
    		portGrid.add(motorPortName, 2, 0);

    		Label legacyLabel = new Label();
    		legacyLabel.setText("Port " + 0);
    		portGrid.add(legacyLabel, 0, 0);
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
    public void setBrick(BrickSimulator brick) {
        this.brick = brick;
    }

    /**
     * Sets the brick to be edited.
     *
     */
    public void fillFieldsWithCurrentValues() {
        brickNameField.setText(brick.getName());
        brickPortField.setText(brick.getPort().toString());
        brickSerialField.setText(brick.getSerial());

        MotorBrickSimulator mb = (MotorBrickSimulator)brick;

        //motorPortName.setText(mb.getPortName());
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (super.isInputValid()) {
            brick.setName(brickNameField.getText());
            brick.setPort(Integer.parseInt(brickPortField.getText()));
            brick.setSerial(brickSerialField.getText());

            MotorBrickSimulator mb = (MotorBrickSimulator)brick;

            //mb.setPortName(motorPortName.getText());

            mb.fixupUnMarshaling();

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


}