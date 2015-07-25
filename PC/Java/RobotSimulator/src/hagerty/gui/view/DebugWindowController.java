package hagerty.gui.view;

import hagerty.simulator.modules.BrickSimulator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Dialog to edit details of a Motor Controller.
 *
 * @author Hagerty High
 */
public class DebugWindowController {

    @FXML
    private Label brickDebugField;

    private Stage dialogStage;


    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	startLiveDebug();
    }

    /**
     * Start a thread thats queries the Brick Simulators and displays current values here.
     */
    public void startLiveDebug() {

        Task<Void> task = new Task<Void>() {
      	  @Override
      	  public Void call() throws Exception {
      	    int i = 0;
      	    while (true) {
      	      final int finalI = i;
      	      Platform.runLater(new Runnable() {
      	        @Override
      	        public void run() {
      	          brickDebugField.setText("" + finalI);
      	        }
      	      });
      	      i++;
      	      Thread.sleep(1000);
      	    }
      	  }
      	};
      	Thread th = new Thread(task);
      	th.setDaemon(true);
      	th.start();
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
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

}