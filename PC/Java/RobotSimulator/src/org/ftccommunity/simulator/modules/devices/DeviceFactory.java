package org.ftccommunity.simulator.modules.devices;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DeviceFactory {
	public static Device buildDevice(DeviceType type) {
        Device device;
        switch (type) {
		case NONE:
			device = new NullDevice();
			break;
		case TETRIX_MOTOR:
			device = new TetrixMotorControllerDevice();
			break;
		case USB_MOTOR:
			device = new USBMotorControllerDevice();
			break;

            case LEGO_LIGHT:
            // fall through
            case TETRIX_SERVO:
            // fall through
            case USB_SERVO:
            throw new NotImplementedException();
            default:
                throw new AssertionError("You did not specify a valid type");
        }
        return device;
	}
}
