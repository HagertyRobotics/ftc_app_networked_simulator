package org.ftccommunity.simulator.net.encoder;

import org.ftccommunity.simulator.net.protocol.SimulatorData;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<SimulatorData.Data> {
    public MessageEncoder() {
        super(false);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, SimulatorData.Data msg, ByteBuf out) throws Exception {
        int length = msg.getSerializedSize();
        out.ensureWritable(4 + length);

        out.writeInt(length);
        out.writeBytes(msg.toByteArray());
    }

    private int encode(final SimulatorData.Data msg, byte[] array, final int offset, final int length) {
        System.arraycopy(msg.toByteArray(), 0, array, offset, length);
        return length;
    }
}
