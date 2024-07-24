package com.hancomins.LogExpress.writer;


import com.hancomins.LogExpress.configuration.Configuration;
import com.hancomins.LogExpress.configuration.WriterOption;
import com.hancomins.LogExpress.configuration.WriterType;
import com.hancomins.LogExpress.queue.AbsLineQueue;
import com.hancomins.LogExpress.queue.OnPushLineListener;
import com.hancomins.LogExpress.util.Files;
import com.hancomins.LogExpress.util.SysTool;
import com.hancomins.LogExpress.InLogger;
import com.hancomins.LogExpress.Line;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


@SuppressWarnings("ResultOfMethodCallIgnored")
final public class WriteWorker extends Thread implements OnPushLineListener {

	private static final AtomicInteger TR_NUMBER = new AtomicInteger(0);

	private volatile boolean isWait = false;
	private volatile boolean isAlive = false;

	private volatile boolean isAutoShutdown = false;
	private boolean isDebug = false;
	private boolean isExistCheck = false;
	
	private int waitTimeout = 3000;
	private AbsLineQueue lineQueue = null;
	private final Object monitor = new Object();
	private LinkedHashMap<String, WriterRackStruct> writerMap = new LinkedHashMap<String, WriterRackStruct>();
	private WriterRackStruct[] writerRackArray = null;
	private WriterRackStruct defaultWriterRack;
	private OnTerminatedListener onTerminatedListener = null;




	
	public WriteWorker(Configuration configure) {
		try {
			waitTimeout = configure.getWorkerInterval();
			buildWriterRackStruct(configure);
			//mConsoleWriter = new ConsoleWriter(configure.getConsoleBufferSize());
			isDebug = InLogger.isEnabled();
			isAutoShutdown = configure.isAutoShutdown();
			isExistCheck = configure.isFileExistCheck();
			setName("LogExpressWriteWorker#" + TR_NUMBER.incrementAndGet());
			setDaemon(configure.isDaemonThread());
			if (isDebug) {
				InLogger.DEBUG("Create WriteWorker thread (" + getName() + ")");
				for(WriterRackStruct writerRackStruct : writerMap.values()) {
					InLogger.DEBUG("\t " + writerRackStruct.toString());
				}

			}
		} catch (Throwable e) {
			InLogger.ERROR("WriteWorker create error", e);
		}


	}
	
	public void setLineQueue(AbsLineQueue lineQueue) {
		this.lineQueue = lineQueue;
		lineQueue.setPushLineEvent(this);
	}
	
	
	@Override
	public void onPushLine() {
		wakeup();
	}
	
	@Override
	public synchronized void start() {
		if(isAlive) return;
		if(isDebug) {
			InLogger.INFO("start WriteWorker");
		}
		isAlive = true;
		super.start();
	}
	
	
	public boolean isAliveWorker() {
		return isAlive;
	}

	
	
	public void wakeup() {
		if(!isWait) return;
		synchronized (monitor) {
			if(!isWait) return;
			monitor.notify();
		}
	}

