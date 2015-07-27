package hagerty.simulator;

import hagerty.utils.*;
import hagerty.simulator.modules.BrickListWrapper;
import hagerty.simulator.modules.BrickSimulator;
import hagerty.simulator.modules.LegacyBrickSimulator;
import hagerty.simulator.modules.MotorBrickSimulator;
import hagerty.simulator.modules.ServoBrickSimulator;
import javafx.collections.ObservableList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class BrickListGenerator implements Runnable {


    DatagramSocket mServerSocket;

    byte[] mReceiveData = new byte[1024];
    byte[] mSendData = new byte[1024];

    hagerty.gui.MainApp mMainApp;

    /** Default Constructor.
     *
     */
    public BrickListGenerator(hagerty.gui.MainApp mainApp) {

    	mMainApp = mainApp;

        try {
        	mServerSocket = new DatagramSocket(7000);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
    	byte[] packet;

    	try {
            while (RobotSimulator.gThreadsAreRunning) {
            	packet = receivePacketFromPhone();
            	handleIncomingPacket(packet, packet.length, false);
            	System.out.println("ModuleLister");
            }
            // Catch unhandled exceptions and cleanup
    	} catch (Exception e) {
    		e.printStackTrace();
    		close();
    	}
    }

    public void close() {
    	try {
    		mServerSocket.close();
    	} catch (Exception ex) {
    		System.out.println("An error occurred while closing!");
    		ex.printStackTrace();
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
    	RobotSimulator.gPhonePort = receivePacket.getPort();
    	RobotSimulator.gPhoneIPAddress = receivePacket.getAddress();

    	// Make a copy of the packet.  Not sure if we need to do this.  Might not hold on to it for long.
    	byte[] mypacket = new byte[receivePacket.getLength()];
    	System.arraycopy(receivePacket.getData(), 0, mypacket, 0, receivePacket.getLength());

    	return mypacket;
    }

    private void sendPacketToPhone(byte[] sendData) {
    	try {
    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, RobotSimulator.gPhoneIPAddress, RobotSimulator.gPhonePort);
        	mServerSocket.send(sendPacket);
        	System.out.println("sendPacketToPhone: (" + Utils.bufferToHexString(sendData,0,sendData.length) + ") len=" + sendData.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleIncomingPacket(byte[] data, int length, boolean wait)
    {
    	System.out.println("Receive Buffer: (" + Utils.bufferToHexString(data,0,25) + ") len=" + data.length);

    	if (data[0] == '?') { // infoCmd
    		sendPacketToPhone(getXmlModuleList(mMainApp.getBrickData()));
        }
    }

    private byte[] getXmlModuleList(ObservableList<BrickSimulator> mBrickList) {
    	try {
	    	JAXBContext context = JAXBContext.newInstance(BrickListWrapper.class, LegacyBrickSimulator.class, MotorBrickSimulator.class, ServoBrickSimulator.class );
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	        // Wrapping our controller data.
	        BrickListWrapper wrapper = new BrickListWrapper();
	        wrapper.setBricks(mBrickList);

	        // Marshalling to generate XML stream.
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			m.marshal(wrapper, outputStream);
			return outputStream.toByteArray();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

    }

}




