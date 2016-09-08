package com.grpc.demo.oneof;


import com.google.protobuf.Empty;
import com.yyc.grpc.contract.GetAllResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.Iterator;
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

        Long start = System.currentTimeMillis();
        Iterator<GetAllResponse> all = simpleServiceStub.getAll(Empty.getDefaultInstance());
        while (all.hasNext()) {
            GetAllResponse response = all.next();
            System.out.println("response:"+response);
        }
        //this time <3 second
        System.out.println("spend time :"+(System.currentTimeMillis()-start));

        simpleClientStart.shutdown();
    }
}
