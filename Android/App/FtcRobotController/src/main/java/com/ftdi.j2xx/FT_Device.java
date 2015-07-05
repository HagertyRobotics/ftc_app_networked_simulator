package com.ftdi.j2xx;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.LinkedBlockingQueue;

public class FT_Device
{
    private static final String TAG = "FTDI_Device::";
    D2xxManager.FtDeviceInfoListNode mDeviceInfoNode;
    String mySerialNumber;

    long mTimeInMilliseconds=0;
    long mOldTimeInMilliseconds=0;
    long mDeltaWriteTime=0;

    LinkedBlockingQueue mWriteQueue = new LinkedBlockingQueue();
    LinkedBlockingQueue mReadQueue = new LinkedBlockingQueue();

    DatagramSocket mSimulatorSocket;
    // the Server's Port
    public static final int SERVERPORT  = 6000;

    public FT_Device(String serialNumber, String description)
    {
        int i;

        this.mDeviceInfoNode = new D2xxManager.FtDeviceInfoListNode();

        this.mDeviceInfoNode.serialNumber = serialNumber;
        this.mDeviceInfoNode.description = description;

        // Start the Network Sender thread
        // This thread will read from the mWriteQueue and send packets to the PC application
        // THe mWriteQueue will get packets from the FT_Device write call
        try {
            mSimulatorSocket = new DatagramSocket(6000);
            Log.v("D2xx::", "Local Port " + mSimulatorSocket.getLocalPort());
            NetworkSender myNetworkSender = new NetworkSender(mWriteQueue, mSimulatorSocket);  // Runnable
            Thread networkSenderThread = new Thread(myNetworkSender);
            networkSenderThread.start();

            NetworkReceiver myNetworkReceiver = new NetworkReceiver(mReadQueue, mSimulatorSocket);
            Thread networkReceiverThread = new Thread(myNetworkReceiver);
            networkReceiverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void close()
    {

    }

    public int read(byte[] data, int length, long wait_ms)
    {
        int rc = 0;
        Log.v("D2xx::", "Read");

        byte[] tempData;
        try {
            tempData = (byte[])mReadQueue.take();
            Log.v("D2xx::", "Read after");
            if (tempData.length == length) {
                System.arraycopy(tempData, 0, data, 0, length);
                rc = length;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rc;
    }

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


    public int write(byte[] data, int length, boolean wait)
    {
        int rc = 0;
        Log.v("D2xx::", "Write");
        if (length <= 0) {
            return rc;
        }

        try {
            mWriteQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
