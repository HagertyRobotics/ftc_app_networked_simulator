package hagerty.gui;

import hagerty.gui.view.*;
import hagerty.simulator.RobotSimulator;
import hagerty.simulator.modules.BrickSimulator;
import hagerty.simulator.modules.LegacyBrickSimulator;
import hagerty.utils.Utils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

public class MainApp extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;

    /**
     * The data as an observable list of Controllers.
     */
    private ObservableList<BrickSimulator> brickList;

    public MainApp() {
        brickList = FXCollections.observableArrayList();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Returns the data as an observable list of Controller Simulators.
     *
     * @return
     */
    public ObservableList<BrickSimulator> getBrickData() {
        return brickList;
    }

    @Override
    public void start(Stage primaryStage) {
        Runtime.getRuntime().addShutdownHook(new Thread("shutdown thread") {
            public void run() {
                System.out.println("***** Threads Exiting *****");
                RobotSimulator.isgThreadsAreRunning();
            }
        });

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Simulator App");

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
            loader.setLocation(this.getClass().getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

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
        File file = Utils.getBrickFilePath(Preferences.userNodeForPackage(this.getClass()));
        if (file != null) {
            try {
                Utils.loadBrickDataFromFile(file, brickList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Shows the controller overview inside the root layout.
     */
    private void showBrickOverview() {
        try {
            // Load controller overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/Overview.fxml"));
            AnchorPane controllerOverview = loader.load();

            // Set controller overview into the center of root layout.
            rootLayout.setCenter(controllerOverview);

            // Give the controller access to the main app.
            OverviewController controller = loader.getController();
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
    public boolean showBrickEditDialog(BrickSimulator brick) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            if (brick instanceof LegacyBrickSimulator)
                loader.setLocation(this.getClass().getResource("view/EditLegacyDialog.fxml"));
            else
                loader.setLocation(this.getClass().getResource("view/EditDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Controller");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Give the Brick we are editing to the controller.
            EditDialogController c = loader.getController();
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
     * Opens a dialog to edit details for the specified brick. If the user
     * clicks OK, the changes are saved into the provided brick object and true
     * is returned.
     *
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showBrickDebugWindow() {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/DebugWindow.fxml"));
            AnchorPane page = loader.load();

            VBox vbox = new VBox();
            vbox.setPadding(new Insets(10));
            vbox.setSpacing(8);

            // Read the current list of modules from the GUI MainApp class
            List<BrickSimulator> brickList = this.getBrickData();

            for (BrickSimulator currentBrick : brickList) {
                currentBrick.setupDebugGuiVbox(vbox);
            }
            page.getChildren().add(vbox);

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Debug");
            dialogStage.initModality(Modality.NONE);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Call some routines in the controller
            DebugWindowController c = loader.getController();
            c.setDialogStage(dialogStage);
            c.setMainApp(this);

            // Set the dialog icon.
            dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Opens a dialog to choose the type of brick we are creating
     *
     * @param brick the controller object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showBrickNewDialog(BrickSimulator[] brick) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/NewDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Controller");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Brick into the controller.
            NewDialogController c = loader.getController();
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
     * Returns the main stage.
     *
     * @return the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}