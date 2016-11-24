package com.grpc.demo.oneof;

import com.yyc.grpc.contract.GetAllResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class SimpleServer extends SimpleServiceGrpc.SimpleServiceImplBase{

    private final static ExecutorService executors = Executors.newCachedThreadPool();

    public void getAll(com.google.protobuf.Empty request,
                       io.grpc.stub.StreamObserver<com.yyc.grpc.contract.GetAllResponse> responseObserver) {
        CountDownLatch counter =  new CountDownLatch(3);

        executors.submit(new AbstractTask(counter,responseObserver){
            @Override
            GetAllResponse invoke() {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return GetAllResponse.newBuilder().setMember1("11111").build();
            }
        });

        executors.submit(new AbstractTask(counter,responseObserver){
            @Override
            GetAllResponse invoke() {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return GetAllResponse.newBuilder().setMember2(222222).build();
            }
        });

        executors.submit(new AbstractTask(counter,responseObserver){
            @Override
            GetAllResponse invoke() {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return GetAllResponse.newBuilder().setMember3("333333").build();
            }
        });

        try {
            counter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        responseObserver.onCompleted();
        //System.out.println("---------------------onCompleted ");
    }

}
