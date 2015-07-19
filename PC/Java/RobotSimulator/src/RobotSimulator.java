import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RobotSimulator {

	static ControllerSimulator mSimulator;
	static CoppeliaApiClient mApiClient;

    public static void main(String[] args) {

        System.out.println("Program started");

		LinkedBlockingQueue<ControllerData> mQueue = new LinkedBlockingQueue<ControllerData>(100);

		// Start the network reader

     	mApiClient = new CoppeliaApiClient(mQueue);  // Runnable
     	Thread coppeliaApiClientThread = new Thread(mApiClient,"");
     	coppeliaApiClientThread.start();
		if (mApiClient.init())
         	mApiClient.setRun(true);

		// Start the network reader
        mSimulator = new ControllerSimulator(mQueue);  // Runnable
        Thread simulatorThread = new Thread(mSimulator,"");
        simulatorThread.start();

        while (true) {}

    }
}
