package hagerty.simulator;

import java.net.InetAddress;
import java.util.List;

import hagerty.simulator.modules.BrickSimulator;

public class RobotSimulator  {

	static BrickListGenerator gBrickListGenerator;
	static CoppeliaApiClient gCoppeliaApiClient;
	static public volatile boolean gThreadsAreRunning = true;
    static int gPhonePort;
    static InetAddress gPhoneIPAddress;
    
    static boolean simulatorStarted = false;
    static boolean visualizerStarted = false;

    private RobotSimulator() {

    }

    static public void startSimulator(hagerty.gui.MainApp mainApp) {

    	simulatorStarted = true;

		// Start the module info server
    	System.out.println("Starting Module Lister...");
        gBrickListGenerator = new BrickListGenerator(mainApp);  // Runnable
        Thread moduleListerThread = new Thread(gBrickListGenerator,"");
        moduleListerThread.start();

        // Start the individual threads for each module
        // Read the current list of modules from the GUI MainApp class
        List<BrickSimulator> brickList = mainApp.getBrickData();

        for (BrickSimulator temp : brickList) {
        	Thread t = new Thread(temp,temp.getAlias());  // Make a thread from the object and also set the process name
        	t.start();
			System.out.println(temp.getAlias() + " " + temp.getName());
		}

    }

    static public boolean simulatorStarted() {
    	return simulatorStarted;
    }

    static public void startVisualizer(hagerty.gui.MainApp mainApp) {

    	visualizerStarted = true;

		// Start the module info server
    	System.out.println("Starting Visualizer...");
    	gCoppeliaApiClient = new CoppeliaApiClient(mainApp); // Runnable
    	Thread coppeliaThread = new Thread(gCoppeliaApiClient,"");
    	if (gCoppeliaApiClient.init()) {
    		coppeliaThread.start();
    	} else {
    		System.out.println("Initialization of Visualizer failed");
    	}
    }

    static public boolean visualizerStarted() {
    	return visualizerStarted;
    }
}
