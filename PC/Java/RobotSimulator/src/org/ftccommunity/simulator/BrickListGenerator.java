package org.ftccommunity.simulator;

import com.google.protobuf.InvalidProtocolBufferException;
import org.ftccommunity.utils.Utils;
import org.ftccommunity.gui.MainApp;
import org.ftccommunity.simulator.data.AnalogSimData;
import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.NullSimData;
import org.ftccommunity.simulator.modules.*;
import org.ftccommunity.simulator.modules.devices.Device;
import org.ftccommunity.simulator.modules.devices.LegoLightSensorDevice;
import org.ftccommunity.simulator.modules.devices.NullDevice;
import org.ftccommunity.simulator.modules.devices.TetrixMotorControllerDevice;
import org.ftccommunity.simulator.modules.devices.TetrixServoControllerDevice;
import org.ftccommunity.simulator.modules.devices.USBMotorControllerDevice;

import javafx.collections.ObservableList;
import org.ftccommunity.simulator.net.manager.NetworkManager;
import org.ftccommunity.simulator.net.protocol.SimulatorData;
import sun.nio.ch.Net;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.io.ByteArrayOutputStream;
import java.net.NetworkInterface;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrickListGenerator implements Runnable {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    //DatagramSocket mServerSocket;

    byte[] mReceiveData = new byte[1024];
    byte[] mSendData = new byte[1024];

    MainApp mMainApp;

    public BrickListGenerator(MainApp mainApp) {
        mMainApp = mainApp;
    }

    @Override
    public void run() {
        byte[] packet;

        try {
            while (!Thread.currentThread().isInterrupted()) {
                packet = receivePacketFromPhone();
                NetworkManager.clear(SimulatorData.Type.Types.DEVICE_LIST);
                handleIncomingPacket(packet, false);
            }
            // Catch unhandled exceptions and cleanup
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    private byte[] receivePacketFromPhone() {
        return NetworkManager.getLatestData(SimulatorData.Type.Types.DEVICE_LIST, true);
    }

    private void sendPacketToPhone(String sendData) {
        try {
            NetworkManager.requestSend(SimulatorData.Type.Types.DEVICE_LIST, SimulatorData.Data.Modules.LEGACY_CONTROLLER, sendData);
//            logger.log(Level.FINER, "sendPacketToPhone: (" + Utils.bufferToHexString(sendData, 0, sendData.length()) +
//                                            ") len=" + sendData.length());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    public void handleIncomingPacket(byte[] data, boolean wait) {
        // System.out.println("Receive Buffer: (" + Utils.bufferToHexString(data, 0, 25) + ") len=" + data.length);

        // Wrap the device list in a data in order to be sent correctly
        if (data[0] == '?') {
            logger.log(Level.INFO, "Sending DEVICE_LIST...");
            sendPacketToPhone(getXmlModuleList(mMainApp.getBrickData()));
            //NetworkManager.requestSend(SimulatorData.Type.Types.DEVICE_LIST,
             //                                 SimulatorData.Data.Modules.LEGACY_CONTROLLER,
            //        getXmlModuleList(mMainApp.getBrickData()));
        }

    }

    private String getXmlModuleList(ObservableList<BrickSimulator> mBrickList) {
    	try {
	    	JAXBContext context = JAXBContext.newInstance(
	    			BrickListWrapper.class,
            		LegacyBrickSimulator.class,
            		MotorBrickSimulator.class,
            		ServoBrickSimulator.class,
            		Device.class,
            		NullDevice.class,
            		LegoLightSensorDevice.class,
            		TetrixMotorControllerDevice.class,
            		TetrixServoControllerDevice.class,
            		USBMotorControllerDevice.class,
            		MotorSimData.class,
            		AnalogSimData.class,
            		NullSimData.class
	    	);
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping our controller data.
            BrickListWrapper wrapper = new BrickListWrapper();
            wrapper.setBricks(mBrickList);

            // Marshalling to generate XML stream.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			m.marshal(wrapper, outputStream);
            return outputStream.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
            throw new AssertionError("JAXB should not be throwing", e.getCause());
        }

    }
}




