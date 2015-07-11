import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;

public class ControllerSimulator implements Runnable {

    private LinkedBlockingQueue<ControllerData> mQueue;

    int mPhonePort;
    InetAddress mPhoneIPAddress;
    DatagramSocket mServerSocket;
    
    ControllerData mDataPacket;
    
    byte[] mReceiveData = new byte[1024];             
    byte[] mSendData = new byte[1024];
    
    protected final byte[] mCurrentStateBuffer = new byte[208];

    
    /** Default Constructor.
     *  
     */
    public ControllerSimulator(LinkedBlockingQueue<ControllerData> queue) {
        mQueue = queue;

        for (int i=0;i<mCurrentStateBuffer.length;i++) 
        	mCurrentStateBuffer[i] = 0;
        
        try {
        	mServerSocket = new DatagramSocket(6500);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
    	byte[] packet;
    	
    	try {
            while (true) {
            	packet = receivePacketFromPhone();  
            	handleIncomingPacket(packet, packet.length, false);
            }
            // Catch unhandled exceptions and cleanup
    	} catch (Exception e) {
    		e.printStackTrace();
    		close();
    	}

    }
    
    public void close() {
    	try {
    		mServerSocket.close();
    		mQueue.clear();
    	} catch (Exception ex) {
    		System.out.println("An error occurred while closing!");
    		ex.printStackTrace();
    	}
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
    
     /*
      * receivePacketFromPhone()
      * 
      */
     private byte[] receivePacketFromPhone() {
    	
    	DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
    	try {
    		mServerSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    	
    	// Get the port and address of the sender from the incoming packet and set some global variables
    	// to be used when we reply back.
    	// TODO: do we need to set this every time?
    	mPhonePort = receivePacket.getPort();
    	mPhoneIPAddress = receivePacket.getAddress(); 
    	
    	// Make a copy of the packet.  Not sure if we need to do this.  Might not hold on to it for long.
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
                      
    
    public void handleIncomingPacket(byte[] data, int length, boolean wait)
    {
    	System.out.println("Receive Buffer: (" + bufferToHexString(data,0,25) + ") len=" + data.length);
    	if (data[0] == readCmd[0] && data[2] == readCmd[2]) { // readCmd
            if (data[4] == 3) { // Android asks for 3 bytes, initial query of device type
                //queueUpForReadFromPhone(recSyncCmd3);  // Send receive sync, bytes to follow
                //queueUpForReadFromPhone(controllerTypeLegacy);
            } else if (data[4] == (byte)208) { // Android asks for 208 bytes, full read of device
                // Send receive sync, bytes to follow
                sendPacketToPhone(recSyncCmd208);
                sendPacketToPhone(mCurrentStateBuffer);
            }
        // Write Command
    	} else if (data[0] == writeCmd[0] && data[2] == writeCmd[2]) { 
    		
            // If size is 208(0xd0) bytes then they are writing a full buffer of data to all ports.
            // Note: the buffer we were giving in this case is 208+5 bytes because the "writeCmd" header is attached
            if (data[4] == (byte)0xd0 ) {
                sendPacketToPhone(recSyncCmd0); // Reply, we got your writeCmd
		
    		
	    		// Process the received data packet
	            // Loop through each of the 6 ports and see if the Action flag is set.  
	            // If set then copy the 32 bytes for the port into the CurrentStateBuffer
	    	
	            //for (int i=0;i<6;i++) 
	            int i=0;
	            int p=16+i*32;
		    	// This is for Port P0 only.  16 is the base offset.  Each port has 32 bytes.
		        // If I2C_ACTION is set, take some action
//		        if (data[5+p+32] == (byte)0xff) { // Action flag
		            if ((data[5+p] & (byte)0x01) == (byte)0x01) { // I2C Mode
		                if ((data[5+p] & (byte)0x80) == (byte)0x80) { // Read mode
		                	// Copy this port's 32 bytes into buffer 
		                	System.arraycopy(data, 5+p, mCurrentStateBuffer, p, 32);
		
		                } else { // Write mode
		                	// Copy this port's 32 bytes into buffer 
		                	System.arraycopy(data, 5+p, mCurrentStateBuffer, p, 32);
		                    
		                	
		                	ControllerData cd = new ControllerData();
		                	float m1 = (float)mCurrentStateBuffer[p+4+5]/100.0f;
		                	float m2 = (float)mCurrentStateBuffer[p+4+6]/100.0f;
		                	
		                	System.out.println("motor 1: " + m1 + " motor_2: " + m2);
		                	
		                	cd.setMotorSpeed(1, m1);
		                	cd.setMotorSpeed(2, m2);
		                    mQueue.add(cd);
		                }
		                // Set the Port S0 ready bit in the global part of the Current State Buffer
	                    mCurrentStateBuffer[3] = (byte)0xfe;  // Port S0 ready
		            }
//		        }
            }
    	}  
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



