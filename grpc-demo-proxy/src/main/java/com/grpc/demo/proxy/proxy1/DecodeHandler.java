package com.grpc.demo.proxy.proxy1;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;


@ChannelHandler.Sharable
class DecodeHandler extends ChannelDuplexHandler  {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuf copy = byteBuf.copy();

        byte frameType = byteBuf.getByte(3);
        System.out.println("frameType:"+frameType);
        if (frameType== 1) {
            System.out.println(Utils.readHeadersFrame(ctx,byteBuf.copy()));

        }

        if (frameType == 0) {
            System.out.println(Utils.readDataFrame(byteBuf.copy()));
        }

        ctx.fireChannelRead(copy);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

}