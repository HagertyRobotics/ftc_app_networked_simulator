package org.ftccommunity.simulator.modules.devices;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ftccommunity.simulator.data.SimData;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

public abstract class Device {
	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected DeviceType mType=null;

	protected SimData[] mSimData;

	public Device() {
	}

	public Device(DeviceType type) {
		mType=type;
	}

	abstract public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer );
	abstract public void updateDebugGuiVbox();
	abstract public void setupDebugGuiVbox(VBox vbox);

	public DeviceType getType() {
		return mType;
	}

	public SimData[] getSimDataArray() {
		return mSimData;
	}
}
