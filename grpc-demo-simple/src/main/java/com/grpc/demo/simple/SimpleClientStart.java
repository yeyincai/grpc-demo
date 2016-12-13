package com.grpc.demo.simple;


import com.google.protobuf.StringValue;
import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;

public class SimpleClientStart {

    private ManagedChannel managedChannel;
    private int PORT = 8888;

    private void createChannel(){
        managedChannel = NettyChannelBuilder.forAddress("localhost",PORT).usePlaintext(true).build();
    }

    private void shutdown(){
        if(managedChannel!=null){
            try {
                managedChannel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SimpleClientStart simpleClientStart = new SimpleClientStart();
        simpleClientStart.createChannel();
        SimpleServiceGrpc.SimpleServiceBlockingStub simpleServiceStub = SimpleServiceGrpc.newBlockingStub(simpleClientStart.managedChannel);

        SayHelloResponse sayHelloResponse = simpleServiceStub.sayHello(StringValue.newBuilder().setValue("grpc-simple-demo").build());
        System.out.println("response:"+sayHelloResponse.getResult());
        simpleClientStart.managedChannel.shutdownNow();
        //simpleClientStart.shutdown();
    }
}
