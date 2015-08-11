package org.ftccommunity.simulator.modules.devices;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.scene.layout.VBox;

@XmlRootElement(name="TetrixServoControllDevice")
@XmlAccessorType(XmlAccessType.NONE)
public class TetrixServoControllerDevice extends Device
{

	public TetrixServoControllerDevice() {
		super(DeviceType.TETRIX_SERVO);
	}

	public void processBuffer(byte[] currentStateBuffer, int portNum ) {
	}

	public void updateDebugGuiVbox() {

	}

	public void setupDebugGuiVbox(VBox vbox) {

	}
}
