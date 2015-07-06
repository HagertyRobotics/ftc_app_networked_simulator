package com.ftdi.j2xx;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class FT_Device
{
    private static final String TAG = "FTDI_Device::";
    D2xxManager.FtDeviceInfoListNode mDeviceInfoNode;
    String mySerialNumber;

    long mTimeInMilliseconds=0;
    long mOldTimeInMilliseconds=0;
    long mDeltaWriteTime=0;

    Socket mSimulatorSocket = null;
    DataOutputStream os = null;
    DataInputStream is = null;

    // the Server's Port
    public static final int SERVERPORT  = 6500;

    public FT_Device(String serialNumber, String description)
    {
        int i;

        this.mDeviceInfoNode = new D2xxManager.FtDeviceInfoListNode();

        this.mDeviceInfoNode.serialNumber = serialNumber;
        this.mDeviceInfoNode.description = description;

        try {
            mSimulatorSocket = new Socket("10.0.1.193", SERVERPORT);
            os = new DataOutputStream(mSimulatorSocket.getOutputStream());
            is = new DataInputStream(mSimulatorSocket.getInputStream());
            mSimulatorSocket.setSoTimeout(200);
        } catch (UnknownHostException e) {
            Log.v("D2xx::", "Error: " + e);
        } catch (IOException e) {
            Log.v("D2xx::", "Error: " + e);
        }
    }


    /*
    *
    */
    public void sendBytesToSimulator(byte[] data) {
        // If everything has been initialized then we want to write some data
        // to the socket we have opened a connection to on port 25
        if (mSimulatorSocket != null && os != null && is != null) {
            try {
                os.write(data, 0, data.length);
            } catch (UnknownHostException e) {
                Log.v("D2xx::", "Error: " + e);
            } catch (IOException e) {
                Log.v("D2xx::", "Error: " + e);
            }
        }
    }

    /*
    *
    */
    private int readBytesFromSimulator(byte[] data, int length) {
        int bytesRead = 0;
        Log.v("D2xx::", "Read: Length: " + length);
        try {
            bytesRead = is.read(data, 0, length);
        } catch (IOException e) {
            Log.v("D2xx::", "Error: " + e);
            e.printStackTrace();
        }

        try {
            if (bytesRead != length) {
                int skipped = is.skipBytes(length);
                Log.v("D2xx::", "Skipped: " + skipped);
            }
        } catch (IOException e) {
            Log.v("D2xx::", "Error: " + e);
            e.printStackTrace();
        }

        Log.v("D2xx::", "Read: BytesRead: " + bytesRead);
        return bytesRead;
    }


    // readInputStreamWithTimeout
    //
    // Considered using this to implement a timeout.  Tried setting timeout in socket instead.
    //
    // example:
    // byte[] inputData = new byte[1024];
    // int readCount = readInputStreamWithTimeout(System.in, inputData, 6000);  // 6 second timeout
    // readCount will indicate number of bytes read; -1 for EOF with no data read.
    //
    public static int readInputStreamWithTimeout(InputStream is, byte[] b, int timeoutMillis)
            throws IOException  {
        int bufferOffset = 0;
        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {
            int readLength = java.lang.Math.min(is.available(),b.length-bufferOffset);
            // can alternatively use bufferedReader, guarded by isReady():
            int readResult = is.read(b, bufferOffset, readLength);
            if (readResult == -1) break;
            bufferOffset += readResult;
        }
        return bufferOffset;
    }


    public int read(byte[] data, int length, long wait_ms)
    {
        int rc = 0;

        if (length <= 0) {
            return -2;
        }
        readBytesFromSimulator(data, length);
        Log.v("D2xx::", "Read: (" + wait_ms + ") " + bufferToHexString(data, 0, data.length));

        rc = length;
        return rc;
    }




    public int write(byte[] data, int length, boolean wait)
    {
        int rc = 0;
        Log.v("D2xx::", "Write: (" + wait + ") " + bufferToHexString(data,0,data.length));
        if (length <= 0) {
            return rc;
        }

        sendBytesToSimulator(data);

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


    //////////////////////////////////  Stubs from original FTDI Class  /////////////////////////////////////


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

    public synchronized void close()
    {

    }
}
