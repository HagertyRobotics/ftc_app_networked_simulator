package org.ftccommunity.simulator;

import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;

import java.util.concurrent.LinkedBlockingQueue;

public class CoppeliaApiClient implements Runnable {
	public static final String LOCAL_HOST = "127.0.0.1";
    public static final boolean WAIT_UNTIL_CONNECTED = true;
    public static final boolean DO_NOT_RECONNECT_ONCE_DISCONNECTED = true;
    public static final int THREAD_CYCLE_IN_MS = 50;
    private static final int MILLI_SECOND_TIMEOUT = 5000;
    final int CONNECT_PORT = 5000;


    long mStartTime;
    IntWA mObjectHandles;

	IntW mLeftMotor;
	IntW mRightMotor;

    double leftMotorSpeed;
    double rightMotorSpeed;

	int mClientID;
	remoteApi mVrep;
	private LinkedBlockingQueue<ControllerData> mQueue;

    private volatile boolean done = false;

	public CoppeliaApiClient(LinkedBlockingQueue<ControllerData> queue) {
		mQueue = queue;
		leftMotorSpeed = 0;
        rightMotorSpeed = 0;
        done = false;
        /*try {
            LOCAL_HOST = InetAddress.getLocalHost().toString();

        } catch (java.net.UnknownHostException ex) {
            System.out.println("Warning: localhost is unknown, reverting to 127.0.0.1");
            LOCAL_HOST = "127.0.0.1";
        }*/

	}
	
	public boolean init() {
		mVrep = new remoteApi();
		mVrep.simxFinish(-1); // just in case, close all opened connections


        mClientID = mVrep.simxStart(LOCAL_HOST,
                CONNECT_PORT, WAIT_UNTIL_CONNECTED,
                DO_NOT_RECONNECT_ONCE_DISCONNECTED,
                MILLI_SECOND_TIMEOUT, THREAD_CYCLE_IN_MS);
		if (mClientID != -1) {
			System.out.println("Connected to remote API server");	

			mObjectHandles = new IntWA(1);
			mLeftMotor = new IntW(1);
			mRightMotor = new IntW(1);
			
			int ret = mVrep.simxGetObjects(mClientID,remoteApi.sim_handle_all, mObjectHandles,
                    remoteApi.simx_opmode_oneshot_wait);
			if (ret == remoteApi.simx_return_ok)
				System.out.format("Number of objects in the scene: %d\n", mObjectHandles.getArray().length);
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
			
			mStartTime = System.currentTimeMillis();
			return true;
			
		} else {
			System.out.println("Failed connecting to remote API server");
			return false;
		}
		
	}

	public void run() {
		while (!done && !Thread.currentThread().isInterrupted())
		{
			ControllerData cd;
			try {
				cd = mQueue.take();
				leftMotorSpeed = cd.getMotorSpeed(1) * 3.14d;
				rightMotorSpeed = cd.getMotorSpeed(2) * 3.14d;

                mVrep.simxSetJointTargetVelocity(mClientID, mLeftMotor.getValue(),(float) -leftMotorSpeed,remoteApi.simx_opmode_oneshot);
                mVrep.simxSetJointTargetVelocity(mClientID, mRightMotor.getValue(),(float) rightMotorSpeed,remoteApi.simx_opmode_oneshot);

			} catch (InterruptedException e) {
                Thread.currentThread().interrupt();
			}
		}
		
		// Before closing the connection to V-REP, make sure that the last command sent out had time to arrive. You can guarantee this with (for example):
		IntW pingTime = new IntW(0);
		mVrep.simxGetPingTime(mClientID,pingTime);

		// Now close the connection to V-REP:	
		mVrep.simxFinish(mClientID);
		System.out.println("Program ended");
	}

    public void requestTerminate() {
        done = true;
    }
}

