package com.hancomins.LogExpress.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class FileWriter {


	private final static int MIN_BUFFER_SIZE = 32;
	
	private final long maxFileSize;
	private final int bufferSize;
	private long currentFileSize;
	private File file;
	private boolean closed = false;

	private FileOutputStream outputStream;
	private FileChannel channel;
	private Buffer buffer;
	// 참조 카운터
	private int refCount;

	private static final AtomicInteger OPEN_FILE_COUNT = new AtomicInteger(0);
	
	FileWriter(File file,int bufferSize, int maxSize) throws IOException {
		if(bufferSize < MIN_BUFFER_SIZE) bufferSize = MIN_BUFFER_SIZE;
		this.file = file;
		this.bufferSize = bufferSize;
		buffer = ByteBuffer.allocateDirect(bufferSize);
		maxFileSize = (long) maxSize * 1024 * 1024;
		initStream(file);
		refCount = 1;
	}

	FileWriter increaseRefCount() {
		refCount++;
		return this;
	}


	public static int getOpenFileCount() {
		return OPEN_FILE_COUNT.get();
	}
	
	private void initStream(File file) throws IOException {
		outputStream = new FileOutputStream(file, true);
		channel = outputStream.getChannel();
		OPEN_FILE_COUNT.incrementAndGet();
	}
	
	
	public File getFile() {
		return file;
	}
	
	
	void write(byte[] data) {
		if(isClosed()) return;
		int leftLen = data.length;
		int dataPos = 0;
		int bufferPos = buffer.position();
		int readLen = Math.min(bufferSize - bufferPos, data.length);// - bufferPos;
		
		do {
			((ByteBuffer) buffer).put(data, dataPos, readLen);
			dataPos += readLen;
			leftLen -= readLen;
			if(buffer.position() == bufferSize) {
				buffer.flip();
				writeFile(channel, (ByteBuffer) buffer);
				buffer.clear();
				bufferPos = 0;
				readLen = Math.min(bufferSize,leftLen);// - bufferPos;
			}
			
		} while(leftLen > 0);
	}
	

	boolean isOverSize() {
		return maxFileSize > 0 && maxFileSize <= currentFileSize;
	}
	
	
	private void writeFile(FileChannel fileChannel,ByteBuffer buffer) {
		if(isClosed()) return;
		try {
			currentFileSize += buffer.limit();
			while(buffer.hasRemaining()) {
                //noinspection ResultOfMethodCallIgnored
                fileChannel.write(buffer);
			}
		} catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
		}
	}
	void close() {
		refCount = 0;
		end();
	}
	
	void end() {
		if(closed) {
			return;
		}
		flush();
		if(refCount > 0) {
			refCount--;
			if(refCount > 0) {
				return;
			}
		}
		closed = true;
		buffer = null;
		try {
			channel.close();
		} catch (Exception ignored) {}
		try {
			outputStream.close();
		} catch (Exception ignored) {}
		channel = null;
		outputStream = null;
		OPEN_FILE_COUNT.decrementAndGet();
	}

	boolean isClosed() {
		return closed;
	}
	
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
    boolean checkExist() {
		if(isClosed()) return false;
		if(!file.exists()) {
			try {
				try {
					channel.close();
					OPEN_FILE_COUNT.decrementAndGet();
					outputStream.close();
				} catch (Exception ignored) {}
				file.createNewFile();
				initStream(file);
				currentFileSize = 0;
				buffer.flip();
				return true;
			} catch (Exception e1) {
                //noinspection CallToPrintStackTrace
                e1.printStackTrace();
			}
		}
		return false; 
	}
	
	
	void flush() {
		if(isClosed()) return;
		if(buffer.position() > 0) {
			buffer.flip();
			writeFile(channel,(ByteBuffer) buffer);
			buffer.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FileWriter) {
			if(file == null && ((FileWriter) obj).file == null) {
				return super.equals(obj);
			}
			return file != null && file.equals(((FileWriter) obj).file);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if(file == null) {
			return super.hashCode();
		}
		return file.hashCode();
	}

	@Override
	public String toString() {
		if(isClosed()) {
			return "FileWriter("  + Integer.toHexString(super.hashCode()) + ") [closed]";
		}

		return "FileWriter("  + Integer.toHexString(super.hashCode()) + ") [maxFileSize=" + maxFileSize + ", bufferSize=" + bufferSize + ", currentFileSize="
				+ currentFileSize + ", file=" + (file == null ? "null" : file.getAbsolutePath()) + "]";
	}
}
