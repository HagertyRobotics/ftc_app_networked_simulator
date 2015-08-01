package hagerty.simulator.io;

import com.google.common.base.Charsets;
import hagerty.simulator.NetworkManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.ftccommunity.simulator.net.SimulatorData;

import java.nio.charset.StandardCharsets;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
        SimulatorData.Data data = (SimulatorData.Data) msg;

        for (byte test : data.getInfo(0).getBytes(Charsets.US_ASCII)) {
            System.out.print(String.format("0x%02X ", test));
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
    public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
