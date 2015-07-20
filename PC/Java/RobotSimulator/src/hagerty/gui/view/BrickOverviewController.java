package hagerty.gui.view;

import hagerty.gui.MainApp;
import hagerty.gui.model.Brick;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class BrickOverviewController {
    @FXML
    private TableView<Brick> brickTable;
    @FXML
    private TableColumn<Brick, String> brickNameColumn;
    @FXML
    private TableColumn<Brick, String> brickAliasColumn;

    @FXML
    private Label brickNameLabel;
    @FXML
    private Label brickIPAddressLabel;
    @FXML
    private Label brickPortLabel;
    @FXML
    private Label brickSerialLabel;



    // Reference to the main application.
    private MainApp mainApp;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public BrickOverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the brick table with the two columns.
        //controllerNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
        brickNameColumn.setCellValueFactory(new PropertyValueFactory<Brick,String>("name"));
        brickAliasColumn.setCellValueFactory(cellData -> cellData.getValue().aliasProperty());

        // Clear brick details.
        showBrickDetails(null);

        // Listen for selection changes and show the brick details when changed.
        brickTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBrickDetails(newValue));
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
    private void showBrickDetails(Brick brick) {
        if (brick != null) {
            // Fill the labels with info from the brick object.
            brickNameLabel.setText(brick.getAlias());
            brickPortLabel.setText(brick.getPort().toString());
            brickSerialLabel.setText(brick.getSerial());
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
        Brick[] tempBrick = new Brick[1];
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
        Brick selectedBrick = brickTable.getSelectionModel().getSelectedItem();
        if (selectedBrick != null) {
            boolean okClicked = mainApp.showBrickEditDialog(selectedBrick);
            if (okClicked) {
                showBrickDetails(selectedBrick);
            }

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
}