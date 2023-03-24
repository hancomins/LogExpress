package com.clipsoft.LogExpress;

import com.clipsoft.LogExpress.configuration.Configuration;
import com.clipsoft.LogExpress.configuration.WriterOption;
import com.clipsoft.LogExpress.queue.AbsLineQueue;
import com.clipsoft.LogExpress.queue.LineQueueFactory;
import com.clipsoft.LogExpress.writer.OnTerminatedListener;
import com.clipsoft.LogExpress.writer.WriteWorker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LoggerContext {
	
	
	private AtomicReference<LinkedHashMap<String, BaseLogger>> mMarkerLoggerMapRef = new AtomicReference<LinkedHashMap<String,BaseLogger>>(new LinkedHashMap<String, BaseLogger>());
	
	
	private Configuration mConfiguration;
	private BaseLogger mDefaultBaseLogger;
	private AbsLineQueue mAbsLineQueue = null;
	private WriteWorker mLogWriter;
	

	private LoggerContext() {}

	LoggerContext(Configuration configure) {
		mConfiguration = configure;
		init();
		initDefaultLogger(mMarkerLoggerMapRef.get());

	}
	
	
	LoggerContext(Configuration configure, ArrayList<BaseLogger> baseLoggers) {
		mConfiguration = configure;
		init();
		if(baseLoggers == null || baseLoggers.isEmpty()) {
			initDefaultLogger(mMarkerLoggerMapRef.get());
			return;
		}

		mDefaultBaseLogger = baseLoggers.get(0);
		LinkedHashMap<String, BaseLogger> newLoggerMap = new LinkedHashMap<String, BaseLogger>();
		for(int i = 0, n = baseLoggers.size(); i < n; ++i) {
			BaseLogger baseLogger = baseLoggers.get(i);
			String marker = baseLogger.getMarker();
			newLoggerMap.put(marker, baseLogger);
			WriterOption option = mConfiguration.getWriterOption(marker);
			if(option == null) option = mConfiguration.getDefaultWriterOption();
			baseLogger.change(mAbsLineQueue, option);
		}
		initDefaultLogger(newLoggerMap);
		LinkedHashMap<String, BaseLogger> current = null;
		do {
			current = mMarkerLoggerMapRef.get();
		} while(!mMarkerLoggerMapRef.compareAndSet(current, newLoggerMap));
	}
	
	Configuration getConfiguration() {
		return mConfiguration;
	}
	
	ArrayList<BaseLogger> getLoggerList() {
		Collection<BaseLogger> baseLoggers = mMarkerLoggerMapRef.get().values();
		return new ArrayList<BaseLogger>(baseLoggers);
	}
	
	private void initDefaultLogger(HashMap<String, BaseLogger> map) {
		WriterOption option = mConfiguration.getDefaultWriterOption();
		String defaultMarker = mConfiguration.getDefaultMarker();
		BaseLogger baseLogger = map.get(defaultMarker);
		if(baseLogger == null) {
			mDefaultBaseLogger = new BaseLogger(defaultMarker,option);
			mDefaultBaseLogger.setLineQueue(mAbsLineQueue);
		}  else {
			mDefaultBaseLogger = baseLogger;
			baseLogger.setLineQueue(mAbsLineQueue);
		}
	}
	
	private void init() {
		mAbsLineQueue = LineQueueFactory.create(mConfiguration.isNonBlockingQueue() ? LineQueueFactory.LineQueueType.NonBlocking : LineQueueFactory.LineQueueType.Blocking,
				mConfiguration.getQueueSize());
		mLogWriter = new WriteWorker(mConfiguration);
		mLogWriter.setLineQueue(mAbsLineQueue);
	}
	
	protected void startLogWriter() {
		if(mLogWriter != null) {
			mLogWriter.start();
		}
	}
	
	protected ShutdownFuture end() {

		final long currentThreadID = Thread.currentThread().getId();
		final ShutdownFuture shutdownFuture = new ShutdownFuture() {
			AtomicBoolean isEnd = new AtomicBoolean(false);
			Runnable onEndCallback = null;


			@Override
			public boolean isEnd() {
				return isEnd.get();
			}

			@Override
			public void onEnd() {
				isEnd.set(true);
				if(onEndCallback != null) {
					callEvent(onEndCallback);
				}
			}

			@Override
			public void setOnEndCallback(Runnable runnable) {
				if(isEnd.get()) {
					callEvent(runnable);
					return;
				}
				onEndCallback = runnable;
			}

			@Override
			public void await() {}

			private void callEvent(Runnable runnable) {
				//System.out.println(Thread.currentThread().getId());
				//System.out.println(Thread.currentThread().getName());

				if(currentThreadID == Thread.currentThread().getId()) {
					Thread thread = new Thread(runnable);
					thread.setName("LOGExpressEndCallback");
					thread.start();
				} else {
					runnable.run();;
				}
			}
		};


		if(!mLogWriter.isAliveWorker()) {
			shutdownFuture.onEnd();
			return shutdownFuture;
		}

		mAbsLineQueue = null;
		WriteWorker worker = mLogWriter;

		worker.end(new OnTerminatedListener() {
			@Override
			public void onTerminated() {
				shutdownFuture.onEnd();
			}
		});
		mLogWriter = null;
		mConfiguration = null;
		mDefaultBaseLogger = null;
		return shutdownFuture;
	}
	
	
	
	
	public BaseLogger defaultLogger() {
		return mDefaultBaseLogger;
	}
	
	/*public boolean available() {
		return mAvailable;
	}*/
	
	
	public BaseLogger logger(String marker) {
		WriterOption option = mConfiguration.getWriterOption(marker);
		if(option == null) {
			option = mConfiguration.getDefaultWriterOption();
		}

		LinkedHashMap<String, BaseLogger> current, next;
		BaseLogger newBaseLogger = null;
		do {
			current = mMarkerLoggerMapRef.get();
			BaseLogger baseLogger = current.get(marker);
			if(baseLogger != null) {
				return baseLogger;
			}
			next = new LinkedHashMap<String, BaseLogger>(current);
			if(newBaseLogger == null) {
				newBaseLogger = new BaseLogger(marker, option);
				newBaseLogger.setLineQueue(mAbsLineQueue);
			}
			next.put(marker, newBaseLogger);
		} while(!mMarkerLoggerMapRef.compareAndSet(current, next));
		
		return newBaseLogger;
	}

}
