package com.grpc.demo.encryption;


import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class SimpleServerStart {

    private int PORT=8888;
    private Server server;

    private void start() throws Exception{
        String filePath = SimpleServerStart.class.getClassLoader().getResource("").getPath();
        File certChain = new File(filePath.concat("server1-public-ca.pem"));
        File privateKey = new File(filePath.concat("server1-private.key"));

        SslContextBuilder scb = SslContextBuilder.forServer(certChain,privateKey);
        SslProvider sp = SslProvider.OPENSSL;
        GrpcSslContexts.configure(scb, sp);
        SslContext sslContext1  = scb.build();

        SocketAddress socketAddress = new InetSocketAddress("127.0.0.1",PORT);
        server = NettyServerBuilder.forAddress(socketAddress)
                .sslContext(sslContext1)
                .addService(new SimpleServer().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SimpleServerStart.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop(){
        try {
            server.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        final SimpleServerStart simpleServerStart = new SimpleServerStart();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(300);

    }

}

