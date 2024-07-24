package com.hancomins.LogExpress.writer;

import java.io.File;

class FileWriterDummy implements IFileWriter {

    private final File file;

    public FileWriterDummy(File file) {
        this.file = file;
    }

    @Override
    public IFileWriter addReference() {
        return this;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void write(byte[] data) {
    }

    @Override
    public boolean isOverSize() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void ensureFileExists() {
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void end() {

    }


    /**
     * 이 FileWriter가 다른 FileWriter와 같은지 확인합니다.
     * File 객체를 비교합니다.
     * @param obj 비교할 객체
     * @return 같으면 true, 그렇지 않으면 false
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if (obj instanceof IFileWriter) {
            File destFile = ((IFileWriter) obj).getFile();
            if (file == null && destFile == null) {
                return super.equals(obj);
            }
            return file != null && file.equals(destFile);
        }
        return false;
    }

    /**
     * 파일 객체의 해시 코드를 반환합니다.
     * @return 파일 객체의 해시 코드
     */
    @Override
    public int hashCode() {
        if (file == null) {
            return super.hashCode();
        }
        return file.hashCode();
    }

    @Override
    public String toString() {
        return "FileWriterDummy(" + Integer.toHexString(super.hashCode()) + ") [closed]";
    }

}
