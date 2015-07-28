package hagerty.simulator.modules;

import hagerty.simulator.legacy.data.LegacyMotorSimData;
import hagerty.simulator.legacy.data.SimData;
import hagerty.simulator.legacy.data.SimDataFactory;
import hagerty.simulator.legacy.data.SimDataType;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.Logger;


/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@XmlRootElement(name="Legacy")
public class LegacyBrickSimulator extends BrickSimulator {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected final byte[] mCurrentStateBuffer = new byte[208];
    /*
     ** Packet types
     */
    protected final byte[] writeCmd = {85, -86, 0, 0, 0};
    protected final byte[] readCmd = {85, -86, -128, 0, 0};
    protected final byte[] recSyncCmd3 = {51, -52, 0, 0, 3};
    protected final byte[] recSyncCmd0 = {51, -52, -128, 0, 0};
    protected final byte[] recSyncCmd208 = {51, -52, -128, 0, (byte) 208};
    protected final byte[] controllerTypeLegacy = {0, 77, 73};       // Controller type USBLegacyModule
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

    private void sendPacketToPhone(byte[] sendData) {
    	try {
    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mPhoneIPAddress, mPhonePort);
        	mServerSocket.send(sendPacket);
        	//System.out.println("sendPacketToPhone: (" + bufferToHexString(sendData,0,sendData.length) + ") len=" + sendData.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleIncomingPacket(byte[] data, int length, boolean wait)
    {
    	//System.out.println("Receive Buffer: (" + bufferToHexString(data,0,25) + ") len=" + data.length);

    	if (data[0] == readCmd[0] && data[2] == readCmd[2] && data[4] == (byte)208) { // readCmd
    		sendPacketToPhone(mCurrentStateBuffer);
    		// Set the Port S0 ready bit in the global part of the Current State Buffer
    		mCurrentStateBuffer[3] = (byte)0xfe;  // Port S0 ready
        } else {

	        // Write Command
	    	// Process the received data packet
	        // Loop through each of the 6 ports and see if the Action flag is set.
	        // If set then copy the 32 bytes for the port into the CurrentStateBuffer

	        for (int i=0;i<6;i++) {
	        	switch (portType[i]) {
	        	case LEGACY_MOTOR:
	        		((LegacyMotorSimData) portSimData[i]).processBuffer(i, data, mCurrentStateBuffer );
	        		break;
	        	case LEGACY_LIGHT:
	        		break;
	        	case LEGACY_TOUCH:
	        		break;
	        	default:
	        		break;
	        	}
	        }
        }
    }


    /**
     * For the LegacyBrickSimulator objects, since we couldn't get the marshaler to handle the list of small
     * SimData objects(6), we created and marshaled a list of the six port types and names.  We now need to create
     * the objects by hand using the unmarshaled list of portTypes and portNames.
     */
    public void fixupUnMarshaling() {
    	for (int i=0;i<6;i++) {
    		// if port is not configured then don't create any SimData objects for it!
    		portSimData[i] = SimDataFactory.buildSimData(portType[i]);
    		if (portSimData[i] != null){
        		portSimData[i].setSimDataName(portName[i]);  // name the newly created SimData object
    		}

    	}
    }

    /**
     *
     */
    public SimData findSimDataName(String name) {
    	for (int i=0;i<6;i++) {
    		if (portName[i] != null) {
    			if (portName[i].equals(name)) {
    				return portSimData[i];
    			}
    		}
    	}
    	return null;
    }

    /**
     * Populate the details pane in the Overview window.  This method adds details that are not common
     * with the other types of controllers.  In this Legacy controller, we need to detail the types of each
     * of the six ports.  A blank pane is passed in and the routine will fill in a 3x6 grid with the port info.
     */
    public void populateDetailsPane(Pane pane) {

		GridPane grid = new GridPane();
		grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.prefWidthProperty().bind(pane.widthProperty());
        grid.prefHeightProperty().bind(pane.heightProperty());
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(25);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(50);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPercentWidth(25);
		grid.getColumnConstraints().addAll(col1,col2,col3);

		for (int i=0;i<6;i++) {
			Text portText = new Text("Port " + i);
			grid.add(portText, 0, i);
			Text typeText = new Text(portType[i].getName());
			grid.add(typeText, 1,  i);
			Text nameText = new Text(portName[i]);
			grid.add(nameText, 2,  i);

		}

		pane.getChildren().add(grid);
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

    public void setPortName(String[] s) {
    	portName = s;
    }

    public SimDataType[] getPortType() {
        return portType;
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