package com.grpc.demo.proxy;


import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;

public class SimpleServerStart {

    public SimpleServerStart (int port){
        this.port = port;
    }

    private int port;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(port).addService(new SimpleServer().bindService()).build();
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
        final SimpleServerStart simpleServerStart = new SimpleServerStart(8888);
        simpleServerStart.start();

       /* final SimpleServerStart simpleServerStart1 = new SimpleServerStart(8889);
        simpleServerStart1.start();*/
        TimeUnit.SECONDS.sleep(3000);
    }

}

