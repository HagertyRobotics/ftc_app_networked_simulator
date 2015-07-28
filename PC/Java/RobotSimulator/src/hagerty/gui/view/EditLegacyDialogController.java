package hagerty.gui.view;

import hagerty.simulator.legacy.data.SimDataType;
import hagerty.simulator.modules.BrickSimulator;
import hagerty.simulator.modules.LegacyBrickSimulator;
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
public class EditLegacyDialogController extends EditDialogController {

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

    private ChoiceBox[] legacyChoiceBoxes;
    private TextField[] legacyPortNames;

    private Stage dialogStage;
    private BrickSimulator brick;
    private boolean okClicked = false;

    public EditLegacyDialogController() {
        legacyChoiceBoxes = new ChoiceBox[6];
        legacyPortNames = new TextField[6];
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	for (int i=0;i<6;i++) {
            legacyChoiceBoxes[i] = new ChoiceBox<>(FXCollections.observableArrayList(
                    SimDataType.NONE,
                    SimDataType.LEGACY_MOTOR,
                    SimDataType.LEGACY_LIGHT,
                    SimDataType.LEGACY_TOUCH));

    		legacyChoiceBoxes[i].getSelectionModel().selectFirst();
    		portGrid.add(legacyChoiceBoxes[i], 1, i);

    		legacyPortNames[i] = new TextField();
    		portGrid.add(legacyPortNames[i], 2, i);

    		Label legacyLabel = new Label();
    		legacyLabel.setText("Port " + i);
    		portGrid.add(legacyLabel, 0, i);

    	}
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
        brickNameField.setText(brick.getAlias());
        brickPortField.setText(brick.getPort().toString());
        brickSerialField.setText(brick.getSerial());

        LegacyBrickSimulator lb = (LegacyBrickSimulator)brick;

        for (int i=0;i<6;i++) {
        	legacyChoiceBoxes[i].getSelectionModel().select(
        			lb.getPortType()[i]);
        	legacyPortNames[i].setText(lb.getPortName()[i]);
    	}
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (super.isInputValid()) {
            brick.setAlias(brickNameField.getText());
            brick.setPort(Integer.parseInt(brickPortField.getText()));
            brick.setSerial(brickSerialField.getText());

            LegacyBrickSimulator lb = (LegacyBrickSimulator)brick;

            for (int i=0;i<6;i++) {
            	lb.getPortType()[i] = (SimDataType)legacyChoiceBoxes[i].getSelectionModel().getSelectedItem();
            	lb.getPortName()[i] = legacyPortNames[i].getText();
        	}

            lb.fixupUnMarshaling();

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