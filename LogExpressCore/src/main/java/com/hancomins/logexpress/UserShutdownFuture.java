package com.hancomins.logexpress;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

class UserShutdownFuture implements ShutdownFuture {
    private final ArrayList<Runnable> eventList = new ArrayList<Runnable>();
    private final Object monitor = new Object();
    private final AtomicBoolean isEnd = new AtomicBoolean();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public boolean isEnd() {
        return isEnd.get();
    }

    @Override
    public void onEnd() {
        synchronized (monitor) {
            isEnd.set(true);

            for (Runnable runnable : eventList) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    if (InLogger.isEnabled()) {
                        InLogger.ERROR("Shutdown event call failed.", e);
                    }
                }
            }
            countDownLatch.countDown();
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