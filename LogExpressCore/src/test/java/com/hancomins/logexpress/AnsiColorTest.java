package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.ColorOption;
import com.hancomins.logexpress.configuration.Configuration;
import com.hancomins.logexpress.configuration.WriterOption;


import java.io.File;
import java.io.FileNotFoundException;

public class AnsiColorTest {

    static final boolean isWindows = System.getProperty("os.name")
            .toLowerCase().startsWith("windows");

    public static void main(String[] args) throws FileNotFoundException {

        String file = isWindows ?  "C:\\Temp\\test.txt" : "/tmp/test.txt";
        File logFile = new File(file);
        if(logFile.exists()) {
            logFile.delete();
        }


        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption writerOption = configuration.getDefaultWriterOption();
        //configuration.newWriterOption(
        writerOption.setFile(file);
        ColorOption colorOption = writerOption.colorOption();
        colorOption.enableConsole(true).putColorCode("info","message", "WHITE;GREEN");
        colorOption.enableConsole(true).putColorCode("info","time", "CYAN");
        colorOption.enableConsole(true).putColorCode("info","level", "BLACK;GREEN");

        LogExpress.updateConfig(configuration);


        LogExpress.info("Test");


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        try {
            LogExpress.shutdown().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
