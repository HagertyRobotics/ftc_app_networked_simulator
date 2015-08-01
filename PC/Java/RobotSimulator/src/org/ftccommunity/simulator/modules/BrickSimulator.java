package org.ftccommunity.simulator.modules;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.ftccommunity.simulator.data.SimData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Model class for a Motor Controller, called "Brick" to avoid confusion with "Controller"
 *
 * @author Hagerty High
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BrickSimulator implements Runnable {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected final StringProperty alias;
    protected final StringProperty serial;
    protected IntegerProperty mPort;
    int mPhonePort;
    InetAddress mPhoneIPAddress;
    DatagramSocket mServerSocket;

    byte[] mReceiveData = new byte[1024];
    byte[] mSendData = new byte[1024];

    /** Default Constructor.
     *
     */
    public BrickSimulator() {
        this.alias = new SimpleStringProperty("");
        mPort = new SimpleIntegerProperty(0);
        serial = new SimpleStringProperty("");
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
    public abstract String getName();

	public abstract void setupDebugGuiVbox(VBox vbox);

	public abstract void updateDebugGuiVbox();

	public abstract void fixupUnMarshaling();

	public abstract void populateDetailsPane(Pane pane);

	public abstract SimData findSimDataName(String name);

	public abstract void handleIncomingPacket(byte[] data, int length, boolean wait);



    //---------------------------------------------------------------
    //
    // Getters and Setters for the marshaler and demarshaler
    //
    public String getAlias() {
        return alias.get();
    }

    @XmlElement
    public void setAlias(String alias) {
        this.alias.set(alias);
    }

    public Integer getPort() {
    	return mPort.get();
    }

    @XmlElement
    public void setPort(Integer port) {
    	mPort.set(port);
    }

    public String getSerial() {
        return serial.get();
    }

    @XmlElement
    public void setSerial(String serial) {
        this.serial.set(serial);
    }

    public StringProperty aliasProperty() {
        return alias;
    }

    public StringProperty serialProperty() {
        return serial;
    }



}

