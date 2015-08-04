package hagerty.simulator.legacy.data;

import org.ftccommunity.simulator.net.protocol.SimulatorData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SimDataFactory {
	public static SimData buildSimData(SimulatorData.Type.Types type) {
        SimData simData;
        switch (type) {
		case NONE:
			simData = null;
			break;
		case LEGACY_MOTOR:
			simData = new LegacyMotorSimData();
			break;
		case LEGACY_LIGHT:
            // fall through
            case LEGACY_TOUCH:
            // fallthrough
            case USB_MOTOR:
            // fallthrough
            case USB_SERVO:
            throw new NotImplementedException();
            default:
                throw new AssertionError("You did not specify a valid type");
        }
        return simData;
	}
}
