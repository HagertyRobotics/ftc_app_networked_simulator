package org.ftccommunity.gui.view;


import org.ftccommunity.simulator.modules.BrickSimulator;
import org.ftccommunity.simulator.modules.devices.DeviceType;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
public class EditDialogController {

	@FXML
	private Label brickTypeField;
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

    private ChoiceBox[] choiceBoxes;
    private TextField[][] portNames = new TextField[6][2];

    private Stage dialogStage;
    private BrickSimulator brick;
    private boolean okClicked = false;

    public EditDialogController() {
        choiceBoxes = new ChoiceBox[6];
    }

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
    public void setBrick(BrickSimulator brick) {
        this.brick = brick;
    }

    /**
     *
     *
     */
    public void fillFieldsWithCurrentValues() {
    	// Draw fields

    	brickTypeField = new Label("Hello");
    	for (int i=0;i<brick.getNumberOfPorts();i++) {
    		// Leftmost label
    		Label portLabel = new Label();
    		portLabel.setText("Port " + i);
    		portGrid.add(portLabel, 0, i);

    		// Selection box
            choiceBoxes[i] = new ChoiceBox<>(FXCollections.observableArrayList(brick.getDeviceTypeList()));
    		choiceBoxes[i].getSelectionModel().selectFirst();
    		portGrid.add(choiceBoxes[i], 1, i);

    		// Port name input box
    		TextField portName1 = new TextField();
    		portNames[i][0]=portName1;
    		portGrid.add(portName1, 2, i);

    		TextField portName2 = new TextField();
    		portNames[i][1]=portName2;
    		portGrid.add(portName2, 3, i);
    	}

    	// Fill fields
        brickNameField.setText(brick.getName());
        brickPortField.setText(brick.getPort().toString());
        brickSerialField.setText(brick.getSerial());

        for (int i=0;i<brick.getNumberOfPorts();i++) {
        	choiceBoxes[i].getSelectionModel().select(brick.getPortDeviceType(i));
        	List<String> nameList = brick.getPortDevice(i).getPortNames();
        	for (int j=0;j<nameList.size();j++) {
        		portNames[i][j].setText(nameList.get(j));
        	}
    	}
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
            brick.setName(brickNameField.getText());
            brick.setPort(Integer.parseInt(brickPortField.getText()));
            brick.setSerial(brickSerialField.getText());

            for (int i=0;i<brick.getNumberOfPorts();i++) {
            	DeviceType d = (DeviceType)choiceBoxes[i].getSelectionModel().getSelectedItem();
            	brick.setPortDeviceType(i, d);

            	// Save name fields in SimData objects
            	int len = brick.getPortDevice(i).getNumberOfPorts();
            	String[] nameList = new String[len];
            	for (int j=0;j<len;j++) {
            		nameList[j] = portNames[i][j].getText();
            	}

            	brick.getPortDevice(i).setDeviceNames(nameList);
        	}

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
    protected boolean isInputValid() {
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
