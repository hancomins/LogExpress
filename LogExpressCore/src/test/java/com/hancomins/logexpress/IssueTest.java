package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.Configuration;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IssueTest {

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static String LOG_FILE_PATH = isWindows() ? "C:\\temp\\logexpress.log" : "/tmp/logexpress.log";

    @Test
    public void updateConfigTest() throws InterruptedException, FileNotFoundException {
        File file = new File(LOG_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        LogExpress.reset();


        Logger LOG = LogExpress.newLogger("com.test", "com.test");

        LOG.info("info");
        LOG.debug("debug");
        LOG.trace("trace");

        Configuration configuration = LogExpress.cloneConfiguration();
        configuration.getDefaultWriterOption().setFile(LOG_FILE_PATH);

        configuration.setDefaultLevel(Level.INFO);
        LogExpress.updateConfig(configuration);

        Thread.sleep(500);

        LOG.info("info");
        LOG.debug("debug");
        LOG.trace("trace");

        LogExpress.shutdown().await();

        String[] logLines = getLogLines();
        for(String line : logLines) {
            System.out.println(line);
            assertTrue(line.contains("INFO"));
            assertFalse(line.contains("DEBUG"));
            assertFalse(line.contains("TRACE"));
        }



    }

    String[] getLogLines() throws FileNotFoundException {
        File file = new File(LOG_FILE_PATH);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        ArrayList<String> lines = new ArrayList<String>();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            fr.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.toArray(new String[0]);
    }

}
