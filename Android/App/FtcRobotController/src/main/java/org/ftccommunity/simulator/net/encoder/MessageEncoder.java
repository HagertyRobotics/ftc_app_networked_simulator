package org.ftccommunity.simulator.net.encoder;

import android.util.Log;

import org.ftccommunity.simulator.net.protocol.SimulatorData;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<SimulatorData.Data> {
    public MessageEncoder() {
        super(false);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          SimulatorData.Data msg, ByteBuf out) throws Exception {
        int length = msg.getSerializedSize();
        out.writeInt(length);
        out.ensureWritable(length);
        final int offset = out.arrayOffset() + out.writerIndex();
        final byte[] array = out.array();
        out.writerIndex(out.writerIndex() + encode(msg, array, offset, length));
        if (msg.getType().getType() != SimulatorData.Type.Types.HEARTBEAT) {
            Log.d("SIM_NETWORKING::", "Encoded data type " +
                    msg.getType().getType().getValueDescriptor().getName());
        }
    }

    private int encode(final SimulatorData.Data msg, byte[] array, final int offset, final int length) {
        System.arraycopy(msg.toByteArray(), 0, array, offset, length);
        return length;
    }
}
