package com.clipsoft.LogExpress;

import com.clipsoft.LogExpress.util.StandardCharsets;
import com.clipsoft.LogExpress.writer.CurrentTimeMillisGetter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class InLogger {



    private final static String KEY_DIR = "LogExpress.inLogPath";
    private final static String FILE_NAME = "LogExpress";
    private final static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat TimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private final static ReentrantLock lock = new ReentrantLock();
    private final static int LOCK_TIMEOUT = 5000;

    private static boolean enabled = false;
    private static boolean isWriteFile = false;
    private static boolean isWriteConsole = false;
    private static final Object SetMonitor = new Object();



    static  {
        InLogger.reset();
    }


    private InLogger() {}

    public static void enable() {
        synchronized (SetMonitor) {
            System.setProperty("LogExpress.debug", "true");
            enabled = true;
        }
    }

    public static void enableFile(boolean enable) {
        synchronized (SetMonitor) {
            System.setProperty("LogExpress.debug.file",enabled ?  "true" : "false");
            isWriteFile = enable;
        }
    }

    public static void enableConsole(boolean enabled) {
        synchronized (SetMonitor) {
            System.setProperty("LogExpress.debug.console", enabled ?  "true" : "false");
            isWriteConsole = enabled;
        }
    }


    public static void disable() {
        synchronized (SetMonitor) {
            System.setProperty("LogExpress.debug", "false");
            enabled = false;
        }
    }

    public static void reset() {
        enabled = "true".equalsIgnoreCase((System.getProperty("LogExpress.debug", "false") + "").trim() );
    }

    public static boolean isEnabled() {
        return enabled;
    }


    public static boolean isFileEnabled() {
        return enabled;
    }

    public static boolean isConsoleEnabled() {
        return enabled;
    }


    private static File getInLogDir() {
        String path = System.getProperty(KEY_DIR);
        File inLogDir = new File("." + File.separator);
        if(path != null) {
            inLogDir = new File(path);
            try {
                inLogDir.mkdirs();
            } catch (Exception ignored) {}
            if(!inLogDir.isDirectory()) {
                inLogDir = new File("." + File.separator);
            }
        }
        return inLogDir;
    }

    public static void ERROR(String message, Throwable e)  {
        writeLog( new StringBuilder("[LogExpress::ERROR] ").append(TimeFormat.format(new Date(CurrentTimeMillisGetter.currentTimeMillis()))).append(' ').append(message).toString(), e, true);
    }

    public static void ERROR(String message)  {
        writeLog( new StringBuilder("[LogExpress::ERROR] ").append(TimeFormat.format(new Date(CurrentTimeMillisGetter.currentTimeMillis()))).append(' ').append(message).toString(), null, true);
    }

    public static void INFO(String message)  {
        if(!enabled) return;
        writeLog(new StringBuilder("[LogExpress::INFO] ").append(TimeFormat.format(new Date(CurrentTimeMillisGetter.currentTimeMillis()))).append(' ').append(message).toString(), null, false);
    }

    public static void WARN(String message)  {
        if(!enabled) return;
        writeLog(new StringBuilder("[LogExpress::WARN] ").append(TimeFormat.format(new Date(CurrentTimeMillisGetter.currentTimeMillis()))).append(' ').append(message).toString(), null, false);
    }

    public static void DEBUG(String message)  {
        if(!enabled) return;
        writeLog(new StringBuilder("[LogExpress::DEBUG] ").append(TimeFormat.format(new Date(CurrentTimeMillisGetter.currentTimeMillis()))).append(' ').append(message).toString(), null, false);
    }

    private static void writeLog(String message, Throwable e, boolean isError)  {
        if(!enabled && !isError) return;
        try {
            lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}

        if(isWriteFile) {
            writeLineInFile(message);
        }
        if(isWriteConsole) {
            System.out.println(message);
        }

        if(e != null) {
            e.printStackTrace();
            if(isWriteFile) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream(baos, true);
                e.printStackTrace(printStream);
                writeLineInFile(baos.toString());
            }
        }
        lock.unlock();
    }

    private static void writeLineInFile(String message) {
        File file = new File(getInLogDir(),FILE_NAME + "." + DateFormat.format(new Date()) + ".txt");
        FileOutputStream fos = null;
        try {
            if(!file.isFile()) {
                try {
                    file.createNewFile();
                } catch (IOException ignored2) {}
            }
            fos = new FileOutputStream(file,true);
            PrintWriter printWriter = new PrintWriter(fos);
            printWriter.println(message);
            printWriter.flush();
            try {
                fos.close();
                printWriter.close();
            } catch (IOException ignored) {}
        } catch (FileNotFoundException ignored) {}
    }

}
