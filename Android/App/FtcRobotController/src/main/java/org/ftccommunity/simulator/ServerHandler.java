package org.ftccommunity.simulator;

import org.ftccommunity.simulator.protobuf.SimulatorData;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // Return the data
        ByteBuf da = (ByteBuf) msg;
        ctx.write(da); // (1)
        ctx.flush();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        SimulatorData.Data.Builder dataBuilder = SimulatorData.Data.newBuilder()
                .setType(SimulatorData.Type.newBuilder().setType(SimulatorData.Type.Types.LEGACY_MOTOR))
                .setModule(SimulatorData.Data.Modules.LEGACY_CONTROLLER)
                .setDataName("info")
                .addInfo(new String(new byte[]{
                        34, 43, 90
                }, StandardCharsets.US_ASCII));
        SimulatorData.Data data = dataBuilder.build();

        final ByteBuf time = ctx.alloc().buffer(4 + data.getSerializedSize());
        time.writeInt(data.getSerializedSize());
        time.writeBytes(data.toByteArray());

        ctx.writeAndFlush(time);
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