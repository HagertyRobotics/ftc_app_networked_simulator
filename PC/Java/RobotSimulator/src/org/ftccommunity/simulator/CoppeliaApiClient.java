package org.ftccommunity.simulator;


import org.ftccommunity.gui.MainApp;
import org.ftccommunity.simulator.data.AnalogSimData;
import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.SimData;
import org.ftccommunity.simulator.modules.BrickSimulator;

import coppelia.BoolW;
import coppelia.FloatWAA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;

import java.util.List;
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
	IntW mLeftMotorHandle;
	IntW mRightMotorHandle;
	IntW mLightSensorHandle;
	BoolW mLightSensorDetectionState;
	FloatWAA mLightSensorAuxValues;

	//IntWA mLightSensorResolution;
	//CharWA mLightSensorImage;

	int mClientID;
	remoteApi mVrep;
	final org.ftccommunity.gui.MainApp mMainApp;
    private volatile boolean done;
    int ret;

	public CoppeliaApiClient(MainApp mainApp) {
		mMainApp = mainApp;
        done = false;
    }

	public boolean init() {
		int ret;

		mVrep = new remoteApi();
		mVrep.simxFinish(-1); // just in case, close all opened connections

        mClientID = mVrep.simxStart(LOCAL_HOST,
                CONNECT_PORT, WAIT_UNTIL_CONNECTED,
                DO_NOT_RECONNECT_ONCE_DISCONNECTED,
                MILLI_SECOND_TIMEOUT, THREAD_CYCLE_IN_MS);
        if (mClientID != -1) {
            System.out.println("Connected to remote API server");

			mObjectHandles = new IntWA(1);
			mLeftMotorHandle = new IntW(1);
			mRightMotorHandle = new IntW(1);

			mLightSensorHandle = new IntW(1);
			mLightSensorDetectionState = new BoolW(true);
			mLightSensorAuxValues = new FloatWAA(20);

            ret = mVrep.simxGetObjects(mClientID, remoteApi.sim_handle_all, mObjectHandles,
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

			mVrep.simxGetObjectHandle(mClientID,"remoteApiControlledBubbleRobLeftMotor",mLeftMotorHandle,remoteApi.simx_opmode_oneshot_wait);
			mVrep.simxGetObjectHandle(mClientID,"remoteApiControlledBubbleRobRightMotor",mRightMotorHandle,remoteApi.simx_opmode_oneshot_wait);

			ret = mVrep.simxGetObjectHandle(mClientID,"lightSensor",mLightSensorHandle, remoteApi.simx_opmode_oneshot_wait);
			if (ret == remoteApi.simx_return_ok)
                System.out.println("Got Vision Handle");
            else
				System.out.format("Error: get vision handle returned with error");

			mStartTime=System.currentTimeMillis();
			return true;

		} else {
			System.out.println("Failed connecting to remote API server");
			return false;
		}

	}

	public void run()
	{
		boolean done=false;
		float leftMotorSpeed=0;
		float rightMotorSpeed=0;

		/*
		 * Read the current list of modules from the GUI MainApp class
		 */
		SimData simDataMotor1=null;
		List<BrickSimulator> brickList = mMainApp.getBrickData();
		for (BrickSimulator currentBrick : brickList) {
			simDataMotor1 = currentBrick.findSimDataByName("motor_1");
			if (simDataMotor1 != null) break;
		}
		if (simDataMotor1 == null) {
			System.out.println("Failed to find a port named 'motor_1'");
			done = true;
		}

		SimData simDataMotor2=null;
		for (BrickSimulator currentBrick : brickList) {
			simDataMotor2 = currentBrick.findSimDataByName("motor_2");
			if (simDataMotor2 != null) break;
		}

		if (simDataMotor2 == null) {
			System.out.println("Failed to find a port named 'motor_2'");
			done = true;
		}

		SimData simDataLight1=null;
		for (BrickSimulator currentBrick : brickList) {
			simDataLight1 = currentBrick.findSimDataByName("light_1");
			if (simDataLight1 != null) break;
		}

		if (simDataLight1 == null) {
			System.out.println("Failed to find a port named 'light_1'");
			done = true;
		}

		ret = mVrep.simxReadVisionSensor(
				mClientID,
				mLightSensorHandle.getValue(),
				mLightSensorDetectionState,
				mLightSensorAuxValues,
				remoteApi.simx_opmode_streaming
				);
		if (ret == remoteApi.simx_return_ok  || ret == remoteApi.simx_return_novalue_flag) {
			System.out.println("got image");
		} else {
			System.out.println("Error: Geting 1st Light Sensor Image. " + ret);
		}

        while (!done && !Thread.currentThread().isInterrupted()) {
			leftMotorSpeed = ((MotorSimData)simDataMotor1).getMotorSpeed() * 3.14f;
			rightMotorSpeed = ((MotorSimData)simDataMotor2).getMotorSpeed() * 3.14f;

			mVrep.simxSetJointTargetVelocity(mClientID,mLeftMotorHandle.getValue(),-leftMotorSpeed,remoteApi.simx_opmode_oneshot);
			mVrep.simxSetJointTargetVelocity(mClientID,mRightMotorHandle.getValue(),rightMotorSpeed,remoteApi.simx_opmode_oneshot);

			ret = mVrep.simxReadVisionSensor(
					mClientID,
					mLightSensorHandle.getValue(),
					mLightSensorDetectionState,
					mLightSensorAuxValues,
					remoteApi.simx_opmode_buffer
					);
			if (ret == remoteApi.simx_return_ok) {
				float light = mLightSensorAuxValues.getArray()[0].getArray()[11];
				//System.out.println("got image " + light);
				((AnalogSimData)simDataLight1).setAnalogValue(light);
			} else if (ret == remoteApi.simx_return_novalue_flag) {
				//System.out.println("no image");
			} else {
				System.out.println("Error: Geting Light Sensor Image. " + ret);
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

