package org.ftccommunity.simulator.modules.devices;

import org.ftccommunity.simulator.net.protocol.SimulatorData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DeviceFactory {
	public static Device buildDevice(SimulatorData.Type.Types type) {
        Device device;
        switch (type) {
            case NONE:
                device = new NullDevice();
                break;
            case LEGACY_MOTOR:
                device = new TetrixMotorControllerDevice();
                break;
            case USB_MOTOR:
                device = new USBMotorControllerDevice();
                break;
            case LEGACY_TOUCH:
                device = new LegoLightSensorDevice();
                break;

                // fall through
            case LEGACY_LIGHT:
                // fall through
            case USB_SERVO:
                throw new NotImplementedException();
            default:
                    throw new AssertionError("You did not specify a valid type");
        }
        return device;
	}
}
