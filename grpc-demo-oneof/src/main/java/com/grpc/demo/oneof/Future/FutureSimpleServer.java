package com.grpc.demo.oneof.Future;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import com.yyc.grpc.contract.*;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.ExecutionException;

public class FutureSimpleServer extends SimpleServiceGrpc.SimpleServiceImplBase{
    private static final ManagedChannel managedChannel = NettyChannelBuilder.forAddress("localhost",9000).usePlaintext(true).build();
    private static final SimpleService1Grpc.SimpleService1FutureStub simpleService1Stub = SimpleService1Grpc.newFutureStub(managedChannel);

    private static final ManagedChannel managedChanne2 = NettyChannelBuilder.forAddress("localhost",9001).usePlaintext(true).build();
    private static final SimpleService2Grpc.SimpleService2FutureStub simpleService2Stub = SimpleService2Grpc.newFutureStub(managedChanne2);

    private static final ManagedChannel managedChanne3 = NettyChannelBuilder.forAddress("localhost",9002).usePlaintext(true).build();
    private static final SimpleService3Grpc.SimpleService3FutureStub simpleService3Stub = SimpleService3Grpc.newFutureStub(managedChanne3);


    public void getAll(com.google.protobuf.Empty request,
                       io.grpc.stub.StreamObserver<GetAllResponse> responseObserver) {

        ListenableFuture<GetAllResponse1> all1 = simpleService1Stub.getAll(Empty.getDefaultInstance());

        ListenableFuture<GetAllResponse2> all2 = simpleService2Stub.getAll(Empty.getDefaultInstance());

        ListenableFuture<GetAllResponse3> all3 = simpleService3Stub.getAll(Empty.getDefaultInstance());

        try {
            responseObserver.onNext(GetAllResponse.newBuilder().setMember1(all1.get().getMember1()).build());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try {
            responseObserver.onNext(GetAllResponse.newBuilder().setMember2(all2.get().getMember2()).build());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try {
            responseObserver.onNext(GetAllResponse.newBuilder().setMember3(all3.get().getMember3()).build());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        responseObserver.onCompleted();

        //System.out.println("---------------------onCompleted ");
    }

}
