package org.ftccommunity.simulator.net.decoder;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

import java.util.List;

@Deprecated
public class HeartbeatDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> list) throws Exception {
        long size = 0;
        if (in.readableBytes() < 4) {
            return;
        }

        if (in.readableBytes() >= 4) {
            size = in.readUnsignedInt();
        }

        if (in.readableBytes() < size) {
            return;
        }

        SimulatorData.Data test;
        try {
            test = SimulatorData.Data.parseFrom(in.readBytes((int) size).array());
        } catch (InvalidProtocolBufferException e) {
            return;
        }
        if (test != null) {
            list.add(test);
        }
    }
}
