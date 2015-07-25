package hagerty.simulator.modules;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import hagerty.simulator.legacy.data.LegacyMotorSimData;
import hagerty.simulator.legacy.data.SimData;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Legacy")
public class LegacyBrickSimulator extends BrickSimulator {

    private final String name = "Core Legacy Module";

    @XmlElement
    private String[] portNames = new String[6];
    @XmlElement
    private int[] portNumbers = new int[6];
    @XmlElement
    private SimData[] portSimData = new SimData[6];

    /**
     * Default constructor.
     */
    public LegacyBrickSimulator() {
    	//ports[0]= new SimpleStringProperty("S0");
    	portSimData[0] = new LegacyMotorSimData();
    	portSimData[1] = null;
    	portSimData[2] = null;
    	portSimData[3] = null;
    	portSimData[4] = null;
    	portSimData[5] = null;

    	for (int i=0;i<6;i++) {
    		portNames[i] = "";
    		portNumbers[i] = 1;
    	}
    }

    public String getName() {
    	return name;
    }

    public SimData[] getPortSimData() {
    	return portSimData;
    }

    public String[] getPortNames() {
    	return portNames;
    }

    public int[] getPortNumbers() {
    	return portNumbers;
    }

    public void setPortNames(String[] s) {
    	portNames = s;
    }

    public void setPortNumbers(int[] n) {
    	portNumbers = n;
    }

	public void setupDebugGuiVbox(VBox vbox) {

		for (int i=0;i<6;i++) {
			if (portSimData[i] != null) {
				portSimData[i].setupDebugGuiVbox(vbox);
			}
		}
	}

	public void populateDebugGuiVbox() {

		for (int i=0;i<6;i++) {
			if (portSimData[i] != null) {
				portSimData[i].populateDebugGuiVbox();
			}
		}
	}

}