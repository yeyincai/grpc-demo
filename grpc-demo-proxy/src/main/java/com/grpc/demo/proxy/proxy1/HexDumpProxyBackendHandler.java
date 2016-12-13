package com.grpc.demo.proxy.proxy1;

import io.netty.channel.*;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.util.ReferenceCountUtil;

public class HexDumpProxyBackendHandler extends ChannelInboundHandlerAdapter {


    public HexDumpProxyBackendHandler() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("HexDumpProxyBackendHandler channelActive ");
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        final Channel inboundChannel;
        if(msg instanceof DefaultHttp2HeadersFrame){
            final DefaultHttp2HeadersFrame headersFrame = (DefaultHttp2HeadersFrame)msg;
            final int streamId = ClientToProxyChannels.clientToProxyStreamIdMap.get(headersFrame.streamId());
            inboundChannel = ClientToProxyChannels.clientToProxyChannelMap.get(headersFrame.streamId());

            msg = new DefaultHttp2HeadersFrame(headersFrame.headers(),headersFrame.isEndStream(), headersFrame.padding())
                    .streamId(streamId);

            if(headersFrame.isEndStream()){
                ClientToProxyChannels.clientToProxyStreamIdMap.remove(headersFrame.streamId());
                ClientToProxyChannels.clientToProxyChannelMap.remove(headersFrame.streamId());
            }

        } else if(msg instanceof DefaultHttp2DataFrame){
            final DefaultHttp2DataFrame dataFrame = (DefaultHttp2DataFrame)msg;

            final int streamId = ClientToProxyChannels.clientToProxyStreamIdMap.get(dataFrame.streamId());
            inboundChannel = ClientToProxyChannels.clientToProxyChannelMap.get(dataFrame.streamId());

            msg = new DefaultHttp2DataFrame(dataFrame.content(), dataFrame.isEndStream(), dataFrame.padding()).streamId
                    (streamId);

            if(dataFrame.isEndStream()){
                ClientToProxyChannels.clientToProxyStreamIdMap.remove(dataFrame.streamId());
                ClientToProxyChannels.clientToProxyChannelMap.remove(dataFrame.streamId());
            }

        } else {
            ReferenceCountUtil.release(msg);
            return;
        }


        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
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


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        HexDumpProxyFrontendHandler.closeOnFlush(ctx.channel());
    }



}
