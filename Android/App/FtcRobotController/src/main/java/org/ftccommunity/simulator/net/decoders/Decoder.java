package org.ftccommunity.simulator.net.decoders;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import org.ftccommunity.simulator.net.protocol.SimulatorData;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;


public class Decoder extends ByteToMessageDecoder { // (1)
    @Override
    protected void decode(io.netty.channel.ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        final String TAG = "SERVER_H::";
        long size = 0;

        if (in.readableBytes() < 4) {
            return;
        } else {
            size = in.readUnsignedInt();
        }

        if (in.readableBytes() < size) {
            return;
        }

        SimulatorData.Data test;
        try {
            test = SimulatorData.Data.parseFrom(in.readBytes((int) size).array());
            out.add(test);
            Log.d("SIM_NETWORKING::", "Decoded data type " +
                    test.getType().getType().getValueDescriptor().getName());
        } catch (InvalidProtocolBufferException e) {
            Log.w(TAG, "An attempt to decode an otherwise valid packet failed", e);
        }
    }
}

