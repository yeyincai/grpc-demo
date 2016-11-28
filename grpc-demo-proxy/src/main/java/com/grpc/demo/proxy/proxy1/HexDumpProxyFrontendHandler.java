package com.grpc.demo.proxy.proxy1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http2.Http2Exception;


/**
 * Created by yeyc on 2016/11/21.
 */
public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private Channel outboundChannel;

    static {

    }

    //private AttributeKey<Map<Integer,Channel>> streamIdKey =  AttributeKey.newInstance("streamIdKey");

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();
        {
            Bootstrap b1 = new Bootstrap();
            b1.group(inboundChannel.eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new HexDumpProxyBackendHandler(inboundChannel))
                    .option(ChannelOption.AUTO_READ, false);


            ChannelFuture f1 = b1.connect("127.0.0.1", 8888);
            outboundChannel = f1.channel();
            //2
            f1.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // connection complete start to read first data
                        inboundChannel.read();
                    } else {
                        // Close the connection if the connection attempt has failed.
                        inboundChannel.close();
                    }
                }
            });

        }

    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Http2Exception {

        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
