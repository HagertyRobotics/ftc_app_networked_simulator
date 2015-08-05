package org.ftccommunity.simulator.net.handler;

import com.ftdi.j2xx.NetworkManager;
import com.google.common.base.Charsets;

import org.ftccommunity.simulator.net.tasks.HeartbeatTask;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        SimulatorData.Data receiveData = (SimulatorData.Data) msg;
        // We don't need to queue heartbearts (OPT_DATA2), just reply back
        if (receiveData.getType().getType() != SimulatorData.Type.Types.OPT_DATA2) {
            NetworkManager.add(receiveData);
        } else { // Acknowledge an OPT_DATA2 with another Heartbeat
            SimulatorData.Data heartbeat = HeartbeatTask.buildMessage();

            final ByteBuf time = ctx.alloc().buffer(4 + heartbeat.getSerializedSize());
            time.writeInt(heartbeat.getSerializedSize());
            time.writeBytes(heartbeat.toByteArray());
            ctx.writeAndFlush(time);
        }

        // Write and send the data according to protocol (send size of data then the data itself)
        SimulatorData.Data[] writeData = NetworkManager.getWriteData();
        int size = 0;
        for (SimulatorData.Data data : writeData) {
            size += data.getSerializedSize() + 4;
        }
        final ByteBuf buf = ctx.alloc().buffer(size);
        for (SimulatorData.Data data: writeData) {
            buf.writeInt(data.getSerializedSize());
            buf.writeBytes(data.toByteArray());
        }
        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // NetworkManager.mWriteToPcQueueNew = new PendingWriteQueue(ctx);
        //this.ctx = ctx;
        SimulatorData.Data.Builder dataBuilder = SimulatorData.Data.newBuilder()
                .setType(SimulatorData.Type.newBuilder().setType(SimulatorData.Type.Types.OPT_DATA2))
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
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}