package com.grpc.demo.interceptor;

import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;

/**
 * Created by yeyc on 2016/9/5.
 */
public class SimpleServer extends SimpleServiceGrpc.SimpleServiceImplBase{

    @Override
    public void sayHello(com.google.protobuf.StringValue request,
                         io.grpc.stub.StreamObserver<com.yyc.grpc.contract.SayHelloResponse> responseObserver) {
        SayHelloResponse sayHelloResponse = SayHelloResponse.newBuilder().setResult(request.getValue().concat("hello world")).build();
        responseObserver.onNext(sayHelloResponse);
        responseObserver.onCompleted();
    }
}
