package com.clipsoft.LogExpress.writer;

public class CurrentTimeMillisGetter {

    private static TimeMillisDelegator GET_TIME_MILLIS_DELEGATOR = new TimeMillisDelegator() {
        @Override
        public long getCurrentTimeMillis() {
            return System.currentTimeMillis();
        }
    };

    public static void setGetTimeMillisDelegator(TimeMillisDelegator timeMillisDelegator) {
        GET_TIME_MILLIS_DELEGATOR = timeMillisDelegator;
    }

    public static long currentTimeMillis() {
        return GET_TIME_MILLIS_DELEGATOR.getCurrentTimeMillis();
    }

    public static interface TimeMillisDelegator {
        public long getCurrentTimeMillis();
    }
}
