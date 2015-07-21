package hagerty.gui;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.bind.*;

import hagerty.gui.model.Brick;
import hagerty.gui.model.BrickListWrapper;
import hagerty.gui.model.LegacyBrick;
import hagerty.gui.model.MotorBrick;
import hagerty.gui.model.ServoBrick;
import hagerty.gui.view.BrickEditDialogController;
import hagerty.gui.view.BrickNewDialogController;
import hagerty.gui.view.BrickOverviewController;
import hagerty.gui.view.RootLayoutController;

import hagerty.simulator.RobotSimulator;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    /**
     * The data as an observable list of Controllers.
     */
    private ObservableList<Brick> brickList = FXCollections.observableArrayList();

    /**
     * Constructor
     */
    public MainApp() {

    }

    /**
     * Returns the data as an observable list of Controllers.
     * @return
     */
    public ObservableList<Brick> getBrickData() {
        return brickList;
    }

    @Override
    public void start(Stage primaryStage) {

    	Runtime.getRuntime().addShutdownHook(new Thread("shutdown thread") {
            public void run() {
                System.out.println("***** Threads Exiting *****");
                RobotSimulator.gThreadsAreRunning = false;

            }
        });

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AddressApp");

        // Set the application icon.
        this.primaryStage.getIcons().add(new Image("file:resources/images/robot.png"));

        initRootLayout();

        showBrickOverview();

        
    }

    /**
     * Initializes the root layout and tries to load the last opened
     * controller file.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class
                    .getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Try to load last opened controller file.
        File file = getBrickFilePath();
        if (file != null) {
            loadBrickDataFromFile(file);
        }
    }

    /**
     * Shows the controller overview inside the root layout.
     */
    public void showBrickOverview() {
        try {
            // Load controller overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BrickOverview.fxml"));
            AnchorPane controllerOverview = (AnchorPane) loader.load();

            // Set controller overview into the center of root layout.
            rootLayout.setCenter(controllerOverview);

            // Give the controller access to the main app.
            BrickOverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog to edit details for the specified brick. If the user
     * clicks OK, the changes are saved into the provided brick object and true
     * is returned.
     *
     * @param brick the controller object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showBrickEditDialog(Brick brick) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BrickEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Controller");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Brick into the controller.
            BrickEditDialogController c = loader.getController();
            c.setDialogStage(dialogStage);
            c.setBrick(brick);
            c.fillFieldsWithCurrentValues();

            // Set the dialog icon.
            dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return c.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens a dialog to choose the type of brick we are creating
     *
     * @param brick the controller object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showBrickNewDialog(Brick[] brick) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BrickNewDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Controller");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Brick into the controller.
            BrickNewDialogController c = loader.getController();
            c.setDialogStage(dialogStage);
            c.setBrick(brick);
            c.initChoiceBox();


            // Set the dialog icon.
            dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return c.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * Returns the controller file preference, i.e. the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     *
     * @return
     */
    public File getBrickFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file the file or null to remove the path
     */
    public void setBrickFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Update the stage title.
            primaryStage.setTitle("AddressApp - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Update the stage title.
            primaryStage.setTitle("AddressApp");
        }
    }

    /**
     * Loads controller data from the specified file. The current controller data will
     * be replaced.
     *
     * @param file
     */
    public void loadBrickDataFromFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(BrickListWrapper.class, LegacyBrick.class, MotorBrick.class, ServoBrick.class );
            Unmarshaller um = context.createUnmarshaller();
            try {
            // Reading XML from the file and unmarshalling.
            BrickListWrapper wrapper = (BrickListWrapper) um.unmarshal(file);

            brickList.clear();
            brickList.addAll(wrapper.getBricks());

            // Save the file path to the registry.
            setBrickFilePath(file);

            } catch (UnmarshalException e) {
            	System.err.println("UnmarshalExcemption: ");
            } catch (JAXBException e) {
            	System.err.println("UnmarshalExcemption: ");
            }

        } catch (Exception e) { // catches ANY exception
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("Error");
        	alert.setHeaderText("Could not load data");
        	alert.setContentText("Could not load data from file:\n" + file.getPath());

        	alert.showAndWait();
        }
    }

    /**
     * Saves the current controller data to the specified file.
     *
     * @param file
     */
    public void saveBrickDataToFile(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(BrickListWrapper.class, LegacyBrick.class, MotorBrick.class, ServoBrick.class );
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping our controller data.
            BrickListWrapper wrapper = new BrickListWrapper();
            wrapper.setBricks(brickList);

            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);
            //m.marshal(wrapper, System.out);

            // Save the file path to the registry.
            setBrickFilePath(file);
        } catch (Exception e) { // catches ANY exception
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("Error");
        	alert.setHeaderText("Could not save data");
        	alert.setContentText("Could not save data to file:\n" + file.getPath());

        	alert.showAndWait();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}