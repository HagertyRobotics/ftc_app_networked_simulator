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
    byte[] write_legacy_buffer= new byte[255];;
    byte[] read_legacy_buffer= new byte[255];;
    
    protected final byte[] mCurrentStateBuffer = new byte[208];
    		
    // Packet types and constants
    protected final byte ADDRESS_VERSION_NUMBER=(byte) 0;
    protected final byte ADDRESS_MANUFACTURER_CODE=(byte) 1;
    protected final byte ADDRESS_DEVICE_ID=(byte) 2;
    protected final byte SEND_SYNC_BYTE_1=(byte) 0x55;
    protected final byte SEND_SYNC_BYTE_2=(byte) 0xaa;
    protected final byte REC_SYNC_BYTE_1=(byte) 0x33;
    protected final byte REC_SYNC_BYTE_2=(byte) 0xcc;
    protected final byte WRITE_MASK=(byte) 0;
    protected final byte READ_MASK=(byte) -128;
    protected final byte EMPTY_RESPONSE_BYTE=(byte) 0;
    
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
    	byte rchar;
		int state = 0;
		byte[] rec_packet = new byte[10];
		byte[] out_buf = new byte[5];
		int count = 0;
    	int i;
    	
    	for (i=0;i<255;i++) read_legacy_buffer[i]=0;
    	for (i=0;i<255;i++) read_legacy_buffer[i]=0;
    	
		// Create a socket object from the ServerSocket to listen and accept 
		// connections.
		// Open input and output streams
		try {
			ftcServer = new ServerSocket(SERVERPORT);
			clientSocket = ftcServer.accept();
			is = new DataInputStream(clientSocket.getInputStream());
			os = new DataOutputStream(clientSocket.getOutputStream());
		
	     
	        while (true) {
				rchar = is.readByte();
	
				switch (state) {
				case 0:
					rec_packet[0] = rchar;
					if (rchar == SEND_SYNC_BYTE_1) state = 1;
				break;
				case 1:
					rec_packet[1] = rchar;
					if (rchar == SEND_SYNC_BYTE_2) state = 2;
					else if (rchar == SEND_SYNC_BYTE_1) state = 1;
					else state = 0;
				break;
				case 2:
					rec_packet[2] = rchar;
					state = 3;
				break;
				case 3:
					rec_packet[3] = rchar;
					state = 4;
				break;
				case 4:
					rec_packet[4] = rchar;
					state = 0;
					//
					// Read state
					//
					if (rec_packet[2] == (byte)0x80) {  
						if (rec_packet[4] == 3) {  // Android asking what type of controller we are
	
							// Send a packet to Android saying we are writing 3 bytes
							out_buf[0] = REC_SYNC_BYTE_1;
							out_buf[1] = REC_SYNC_BYTE_2;
							out_buf[2] = 0x00;
							out_buf[3] = 0x00;
							out_buf[4] = 0x03;
							os.write(out_buf,0,5);
	
							// Send 3 bytes with controller type
							out_buf[0] = 0x00;
							out_buf[1] = 77;
							out_buf[2] = 73;
							os.write(out_buf,0,3);
						}
						if (rec_packet[4] == (byte)208) {
	
							// Send a packet to Android saying we are about to send 208 bytes
							out_buf[0] = REC_SYNC_BYTE_1;
							out_buf[1] = REC_SYNC_BYTE_2;
							out_buf[2] = (byte) 0x80;
							out_buf[3] = 0x00;
							out_buf[4] = (byte) 208;
							os.write(out_buf,0,5);
							System.out.println("OS: Read Sync");
							
							// Send the Current Buffer
							os.write(mCurrentStateBuffer,0, 208);
							System.out.println("OS: 208 Byte Buffer");
						}
					//
					// Write State
					//
					} else {
						// Send a packet to Android acknowledging their writing 208 bytes
						out_buf[0] = REC_SYNC_BYTE_1;
						out_buf[1] = REC_SYNC_BYTE_2;
						out_buf[2] = (byte) 0x80;
						out_buf[3] = 0x00;
						out_buf[4] = 0;
						os.write(out_buf,0, 5);
						System.out.println("OS: Write Sync");
						
						// Read the incoming buffer using a routine that keeps reading until all bytes are
						// read.  DataInputStream will return a partial read if not all bytes are available.
						readBytesFromAndroid(mCurrentStateBuffer, 208);
						System.out.println("IS: Read 208: " + bufferToHexString(mCurrentStateBuffer,0,20));
						
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
				break;
				} // switch
	        } // while
		} catch (IOException e) {
			System.out.println(e);
		}
    }
    
    private void readBytesFromAndroid(byte[] data, int length) {
        int totalRead = 0;

        while (totalRead < length) {
            int bytesRead = 0;
            try {
                bytesRead = is.read(data, totalRead, length - totalRead);
            } catch (IOException e) {
                System.out.println("Error: " + e);
                e.printStackTrace();
            }

            totalRead += bytesRead;
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






		
