package org.ftccommunity.simulator;

import org.ftccommunity.simulator.net.manager.NetworkManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

import static java.lang.Thread.currentThread;

public class ControllerSimulator implements Runnable {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    /*
     ** Packet types
     */
    protected final byte[] writeCmd = {85, -86, 0, 0, 0};
    protected final byte[] readCmd = {85, -86, -128, 0, 0};
    protected final byte[] recSyncCmd3 = {51, -52, 0, 0, 3};
    protected final byte[] recSyncCmd0 = {51, -52, -128, 0, 0};
    protected final byte[] recSyncCmd208 = {51, -52, -128, 0, (byte) 208};
    protected final byte[] controllerTypeLegacy = {0, 77, 73};       // Controller type USBLegacyModule
    protected final byte[] mCurrentStateBuffer = new byte[208];

    int mPhonePort;
    InetAddress mPhoneIPAddress;

    ControllerData mDataPacket;
    byte[] mReceiveData = new byte[1024];
    byte[] mSendData = new byte[1024];
    private LinkedBlockingQueue<ControllerData> mQueue;
    private volatile boolean running = true;

    private int packetsReceived;
    private int packetsSent;

    public ControllerSimulator(LinkedBlockingQueue<ControllerData> queue) throws
            IOException {

        packetsReceived = 0;
        packetsSent = 0;

        mQueue = queue;
    }

    @Override
    public void run() {
        logger.log(Level.FINE, "Controller Sim. Started");
        byte[] packet;

        try {
            while(!currentThread().isInterrupted() && running) {
                packet = receivePacketFromPhone();
                handleIncomingPacket(packet);
                logger.log(Level.FINEST, "Received packet.");
            }
            //Catch unhandled exceptions and cleanup
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    private byte[] receivePacketFromPhone() {
        packetsReceived++;
    	return NetworkManager.getLatestData(SimulatorData.Type.Types.SIM_DATA);
    }


    private void sendPacketToPhone(byte[] sendData) {
        NetworkManager.requestSend(SimulatorData.Type.Types.SIM_DATA, SimulatorData.Data.Modules.LEGACY_CONTROLLER, sendData);
        logger.log(Level.FINER, "sendPacketToPhone: (" + bufferToHexString(sendData, 0, sendData.length) + ") length=" + sendData.length);
    }


    public void handleIncomingPacket(byte[] data) {
        logger.log(Level.FINER, "Receive Buffer: (" + bufferToHexString(data, 0, 25) + ") length=" + data.length);

        if (data[0] == readCmd[0] && data[2] == readCmd[2] && data[4] == (byte) 208) { // readCmd
            sendPacketToPhone(mCurrentStateBuffer);
            // Set the Port S0 ready bit in the global part of the Current State Buffer
            mCurrentStateBuffer[3] = (byte) 0xfe;  // Port S0 ready
        } else {
            // Write Command
            // Process the received data packet
            // Loop through each of the 6 ports and see if the Action flag is set.
            // If set then copy the 32 bytes for the port into the CurrentStateBuffer

            //for (int i=0;i<6;i++)
            int i = 0;
            int p = 16 + i * 32;
            // This is for Port P0 only.  16 is the base offset.  Each port has 32 bytes.
            // If I2C_ACTION is set, take some action
            //        if (data[p+32] == (byte)0xff) { // Action flag
            if ((data[p] & (byte) 0x01) == (byte) 0x01) { // I2C Mode
                if ((data[p] & (byte) 0x80) == (byte) 0x80) { // Read mode
                    // Copy this port's 32 bytes into buffer
                    System.arraycopy(data, p, mCurrentStateBuffer, p, 32);

                } else {
                    // Write mode
                    // Copy this port's 32 bytes into buffer
                    System.arraycopy(data, p, mCurrentStateBuffer, p, 32);


                    ControllerData cd = new ControllerData(2);
                    if (mCurrentStateBuffer[p + 4 + 5] == (byte) 0x80) {
                        cd.setFloatMode(1, true);
                    } else {
                        float m1 = (float) mCurrentStateBuffer[p + 4 + 5] / 100.0f;
                        cd.setMotorSpeed(1, m1);
                    }

                    if (mCurrentStateBuffer[p + 4 + 6] == (byte) 0x80) {
                        cd.setFloatMode(2, true);
                    } else {
                        float m1 = (float) mCurrentStateBuffer[p + 4 + 6] / 100.0f;
                        cd.setMotorSpeed(2, m1);
                    }
                    for (int j = 0; j < 3; j++) {
                        try {
                            mQueue.add(cd);
                            break;
                        } catch (java.lang.IllegalStateException ex) {
                            System.out.print("An error occurred while trying to add to queue. Retrying...");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException interrupt) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                }

            }
        }
    }


    private String bufferToHexString(byte[] data, int start, int length) {
        int i;
        int myStop;
        StringBuilder sb = new StringBuilder();
        myStop = (length > data.length) ? data.length : length;
        for (i=start; i<start+myStop; i++) {
            sb.append(String.format("%02x ", data[i]));
        }
        return sb.toString();
    }

    public  void requestTerminate() {
        running = false;
    }

    public synchronized int getPacketsReceived() {
        return packetsReceived;
    }

    public synchronized int getPacketsSent() {
        return packetsSent;
    }

    public synchronized void resetCount() {
        packetsSent = 0;
        packetsReceived = 0;
    }
}




