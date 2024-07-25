package com.hancomins.logexpress.queue;

import com.hancomins.logexpress.Line;

import java.util.ArrayDeque;

public class BlockingLineQueue extends AbsLineQueue {

    private final ArrayDeque<Line> lineQueue;
    private final Object monitor = new Object();

    protected BlockingLineQueue(int capacity) {
        super(capacity);
        lineQueue = new ArrayDeque<Line>(capacity);
    }



    @Override
    public void push(Line line) {
        synchronized (monitor) {
            if(lineQueue.size() > capacity) {
                try {
                    monitor.wait();
                } catch (InterruptedException ignored) {}
            }
            lineQueue.push(line);
            super.push(line);
        }
    }

    @Override
    public Line pop() {
        synchronized (monitor) {
            monitor.notify();
            return lineQueue.poll();
        }
    }


}
