package com.ftdi.j2xx;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FT_Device
{
    private static final String TAG = "FTDI_Device::";
    D2xxManager.FtDeviceInfoListNode mDeviceInfoNode;
    String mySerialNumber;
    int mMotor1Encoder;  // Simulate motor 1 encoder.  Increment each write.
    int mMotor2Encoder;  // Simulate motor 2 encoder.  Increment each write.
    double mMotor1TotalError;
    double mMotor2TotalError;
    long mTimeInMilliseconds=0;
    long mOldTimeInMilliseconds=0;
    long mDeltaWriteTime=0;

    NetworkManager mNetworkManager;
    LinkedBlockingQueue mWriteToPcQueue;
    LinkedBlockingQueue mReadFromPcQueue;

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

        mNetworkManager = new NetworkManager();
        mReadFromPcQueue = mNetworkManager.getReadFromPcQueue();
        mWriteToPcQueue = mNetworkManager.getWriteToPcQueue();
    }


    public synchronized void close()
    {

    }

    private int getPacketFromPC(byte[] data, int length, long wait_ms) {
        int rc = 0;
        byte[] packet;

        try {
            packet = (byte[])mReadFromPcQueue.poll(wait_ms, TimeUnit.MILLISECONDS);

            // If timed out waiting for packet then return the last packet that was read
            if (packet==null) {
                System.arraycopy(mCurrentStateBuffer, 0, data, 0, 208);
                rc = length;
            } else {
                System.arraycopy(packet, 0, data, 0, length);                   // return the packet
                System.arraycopy(packet, 0, mCurrentStateBuffer, 0, length);    // Save in case we lose a packet
                rc = length;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rc;
    }

    private void sendPacketToPC(byte[] data, int start, int length) {
        byte[] tempBytes = new byte[length];
        System.arraycopy(data, start, tempBytes, 0, length);
        tempBytes[1] =(byte)mPacketCount;   // Add a counter so we can see lost packets
        mPacketCount++;
        mWriteToPcQueue.add(tempBytes);
    }

    public int read(byte[] data, int length, long wait_ms)
    {
        int rc = 0;
        Object localObject1;
        String logString[];

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

        Log.v("Legacy", "READ(): Buffer len=" + length + " (" + bufferToHexString(data,0,length) + ")");

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

        Log.v("Legacy", "WRITE(): Buffer len=" + length + " (" + bufferToHexString(data,0,length) + ")");

        // Write Command
        if (data[0] == writeCmd[0] && data[2] == writeCmd[2]) {  // writeCmd


            // If size is 208(0xd0) bytes then they are writing a full buffer of data to all ports.
            // Note: the buffer we were giving in this case is 208+5 bytes because the "writeCmd" header is attached
            if (data[4] == (byte)0xd0 ) {

                queueUpForReadFromPhone(recSyncCmd0); // Reply, we got your writeCmd

                // Now, the reset of the buffer minus the 5 header bytes should be 208 (0xd0) bytes.
                // These 208 bytes need to be written to the connected module.
                // Write the entire received buffer into the mCurrentState buffer.
                // We will use this buffer when the ftc_app askes for the current state of the module.

                // Write the 208 buffer to the NetworkManager queue to be sent to the PC Simulator
                sendPacketToPC(data, 5, 208);

                // Check delta time to see if we are too slow in our simulation.
                // Baud rate was 250,000 with real USB port connected to module
                // We are getting deltas of 31ms between each write call
//                mTimeInMilliseconds = SystemClock.uptimeMillis();
//                mDeltaWriteTime = mTimeInMilliseconds - mOldTimeInMilliseconds;
//                mOldTimeInMilliseconds = mTimeInMilliseconds;
//                Log.v("Legacy", "WRITE: Delta Time = " + mDeltaWriteTime);

                // Set the Port S0 ready bit in the global part of the Current State Buffer
                mCurrentStateBuffer[3] = (byte)0xfe;  // Port S0 ready

            }
            // Read Command
        } else if (data[0] == readCmd[0] && data[2] == readCmd[2]) { // readCmd
            if (data[4] == 3) { // Android asks for 3 bytes, initial query of device type
                queueUpForReadFromPhone(recSyncCmd3);  // Send receive sync, bytes to follow
                queueUpForReadFromPhone(controllerTypeLegacy);
            } else if (data[4] == (byte)208) { // Android asks for 208 bytes, full read of device
                queueUpForReadFromPhone(recSyncCmd208);  // Send receive sync loop back
                sendPacketToPC(data,0,5);   // Send the actual message to the PC so it can respond
            }
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

