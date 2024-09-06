package com.hancomins.logexpress;


import com.hancomins.logexpress.configuration.Configuration;

import java.io.File;


public class TestMain {

    public static void main(String[] args) throws InterruptedException {

        Logger LOG = LogExpress.newLogger("com.test", "com.test");


        Configuration configuration = LogExpress.cloneConfiguration();
        configuration.setDefaultLevel(Level.INFO);
        LogExpress.updateConfig(configuration);

        Thread.sleep(500);

        LOG.info("info");
        LOG.debug("debug");
        LOG.trace("trace");

        LogExpress.shutdown().await();



        if(1 <2 ) return;


        //Configuration configuration = logexpress.cloneConfiguration();
        // Get test.log path of Windows temp directory.
        String logPath = System.getProperty("java.io.tmpdir") + "test.log";
        //deleteDirectory(new File(logPath + File.separator + "project"));
        File file = new File(logPath);
        if (file.exists()) {
            file.delete();
        }
        int testCase = 10000000;
/*        WriterOption writerOption = configuration.getDefaultOption();
        configuration.setQueueSize(128000);
        writerOption.clearWriterType();
        writerOption.setStackTraceDepth(1);
        writerOption.addWriterType(WriterType.File);
        //writerOption.setFile(logPath);
        logexpress.updateConfig(configuration);*/
        Logger logger  =  LogExpress.newLogger(TestMain.class);
        long start = System.currentTimeMillis();
        for(int i = 0; i < testCase; i++) {
            logger.debug(i + "번째 라인 기록 완료!");
            if(i % 1000000 == 0) {
                System.out.println(i + "번째 라인 기록 완료!");
            }
        }
        System.out.println("라인 기록 완료. : " + (System.currentTimeMillis() - start) + "ms");

        //ShutdownFuture shutdownFuture = logexpress.shutdown();
        //shutdownFuture.await();


        file.delete();

    }

    // File 의 객체를 인자로 받아서 하위 디렉토리까지 모두 제거하는 메서드. java6 기반이고, 재귀 호출을 사용,.
    public static void deleteDirectory(File file) {
        if(file.exists()) {
            if(file.isDirectory()) {
                File[] files = file.listFiles();
                for(int i = 0; i < files.length; i++) {
                    deleteDirectory(files[i]);
                }
            }
            file.delete();
        }
    }

}
