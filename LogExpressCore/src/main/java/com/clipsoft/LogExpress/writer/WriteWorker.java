package com.clipsoft.LogExpress.writer;

import com.clipsoft.LogExpress.*;
import com.clipsoft.LogExpress.configuration.Configuration;
import com.clipsoft.LogExpress.configuration.WriterOption;
import com.clipsoft.LogExpress.configuration.WriterType;
import com.clipsoft.LogExpress.queue.AbsLineQueue;
import com.clipsoft.LogExpress.queue.OnPushLineListener;
import com.clipsoft.LogExpress.util.Systool;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


final public class WriteWorker extends Thread implements OnPushLineListener {

	private static AtomicInteger TR_NUMBER = new AtomicInteger(0);

	private volatile boolean mIsWait = false;
	private volatile boolean mIsAlive = false;

	private volatile boolean mIsAutoShutdown = false;
	private boolean mIsDebug = false;
	private boolean mIsExistCheck = false;
	
	private int mWaitTimeout = 3000;
	private AbsLineQueue mLineQueue = null;
	private final Object mMonitor = new Object();
	private LinkedHashMap<String, WriterRackStruct> mWriterMap = new LinkedHashMap<String, WriterRackStruct>();
	private WriterRackStruct[] mWriterRackArray = null;
	private WriterRackStruct mDefaultWriterRack;
	//private ConsoleWriter mConsoleWriter = null;
	private OnTerminatedListener mOnTerminatedListener = null;


	
	public WriteWorker(Configuration configure) {
		try {
			mWaitTimeout = configure.getWorkerInterval();
			buildWriterRackStruct(configure);
			//mConsoleWriter = new ConsoleWriter(configure.getConsoleBufferSize());
			mIsDebug = InLogger.isEnabled();
			mIsAutoShutdown = configure.isAutoShutdown();
			mIsExistCheck = configure.isFileExistCheck();
			setName("LogExpressWriteWorker#" + TR_NUMBER.incrementAndGet());
			setDaemon(configure.isDaemonThread());
			if (mIsDebug) {
				InLogger.DEBUG("Create WriteWorker thread (" + getName() + ")");
				for(WriterRackStruct writerRackStruct : mWriterMap.values()) {
					InLogger.DEBUG("\t " + writerRackStruct.toString());
				}

			}
		} catch (Throwable e) {
			InLogger.ERROR("WriteWorker create error", e);
		}


	}
	
	public void setLineQueue(AbsLineQueue lineQueue) {
		mLineQueue = lineQueue;
		mLineQueue.setPushLineEvent(this);
	}
	
	
	@Override
	public void onPushLine() {
		wakeup();
	}
	
	@Override
	public synchronized void start() {
		if(mIsAlive) return;
		if(mIsDebug) {
			InLogger.INFO("start WriteWorker");
		}
		mIsAlive = true;
		super.start();
	}
	
	
	public boolean isAliveWorker() {
		return mIsAlive;
	}

	
	
	public void wakeup() {
		if(!mIsWait) return;
		synchronized (mMonitor) {
			if(!mIsWait) return;
			mMonitor.notify();
		}
	}
	
	private static boolean tesFileNamePattern(String strFileNamePattern, FileNamePattern fileNamePattern) {
		File file = null;
		 try {
			 file = fileNamePattern.toFile(CurrentTimeMillisGetter.currentTimeMillis(), "test-name-" + CurrentTimeMillisGetter.currentTimeMillis(), "test-marker-" + CurrentTimeMillisGetter.currentTimeMillis(), CurrentTimeMillisGetter.currentTimeMillis(), 100);
			 makeDirParentsOf(file);
			 file.delete();
			 if(!file.createNewFile()) {
				 InLogger.ERROR("Cannot use a file path or pattern. `" + strFileNamePattern + "` (Real path: " +file.getAbsolutePath() + ")", null);
				 return false;
			 }
			 file.delete();
			 File parentDir = file.getParentFile();
			 try {
				 parentDir.delete();
			 } catch (Exception ignored) {}
			 return true;
		 } catch (Exception e) {
			 InLogger.ERROR("File path or pattern is invalid. `" + strFileNamePattern + "` (Real path: " +(file == null? "null" : file.getAbsolutePath()) + ")", e);
			 return false;
		 }
	}
	
