package org.ftccommunity.simulator;

import com.qualcomm.robotcore.util.RobotLog;

import org.ftccommunity.simulator.net.decoders.Decoder;
import org.ftccommunity.simulator.net.encoder.MessageEncoder;
import org.ftccommunity.simulator.net.handler.ServerHandler;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Discards any incoming data.
 */
public class Server implements Runnable {
    private int port;
    private Channel channel;
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1); // (1)
    private EventLoopGroup workerGroup = new NioEventLoopGroup(2);

    public Server(int port) {
        this.port = port;
    }

    public void run() {

        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(1, 1, 2), new MessageEncoder(), new Decoder(),
                                    new ServerHandler());
                        }
                    })
                    // .option(ChannelOption.SO_BACKLOG, 64) // (5)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
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
                channel = f.channel();
                channel.closeFuture().sync();

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (UnknownHostException e) {
                RobotLog.e("Something Bad happened " + e.toString());
            }
            // Wait until the server socket is closed.
            // You can do this to gracefully
            // shut down your server.

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            RobotLog.i("Shutdown Server");
        }
    }

    public void stop() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        RobotLog.i("Shutdown Server");
    }


    public void fireEvent(Events event) {
        if (channel != null) {
            channel.pipeline().fireUserEventTriggered(event);
        }
    }

    public enum Events {
        CMD_READ,
        CMD_WRITE,
        CMD_HEARTBEAT
    }
}