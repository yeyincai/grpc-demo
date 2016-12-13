package com.grpc.demo.proxy;

import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;

/**
 * Created by yeyc on 2016/9/5.
 */
public class SimpleServer extends SimpleServiceGrpc.SimpleServiceImplBase{

    @Override
    public void sayHello(com.google.protobuf.StringValue request,
                         io.grpc.stub.StreamObserver<com.yyc.grpc.contract.SayHelloResponse> responseObserver) {

        System.out.println(request.getValue());
        SayHelloResponse sayHelloResponse = SayHelloResponse.newBuilder().setResult(request.getValue()).build();
        responseObserver.onNext(sayHelloResponse);
        responseObserver.onCompleted();
    }
}
