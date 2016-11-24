package com.grpc.demo.oneof.Future;


import com.yyc.grpc.contract.GetAllResponse1;
import com.yyc.grpc.contract.SimpleService1Grpc;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by yeyc on 2016/11/23.
 */
public class Server1 {
    private int PORT=9000;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new Server1Impl().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                Server1.this.stop();
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
        final Server1 simpleServerStart = new Server1();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(300);
    }

}

class Server1Impl extends SimpleService1Grpc.SimpleService1ImplBase {

    public void getAll(com.google.protobuf.Empty request,
                       io.grpc.stub.StreamObserver<com.yyc.grpc.contract.GetAllResponse1> responseObserver) {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GetAllResponse1 getAllResponse1 = GetAllResponse1.newBuilder().setMember1("111111").build();
        responseObserver.onNext(getAllResponse1);
        responseObserver.onCompleted();
    }
}

