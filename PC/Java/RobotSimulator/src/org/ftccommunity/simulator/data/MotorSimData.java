package org.ftccommunity.simulator.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="MotorSimData")
@XmlAccessorType(XmlAccessType.NONE)
public class MotorSimData extends SimData {

	private float mMotorSpeed=0;
	private boolean mMotorFloatMode=false;

	public MotorSimData() {
		super();
		construct();
	}

    @Override
    protected void construct() {
        //System.out.println("Building Legacy Motor SimData");
    }

	public float getMotorSpeed() {
		lock.readLock().lock();
		try {
			return mMotorSpeed;
		} finally {
			lock.readLock().unlock();
		}
	}

	public boolean getMotorFloatMode() {
		return mMotorFloatMode;
	}

	public void setMotorSpeed(byte speedByte) {
		lock.writeLock().lock();
		try {
			if (speedByte == (byte)0x80) {
				mMotorFloatMode = true;
				mMotorSpeed=0.0f;
			} else {
				mMotorSpeed = (float)speedByte/100.0f;
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
}
