package org.ftccommunity.simulator.modules.devices;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.scene.layout.VBox;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

@XmlRootElement(name="TetrixServoControllDevice")
@XmlAccessorType(XmlAccessType.NONE)
public class TetrixServoControllerDevice extends Device
{

	public TetrixServoControllerDevice() {
		// TODO: use proper servo
		super(SimulatorData.Type.Types.USB_MOTOR);
	}

	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer, int portNum ) {
	}

	public void updateDebugGuiVbox() {

	}

	public void setupDebugGuiVbox(VBox vbox) {

	}
}
