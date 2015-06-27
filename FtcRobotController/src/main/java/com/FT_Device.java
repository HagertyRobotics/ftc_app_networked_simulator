package com.ftdi.j2xx;

import android.util.Log;

import com.qualcomm.robotcore.util.TypeConversion;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FT_Device
{

    private static final String TAG = "FTDI_Device::";
    D2xxManager.FtDeviceInfoListNode mDeviceInfoNode;
    String mySerialNumber;
    int mMotor1Encoder;  // Simulate motor 1 encoder.  Increment each write.
    int mMotor2Encoder;  // Simulate motor 2 encoder.  Increment each write.
    double mMotor1TotalError;
    double mMotor2TotalError;

    protected final byte[] mCurrentStateBuffer = new byte[208];

    private enum WriteStates {
        READY_FOR_WRITE_COMMANDS,
        READY_FOR_WRITE_DATA,
    }

    // Synchronized by 'this'
    private WriteStates mWriteState = WriteStates.READY_FOR_WRITE_COMMANDS;

    protected final Queue<CacheWriteRecord> readQueue = new ConcurrentLinkedQueue();
    protected volatile boolean writeLocked = false;

    public FT_Device(String serialNumber, String description)
    {
        int i;
        this.mDeviceInfoNode = new D2xxManager.FtDeviceInfoListNode();

        this.mDeviceInfoNode.serialNumber = serialNumber;
        this.mDeviceInfoNode.description = description;

        mMotor1Encoder = 0;
        mMotor2Encoder = 0;
        mMotor1TotalError = 0;  // used in the RUN_TO_POSITION PID
        mMotor2TotalError = 0;
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
                localObject1 = (CacheWriteRecord)this.readQueue.poll();
                if (localObject1 == null)
                    return rc;

                System.arraycopy(((CacheWriteRecord)localObject1).data, 0, data, 0, length);
                rc = length;
                if (length == 5) {
                    Log.v("Legacy", "READ: Response Header (" + bufferToHexString(data,0,length) + ") len=" + length);
                } else if (length == 3) {
                    Log.v("Legacy", "READ: Response Header (" + bufferToHexString(data,0,length) + ") len=" + length);
                } else if (length == 208) {
                    //Log.v("Legacy", "READ: Response Buffer S0 (" + bufferToHexString(data,16+4,20) + "...) len=" + length);
                    Log.v("Legacy", "READ: Response Buffer FLAGS 0=" + bufferToHexString(data,0,3) + " 16=" + bufferToHexString(data,16,4) + "47=" + bufferToHexString(data,47,1));
                }
            }
        } finally {
            this.writeLocked = false;
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

    public void queueUpForReadFromPhone(byte[] data) {
        //while (this.writeLocked) Thread.yield();
        this.readQueue.add(new CacheWriteRecord(data));
    }
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
            if (data[4] == (byte)0xd0 ) {
                //Log.v("Legacy", "WRITE: Write Header (" + bufferToHexString(data,0,5) + ") len=" + length);
                queueUpForReadFromPhone(recSyncCmd0); // Reply we got your writeCmd

                Log.v("Legacy", "WRITE: Write Buffer S0 (" + bufferToHexString(data, 5+16+4, 20) + ") len=" + length);
                Log.v("Legacy", "WRITE: Write Buffer FLAGS 0=" + bufferToHexString(data,5+0,3) + " 16=" + bufferToHexString(data,5+16,4) + "47=" + bufferToHexString(data,5+47,1));

                // Now, the reset of the buffer minus the header 5 bytes should be 208 (0xd0) bytes that need to be written to the connected devices
                // Write the entire received buffer into the mCurrentState buffer so the android can see what we are up to
                // 4 bytes of header (r/w, i2c address, i2c register, i2c buffer len)
                System.arraycopy(data, 5, mCurrentStateBuffer, 0, 208);

                // This is for Port P0 only.  16 is the base offset.  Each port has 32 bytes.
                // If I2C_ACTION is set, take some action
                if (mCurrentStateBuffer[47] == (byte)0xff) { // Action flag
                    if ((mCurrentStateBuffer[16] & (byte)0x01) == (byte)0x01) { // I2C Mode
                        if ((mCurrentStateBuffer[16] & (byte)0x80) == (byte)0x80) { // Read mode
                            // just for fun, simulate reading the encoder from i2c.
                            // really just from the mMotor1Encoder class variable
                            // +4 to get past the header, motor 1 encoder starts at 12 and is 4 bytes long
                            mCurrentStateBuffer[16+4+12+0] = (byte)(mMotor1Encoder >> 24);
                            mCurrentStateBuffer[16+4+12+1] = (byte)(mMotor1Encoder >> 16);
                            mCurrentStateBuffer[16+4+12+2] = (byte)(mMotor1Encoder >> 8);
                            mCurrentStateBuffer[16+4+12+3] = (byte)(mMotor1Encoder >> 0);

                            mCurrentStateBuffer[16+4+16+0] = (byte)(mMotor2Encoder >> 24);
                            mCurrentStateBuffer[16+4+16+1] = (byte)(mMotor2Encoder >> 16);
                            mCurrentStateBuffer[16+4+16+2] = (byte)(mMotor2Encoder >> 8);
                            mCurrentStateBuffer[16+4+16+3] = (byte)(mMotor2Encoder >> 0);

                        } else { // Write mode

                            // Just for fun, simulate some of the motor modes
                            simulateWritesToMotor1();
                            simulateWritesToMotor2();
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

    private void simulateWritesToMotor1() {
        // Simulate writes to Motor 1
        // Check for the 4 different states
        switch ((mCurrentStateBuffer[16+4+4] & (byte)0x03)) {
            case (byte)0x00:  // Run with no encoder
                // Check the motor power we just received and increment/decrement encoder
                // For now just increment encoder by motor power
                // Note: Float power = 0x80 (Negative Zero)
                if ((mCurrentStateBuffer[16+4+5] != (byte)0x00) && (mCurrentStateBuffer[16+4+5] != (byte)0x80)) {  // Motor 1 Power
                        mMotor1Encoder += mCurrentStateBuffer[16+4+5];
                }
                break;
            case (byte)0x01:  // Run with PID on encoder
                break;
            case (byte)0x02:  // Run to position
                double P = 0.5;
                double I = 0.05;
                int error = getMotor1TargetEncoder() - getMotor1CurrentEncoder();
                mMotor1TotalError += error;
                if (mMotor1TotalError > 2000) mMotor1TotalError = 2000;
                if (mMotor1TotalError < -2000) mMotor1TotalError = -2000;

                Log.v("Legacy", "PID: " + error + " " + mMotor1TotalError);
                int power = (int)(P*error) + (int)(mMotor1TotalError*I);
                if (power > 100) power=100;
                if (power < -100) power= -100;
                mMotor1Encoder += power;
                break;
            case (byte)0x03:  // Reset encoder
                mMotor1Encoder = 0;
                break;
        }
    }

    private void simulateWritesToMotor2() {
        // Simulate writes to Motor 2
        // Check for the 4 different states
        switch ((mCurrentStateBuffer[16+4+7] & (byte)0x03)) {
            case (byte)0x00:  // Run with no encoder
                // Check the motor power we just received and increment/decrement encoder
                // For now just increment encoder by motor power
                // Note: Float power = 0x80 (Negative Zero)
                if ((mCurrentStateBuffer[16+4+6] != (byte)0x00) && (mCurrentStateBuffer[16+4+6] != (byte)0x80)) {  // Motor 2 Power
                    mMotor1Encoder += mCurrentStateBuffer[16+4+6];
                }
                break;
            case (byte)0x01:  // Run with PID on encoder
                break;
            case (byte)0x02:  // Run to position
                double P = 0.5;
                double I = 0.05;
                int error = getMotor2TargetEncoder() - getMotor2CurrentEncoder();
                mMotor2TotalError += error;
                if (mMotor2TotalError > 2000) mMotor2TotalError = 2000;
                if (mMotor2TotalError < -2000) mMotor2TotalError = -2000;

                Log.v("Legacy", "PID: " + error + " " + mMotor2TotalError);
                int power = (int)(P*error) + (int)(mMotor2TotalError*I);
                if (power > 100) power=100;
                if (power < -100) power= -100;
                mMotor2Encoder += power;
                break;
            case (byte)0x03:  // Reset encoder
                mMotor2Encoder = 0;
                break;
        }
    }

    private int getMotor1CurrentEncoder() {
        byte[] arrayOfByte1 = new byte[4];
        System.arraycopy(mCurrentStateBuffer, 16+4+12, arrayOfByte1, 0, arrayOfByte1.length);
        return TypeConversion.byteArrayToInt(arrayOfByte1);
    }

    private int getMotor2CurrentEncoder() {
        byte[] arrayOfByte1 = new byte[4];
        System.arraycopy(mCurrentStateBuffer, 16+4+16, arrayOfByte1, 0, arrayOfByte1.length);
        return TypeConversion.byteArrayToInt(arrayOfByte1);
    }

    private int getMotor1TargetEncoder() {
        byte[] arrayOfByte1 = new byte[4];
        System.arraycopy(mCurrentStateBuffer, 16+4+0, arrayOfByte1, 0, arrayOfByte1.length);
        return TypeConversion.byteArrayToInt(arrayOfByte1);
    }

    private int getMotor2TargetEncoder() {
        byte[] arrayOfByte1 = new byte[4];
        System.arraycopy(mCurrentStateBuffer, 16+4+8, arrayOfByte1, 0, arrayOfByte1.length);
        return TypeConversion.byteArrayToInt(arrayOfByte1);
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
