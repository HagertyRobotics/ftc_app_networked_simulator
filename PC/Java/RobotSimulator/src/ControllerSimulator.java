import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class ControllerSimulator implements Runnable {

    private LinkedBlockingQueue<ControllerData> queue;
    
 // the Server's Port
    public static final int SERVERPORT  = 6500;

    int mPhonePort;
    InetAddress mPhoneIPAddress;
    
    ServerSocket ftcServer = null;
    DataInputStream is = null;
    DataOutputStream os = null;
    Socket clientSocket = null;

    
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
    }
 

    @Override
    public void run() {
    	
    	 // Create a socket object from the ServerSocket to listen and accept 
		 // connections.
		 // Open input and output streams
	     try {
	    	 ftcServer = new ServerSocket(SERVERPORT);
	    	 clientSocket = ftcServer.accept();
	    	 is = new DataInputStream(clientSocket.getInputStream());
	    	 os = new DataOutputStream(clientSocket.getOutputStream());
	     } catch (IOException e) {
	    	 System.out.println(e);
	     }
	     
        while (true) {
        	try {
				System.out.println(is.read());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	//packet = receivePacketFromPhone();  
        	//handleIncomingPacket(packet, packet.length, false);
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




