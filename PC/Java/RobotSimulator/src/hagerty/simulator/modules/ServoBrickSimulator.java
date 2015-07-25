package hagerty.simulator.modules;

import javax.xml.bind.annotation.XmlRootElement;

import javafx.scene.layout.VBox;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Servo")
public class ServoBrickSimulator extends BrickSimulator {

    private final String name = "Core Servo Controller";

    /**
     * Default constructor.
     */
    public ServoBrickSimulator() {
    }

    public String getName() {
    	return name;
    }

	public void setupDebugGuiVbox(VBox vbox) {}

	public void populateDebugGuiVbox() {}

}