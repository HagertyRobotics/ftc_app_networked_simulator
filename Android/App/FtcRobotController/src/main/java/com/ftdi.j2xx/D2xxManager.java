package com.ftdi.j2xx;

import android.content.Context;
import android.util.Log;

import org.ftccommunity.simulator.Server;
import org.ftccommunity.simulator.net.SimulatorData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class D2xxManager
{
    public static final int PHONEPORT = 7000;
    public static final int MODULE_LISTER_PORT = 7000;
    public static final String PC_IP_ADDRESS = "192.168.1.119"; // "10.0.1.193";
    protected static final String ACTION_USB_PERMISSION = "com.ftdi.j2xx";
    private static final String TAG = "D2xx::";
    private static D2xxManager mInstance = null;
    InetAddress mIPAddress;
    private ArrayList<FT_Device> mFtdiDevices;
    private Server server;

    private D2xxManager(Context parentContext)
            throws D2xxManager.D2xxException
    {
        Log.v(TAG, "Start constructor");
        server = new Server(7002);
        Thread serverThread = new Thread(server);
        serverThread.start();

        try {
            mIPAddress = InetAddress.getByName(PC_IP_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new NetworkManager(NetworkManager.NetworkTypes.WIFI);
        if (parentContext == null) {
            throw new D2xxException("D2xx init failed: Can not find parentContext!");
        }
        //updateContext(parentContext);


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
        byte[] receiveData = new byte[1024];
        boolean done = false;

        if (parentContext == null) return rc;

        // Check if this is the first time, don't change anything after the 1st call, we already made a list
        // Real code would look again for new usb devices
        if (this.mFtdiDevices == null) {

            final byte[] sendData = new byte[1];
            sendData[0] = '?';      // Simple packet, ask for a list of connected modules
            NetworkManager.requestSend(SimulatorData.Type.Types.BRICK_INFO, SimulatorData.Data.Modules.LEGACY_CONTROLLER, sendData);
            // DatagramPacket send_packet = new DatagramPacket(sendData, sendData.length, mIPAddress, MODULE_LISTER_PORT);
            while (!done) {

                // Send a query for a list of connected modules
                NetworkManager.requestSend(SimulatorData.Type.Types.BRICK_INFO,
                        SimulatorData.Data.Modules.LEGACY_CONTROLLER,
                        sendData);
                Log.d("D2xx::", "Send Packet to PC");
                receiveData = NetworkManager.getLatestData(SimulatorData.Type.Types.BRICK_INFO);

                // If we got a reply from the PC then parse the xml and return a list of FT_Device objects
                this.mFtdiDevices = buildFT_DeviceList(receiveData);

            }
            //ftDev = new FT_Device("A501E27V", "Hagerty USB1");
            //devices.add(ftDev);
        }

        rc = this.mFtdiDevices.size();

        return rc;
    }

    public synchronized FT_Device openBySerialNumber(Context parentContext, String serialNumber)
    {
        FtDeviceInfoListNode devInfo = null;
        FT_Device ftDev = null;

        if (parentContext == null) return ftDev;

        //updateContext(parentContext);

        for (int i = 0; i < this.mFtdiDevices.size(); i++) {
            FT_Device tmpDev = (FT_Device) this.mFtdiDevices.get(i);
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
        return ((FT_Device)this.mFtdiDevices.get(index)).mDeviceInfoNode;
    }

    private ArrayList buildFT_DeviceList(byte[] inputText) {
        ArrayList devices = new ArrayList();
        FT_Device ftDev = null;

        try {
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(inputText)));
            doc.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "/bricks/*";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
                Log.d("D2xx::", "Current Element :" + nNode.getNodeName());
                if (nNode.getNodeName().equals("Legacy")) {
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String serial = eElement.getElementsByTagName("serial").item(0).getTextContent();
                        String alias = eElement.getElementsByTagName("alias").item(0).getTextContent();
                        String portString = eElement.getElementsByTagName("port").item(0).getTextContent();

                        ftDev = new FT_Device_Legacy(serial, alias, PC_IP_ADDRESS, Integer.parseInt(portString));
                        devices.add(ftDev);
                    }
                } else if (nNode.getNodeName().equals("motor")) {
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String serial = eElement.getElementsByTagName("serial").item(0).getTextContent();
                        String alias = eElement.getElementsByTagName("alias").item(0).getTextContent();
                        String portString = eElement.getElementsByTagName("port").item(0).getTextContent();

                        ftDev = new FT_Device_Motor(serial, alias, PC_IP_ADDRESS, Integer.parseInt(portString));
                        devices.add(ftDev);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return devices;
    }

    public static class D2xxException extends IOException
    {
        private static final long serialVersionUID = 1L;

        public D2xxException() {
        }

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
