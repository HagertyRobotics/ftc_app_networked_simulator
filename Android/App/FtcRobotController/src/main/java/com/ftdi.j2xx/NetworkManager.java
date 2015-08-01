package com.ftdi.j2xx;

import android.util.Log;

import org.ftccommunity.simulator.protobuf.SimulatorData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.channel.PendingWriteQueue;


/**
 *
 */
public class NetworkManager {
    // the Server's Port
    public static final int PHONE_PORT  = 6000;
    public static final String PC_IP_ADDRESS  = "192.168.2.189";
    public static final int SENDING_PORT = 6500;
    public static PendingWriteQueue mWriteToPcQueueNew;

    private DatagramSocket mSimulatorSocket;
    private static LinkedBlockingQueue<SimulatorData.Data> mWriteToPcQueue = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<SimulatorData.Data> mReadFromPcQueue = new LinkedBlockingQueue<>();

    private static boolean serverWorking;


    public NetworkManager(String ipAddress, int port) {
        // Start the Network Sender thread
        // This thread will read from the mWriteToPcQueue and send packets to the PC application
        // The mWriteToPcQueue will get packets from the FT_Device write call
        try {
            mSimulatorSocket = new DatagramSocket(PHONE_PORT);
            Log.v("D2xx::", "Local Port " + mSimulatorSocket.getLocalPort());
            NetworkSender myNetworkSender = new NetworkSender(mWriteToPcQueue,
                    PC_IP_ADDRESS, mSimulatorSocket, SENDING_PORT);  // Runnable
            Thread networkSenderThread = new Thread(myNetworkSender);
            networkSenderThread.start();

            NetworkReceiver myNetworkReceiver = new NetworkReceiver(mReadFromPcQueue, mSimulatorSocket);
            Thread networkReceiverThread = new Thread(myNetworkReceiver);
            networkReceiverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isServerWorking() {
        return serverWorking;
    }

    public static void setServerWorking(boolean serverWorking) {
        NetworkManager.serverWorking = serverWorking;
    }


    public static LinkedBlockingQueue<SimulatorData.Data> getWriteToPcQueue() {
        return mWriteToPcQueue;
    }

    public static LinkedBlockingQueue<SimulatorData.Data> getReadFromPcQueue() {
        return mReadFromPcQueue;
    };

    public static SimulatorData.Data[] getWriteData() {
        return (SimulatorData.Data[]) mWriteToPcQueue.toArray() ;
    }



    /**
     * NetworkRecevier
     * Receive packets from the PC simulator and feed them into a queue that the FT_Device class will read
     * The FT_Device class will pretend to be a FTDI USB device and feed the
     * received packets to the FTC_APP
     *
     */
    public class NetworkReceiver implements Runnable {
        private LinkedBlockingQueue<SimulatorData.Data> queue;
        DatagramSocket mSocket;
        byte[] mReceiveData = new byte[1024];

        public NetworkReceiver(LinkedBlockingQueue<SimulatorData.Data> queue, DatagramSocket my_socket) {
            this.queue = queue;
            this.mSocket = my_socket;
        }


        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
                try {
                    mSocket.receive(receivePacket);

                    byte[] readBuffer = new byte[receivePacket.getLength()];
                    System.arraycopy(receivePacket.getData(), 0, readBuffer, 0, receivePacket.getLength());
                    Log.v("D2xx::", "Receive: " + readBuffer[0] + "Len: " + receivePacket.getLength());
                    queue.put(readBuffer);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * NetworkSender
     * Send packets to the PC simulator.
     * Pull the packets from the passed queue.  Packets were queued in the FT_Device class after being
     * received from the USB transmit functions of this app.  The FT_Device class is simulating the
     * USB stack using a UDP network connection to the PC simulator.
     *
     */
    public class NetworkSender implements Runnable {
        private LinkedBlockingQueue queue;

        DatagramSocket mSocket;
        int mDestPort;
        private InetAddress IPAddress;

        public NetworkSender(LinkedBlockingQueue queue, String ipAddress, DatagramSocket mySocket, int destPort) {
            this.queue = queue;
            this.mSocket = mySocket;
            this.mDestPort = destPort;

            try {
                this.IPAddress = InetAddress.getByName(ipAddress);
            } catch (IOException e) {
                Log.e("FTC Controller", "The following ip address is not invalid: " +
                        ipAddress + "Details: " + e.getMessage(), e);
                throw new AssertionError("IP Address is invalid!");
            }

        }

        @Override
        public void run() {
            byte[] writeBuf;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    writeBuf = (byte[])queue.take();
                    DatagramPacket send_packet = new DatagramPacket(writeBuf,writeBuf.length, IPAddress, SENDING_PORT);
                    mSocket.send(send_packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void buildConnection() {

        }
    }


}
