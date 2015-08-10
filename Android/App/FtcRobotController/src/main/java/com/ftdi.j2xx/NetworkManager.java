package com.ftdi.j2xx;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedListMultimap;
import com.qualcomm.robotcore.util.RobotLog;

import org.ftccommunity.simulator.net.protocol.SimulatorData;
import org.ftccommunity.simulator.net.tasks.HeartbeatTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private static final ConcurrentLinkedQueue<SimulatorData.Data> receivedQueue = new ConcurrentLinkedQueue<>();
    private static final LinkedListMultimap<SimulatorData.Type.Types, SimulatorData.Data> main = LinkedListMultimap.create();
    private static final ConcurrentLinkedQueue<SimulatorData.Data> sendingQueue = new ConcurrentLinkedQueue<>();
    private static boolean serverWorking;
    private static InetAddress robotAddress;
    private static volatile boolean isReady;

    public NetworkManager(NetworkTypes type) {
        if (type == NetworkTypes.BLUETOOTH) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.startDiscovery();
            while (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            LinkedList<BluetoothDevice> devices = new LinkedList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
            BluetoothDevice main;
            for (BluetoothDevice device : devices) {
                RobotLog.i(device.toString());
            }
            if (devices.size() > 0) {
                main = devices.getLast();
            } else {
                throw new IllegalStateException("No bonded devices found, but bluetooth mode is enabled");
            }
            UUID bluetoothUuid = UUID.randomUUID();
            BluetoothServerSocket socket = null;
            RobotLog.w("Use the following UUID to connect " + bluetoothUuid.toString());
            try {
                socket = adapter.listenUsingRfcommWithServiceRecord("FTC_Sim", bluetoothUuid);
            } catch (IOException e) {
                RobotLog.e(e.toString());
            }
            if (socket == null) {
                throw new NullPointerException("Socket never got initialized");
            }
            BluetoothSocket mainSocket;
            try {
                socket.accept();
            } catch (IOException e) {
                RobotLog.e(e.toString());
            }
        }

        Thread processThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    processQueue();
                }
            }
        }, "Process Queue");
        processThread.start();
    }

    public static void add(@NotNull SimulatorData.Data data) {
        synchronized (receivedQueue) {
            receivedQueue.add(data);
        }
    }

    public synchronized static void processQueue() {
        while (receivedQueue.size() < 1) {
            try {
                Thread.sleep(10);
                Thread.yield();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ConcurrentLinkedQueue<SimulatorData.Data> temp = new ConcurrentLinkedQueue<>();
        // Move the contents over to an new queue
        synchronized (receivedQueue) {
            temp.addAll(receivedQueue);
            receivedQueue.clear();
        }

        // Flip the old moved data into a new container
        LinkedList<SimulatorData.Data> tempB = new LinkedList<>();
        while (!temp.isEmpty()) {
            tempB.add(temp.poll());
        }
        temp.clear();

        // Then, add the flipped data so the oldest gets processed first
        while (!tempB.isEmpty()) {
            SimulatorData.Data data = tempB.poll();
            main.put(data.getType().getType(), data);
        }
        tempB.clear();
    }

    public static boolean isServerWorking() {
        return serverWorking;
    }

    public static void setServerWorking(boolean serverWorking) {
        NetworkManager.serverWorking = serverWorking;
    }

    public static SimulatorData.Data[] getWriteData() {
        synchronized (sendingQueue) {
            return sendingQueue.toArray(new SimulatorData.Data[sendingQueue.size()]);
        }
    }
/*
 /*   *//*
*
     * NetworkRecevier
     * Receive packets from the PC simulator and feed them into a queue that the FT_Device class will read
     * The FT_Device class will pretend to be a FTDI USB device and feed the
     * received packets to the FTC_APP
     *
     *//*
/*
    public class NetworkReceiver implements Runnable {
        private LinkedBlockingQueue queue;
        DatagramSocket mSocket;
        byte[] mReceiveData = new byte[1024];

        public NetworkReceiver(LinkedBlockingQueue queue, DatagramSocket my_socket) {
            this.queue = queue;
            this.mSocket = my_socket;
>>>>>>> develop
        }
    }
*/

    public static SimulatorData.Data getLatestMessage(@NotNull SimulatorData.Type.Types type, boolean block) {
        if (block) {
            while (!Thread.currentThread().isInterrupted()) {
                if (main.get(type).size() > 0) {
                    synchronized (main) {
                        // return main.get(type).remove(main.get(type).size() - 1);
                        return main.get(type).remove(main.get(type).size() - 1);
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

/*
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
>>>>>>> develop
                }
            }
        }*/

        }
        synchronized (main) {
            return main.get(type).remove(main.get(type).size() - 1);
        }
    }

    public static byte[] getLatestData(@NotNull SimulatorData.Type.Types type, boolean block) {
        SimulatorData.Data data = getLatestMessage(type, block);
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

    /**
     * Gets the next data to send
     *
     * @return the next data to send
     */
    public synchronized static SimulatorData.Data getNextSend() {
        if (sendingQueue.size() > 100) {
            ConcurrentLinkedQueue<SimulatorData.Data> temp = new ConcurrentLinkedQueue<>();
            for (int i = sendingQueue.size() - 1; i > sendingQueue.size() / 2; i--) {
                temp.add(sendingQueue.poll());
            }
            sendingQueue.clear();
            sendingQueue.addAll(temp);
        }

        if (sendingQueue.size() > 0) {
            return sendingQueue.poll();
        } else {
            return HeartbeatTask.buildMessage();
        }
    }

    /**
     * Rertrieve the next datas to send
     *
     * @return an array of the entire sending queue
     */
    public static SimulatorData.Data[] getNextSends() {
        return getNextSends(sendingQueue.size());
    }

    /**
     * Rertieve an the next datas to send based on a specificed amount
     *
     * @param size the maximum, inclusive size of the data array
     * @return a data array of the next datas to send up to a limit
     */
    public static SimulatorData.Data[] getNextSends(int size) {
        return getNextSends(size, true);
    }

    /**
     * Retrieve an array of the next datas to send up to a specific size
     *
     * @param size       the maximum size of the returned array
     * @param autoShrink if true this automatically adjusts the size returned
     * @return a data array of the next datas to send
     */
    public synchronized static SimulatorData.Data[] getNextSends(final int size, final boolean autoShrink) {
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
        synchronized (sendingQueue) {
            for (int i = 0; i < datas.length; i++) {
                datas[i] = sendingQueue.poll();
            }
        }

        return datas;
    }

    /**
     * Cleanup the sending queue
     */
    private synchronized static void cleanup() {
        if (sendingQueue.size() > 100) {
            ConcurrentLinkedQueue<SimulatorData.Data> temp = new ConcurrentLinkedQueue<>();
            synchronized (sendingQueue) {
                for (int i = sendingQueue.size() - 1; i > sendingQueue.size() / 2; i--) {
                    temp.add(sendingQueue.poll());
                    sendingQueue.clear();
                }
                sendingQueue.addAll(temp);
            }
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

    public enum NetworkTypes {
        BLUETOOTH,
        USB,
        WIFI
    }
}
