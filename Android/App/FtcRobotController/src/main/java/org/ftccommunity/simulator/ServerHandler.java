package org.ftccommunity.simulator;

import com.ftdi.j2xx.NetworkManager;
import com.google.common.base.Charsets;

import org.ftccommunity.simulator.net.HeartbeatTask;
import org.ftccommunity.simulator.net.SimulatorData;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        SimulatorData.Data receiveData = (SimulatorData.Data) msg;
       /* try {
            Thread.sleep(2);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }*/

        // TODO: write heartbeat
        // ctx.executor().scheduleAtFixedRate()
        // We don't need to queue heartbearts (OPT_DATA2)
        if (receiveData.getType().getType() != SimulatorData.Type.Types.OPT_DATA2) {
            NetworkManager.add(receiveData);
        } else { // Acknowledge an OPT_DATA2 with another Heartbeat
            ctx.write(HeartbeatTask.buildMessage());
        }

        for (SimulatorData.Data data: NetworkManager.getWriteData()) {
            ctx.write(data);
        }
        ctx.flush();
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
       /*f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                // assert f == future;
                ctx.close();
            }
        });*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}