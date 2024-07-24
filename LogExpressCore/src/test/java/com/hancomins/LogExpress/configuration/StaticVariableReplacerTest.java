package com.hancomins.LogExpress.configuration;

import org.junit.Test;

import static org.junit.Assert.*;

public class StaticVariableReplacerTest {

    @Test
    public void replaceEnvTest() {
        String firstKey = System.getenv().keySet().iterator().next();
        String firstValue = System.getenv(firstKey);
        String result = StaticVariableReplacer.replaceEnv("테스트 값%" + firstKey + "%은 이거다.");
        assertEquals("테스트 값" + firstValue + "은 이거다.", result);

        result = StaticVariableReplacer.replaceEnv("%" + firstKey + "%테스트 값%NONE_KEY%은 이거다.%" + firstKey + "%");
        assertEquals(firstValue + "테스트 값%NONE_KEY%은 이거다." + firstValue, result);


    }


    @Test
    public void replacePropertiesTest() {
        String firstKey = "java.version";
        String firstValue = System.getProperty(firstKey);
        String secondKey = "java.home";
        String secondValue = System.getProperty(secondKey);

        String result = StaticVariableReplacer.replaceProperty("자바 버전은 ${" + firstKey + "} 이고, java.home 은 ${java.home} 여깄다.");
        assertEquals("자바 버전은 " + firstValue + " 이고, java.home 은 " + secondValue + " 여깄다.", result);

        result = StaticVariableReplacer.replaceProperty("${" + firstKey + "}테스트 값${NONE_KEY}은 이거다.${" + firstKey + "}");
        assertEquals(firstValue + "테스트 값${NONE_KEY}은 이거다." + firstValue, result);


    }

}