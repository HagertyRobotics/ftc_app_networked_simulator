package com.ftdi.j2xx;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedListMultimap;

import org.ftccommunity.simulator.protobuf.SimulatorData;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public final class NetworkManager {
    // the Server's Port
    /*
    public static final int PHONE_PORT  = 6000;
    public static final String PC_IP_ADDRESS  = "192.168.2.189";
    public static final int SENDING_PORT = 6500;
    */
    //public static PendingWriteQueue mWriteToPcQueueNew;

    // private DatagramSocket mSimulatorSocket;
    private static LinkedBlockingQueue<SimulatorData.Data> mWriteToPcQueue = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<SimulatorData.Data> mReadFromPcQueue = new LinkedBlockingQueue<>();

    private static boolean serverWorking;

    private static LinkedListMultimap<SimulatorData.Type.Types, SimulatorData.Data> main = LinkedListMultimap.create();
    private static LinkedList<SimulatorData.Data> receivedQueue = new LinkedList<>();
    private static LinkedList<SimulatorData.Data> sendingQueue = new LinkedList<>();
    private static InetAddress robotAddress;
    private static boolean isReady;

    public NetworkManager(String ipAddress, int port) {
        /*// Start the Network Sender thread
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
        }*/
    }

    public static void add(@NotNull SimulatorData.Data data) {
        receivedQueue.add(data);
    }

    public synchronized static void processQueue() {
        for (SimulatorData.Data data : receivedQueue) {
            main.put(data.getType().getType(), data);
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
    }

    public static SimulatorData.Data[] getWriteData() {
        return (SimulatorData.Data[]) mWriteToPcQueue.toArray() ;
    }


    public static SimulatorData.Data getLatestMessage(@NotNull SimulatorData.Type.Types type) {
        return ((LinkedList<SimulatorData.Data>) main.get(type)).getLast();
    }

    public static byte[] getLatestData(@NotNull SimulatorData.Type.Types type) {
        SimulatorData.Data data = getLatestMessage(type);
        return data.getInfo(0).getBytes(Charsets.US_ASCII);
    }

    public static void clear(SimulatorData.Type.Types type) {
        main.get(type).clear();
    }

    public static void requestSend(SimulatorData.Type.Types type, SimulatorData.Data.Modules module, byte[] data) {
        SimulatorData.Data.Builder sendDataBuilder = SimulatorData.Data.newBuilder();
        sendDataBuilder.setType(SimulatorData.Type.newBuilder().setType(type).build())
                .setModule(module)
                .addInfo(new String(data, Charsets.US_ASCII));
        sendingQueue.add(sendDataBuilder.build());
    }

    public static SimulatorData.Data getNextSend() {
        if (sendingQueue.size() > 100) {
            LinkedList<SimulatorData.Data> temp = new LinkedList<>();
            for (int i = sendingQueue.size() - 1; i > sendingQueue.size() / 2; i--) {
                temp.add(sendingQueue.get(i));
            }
            sendingQueue = temp;
        }
        return sendingQueue.removeFirst();
    }

    public static SimulatorData.Data[] getNextSends() {
        return getNextSends(sendingQueue.size());
    }

    public static SimulatorData.Data[] getNextSends(int size) {
        return getNextSends(size, true);
    }

    public static SimulatorData.Data[] getNextSends(final int size, final boolean autoShrink) {
        int currentSize = size;
        if (currentSize <= sendingQueue.size() / 2) {
            cleanup();
        }

        if (size > sendingQueue.size() && !autoShrink) {
            throw new IndexOutOfBoundsException("Size is bigger then sending queue");
        }

        if (autoShrink) {
            if (size > sendingQueue.size()) {
                currentSize = sendingQueue.size();
            }
        }

        SimulatorData.Data[] datas = new SimulatorData.Data[currentSize];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = receivedQueue.removeLast();
        }

        return datas;
    }

    private static void cleanup() {
        if (sendingQueue.size() > 100) {
            LinkedList<SimulatorData.Data> temp = new LinkedList<>();
            for (int i = sendingQueue.size() - 1; i > sendingQueue.size() / 2; i--) {
                temp.add(sendingQueue.get(i));
            }
            sendingQueue = temp;
        }
    }

    public static InetAddress getRobotAddress() {
        return robotAddress;
    }

    public static void setRobotAddress(InetAddress robotAddress) {
        NetworkManager.robotAddress = robotAddress;
    }

    public static boolean isReady() {
        return isReady;
    }

    public static void changeReadiness(boolean isReady) {
        NetworkManager.isReady = isReady;
    }


    /**
     * NetworkRecevier
     * Receive packets from the PC simulator and feed them into a queue that the FT_Device class will read
     * The FT_Device class will pretend to be a FTDI USB device and feed the
     * received packets to the FTC_APP
     *
     *//*
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



    *//**
     * NetworkSender
     * Send packets to the PC simulator.
     * Pull the packets from the passed queue.  Packets were queued in the FT_Device class after being
     * received from the USB transmit functions of this app.  The FT_Device class is simulating the
     * USB stack using a UDP network connection to the PC simulator.
     *
     *//*
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

*/
}
