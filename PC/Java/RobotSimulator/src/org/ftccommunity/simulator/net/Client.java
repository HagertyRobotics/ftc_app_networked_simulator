package org.ftccommunity.simulator.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.ftccommunity.simulator.io.handler.ClientHandler;
import org.ftccommunity.simulator.net.decoder.Decoder;
import org.ftccommunity.simulator.net.encoder.MessageEncoder;
import org.ftccommunity.simulator.net.manager.NetworkManager;

/**
 * Created by David on 8/17/2015.
 */
public class Client implements Runnable {
    private final EventLoopGroup workerGroup;

    public Client() {
        workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void run() {
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new io.netty.channel.ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new IdleStateHandler(1, 1, 2), new MessageEncoder(),
                            new Decoder(), new ClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(NetworkManager.host, NetworkManager.port).sync(); // (5)
            f.channel().closeFuture().sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
        }
        System.out.print("Server closed");
    }

}
