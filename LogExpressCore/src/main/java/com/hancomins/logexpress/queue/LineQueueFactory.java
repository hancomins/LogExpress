package com.hancomins.logexpress.queue;

import com.hancomins.logexpress.InLogger;

public class LineQueueFactory {

    public enum LineQueueType {
        Blocking,NonBlocking
    }


    public static AbsLineQueue create(LineQueueType type, int capacity) {
        if(type == LineQueueType.Blocking) {
            if(InLogger.isEnabled()) {
                InLogger.DEBUG("BlockingLineQueue is created. capacity : " + capacity);
            }
            return new BlockingLineQueue(capacity);
        }
        InLogger.DEBUG("Non-BlockingLineQueue is created. capacity : " + capacity);

        return new ConcurrentLineQueue(capacity);
    }

}
