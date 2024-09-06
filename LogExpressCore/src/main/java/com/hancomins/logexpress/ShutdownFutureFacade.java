package com.hancomins.logexpress;

public class ShutdownFutureFacade implements ShutdownFuture {

    private final ShutdownFuture shutdownFuture;

    public ShutdownFutureFacade(ShutdownFuture shutdownFuture) {
        this.shutdownFuture = shutdownFuture;
    }

    @Override
    public boolean isEnd() {
        return shutdownFuture.isEnd();
    }

    @Override
    public void onEnd() {}

    @Override
    public void addOnEndCallback(Runnable runnable) {
        shutdownFuture.addOnEndCallback(runnable);
    }

    @Override
    public void await() throws InterruptedException {
        shutdownFuture.await();
    }

}
