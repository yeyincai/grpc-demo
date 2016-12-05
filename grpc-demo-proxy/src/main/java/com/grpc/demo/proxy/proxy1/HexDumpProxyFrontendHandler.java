package com.grpc.demo.proxy.proxy1;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2HeadersFrame;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * Created by yeyc on 2016/11/21.
 */
public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private Channel outboundChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //outboundChannel =  selectChannel("/com.yyc.grpc.contract.SimpleService/SayHello",ctx.channel());
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Http2Exception {
        //创建channel;
        if (outboundChannel == null) {
            Http2HeadersFrame http2Headers = (Http2HeadersFrame) msg;
            final Channel inboundChannel = ctx.channel();
            outboundChannel = selectChannel(http2Headers.headers().path().toString(), inboundChannel);
        }

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

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();


    private Channel selectChannel(String path, final Channel inboundChannel) {
        String serviceName = getServiceName(path);
        List<InetSocketAddress> inetSocketAddresses = Servers.servers.get(serviceName);

        int i = new Random().nextInt(inetSocketAddresses.size());
        InetSocketAddress inetSocketAddress = inetSocketAddresses.get(i);

        Bootstrap b1 = new Bootstrap();
        Channel hexDumpProxyBackendHandler = b1.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("proxy-serverHttp2FrameCodec", new Http2FrameCodec(false));
                        pipeline.addLast("HexDumpProxyBackendHandler", new HexDumpProxyBackendHandler(inboundChannel));
                    }
                }).option(ChannelOption.AUTO_READ, false).connect(inetSocketAddress).channel();
        ChannelHandlerContext channelHandlerContext = hexDumpProxyBackendHandler.pipeline().lastContext();
        channelHandlerContext.writeAndFlush(Http2CodecUtil.connectionPrefaceBuf().retainedDuplicate());

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return hexDumpProxyBackendHandler;

    }

    private static String getServiceName(String fullPath) {
        final String GRPC_PATH_SEPERATOR = "/";
        final String PACKAGE_SEPERATOR = ".";
        Preconditions.checkNotNull(fullPath, "fullPath");
        int pos = fullPath.lastIndexOf(PACKAGE_SEPERATOR);
        String serviceMethod = fullPath;
        if (pos > 0) {
            serviceMethod = fullPath.substring(pos + 1);
        }

        String[] splits = serviceMethod.split(GRPC_PATH_SEPERATOR);
        if (splits.length != 2) {
            throw new IllegalArgumentException(fullPath + "is not valid path");
        }
        return splits[0];
    }


}
