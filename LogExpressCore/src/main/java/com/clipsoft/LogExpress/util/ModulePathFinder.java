package com.clipsoft.LogExpress.util;

import java.io.File;
import java.net.URLDecoder;

public class ModulePathFinder {

    public static final String PROPERTY_NAME_DEVELOPMENT_DIR = "clipboot.dir.root";


    public static void setRootDir(String dir) {
        System.setProperty(PROPERTY_NAME_DEVELOPMENT_DIR, dir);
    }



    public static File find(Class<?> clazz) {
        String devMainDir = System.getProperty(PROPERTY_NAME_DEVELOPMENT_DIR);
        if(System.getProperty(PROPERTY_NAME_DEVELOPMENT_DIR) != null) {
            File file = new File(devMainDir);
            return file;
        }

        String filePath = URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());

        int jarIdx =  filePath.lastIndexOf(".jar!/");
        // 만약 윈도우에서 Sun 외의에 다른 벤더가 제작한 JAVA 1.4 에서 실행할 경우 기본 separator 인 \ 가 아닌 / 로 반환이 된다. 이 문제를 해결.
        if(jarIdx < 0) jarIdx =  filePath.lastIndexOf(".jar!\\");
        if(jarIdx > 0) {
            jarIdx = filePath.lastIndexOf("/", jarIdx);
            if(jarIdx < 0) jarIdx =  filePath.lastIndexOf("\\", jarIdx);
            if(jarIdx > 0) filePath = filePath.substring(0,jarIdx).replaceAll("file[':']", "");
        }

        File objFile = new File(filePath);
        if (objFile.isFile()) {
            if (objFile.getName().matches("(.*[.]jar$)|(.*[.]was$)")) {
                return objFile.getParentFile();
            } else if (objFile.getName().matches(".*[.]class$")) {
                String classFilePath = clazz.getName().replaceAll("['.']{1,}", File.separator).replaceAll("['.']{1,}", "/") + ".class";
                filePath = filePath.replaceAll(classFilePath, "").replaceAll("['$'].+$", "");
                return new File(filePath);
            }
        } else if (objFile.isDirectory()) {
            return objFile;
        }
        return objFile.getAbsoluteFile();
    }
}
