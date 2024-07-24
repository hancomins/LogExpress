package com.hancomins.LogExpress;

import com.hancomins.LogExpress.configuration.Configuration;

public class AutoShutdownTest {

    public static void main(String[] arg) {
        Configuration configuration = LogExpress.cloneConfiguration();
        configuration.setAutoShutdown(true);



        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                LogExpress.info("Test");
            }
        };



    }

}
