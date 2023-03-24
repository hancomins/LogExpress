package com.clipsoft.LogExpress;

class LoggerImpl implements Logger {

    private BaseLogger baseLogger;
    private int addedStackTraceElementIndex = 0;


    protected LoggerImpl(BaseLogger baseLogger) {
        this.baseLogger = baseLogger;
    }



    @Override
    public String getMarker() {
        return baseLogger.getMarker();
    }

    @Override
    public Level getLevel() {
        return baseLogger.getLevel();
    }

    @Override
    public boolean isInfo() {
        return this.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return baseLogger.isInfoEnabled();
    }

    @Override
    public boolean isError() {
        return baseLogger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return baseLogger.isErrorEnabled();
    }

    @Override
    public boolean isDebug() {
        return baseLogger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return baseLogger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return baseLogger.isTraceEnabled();
    }

    @Override
    public boolean isTrace() {
        return baseLogger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return baseLogger.isWarnEnabled();
    }

    @Override
    public boolean isWarn() {
        return baseLogger.isWarnEnabled();
    }

    @Override
    public void setAddedStackTraceElementIndex(int addedStackTraceElementIndex) {
        this.addedStackTraceElementIndex = addedStackTraceElementIndex;
    }

    @Override
    public boolean isLoggable(Level level) {
        return baseLogger.isLoggable(level);
    }

    @Override
    public void info(CharSequence messages) {
        baseLogger.info("",messages, addedStackTraceElementIndex);
    }

    @Override
    public void info(String messages, Object... args) {
        baseLogger.info("",MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void info(CharSequence messages, Throwable e) {
        baseLogger.info( "",messages, e, addedStackTraceElementIndex);
    }

    @Override
    public void error(CharSequence messages, Throwable e) {
        baseLogger.error( "",messages, e, addedStackTraceElementIndex);
    }

    @Override
    public void error(CharSequence messages) {
        baseLogger.error( "",messages, addedStackTraceElementIndex);
    }

    @Override
    public void error(String messages, Object... args) {
        baseLogger.error("",MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void debug(CharSequence messages) {
        baseLogger.debug("", messages, addedStackTraceElementIndex);
    }

    @Override
    public void debug(String messages, Object... args) {
        baseLogger.debug("",MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void debug(CharSequence messages, Throwable e) {
        baseLogger.debug("", messages, e, addedStackTraceElementIndex);
    }

    @Override
    public void trace(CharSequence messages) {
        baseLogger.trace( "",messages, addedStackTraceElementIndex);
    }

    @Override
    public void trace(String messages, Object... args) {
        baseLogger.trace("",MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void trace(CharSequence messages, Throwable e) {
        baseLogger.trace("", messages, e, addedStackTraceElementIndex);
    }

    @Override
    public void warn(CharSequence messages) {
        baseLogger.warn("",messages, addedStackTraceElementIndex);
    }

    @Override
    public void warn(String messages, Object... args) {
        baseLogger.warn("",MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void warn(CharSequence messages, Throwable e) {
        baseLogger.warn("", messages, e, addedStackTraceElementIndex);
    }

}
