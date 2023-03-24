package com.clipsoft.LogExpress.writer;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Calendar;

final class WriterRackStruct {
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
	
	final void newDate() {
		this.tomorrow = getDate(1);
		this.today = getDate(0);
	}
	
	

	private final long getDate(int add) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(CurrentTimeMillisGetter.currentTimeMillis());
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, add);		
		return calendar.getTimeInMillis();
	}
	
	
	void end() {
		fileNamePattern = null;
		if(fileWriter != null) {
			fileWriter.end();
		}
		fileWriter = null;
	}
}
