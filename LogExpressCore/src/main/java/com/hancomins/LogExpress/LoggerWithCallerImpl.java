package com.hancomins.LogExpress;

class LoggerWithCallerImpl implements Logger {

    private final String className;
    private final BaseLogger baseLogger;
    private int addedStackTraceElementIndex = 0;


    protected LoggerWithCallerImpl(String className, BaseLogger baseLogger) {
        this.className = className == null ? "" : className;
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
    public boolean isFatalEnabled() {
        return baseLogger.isFatalEnabled();
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
        baseLogger.info(className, messages, addedStackTraceElementIndex);
    }

    @Override
    public void info(String messages, Object... args) {
        baseLogger.info(className,MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void info(CharSequence messages, Throwable e) {
        baseLogger.info(className, messages, e, addedStackTraceElementIndex);
    }

    @Override
    public void error(CharSequence messages, Throwable e) {
        baseLogger.error(className, messages, e, addedStackTraceElementIndex);
    }


    @Override
    public void error(CharSequence messages) {
        baseLogger.error(className, messages, addedStackTraceElementIndex);
    }

    @Override
    public void error(String messages, Object... args) {
        baseLogger.error(className,MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void debug(CharSequence messages) {
        baseLogger.debug(className, messages, addedStackTraceElementIndex);
    }

    @Override
    public void debug(String messages, Object... args) {
        baseLogger.debug(className,MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void debug(CharSequence messages, Throwable e) {
        baseLogger.debug(className, messages, e, addedStackTraceElementIndex);
    }

    @Override
    public void trace(CharSequence messages) {
        baseLogger.trace(className, messages, addedStackTraceElementIndex);
    }

    @Override
    public void trace(String messages, Object... args) {
        baseLogger.trace(className,MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void trace(CharSequence messages, Throwable e) {
        baseLogger.trace(className, messages, e, addedStackTraceElementIndex);
    }

    @Override
    public void warn(CharSequence messages) {
        baseLogger.warn(className,messages, addedStackTraceElementIndex);
    }

    @Override
    public void warn(String messages, Object... args) {
        baseLogger.warn(className,MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void warn(CharSequence messages, Throwable e) {
        baseLogger.warn(className, messages, e, addedStackTraceElementIndex);
    }


    @Override
    public void fatal(CharSequence messages) {
        baseLogger.fatal(className, messages, addedStackTraceElementIndex);
    }

    @Override
    public void fatal(String messages, Object... args) {
        baseLogger.fatal(className,MessageFormatter.newFormat(messages,args), addedStackTraceElementIndex);
    }

    @Override
    public void fatal(CharSequence messageFormatter, Throwable e) {
        baseLogger.fatal(className, messageFormatter, e, addedStackTraceElementIndex);
    }
}
