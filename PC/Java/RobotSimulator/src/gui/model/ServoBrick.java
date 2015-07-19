package gui.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="servo")
public class ServoBrick extends Brick {

    private final String name = "Core Servo Controller";

    /**
     * Default constructor.
     */
    public ServoBrick() {
    }

    public String getName() {
    	return name;
    }

}