package org.ftccommunity.simulator.modules.devices;

import org.ftccommunity.simulator.data.SimData;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlAccessType;

import javafx.scene.layout.VBox;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class Device {

    protected DeviceType mType=null;

    @XmlElementRef(name="SimData")
	protected SimData[] mSimData;

	public Device() {
	}

	public Device(DeviceType type) {
		mType=type;
	}

	abstract public void processBuffer(byte[] currentStateBuffer, int portNum);
	abstract public void updateDebugGuiVbox();
	abstract public void setupDebugGuiVbox(VBox vbox);

	public DeviceType getType() {
		return mType;
	}

	public void setmSimData(SimData[] s) {
		mSimData = s;
	}

	public SimData[] getmSimData() {
		return mSimData;
	}

	public SimData[] getSimDataArray() {
		return mSimData;
	}

	public int getNumberOfPorts() {
		return mSimData.length;
	}

	public List<String> getPortNames() {
		List<String> names = new ArrayList<>();
		int len = mSimData.length;
		for (int i=0;i<len;i++) {
			names.add(mSimData[i].getName());
		}
		return names;
	}

	public void setDeviceNames(String[] names) {
		for (int i = 0; i < mSimData.length; i++) {
			mSimData[i].setName(names[i]);
		}
	}

	public SimData findSimDataByName(String name) {
		int len = mSimData.length;
		for (int i=0;i<len;i++) {
			if (mSimData[i].getName().equals(name)) {
				return mSimData[i];
			}
		}
		return null;
	}
}
