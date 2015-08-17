package org.ftccommunity.simulator.io.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;

@Deprecated
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
/*    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SimulatorData.Data data = (SimulatorData.Data) msg;
        if (data.getType().getType() == SimulatorData.Type.Types.HEARTBEAT) {
            InetAddress address = InetAddresses.forString(data.getInfo(0));
            try {
                if (address.isReachable(100)) {
                    NetworkManager.changeReadiness(true);
                    NetworkManager.setRobotAddress(address);
                } else {
                    NetworkManager.changeReadiness(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }*/
}
