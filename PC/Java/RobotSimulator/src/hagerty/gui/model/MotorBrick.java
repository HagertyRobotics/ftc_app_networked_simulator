package hagerty.gui.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="motor")
public class MotorBrick extends Brick {

    private final String name = "Core Motor Controller";

    /**
     * Default constructor.
     */
    public MotorBrick() {
    }

    public String getName() {
    	return name;
    }

}