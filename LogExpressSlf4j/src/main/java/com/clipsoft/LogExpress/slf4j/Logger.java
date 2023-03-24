package com.clipsoft.LogExpress.slf4j;

import com.clipsoft.LogExpress.BaseLogger;
import com.clipsoft.LogExpress.Level;
import com.clipsoft.LogExpress.LogExpress;
import com.clipsoft.LogExpress.MessageFormatter;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

import java.util.concurrent.ConcurrentHashMap;

public class Logger implements LocationAwareLogger {

    private String callerFQCN = "";
    private BaseLogger baseLogger = LogExpress.baseLogger();
    private ConcurrentHashMap<String, BaseLogger> loggerMap = new ConcurrentHashMap<String, BaseLogger>();

    private final int addedElementIndex = 2;


    protected Logger(String name) {
        callerFQCN = name;
        baseLogger = LogExpress.baseLogger();
    }

    @Override
    public void log(Marker marker, String callerFQCN, int level, String message, Object[] argArray, Throwable t) {


        Level expressLevel = slf4jLevelIntToLogExpressLevel(level);

        BaseLogger logger = getBaseLogger(marker);
        if(callerFQCN == null) callerFQCN = "";

        if (baseLogger.isLoggable(expressLevel)) {
            switch (expressLevel) {
                case TRACE:
                    logger.trace(callerFQCN, MessageFormatter.newFormat(message,argArray), t, addedElementIndex);
                    break;
                case DEBUG:
                    logger.debug(callerFQCN, MessageFormatter.newFormat(message,argArray), t, addedElementIndex);
                    break;
                case INFO:
                    logger.info(callerFQCN, MessageFormatter.newFormat(message,argArray), t, addedElementIndex);
                    break;
                case WARN:
                    logger.warn(callerFQCN, MessageFormatter.newFormat(message,argArray), t, addedElementIndex);
                    break;
                case ERROR:
                    logger.error(callerFQCN, MessageFormatter.newFormat(message,argArray), t, addedElementIndex);
                    break;
            }
        }
    }

    private Level slf4jLevelIntToLogExpressLevel(int slf4jLevelInt) {
        Level logExpressLevel;
        switch (slf4jLevelInt) {
            case LocationAwareLogger.TRACE_INT:
                logExpressLevel = Level.TRACE;
                break;
            case LocationAwareLogger.DEBUG_INT:
                logExpressLevel = Level.DEBUG;
                break;
            case LocationAwareLogger.INFO_INT:
                logExpressLevel = Level.INFO;
                break;
            case LocationAwareLogger.WARN_INT:
                logExpressLevel = Level.WARN;
                break;
            case LocationAwareLogger.ERROR_INT:
                logExpressLevel = Level.ERROR;
                break;
            default:
                throw new IllegalStateException("Level number " + slf4jLevelInt + " is not recognized.");
        }
        return logExpressLevel;
    }

    private BaseLogger getBaseLogger(Marker marker) {
        BaseLogger logger = baseLogger;
        String markerString = marker == null ? "" : marker.getName();
        if(markerString != null && !markerString.isEmpty()) {
            logger = loggerMap.get(markerString);
            if(logger == null) {
                logger = LogExpress.baseLogger(markerString);
                loggerMap.put(markerString, logger);
            }
        }
        return logger;
    }


    @Override
    public String getName() {
        return callerFQCN;
    }

