package com.grpc.demo.interceptor;


import io.grpc.*;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;

public class SimpleServerStart {

    private int PORT=8888;
    private Server server;

    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(ServerInterceptors.intercept(new SimpleServer().bindService(),new ServerRequestInterceptor(),new ServerResponseInterceptor()))
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SimpleServerStart.this.stop();
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
        final SimpleServerStart simpleServerStart = new SimpleServerStart();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(120);
    }


    class ServerResponseInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            Metadata.Key<String> metaDataKey = Metadata.Key.of("Client-request",
                    Metadata.ASCII_STRING_MARSHALLER);
            System.out.println("Client-request:"+headers.get(metaDataKey));
            return next.startCall(call, headers);
        }
    }

    class ServerRequestInterceptor implements ServerInterceptor{
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                @Override
                public void sendHeaders(Metadata responseHeaders) {
                    responseHeaders.put(Metadata.Key.of("Server-Request", Metadata.ASCII_STRING_MARSHALLER), "ServerRequest-extendValue");
                    super.sendHeaders(responseHeaders);
                }
            }, headers);
        }
    }

}

