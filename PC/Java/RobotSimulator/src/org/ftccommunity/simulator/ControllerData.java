package org.ftccommunity.simulator;

public class ControllerData {

	float[] mMotorSpeed = new float[2];
	boolean	mFloatMode = false;

	boolean[] mFloatMode = new boolean[2];

	public ControllerData() {
		mFloatMode[0] = false;
		mFloatMode[1] = false;
	}

	float getMotorSpeed(int i) {
		return mMotorSpeed[i-1];
	}

	void setMotorSpeed(int i, float motorSpeed){
		mMotorSpeed[i-1] = motorSpeed;
	}

	void setFloatMode(int i, boolean floatMode) {
		mFloatMode[i-1] = floatMode;
		setMotorSpeed(i, 0);
	}

	boolean getFloatMode(int i){
		return mFloatMode[i-1];
	}

}
