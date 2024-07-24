package com.hancomins.LogExpress.writer;

import java.io.File;
import java.io.IOException;

public interface IFileWriter {

    /**
     * 이 FileWriter의 참조 카운터를 증가시킵니다.
     *
     * @return 이 FileWriter 인스턴스
     */
    IFileWriter addReference();

    /**
     * 이 FileWriter와 연결된 파일을 반환합니다.
     *
     * @return 이 FileWriter와 연결된 파일
     */
    File getFile();

    /**
     * 지정된 데이터를 파일에 씁니다.
     *
     * @param data 쓸 데이터
     * @throws IOException 입출력 예외 발생 시
     */
    void write(byte[] data) throws IOException;

    /**
     * 파일 크기가 최대 파일 크기를 초과하는지 확인합니다.
     *
     * @return 파일 크기가 최대 파일 크기를 초과하면 true, 그렇지 않으면 false
     */
    boolean isOverSize();

    /**
     * 모든 참조 카운터를 무효화 시키고 FileWriter를 완전히 닫습니다.
     */
    void close();

    /**
     * 버퍼를 플러시하고 남은 데이터를 파일에 씁니다.
     *
     * @throws IOException 입출력 예외 발생 시
     */
    void flush() throws IOException;

    /**
     * 파일이 존재하는지 확인하고 존재하지 않으면 스트림을 재초기화합니다.
     *
     * @throws IOException 입출력 예외 발생 시
     */
    void ensureFileExists() throws IOException;

    /**
     * 이 FileWriter가 닫혔는지 확인합니다.
     *
     * @return 이 FileWriter가 닫혔으면 true, 그렇지 않으면 false
     */
    boolean isClosed();

    /**
     * FileWriter를 종료합니다.
     * 만약 참조 카운터가 1보다 크면 참조 카운터만 감소시킵니다.
     */
    void end();
}