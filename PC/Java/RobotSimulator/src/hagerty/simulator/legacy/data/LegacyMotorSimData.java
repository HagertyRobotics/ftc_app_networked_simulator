package hagerty.simulator.legacy.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class LegacyMotorSimData extends SimData {

	volatile float mMotor1Speed=0.0f;
	volatile float mMotor2Speed=0.0f;
	volatile boolean mMotor1FloatMode=false;
	volatile boolean mMotor2FloatMode=false;

	public LegacyMotorSimData() {
	}

	public float getMotor1Speed() {
		return mMotor1Speed;
	}

	public float getMotor2Speed() {
		return mMotor2Speed;
	}

	public void setMotor1Speed(float motorSpeed){
		mMotor1Speed = motorSpeed;
	}

	public void setMotor2Speed(float motorSpeed){
		mMotor2Speed = motorSpeed;
	}

	public void setMotor1FloatMode(boolean floatMode) {
		mMotor1FloatMode = floatMode;
		setMotor1Speed(0.0f);
	}

	public void setMotor2FloatMode(boolean floatMode) {
		mMotor2FloatMode = floatMode;
		setMotor2Speed(0.0f);
	}

	public boolean getMotor1FloatMode() {
		return mMotor1FloatMode;
	}

	public boolean getMotor2FloatMode() {
		return mMotor2FloatMode;
	}
}