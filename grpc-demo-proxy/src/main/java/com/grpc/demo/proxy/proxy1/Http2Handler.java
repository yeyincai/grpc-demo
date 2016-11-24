package com.grpc.demo.proxy.proxy1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2FrameCodec;

/**
 * Created by yeyc on 2016/11/23.
 */
public class Http2Handler extends Http2FrameCodec {
    /**
     * Construct a new handler.
     *
     * @param server {@code true} this is a server
     */
    public Http2Handler(boolean server) {
        super(server);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        System.out.println(msg);
    }

}
