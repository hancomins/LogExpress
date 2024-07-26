package com.hancomins.logexpress.slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LogExpressLoggerFactory implements org.slf4j.ILoggerFactory {

    ConcurrentMap<String, org.slf4j.Logger> loggerMap;

    public LogExpressLoggerFactory() {
        loggerMap = new ConcurrentHashMap<String, org.slf4j.Logger>();
    }




    public org.slf4j.Logger getLogger(String name) {
        if (name == null || name.equalsIgnoreCase(org.slf4j.Logger.ROOT_LOGGER_NAME)) {
            name = "";
        }
        org.slf4j.Logger slf4jLogger = loggerMap.get(name);
        if (slf4jLogger != null)
            return slf4jLogger;

        Logger logger = new Logger(name);
        loggerMap.put(name, logger);
        return logger;
    }
}
