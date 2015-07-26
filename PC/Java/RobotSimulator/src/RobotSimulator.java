import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.concurrent.LinkedBlockingQueue;

public class RobotSimulator
{	
	public static void main(String[] args)
	{
		System.out.println("Program started");
		
		LinkedBlockingQueue<ControllerData> mQueue = new LinkedBlockingQueue<ControllerData>(100);
		
		CoppeliaApiClient client = new CoppeliaApiClient(mQueue);
		
		// Start the network reader 
        ControllerSimulator simulator;
		try {
            simulator = new ControllerSimulator(mQueue);  // Runnable
        } catch (Exception ex) {
            System.out.println("Sorry, this application cannot continue.\nAborting! Details:");
            ex.printStackTrace();
            return;
        }
        Thread simulatorThread = new Thread(simulator);
		
		try {
            System.out.print("Starting up...");
            if (client.init()) {
                System.out.println("Done!");
                simulatorThread.start();
                client.loop();
            } else {
                System.out.println("Failed!");
                simulator.close();
            }

        } catch (Exception ex) {

            simulator.close();
            System.out.println("\nAn error occurred during execution. Stacktrace:");
            ex.printStackTrace();

            try {
                Thread.sleep(25); // Wait a brief period for termination
                simulatorThread.join(); // Get the thread to join
            } catch (InterruptedException e) {
                System.out.println("Interruption acknowledged!");
            }
        } catch (java.lang.UnsatisfiedLinkError libraryNotFound){
            System.out.println("I could not find the 'remoteApiJava.dll' in the system PATH or the current directory.");
            simulator.close();
            try {
                Thread.sleep(25); // Wait a brief period for termination
                simulatorThread.join(); // Get the thread to join
            } catch (InterruptedException e) {
                e.printStackTrace();
        }
    }
        System.out.print("Cleaning up...");
		simulator.requestTerminate();
        simulator.close();
        try {
            simulatorThread.join(300);
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
        System.out.println("Done!");
        System.out.println("Program ended");
	}
}
			
