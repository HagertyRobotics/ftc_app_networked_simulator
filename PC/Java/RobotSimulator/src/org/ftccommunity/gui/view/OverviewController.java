package org.ftccommunity.gui.view;

import org.ftccommunity.gui.MainApp;
import org.ftccommunity.simulator.RobotSimulator;
import org.ftccommunity.simulator.modules.BrickSimulator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

public class OverviewController {
    @FXML
    private TableView<BrickSimulator> brickTable;
    @FXML
    private TableColumn<BrickSimulator, String> brickNameColumn;
    @FXML
    private TableColumn<BrickSimulator, String> brickAliasColumn;

    @FXML
    private Label brickNameLabel;
    @FXML
    private Label brickPortLabel;
    @FXML
    private Label brickSerialLabel;
    @FXML
    private Pane detailsPane;

    // Reference to the main application.
    private MainApp mainApp;
    private RobotSimulator simulator;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the brick table with the two columns.
        //controllerNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
        //noinspection Convert2Diamond
    	// the "type" is tied to getType() in BrickSimulator
        brickNameColumn.setCellValueFactory(new PropertyValueFactory<BrickSimulator,String>("type"));
        brickAliasColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // Show brick details in the Details window.
        showBrickDetails(null);

        // Listen for selection changes and show the brick details when changed.
        brickTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBrickDetails(newValue));

        simulator = new RobotSimulator();
    }



    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the table
        brickTable.setItems(mainApp.getBrickData());
    }

    /**
     * Fills all text fields to show details about the brick.
     * If the specified brick is null, all text fields are cleared.
     *
     * @param brick or null
     */
    private void showBrickDetails(BrickSimulator brick) {
        if (brick != null) {
            // Fill the labels with info from the brick object.
            brickNameLabel.setText(brick.getName());
            brickPortLabel.setText(brick.getPort().toString());
            brickSerialLabel.setText(brick.getSerial());
            detailsPane.getChildren().clear();
            brick.populateDetailsPane(detailsPane);
        } else {
            // Brick is null, remove all the text.
            brickNameLabel.setText("");
            brickPortLabel.setText("");
            brickSerialLabel.setText("");
        }
    }

    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeleteBrick() {
        int selectedIndex = brickTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            brickTable.getItems().remove(selectedIndex);
        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Controller Selected");
            alert.setContentText("Please select a controller in the table.");

            alert.showAndWait();
        }
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new brick.
     */
    @FXML
    private void handleNewBrick() {
        BrickSimulator[] tempBrick = new BrickSimulator[1];
        boolean okClicked = mainApp.showBrickNewDialog(tempBrick);
        if (okClicked) {
            mainApp.getBrickData().add(tempBrick[0]);
        }
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected brick.
     */
    @FXML
    private void handleEditBrick() {
        BrickSimulator selectedBrick = brickTable.getSelectionModel().getSelectedItem();
        if (selectedBrick != null) {
            boolean okClicked = mainApp.showBrickEditDialog(selectedBrick);
            if (okClicked) {
                showBrickDetails(selectedBrick);
            }
        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(MainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Controller Selected");
            alert.setContentText("Please select a controller in the table.");

            alert.showAndWait();
        }
    }

    /**
     * Called when the user clicks the Start Simulator button.
     */
    @FXML
    private void handleStartSimulatorButton() {
    	if (!RobotSimulator.simulatorStarted())
    		RobotSimulator.startSimulator(mainApp);
    }

    /**
     * Called when the user clicks the Start Visualizer button.
     */
    @FXML
    private void handleStartVisualizerButton() {
    	if (!RobotSimulator.visualizerStarted())
    		RobotSimulator.startVisualizer(mainApp);
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected brick.
     */
    @FXML
    private void handleStartDebugButton() {
            mainApp.showBrickDebugWindow();
    }


}