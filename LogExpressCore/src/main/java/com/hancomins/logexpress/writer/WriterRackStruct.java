package com.hancomins.logexpress.writer;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.TimeZone;

final class WriterRackStruct {

	private final static long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

	final String marker;
	final int fileMaxSize;
	final int fileBufferSize;
	final int history;
	long today;
	long tomorrow;
	Charset charset;
	FileNamePattern fileNamePattern;
	
	boolean isWriteConsole = false;
	FileWriter fileWriter;


	@Override
	public String toString() {
		return "WriterRackStruct [marker=" + marker + ", fileMaxSize=" + fileMaxSize + ", fileBufferSize="
				+ fileBufferSize + ", history=" + history + ", today=" + today + ", tomorrow=" + tomorrow + ", charset="
				+ charset + ", fileNamePattern=" + fileNamePattern + ", isWriteConsole=" + isWriteConsole + ", fileWriter="
				+ (fileWriter != null ? fileWriter.toString() : "null")  + "]";
	}

	
	
	WriterRackStruct(String marker,String encoding, int fileMaxSize, int fileBufferSize, int history) {
		
		if(encoding != null && !encoding.isEmpty()) {
			try {
				this.charset = Charset.forName(encoding);
			} catch (UnsupportedCharsetException e) {
				this.charset = Charset.defaultCharset();
			}
		} else {
			this.charset = Charset.defaultCharset();
		}
		this.marker = marker;
		this.fileBufferSize = fileBufferSize;
		this.fileMaxSize = fileMaxSize;
		this.history = history;
		newDate();
	}
	
	void newDate() {
		long current = CurrentTimeMillisGetter.currentTimeMillis();
		this.tomorrow = addDate(current, 1);
		this.today = addDate(current,0);
	}







	// 현재 시간에 대한 TimeZone의 offset을 계산합니다.
	private static long addDate(final long current, int add) {
		// 현재 시간을 밀리초 단위로 가져옵니다.
        TimeZone timeZone = TimeZone.getDefault();
		int offset = timeZone.getOffset(current);
		// 자정 시간의 타임스탬프를 구하기 위해 현재 시간에서 시, 분, 초, 밀리초를 제거합니다.
		long midnightTimeMillis = (current + offset) / ONE_DAY_MILLIS * ONE_DAY_MILLIS - offset;
		// 추가할 일수를 더합니다.
        return midnightTimeMillis + (long)add * ONE_DAY_MILLIS;
	}

	
	
	void end() {
		fileNamePattern = null;
		if(fileWriter != null) {
			fileWriter.end();
		}
		fileWriter = null;
	}



}
