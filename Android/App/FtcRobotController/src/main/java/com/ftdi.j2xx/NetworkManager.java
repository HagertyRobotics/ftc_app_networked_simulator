package com.ftdi.j2xx;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;


/**
 *
 */
public class NetworkManager {

    DatagramSocket mSocket;
    LinkedBlockingQueue mWriteToPcQueue = new LinkedBlockingQueue();
    LinkedBlockingQueue mReadFromPcQueue = new LinkedBlockingQueue();


    public NetworkManager(String ipAddress, int port) {
        // Start the Network Sender thread
        // This thread will read from the mWriteToPcQueue and send packets to the PC application
        // The mWriteToPcQueue will get packets from the FT_Device write call
        try {
            mSocket = new DatagramSocket(port);
            Log.v("D2xx::", "Local Port " + mSocket.getLocalPort());
            NetworkSender myNetworkSender = new NetworkSender(mWriteToPcQueue, ipAddress, mSocket, port);  // Runnable
            Thread networkSenderThread = new Thread(myNetworkSender);
            networkSenderThread.start();

            NetworkReceiver myNetworkReceiver = new NetworkReceiver(mReadFromPcQueue, mSocket);
            Thread networkReceiverThread = new Thread(myNetworkReceiver);
            networkReceiverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    LinkedBlockingQueue getWriteToPcQueue() {
        return mWriteToPcQueue;
    }

    LinkedBlockingQueue getReadFromPcQueue() {
        return mReadFromPcQueue;
    }



    /**
     * NetworkRecevier
     * Receive packets from the PC simulator and feed them into a queue that the FT_Device class will read
     * The FT_Device class will pretend to be a FTDI USB device and feed the
     * received packets to the FTC_APP
     *
     */
    public class NetworkReceiver implements Runnable {
        private LinkedBlockingQueue queue;
        DatagramSocket mSocket;
        byte[] mReceiveData = new byte[1024];

        public NetworkReceiver(LinkedBlockingQueue queue, DatagramSocket my_socket) {
            this.queue = queue;
            this.mSocket = my_socket;
        }


        @Override
        public void run() {

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
                try {
                    mSocket.receive(receivePacket);

                    byte[] readBuffer = new byte[receivePacket.getLength()];
                    System.arraycopy(receivePacket.getData(), 0, readBuffer, 0, receivePacket.getLength());
                    Log.v("D2xx::", "Receive: " + readBuffer[0] + " Len: " + receivePacket.getLength());
                    queue.put(readBuffer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
        InetAddress IPAddress;
        int mDestPort;

        public NetworkSender(LinkedBlockingQueue queue, String ipAddress, DatagramSocket mySocket, int destPort) {
            this.queue = queue;
            this.mSocket = mySocket;
            this.mDestPort = destPort;

            try {
                IPAddress = InetAddress.getByName(ipAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            byte[] writeBuf;

            while (true) {
                try {
                    writeBuf = (byte[])queue.take();
                    DatagramPacket send_packet = new DatagramPacket(writeBuf,writeBuf.length, IPAddress, mDestPort);
                    mSocket.send(send_packet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
