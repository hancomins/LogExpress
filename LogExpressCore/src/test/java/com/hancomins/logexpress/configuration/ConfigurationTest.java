package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.LogExpress;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;


public class ConfigurationTest {

    @Test
    public void configFileLoadFromResourceTest() throws InterruptedException {
        LogExpress.shutdown().await();
        LogExpress.updateConfig(LogExpress.cloneConfiguration());

        LogExpress.info("Test");
        assertEquals(LogExpress.cloneConfiguration().getDefaultMarker(), "test");
    }

    @Test
    public void warnConfigFileLoad() throws InterruptedException, IOException {
        LogExpress.shutdown().await();
        Configuration configuration = Configuration.newConfiguration(new StringReader("{}"));
        LogExpress.updateConfig(configuration);
        LogExpress.newLogger("ok");
        LogExpress.info("Test");
    }

    @Test
    public void loadNullPropertiesTest() throws InterruptedException, IOException {
        LogExpress.shutdown().await();
        LogExpress.updateConfig(null);
        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption writerOption = configuration.newWriterOption("test");
        writerOption.setLinePattern("${testr} {messge}" );
        writerOption.addWriterType(WriterType.Console);
        LogExpress.updateConfig(configuration);


        LogExpress.newLogger("ok");
        LogExpress.info("Test");
    }



}