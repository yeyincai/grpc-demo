package com.grpc.demo.nameResolver;


import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class SimpleServerStart2 {

    private int PORT=9999;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new SimpleServer().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SimpleServerStart2.this.stop();
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
        final SimpleServerStart2 simpleServerStart = new SimpleServerStart2();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(3000);
    }

    public static void main1(String[] args) throws UnknownHostException {
        InetAddress[] allByName = InetAddress.getAllByName("www.ppmoney.com");

        for(int i=0;i<allByName.length;i++){
            System.out.println(allByName[i].getHostAddress());
        }
    }

}

