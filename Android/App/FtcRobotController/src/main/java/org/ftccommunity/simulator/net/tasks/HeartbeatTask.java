package org.ftccommunity.simulator.net.tasks;

import com.google.common.base.Charsets;

import org.ftccommunity.simulator.net.protocol.SimulatorData;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;

public class HeartbeatTask implements Runnable {
    private static int port = 0;
    private Channel chl;

    public HeartbeatTask(Channel channel, int bondedPort) {
        this.chl = channel;
    }

    public static synchronized void setPort(int bondedPort) {
        port = bondedPort;
    }

    public static SimulatorData.Data buildMessage() {
        // ByteBuf info = new EmptyByteBuf(new PooledByteBufAllocator(false));
        String infoString = "";
        try {

            infoString = InetAddress.getLocalHost().getHostAddress() + "%20" + "null" + "%20" + port +
                    "%20";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        byte[] dataEncoded = infoString.getBytes(Charsets.US_ASCII);
        infoString += dataEncoded.length;
        dataEncoded = infoString.getBytes(Charsets.US_ASCII);

        // info.writeBytes(dataEncoded);
        SimulatorData.Data.Builder dataBuilded = SimulatorData.Data.newBuilder()
                .setType(SimulatorData.Type.newBuilder()
                        .setType(SimulatorData.Type.Types.HEARTBEAT))
                .setModule(SimulatorData.Data.Modules.LEGACY_CONTROLLER)
                .addInfo((new PingWebSocketFrame()).toString());
        return dataBuilded.build();
    }

    /**
     * This generates and sends a Ping request to the server; the data layout in the ping request is: the IP
     * address of the client (encoded in byte form via US_ASCII), the seen IP address (or hostname) of the server,
     * the port we are listening on, and a size of the previous data
     */
    @Override
    public void run() {
        ByteBuf info = new EmptyByteBuf(new PooledByteBufAllocator(false));
        String infoString = "";
        try {
            infoString = InetAddress.getLocalHost().getHostAddress() + "%20" + "null" + "%20" + port +
                    "%20";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        byte[] dataEncoded = infoString.getBytes(Charsets.US_ASCII);
        infoString += dataEncoded.length;
        dataEncoded = infoString.getBytes(Charsets.US_ASCII);

        info.writeBytes(dataEncoded);
        SimulatorData.Data.Builder dataBuilder = SimulatorData.Data.newBuilder()
                .setType(SimulatorData.Type.newBuilder()
                        .setType(SimulatorData.Type.Types.HEARTBEAT))
                .setModule(SimulatorData.Data.Modules.LEGACY_CONTROLLER)
                .addInfo((new PingWebSocketFrame(info)).toString());
        chl.writeAndFlush(dataBuilder.build());
    }
}
