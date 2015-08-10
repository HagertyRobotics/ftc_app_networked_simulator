package org.ftccommunity.simulator;

import org.ftccommunity.gui.MainApp;
import org.ftccommunity.simulator.modules.BrickSimulator;
import org.ftccommunity.simulator.net.manager.NetworkManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RobotSimulator  {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static BrickListGenerator gBrickListGenerator;
    private static CoppeliaApiClient gCoppeliaApiClient;
    private static volatile boolean gThreadsAreRunning = true;
    private static LinkedList<Thread> threadLinkedList = new LinkedList<>();
    private static int gPhonePort;
    private static InetAddress gPhoneIPAddress;

    private static boolean simulatorStarted = false;
    private static boolean visualizerStarted = false;

    static public void startSimulator(MainApp mainApp) {
        Thread processQueue = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                NetworkManager.processQueue();
            }
        });
        processQueue.start();
        simulatorStarted = true;

        System.out.println("Starting Module Lister...");
        gBrickListGenerator = new BrickListGenerator(mainApp);  // Runnable
        Thread moduleListerThread = new Thread(gBrickListGenerator, "Brick List Generator");
        moduleListerThread.start();
        NetworkManager.start();

        final boolean useMulticast = false;
        if (useMulticast) {
            System.out.println("Seeing if we can get a multicast...");
            Thread multicastListener = new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(7003);
                    } catch (SocketException e) {
                        logger.log(Level.SEVERE, e.toString());
                    }

                    while (!Thread.currentThread().isInterrupted() && socket != null) {
                        DatagramPacket packet = new DatagramPacket(new byte[1], 1);
                        try {
                            socket.receive(packet);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, e.toString());
                        }
                        System.out.println(packet.getAddress().getHostAddress());
                    }
                }
            });
            multicastListener.start();
        }

        // Start the individual threads for each module
        // Read the current list of modules from the GUI MainApp class
        List<BrickSimulator> brickList = mainApp.getBrickData();

        for (BrickSimulator temp : brickList) {
            Thread t = new Thread(temp, temp.getName());  // Make a thread from the object and also set the process name
            t.start();
            threadLinkedList.add(t);
            System.out.println(temp.getName() + " " + temp.getName());
        }
    }

    static public boolean simulatorStarted() {
        return simulatorStarted;
    }

    static public void startVisualizer(MainApp mainApp) {
        visualizerStarted = true;

        // Start the module info server
        System.out.println("Starting Visualizer...");
        gCoppeliaApiClient = new CoppeliaApiClient(mainApp); // Runnable
        Thread coppeliaThread = new Thread(gCoppeliaApiClient);
        if (gCoppeliaApiClient.init()) {
            coppeliaThread.start();
        } else {
            System.out.println("Initialization of Visualizer failed");
        }
    }

    static public boolean visualizerStarted() {
        return visualizerStarted;
    }

    public static boolean isgThreadsAreRunning() {
        return gThreadsAreRunning;
    }

    public static void requestTermination() {
        gThreadsAreRunning = false;
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, ex.toString());
        }
        threadLinkedList.forEach(Thread::interrupt);
        Thread.currentThread().interrupt();
    }

    public static int getPhonePort() {
        return gPhonePort;
    }

    public static InetAddress getPhoneIPAddress() {
        return gPhoneIPAddress;
    }

    public static void setPhoneConnectionDetails(int phonePort, InetAddress phoneIpAddress) {
        gPhonePort = phonePort;
        gPhoneIPAddress = phoneIpAddress;
    }
}