package org.ftccommunity.simulator.modules.devices;

import org.ftccommunity.simulator.data.NullSimData;
import org.ftccommunity.simulator.data.SimData;

import javax.xml.bind.annotation.XmlRootElement;

import javafx.scene.layout.VBox;

@XmlRootElement(name="NullDevice")
//@XmlAccessorType(XmlAccessType.NONE)
public class NullDevice extends Device {

	public NullDevice() {
		super(DeviceType.NONE);
		mSimData = new SimData[1];
		mSimData[0] = new NullSimData();	// Add 1st motor
	}

	public void processBuffer(byte[] currentStateBuffer, int portNum) {
	}

	public void updateDebugGuiVbox() {
	}

	public void setupDebugGuiVbox(VBox vbox) {
	}
}
