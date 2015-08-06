package org.ftccommunity.simulator.modules;


import org.ftccommunity.simulator.data.SimData;
import org.ftccommunity.simulator.modules.devices.Device;
import org.ftccommunity.simulator.modules.devices.DeviceFactory;
import org.ftccommunity.simulator.modules.devices.DeviceType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

/**
 * Model class for a Motor Controller, called "Brick" to avoid confusion with "Controller"
 *
 * @author Hagerty High
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BrickSimulator implements Runnable {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected String mType;
    protected int mNumberOfPorts;

    protected final StringProperty mName;
    protected final StringProperty mSerial;
    protected IntegerProperty mPort;
    protected int mPhonePort;
    protected InetAddress mPhoneIPAddress;
    protected DatagramSocket mServerSocket;
    protected String mFXMLFileName;

    @XmlElementRef(name="Devices")
    protected Device[] mDevices;

    byte[] mReceiveData = new byte[1024];
    byte[] mSendData = new byte[1024];

    /** Default Constructor.
     *
     */
    public BrickSimulator() {
        mName = new SimpleStringProperty("");
        mPort = new SimpleIntegerProperty(0);
        mSerial = new SimpleStringProperty("");
    }


    @Override
    public void run() {
    	byte[] packet;
        try {
        	mServerSocket = new DatagramSocket(mPort.intValue());

            while (!Thread.currentThread().isInterrupted()) {
                packet = receivePacketFromPhone();
            	handleIncomingPacket(packet, packet.length, false);
            }
            // Catch unhandled exceptions and cleanup
    	} catch (Exception e) {
    		e.printStackTrace();
    		close();
    	}
    }

    private byte[] receivePacketFromPhone() {
    	DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
    	try {
    		mServerSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

    	// Get the port and address of the sender from the incoming packet and set some global variables
    	// to be used when we reply back.
    	// TODO: do we need to set this every time?
    	mPhonePort = receivePacket.getPort();
    	mPhoneIPAddress = receivePacket.getAddress();

    	// Make a copy of the packet.  Not sure if we need to do this.  Might not hold on to it for long.
    	byte[] mypacket = new byte[receivePacket.getLength()];
    	System.arraycopy(receivePacket.getData(), 0, mypacket, 0, receivePacket.getLength());

    	return mypacket;
    }

    public void close() {
    	try {
    		mServerSocket.close();
    	} catch (Exception ex) {
    		System.out.println("An error occurred while closing!");
    		ex.printStackTrace();
    	}
    }

	public abstract void setupDebugGuiVbox(VBox vbox);

	public abstract void updateDebugGuiVbox();

	public abstract void populateDetailsPane(Pane pane);

	public abstract void handleIncomingPacket(byte[] data, int length, boolean wait);

	public abstract List<DeviceType> getDeviceTypeList();


    //---------------------------------------------------------------
    //
    // Getters and Setters
    //

	public int getNumberOfPorts() {
		return mNumberOfPorts;
	}

    public String getType() {
        return mType;
    }

    public String getName() {
        return mName.get();
    }

    @XmlElement
    public void setName(String name) {
        mName.set(name);
    }

    public Integer getPort() {
    	return mPort.get();
    }

    @XmlElement
    public void setPort(Integer port) {
    	mPort.set(port);
    }

    public String getSerial() {
        return mSerial.get();
    }

    @XmlElement
    public void setSerial(String serial) {
        mSerial.set(serial);
    }

    public StringProperty nameProperty() {
        return mName;
    }

    public StringProperty serialProperty() {
        return mSerial;
    }

    public Device getPortDevice(int i) {
    	// TODO add try catch for device length
    	return mDevices[i];
    }

    public DeviceType getPortDeviceType(int i) {
    	return mDevices[i].getType();
    }

    public void setPortDeviceType(int i, DeviceType type) {
    	mDevices[i] = DeviceFactory.buildDevice(type);
    }

    public String getFXMLFileName() {
    	return mFXMLFileName;
    }

    public Device[] getDevices() {
    	return mDevices;
    }

    public void setDevices(Device[] dev) {
    	mDevices = dev;
    }

    /**
    *
    */
    public SimData findSimDataByName(String name) {
	   for (Device d: mDevices) {
		   SimData s = d.findSimDataByName(name);
		   if (s != null) {
			   return s;
		   }
	   }
//   	for (int i=0;i<mNumberOfPorts;i++) {
//   		if (portName[i] != null) {
//   			if (portName[i].equals(name)) {
//   				return portSimData[i];
//   			}
//   		}
//   	}
	   return null;
    }
}

