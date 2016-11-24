package com.grpc.demo.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * Created by yeyc on 2016/11/23.
 */
public class ByteBufTest {

    public static void main(String[] args) {
        ByteBuf heapBuffer = Unpooled.buffer(8);

        heapBuffer.writeCharSequence("grpc-simple-demo", StandardCharsets.UTF_8);

        System.out.println(heapBuffer);
        System.out.println(heapBuffer.readableBytes());
        System.out.println(heapBuffer.writableBytes());

        byte[] b = new byte[4];
        heapBuffer.readBytes(b);
        String str = new String(b);
        System.out.println(str);

        System.out.println(heapBuffer.readableBytes());
        System.out.println(heapBuffer.writableBytes());

        heapBuffer.retain();//保留

        System.out.println(heapBuffer.readableBytes());
        System.out.println(heapBuffer.writableBytes());

    }
}
