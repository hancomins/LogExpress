package org.slf4j.impl;

import com.hancomins.logexpress.slf4j.LogExpressLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import java.lang.reflect.Field;

public class StaticLoggerBinder implements LoggerFactoryBinder {

    private static static finalLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static static finalLoggerBinder getSingleton() {
        return SINGLETON;
    }


    private static final String loggerFactoryClassStr = LogExpressLoggerFactory.class.getName();


    private final ILoggerFactory loggerFactory;

    private StaticLoggerBinder() {
        loggerFactory = new LogExpressLoggerFactory();
    }

    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }

    public static void forceBind() {
        Class<?> loggerFactoryClass = org.slf4j.LoggerFactory.class;
        try {
            Field field = loggerFactoryClass.getDeclaredField("STATIC_LOGGER_BINDER_PATH");
            field.setAccessible(true);
            String name = StaticLoggerBinder.class.getCanonicalName();
            name = name.replace(".","/") + ".class";
            field.set(null, name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


}