    @Override
    public boolean isTraceEnabled() {
        return baseLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {

        baseLogger.trace(callerFQCN,msg, null, addedElementIndex);
    }

    @Override
    public void trace(String format, Object arg) {

        baseLogger.trace(callerFQCN,MessageFormatter.newFormat(format,arg), null, addedElementIndex);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

        baseLogger.trace(callerFQCN,MessageFormatter.newFormat(format,arg1,arg2), null, addedElementIndex);
    }

    @Override
    public void trace(String format, Object... arguments) {

        baseLogger.trace(callerFQCN,MessageFormatter.newFormat(format,arguments), null, addedElementIndex);

    }

    @Override
    public void trace(String msg, Throwable t) {

        baseLogger.trace(callerFQCN,msg, t, addedElementIndex);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        BaseLogger logger = getBaseLogger(marker);
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.trace(marker.getName(),msg,null, addedElementIndex);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.trace(marker.getName(),MessageFormatter.newFormat(format, arg), null, addedElementIndex);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        BaseLogger logger = getBaseLogger(marker);
        logger.trace(marker.getName(),MessageFormatter.newFormat(format, arg1,arg2), null, addedElementIndex);
    }

    @Override
    public void trace(Marker marker, String format, Object... arguments) {
        BaseLogger logger = getBaseLogger(marker);
        logger.trace(marker.getName(),MessageFormatter.newFormat(format, arguments), null, addedElementIndex);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        BaseLogger logger = getBaseLogger(marker);
        logger.trace(marker.getName(),msg,t, addedElementIndex);
    }


    @Override
    public boolean isDebugEnabled() {
        return baseLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {

        baseLogger.debug(callerFQCN,msg, null, addedElementIndex);
    }

    @Override
    public void debug(String format, Object arg) {

        baseLogger.debug(callerFQCN,MessageFormatter.newFormat(format,arg), null, addedElementIndex);

    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {

        baseLogger.debug(callerFQCN,MessageFormatter.newFormat(format,arg1,arg2), null, addedElementIndex);
    }

    @Override
    public void debug(String format, Object... arguments) {
        baseLogger.debug(callerFQCN,MessageFormatter.newFormat(format,arguments), null, addedElementIndex);

    }

    @Override
    public void debug(String msg, Throwable t) {
        baseLogger.debug(callerFQCN,msg, t, addedElementIndex);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        BaseLogger logger = getBaseLogger(marker);
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.debug(marker.getName(),msg,null, addedElementIndex);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.debug(marker.getName(),MessageFormatter.newFormat(format, arg), null, addedElementIndex);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        BaseLogger logger = getBaseLogger(marker);
        logger.debug(marker.getName(),MessageFormatter.newFormat(format, arg1,arg2), null, addedElementIndex);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        BaseLogger logger = getBaseLogger(marker);
        logger.debug(marker.getName(),MessageFormatter.newFormat(format, arguments), null, addedElementIndex);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        BaseLogger logger = getBaseLogger(marker);
        logger.debug(marker.getName(),msg,t, addedElementIndex);
    }


    @Override
    public boolean isInfoEnabled() {
        return baseLogger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {

        baseLogger.info(callerFQCN,msg, null, addedElementIndex);

    }

    @Override
    public void info(String format, Object arg) {

        baseLogger.info(callerFQCN,MessageFormatter.newFormat(format,arg), null, addedElementIndex);

    }

    @Override
    public void info(String format, Object arg1, Object arg2) {

        baseLogger.info(callerFQCN,MessageFormatter.newFormat(format,arg1, arg2), null, addedElementIndex);
    }

    @Override
    public void info(String format, Object... arguments) {

        baseLogger.info(callerFQCN,MessageFormatter.newFormat(format,arguments), null, addedElementIndex);
    }

    @Override
    public void info(String msg, Throwable t) {

        baseLogger.info(callerFQCN,msg,t, addedElementIndex);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        BaseLogger logger = getBaseLogger(marker);
        return logger.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.info(marker.getName(),msg,null, addedElementIndex);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.info(marker.getName(),MessageFormatter.newFormat(format, arg), null, addedElementIndex);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        BaseLogger logger = getBaseLogger(marker);
        logger.info(marker.getName(),MessageFormatter.newFormat(format, arg1,arg2), null, addedElementIndex);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        BaseLogger logger = getBaseLogger(marker);
        logger.info(marker.getName(),MessageFormatter.newFormat(format, arguments), null, addedElementIndex);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        BaseLogger logger = getBaseLogger(marker);
        logger.info(marker.getName(),msg,t, addedElementIndex);
    }


    @Override
    public boolean isWarnEnabled() {
        return baseLogger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        baseLogger.warn(callerFQCN,msg,null, addedElementIndex);
    }

    @Override
    public void warn(String format, Object arg) {
        baseLogger.warn(callerFQCN,MessageFormatter.newFormat(format,arg),null, addedElementIndex);
    }

    @Override
    public void warn(String format, Object... arguments) {
        baseLogger.warn(callerFQCN,MessageFormatter.newFormat(format,arguments),null,addedElementIndex);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        baseLogger.warn(callerFQCN,MessageFormatter.newFormat(format,arg1, arg2),null,addedElementIndex);
    }

    @Override
    public void warn(String msg, Throwable t) {

        baseLogger.warn(callerFQCN,msg,t, addedElementIndex);


    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        BaseLogger logger = getBaseLogger(marker);
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.warn(marker.getName(),msg,null, addedElementIndex);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.warn(marker.getName(),MessageFormatter.newFormat(format, arg), null, addedElementIndex);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        BaseLogger logger = getBaseLogger(marker);
        logger.warn(marker.getName(),MessageFormatter.newFormat(format, arg1,arg2), null, addedElementIndex);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        BaseLogger logger = getBaseLogger(marker);
        logger.warn(marker.getName(),MessageFormatter.newFormat(format, arguments), null, addedElementIndex);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        BaseLogger logger = getBaseLogger(marker);
        logger.warn(marker.getName(),msg,t, addedElementIndex);
    }



    @Override
    public boolean isErrorEnabled() {
        return baseLogger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        baseLogger.error(callerFQCN,msg,null, addedElementIndex);
    }

    @Override
    public void error(String format, Object arg) {
        baseLogger.error(callerFQCN,MessageFormatter.newFormat(format,arg),null, addedElementIndex);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        baseLogger.error(callerFQCN,MessageFormatter.newFormat(format,arg1, arg2),null, addedElementIndex);
    }

    @Override
    public void error(String format, Object... arguments) {
        baseLogger.error(callerFQCN,MessageFormatter.newFormat(format,arguments),null, addedElementIndex);
    }

    @Override
    public void error(String msg, Throwable t) {
        baseLogger.error(callerFQCN,msg,t, addedElementIndex);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        BaseLogger logger = getBaseLogger(marker);
        return logger.isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.error(marker.getName(),msg,null, addedElementIndex);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        BaseLogger logger = getBaseLogger(marker);
        logger.error(marker.getName(),MessageFormatter.newFormat(format, arg), null, addedElementIndex);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        BaseLogger logger = getBaseLogger(marker);
        logger.error(marker.getName(),MessageFormatter.newFormat(format, arg1,arg2), null, addedElementIndex);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        BaseLogger logger = getBaseLogger(marker);
        logger.error(marker.getName(),MessageFormatter.newFormat(format, arguments), null, addedElementIndex);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        BaseLogger logger = getBaseLogger(marker);
        logger.error(marker.getName(),msg,t, addedElementIndex);
    }
}
