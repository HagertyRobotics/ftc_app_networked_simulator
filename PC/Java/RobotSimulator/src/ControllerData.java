
public class ControllerData {

	float[] mMotorSpeed = new float[2];
	boolean	mFloatMode = false;
	
	public ControllerData() {
		
	}
	
	float getMotorSpeed(int i) {
		return mMotorSpeed[i-1];
	}
	
	void setMotorSpeed(int i, float motorSpeed){
		mMotorSpeed[i-1] = motorSpeed;
	}
	
	void setFloatMode(int i, boolean floatMode) {
		mFloatMode = floatMode;
	}

}