	private void buildWriterRackStruct(Configuration configure) {
		 WriterOption[] logWriterConfigures =  configure.getWriterOptions();
		 String defaultMarker = configure.getDefaultMarker();
		 
		 WriterRackStruct firstRack = null;
		 for(int i = 0; i < logWriterConfigures.length; ++i) {
			 WriterOption logWriterConfigure = logWriterConfigures[i];
			 String strFilePattern = logWriterConfigure.getFile();
			 FileNamePattern fileNamePattern = null;
			 if(strFilePattern != null && !strFilePattern.isEmpty()) {
				 fileNamePattern = FileNamePattern.parse(strFilePattern);
				 if(!tesFileNamePattern(strFilePattern, fileNamePattern)) {
					 continue;
				 }
			 }
			 String[] markers = logWriterConfigure.getMarkers();
			 WriterRackStruct rack = null;
			 
			 for(int markerIndex = 0; markerIndex < markers.length; ++markerIndex) {
				 String marker = markers[markerIndex];
				 // 만약 파일 이름에 마커 이름이 들어갈 경우와 그렇지 않은 경우를 분리한다.
				 // 마커 이름이 들어가는 경우는 같은 설정이라도 쓰려지는 파일이 다르기 때문에 두개의 rack 으로 분리해야한다.
				 if(fileNamePattern != null && fileNamePattern.isMarkerInPattern()) {
					 try {
						rack = makeRack(marker, fileNamePattern, logWriterConfigure);
						if(firstRack == null) firstRack = rack;
						if(marker.equals(defaultMarker)) {
							mDefaultWriterRack = rack;
						}
						mWriterMap.put(marker, rack);
					} catch (IOException e) {
						 InLogger.ERROR("Unable to initialize the writer given the marker `" + marker + "`.\n", e);
					 }
				 } else {
					 
					try {
						if(rack == null) {
							rack = makeRack("", fileNamePattern, logWriterConfigure);
							if(firstRack == null) firstRack = rack;
							if(marker.equals(defaultMarker)) {
								mDefaultWriterRack = rack;
							}
						}
						mWriterMap.put(marker, rack);
						
					} catch (IOException e) {
						InLogger.ERROR("Unable to initialize the writer given the marker `" + marker + "`.\n", e);
					}
				 }
			 }
		 }
		 if(mDefaultWriterRack == null) {
			 mDefaultWriterRack = firstRack;
		 }
		 initWriterRackArray();
	}

	
	private void initWriterRackArray() {
		mWriterRackArray = new WriterRackStruct[mWriterMap.size()];
		mWriterMap.values().toArray(mWriterRackArray);
	}
	
	private WriterRackStruct makeRack(String marker,FileNamePattern pattern,WriterOption configure) throws IOException {
		 WriterRackStruct rack = new WriterRackStruct(marker,configure.getEncoding(), configure.getMaxSize(), configure.getBufferSize(), configure.getHistory());
		 rack.fileNamePattern = pattern;
		 WriterType[] types = configure.getWriterTypes();
		 for(int i = 0; i < types.length; ++i) {
			 WriterType type = types[i];
			 if(type == WriterType.File) {
				if(pattern == null) {
					if(mIsDebug) {
						InLogger.ERROR("You cannot use a file path or pattern.");
					}
					continue;
				}
				int maxSize = configure.getMaxSize();
				File file = pattern.toFileOverMaxSize(marker,maxSize);
				makeDirParentsOf(file);
				rack.fileWriter = new FileWriter(file, configure.getBufferSize(), configure.getMaxSize()); 
			 } else if(type == WriterType.Console) {
				 rack.isWriteConsole = true;
			 }
		 }
		 return rack;
	}
	
	private void checkExist() {
		if(mDefaultWriterRack != null && mDefaultWriterRack.fileWriter != null) {
			if(mDefaultWriterRack.fileWriter.checkExist() && mIsDebug) {
				InLogger.ERROR("Log file could not be found. regenerate `" + mDefaultWriterRack.fileWriter.getFile() + "` (" + getName() + ")" );
			}
		}
		for(int i = 0; i < mWriterRackArray.length; ++i) {
			WriterRackStruct rack =  mWriterRackArray[i];
			if(rack.fileWriter != null && rack.fileWriter.checkExist() && mIsDebug && mDefaultWriterRack != null) {
				InLogger.ERROR("Log file could not be found. regenerate `" + mDefaultWriterRack.fileWriter.getFile() + "` (" + getName() + ")" );
			}
		}
	}
	
	
	private void flushWrite() {
		if(mDefaultWriterRack.fileWriter != null) {
			mDefaultWriterRack.fileWriter.flush();
		}
		if(mWriterRackArray != null) {
			for(int i = 0; i < mWriterRackArray.length; ++i) {
				WriterRackStruct rack =  mWriterRackArray[i];
				if(rack.fileWriter != null) rack.fileWriter.flush();
			}
			//mConsoleWriter.flush();
		}

	}



