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
    protected void encode(ChannelHandlerContext channelHandlerContext, SimulatorData.Data o, ByteBuf byteBuf) throws Exception {
        int length = o.getSerializedSize();
        byteBuf.writeInt(length);
        byteBuf.ensureWritable(length);
        final int offset = byteBuf.arrayOffset() + byteBuf.writerIndex();
        final byte[] array = byteBuf.array();
        byteBuf.writerIndex(byteBuf.writerIndex() + encode(o, array, offset, length));
    }

    private int encode(final SimulatorData.Data msg, byte[] array, final int offset, final int length) {
        System.arraycopy(msg.toByteArray(), 0, array, offset, length);
        return length;
    }
}
