package com.grpc.demo.encryption;


import com.google.protobuf.StringValue;
import com.yyc.grpc.contract.SayHelloResponse;
import com.yyc.grpc.contract.SimpleServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class SimpleClientStart {

    private ManagedChannel managedChannel;
    private int PORT = 8888;

    private void createChannel(){
        SslContext sslContext = null;
        try {
            String filePath = SimpleServerStart.class.getClassLoader().getResource("").getPath();
            File certChain = new File(filePath.concat("server1-public-ca.pem"));
            sslContext = GrpcSslContexts.forClient()
                    .sslProvider(SslProvider.OPENSSL)
                    .trustManager(certChain)
                    .build();

        } catch (SSLException e) {
            e.printStackTrace();
        }
        //Mind :this the set  host=waterzooi.test.google.be ,cause CA defined,
        // so C:\Windows\System32\drivers\etc\hosts append 127.0.0.1  waterzooi.test.google.be
        managedChannel = NettyChannelBuilder.forAddress("waterzooi.test.google.be",PORT)
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext).build();
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

        SayHelloResponse sayHelloResponse = simpleServiceStub.sayHello(StringValue.newBuilder().setValue("grpc-encryption-demo").build());
        System.out.println("response:"+sayHelloResponse.getResult());
        simpleClientStart.shutdown();
    }
}
