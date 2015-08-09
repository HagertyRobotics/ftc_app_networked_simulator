package org.ftccommunity.simulator.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="AnalogSimData")
@XmlAccessorType(XmlAccessType.NONE)
public class AnalogSimData extends SimData {

	float mAnalogValue=0.0f;

	public AnalogSimData() {
		super();
		construct();
	}

    @Override
    protected void construct() {
    }

	public float getAnalogValue() {
		lock.readLock().lock();
		try {
			return mAnalogValue;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setAnalogValue(float a) {
		lock.writeLock().lock();
		try {
			mAnalogValue = a;
		} finally {
			lock.writeLock().unlock();
		}
	}
}