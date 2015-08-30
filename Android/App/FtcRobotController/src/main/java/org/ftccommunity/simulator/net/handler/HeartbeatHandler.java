package org.ftccommunity.simulator.net.handler;

import android.util.Log;

import org.ftccommunity.simulator.net.protocol.SimulatorData;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SimulatorData.Data data = (SimulatorData.Data) msg;
        if (data.getType().getType() == SimulatorData.Type.Types.HEARTBEAT) {
            Log.i("HeartbeatHandler:: ", "Got HEARTBEAT!");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
