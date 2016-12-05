package com.grpc.demo.proxy;

import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.YycServiceGrpc;

/**
 * Created by yeyc on 2016/9/5.
 */
public class YycServer extends YycServiceGrpc.YycServiceImplBase{

    @Override
    public void sayHello(com.google.protobuf.StringValue request,
                         io.grpc.stub.StreamObserver<SayHelloResponse> responseObserver) {
        String result =request.getValue().concat("hello world");
        System.out.println(result);
        SayHelloResponse sayHelloResponse = SayHelloResponse.newBuilder().setResult(result).build();
        responseObserver.onNext(sayHelloResponse);
        responseObserver.onCompleted();
    }
}
