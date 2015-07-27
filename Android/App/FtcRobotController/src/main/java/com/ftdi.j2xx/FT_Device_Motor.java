package com.ftdi.j2xx;

import android.util.Log;

public class FT_Device_Motor extends FT_Device {

    public FT_Device_Motor(String serialNumber, String description, String ipAddress, int port) {
        super(serialNumber, description, ipAddress, port);
    }

    /*
    ** Packet types
    */
    protected final byte[] WRITE_COMMAND = { 85, -86, 0, 0, 0 };
    protected final byte[] READ_COMMAND = { 85, -86, -128, 0, 0 };
    protected final byte[] RECIEVE_SYNC_COMMAND_3 = { 51, -52, 0, 0, 3};
    protected final byte[] RECEIVE_SYNC_COMMAND_0 = { 51, -52, -128, 0, 0};
    protected final byte[] RECEIVE_SYNC_COMMAND_94 = { 51, -52, -128, 0, (byte)94};
    protected final byte[] CONTROLLER_TYPE_LEGACY = { 0, 77, 77};       // Controller type USB Motor Module

    public int write(byte[] data, int length, boolean wait)
    {

        int rc = 0;

        if (length <= 0) {
            return rc;
        }

        Log.v(mFT_DeviceDescription, "WRITE(): Buffer len=" + length + " (" + bufferToHexString(data, 0, length) + ")");

        // Write Command
        if (data[0] == WRITE_COMMAND[0] && data[2] == WRITE_COMMAND[2]) {  // WRITE_COMMAND


            // If size is 94(0xd0) bytes then they are writing a full buffer of data to all ports.
            // Note: the buffer we were giving in this case is 94+5 bytes because the "WRITE_COMMAND" header is attached
            if (data[4] == (byte)94 ) {

                queueUpForReadFromPhone(RECEIVE_SYNC_COMMAND_0); // Reply, we got your WRITE_COMMAND

                // Now, the reset of the buffer minus the 5 header bytes should be 94 (0xd0) bytes.
                // These 94 bytes need to be written to the connected module.
                // Write the entire received buffer into the mCurrentState buffer.
                // We will use this buffer when the ftc_app askes for the current state of the module.

                // Write the 94 buffer to the NetworkManager queue to be sent to the PC Simulator
                super.sendPacketToPC(data, 5, 94);

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
        } else if (data[0] == READ_COMMAND[0] && data[2] == READ_COMMAND[2]) { // READ_COMMAND
            if (data[4] == 3) { // Android asks for 3 bytes, initial query of device type
                queueUpForReadFromPhone(RECIEVE_SYNC_COMMAND_3);  // Send receive sync, bytes to follow
                queueUpForReadFromPhone(CONTROLLER_TYPE_LEGACY);
            } else if (data[4] == (byte)94) { // Android asks for 94 bytes, full read of device
                queueUpForReadFromPhone(RECEIVE_SYNC_COMMAND_94);  // Send receive sync loop back
                sendPacketToPC(data,0,5);   // Send the actual message to the PC so it can respond
            }
        }

        rc = length;
        return rc;
    }

}
