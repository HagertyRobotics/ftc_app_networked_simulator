package hagerty.simulator;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedListMultimap;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.ftccommunity.simulator.net.SimulatorData;

import java.net.InetAddress;
import java.util.LinkedList;

public final class NetworkManager {
    private static LinkedListMultimap<SimulatorData.Type.Types, SimulatorData.Data> main = LinkedListMultimap.create();
    private static LinkedList<SimulatorData.Data> receivedQueue = new LinkedList<>();
    private static LinkedList<SimulatorData.Data> sendingQueue = new LinkedList<>();
    private static InetAddress robotAddress;
    private static boolean isReady;

    /**
     * Add a recieved packet to the processing queue for deferred processing
     * @param data The data to add to processing queue
     */
    public static void add(@NotNull SimulatorData.Data data) {
        receivedQueue.add(data);
    }

    /**
     * Force the queue to sort the processing queue into their respective types
     */
    public synchronized static void processQueue() {
        for (SimulatorData.Data data : receivedQueue) {
            main.put(data.getType().getType(), data);
        }
    }

    /**
     * Find what the latest data is and returns it based on the type need
     * @param type the type of packet to get
     * @return The latest message in the queue, based on the type
     */
    @NotNull
    public static SimulatorData.Data getLatestMessage(@NotNull SimulatorData.Type.Types type) {
        return ((LinkedList<SimulatorData.Data>) main.get(type)).getLast();
    }

    /**
     *  This returns that data information based on the type specifed
     * @param type the type of data to get
     * @return a byte array of the latest data
     */
    @NotNull
    public static byte[] getLatestData(@NotNull SimulatorData.Type.Types type) {
        SimulatorData.Data data = getLatestMessage(type);
        return data.getInfo(0).getBytes(Charsets.US_ASCII);
    }

    /**
     * Clears the queue for a specific type
     * @param type the type of data to get
     */
    public static void clear(SimulatorData.Type.Types type) {
        main.get(type).clear();
    }

    /**
     * Request the packets to be sent, the sending does not have a guarantee to be sent
     * @param type the type of data to send
     * @param module which module correlates to the data being sent
     * @param data a byte array of data to send
     */
    public static void requestSend(SimulatorData.Type.Types type, SimulatorData.Data.Modules module, byte[] data) {
        SimulatorData.Data.Builder sendDataBuilder = SimulatorData.Data.newBuilder();
        sendDataBuilder.setType(SimulatorData.Type.newBuilder().setType(type).build())
                .setModule(module)
                .addInfo(new String(data, Charsets.US_ASCII));
        sendingQueue.add(sendDataBuilder.build());
    }

    /**
     * Gets the next data to send
     * @return the next data to send
     */
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

    /**
     * Rertrieve the next datas to send
     * @return an array of the entire sending queue
     */
    public static SimulatorData.Data[] getNextSends() {
        return getNextSends(sendingQueue.size());
    }

    /**
     * Rertieve an the next datas to send based on a specificed amount
     * @param size the maximum, inclusive size of the data array
     * @return a data array of the next datas to send up to a limit
     */
    public static SimulatorData.Data[] getNextSends(int size) {
        return getNextSends(size, true);
    }

    /**
     * Retrieve an array of the next datas to send up to a specific size
     * @param size the maximum size of the returned array
     * @param autoShrink if true this automatically adjusts the size returned
     * @return a data array of the next datas to send
     */
    public static SimulatorData.Data[] getNextSends(final int size, final boolean autoShrink) {
        int currentSize = size;
        if (currentSize <= sendingQueue.size() / 2) {
            cleanup();
        }

        if (size > sendingQueue.size() && !autoShrink) {
            throw new IndexOutOfBoundsException("Size is bigger then sending queue");
        }

        if (autoShrink) {
            if (size >  sendingQueue.size()) {
                currentSize = sendingQueue.size();
            }
        }

        SimulatorData.Data[] datas = new SimulatorData.Data[currentSize];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = receivedQueue.removeLast();
        }

        return datas;
    }

    /**
     * Cleanup the sending queue
     */
    private static void cleanup() {
        if (sendingQueue.size() > 100) {
            LinkedList<SimulatorData.Data> temp = new LinkedList<>();
            for (int i = sendingQueue.size() - 1; i > sendingQueue.size() / 2; i--) {
                temp.add(sendingQueue.get(i));
            }
            sendingQueue = temp;
        }
    }

    /**
     * The address of the Robot Controller
     * @return the robot controller IP address
     */
    @Nullable
    public static InetAddress getRobotAddress() {
        return robotAddress;
    }

    /**
     * Sets the current Robot IP address
     * @param robotAddress an <code>InetAddress</code> of the Robot Controller
     */
    public static void setRobotAddress(@NotNull InetAddress robotAddress) {
        NetworkManager.robotAddress = robotAddress;
    }

    /**
     * Returns if enough data has been received to start up
     * @return whether or not the robot server can start up
     */
    @NotNull
    public static boolean isReady() {
        return isReady;
    }

    /**
     * Change the readiness state of the Manager
     * @param isReady what the current status is of the network
     */
    public static void changeReadiness(boolean isReady) {
        NetworkManager.isReady = isReady;
    }
}
