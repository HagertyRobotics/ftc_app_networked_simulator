package org.ftccommunity.simulator.modules;

import org.ftccommunity.simulator.data.SimData;
import org.ftccommunity.simulator.modules.devices.Device;
import org.ftccommunity.simulator.modules.devices.DeviceType;
import org.ftccommunity.simulator.modules.devices.USBMotorControllerDevice;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.ftccommunity.simulator.net.manager.NetworkManager;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Model class for a Motor Controller
 *
 * @author Hagerty High
 */
@SuppressWarnings("ALL")
@XmlRootElement(name="Motor")
public class MotorBrickSimulator extends BrickSimulator {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected final byte[] mCurrentStateBuffer = new byte[94];

    /*
     ** Packet types
     */
    protected final byte[] writeCmd = {85, -86, 0, 0, 0};
    protected final byte[] readCmd = {85, -86, -128, 0, 0};
    protected final byte[] recSyncCmd3 = {51, -52, 0, 0, 3};
    protected final byte[] recSyncCmd0 = {51, -52, -128, 0, 0};
    protected final byte[] recSyncCmd94 = {51, -52, -128, 0, (byte) 94};
    protected final byte[] controllerTypeLegacy = {0, 77, 77};       // Controller type USBLegacyModule


    /**
     * Default constructor.
     */
    public MotorBrickSimulator() {
    	mType = "Core Motor Controller";
    	mFXMLFileName = "view/EditDialog.fxml";
    	mNumberOfPorts = 1;
    	mDevices = new Device[mNumberOfPorts];
    	for (int i=0;i<mNumberOfPorts;i++) {
    		mDevices[i] = new USBMotorControllerDevice();
    	}
    }

    @Override
    protected byte[] receivePacketFromPhone() {
        return new byte[0];
    }

    private void sendPacketToPhone(byte[] sendData) {
    	/*try {
    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mPhoneIPAddress, mPhonePort);
        	mServerSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        NetworkManager.requestSend(SimulatorData.Type.Types.LEGACY_MOTOR,
                                          SimulatorData.Data.Modules.LEGACY_MOTOR, sendData);
    }


    public void handleIncomingPacket(byte[] data, int length, boolean wait)
    {
    	if (data[0] == readCmd[0] && data[2] == readCmd[2] && data[4] == (byte)94) { // readCmd
    		sendPacketToPhone(mCurrentStateBuffer);
        } else {
	        // Write Command...
	    	// Process the received data packet
	        // Loop through each of the ports in this object

	        for (int i=0;i<mNumberOfPorts;i++) {
	        	mDevices[i].processBuffer(data, mCurrentStateBuffer, i);
	        }
        }
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
        grid.setStyle("-fx-border: 2px solid; -fx-border-color: blue;");
        //grid.setGridLinesVisible(true);

		for (int i=0;i<mNumberOfPorts;i++) {
			Text portText = new Text("Port " + i);
			grid.add(portText, 0, i);

			Text typeText = new Text(mDevices[i].getType().getName());
			grid.add(typeText, 1,  i);

        	List<String> nameList = getPortDevice(i).getPortNames();
        	for (int j=0;j<nameList.size();j++) {
        		Text nameText = new Text(nameList.get(j));
        		grid.add(nameText,j+2,i);
        	}
		}
		pane.getChildren().add(grid);
	}

    @Override
    public SimData findSimDataName(String name) {
        return null;
    }

    /**
     * Getters/Setters
     */



    /**
     * GUI Stuff
     */
	public void setupDebugGuiVbox(VBox vbox) {
		mDevices[0].setupDebugGuiVbox(vbox);
	}

	public void updateDebugGuiVbox() {
		mDevices[0].updateDebugGuiVbox();
	}

	public List<SimulatorData.Type.Types> getDeviceTypeList() {
		List<SimulatorData.Type.Types> dtl = new ArrayList<>();
		dtl.add(SimulatorData.Type.Types.USB_MOTOR);
		return dtl;
	}

}