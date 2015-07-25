package hagerty.simulator.modules;

import javax.xml.bind.annotation.XmlRootElement;

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
    
	public void setupDebugGuiVbox(VBox vbox) {}
	
	public void populateDebugGuiVbox() {}

}