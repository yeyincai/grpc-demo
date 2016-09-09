package com.grpc.demo.stream;

import com.yyc.grpc.contract.SimpleServiceGrpc;
import com.yyc.grpc.contract.WeChatRequest;
import com.yyc.grpc.contract.WeChatResponse;
import io.grpc.stub.StreamObserver;

/**
 * Created by yeyc on 2016/9/5.
 */
public class SimpleServer extends SimpleServiceGrpc.SimpleServiceImplBase{

    @Override
    public io.grpc.stub.StreamObserver<com.yyc.grpc.contract.WeChatRequest> weChat(
            io.grpc.stub.StreamObserver<com.yyc.grpc.contract.WeChatResponse> responseObserver) {

        StreamObserver<WeChatRequest> requestStreamObserver = new StreamObserver<WeChatRequest>(){
            @Override
            public void onNext(WeChatRequest value) {
                System.out.println("client value:"+value.getRequest());
                responseObserver.onNext(WeChatResponse.newBuilder().setResponse("I m server response client request value="+value.getRequest()).build());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("client onCompleted!!");
                //this client onCompleted  then server onCompleted
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }
}
