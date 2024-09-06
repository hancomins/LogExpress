package com.hancomins.logexpress.writer;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ConsoleWriter {

	private final int mBufferSize;
	private Buffer mBuffer;

	private byte[] mRawBuffer;
	
	ConsoleWriter(int bufferSize) {

		mBufferSize = bufferSize;
		if(bufferSize < 0) return;
		mRawBuffer = new byte[bufferSize];
		mBuffer = ByteBuffer.wrap(mRawBuffer);
	}
	
	void write(byte[] data) {
		if(mBuffer == null) {
			System.out.write(data, 0, data.length);
			return;
		}
				
		int leftLen = data.length;
		int dataPos = 0;
		int bufferPos = mBuffer.position();
		int readLen = Math.min(mBufferSize - bufferPos, data.length);// - bufferPos;
		
		do {
			((ByteBuffer)mBuffer).put(data, dataPos, readLen);
			dataPos += readLen;
			leftLen -= readLen;
			if(mBuffer.position() == mBufferSize) {
				Buffer buffer = mBuffer;
				mBuffer.flip();
				System.out.write(mRawBuffer, 0, mRawBuffer.length);
				mBuffer.clear();
				bufferPos = 0;
				readLen = Math.min(mBufferSize,leftLen);// - bufferPos;
			}
			
		} while(leftLen > 0);
	}
	
	
	
	
	void flush() {
		if(mBuffer != null && mBuffer.position() > 0) {
			mBuffer.flip();
			System.out.write(mRawBuffer, 0, mBuffer.limit());
			mBuffer.clear();
		}
	}
}
