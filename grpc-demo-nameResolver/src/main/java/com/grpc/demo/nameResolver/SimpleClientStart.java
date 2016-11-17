package com.grpc.demo.nameResolver;


import com.google.protobuf.StringValue;
import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;

import java.util.concurrent.TimeUnit;

public class SimpleClientStart {

    private ManagedChannel managedChannel;

    private void createChannel(){
        managedChannel = NettyChannelBuilder
                .forTarget("server://127.0.0.1:9999,127.0.0.1:8888")
                .nameResolverFactory(new ServerNameResolverProvider())
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .usePlaintext(true).build();
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

        for (int i=0;i<100;i++) {

            SayHelloResponse sayHelloResponse = simpleServiceStub.sayHello(StringValue.newBuilder().setValue("grpc-nameResolver-demo").build());
            System.out.println("response:" + sayHelloResponse.getResult());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e){


            }

        }
        simpleClientStart.shutdown();
    }
}
