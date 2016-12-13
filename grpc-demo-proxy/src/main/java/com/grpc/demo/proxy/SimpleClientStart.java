package com.grpc.demo.proxy;


import com.google.protobuf.StringValue;
import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;
import io.grpc.*;
import io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleClientStart {

    private int PORT = 8080;
    private ManagedChannel managedChannel;
    private ManagedChannel createChannel() {
        managedChannel = NettyChannelBuilder.forAddress("localhost", PORT)
                .intercept(new ClientRequestInterceptor()).usePlaintext(true).build();
        return managedChannel;
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


    public static void main(String[] args) throws InterruptedException {
        AtomicInteger integer = new AtomicInteger(0);
        SimpleClientStart simpleClientStart = new SimpleClientStart();
        SimpleServiceGrpc.SimpleServiceBlockingStub simpleServiceStub = SimpleServiceGrpc.newBlockingStub(simpleClientStart.createChannel());
        String request = "grpc-simple-demo"+integer.incrementAndGet();
        SayHelloResponse sayHelloResponse = simpleServiceStub.sayHello(StringValue.newBuilder().setValue(request).build());
        assert request.equalsIgnoreCase(sayHelloResponse.getResult());
        simpleClientStart.managedChannel.shutdown();

       if(true) {
            SimpleExecutor simpleExecutor = new SimpleExecutor(() -> {
                SimpleClientStart simpleClientStart1 = new SimpleClientStart();
                SimpleServiceGrpc.SimpleServiceBlockingStub simpleServiceStub1 = SimpleServiceGrpc.newBlockingStub(simpleClientStart1.createChannel());
                String request1 = "grpc-simple-demo"+integer.incrementAndGet();
                SayHelloResponse sayHelloResponse1 = simpleServiceStub1.sayHello(StringValue.newBuilder().setValue(request1).build());
                assert request1.equalsIgnoreCase(sayHelloResponse1.getResult());
                simpleClientStart1.managedChannel.shutdown();
            });
            simpleExecutor.execute(5, 100);
        }


    }
}
