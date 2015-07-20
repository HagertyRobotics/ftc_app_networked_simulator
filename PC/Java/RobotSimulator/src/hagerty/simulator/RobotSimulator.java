package hagerty.simulator;

import java.net.InetAddress;

public class RobotSimulator  {

	static ModuleLister gModuleLister;
	static public volatile boolean gThreadsAreRunning = true;
    static int gPhonePort;
    static InetAddress gPhoneIPAddress;

    private RobotSimulator() {

    }

    static public void startSimulator(hagerty.gui.MainApp mainApp) {

		// Start the module info server
        gModuleLister = new ModuleLister(mainApp);  // Runnable
        Thread moduleListerThread = new Thread(gModuleLister,"");
        moduleListerThread.start();

    }
}
