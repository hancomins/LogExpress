package com.clipsoft.LogExpress.queue;

import com.clipsoft.LogExpress.InLogger;

public class LineQueueFactory {

    public static enum LineQueueType {
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
