package com.grpc.demo.oneof.parseq;

import com.google.protobuf.Empty;
import com.linkedin.parseq.*;
import com.linkedin.parseq.promise.Promise;
import com.linkedin.parseq.promise.Promises;
import com.yyc.grpc.contract.*;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ParseqSimpleServer extends SimpleServiceGrpc.SimpleServiceImplBase {

    private static final ManagedChannel managedChannel = NettyChannelBuilder.forAddress("localhost", 9000).usePlaintext(true).build();
    private static final SimpleService1Grpc.SimpleService1BlockingStub simpleService1Stub = SimpleService1Grpc.newBlockingStub(managedChannel);

    private static final ManagedChannel managedChanne2 = NettyChannelBuilder.forAddress("localhost", 9001).usePlaintext(true).build();
    private static final SimpleService2Grpc.SimpleService2BlockingStub simpleService2Stub = SimpleService2Grpc.newBlockingStub(managedChanne2);

    private static final ManagedChannel managedChanne3 = NettyChannelBuilder.forAddress("localhost", 9002).usePlaintext(true).build();
    private static final SimpleService3Grpc.SimpleService3BlockingStub simpleService3Stub = SimpleService3Grpc.newBlockingStub(managedChanne3);


    private static final ExecutorService taskScheduler = Executors.newCachedThreadPool();

    private static final ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Engine engine = new EngineBuilder()
            .setTaskExecutor(taskScheduler)
            .setTimerScheduler(timerScheduler)
            .build();


    public void getAll(com.google.protobuf.Empty request,
                       io.grpc.stub.StreamObserver<GetAllResponse> responseObserver) {
        Task<String> task1 = new BaseTask<String>() {
            @Override
            protected Promise<? extends String> run(Context context) throws Exception {
                String member1 = simpleService1Stub.getAll(Empty.getDefaultInstance()).getMember1();
                GetAllResponse build = GetAllResponse.newBuilder().setMember1(member1).build();
                synchronized (responseObserver) {
                    responseObserver.onNext(build);
                }
                return Promises.value(member1);
            }
        };

        Task<Integer> task2 = new BaseTask<Integer>() {
            @Override
            protected Promise<? extends Integer> run(Context context) throws Exception {
                Integer member2 = simpleService2Stub.getAll(Empty.getDefaultInstance()).getMember2();
                GetAllResponse build = GetAllResponse.newBuilder().setMember2(member2).build();
                synchronized (responseObserver) {
                    responseObserver.onNext(build);
                }
                return Promises.value(member2);
            }
        };


        Task<String> task3 = new BaseTask<String>() {
            @Override
            protected Promise<? extends String> run(Context context) throws Exception {
                String member3 = simpleService3Stub.getAll(Empty.getDefaultInstance()).getMember3();
                GetAllResponse build = GetAllResponse.newBuilder().setMember3(member3).build();
                synchronized (responseObserver) {
                    responseObserver.onNext(build);
                }
                return Promises.value(member3);
            }
        };

        engine.run(task1);
        engine.run(task2);
        engine.run(task3);

        try {
            task1.await();
            task2.await();
            task3.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }


        //System.out.println("---------------------onCompleted ");
    }

}
