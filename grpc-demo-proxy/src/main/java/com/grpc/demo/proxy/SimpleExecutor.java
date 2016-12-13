package com.grpc.demo.proxy;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleExecutor {
    private static Logger logger = LoggerFactory.getLogger(SimpleExecutor.class);
    private final Runnable task;
    private final AtomicLong counter = new AtomicLong(0L);
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    final int threadCount = 100;

    public SimpleExecutor(Runnable task) {
        Preconditions.checkNotNull(task, "task should not be null");
        this.task = task;
    }

    public void execute(int threadCount, int durationInSeconds) {
        Preconditions.checkArgument(threadCount > 0, "threadCount must bigger than 0");
        Preconditions.checkArgument(durationInSeconds > 0, "durationInSeconds must bigger than 0");

        for(int firstStartTime = 0; firstStartTime < threadCount; ++firstStartTime) {
            int threadNumber = firstStartTime + 1;
            Thread startTime = new Thread(() -> {
                while(!this.stopFlag.get()) {
                    try {
                        this.task.run();
                        this.counter.incrementAndGet();
                    } catch (Exception var3) {
                        var3.printStackTrace();
                    }
                }

                System.err.println("*** work thread " + threadNumber + " quit ***");
            });
            startTime.setDaemon(true);
            startTime.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.err.println("*** stop client ***");
                SimpleExecutor.this.stopFlag.set(true);
            }
        });
        long var14 = System.currentTimeMillis();

        for(long var15 = System.currentTimeMillis(); !this.stopFlag.get(); var15 = System.currentTimeMillis()) {
            if(System.currentTimeMillis() - var14 >= (long)(durationInSeconds * 1000)) {
                System.out.println("durationInSeconds timeout, begin to stop work thread");
                this.stopFlag.set(true);
                break;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException var13) {
                ;
            }

            long count = this.counter.get();
            long currentTime = System.currentTimeMillis();
            long qps = count * 1000L / (currentTime - var15);
            System.out.println("qps=" + qps);
            this.counter.set(0L);
        }

        System.out.println("task executed success");
    }
}
