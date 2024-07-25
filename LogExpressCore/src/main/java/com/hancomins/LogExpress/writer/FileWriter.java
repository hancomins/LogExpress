package com.hancomins.LogExpress.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 데이터를 파일에 버퍼링하고 크기 관리를 통해 쓰기 위한 클래스입니다.
 * @author beom
 */
public class FileWriter {

	/**
	 * 최소 버퍼 크기
	 */
	private final static int MIN_BUFFER_SIZE = 32;

	/**
	 * 최대 파일 크기
	 */
	private final long maxFileSize;

	/**
	 * 버퍼 크기
	 */
	private final int bufferSize;

	/**
	 * 현재 파일 크기
	 */
	private long currentFileSize;

	/**
	 * 파일 객체
	 */
	private final File file;

	/**
	 * 파일이 닫혔는지 여부
	 */
	private boolean closed = false;

	/**
	 * 파일 출력 스트림
	 */
	private FileOutputStream outputStream;

	/**
	 * 파일 채널
	 */
	private FileChannel channel;

	/**
	 * 버퍼 객체
	 */
	private Buffer buffer;

	/**
	 * 참조 카운터
	 */
	private int refCount;

	/**
	 * 열려 있는 파일 수
	 */
	private static final AtomicInteger OPEN_FILE_COUNT = new AtomicInteger(0);

	/**
	 * 지정된 파일, 버퍼 크기 및 최대 파일 크기로 FileWriter를 생성합니다.
	 *
	 * @param file 파일 객체
	 * @param bufferSize 버퍼 크기
	 * @param maxSize 최대 파일 크기 (메가바이트 단위)
	 * @throws IOException 입출력 예외 발생 시
	 */
	FileWriter(File file, int bufferSize, int maxSize) throws IOException {
		if (bufferSize < MIN_BUFFER_SIZE) bufferSize = MIN_BUFFER_SIZE;
		this.file = file;
		this.bufferSize = bufferSize;
		buffer = ByteBuffer.allocateDirect(bufferSize);
		maxFileSize = (long) maxSize * 1024 * 1024;
		initStream(file);
		refCount = 1;
	}

	/**
	 * 이 FileWriter의 참조 카운터를 증가시킵니다.
	 *
	 * @return 이 FileWriter 인스턴스
	 */
	public FileWriter addReference() {
		if(isClosed()) {
			return this;
		}
		refCount++;
		return this;
	}

	/**
	 * 열려 있는 FileWriter 인스턴스의 수를 반환합니다.
	 *
	 * @return 열려 있는 FileWriter 인스턴스의 수
	 */
	public static int getOpenFileCount() {
		return OPEN_FILE_COUNT.get();
	}

	/**
	 * 이 FileWriter와 연결된 파일을 반환합니다.
	 *
	 * @return 이 FileWriter와 연결된 파일
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 지정된 데이터를 파일에 씁니다.
	 *
	 * @param data 쓸 데이터
	 */
	public void write(byte[] data) throws IOException {
		if (isClosed()) return;
		int leftLen = data.length;
		int dataPos = 0;
		int bufferPos = buffer.position();
		int readLen = Math.min(bufferSize - bufferPos, data.length);

		do {
			((ByteBuffer) buffer).put(data, dataPos, readLen);
			dataPos += readLen;
			leftLen -= readLen;
			if (buffer.position() == bufferSize) {
				buffer.flip();
				writeFile(channel, (ByteBuffer) buffer);
				buffer.clear();
				bufferPos = 0;
				readLen = Math.min(bufferSize, leftLen);
			}
		} while (leftLen > 0);
	}

	/**
	 * 파일 크기가 최대 파일 크기를 초과하는지 확인합니다.
	 *
	 * @return 파일 크기가 최대 파일 크기를 초과하면 true, 그렇지 않으면 false
	 */
	public boolean isOverSize() {
		return maxFileSize > 0 && maxFileSize <= currentFileSize;
	}

	/**
	 * 모든 참조 카운터를 무효화 시키고 FileWriter를 완전히 닫습니다.
	 */
	public void close() {
		refCount = 0;
		end();
	}

	/**
	 * 버퍼를 플러시하고 남은 데이터를 파일에 씁니다.
	 */
	public void flush() throws IOException {
		if (isClosed()) return;
		if (buffer.position() > 0) {
			buffer.flip();
			writeFile(channel, (ByteBuffer) buffer);
			buffer.clear();
		}
	}

	/**
	 * 파일이 존재하는지 확인하고 존재하지 않으면 스트림을 재초기화합니다.
	 * @throws IOException 입출력 예외 발생 시
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void ensureFileExists() throws IOException {

		if (isClosed()) return;

		if (!file.exists()) {
			try {
				try {
					channel.close();
					OPEN_FILE_COUNT.decrementAndGet();
					outputStream.close();
				} catch (Exception ignored) {
				}
				file.createNewFile();
				initStream(file);
				currentFileSize = 0;
				buffer.flip();
			} catch (IOException e) {
				throw new IOException("Failed to ensure file exists and reinitialize stream for file: " + file.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * 이 FileWriter가 닫혔는지 확인합니다.
	 *
	 * @return 이 FileWriter가 닫혔으면 true, 그렇지 않으면 false
	 */
	public boolean isClosed() {
		return closed;
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
		if (obj instanceof FileWriter) {
			File destFile = ((FileWriter) obj).getFile();
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
		if (isClosed()) {
			return "FileWriter(" + Integer.toHexString(super.hashCode()) + ") [closed]";
		}
		return "FileWriter(" + Integer.toHexString(super.hashCode()) + ") [maxFileSize=" + maxFileSize + ", bufferSize=" + bufferSize + ", currentFileSize="
				+ currentFileSize + ", file=" + (file == null ? "null" : file.getAbsolutePath()) + "]";
	}

	private void initStream(File file) throws IOException {
		outputStream = new FileOutputStream(file, true);
		channel = outputStream.getChannel();
		OPEN_FILE_COUNT.incrementAndGet();
	}


	private void writeFile(FileChannel fileChannel, ByteBuffer buffer) throws IOException {
		if (isClosed()) return;

		try {
			currentFileSize += buffer.limit();
			while (buffer.hasRemaining()) {
				fileChannel.write(buffer);
			}
		} catch (IOException e) {
			throw new IOException("Failed to write data to file: " + file.getAbsolutePath(), e);
		}

	}

	/**
	 * FileWriter를 종료합니다.
	 * 만약 참조 카운터가 1보다 크면 참조 카운터만 감소시킵니다.
	 */
	public void end() {
		if (closed) {
			return;
		}
        try {
            flush();
        } catch (IOException ignored) {
			refCount = 0;
        }
        if (refCount > 0) {
			refCount--;
			if (refCount > 0) {
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
}