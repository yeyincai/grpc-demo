package com.grpc.demo.proxy.proxy1;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MockProxyService extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        final Channel inboundChannel = ctx.channel();

        //00 00 00 04 00 00 00 00 00
        ByteBuf byteBuf1 = Unpooled.buffer();
        byteBuf1.writeByte(0x00);
        byteBuf1.writeByte(0x00);
        byteBuf1.writeByte(0x00);
        byteBuf1.writeByte(0x04);
        byteBuf1.writeByte(0x00);
        byteBuf1.writeByte(0x00);
        byteBuf1.writeByte(0x00);
        byteBuf1.writeByte(0x00);
        byteBuf1.writeByte(0x00);
        ctx.write(byteBuf1);

        //setting
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x0c);
        byteBuf.writeByte(0x04);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x03);
        byteBuf.writeByte(0x7f);
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0x00);
        //04 00 10 00 00 00 00 04 08 00 00 00 00 00 00 0f
        byteBuf.writeByte(0x04);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x10);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x04);
        byteBuf.writeByte(0x08);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x0f);
        //00 01
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(0x01);

        ctx.write(byteBuf);

        ctx.flush();




    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println(" MockProxyService  channelRead::::::"+msg);
        //header
        //data

        ctx.fireChannelRead(msg);


    }


}
