package com.hancomins.logexpress.writer;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@SuppressWarnings("ALL")
public class FileNamePatternTest extends TestCase {

    static Random random = new Random(System.currentTimeMillis());
    static Object randomLock = new Object();

    private static int randomInt() {
        synchronized (randomLock) {
            return random.nextInt();
        }
    }

    @Test
    public void testFileNamePattern() throws IOException {
        String strFileNamePattern;
        long current = System.currentTimeMillis();
        int num = Math.abs(randomInt());
        int pid=  Math.abs(randomInt());
        FileNamePattern fileNamePattern = FileNamePattern.parse("/Program Files (x86)/hancomins/CLIP eForm v5.0/CLIPreportAPIServer-previewer/log/{hostname}/{pid}.{hostname}.api.{date:yyyy-MM-dd}_{number}.log");
        File file = fileNamePattern.toFile(pid, "test-name", "test-makrer", current, num);
        System.out.println(file.getAbsolutePath());
        String name = file.getName();
        assertEquals(name, pid +  ".test-name.api." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "_" + num + ".log");

        fileNamePattern = FileNamePattern.parse("/Program Files (x86)/hancomins/CLIP eForm v5.0/CLIPreportAPIServer-previewer/log/{hostname}/{pid}.{hostname}.api.{date}_{number}.log");
        file = fileNamePattern.toFile(pid, "test-name", "test-makrer", current, num);
        System.out.println(file.getAbsolutePath());
        name = file.getName();
        assertEquals(name, pid +  ".test-name.api." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "_" + num + ".log");

        assertEquals(file.getParentFile().getName(), "test-name");




        file.delete();

    }
}