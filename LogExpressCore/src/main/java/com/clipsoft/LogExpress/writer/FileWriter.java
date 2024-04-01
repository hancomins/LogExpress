package com.clipsoft.LogExpress.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileWriter {
	
	private final static int MIN_BUFFER_SIZE = 32;
	
	private final long mMaxFileSize;
	private final int mBufferSize;
	private long mCurrentFileSize;
	private File mFile;


	private FileOutputStream mOutputStream;
	private FileChannel mChannel;
	private Buffer mBuffer;


	
	
	FileWriter(File file,int bufferSize, int maxSize) throws IOException {
		if(bufferSize < MIN_BUFFER_SIZE) bufferSize = MIN_BUFFER_SIZE;
		mFile = file;
		mBufferSize = bufferSize;
		mBuffer = ByteBuffer.allocateDirect(bufferSize);
		mMaxFileSize = maxSize * 1024 * 1024;
		initStream(mFile);;
	}
	
	private void initStream(File file) throws IOException {
		mOutputStream = new FileOutputStream(file, true); 
		mChannel = mOutputStream.getChannel();
	}
	
	
	public File getFile() {
		return mFile;
	}
	
	
	void write(byte[] data) {
		int leftLen = data.length;
		int dataPos = 0;
		int bufferPos = mBuffer.position();
		int readLen = Math.min(mBufferSize - bufferPos, data.length);// - bufferPos;
		
		do {
			((ByteBuffer) mBuffer).put(data, dataPos, readLen);
			dataPos += readLen;
			leftLen -= readLen;
			if(mBuffer.position() == mBufferSize) {
				mBuffer.flip();
				writeFile(mChannel, (ByteBuffer) mBuffer);
				mBuffer.clear();
				bufferPos = 0;
				readLen = Math.min(mBufferSize,leftLen);// - bufferPos;
			}
			
		} while(leftLen > 0);
	}
	

	boolean isOverSize() {
		return mMaxFileSize > 0 && mMaxFileSize <= mCurrentFileSize;
	}
	
	
	private void writeFile(FileChannel fileChannel,ByteBuffer buffer) {
		try {
			mCurrentFileSize += buffer.limit();
			fileChannel.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void end() {
		flush();
		mBuffer = null;
		try {
			mChannel.close();
		} catch (Exception ignored) {}
		try {
			mOutputStream.close();
		} catch (Exception ignored) {}
		mChannel = null;
		mOutputStream = null;
		mFile = null;
		
	}
	
	
	boolean checkExist() {
		if(!mFile.exists()) {
			try {
				try {
					mChannel.close();
					mOutputStream.close();
				} catch (Exception ignored) {}
				mFile.createNewFile();
				initStream(mFile); 
				mCurrentFileSize = 0;
				mBuffer.flip();
				return true;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return false; 
	}
	
	
	void flush() {
		if(mBuffer.position() > 0) {
			mBuffer.flip();
			writeFile(mChannel,(ByteBuffer) mBuffer);
			mBuffer.clear();
		}
	}

	@Override
	public String toString() {
		return "FileWriter [mMaxFileSize=" + mMaxFileSize + ", mBufferSize=" + mBufferSize + ", mCurrentFileSize="
				+ mCurrentFileSize + ", mFile=" + (mFile == null ? "null" : mFile.getAbsolutePath()) + "]";
	}
}
