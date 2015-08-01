package hagerty.simulator.io;

import com.google.common.base.Charsets;
import com.google.common.net.InetAddresses;
import com.google.protobuf.InvalidProtocolBufferException;
import hagerty.simulator.NetworkManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.ftccommunity.simulator.net.SimulatorData;
import sun.nio.ch.Net;

import java.io.IOException;
import java.net.InetAddress;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SimulatorData.Data data = (SimulatorData.Data) msg;
        if (data.getType().getType() == SimulatorData.Type.Types.OPT_DATA2) {
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
    }
}
