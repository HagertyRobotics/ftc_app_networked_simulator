import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RobotSimulator extends Application {

	static ControllerSimulator mSimulator;
	static CoppeliaApiClient mApiClient;

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (mApiClient.init()) {
                	mApiClient.setRun(true);
                }
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {

        System.out.println("Program started");

		LinkedBlockingQueue<ControllerData> mQueue = new LinkedBlockingQueue<ControllerData>(100);

		// Start the network reader
        mApiClient = new CoppeliaApiClient(mQueue);  // Runnable
        Thread coppeliaApiClientThread = new Thread(mApiClient,"");
        coppeliaApiClientThread.start();


		// Start the network reader
        mSimulator = new ControllerSimulator(mQueue);  // Runnable
        Thread simulatorThread = new Thread(mSimulator,"");
        simulatorThread.start();

        launch(args);

		System.out.println("Program ended");
    }
}
