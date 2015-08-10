package org.ftccommunity.simulator.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.ftccommunity.gui.MainApp;
import org.ftccommunity.simulator.BrickListGenerator;
import org.ftccommunity.simulator.io.decoder.Decoder;
import org.ftccommunity.simulator.io.decoder.HeartbeatDecoder;
import org.ftccommunity.simulator.io.handler.ClientHandler;
import org.ftccommunity.simulator.io.handler.HeartbeatHandler;

import java.net.InetSocketAddress;

public class Client {
    public static void main(String[] args) throws Exception {
        String host = "192.168.44.1";
        int port = 7002;
        io.netty.channel.EventLoopGroup workerGroup = new io.netty.channel.nio.NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioDatagramChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, false)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new io.netty.channel.ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel channel) throws Exception {
                            channel.pipeline().addLast(new HeartbeatDecoder(), new HeartbeatHandler());
                        }
                    });
            b.bind(new InetSocketAddress(7003)).sync().channel();

        } catch (Exception e) {
            e.printStackTrace();
        }
/*

        while (!NetworkManager.isReady()) {
            System.out.print("Wait on robot...");
            Thread.sleep(2000);
            if (!NetworkManager.isReady()) {
                System.out.println("nope, not just yet");
            } else {
                System.out.println("found! Yea!");
            }

        }
*/


        try {
            MainApp mainApp = new MainApp();
            BrickListGenerator gen = new BrickListGenerator(mainApp);
            (new Thread(gen)).start();

            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new io.netty.channel.ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new IdleStateHandler(1, 1, 2), new Decoder(),
                                                 new ClientHandler());
                }
            });

            // Start the client.
            io.netty.channel.ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
