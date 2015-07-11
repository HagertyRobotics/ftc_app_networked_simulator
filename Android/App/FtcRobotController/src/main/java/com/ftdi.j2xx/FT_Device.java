package com.ftdi.j2xx;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FT_Device
{
    private static final String TAG = "FTDI_Device::";
    D2xxManager.FtDeviceInfoListNode mDeviceInfoNode;
    String mySerialNumber;

    // the Server's Port
    public static final int PHONE_PORT  = 6000;
    public static final int PC_PORT = 6500;
    public static final String PC_IP_ADDRESS  = "10.0.1.193";
    InetAddress mIPAddress;

    long mTimeInMilliseconds=0;
    long mOldTimeInMilliseconds=0;
    long mDeltaWriteTime=0;

    DatagramSocket mSimulatorSocket;
    byte[] mReceiveData = new byte[1024];

    protected final byte[] mCurrentStateBuffer = new byte[208];

    int mPacketCount=0;

    // Queue used to pass packets between writes and reads in the onboard simulator.
    // Read and Writes come from the ftc_app when it thinks it is talking to the
    // FTDI driver.
    protected final Queue<CacheWriteRecord> readQueue = new ConcurrentLinkedQueue();
    protected volatile boolean writeLocked = false;

    public FT_Device(String serialNumber, String description)
    {
        int i;
        mDeviceInfoNode = new D2xxManager.FtDeviceInfoListNode();

        mDeviceInfoNode.serialNumber = serialNumber;
        mDeviceInfoNode.description = description;

        try {
            mIPAddress = InetAddress.getByName(PC_IP_ADDRESS);
            mSimulatorSocket = new DatagramSocket(PHONE_PORT);
            Log.v("D2xx::", "Local Port " + mSimulatorSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void close()
    {

    }

    private int getPacketFromPC(byte[] data, int length, long wait_ms) {
        int rc = 0;

        DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
        try {
            mSimulatorSocket.setSoTimeout(300);
            mSimulatorSocket.receive(receivePacket);

            // If packet is not the size we are expecting, just return 0
            // Is this ok?
            if (length != receivePacket.getLength()) {
                return 0;
            }

            // Copy the received packet into the passed buffer
            System.arraycopy(receivePacket.getData(), 0, data, 0, receivePacket.getLength());
            rc = receivePacket.getLength();
            Log.v("D2xx::", "Receive: " + bufferToHexString(data, 0, 5) + "Len: " + receivePacket.getLength());
        } catch (IOException e) {
            //e.printStackTrace();
            rc=0;
        }

        return rc;
    }

    private void sendPacketToPC(byte[] data) {
        try {
            DatagramPacket send_packet = new DatagramPacket(data,data.length, mIPAddress, PC_PORT);
            mSimulatorSocket.send(send_packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int read(byte[] data, int length, long wait_ms)
    {
        int rc = 0;
        Object localObject1;

        if (length <= 0) {
            return -2;
        }

        // Check onboard read queue and see if we have a override
        // Use this packet instead of reading one from the network
        if (!this.readQueue.isEmpty()) {
            localObject1 = this.readQueue.poll();
            if (localObject1 == null)
                return rc;
            System.arraycopy(((CacheWriteRecord)localObject1).data, 0, data, 0, length);
            rc = length;
        } else {
            rc = getPacketFromPC(data, length, wait_ms);
        }

        return rc;
    }


    public void queueUpForReadFromPhone(byte[] data) {
        //while (this.writeLocked) Thread.yield();
        this.readQueue.add(new CacheWriteRecord(data));
    }

    /*
    ** Packet types
    */
    protected final byte[] writeCmd = { 85, -86, 0, 0, 0 };
    protected final byte[] readCmd = { 85, -86, -128, 0, 0 };
    protected final byte[] recSyncCmd3 = { 51, -52, 0, 0, 3};
    protected final byte[] recSyncCmd0 = { 51, -52, -128, 0, 0};
    protected final byte[] recSyncCmd208 = { 51, -52, -128, 0, (byte)208};
    protected final byte[] controllerTypeLegacy = { 0, 77, 73};       // Controller type USBLegacyModule

    public int write(byte[] data, int length, boolean wait)
    {

        int rc = 0;

        if (length <= 0) {
            return rc;
        }

        // For the first read command that the phone sends, queue up a reply so the
        // PC doesn't have to.  We were loosing the first packets and the phone doesn't
        // retry the initial packet.
        if (data[0] == readCmd[0] && data[2] == readCmd[2] && data[4] == 3) { // readCmd
            // Android asks for 3 bytes, initial query of device type
            //Log.v("Legacy", "WRITE: Read Header (" + bufferToHexString(data,0,length) + ") len=" + length);
            queueUpForReadFromPhone(recSyncCmd3);  // Send receive sync, bytes to follow
            queueUpForReadFromPhone(controllerTypeLegacy);
        } else {

            Log.v("Legacy", "WRITE(): Buffer (" + bufferToHexString(data, 0, length) + ") len=" + length);
            sendPacketToPC(data);
        }

        rc = length;
        return rc;
    }


    private String bufferToHexString(byte[] data, int start, int length) {
        int i;
        int myStop;
        StringBuilder sb = new StringBuilder();
        //byte [] subArray = Arrays.copyOfRange(a, 4, 6);
        myStop = (length > data.length) ? data.length : length;
        for (i=start; i<start+myStop; i++) {
            sb.append(String.format("%02x ", data[i]));
        }
        return sb.toString();
    }



    /////////////////////   Stub routines from original FTDI Driver  ////////////////////////

    public int read(byte[] data, int length)
    {
        long l=0;
        return read(data, length, l);
    }

    public int read(byte[] data)
    {
        long l=0;
        return read(data, data.length,l);
    }

    public int write(byte[] data, int length)
    {
        return write(data, length, true);
    }

    public int write(byte[] data)
    {
        return write(data, data.length, true);
    }

    public boolean purge(byte flags)
    {
        return true;
    }

    public boolean setBaudRate(int baudRate)
    {
        return true;
    }

    public boolean setDataCharacteristics(byte dataBits, byte stopBits, byte parity)
    {
        return true;
    }

    public boolean setLatencyTimer(byte latency)
    {
        return true;
    }

    protected static class CacheWriteRecord {
        public byte[] data;

        public CacheWriteRecord(byte[] data) {
            this.data = data;
        }
    }
}

