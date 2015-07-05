package com.ftdi.j2xx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * NetworkSender
 * Send packets to the PC simulator.
 * Pull the packets from the passed queue.  Packets were queued in the FT_Device class after begin
 * received from the USB transmit functions of this app.  The FT_Device class is simulating the
 * USB stack using a UDP network connection to the PC simulator.
 *
 */
public class NetworkSender implements Runnable {
    private LinkedBlockingQueue queue;

    DatagramSocket mSocket;
    InetAddress IPAddress;

    public NetworkSender(LinkedBlockingQueue queue, DatagramSocket mySocket) {
        this.queue = queue;
        this.mSocket = mySocket;

        try {
            IPAddress = InetAddress.getByName("10.0.1.193");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        byte[] writeBuf;

        while (true) {
            try {
                writeBuf = (byte[])queue.take();
                DatagramPacket send_packet = new DatagramPacket(writeBuf,writeBuf.length, IPAddress, 6500);
                mSocket.send(send_packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

