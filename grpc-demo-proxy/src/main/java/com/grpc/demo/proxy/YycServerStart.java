package com.grpc.demo.proxy;


import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;

public class YycServerStart {

    public YycServerStart(int port){
        this.port = port;
    }

    private int port;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(port).addService(new YycServer().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                YycServerStart.this.stop();
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
        final YycServerStart simpleServerStart = new YycServerStart(9999);
        simpleServerStart.start();

        final YycServerStart simpleServerStart1 = new YycServerStart(9998);
        simpleServerStart1.start();
        TimeUnit.SECONDS.sleep(3000);
    }

}

