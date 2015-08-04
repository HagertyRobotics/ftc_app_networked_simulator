package org.ftccommunity.simulator.modules.devices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "SimulationDataType")
@XmlEnum
public enum DeviceType {
	NONE("None"),
	TETRIX_MOTOR("Tetrix Motor Controller"),
	TETRIX_SERVO("Tetrix Servo Controller"),
	LEGO_LIGHT("Lego Light Sensor"),
	LEGO_TOUCH("Lego Touch Sensor"),
	USB_MOTOR("USB Motor Controller"),
	USB_SERVO("USB Servo Controller");

	private final String mName;
	DeviceType(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}
}
