package com.hancomins.logexpress;

@SuppressWarnings("unused")
public class LoggerFacade implements Logger{
    private final Logger logger;

    public LoggerFacade(Logger logger){
        this.logger = logger;
    }

    @Override
    public String getMarker() {
        return logger.getMarker();
    }

    @Override
    public Level getLevel() {
        return logger.getLevel();
    }

    @Override
    public boolean isInfo() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isError() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isDebug() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isTrace() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isWarn() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
    }

    @Override
    public void setAddedStackTraceElementIndex(int addedStackTraceElementIndex) {
        logger.setAddedStackTraceElementIndex(addedStackTraceElementIndex);
    }

    @Override
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    @Override
    public void info(CharSequence messages) {
        logger.info(messages);
    }

    @Override
    public void info(String messages, Object... args) {
        logger.info(messages, args);
    }

    @Override
    public void info(CharSequence message, Throwable e) {
        logger.info(message, e);
    }

    @Override
    public void error(CharSequence messages) {
        logger.error(messages);
    }

    @Override
    public void error(String messages, Object... args) {
        logger.error(messages, args);
    }

    @Override
    public void error(CharSequence messageFormatter, Throwable e) {
        logger.error(messageFormatter, e);
    }

    @Override
    public void fatal(CharSequence messages) {
        logger.fatal(messages);
    }

    @Override
    public void fatal(String messages, Object... args) {
        logger.fatal(messages, args);
    }

    @Override
    public void fatal(CharSequence messageFormatter, Throwable e) {
        logger.fatal(messageFormatter, e);
    }

    @Override
    public void debug(CharSequence messages) {
        logger.debug(messages);
    }

    @Override
    public void debug(String messages, Object... args) {
        logger.debug(messages, args);
    }

    @Override
    public void debug(CharSequence messages, Throwable e) {
        logger.debug(messages, e);
    }

    @Override
    public void trace(CharSequence messages) {
        logger.trace(messages);
    }


    @Override
    public void trace(String message, Object... args) {
        logger.trace(message, args);
    }

    @Override
    public void trace(CharSequence messages, Throwable e) {
        logger.trace(messages, e);
    }

    @Override
    public void warn(CharSequence messages) {
        logger.warn(messages);
    }

    @Override
    public void warn(String messages, Object... args) {
        logger.warn(messages, args);
    }

    @Override
    public void warn(CharSequence messages, Throwable e) {
        logger.warn(messages, e);
    }

}
