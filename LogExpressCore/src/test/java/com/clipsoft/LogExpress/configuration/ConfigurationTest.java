package com.clipsoft.LogExpress.configuration;

import com.clipsoft.LogExpress.LogExpress;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ConfigurationTest {

    @Test
    public void configFileLoadFromResourceTest() {
        LogExpress.info("Test");
        assertEquals(LogExpress.cloneConfiguration().getDefaultMarker(), "test");

    }

}