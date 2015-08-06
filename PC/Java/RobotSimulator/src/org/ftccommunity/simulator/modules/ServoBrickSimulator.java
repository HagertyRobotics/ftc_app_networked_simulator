package org.ftccommunity.simulator.modules;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.modules.devices.Device;
import org.ftccommunity.simulator.modules.devices.DeviceType;
import org.ftccommunity.simulator.modules.devices.NullDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Servo")
public class ServoBrickSimulator extends BrickSimulator {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public ServoBrickSimulator() {
    	mType = "Core Servo Controller";
    	mFXMLFileName = "view/EditDialog.fxml";
    	mNumberOfPorts = 6;
    	mDevices = new Device[mNumberOfPorts];
    	for (int i=0;i<mNumberOfPorts;i++) {
    		mDevices[i] = new NullDevice();
    	}
    }

    public void fixupUnMarshaling() {}

	public void setupDebugGuiVbox(VBox vbox) {}

	public void updateDebugGuiVbox() {}

	public void populateDetailsPane(Pane pane) {}

    public void handleIncomingPacket(byte[] data, int length, boolean wait) {
    }

	public List<DeviceType> getDeviceTypeList() {
		List<DeviceType> dtl = new ArrayList<>();
		dtl.add(DeviceType.USB_SERVO);
		return dtl;
	}
}