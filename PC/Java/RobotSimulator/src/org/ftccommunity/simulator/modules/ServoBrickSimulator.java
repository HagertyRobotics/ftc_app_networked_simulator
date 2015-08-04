package org.ftccommunity.simulator.modules;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.data.SimData;

import java.util.logging.Logger;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Servo")
public class ServoBrickSimulator extends BrickSimulator {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected final String mType = "Core Servo Controller";

    public ServoBrickSimulator() {
    }

    public void fixupUnMarshaling() {}

	public void setupDebugGuiVbox(VBox vbox) {}

	public void updateDebugGuiVbox() {}

	public void populateDetailsPane(Pane pane) {}

    public SimData findSimDataByName(String name) {
        return null;
    }

    public void handleIncomingPacket(byte[] data, int length, boolean wait) {
    }

}