package org.ftccommunity.simulator;

import com.qualcomm.robotcore.util.RobotLog;

import org.ftccommunity.simulator.net.decoders.Decoder;
import org.ftccommunity.simulator.net.handler.ServerHandler;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Discards any incoming data.
 */
public class Server implements Runnable {
    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Decoder(), new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)


            // Log all IP addresses
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()){
                    NetworkInterface current = interfaces.nextElement();
                    RobotLog.i(current.toString());
                    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
                    Enumeration<InetAddress> addresses = current.getInetAddresses();
                    while (addresses.hasMoreElements()){
                        InetAddress current_addr = addresses.nextElement();
                        if (current_addr.isLoopbackAddress()) continue;
                        RobotLog.i(current_addr.getHostAddress());
                    }
                }
            } catch (SocketException e) {
                RobotLog.e(e.toString());
            }

            ChannelFuture f = null;
            try {
                RobotLog.w("Starting Server on " + port + "@" + InetAddress.getLocalHost().getHostAddress());
                // Bind and start to accept incoming connections.
                f = b.bind(port).sync();
                f.channel().closeFuture().sync();

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (UnknownHostException e) {
                RobotLog.e("Something Bad happened " + e.toString());
            }
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            RobotLog.i("Shutdown Server");
        }
    }
}