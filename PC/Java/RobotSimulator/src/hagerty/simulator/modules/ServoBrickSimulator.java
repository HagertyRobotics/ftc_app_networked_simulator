package hagerty.simulator.modules;

import hagerty.simulator.legacy.data.SimData;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Servo")
public class ServoBrickSimulator extends BrickSimulator {
    private final String name = "Core Servo Controller";

    public ServoBrickSimulator() {
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