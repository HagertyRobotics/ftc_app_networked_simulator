package org.ftccommunity.simulator.net.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageEncoder extends MessageToByteEncoder<SimulatorData.Data> {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public MessageEncoder() {
        super(false);
    }

/*
    @Override
    protected void encode(ChannelHandlerContext ctx, SimulatorData.Data msg, ByteBuf out) throws Exception {
        int length = msg.getSerializedSize();
        out.ensureWritable(4 + length);

        out.writeInt(length);
        out.writeBytes(msg.toByteArray());
    }*/


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          SimulatorData.Data msg, ByteBuf out) throws Exception {
        /*
        // Setup for write
        int length = msg.getSerializedSize();
        out.writeInt(length);
        out.ensureWritable(length);
        final int offset = out.arrayOffset() + out.writerIndex();

        // Write the data to the backing array
        final byte[] array = out.array();
        out.writerIndex(out.writerIndex() + encode(msg, array, offset, length));
        */
       /*/ final int length = msg.getSerializedSize();
        out.ensureWritable(4 + length);
        out.writeInt(length);
        out.writeBytes(msg.toByteArray());

        if (msg.getType().getType() != SimulatorData.Type.Types.HEARTBEAT) {
            logger.log(Level.INFO, "Encoded data type " +
                    msg.getType().getType().getValueDescriptor().getName());
        }*/

        int length = msg.getSerializedSize();
        out.writeInt(length);
        out.ensureWritable(length);
        final int offset = out.arrayOffset() + out.writerIndex();
        final byte[] array = out.array();
        out.writerIndex(out.writerIndex() + encode(msg, array, offset, length));
        if (msg.getType().getType() != SimulatorData.Type.Types.HEARTBEAT) {
            logger.log(Level.FINER, "SIM_NETWORKING::", "Encoded data type " +
                    msg.getType().getType().toString());
        }
    }

    private int encode(final SimulatorData.Data msg, byte[] array, final int offset, final int length) {
        System.arraycopy(msg.toByteArray(), 0, array, offset, length);
        return length;
    }
}
