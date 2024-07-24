package com.hancomins.LogExpress.queue;

import com.hancomins.LogExpress.InLogger;

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
