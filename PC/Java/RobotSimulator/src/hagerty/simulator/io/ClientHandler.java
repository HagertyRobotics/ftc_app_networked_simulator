package hagerty.simulator.io;

import com.google.common.base.Charsets;
import com.google.common.net.HostAndPort;
import hagerty.simulator.HeartbeatTask;
import hagerty.simulator.NetworkManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.ftccommunity.simulator.net.SimulatorData;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SimulatorData.Data data = (SimulatorData.Data) msg;

        for (byte test : data.getInfo(0).getBytes(Charsets.US_ASCII)) {
            System.out.print(String.format("0x%02X ", test));
        }
        // We don't need to queue heartbearts (OPT_DATA2)
        if (data.getType().getType() != SimulatorData.Type.Types.OPT_DATA2) {
            NetworkManager.add(data);
        } else { // Acknowledge an OPT_DATA2 with another Heartbeat
            ctx.write(HeartbeatTask.buildMessage());
        }


        System.out.println();
        SimulatorData.Data next  = NetworkManager.getNextSend();
        if (next != null) {
            final ByteBuf writeBuffer = ctx.alloc().buffer(4 + data.getSerializedSize());
            writeBuffer.writeInt(data.getSerializedSize());
            writeBuffer.writeBytes(data.toByteArray());
            ctx.writeAndFlush(writeBuffer);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        HeartbeatTask.setPort(7002);
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
    public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
