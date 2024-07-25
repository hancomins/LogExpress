package com.hancomins.LogExpress;

import com.hancomins.LogExpress.configuration.Configuration;

import java.util.Set;

public class AutoShutdownTest {

    public static void main(String[] arg) {

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread : threadSet) {

            System.out.println(thread.getName() +  "  " +thread.getId() + "  " + thread.getThreadGroup().getName());
        }

        Configuration configuration = LogExpress.cloneConfiguration();
        configuration.setAutoShutdown(true);


        LogExpress.updateConfig(configuration);




        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                for (Thread thread : threadSet) {
                    System.out.println(thread.getName() +  "  " +thread.getId() + "  " + thread.getThreadGroup().getName());
                }
                LogExpress.info("Test");

                for(int i = 0; i < 10000; i++) {
                    System.out.println(i);
                    LogExpress.info("closed" + i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }




            }
        };


        thread.setDaemon(false);
        thread.start();



        System.out.println("end");


        return;
    }

}
