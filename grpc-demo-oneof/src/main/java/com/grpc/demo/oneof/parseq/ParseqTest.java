package com.grpc.demo.oneof.parseq;

import com.linkedin.parseq.Engine;
import com.linkedin.parseq.EngineBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Created by yeyc on 2016/11/22.
 */
public class ParseqTest {
    private static final int numCores = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService taskScheduler = Executors.newFixedThreadPool(numCores+1);

    private static final ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Engine engine = new EngineBuilder()
            .setTaskExecutor(taskScheduler)
            .setTimerScheduler(timerScheduler)
            .build();

    public static void main(String[] args) throws InterruptedException{
        System.out.println(numCores);


    }

}
