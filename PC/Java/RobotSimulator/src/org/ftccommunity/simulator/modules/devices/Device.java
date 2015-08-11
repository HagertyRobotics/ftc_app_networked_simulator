package org.ftccommunity.simulator.modules.devices;

import org.ftccommunity.simulator.data.SimData;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlAccessType;

import javafx.scene.layout.VBox;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class Device {

    protected SimulatorData.Type.Types mType = null;

    @XmlElementRef(name="SimData")
	protected SimData[] mSimData;

	public Device() {
	}

	public Device(SimulatorData.Type.Types type) {
		mType = type;
	}

	abstract public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer, int portNum);
	abstract public void updateDebugGuiVbox();
	abstract public void setupDebugGuiVbox(VBox vbox);

	public SimulatorData.Type.Types getType() {
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
		for (SimData aMSimData : mSimData) {
			names.add(aMSimData.getName());
		}
		return names;
	}

	public void setDeviceNames(String[] names) {
		for (int i = 0; i < mSimData.length; i++) {
			mSimData[i].setName(names[i]);
		}
	}

	public SimData findSimDataByName(String name) {
		for (SimData aMSimData : mSimData) {
			if (aMSimData.getName().equals(name)) {
				return aMSimData;
			}
		}
		return null;
	}
}
