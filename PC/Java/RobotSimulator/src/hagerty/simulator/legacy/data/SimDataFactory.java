package hagerty.simulator.legacy.data;

public class SimDataFactory {
	public static SimData buildSimData(SimDataType type) {
		SimData simData = null;
		switch (type) {
		case NONE:
			simData = null;
			break;
		case LEGACY_MOTOR:
			simData = new LegacyMotorSimData();
			break;
		case LEGACY_LIGHT:
			break;
		case LEGACY_TOUCH:
			break;
		case USB_MOTOR:
			break;
		case USB_SERVO:
			break;
		default:
			// throw exception
			break;
		}
		return simData;
	}
}
