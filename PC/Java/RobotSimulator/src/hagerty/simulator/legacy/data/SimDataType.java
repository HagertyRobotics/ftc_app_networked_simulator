package hagerty.simulator.legacy.data;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "SimulationDataType")
@XmlEnum
public enum SimDataType {
	NONE("None"),
	LEGACY_MOTOR("Motor Controller"),
	LEGACY_LIGHT("Light Sensor"),
	LEGACY_TOUCH("Touch Sensor"),
	USB_MOTOR("USB Motor Controller"),
	USB_SERVO("USB Servo Controller");

	private final String mName;
	SimDataType(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}
}
