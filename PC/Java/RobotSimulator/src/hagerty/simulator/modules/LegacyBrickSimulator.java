package hagerty.simulator.modules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import hagerty.simulator.legacy.data.SimData;
import hagerty.simulator.legacy.data.SimDataFactory;
import hagerty.simulator.legacy.data.SimDataType;
import javafx.scene.layout.VBox;


/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Legacy")
public class LegacyBrickSimulator extends BrickSimulator {

    private final String name = "Core Legacy Module";

    @XmlElement
    private String[] portName = new String[6];
    @XmlElement
    private SimDataType[] portType = new SimDataType[6];

    private SimData[] portSimData = new SimData[6];

    /**
     * Default constructor.
     */
    public LegacyBrickSimulator() {
    	for (int i=0;i<6;i++) {
    		portName[i] = null;
    		portType[i] = SimDataType.NONE;
    		portSimData[i] = null;
    	}
    }

    /**
     * For the LegacyBrickSimulator objects, since we couldn't get the marshaler to handle the list of small
     * SimData objects(6), we need to create the objects by hand using the unmarshaled list of portTypes.
     */
    public void fixupUnMarshaling() {
    	for (int i=0;i<6;i++) {
    		// if port is not configured then don't create any SimData objects for it!
    		System.out.println("fixup: " + portName[i]);
    		portSimData[i] = SimDataFactory.buildSimData(portType[i]);
    		if (portSimData[i] != null){
        		portSimData[i].setSimDataName(portName[i]);  // name the newly created SimData object
    		}

    	}
    }

    /**
     * Getters/Setters
     */
    public String getName() {
    	return name;
    }

    public SimData[] getPortSimData() {
    	return portSimData;
    }

    public String[] getPortName() {
    	return portName;
    }

    public SimDataType[] getPortType() {
    	return portType;
    }

    public void setPortName(String[] s) {
    	portName = s;
    }

    public void setPortType(SimDataType[] type) {
    	portType = type;
    }


    /**
     * GUI Stuff
     */
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