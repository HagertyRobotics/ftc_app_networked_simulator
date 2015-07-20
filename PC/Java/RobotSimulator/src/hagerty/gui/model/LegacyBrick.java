package hagerty.gui.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Legacy")
public class LegacyBrick extends Brick {

    private final String name = "Core Legacy Module";

    /**
     * Default constructor.
     */
    public LegacyBrick() {
    }

    public String getName() {
    	return name;
    }

}