package com.grpc.demo.proxy.proxy1;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yeyc on 2016/12/12.
 */
public class ProxyToServerChannel  {

    private Channel channel;

    private AtomicInteger count;

    public ProxyToServerChannel(Channel channel){
        this.channel =channel;
        count = new AtomicInteger(0);
    }

    public synchronized int getCount(){
        return count.incrementAndGet();
    }

    public Channel getChannel() {
        return channel;
    }
}
