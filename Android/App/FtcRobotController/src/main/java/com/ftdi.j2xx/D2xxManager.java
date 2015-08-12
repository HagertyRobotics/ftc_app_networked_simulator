package com.ftdi.j2xx;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.net.InetAddresses;
import com.qualcomm.robotcore.util.RobotLog;

import org.ftccommunity.simulator.Server;
import org.ftccommunity.simulator.net.protocol.SimulatorData;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class D2xxManager
{
    protected static final String ACTION_USB_PERMISSION = "com.ftdi.j2xx";
    private static final String TAG = "D2xx::";
    private static D2xxManager mInstance = null;
    public boolean useWifi;
    public boolean multicast;
    public boolean simulate;

    private ArrayList<FT_Device> mFtdiDevices;
    private Server server;

    private D2xxManager(Context parentContext)
            throws D2xxManager.D2xxException
    {
        Log.v(TAG, "Start constructor");
        Log.v(TAG, "Init Server");
        server = new Server(7002);
        Thread serverThread = new Thread(server);
        serverThread.start();

        new NetworkManager(NetworkManager.NetworkTypes.WIFI);

        if (parentContext == null) {
            throw new D2xxException("D2xx init failed: Can not find parentContext!");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parentContext);
        useWifi = preferences.getBoolean("sim_use_wifi", true);
        simulate = preferences.getBoolean("pref_simulatorEnabled", true);
        multicast= preferences.getBoolean("sim_net_multicast", true);

        // Multicast
        if (multicast) {
            Thread multicastServer = new Thread(new Runnable() {
                DatagramSocket socket;
                @Override
                public void run() {
                    try {
                        socket = new DatagramSocket(7003);
                    } catch (SocketException e) {
                        RobotLog.e(e.toString());
                    }
                    // Always multicast every 100 milliseconds
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            socket.send(new DatagramPacket(new byte[1], 1,
                                    InetAddresses.forString("255.255.255.255"), 7003));
                        } catch (IOException e) {
                            RobotLog.e(e.toString());
                            RobotLog.e("Stopping Multicast System!");
                            break;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }, "Multicast Server");
            multicastServer.start();
        }

        Log.v("D2xx::", "End constructor");
    }

    public static synchronized D2xxManager getInstance(Context parentContext)
            throws D2xxManager.D2xxException
    {
        if (mInstance == null) mInstance = new D2xxManager(parentContext);

        return mInstance;
    }

    public int createDeviceInfoList(Context parentContext)
    {
        FT_Device ftDev = null;
        int rc = 0;
        byte[] receiveData;
        boolean done=false;

        if (parentContext == null) return rc;

        // Check if this is the first time, don't change anything after the 1st call, we already made a list
        // Real code would look again for new usb devices
        if (simulate) {
            if (useWifi) {
                if (this.mFtdiDevices == null) {
                    final byte[] sendData = new byte[1];
                    sendData[0] = '?';      // Simple packet, ask for a list of connected modules
                    NetworkManager.requestSend(SimulatorData.Type.Types.DEVICE_LIST,
                            SimulatorData.Data.Modules.LEGACY_CONTROLLER, sendData);
                    while (!done) {

                        // Send a query for a list of connected modules
                        NetworkManager.requestSend(SimulatorData.Type.Types.DEVICE_LIST,
                                SimulatorData.Data.Modules.LEGACY_CONTROLLER,
                                sendData);
                        Log.d("D2xx::", "Send Packet to PC");
                        try {
                            receiveData = NetworkManager.getLatestData(
                                    SimulatorData.Type.Types.DEVICE_LIST, true, true);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return rc;
                        }

                        Log.d(TAG, "Got a reply!");
                        // If we got a reply from the PC then parse the xml and return a list of FT_Device objects

                        // De-convert the data
                            this.mFtdiDevices = buildFT_DeviceList(receiveData);
                            done = true;
                        }
                    }
                    //ftDev = new FT_Device("A501E27V", "Hagerty USB1");
                    //devices.add(ftDev);
                }
            }


        rc = this.mFtdiDevices.size();
        return rc;
    }

    public synchronized FT_Device openBySerialNumber(Context parentContext, String serialNumber)
    {
        FtDeviceInfoListNode devInfo = null;
        FT_Device ftDev = null;

        if (parentContext == null) return null;

        //updateContext(parentContext);

        for (int i = 0; i < this.mFtdiDevices.size(); i++) {
            FT_Device tmpDev = this.mFtdiDevices.get(i);
            if (tmpDev == null)
                continue;
            devInfo = tmpDev.mDeviceInfoNode;

            if (devInfo == null) {
                Log.d("D2xx::", "***devInfo cannot be null***");
            } else if (devInfo.serialNumber.equals(serialNumber)) {
                ftDev = tmpDev;
                break;
            }

        }

        return ftDev;
    }

    public synchronized FtDeviceInfoListNode getDeviceInfoListDetail(int index)
    {
        if ((index > this.mFtdiDevices.size()) || (index < 0))
            return null;
        return this.mFtdiDevices.get(index).mDeviceInfoNode;
    }

    @NotNull
    private ArrayList<FT_Device> buildFT_DeviceList(byte[] inputText) {
        ArrayList<FT_Device> devices = new ArrayList<>();
        FT_Device ftDev = null;

        try {
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(inputText))));
            doc.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "/bricks/*";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc,
                    XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
                Log.d("D2xx::", "Current Element: " + nNode.getNodeName());
                if (nNode.getNodeName().equals("Legacy")) {
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Log.d("D2xx::", "Processing Legacy ");
                        Element eElement = (Element) nNode;
                        String serial = eElement.getElementsByTagName("serial").item(0).getTextContent();
                        String name = eElement.getElementsByTagName("name").item(0).getTextContent();

                        ftDev = new FT_Device_Legacy(serial, name);
                        devices.add(ftDev);
                    }
                } else if (nNode.getNodeName().equals("Motor")) {
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Log.d("D2xx::", "Processing Motor ");
                        Element eElement = (Element) nNode;
                        String serial = eElement.getElementsByTagName("serial").item(0).getTextContent();
                        String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                        ftDev = new FT_Device_Motor(serial, name);
                        devices.add(ftDev);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return devices;
    }

    public static class D2xxException extends IOException
    {
        private static final long serialVersionUID = 1L;
        public D2xxException(String ftStatusMsg) {
            super();
        }
    }

    public static class FtDeviceInfoListNode
    {
        public int flags;
        public short bcdDevice;
        public int type;
        public byte iSerialNumber;
        public int id;
        public int location;
        public String serialNumber;
        public String description;
        public int handle;
        public int breakOnParam;
        public short modemStatus;
        public short lineStatus;
    }

}