	private static final AtomicInteger FILE_NUMBER_FOR_PATTERN_TEST = new AtomicInteger(0);


	
	private static boolean tesFileNamePattern(String strFileNamePattern, FileNamePattern fileNamePattern) {
		File file = null;
		 try
		 {
			 file = fileNamePattern.toFile(CurrentTimeMillisGetter.currentTimeMillis(), "test-name-" + CurrentTimeMillisGetter.currentTimeMillis(), "test-marker-" + CurrentTimeMillisGetter.currentTimeMillis(), CurrentTimeMillisGetter.currentTimeMillis(), FILE_NUMBER_FOR_PATTERN_TEST.incrementAndGet());
			 if(file.isDirectory()) {
				 InLogger.ERROR("Cannot use a directory path or pattern. `" + strFileNamePattern + "` (Real path: " +file.getAbsolutePath() + ")", null);
				 return false;
			 }
			 file = new File( file.getParentFile(),Files.getNameWithoutExtension(file) + "-test" + FILE_NUMBER_FOR_PATTERN_TEST + "." + Files.getExtension(file));

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
		synchronized (monitor) {

			WriterOption[] logWriterConfigures = configure.getWriterOptions();
			String defaultMarker = configure.getDefaultMarker();

			WriterRackStruct firstRack = null;
			//noinspection ForLoopReplaceableByForEach
			for (int i = 0; i < logWriterConfigures.length; ++i) {
				WriterOption logWriterConfigure = logWriterConfigures[i];
				String strFilePattern = logWriterConfigure.getFile();
				FileNamePattern fileNamePattern = null;
				if (strFilePattern != null && !strFilePattern.isEmpty()) {
					fileNamePattern = FileNamePattern.parse(strFilePattern);
					if (!tesFileNamePattern(strFilePattern, fileNamePattern)) {
						continue;
					}
				}
				String[] markers = logWriterConfigure.getMarkers();
				WriterRackStruct rack = null;

				//noinspection ForLoopReplaceableByForEach
				for (int markerIndex = 0; markerIndex < markers.length; ++markerIndex) {
					String marker = markers[markerIndex];
					// 만약 파일 이름에 마커 이름이 들어갈 경우와 그렇지 않은 경우를 분리한다.
					// 마커 이름이 들어가는 경우는 같은 설정이라도 쓰려지는 파일이 다르기 때문에 두개의 rack 으로 분리해야한다.
					if (fileNamePattern != null && fileNamePattern.isMarkerInPattern()) {
						try {
							rack = makeRack(marker, fileNamePattern, logWriterConfigure);
							if (firstRack == null) firstRack = rack;
							if (marker.equals(defaultMarker)) {
								defaultWriterRack = rack;

							}
							writerMap.put(marker, rack);
						} catch (IOException e) {
							InLogger.ERROR("Unable to initialize the writer given the marker `" + marker + "`.\n", e);
						}
					} else {

						try {
							if (rack == null) {
								rack = makeRack("", fileNamePattern, logWriterConfigure);
								if (firstRack == null) firstRack = rack;
								if (marker.equals(defaultMarker)) {
									defaultWriterRack = rack;
								}
							}
							writerMap.put(marker, rack);

						} catch (IOException e) {
							InLogger.ERROR("Unable to initialize the writer given the marker `" + marker + "`.\n", e);
						}
					}
				}
			}
			if (defaultWriterRack == null) {
				defaultWriterRack = firstRack;
			}
			initWriterRackArray();
		}
	}

	
	private void initWriterRackArray() {
		writerRackArray = new WriterRackStruct[writerMap.size()];
		writerMap.values().toArray(writerRackArray);
	}
	
	private WriterRackStruct makeRack(String marker,FileNamePattern pattern,WriterOption configure) throws IOException {
		 WriterRackStruct rack = new WriterRackStruct(marker,configure.getEncoding(), configure.getMaxSize(), configure.getBufferSize(), configure.getHistory());
		 rack.fileNamePattern = pattern;
		 WriterType[] types = configure.getWriterTypes();

		 for(int i = 0; i < types.length; ++i) {
			 WriterType type = types[i];
			 if(type == WriterType.File) {
				if(pattern == null) {
					if(isDebug) {
						InLogger.ERROR("You cannot use a file path or pattern.");
					}
					continue;
				}
				int maxSize = configure.getMaxSize();
				File newFile = pattern.toFileOverMaxSize(marker,maxSize);
				makeDirParentsOf(newFile);
				FileWriter fileWriter = findFileWriter(newFile);
				rack.fileWriter = fileWriter == null ? new FileWriter(newFile, configure.getBufferSize(), configure.getMaxSize()) : fileWriter;
			 } else if(type == WriterType.Console) {
				 rack.isWriteConsole = true;
			 }
		 }
		 return rack;
	}
	
	private void checkExist() {
		if(defaultWriterRack != null && defaultWriterRack.fileWriter != null) {
			if(defaultWriterRack.fileWriter.checkExist() && isDebug) {
				InLogger.ERROR("Log file could not be found. regenerate `" + defaultWriterRack.fileWriter.getFile() + "` (" + getName() + ")" );
			}
		}
		//noinspection ForLoopReplaceableByForEach
		for(int i = 0; i < writerRackArray.length; ++i) {
			WriterRackStruct rack =  writerRackArray[i];
			if(rack.fileWriter != null && rack.fileWriter.checkExist() && isDebug && defaultWriterRack != null) {
				InLogger.ERROR("Log file could not be found. regenerate `" + defaultWriterRack.fileWriter.getFile() + "` (" + getName() + ")" );
			}
		}
	}
	
	
	private void flushWrite() {
		if(defaultWriterRack != null && defaultWriterRack.fileWriter != null) {
			defaultWriterRack.fileWriter.flush();
		}
		if(writerRackArray != null) {
			//noinspection ForLoopReplaceableByForEach
			for(int i = 0; i < writerRackArray.length; ++i) {
				WriterRackStruct rack =  writerRackArray[i];
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

		while(isAlive) {
			Line line = lineQueue.pop();
			if(line == null) {
				if(!isWait) {
					if(isExistCheck) checkExist();
					flushWrite();
					line = lineQueue.pop();
				}
			}

			if(line == null) {
				if(onTerminatedListener != null || callShutdown) {
					endLoop();
					return;
				}
				try {
					synchronized (monitor) {
						line = lineQueue.pop();
						if(line == null) {
							if (isDebug && !isWait) {
								InLogger.INFO("WriteWorker in Wait (" + getName() + ")");
							}
							isWait = true;
							if(isAutoShutdown && !isMainThread()) {
								callShutdown = true;
								if(InLogger.isEnabled()) {
									InLogger.INFO("Thread id 1 has ended. WriteWorker in AutoShutdown (" + getName() + ")");
								}
								continue;
							}
							monitor.wait(waitTimeout);
						}
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				continue;
			}

			isWait = false;
			WriterRackStruct rack = getWriterRack(line);
			String message = line.release().toString();
			if(rack != null) {
				writeConsole(rack, message);
				byte[] stringBuffer = message.getBytes(rack.charset);
				writeFile(rack, line.getTime(), stringBuffer);
			} else {
				InLogger.ERROR("Cannot find the writer for the marker `" + line.getMarker() + "`.", null);
				InLogger.DEBUG(message);
			}
			line = null;


		}
		endLoop();
	}

	private void endLoop() {
		flushWrite();
		terminate();
		if(isDebug) {
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

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private void writeFile(WriterRackStruct rack,long time,  byte[] stringBuffer) {

		if(rack.fileWriter != null) {
			// 파일 패턴에 파일 번호가 있고, 설정한 최대 파일 크기를 넘어갈 경우.

			//todo 지워야함.
			String tomorrowDate = formatter.format(rack.tomorrow);
			String todayDate = formatter.format(rack.today);
			String current = formatter.format(time);

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
		WriterRackStruct rack = writerMap.get(marker);
		if(rack == null) {
			rack = defaultWriterRack;
		}
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
			if(isDebug) {
				 InLogger.INFO("Write to the next file. `" + newFile + "` (" + getName() + ")" );
			 }
			FileWriter fileWriter = findFileWriter(newFile);
            rack.fileWriter = fileWriter == null ? new FileWriter(newFile, rack.fileBufferSize, rack.fileMaxSize) : fileWriter;
		} catch (IOException e) {
			InLogger.ERROR("Cannot advance to the next file of marker `" + rack.marker + "`.", e);
		}
	}
	
	private void cleanupLogFileByHistory(WriterRackStruct rack) {		
		OldFileCleaner cleaner = new OldFileCleaner(rack.fileNamePattern, SysTool.pid(), SysTool.hostname(),rack.marker);
		cleaner.clean(rack.history);
	}
	
	
	private void terminate() {
		synchronized (monitor) {
			if(!isAlive) {
				return;
			}
			if (isDebug) {
				InLogger.DEBUG("Call WriteWorker terminate() (" + getName() + ")");
			}
			isWait = false;
			isAlive = false;
			lineQueue = null;
			for (WriterRackStruct rack : writerMap.values()) {
				rack.end();
			}
			writerMap.clear();
			writerMap = null;
			Arrays.fill(writerRackArray, null);
			writerRackArray = null;
			if (defaultWriterRack != null) defaultWriterRack.end();
			defaultWriterRack = null;
			//mConsoleWriter = null;
			if (onTerminatedListener != null) {
				onTerminatedListener.onTerminated();
			}
			onTerminatedListener = null;
		}
	}

	private FileWriter findFileWriter(File file) {
		for(WriterRackStruct rack : writerMap.values()) {
			if(rack.fileWriter != null && !rack.fileWriter.isClosed() && rack.fileWriter.getFile().equals(file)) {
				return rack.fileWriter;
			}
		}
		return null;
	}
	
	
	
	public void end(OnTerminatedListener onTerminatedListener) {
		if(!isAlive() || !isAlive) {
			onTerminatedListener.onTerminated();
			return;
		}
		this.onTerminatedListener = onTerminatedListener;
		wakeup();
	}
	

	

}
