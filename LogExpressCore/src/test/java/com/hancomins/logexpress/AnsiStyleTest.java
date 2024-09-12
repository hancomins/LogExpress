package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.StyleOption;
import com.hancomins.logexpress.configuration.Configuration;
import com.hancomins.logexpress.configuration.WriterOption;
import org.junit.Test;


import java.io.*;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class AnsiStyleTest {

    static final boolean isWindows = System.getProperty("os.name")
            .toLowerCase().startsWith("windows");

    @Test
    public void testColorConsoleColorFile() throws IOException {

        String file = isWindows ?  "C:\\Temp\\testWriteColor.txt" : "/tmp/testWriteColor.txt";
        File logFile = new File(file);
        if(logFile.exists()) {
            logFile.delete();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pw = new PrintStream(baos);
        PrintStream origin = System.out;
        System.setOut(pw);


        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption writerOption = configuration.getDefaultWriterOption();
        writerOption.setFile(file);
        @SuppressWarnings("UnusedAssignment")
        StyleOption styleOption = writerOption.styleOption();
        styleOption = configuration.defaultStyleOption();

        String currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "";
        if(currentHour.length() == 1) {
            currentHour = "0" + currentHour;
        }
        writerOption.setLinePattern("{time:HH} [{level}] {message}");

        styleOption.enableConsole(true).enableFile(true);

        styleOption.setStyle("info","message", "green");
        styleOption.setStyle("all","time", "CYAN");
        styleOption.setStyle("info","level", "BLACK;GREEN");

        styleOption.setStyle("error","message", "red");
        styleOption.setStyle("error","level", "BLACK;red");

        styleOption.setStyle("warn","message", "yellow");
        styleOption.setStyle("warn","level", "BLACK;yellow");




        LogExpress.updateConfig(configuration);

        Logger LOG = LogExpress.newLogger(AnsiStyleTest.class);
        LOG.info("INFO MESSAGE");
        LOG.error("ERROR MESSAGE");
        LOG.warn("WARN MESSAGE");


        try {
            LogExpress.shutdown().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        baos.flush();
        System.setOut(origin);
        String result = baos.toString();
        String[] results = result.split("\n");
        System.out.println(results[0]);
        assertEquals(results[0], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mINFO MESSAGE\u001B[0m");
        assertEquals(results[1], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;41mERROR\u001B[0m] \u001B[31mERROR MESSAGE\u001B[0m");
        assertEquals(results[2], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;43mWARN\u001B[0m] \u001B[33mWARN MESSAGE\u001B[0m");
        assertEquals(results[3], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mLOGExpress shutdown called.\u001B[0m");
        System.out.println(result);

        String[] fileResults = readFileToArray(file);
        assertEquals(fileResults[0], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mINFO MESSAGE\u001B[0m");
        assertEquals(fileResults[1], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;41mERROR\u001B[0m] \u001B[31mERROR MESSAGE\u001B[0m");
        assertEquals(fileResults[2], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;43mWARN\u001B[0m] \u001B[33mWARN MESSAGE\u001B[0m");
        assertEquals(fileResults[3], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mLOGExpress shutdown called.\u001B[0m");





    }

    @Test
    public void testColorConsoleColorFileDefaultAllStyle() throws IOException {

        String file = isWindows ?  "C:\\Temp\\testWriteColor.txt" : "/tmp/testWriteColor.txt";
        File logFile = new File(file);
        if(logFile.exists()) {
            logFile.delete();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pw = new PrintStream(baos);
        PrintStream origin = System.out;
        System.setOut(pw);


        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption writerOption = configuration.getDefaultWriterOption();
        writerOption.setFile(file);
        @SuppressWarnings("UnusedAssignment")
        StyleOption styleOption = writerOption.styleOption();
        styleOption = configuration.defaultStyleOption();

        String currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "";
        if(currentHour.length() == 1) {
            currentHour = "0" + currentHour;
        }
        writerOption.setLinePattern("{time:HH} ITALIC [{level}] {message}");

        styleOption.enableConsole(true).enableFile(true);

        styleOption.setStyle("all","all", "+ITALIC;UNDERLINE;");

        styleOption.setStyle("info","message", "+STRIKE");
        styleOption.setStyle("all","time", "CYAN");
        styleOption.setStyle("info","level", "BLACK;GREEN");

        styleOption.setStyle("error","message", "red");
        styleOption.setStyle("error","level", "BLACK;red");

        styleOption.setStyle("warn","message", "yellow");
        styleOption.setStyle("warn","level", "BLACK;yellow");




        LogExpress.updateConfig(configuration);

        Logger LOG = LogExpress.newLogger(AnsiStyleTest.class);
        LOG.info("INFO MESSAGE");
        LOG.error("ERROR MESSAGE");
        LOG.warn("WARN MESSAGE");


        try {
            LogExpress.shutdown().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        baos.flush();
        System.setOut(origin);
        String result = baos.toString();
        String[] results = result.split("\n");

        System.out.println(result);
        //assertEquals(results[0], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mINFO MESSAGE\u001B[0m");
        assertEquals(results[0], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;42mINFO\u001B[0m\u001B[3;4m] \u001B[0m\u001B[32;3;4;9mINFO MESSAGE\u001B[0m");
        assertEquals(results[1], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;41mERROR\u001B[0m\u001B[3;4m] \u001B[0m\u001B[31mERROR MESSAGE\u001B[0m");
        assertEquals(results[2], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;43mWARN\u001B[0m\u001B[3;4m] \u001B[0m\u001B[33mWARN MESSAGE\u001B[0m");
        assertEquals(results[3], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;42mINFO\u001B[0m\u001B[3;4m] \u001B[0m\u001B[32;3;4;9mLOGExpress shutdown called.\u001B[0m");


        String[] fileResults = readFileToArray(file);
        assertEquals(fileResults[0], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;42mINFO\u001B[0m\u001B[3;4m] \u001B[0m\u001B[32;3;4;9mINFO MESSAGE\u001B[0m");
        assertEquals(fileResults[1], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;41mERROR\u001B[0m\u001B[3;4m] \u001B[0m\u001B[31mERROR MESSAGE\u001B[0m");
        assertEquals(fileResults[2], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;43mWARN\u001B[0m\u001B[3;4m] \u001B[0m\u001B[33mWARN MESSAGE\u001B[0m");
        assertEquals(fileResults[3], "\u001B[36m" + currentHour + "\u001B[0m\u001B[3;4m ITALIC [\u001B[0m\u001B[30;42mINFO\u001B[0m\u001B[3;4m] \u001B[0m\u001B[32;3;4;9mLOGExpress shutdown called.\u001B[0m");





    }

    @Test
    public void testColorConsoleNoneColorFile() throws IOException {


        String file = isWindows ?  "C:\\Temp\\testWriteColor.txt" : "/tmp/testWriteColor.txt";
        File logFile = new File(file);
        if(logFile.exists()) {
            logFile.delete();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pw = new PrintStream(baos);
        PrintStream origin = System.out;
        System.setOut(pw);


        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption writerOption = configuration.getDefaultWriterOption();
        writerOption.setFile(file);
        StyleOption styleOption = writerOption.styleOption();


        String currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "";
        if(currentHour.length() == 1) {
            currentHour = "0" + currentHour;
        }
        writerOption.setLinePattern("{time:HH} [{level}] {message}");

        styleOption.enableConsole(false).enableFile(true);

        styleOption.setStyle("info","message", "green");
        styleOption.setStyle("all","time", "CYAN");
        styleOption.setStyle("info","level", "BLACK;GREEN");

        styleOption.setStyle("error","message", "red");
        styleOption.setStyle("error","level", "BLACK;red");

        styleOption.setStyle("warn","message", "yellow");
        styleOption.setStyle("warn","level", "BLACK;yellow");




        LogExpress.updateConfig(configuration);

        Logger LOG = LogExpress.newLogger(AnsiStyleTest.class);
        LOG.info("INFO MESSAGE");
        LOG.error("ERROR MESSAGE");
        LOG.warn("WARN MESSAGE");

        try {
            LogExpress.shutdown().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        baos.flush();

        System.setOut(origin);
        String result = baos.toString();
        String[] results = result.split("\n");
        System.out.println(results[0]);
        assertEquals(results[0], currentHour + " [INFO] INFO MESSAGE");
        assertEquals(results[1], currentHour + " [ERROR] ERROR MESSAGE");
        assertEquals(results[2], currentHour + " [WARN] WARN MESSAGE");
        assertEquals(results[3], currentHour + " [INFO] LOGExpress shutdown called.");
        System.out.println(result);

        results = readFileToArray(file);

        System.out.println(results[0]);
        assertEquals(results[0], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mINFO MESSAGE\u001B[0m");
        assertEquals(results[1], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;41mERROR\u001B[0m] \u001B[31mERROR MESSAGE\u001B[0m");
        assertEquals(results[2], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;43mWARN\u001B[0m] \u001B[33mWARN MESSAGE\u001B[0m");
        assertEquals(results[3], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mLOGExpress shutdown called.\u001B[0m");

        System.out.println(result);
        for(String s : results) {
            System.out.println(s);
        }
    }

    @Test
    public void testNoneColorConsoleNoneColorFile() throws IOException {


        String file = isWindows ?  "C:\\Temp\\testWriteColor.txt" : "/tmp/testWriteColor.txt";
        File logFile = new File(file);
        if(logFile.exists()) {
            logFile.delete();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pw = new PrintStream(baos);
        PrintStream origin = System.out;
        System.setOut(pw);


        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption writerOption = configuration.getDefaultWriterOption();
        writerOption.setFile(file);
        StyleOption styleOption = writerOption.styleOption();


        String currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "";
        if(currentHour.length() == 1) {
            currentHour = "0" + currentHour;
        }
        writerOption.setLinePattern("{time:HH} [{level}] {message}");

        styleOption.enableConsole(false).enableFile(false);

        styleOption.setStyle("info","message", "green");
        styleOption.setStyle("all","time", "CYAN");
        styleOption.setStyle("info","level", "BLACK;GREEN");

        styleOption.setStyle("error","message", "red");
        styleOption.setStyle("error","level", "BLACK;red");

        styleOption.setStyle("warn","message", "yellow");
        styleOption.setStyle("warn","level", "BLACK;yellow");




        LogExpress.updateConfig(configuration);

        Logger LOG = LogExpress.newLogger(AnsiStyleTest.class);
        LOG.info("INFO MESSAGE");
        LOG.error("ERROR MESSAGE");
        LOG.warn("WARN MESSAGE");

        try {
            LogExpress.shutdown().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        baos.flush();

        System.setOut(origin);
        String result = baos.toString();
        String[] results = result.split("\n");
        System.out.println(results[0]);
        assertEquals(results[0], currentHour + " [INFO] INFO MESSAGE");
        assertEquals(results[1], currentHour + " [ERROR] ERROR MESSAGE");
        assertEquals(results[2], currentHour + " [WARN] WARN MESSAGE");
        assertEquals(results[3], currentHour + " [INFO] LOGExpress shutdown called.");
        System.out.println(result);

        results = readFileToArray(file);

        System.out.println(results[0]);
        assertEquals(results[0], currentHour + " [INFO] INFO MESSAGE");
        assertEquals(results[1], currentHour + " [ERROR] ERROR MESSAGE");
        assertEquals(results[2], currentHour + " [WARN] WARN MESSAGE");
        assertEquals(results[3], currentHour + " [INFO] LOGExpress shutdown called.");

        System.out.println(result);
        for(String s : results) {
            System.out.println(s);
        }
    }

    @Test
    public void testNoneColorConsoleColorFile() throws IOException {

        String file = isWindows ?  "C:\\Temp\\testWriteColor.txt" : "/tmp/testWriteColor.txt";
        File logFile = new File(file);
        if(logFile.exists()) {
            logFile.delete();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pw = new PrintStream(baos);
        PrintStream origin = System.out;
        System.setOut(pw);


        Configuration configuration = LogExpress.cloneConfiguration();
        WriterOption writerOption = configuration.getDefaultWriterOption();
        writerOption.setFile(file);
        StyleOption styleOption = writerOption.styleOption();


        String currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "";
        if(currentHour.length() == 1) {
            currentHour = "0" + currentHour;
        }
        writerOption.setLinePattern("{time:HH} [{level}] {message}");

        styleOption.enableConsole(true).enableFile(false);

        styleOption.setStyle("info","message", "green");
        styleOption.setStyle("all","time", "CYAN");
        styleOption.setStyle("info","level", "BLACK;GREEN");

        styleOption.setStyle("error","message", "red");
        styleOption.setStyle("error","level", "BLACK;red");

        styleOption.setStyle("warn","message", "yellow");
        styleOption.setStyle("warn","level", "BLACK;yellow");




        LogExpress.updateConfig(configuration);

        Logger LOG = LogExpress.newLogger(AnsiStyleTest.class);
        LOG.info("INFO MESSAGE");
        LOG.error("ERROR MESSAGE");
        LOG.warn("WARN MESSAGE");

        try {
            LogExpress.shutdown().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        baos.flush();

        System.setOut(origin);
        String result = baos.toString();
        String[] results = result.split("\n");
        System.out.println(results[0]);
        assertEquals(results[0], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mINFO MESSAGE\u001B[0m");
        assertEquals(results[1], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;41mERROR\u001B[0m] \u001B[31mERROR MESSAGE\u001B[0m");
        assertEquals(results[2], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;43mWARN\u001B[0m] \u001B[33mWARN MESSAGE\u001B[0m");
        assertEquals(results[3], "\u001B[36m" + currentHour + "\u001B[0m" + " [\u001B[30;42mINFO\u001B[0m] \u001B[32mLOGExpress shutdown called.\u001B[0m");
        System.out.println(result);

        String[] fileResults = readFileToArray(file);

        System.out.println(results[0]);
        assertEquals(fileResults[0], currentHour + " [INFO] INFO MESSAGE");
        assertEquals(fileResults[1], currentHour + " [ERROR] ERROR MESSAGE");
        assertEquals(fileResults[2], currentHour + " [WARN] WARN MESSAGE");
        assertEquals(fileResults[3], currentHour + " [INFO] LOGExpress shutdown called.");
        System.out.println(result);
    }

    private static String[] readFileToArray(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder sb = new StringBuilder();
        while((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString().split("\n");
    }
}
