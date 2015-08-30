package org.ftccommunity.simulator.io.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.ftccommunity.simulator.net.manager.NetworkManager;
import org.ftccommunity.simulator.net.protocol.SimulatorData;
import org.ftccommunity.simulator.net.tasks.HeartbeatTask;

import java.io.IOException;
import java.net.SocketException;

public class ClientHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SimulatorData.Data received = (SimulatorData.Data) msg;

        // Uncomment to display all data
        /*for (byte test : data.getInfo(0).getBytes(Charsets.US_ASCII)) {
            System.out.print(String.format("0x%02X ", test));
        }*/

        // We don't need to queue heartbearts (HEARTBEAT)
        if (received.getType().getType() != SimulatorData.Type.Types.HEARTBEAT) {
            // Print out size and data
            // System.out.println("Received Data of significance with size=" + data.getSerializedSize());
            NetworkManager.add(received);
        }

        while (ctx.channel().isWritable()) {
            SimulatorData.Data data = NetworkManager.getNextSend();
            if (data != null) {
                ctx.write(data);
            } else {
                break;
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE ||
                        e.state() == IdleState.READER_IDLE ||
                        e.state() == IdleState.ALL_IDLE) {
                while (ctx.channel().isWritable()) {
                    SimulatorData.Data data = NetworkManager.getNextSend();
                    if (data != null) {
                        ctx.write(data);
                    } else {
                        ctx.write(HeartbeatTask.buildMessage());
                        break;
                    }
                }
                ctx.flush();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException) {
            cause.printStackTrace();
        } else if (cause instanceof IOException) {
            cause.printStackTrace();
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
