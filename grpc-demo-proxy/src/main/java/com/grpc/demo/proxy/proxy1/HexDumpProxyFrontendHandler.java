package com.grpc.demo.proxy.proxy1;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.util.ReferenceCountUtil;

import static com.grpc.demo.proxy.proxy1.ClientToProxyChannels.clientToProxyChannelMap;
import static com.grpc.demo.proxy.proxy1.ClientToProxyChannels.clientToProxyStreamIdMap;


/**
 * Created by yeyc on 2016/11/21.
 */
public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private ProxyToServerChannel outboundChannel;
    private Integer streamId;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        outboundChannel = InitChannel.proxyChannel;
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Http2Exception {
        synchronized (HexDumpProxyFrontendHandler.class) {
            final Channel inboundChannel = ctx.channel();

            if (msg instanceof DefaultHttp2HeadersFrame) {
                streamId = outboundChannel.getCount() * 2 + 1;
                DefaultHttp2HeadersFrame headersFrame = (DefaultHttp2HeadersFrame) msg;
                msg = new DefaultHttp2HeadersFrame(headersFrame.headers(), headersFrame.isEndStream(), headersFrame.padding())
                        .streamId(streamId);

                //设置后端channel的值
                clientToProxyChannelMap.putIfAbsent(streamId, inboundChannel);
                clientToProxyStreamIdMap.putIfAbsent(streamId, headersFrame.streamId());
            } else if (msg instanceof DefaultHttp2DataFrame) {
                DefaultHttp2DataFrame dataFrame = (DefaultHttp2DataFrame) msg;
                msg = new DefaultHttp2DataFrame(dataFrame.content(), dataFrame.isEndStream(), dataFrame.padding()).streamId
                        (streamId);
            } else {
                ReferenceCountUtil.release(msg);
                return;
            }


            outboundChannel.getChannel().writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
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
