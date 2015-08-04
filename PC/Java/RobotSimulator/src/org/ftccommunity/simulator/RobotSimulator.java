package org.ftccommunity.simulator;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.ftccommunity.simulator.modules.BrickSimulator;

public class RobotSimulator  {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static BrickListGenerator gBrickListGenerator;
    private static CoppeliaApiClient gCoppeliaApiClient;
    private static volatile boolean gThreadsAreRunning = true;
    private static LinkedList<Thread> threadLinkedList = new LinkedList<>();
    private static int gPhonePort;
    private static InetAddress gPhoneIPAddress;

    private static boolean simulatorStarted = false;
    private static boolean visualizerStarted = false;

    static public void startSimulator(org.ftccommunity.gui.MainApp mainApp) {
    	simulatorStarted = true;

		// Start the module info server
    	System.out.println("Starting Module Lister...");
        gBrickListGenerator = new BrickListGenerator(mainApp);  // Runnable
        Thread moduleListerThread = new Thread(gBrickListGenerator,"");
        moduleListerThread.start();

        
        // Read the current list of modules from the GUI MainApp class
        // Start the individual threads for each module
        List<BrickSimulator> brickList = mainApp.getBrickData();
        for (BrickSimulator temp : brickList) {
        	Thread t = new Thread(temp,temp.getName());  // Make a thread from the object and also set the process name
        	t.start();
            threadLinkedList.add(t);
            System.out.println("Starting: " + temp.getName() + "  \"" + temp.getName() + "\"");
		}
    }

    static public boolean simulatorStarted() {
    	return simulatorStarted;
    }

    static public void startVisualizer(org.ftccommunity.gui.MainApp mainApp) {

    	visualizerStarted = true;

		// Start the module info server
    	System.out.println("Starting Visualizer...");
    	gCoppeliaApiClient = new CoppeliaApiClient(mainApp); // Runnable
        Thread coppeliaThread = new Thread(gCoppeliaApiClient);
        if (gCoppeliaApiClient.init()) {
    		coppeliaThread.start();
    	} else {
    		System.out.println("Initialization of Visualizer failed");
    	}
    }

    static public boolean visualizerStarted() {
    	return visualizerStarted;
    }

    public static boolean isgThreadsAreRunning() {
        return gThreadsAreRunning;
    }

    public static void requestTermination() {
        gThreadsAreRunning = false;
        try {
            Thread.currentThread().wait(50);
        } catch (InterruptedException ex) {
            ex.toString(); // Do nothing
        }
        threadLinkedList.forEach(Thread::interrupt);
        Thread.currentThread().interrupt();
    }

    public static int getPhonePort() {
        return gPhonePort;
    }

    public static InetAddress getPhoneIPAddress() {
        return gPhoneIPAddress;
    }

    public static void GetPhoneConnectionDetails(int phonePort, InetAddress phoneIpAddress) {
        gPhonePort = phonePort;
        gPhoneIPAddress = phoneIpAddress;
    }
}
