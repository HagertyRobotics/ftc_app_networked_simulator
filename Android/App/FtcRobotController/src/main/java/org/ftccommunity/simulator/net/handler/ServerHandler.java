package org.ftccommunity.simulator.net.handler;

import com.ftdi.j2xx.NetworkManager;
import com.google.common.base.Charsets;
import com.qualcomm.robotcore.util.RobotLog;

import org.ftccommunity.simulator.net.protocol.SimulatorData;

import java.io.IOException;
import java.net.SocketException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        SimulatorData.Data receiveData = (SimulatorData.Data) msg;
        // We don't need to queue heartbearts (HEARTBEAT), just reply back
        if (receiveData.getType().getType() != SimulatorData.Type.Types.HEARTBEAT) {
            NetworkManager.add(receiveData);
        }
        /*else { // Acknowledge an HEARTBEAT with another Heartbeat
            SimulatorData.Data heartbeat = HeartbeatTask.buildMessage();
            final ByteBuf time = ctx.alloc().buffer(4 + heartbeat.getSerializedSize());
            time.writeInt(heartbeat.getSerializedSize());
            time.writeBytes(heartbeat.toByteArray());
            while (!ctx.channel().isWritable()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ctx.close();
                    return;
                }
            }
            ctx.writeAndFlush(time);
        }*/

        // Write and send the data according to protocol (send size of data then the data itself)
        final SimulatorData.Data[] writeData = NetworkManager.getWriteData();
//        int size = 0;
//        for (SimulatorData.Data data : writeData) {
//            size += data.getSerializedSize() + 4;
//        }

        // final ByteBuf buf = ctx.alloc().buffer(size);
        //            buf.writeInt(data.getSerializedSize());
//            buf.writeBytes(data.toByteArray());
        for (SimulatorData.Data data : writeData) {
            writeMessage(ctx.channel(), data);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // NetworkManager.mWriteToPcQueueNew = new PendingWriteQueue(ctx);
        //this.ctx = ctx;
        SimulatorData.Data.Builder dataBuilder = SimulatorData.Data.newBuilder()
                .setType(SimulatorData.Type.newBuilder().setType(SimulatorData.Type.Types.HEARTBEAT))
                .setModule(SimulatorData.Data.Modules.LEGACY_CONTROLLER)
                .setDataName("info")
                // Completely arbitrary
                .addInfo(new String(new byte[]{
                        34, 43, 90, 127, 76, 97
                }, Charsets.US_ASCII));
        SimulatorData.Data data = dataBuilder.build();

        final ByteBuf time = ctx.alloc().buffer(4 + data.getSerializedSize());
        time.writeInt(data.getSerializedSize());
        time.writeBytes(data.toByteArray());

        ctx.writeAndFlush(time);
        NetworkManager.setServerWorking(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException) {
            RobotLog.e(cause.toString());
        } else if (cause instanceof IOException) {
            RobotLog.e(cause.toString());
        } else {
            ctx.close();
        }
    }

    private void writeMessage(Channel channel, SimulatorData.Data data) {
        while (!channel.isWritable()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        channel.write(data);
    }
}