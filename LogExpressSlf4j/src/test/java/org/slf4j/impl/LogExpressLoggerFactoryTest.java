package org.slf4j.impl;

import com.clipsoft.LogExpress.Level;
import com.clipsoft.LogExpress.LogExpress;
import com.clipsoft.LogExpress.configuration.Configuration;
import com.clipsoft.LogExpress.configuration.WriterOption;
import com.clipsoft.LogExpress.slf4j.LogExpressLoggerFactory;
import com.clipsoft.LogExpress.slf4j.Logger;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Iterator;

import static org.junit.Assert.*;

public class LogExpressLoggerFactoryTest {

    @Test
    public void testGetLogger() throws Exception {
        Configuration configuration = LogExpress.cloneConfiguration();
        configuration.setDefaultMarker("test");
        WriterOption option = configuration.newWriterOption("test");
        option.setLevel(Level.TRACE);
        option.setLinePattern("{time:hh:mm:ss.SSS} [{level}] {caller-simple} {hostname}:{pid}:{thread}<{tid}>:{class-name} {class}.{method}():{line} {message}");
        LogExpress.updateConfig(configuration);


        LogExpressLoggerFactory logExpressLoggerFactory = new LogExpressLoggerFactory();
        org.slf4j.Logger log = logExpressLoggerFactory.getLogger("test");
        assertTrue(log instanceof Logger);
        LoggerFactory.getILoggerFactory();

        Marker mk = MarkerFactory.getMarker("test");
        org.slf4j.Logger logger = LoggerFactory.getLogger(LogExpressLoggerFactoryTest.class);
        logger.info("test");
        logger.info(mk ,"test2");
        com.clipsoft.LogExpress.slf4j.Logger expressLog = (Logger) logger;

        //public void log(Marker marker, String callerFQCN, int level, String message, Object[] argArray, Throwable t) {


        expressLog.log(mk ,this.getClass().getSimpleName(), 0,"test", null, null);

        assertTrue(logger instanceof Logger);

    }

}