package com.grpc.demo.proxy.proxy1;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * Created by yeyc on 2016/12/12.
 */
public class ClientToProxyChannels {
    public static final Map<Integer,Channel> clientToProxyChannelMap = Maps.newConcurrentMap();

    public static final Map<Integer,Integer> clientToProxyStreamIdMap = Maps.newConcurrentMap();
}
