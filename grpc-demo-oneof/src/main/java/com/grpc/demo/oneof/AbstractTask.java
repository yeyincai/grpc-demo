package com.grpc.demo.oneof;


import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

abstract class AbstractTask implements Runnable {


    private CountDownLatch counter;

    private StreamObserver so;

    public AbstractTask(CountDownLatch counter, StreamObserver so) {
        this.counter = counter;
        this.so = so;
    }

    @Override
    public void run() {
        try {
            Object result = invoke();
            synchronized (so) {

                so.onNext(result);
            }
        } catch (Exception e) {
        } finally {
            counter.countDown();

        }
    }

    abstract com.yyc.grpc.contract.GetAllResponse invoke();

}
