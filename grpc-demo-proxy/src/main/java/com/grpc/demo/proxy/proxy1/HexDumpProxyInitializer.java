package com.grpc.demo.proxy.proxy1;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by yeyc on 2016/11/21.
 */
public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel>{


    public HexDumpProxyInitializer() {

    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast(new MockProxyService());
        //pipeline.addLast(new HexDumpProxyFrontendHandler());
    }
}
