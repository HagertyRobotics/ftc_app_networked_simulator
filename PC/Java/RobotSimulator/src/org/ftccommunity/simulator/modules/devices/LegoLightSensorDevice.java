package org.ftccommunity.simulator.modules.devices;

import javafx.scene.layout.VBox;

public class LegoLightSensorDevice extends Device {

	public LegoLightSensorDevice() {
		super(DeviceType.TETRIX_SERVO);
	}

	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer ) {
	}

	public void updateDebugGuiVbox() {
	}

	public void setupDebugGuiVbox(VBox vbox) {
	}
}
