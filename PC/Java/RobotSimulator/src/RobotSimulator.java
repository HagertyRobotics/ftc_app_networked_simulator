import java.util.concurrent.LinkedBlockingQueue;

public class RobotSimulator
{	
	public static void main(String[] args)
	{
		System.out.println("Program started");
		
		LinkedBlockingQueue<ControllerData> mQueue = new LinkedBlockingQueue<ControllerData>(100);
		
		CoppeliaApiClient client = new CoppeliaApiClient(mQueue);
		
		// Start the network reader 
        ControllerSimulator simulator = new ControllerSimulator(mQueue);  // Runnable
        Thread simulatorThread = new Thread(simulator,"");
        simulatorThread.start();
		
		
					
		if (client.init()) {
			client.loop();
		}
		
		System.out.println("Program ended");
	}
}
			
