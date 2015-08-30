package org.ftccommunity.simulator.net.handler;

import org.ftccommunity.simulator.net.NetworkManager;
import com.google.common.base.Charsets;
import com.qualcomm.robotcore.util.RobotLog;

import org.ftccommunity.simulator.net.protocol.SimulatorData;
import org.ftccommunity.simulator.net.tasks.HeartbeatTask;

import java.io.IOException;
import java.net.SocketException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        SimulatorData.Data receiveData = (SimulatorData.Data) msg;
        // We don't need to queue heartbearts (HEARTBEAT), just reply back
        if (receiveData.getType().getType() != SimulatorData.Type.Types.HEARTBEAT) {
            NetworkManager.add(receiveData);
        }

        while (ctx.channel().isWritable()) {
            SimulatorData.Data data = NetworkManager.getNextSend();
            if (data != null) {
                ctx.write(data);
            } else {
                break;
            }
        }/*// Write and send the data according to protocol (send size of data then the data itself)
        final SimulatorData.Data[] writeData = NetworkManager.getWriteData();
        for (SimulatorData.Data data : writeData) {
            writeMessage(ctx.channel(), data);
        }*/
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE ||
                    e.state() == IdleState.READER_IDLE ||
                    e.state() == IdleState.ALL_IDLE) {
                ctx.writeAndFlush(HeartbeatTask.buildMessage());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        SimulatorData.Data.Builder dataBuilder = SimulatorData.Data.newBuilder()
                .setType(SimulatorData.Type.newBuilder().setType(SimulatorData.Type.Types.HEARTBEAT))
                .setModule(SimulatorData.Data.Modules.LEGACY_CONTROLLER)
                .setDataName("info")
                // Completely arbitrary
                .addInfo(new String(new byte[]{
                        34, 43, 90, 127, 76, 97
                }, Charsets.US_ASCII));
        SimulatorData.Data data = dataBuilder.build();
        ctx.writeAndFlush(data);
//        final ByteBuf time = ctx.alloc().buffer(4 + data.getSerializedSize());
//        time.writeInt(data.getSerializedSize());
//        time.writeBytes(data.toByteArray());

//        ctx.writeAndFlush(time);
        NetworkManager.setServerWorking(true);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        // Write and send the data according to protocol (send size of data then the data itself)
        while (ctx.channel().isWritable()) {
            SimulatorData.Data data = NetworkManager.getNextSend();
            if (data != null) {
                ctx.write(data);
            } else {
                break;
            }
        }
        ctx.flush();
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
}