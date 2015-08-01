package hagerty.simulator;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedListMultimap;
import com.sun.istack.internal.NotNull;
import org.ftccommunity.simulator.net.SimulatorData;

import java.net.InetAddress;
import java.util.LinkedList;

public final class NetworkManager {
    private static LinkedListMultimap<SimulatorData.Type.Types, SimulatorData.Data> main = LinkedListMultimap.create();
    private static LinkedList<SimulatorData.Data> receivedQueue = new LinkedList<>();
    private static LinkedList<SimulatorData.Data> sendingQueue = new LinkedList<>();
    private static InetAddress robotAddress;
    private static boolean isReady;

    public static void add(@NotNull SimulatorData.Data data) {
        receivedQueue.add(data);
    }

    public synchronized static void processQueue() {
        for (SimulatorData.Data data : receivedQueue) {
            main.put(data.getType().getType(), data);
        }
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
}
