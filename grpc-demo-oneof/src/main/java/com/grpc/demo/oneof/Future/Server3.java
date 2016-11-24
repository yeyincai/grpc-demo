package com.grpc.demo.oneof.Future;


import com.yyc.grpc.contract.GetAllResponse3;
import com.yyc.grpc.contract.SimpleService3Grpc;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;


public class Server3 {
    private int PORT=9002;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new Server3Impl().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                Server3.this.stop();
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
        final Server3 simpleServerStart = new Server3();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(300);
    }

}

class Server3Impl extends SimpleService3Grpc.SimpleService3ImplBase {

    public void getAll(com.google.protobuf.Empty request,
                       io.grpc.stub.StreamObserver<com.yyc.grpc.contract.GetAllResponse3> responseObserver) {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GetAllResponse3 getAllResponse1 = GetAllResponse3.newBuilder().setMember3("3333").build();
        responseObserver.onNext(getAllResponse1);
        responseObserver.onCompleted();
    }
}

