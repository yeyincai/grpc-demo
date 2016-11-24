package com.grpc.demo.proxy.proxy1;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by yeyc on 2016/11/21.
 */
public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel>{
    private final  String remoteHost;
    private final int remotePort;

    public HexDumpProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast( new DecodeHandler());

        pipeline.addLast(new HexDumpProxyFrontendHandler(remoteHost, remotePort));
    }
}
