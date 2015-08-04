package org.ftccommunity.simulator.modules.devices;

import javafx.scene.layout.VBox;

public class TetrixServoControllerDevice extends Device
{

	public TetrixServoControllerDevice() {
		super(DeviceType.TETRIX_SERVO);
	}

	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer ) {
	}

	public void updateDebugGuiVbox() {

	}

	public void setupDebugGuiVbox(VBox vbox) {

	}
}
