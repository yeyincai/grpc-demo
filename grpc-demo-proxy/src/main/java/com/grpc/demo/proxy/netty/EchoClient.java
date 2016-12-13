package com.grpc.demo.proxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public final class EchoClient {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public static void main(String[] args) throws Exception {

        //SimpleExecutor simpleExecutor = new SimpleExecutor(() -> {

            EventLoopGroup group = new NioEventLoopGroup(1);
            ChannelFuture f = null;
            Bootstrap b = new Bootstrap();
            try {
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast(new EchoClientHandler());
                            }
                        });
                try {
                    f = b.connect(HOST, PORT).sync();
                    f.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } finally {
                group.shutdownGracefully();
            }
        //});
        //simpleExecutor.execute(20, 100);

    }
}