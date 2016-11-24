package com.grpc.demo.oneof.Future;

import com.yyc.grpc.contract.GetAllResponse2;
import com.yyc.grpc.contract.SimpleService2Grpc;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;


/**
 * Created by yeyc on 2016/11/23.
 */
public class Server2 {
    private int PORT=9001;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new Server2Impl().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                Server2.this.stop();
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
        final Server2 simpleServerStart = new Server2();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(300);
    }

}

class Server2Impl extends SimpleService2Grpc.SimpleService2ImplBase {

    public void getAll(com.google.protobuf.Empty request,
                       io.grpc.stub.StreamObserver<com.yyc.grpc.contract.GetAllResponse2> responseObserver) {

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GetAllResponse2 getAllResponse2 = GetAllResponse2.newBuilder().setMember2(111111).build();
        responseObserver.onNext(getAllResponse2);
        responseObserver.onCompleted();
    }
}

