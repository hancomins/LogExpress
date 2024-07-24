package com.hancomins.LogExpress.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Files {

    public static void write(File file, byte[] buffer) throws IOException {
        if(file.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
        fos.write(buffer, 0, buffer.length);
        fos.close();
    }

    public static byte[] readAllBytes(File file) throws IOException {
        if (!file.isFile()) throw new FileNotFoundException();
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        int count = 0;
        for(int i = 0; i < buffer.length; i += count) {
            count = fis.read(buffer, i, buffer.length - i);
            if(count < 1) break;
        }
        fis.close();
        return buffer;
    }


    public static String getNameWithoutExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if(index < 0) return name;
        return name.substring(0, index);
    }

    public static String getExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if(index < 0) return "";
        return name.substring(index + 1);
    }

    // OS System Environment load method.
    /*public static String getEnv(String key) {
        return System.getenv(key);
    }*/




}
