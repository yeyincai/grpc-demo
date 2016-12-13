package com.grpc.demo.proxy.proxy1;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * Created by yeyc on 2016/12/5.
 */
public class Servers {
    public static final Map<String,List<InetSocketAddress>> servers = init();


    private static Map init(){
        Map<String,List<InetSocketAddress>> result = Maps.newConcurrentMap();
        List<InetSocketAddress> s1 = Lists.newArrayList();
        s1.add(new InetSocketAddress("127.0.0.1",8888));
       // s1.add(new InetSocketAddress("127.0.0.1",8889));
        result.put("SimpleService",s1);

        List<InetSocketAddress> s2 = Lists.newArrayList();
        s2.add(new InetSocketAddress("127.0.0.1",9998));
        s2.add(new InetSocketAddress("127.0.0.1",9999));
        result.put("YycService",s2);
        return result;
    }
}
