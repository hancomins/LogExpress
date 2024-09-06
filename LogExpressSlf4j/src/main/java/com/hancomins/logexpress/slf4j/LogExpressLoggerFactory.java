package com.hancomins.logexpress.slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A factory for creating SLF4J Logger instances.<br>
 * This factory maintains a map of logger instances to ensure that<br>
 * the same logger is returned for the same name.<br>
 * <br>
 * SLF4J Logger 인스턴스를 생성하는 팩토리입니다.<br>
 * 이 팩토리는 로거 인스턴스의 맵을 유지하여<br>
 * 동일한 이름에 대해 동일한 로거가 반환되도록 합니다.<br>
 *
 * @see org.slf4j.ILoggerFactory
 * @author beom
 */
public class LogExpressLoggerFactory implements org.slf4j.ILoggerFactory {

    // A concurrent map to store logger instances by name.<br>
    // 이름별로 로거 인스턴스를 저장하는 동시성 맵입니다.<br>
    private final ConcurrentMap<String, org.slf4j.Logger> loggerMap;

    /**
     * Constructs a new LogExpressLoggerFactory.<br>
     * Initializes the logger map.<br>
     * <br>
     * 새로운 LogExpressLoggerFactory를 생성합니다.<br>
     * 로거 맵을 초기화합니다.<br>
     */
    public LogExpressLoggerFactory() {
        loggerMap = new ConcurrentHashMap<String, org.slf4j.Logger>();
    }

    /**
     * Returns a logger instance for the given name.<br>
     * If a logger with the given name already exists, it is returned.<br>
     * Otherwise, a new logger is created, stored in the map, and returned.<br>
     * <br>
     * 주어진 이름에 대한 로거 인스턴스를 반환합니다.<br>
     * 주어진 이름의 로거가 이미 존재하는 경우, 해당 로거를 반환합니다.<br>
     * 그렇지 않으면 새로운 로거를 생성하여 맵에 저장하고 반환합니다.<br>
     *
     * @param name the name of the logger<br>
     *             로거의 이름<br>
     * @return the logger instance<br>
     *         로거 인스턴스<br>
     */
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