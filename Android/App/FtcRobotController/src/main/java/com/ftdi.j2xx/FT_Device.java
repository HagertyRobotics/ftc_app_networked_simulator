package com.ftdi.j2xx;

import android.os.SystemClock;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    public int read(byte[] data, int length, long wait_ms)
    {
        int rc = 0;
        Object localObject1;
        String logString[];

        if (length <= 0) {
            return -2;
        }

        try
        {
            this.writeLocked = true;

            if (!this.readQueue.isEmpty()) {
                localObject1 = this.readQueue.poll();
                if (localObject1 == null)
                    return rc;

                System.arraycopy(((CacheWriteRecord)localObject1).data, 0, data, 0, length);
                rc = length;
            }
        } finally {
            this.writeLocked = false;
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

        // Write Command
        if (data[0] == writeCmd[0] && data[2] == writeCmd[2]) {  // writeCmd


            // If size is 208(0xd0) bytes then they are writing a full buffer of data to all ports.
            // Note: the buffer we were giving in this case is 208+5 bytes because the "writeCmd" header is attached
            if (data[4] == (byte)0xd0 ) {
                //Log.v("Legacy", "WRITE: Write Header (" + bufferToHexString(data,0,5) + ") len=" + length);
                queueUpForReadFromPhone(recSyncCmd0); // Reply, we got your writeCmd

                //Log.v("Legacy", "WRITE: Write Buffer S0 (" + bufferToHexString(data, 5+16+4, 20) + ") len=" + length);
                //Log.v("Legacy", "WRITE: Write Buffer FLAGS 0=" + bufferToHexString(data,5+0,3) + " 16=" + bufferToHexString(data,5+16,4) + "47=" + bufferToHexString(data,5+47,1));

                // Now, the reset of the buffer minus the 5 header bytes should be 208 (0xd0) bytes.
                // These 208 bytes need to be written to the connected module.
                // Write the entire received buffer into the mCurrentState buffer.
                // We will use this buffer when the ftc_app askes for the current state of the module.
                //
                // Note: the buffer we were giving in this case is 208+5 bytes because the "writeCmd" header is attached
                System.arraycopy(data, 5, mCurrentStateBuffer, 0, 208);

                // Write the mCurrentStateBuffer to the NetworkManager queue to be sent to the PC Simulator
                byte[] tempBytes = new byte[208];
                System.arraycopy(mCurrentStateBuffer, 0, tempBytes, 0, 208);
                mWriteToPcQueue.add(tempBytes);

                // Check delta time to see if we are too slow in our simulation.
                // Baud rate was 250,000 with real USB port connected to module
                // We are getting deltas of 31ms between each write call
                mTimeInMilliseconds = SystemClock.uptimeMillis();
                mDeltaWriteTime = mTimeInMilliseconds - mOldTimeInMilliseconds;
                mOldTimeInMilliseconds = mTimeInMilliseconds;
                Log.v("Legacy", "WRITE: Delta Time = " + mDeltaWriteTime);

                // This is for Port P0 only.  16 is the base offset.  Each port has 32 bytes.
                // If I2C_ACTION is set, take some action
                if (mCurrentStateBuffer[47] == (byte)0xff) { // Action flag
                    if ((mCurrentStateBuffer[16] & (byte)0x01) == (byte)0x01) { // I2C Mode
                        if ((mCurrentStateBuffer[16] & (byte)0x80) == (byte)0x80) { // Read mode


                        } else { // Write mode

                        }
                    }
                }

                // Set the Port S0 ready bit in the global part of the Current State Buffer
                mCurrentStateBuffer[3] = (byte)0xfe;  // Port S0 ready

            }
            // Read Command
        } else if (data[0] == readCmd[0] && data[2] == readCmd[2]) { // readCmd
            if (data[4] == 3) { // Android asks for 3 bytes, initial query of device type
                //Log.v("Legacy", "WRITE: Read Header (" + bufferToHexString(data,0,length) + ") len=" + length);
                queueUpForReadFromPhone(recSyncCmd3);  // Send receive sync, bytes to follow
                queueUpForReadFromPhone(controllerTypeLegacy);
            } else if (data[4] == (byte)208) { // Android asks for 208 bytes, full read of device
                //Log.v("Legacy", "WRITE: Read Header (" + bufferToHexString(data,0,length) + ") len=" + length);
                queueUpForReadFromPhone(recSyncCmd208);  // Send receive sync, bytes to follow
                queueUpForReadFromPhone(mCurrentStateBuffer); //
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