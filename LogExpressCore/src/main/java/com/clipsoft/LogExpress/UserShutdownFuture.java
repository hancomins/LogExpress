package com.clipsoft.LogExpress;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

class UserShutdownFuture implements ShutdownFuture {
    private ArrayList<Runnable> eventList = new ArrayList<Runnable>();
    private final Object monitor = new Object();
    private AtomicBoolean isEnd = new AtomicBoolean();
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public boolean isEnd() {
        return isEnd.get();
    }

    @Override
    public void onEnd() {
        synchronized (monitor) {
            isEnd.set(true);
            countDownLatch.countDown();
            for (Runnable runnable : eventList) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    if (InLogger.isEnabled()) {
                        InLogger.ERROR("Shutdown event call failed.", e);
                    }
                }
            }
            eventList.clear();
        }
    }

    @Override
    public void setOnEndCallback(Runnable runnable) {
        synchronized (monitor) {
            if(isEnd.get()) {
                runnable.run();
            } else {
                eventList.add(runnable);
            }
        }

    }

    @Override
    public void await() throws InterruptedException {
        if(isEnd.get()) return;
        countDownLatch.await();
    }
}