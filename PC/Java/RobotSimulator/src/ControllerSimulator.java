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
    
    private byte[] receivePacketFromPhone() {
    	
    	DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
    	try {
    		mServerSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
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
    	// This is for Port P0 only.  16 is the base offset.  Each port has 32 bytes.
        // If I2C_ACTION is set, take some action
//        if (data[47] == (byte)0xff) { // Action flag
//            if ((data[16] & (byte)0x01) == (byte)0x01) { // I2C Mode
//                if ((data[16] & (byte)0x80) == (byte)0x80) { // Read mode
//
//
//                } else { // Write mode
                	ControllerData cd = new ControllerData();
                	float m1 = (float)data[16+4+5]/100.0f;
                	float m2 = (float)data[16+4+6]/100.0f;
                	
                	System.out.println("motor 1: " + m1 + " motor_2: " + m2);
                	
                	cd.setMotorSpeed(1, m1);
                	cd.setMotorSpeed(2, m2);
                    mQueue.add(cd);
//                }
//            }
//        }

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




