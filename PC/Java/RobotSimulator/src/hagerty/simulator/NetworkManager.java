package hagerty.simulator;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedListMultimap;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import hagerty.simulator.io.ClientHandler;
import hagerty.simulator.io.Decoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.ftccommunity.simulator.net.SimulatorData;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class NetworkManager {
    private static LinkedListMultimap<SimulatorData.Type.Types, SimulatorData.Data> main = LinkedListMultimap.create();
    private final static LinkedList<SimulatorData.Data> receivedQueue = new LinkedList<>();
    private static LinkedList<SimulatorData.Data> sendingQueue = new LinkedList<>();
    private static InetAddress robotAddress;
    private static boolean isReady;
    private static String host = "192.168.44.1";
    private static int port = 7002;
    static EventLoopGroup workerGroup = new NioEventLoopGroup();

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
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        synchronized (receivedQueue) {
            for (SimulatorData.Data data : receivedQueue) {
                main.put(data.getType().getType(), data);
            }
        }
    }

    /**
     * Find what the latest data is and returns it based on the type need
     * @param type the type of packet to get
     * @return The latest message in the queue, based on the type
     */
    @NotNull
    public static SimulatorData.Data getLatestMessage(@NotNull SimulatorData.Type.Types type) {
        while (!(main.get(type).size() > 0)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        synchronized (main) {
            return main.get(type).get(main.get(type).size());
        }
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
    public synchronized static void requestSend(SimulatorData.Type.Types type, SimulatorData.Data.Modules module, byte[] data) {
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
    public synchronized static SimulatorData.Data getNextSend() {
        if (sendingQueue.size() > 100) {
            LinkedList<SimulatorData.Data> temp = new LinkedList<>();
            for (int i = sendingQueue.size() - 1; i > sendingQueue.size() / 2; i--) {
                temp.add(sendingQueue.get(i));
            }
            sendingQueue = temp;
        }

        if (sendingQueue.size() > 0) {
            return sendingQueue.removeFirst();
        } else {
            return HeartbeatTask.buildMessage();
        }
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
    public synchronized static SimulatorData.Data[] getNextSends(final int size, final boolean autoShrink) {
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
    private synchronized static void cleanup() {
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

    public static void start() {
       Thread clientListener = new Thread(new Client());
        clientListener.start();
    }

    public static class Client implements Runnable {
        @Override
        public void run() {
            try {
                Bootstrap b = new Bootstrap(); // (1)
                b.group(workerGroup); // (2)
                b.channel(NioSocketChannel.class); // (3)
                b.option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true); // (4)
                b.handler(new io.netty.channel.ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(1, 1, 2), new Decoder(), new ClientHandler());
                    }
                });

                // Start the client.
                ChannelFuture f = b.connect(host, port).sync(); // (5)
                f.channel().closeFuture().sync();
                // ScheduledFuture g = f.channel().eventLoop().scheduleAtFixedRate(new HeartbeatTask(f.channel(), port), 0, 1, TimeUnit.SECONDS);

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                workerGroup.shutdownGracefully();
            }
            System.out.print("Server closed");
        }

    }

}

