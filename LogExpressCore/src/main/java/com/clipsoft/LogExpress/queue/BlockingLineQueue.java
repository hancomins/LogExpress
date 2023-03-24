package com.clipsoft.LogExpress.queue;

import com.clipsoft.LogExpress.Line;

import java.util.ArrayDeque;

public class BlockingLineQueue extends AbsLineQueue {

    private ArrayDeque<Line> lineQueue;
    private Object monitor = new Object();

    protected BlockingLineQueue(int capacity) {
        super(capacity);
        lineQueue = new ArrayDeque<Line>(capacity);
    }



    @Override
    public void push(Line line) {
        synchronized (monitor) {
            if(lineQueue.size() > mCapacity) {
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
