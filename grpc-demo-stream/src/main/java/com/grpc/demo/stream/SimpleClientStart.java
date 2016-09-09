package com.grpc.demo.stream;


import com.yyc.grpc.contract.SimpleServiceGrpc;
import com.yyc.grpc.contract.WeChatRequest;
import com.yyc.grpc.contract.WeChatResponse;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

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

    public static void main(String[] args) throws InterruptedException {
        SimpleClientStart simpleClientStart = new SimpleClientStart();
        simpleClientStart.createChannel();
        //This stub  is not blockingStub
        SimpleServiceGrpc.SimpleServiceStub simpleServiceStub = SimpleServiceGrpc.newStub(simpleClientStart.managedChannel);

        StreamObserver<WeChatRequest> requestStreamObserver = simpleServiceStub.weChat(new StreamObserver<WeChatResponse>() {
            @Override
            public void onNext(WeChatResponse value) {
                System.out.println("server  send:"+value.getResponse());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("server onCompleted!!!");
            }
        });

        WeChatRequest weChatRequest = WeChatRequest.newBuilder().setRequest("client request"+System.currentTimeMillis()).build();
        requestStreamObserver.onNext(weChatRequest);

        TimeUnit.SECONDS.sleep(1);
        WeChatRequest weChatRequest1 = WeChatRequest.newBuilder().setRequest("client request"+System.currentTimeMillis()).build();
        requestStreamObserver.onNext(weChatRequest1);

        TimeUnit.SECONDS.sleep(1);
        WeChatRequest weChatRequest2 = WeChatRequest.newBuilder().setRequest("client request"+System.currentTimeMillis()).build();
        requestStreamObserver.onNext(weChatRequest2);

        TimeUnit.SECONDS.sleep(1);
        WeChatRequest weChatRequest3 = WeChatRequest.newBuilder().setRequest("client request"+System.currentTimeMillis()).build();
        requestStreamObserver.onNext(weChatRequest3);

        requestStreamObserver.onCompleted();


        simpleClientStart.shutdown();
    }
}
