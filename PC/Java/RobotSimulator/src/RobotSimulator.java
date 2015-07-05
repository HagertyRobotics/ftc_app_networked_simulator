import java.util.concurrent.LinkedBlockingQueue;

public class RobotSimulator
{	
	public static void main(String[] args)
	{
		LinkedBlockingQueue<ControllerData> mWriteQueue = new LinkedBlockingQueue<ControllerData>(10);
		
		CoppeliaApiClient client = new CoppeliaApiClient();
		
        ControllerSimulator simulator = new ControllerSimulator(mWriteQueue);  // Runnable
        Thread simulatorThread = new Thread(simulator,"");
        simulatorThread.start();
		
		System.out.println("Program started");
					
		if (client.init()) {
			client.loop();
		}
		
		System.out.println("Program ended");
	}
}
			
