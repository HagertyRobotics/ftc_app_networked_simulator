package hagerty.simulator.modules;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper class to wrap a list of bricks. This is used for saving the
 * list of controllers to XML.
 * 		XmlRootElement(name = "bricks")
 *    	XmlElement(name = "brick")
 * @author Hagerty Robotics
 */

@XmlRootElement(name = "bricks")
public class BrickListWrapper {

    private List<BrickSimulator> bricks;

    @XmlElementRef(name = "brick")
    public List<BrickSimulator> getBricks() {
        return bricks;
    }

    public void setBricks(List<BrickSimulator> bricks) {
        this.bricks = bricks;
    }
}