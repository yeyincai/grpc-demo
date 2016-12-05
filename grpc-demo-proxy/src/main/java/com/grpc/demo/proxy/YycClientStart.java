package com.grpc.demo.proxy;


import com.google.protobuf.StringValue;
import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.YycServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;

public class YycClientStart {

    private ManagedChannel managedChannel;
    private int PORT = 8080;

    private void createChannel() {
        managedChannel = NettyChannelBuilder.forAddress("localhost", PORT).usePlaintext(true).build();
    }

    private void shutdown() {
        if (managedChannel != null) {
            try {
                managedChannel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        YycClientStart simpleClientStart = new YycClientStart();
        simpleClientStart.createChannel();
        /*************************/
        YycServiceGrpc.YycServiceBlockingStub yycServiceBlockingStub = YycServiceGrpc.newBlockingStub(simpleClientStart.managedChannel);

        SayHelloResponse sayHelloResponse1 = yycServiceBlockingStub.sayHello(StringValue.newBuilder().setValue("yyc hello").build());
        System.out.println("response1:" + sayHelloResponse1.getResult());



        SayHelloResponse sayHelloResponse2 = yycServiceBlockingStub.sayHello(StringValue.newBuilder().setValue("yyc hello").build());
        System.out.println("response2:" + sayHelloResponse2.getResult());


        SayHelloResponse sayHelloResponse3 = yycServiceBlockingStub.sayHello(StringValue.newBuilder().setValue("yyc hello").build());
        System.out.println("response3:" + sayHelloResponse3.getResult());

        simpleClientStart.managedChannel.shutdown();


    }
}
