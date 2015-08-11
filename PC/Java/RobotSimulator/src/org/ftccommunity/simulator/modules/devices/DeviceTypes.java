package org.ftccommunity.simulator.modules.devices;

import org.ftccommunity.simulator.net.protocol.SimulatorData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

//@XmlType(name = "DeviceType")
//@XmlEnum
@Deprecated
@XmlAccessorType(XmlAccessType.NONE)
public enum DeviceTypes {
	NONE("None"),
	TETRIX_MOTOR("Tetrix Motor Controller"),
	TETRIX_SERVO("Tetrix Servo Controller"),
	LEGO_LIGHT("Lego Light Sensor"),
	LEGO_TOUCH("Lego Touch Sensor"),
	USB_MOTOR("USB Motor Controller"),
	USB_SERVO("USB Servo Controller");

	private final String mName;
	DeviceTypes(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}
}

