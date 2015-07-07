import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;

public class ControllerSimulator implements Runnable {

    private LinkedBlockingQueue<ControllerData> queue;

    int mPhonePort;
    InetAddress mPhoneIPAddress;
    DatagramSocket mServerSocket;
    
    ControllerData mDataPacket;
    byte[] mReceiveData = new byte[1024];             
    byte[] mSendData = new byte[1024];
    
    protected final byte[] mCurrentStateBuffer = new byte[208];
    		
    // Packet types
    protected final byte[] writeCmd = { 85, -86, 0, 0, 0 };
    protected final byte[] readCmd = { 85, -86, -128, 0, 0 };
    protected final byte[] recSyncCmd3 = { 51, -52, 0, 0, 3};
    protected final byte[] recSyncCmd0 = { 51, -52, -128, 0, 0};
    protected final byte[] recSyncCmd208 = { 51, -52, -128, 0, (byte)208};
    protected final byte[] controllerTypeLegacy = { 0, 77, 73};       // Controller type USBLegacyModule

    long mTimeInMilliseconds=0;
    long mOldTimeInMilliseconds=0;
    long mDeltaWriteTime=0;
    
    /** Default Constructor.
     *  
     */
    public ControllerSimulator(LinkedBlockingQueue<ControllerData> queue) {
        this.queue = queue;

        try {
        	mServerSocket = new DatagramSocket(6500);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
    	byte[] packet;
    	
        while (true) {
        	packet = receivePacketFromPhone();  
        	handleIncomingPacket(packet, packet.length, false);
        }
    }
    
    private byte[] receivePacketFromPhone() {
    	
    	DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
    	try {
    		mServerSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	// Set the global port and address of the sender.
    	// TODO: do we need to set this every time?
    	mPhonePort = receivePacket.getPort();
    	mPhoneIPAddress = receivePacket.getAddress(); 
    	byte[] mypacket = new byte[receivePacket.getLength()];
    	System.arraycopy(receivePacket.getData(), 0, mypacket, 0, receivePacket.getLength());

    	return mypacket;
    }
    
    
    private void sendPacketToPhone(byte[] sendData) {
    	try {	
    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mPhoneIPAddress, mPhonePort);
        	mServerSocket.send(sendPacket);
        	System.out.println("sendPacketToPhone: (" + bufferToHexString(sendData,0,sendData.length) + ") len=" + sendData.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
                      
    
    public int handleIncomingPacket(byte[] data, int length, boolean wait)
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
            	System.out.println("WRITE: Write Header (" + bufferToHexString(data,0,5) + ") len=" + length);
                sendPacketToPhone(recSyncCmd0); // Reply we got your writeCmd

                System.out.println("WRITE: Write Buffer S0 (" + bufferToHexString(data, 5+16+4, 20) + ") len=" + length);
                System.out.println("WRITE: Write Buffer FLAGS 0=" + bufferToHexString(data,5+0,3) + " 16=" + bufferToHexString(data,5+16,4) + "47=" + bufferToHexString(data,5+47,1));

                // Now, the reset of the buffer minus the header 5 bytes should be 208 (0xd0) bytes that need to be written to the connected devices
                // Write the entire received buffer into the mCurrentState buffer so the android can see what we are up to
                // Note: the buffer we were giving in this case is 208+5 bytes because the "writeCmd" header is attached
                System.arraycopy(data, 5, mCurrentStateBuffer, 0, 208);

                // Check delta time to see if we are too slow in our simulation.
                // Baud rate was 250,000 with real USB port connected to module
                // We are getting deltas of 31ms between each write call
                mTimeInMilliseconds = System.currentTimeMillis();
                mDeltaWriteTime = mTimeInMilliseconds - mOldTimeInMilliseconds;
                mOldTimeInMilliseconds = mTimeInMilliseconds;
                System.out.println("Delta Time = " + mDeltaWriteTime);

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
            	System.out.println("WRITE: Read Header (" + bufferToHexString(data,0,length) + ") len=" + length);
            	sendPacketToPhone(recSyncCmd3);  // Send receive sync, bytes to follow
            	sendPacketToPhone(controllerTypeLegacy);
            } else if (data[4] == (byte)208) { // Android asks for 208 bytes, full read of device
            	System.out.println("WRITE: Read Header (" + bufferToHexString(data,0,length) + ") len=" + length);
            	sendPacketToPhone(recSyncCmd208);  // Send receive sync, bytes to follow
            	sendPacketToPhone(mCurrentStateBuffer); //
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
	
}




