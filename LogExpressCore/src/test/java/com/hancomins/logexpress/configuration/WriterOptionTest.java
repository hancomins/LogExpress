package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.LogExpress;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class WriterOptionTest {

    @Test
    public void staticValueReplaceTest() throws InterruptedException {
        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption[] options = configuration.getWriterOptions();
        options[0].setFile("${"  + LogExpress.PROPERTY_PATH +  "}/${"  + LogExpress.PROPERTY_PID +  "}/${"  + LogExpress.PROPERTY_HOSTNAME +  "}/test.txt");
        options[0].addWriterType(WriterType.File);

        LogExpress.updateConfig(configuration);
        System.out.println(options[0].getFile());
        LogExpress.info("ok");

        assertFalse(options[0].getFile().contains("${"));
        File logFile = new File(options[0].getFile());
        assertTrue(logFile.exists());
        System.out.println(new File(options[0].getFile()).isFile());

        logFile.delete();



    }

}