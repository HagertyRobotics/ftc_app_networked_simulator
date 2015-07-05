package com.ftdi.j2xx;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * NetworkRecevier
 * Receive packets from the PC simulator and feed them into a queue that the FT_Device class will read
 * The FT_Device class will pretend to be a FTDI USB device and feed the
 * received packets to the FTC_APP
 *
 */
public class NetworkReceiver implements Runnable {
    private LinkedBlockingQueue queue;
    DatagramSocket mSocket;
    byte[] mReceiveData = new byte[1024];

    public NetworkReceiver(LinkedBlockingQueue queue, DatagramSocket my_socket) {
        this.queue = queue;
        this.mSocket = my_socket;
    }


    @Override
    public void run() {

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(mReceiveData, mReceiveData.length);
            try {
                Log.v("D2xx::", "Before receive");
                mSocket.receive(receivePacket);
                Log.v("D2xx::", "After receive");
                byte[] readBuffer = new byte[receivePacket.getLength()];
                System.arraycopy(receivePacket.getData(), 0, readBuffer, 0, receivePacket.getLength());
                queue.put(readBuffer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
