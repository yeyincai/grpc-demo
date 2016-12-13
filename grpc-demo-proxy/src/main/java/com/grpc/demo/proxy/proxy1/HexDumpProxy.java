package com.grpc.demo.proxy.proxy1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yeyc on 2016/11/21.
 */
public class HexDumpProxy {

    private static final int LOCAL_PORT = 8080;

    private static final int nThreads = Runtime.getRuntime().availableProcessors() + 1;

    private static final Logger logger = LoggerFactory.getLogger(HexDumpProxy.class);

    public static void main(String[] args) throws Exception {

        InitChannel.InitChannel();

        // Configure the bootstrap.
        EventLoopGroup bossGroup = new NioEventLoopGroup(nThreads);
        EventLoopGroup workerGroup = new NioEventLoopGroup(nThreads);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HexDumpProxyInitializer())
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(LOCAL_PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        logger.info("Proxying start *:{}",LOCAL_PORT);

    }

}
