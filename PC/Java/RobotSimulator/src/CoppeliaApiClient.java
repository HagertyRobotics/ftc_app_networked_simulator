import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;

public class CoppeliaApiClient {

	
	long mStartTime;
	IntWA mObjectHandles;
	IntW mLeftMotor;
	IntW mRightMotor;
	int mClientID;
	remoteApi mVrep;
	
	public boolean init() {
		
		mVrep = new remoteApi();
		mVrep.simxFinish(-1); // just in case, close all opened connections
		mClientID = mVrep.simxStart("127.0.0.1",5000,true,true,5000,5);
		if (mClientID!=-1)
		{
			System.out.println("Connected to remote API server");	

			mObjectHandles = new IntWA(1);
			mLeftMotor = new IntW(1);
			mRightMotor = new IntW(1);
			
			int ret=mVrep.simxGetObjects(mClientID,remoteApi.sim_handle_all,mObjectHandles,remoteApi.simx_opmode_oneshot_wait);
			if (ret==remoteApi.simx_return_ok)
				System.out.format("Number of objects in the scene: %d\n",mObjectHandles.getArray().length);
			else
				System.out.format("Remote API function call returned with error code: %d\n",ret);
				
			try
			{
				Thread.sleep(2000);
			}
			catch(InterruptedException ex)
			{
				Thread.currentThread().interrupt();
			}
	
			mVrep.simxGetObjectHandle(mClientID,"remoteApiControlledBubbleRobLeftMotor",mLeftMotor,remoteApi.simx_opmode_oneshot_wait);
			mVrep.simxGetObjectHandle(mClientID,"remoteApiControlledBubbleRobRightMotor",mRightMotor,remoteApi.simx_opmode_oneshot_wait);
			System.out.println("Left = " + mLeftMotor.getValue() + " Right = " + mRightMotor.getValue());
			
			mStartTime=System.currentTimeMillis();
			return true;
			
		} else {
			System.out.println("Failed connecting to remote API server");
			return false;
		}
		
	}
	
	public void loop()
	{
		boolean done=false;
		
		while (!done)
		{
			mVrep.simxSetJointTargetVelocity(mClientID,mLeftMotor.getValue(),(float)3.14,remoteApi.simx_opmode_oneshot);
			mVrep.simxSetJointTargetVelocity(mClientID,mRightMotor.getValue(),(float)3.14,remoteApi.simx_opmode_oneshot);
		}
		
		// Before closing the connection to V-REP, make sure that the last command sent out had time to arrive. You can guarantee this with (for example):
		IntW pingTime = new IntW(0);
		mVrep.simxGetPingTime(mClientID,pingTime);

		// Now close the connection to V-REP:	
		mVrep.simxFinish(mClientID);
		
		System.out.println("Program ended");
	}
}

