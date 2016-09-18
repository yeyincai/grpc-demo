package com.grpc.demo.interceptor;


import com.google.protobuf.StringValue;
import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;
import io.grpc.*;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

public class SimpleClientStart {

    private ManagedChannel managedChannel;
    private int PORT = 8888;

    private void createChannel(){
        managedChannel = NettyChannelBuilder.forAddress("localhost",PORT).usePlaintext(true)
                .intercept(new ClientRequestInterceptor(),new ClientResponseInterceptor() )
                .build();
    }

    private void shutdown(){
        if(managedChannel!=null){
            try {
                managedChannel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SimpleClientStart simpleClientStart = new SimpleClientStart();
        simpleClientStart.createChannel();
        SimpleServiceGrpc.SimpleServiceBlockingStub simpleServiceStub = SimpleServiceGrpc.newBlockingStub(simpleClientStart.managedChannel);

        //add metadata
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("extendKey", Metadata.ASCII_STRING_MARSHALLER), "extendValue");
        MetadataUtils.attachHeaders(simpleServiceStub,metadata);
        SayHelloResponse sayHelloResponse = simpleServiceStub.sayHello(StringValue.newBuilder().setValue("grpc-interceptor-demo").build());
        System.out.println("response:"+sayHelloResponse.getResult());
        simpleClientStart.shutdown();
    }

    class ClientRequestInterceptor implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                   CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                @Override
                public void start(ClientCall.Listener<RespT> responseListener, Metadata headers) {
                    Metadata.Key<String> metaDataKey = Metadata.Key.of("Client-request", Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(metaDataKey, "Client-request-extend-value");
                    super.start(responseListener, headers);
                }
            };
        }
    }

    class ClientResponseInterceptor implements ClientInterceptor {

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                   CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                            responseListener) {
                        @Override
                        public void onHeaders(Metadata headers) {
                            Metadata.Key<String> metaDataKey = Metadata.Key.of("Server-Request",Metadata.ASCII_STRING_MARSHALLER);
                            System.out.println(headers.get(metaDataKey));
                            super.onHeaders(headers);
                        }
                    }, headers);
                }
            };
        }
    }
}
