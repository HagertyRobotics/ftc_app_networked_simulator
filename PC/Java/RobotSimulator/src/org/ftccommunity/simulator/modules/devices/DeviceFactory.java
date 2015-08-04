package org.ftccommunity.simulator.modules.devices;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DeviceFactory {
	public static Device buildSimData(DeviceType type) {
        Device device;
        switch (type) {
		case NONE:
			device = null;
			break;
		case TETRIX_MOTOR:
			device = new TetrixMotorControllerDevice();
			break;
		case TETRIX_SERVO:
            // fall through
            case LEGO_LIGHT:
            // fall through
            case USB_MOTOR:
            // fall through
            case USB_SERVO:
            throw new NotImplementedException();
            default:
                throw new AssertionError("You did not specify a valid type");
        }
        return device;
	}
}
