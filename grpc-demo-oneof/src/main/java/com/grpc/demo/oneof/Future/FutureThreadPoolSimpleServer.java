package com.grpc.demo.oneof.Future;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import com.yyc.grpc.contract.*;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FutureThreadPoolSimpleServer extends SimpleServiceGrpc.SimpleServiceImplBase {
    private static final ManagedChannel managedChannel = NettyChannelBuilder.forAddress("localhost", 9000).usePlaintext(true).build();
    private static final SimpleService1Grpc.SimpleService1FutureStub simpleService1Stub = SimpleService1Grpc.newFutureStub(managedChannel);

    private static final ManagedChannel managedChanne2 = NettyChannelBuilder.forAddress("localhost", 9001).usePlaintext(true).build();
    private static final SimpleService2Grpc.SimpleService2FutureStub simpleService2Stub = SimpleService2Grpc.newFutureStub(managedChanne2);

    private static final ManagedChannel managedChanne3 = NettyChannelBuilder.forAddress("localhost", 9002).usePlaintext(true).build();
    private static final SimpleService3Grpc.SimpleService3FutureStub simpleService3Stub = SimpleService3Grpc.newFutureStub(managedChanne3);

    private static final ExecutorService taskScheduler = Executors.newCachedThreadPool();

    public void getAll(Empty request,
                       io.grpc.stub.StreamObserver<GetAllResponse> responseObserver) {
        CountDownLatch co = new CountDownLatch(3);
        taskScheduler.submit(() -> {
            ListenableFuture<GetAllResponse1> all1 = simpleService1Stub.getAll(Empty.getDefaultInstance());
            try {
                GetAllResponse build = GetAllResponse.newBuilder().setMember1(all1.get().getMember1()).build();
                synchronized (responseObserver) {
                    responseObserver.onNext(build);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            finally {
                co.countDown();
            }
        });


        taskScheduler.submit(() -> {
            ListenableFuture<GetAllResponse2> all2 = simpleService2Stub.getAll(Empty.getDefaultInstance());
            try {
                GetAllResponse build = GetAllResponse.newBuilder().setMember2(all2.get().getMember2()).build();
                synchronized (responseObserver) {
                    responseObserver.onNext(build);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            finally {
                co.countDown();
            }
        });

        taskScheduler.submit(() -> {
            ListenableFuture<GetAllResponse3> all3 = simpleService3Stub.getAll(Empty.getDefaultInstance());
            try {
                GetAllResponse build = GetAllResponse.newBuilder().setMember3(all3.get().getMember3()).build();
                synchronized (responseObserver) {
                    responseObserver.onNext(build);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            finally {
                co.countDown();
            }
        });

        try {
            co.await();
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //System.out.println("---------------------onCompleted ");
    }

}
