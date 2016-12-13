package com.grpc.demo.proxy.proxy1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2FrameCodec;

import java.util.concurrent.TimeUnit;

/**
 * Created by yeyc on 2016/12/12.
 */
public class InitChannel {

    public static ProxyToServerChannel proxyChannel ;

    public static void  InitChannel() {
        Bootstrap b1 = new Bootstrap();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel =  b1.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("proxy-serverHttp2FrameCodec", new Http2FrameCodec(false));
                        pipeline.addLast("HexDumpProxyBackendHandler", new HexDumpProxyBackendHandler());
                    }
                }).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100).connect("127.0.0.1",8888).channel();

        ChannelHandlerContext channelHandlerContext = channel.pipeline().lastContext();
        channelHandlerContext.writeAndFlush(Http2CodecUtil.connectionPrefaceBuf().retainedDuplicate());

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("初始化服务"+8888);

        proxyChannel = new ProxyToServerChannel(channel);

    }

}
