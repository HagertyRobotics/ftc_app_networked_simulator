package hagerty.simulator.modules;

import javax.xml.bind.annotation.XmlRootElement;

import hagerty.simulator.legacy.data.SimDataFactory;
import javafx.scene.layout.VBox;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Motor")
public class MotorBrickSimulator extends BrickSimulator {

    private final String name = "Core Motor Controller";

    /**
     * Default constructor.
     */
    public MotorBrickSimulator() {
    }

    public String getName() {
    	return name;
    }

    public void fixupUnMarshaling() {}

	public void setupDebugGuiVbox(VBox vbox) {}

	public void populateDebugGuiVbox() {}

}