	private void setUncaughtExceptionHandler() {
		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				InLogger.ERROR("UncaughtException in WriteWorker (" + getName() + ")" , e);
			}
		});
	}
	
	
	@Override
	public void run() {

		setUncaughtExceptionHandler();

		boolean callShutdown = false;

		while(mIsAlive) {
			Line line = mLineQueue.pop();
			if(line == null) {
				if(!mIsWait) {
					if(mIsExistCheck) checkExist();
					flushWrite();
					line = mLineQueue.pop();
				}
			}

			if(line == null) {
				if(mOnTerminatedListener != null || callShutdown) {
					endLoop();
					return;
				}
				try {
					synchronized (mMonitor) {
						line = mLineQueue.pop();
						if(line == null) {
							if (mIsDebug && !mIsWait) {
								InLogger.INFO("WriteWorker in Wait (" + getName() + ")");
							}
							mIsWait = true;
							if(mIsAutoShutdown && !isMainThread()) {
								callShutdown = true;
								if(InLogger.isEnabled()) {
									InLogger.INFO("Thread id 1 has ended. WriteWorker in AutoShutdown (" + getName() + ")");
								}
								continue;
							}
							mMonitor.wait(mWaitTimeout);
						}
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				continue;
			}

			mIsWait = false;
			WriterRackStruct rack = getWriterRack(line);
			String message = line.release().toString();
			writeConsole(rack, message);
			byte[] stringBuffer = message.getBytes(rack.charset);
			writeFile(rack,line.getTime(),  stringBuffer);
			line = null;
		}
		endLoop();
	}

	private void endLoop() {
		flushWrite();
		terminate();
		if(mIsDebug) {
			InLogger.DEBUG("WriteWorker Terminated (" + getName() + ")" );
		}
	}


	// 모든 스레드를 탐색하여 thread id 가 1이 있으면 true를 반환하는 메서드.
	private boolean isMainThread() {
		Set<Thread> threads =  Thread.getAllStackTraces().keySet();
		for(Thread th : threads) {
			if(th.getId() == 1) return true;
		}
		return false;
	}
	

	
	private void writeFile(WriterRackStruct rack,long time,  byte[] stringBuffer) {

		if(rack.fileWriter != null) {
			// 파일 패턴에 파일 번호가 있고, 설정한 최대 파일 크기를 넘어갈 경우.
			if(rack.fileNamePattern.isDateInPattern() && time >= rack.tomorrow) {
				cleanupLogFileByHistory(rack);
				rack.newDate();
				nextFile(rack);
				rack.fileWriter.write(stringBuffer);
				rack.fileWriter.flush();
				return;
			}
			rack.fileWriter.write(stringBuffer);
			if(rack.fileNamePattern.isNumberInPattern() && rack.fileWriter.isOverSize()) {
				nextFile(rack);
			}
		}	
	}
	
	private void writeConsole(WriterRackStruct rack, String message) {
		if(rack.isWriteConsole) {
			//mConsoleWriter.write(stringBuffer);
			System.out.print(message);

		}
	}
	
	
	private WriterRackStruct getWriterRack(Line line) {
		String marker = line.getMarker();
		WriterRackStruct rack = mWriterMap.get(marker);
		if(rack == null) rack = mDefaultWriterRack;
		return rack;
		
	}
	
	private static void makeDirParentsOf(File file) {
		File dir = file.getParentFile();
		if(!dir.exists()) {
			dir.mkdirs();
		}
	}


	
	private void nextFile(WriterRackStruct rack) {
		rack.fileWriter.end();
		try {
			File newFile = rack.fileNamePattern.toFileOverMaxSize(rack.marker, rack.fileMaxSize);
			makeDirParentsOf(newFile);
			 if(mIsDebug) {
				 InLogger.INFO("Write to the next file. `" + newFile + "` (" + getName() + ")" );
			  }
			 FileWriter fileWriter = new FileWriter(newFile, rack.fileBufferSize, rack.fileMaxSize);
			 rack.fileWriter = fileWriter;
		} catch (IOException e) {
			InLogger.ERROR("Cannot advance to the next file of marker `" + rack.marker + "`.", e);
		}
	}
	
	private void cleanupLogFileByHistory(WriterRackStruct rack) {		
		OldFileCleaner cleaner = new OldFileCleaner(rack.fileNamePattern, Systool.pid(), Systool.hostname(),rack.marker);
		cleaner.clean(rack.history);
	}
	
	
	private synchronized void terminate() {
		if(mIsDebug) {
			InLogger.DEBUG("Call WriteWorker terminate() (" + getName() + ")" );
		}
		mIsWait = false;	
		mIsAlive = false;
		mLineQueue = null;
		for(WriterRackStruct rack : mWriterMap.values()) {
			rack.end();
		}
		mWriterMap.clear();
		mWriterMap = null;
		Arrays.fill(mWriterRackArray, null);
		mWriterRackArray = null;
		if(mDefaultWriterRack != null) mDefaultWriterRack.end();
		mDefaultWriterRack = null;
		//mConsoleWriter = null;
		if(mOnTerminatedListener != null) {
			mOnTerminatedListener.onTerminated();
		}
		mOnTerminatedListener = null;
	}
	
	
	
	public final void end(OnTerminatedListener onTerminatedListener) {
		if(!isAlive() || !mIsAlive) {
			onTerminatedListener.onTerminated();
			return;
		}
		mOnTerminatedListener = onTerminatedListener;
		wakeup();

	}
	

	

}
