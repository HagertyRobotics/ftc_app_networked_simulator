package org.ftccommunity.simulator.modules.devices;

import org.ftccommunity.simulator.net.protocol.SimulatorData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class DeviceType {
	/*public static DeviceTypes fromProtocol(SimulatorData.Type.Types type) {
		switch (type) {
			case NONE:
				return DeviceTypes.NONE;
			case LEGACY_MOTOR:
				return DeviceTypes.TETRIX_MOTOR;
//			case LEGACY_SERVO:
//				return DeviceTypes.TETRIX_SERVO;
			case LEGACY_LIGHT:
				return DeviceTypes.LEGO_LIGHT;
			case LEGACY_TOUCH:
				return DeviceTypes.LEGO_TOUCH;
			case USB_MOTOR:
				return DeviceTypes.USB_MOTOR;
			case USB_SERVO:
				return DeviceTypes.USB_SERVO;
			default:
				throw new NotImplementedException();
		}
	}*/

	public static SimulatorData.Type.Types fromDeviceTypes(DeviceTypes type) {
		switch (type) {
			case NONE:
				return SimulatorData.Type.Types.NONE;
			case TETRIX_MOTOR:
				return SimulatorData.Type.Types.LEGACY_MOTOR;
			case LEGO_LIGHT:
				return SimulatorData.Type.Types.LEGACY_LIGHT;
			case LEGO_TOUCH:
				return SimulatorData.Type.Types.LEGACY_TOUCH;
			case USB_MOTOR:
				return SimulatorData.Type.Types.USB_MOTOR;
			case USB_SERVO:
				return SimulatorData.Type.Types.USB_SERVO;
			default:
				throw new NotImplementedException();
		}
	}

	public static String getName(SimulatorData.Type.Types type) {
		return type.getDescriptorForType().getName();
	}
}
