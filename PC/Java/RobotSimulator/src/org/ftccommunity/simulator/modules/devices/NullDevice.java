package org.ftccommunity.simulator.modules.devices;

import javafx.scene.layout.VBox;

public class NullDevice extends Device {

	public NullDevice() {
		super(DeviceType.NONE);
	}
	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer ) {
	}

	public void updateDebugGuiVbox() {
	}

	public void setupDebugGuiVbox(VBox vbox) {
	}
}
