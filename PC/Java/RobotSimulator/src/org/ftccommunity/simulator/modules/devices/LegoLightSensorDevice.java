package org.ftccommunity.simulator.modules.devices;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

import javafx.scene.layout.VBox;

//@XmlRootElement(name="LegoLightSensorDevice")
@XmlAccessorType(XmlAccessType.NONE)
public class LegoLightSensorDevice extends Device {

	public LegoLightSensorDevice() {
		super(DeviceType.TETRIX_SERVO);
	}

	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer, int portNum) {
	}

	public void updateDebugGuiVbox() {
	}

	public void setupDebugGuiVbox(VBox vbox) {
	}
}
