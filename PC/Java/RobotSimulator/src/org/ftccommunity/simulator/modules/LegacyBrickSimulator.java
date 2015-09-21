package org.ftccommunity.simulator.modules;


import org.ftccommunity.simulator.modules.devices.Device;
import org.ftccommunity.simulator.modules.devices.DeviceType;
import org.ftccommunity.simulator.modules.devices.NullDevice;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

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
@XmlRootElement(name="Legacy")
public class LegacyBrickSimulator extends BrickSimulator {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected final byte[] mCurrentStateBuffer = new byte[208];
    protected final byte[] temp15 = new byte[15];
    protected final byte[] temp12 = new byte[12];

    /*
     ** Packet types
     */
    protected static final byte PACKET_HEADER_0 = (byte)0x55;
    protected static final byte PACKET_HEADER_1 = (byte)0xaa;
    protected static final byte PACKET_WRITE_FLAG = (byte)0x00;
    protected static final byte PACKET_READ_FLAG = (byte)0x80;

    /**
     * Default constructor.
     */
    public LegacyBrickSimulator() {
    	mType = "Core Legacy Module";
    	mFXMLFileName = "view/EditDialog.fxml";
    	mNumberOfPorts = 6;
    	mDevices = new Device[mNumberOfPorts];
    	for (int i=0;i<mNumberOfPorts;i++) {
    		mDevices[i] = new NullDevice();
    	}
    }

    private void sendPacketToPhone(byte[] sendData) {

		try {
			os.write(sendData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    public void handleIncomingPacket(byte[] data, int length, boolean wait)
    {
    	if (data[0] == PACKET_HEADER_0 && data[1] == PACKET_HEADER_1) { // valid packet

	    	if (data[2] == PACKET_READ_FLAG) { // read command
	    		byte[] tempBuffer = new byte[data[4]];
	    		System.arraycopy(mCurrentStateBuffer, Byte.toUnsignedInt(data[3]), tempBuffer, 0, tempBuffer.length);
	    		sendPacketToPhone(tempBuffer);
	    		//temp15[3] = (byte)0xfe;
	        } else { // write command
	        	int offset = Byte.toUnsignedInt(data[3]);
	        	int len = Byte.toUnsignedInt(data[4]);
	        	System.arraycopy(data, 5, mCurrentStateBuffer, offset, len);
	        	// Loop through each ports and process the buffer
		        for (int i=0;i<mNumberOfPorts;i++) {
		        	mDevices[i].processBuffer(mCurrentStateBuffer, i);
		        }
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
//		ColumnConstraints col1 = new ColumnConstraints();
//		col1.setPercentWidth(20);
//		ColumnConstraints col2 = new ColumnConstraints();
//		col2.setPercentWidth(20);
//		ColumnConstraints col3 = new ColumnConstraints();
//		col3.setPercentWidth(20);
//		ColumnConstraints col4 = new ColumnConstraints();
//		col4.setPercentWidth(20);
//		ColumnConstraints col5 = new ColumnConstraints();
//		col5.setPercentWidth(20);
//		grid.getColumnConstraints().addAll(col1,col2,col3,col4,col5);

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


    /**
     * Getters/Setters
     */



    /**
     * GUI Stuff
     */
	public void setupDebugGuiVbox(VBox vbox) {

		for (int i=0;i<mNumberOfPorts;i++) {
			mDevices[i].setupDebugGuiVbox(vbox);
		}
	}

	public void updateDebugGuiVbox() {

		for (int i=0;i<mNumberOfPorts;i++) {
				mDevices[i].updateDebugGuiVbox();
		}
	}

	public List<DeviceType> getDeviceTypeList() {
		List<DeviceType> dtl = new ArrayList<>();
		dtl.add(DeviceType.NONE);
		dtl.add(DeviceType.TETRIX_MOTOR);
		dtl.add(DeviceType.TETRIX_SERVO);
		dtl.add(DeviceType.LEGO_LIGHT);
		return dtl;
	}


}