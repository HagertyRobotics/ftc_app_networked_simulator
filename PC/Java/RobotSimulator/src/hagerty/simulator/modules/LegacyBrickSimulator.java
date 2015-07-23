package hagerty.simulator.modules;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Legacy")
public class LegacyBrickSimulator extends BrickSimulator {

    private final String name = "Core Legacy Module";

    private StringProperty[] ports = new StringProperty[6];

    /**
     * Default constructor.
     */
    public LegacyBrickSimulator() {
    	ports[0]= new SimpleStringProperty("S0");
    }

    public String getName() {
    	return name;
    }

}