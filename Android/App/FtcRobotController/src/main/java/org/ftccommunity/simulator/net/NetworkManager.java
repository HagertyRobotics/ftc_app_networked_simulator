package org.ftccommunity.simulator.net;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedListMultimap;
import com.qualcomm.robotcore.util.RobotLog;

import org.ftccommunity.simulator.net.protocol.SimulatorData;
import org.ftccommunity.simulator.net.tasks.HeartbeatTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private static final ConcurrentLinkedQueue<SimulatorData.Data> receivedQueue = new ConcurrentLinkedQueue<>();
    private static final LinkedListMultimap<SimulatorData.Type.Types, SimulatorData.Data> main = LinkedListMultimap.create();
    private static final ConcurrentLinkedQueue<SimulatorData.Data> sendingQueue = new ConcurrentLinkedQueue<>();
    private static boolean serverWorking;

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
        SimulatorData.Data[] datas;
        synchronized (sendingQueue) {
            if (sendingQueue.size() > 0) {
                datas = sendingQueue.toArray(new SimulatorData.Data[sendingQueue.size()]);
                sendingQueue.clear();
            } else {
                datas = new SimulatorData.Data[1];
                datas[0] = HeartbeatTask.buildMessage();
            }
        }

            return datas;
    }

    /**
     * Returns the latest message recieved by the software
     * @param type type of message that is returned
     * @param block do we need to want for data, or just get whatever there is (including nothing)
     * @param cache do we want to cache the data (in case we need it again)
     * @return the last message recieved by the software of specific type
     * @throws InterruptedException
     */
    public static SimulatorData.Data getLatestMessage(@NotNull SimulatorData.Type.Types type,
                                                      boolean block, boolean cache) throws InterruptedException {
        if (block) {
            while (!Thread.currentThread().isInterrupted()) {
                if (main.get(type).size() > 0) { // Check if we can something
                    break;
                } else {
                    try {
                        Thread.sleep(10); // Good time to take a nap
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new InterruptedException();
                    }
                }
            } // End while
        }
        synchronized (main) {
            if (cache) {
                SimulatorData.Data data = main.get(type).get(main.get(type).size() - 1);
                if (data == null) {
                    return null;
                }

                // Cleanout the rest, since they should never be used, and put the cached message back in.
                clear(type);
                main.put(type, data);
                return data;
            } else {
                SimulatorData.Data data = main.get(type).remove(main.get(type).size() - 1);
                if (data == null) {
                    return null;
                }
                return data;
            }
        }
    }

    public static byte[] getLatestData(@NotNull SimulatorData.Type.Types type, boolean block) throws InterruptedException {
        SimulatorData.Data data = getLatestMessage(type, block, false);
        if (data == null) {
            return null;
        }
        return data.getInfo(0).getBytes(Charsets.US_ASCII);
    }

    public static byte[] getLatestData(@NotNull SimulatorData.Type.Types type, boolean block, boolean cache) throws InterruptedException {
        SimulatorData.Data data = getLatestMessage(type, block, cache);
        if (data == null) {
            return null;
        }
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
        Log.d("SIM_NETWORK_MANAGER::", "Adding a data of type: " +
                type.getValueDescriptor().getName());
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
    public synchronized static SimulatorData.Data[] getNextSends(final int size,
                                                                 final boolean autoShrink) {
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

    public enum NetworkTypes {
        BLUETOOTH,
        USB,
        WIFI
    }
}
