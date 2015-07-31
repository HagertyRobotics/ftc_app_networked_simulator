package hagerty.simulator.modules;

import hagerty.simulator.legacy.data.SimData;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@SuppressWarnings("ALL")
@XmlRootElement(name="Motor")
public class MotorBrickSimulator extends BrickSimulator {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private final String name = "Core Motor Controller";

    public MotorBrickSimulator() {
    }

    public String getName() {
    	return name;
    }

    public void fixupUnMarshaling() {}

	public void setupDebugGuiVbox(VBox vbox) {}

	public void populateDebugGuiVbox() {}

	public void populateDetailsPane(Pane pane) {}

    public SimData findSimDataName(String name) {
        return null;
    }

    public void handleIncomingPacket(byte[] data, int length, boolean wait) {
    }

}