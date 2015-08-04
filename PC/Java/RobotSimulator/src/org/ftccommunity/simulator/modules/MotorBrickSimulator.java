package org.ftccommunity.simulator.modules;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.SimData;
import org.ftccommunity.simulator.modules.devices.Device;
import org.ftccommunity.simulator.modules.devices.DeviceFactory;
import org.ftccommunity.simulator.modules.devices.DeviceType;
import org.ftccommunity.simulator.modules.devices.NullDevice;
import org.ftccommunity.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
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
    protected final String mType = "Core Motor Controller";
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
    	mFXMLFileName = "view/EditMotorDialog.fxml";
    	mDevices = new Device[1];
    	mDevices[0] = new NullDevice();
    }

    private void sendPacketToPhone(byte[] sendData) {
    	try {
    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mPhoneIPAddress, mPhonePort);
        	mServerSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleIncomingPacket(byte[] data, int length, boolean wait)
    {
    	if (data[0] == readCmd[0] && data[2] == readCmd[2] && data[4] == (byte)94) { // readCmd
    		sendPacketToPhone(mCurrentStateBuffer);
        } else {
	    	// Process the received data packet
        	mDevices[0].processBuffer(data, mCurrentStateBuffer );
        }
    }


    /**
     * For the LegacyBrickSimulator objects, since we couldn't get the marshaler to handle the list of small
     * SimData objects(6), we created and marshaled a list of the six port types and names.  We now need to create
     * the objects by hand using the unmarshaled list of portTypes and portNames.
     */
    public void fixupUnMarshaling() {

    }

    /**
     *
     */
    public SimData findSimDataByName(String name) {
//		if (motor1Name.equals(name)) {
//			return portSimData;
//		} else 	if (motor2Name.equals(name)) {
//			return portSimData;
//		}
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

/*		Text motor1Text = new Text("Motor 1");
		grid.add(motor1Text, 0, 0);
		Text motor1NameText = new Text(motor1Name);
		grid.add(motor1NameText, 1,  0);

		Text motor2Text = new Text("Motor 2");
		grid.add(motor1Text, 0, 0);
		Text motor2NameText = new Text(motor2Name);
		grid.add(motor2NameText, 1,  0);
*/
		pane.getChildren().add(grid);
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

}