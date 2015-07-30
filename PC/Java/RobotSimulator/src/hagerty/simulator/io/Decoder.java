package hagerty.simulator.io;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.ftccommunity.simulator.protobuf.SimulatorData;

import java.util.List;


public class Decoder extends ByteToMessageDecoder { // (1)
    @Override
    protected void decode(io.netty.channel.ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
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
            out.add(test);
        }
    }
}

