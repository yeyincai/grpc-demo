package com.grpc.demo.nameResolver;


import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;

public class SimpleServerStart1 {

    private int PORT=8888;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new SimpleServer().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SimpleServerStart1.this.stop();
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
        final SimpleServerStart1 simpleServerStart = new SimpleServerStart1();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(3000);
    }

}

