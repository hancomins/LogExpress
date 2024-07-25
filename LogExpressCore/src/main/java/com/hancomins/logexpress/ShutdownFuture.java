package com.hancomins.logexpress;

public interface ShutdownFuture {
    public boolean isEnd();
    public void onEnd();
    public void setOnEndCallback(Runnable runnable);

    public void await() throws InterruptedException;

}
