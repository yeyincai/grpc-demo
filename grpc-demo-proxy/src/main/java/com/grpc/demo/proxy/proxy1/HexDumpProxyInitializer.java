package com.grpc.demo.proxy.proxy1;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.Http2FrameCodec;

/**
 * Created by yeyc on 2016/11/21.
 */
public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new Http2FrameCodec(true));
        pipeline.addLast(new HexDumpProxyFrontendHandler());
    }
}
