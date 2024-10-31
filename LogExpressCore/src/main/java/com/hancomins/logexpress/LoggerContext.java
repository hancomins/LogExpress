package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.Configuration;
import com.hancomins.logexpress.configuration.WriterOption;
import com.hancomins.logexpress.queue.AbsLineQueue;
import com.hancomins.logexpress.queue.LineQueueFactory;
import com.hancomins.logexpress.writer.OnTerminatedListener;
import com.hancomins.logexpress.writer.WriteWorker;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class LoggerContext {
	
	
	private final AtomicReference<LinkedHashMap<String, BaseLogger>> markerLoggerMapRef = new AtomicReference<LinkedHashMap<String,BaseLogger>>(new LinkedHashMap<String, BaseLogger>());

	static final LoggerContext EMPTY = new LoggerContext();

	
	private Configuration configuration;
	private BaseLogger defaultBaseLogger;
	private AbsLineQueue absLineQueue = null;
	private WriteWorker logWriter;

	private AtomicBoolean end = new AtomicBoolean();

	static {
		EMPTY.defaultBaseLogger = BaseLogger.EMPTY;
	}
	

	@SuppressWarnings("unused")
    private LoggerContext() {}

	LoggerContext(Configuration configure) {
		configuration = configure;
		init();
		initDefaultLogger(markerLoggerMapRef.get());

	}
	
	
	LoggerContext(Configuration configure, List<BaseLogger> baseLoggers) {
		configuration = configure;
		init();
		if(baseLoggers == null || baseLoggers.isEmpty()) {
			initDefaultLogger(markerLoggerMapRef.get());
			return;
		}

		defaultBaseLogger = baseLoggers.get(0);
		LinkedHashMap<String, BaseLogger> newLoggerMap = new LinkedHashMap<String, BaseLogger>();
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = baseLoggers.size(); i < n; ++i) {
			BaseLogger baseLogger = baseLoggers.get(i);
			String marker = baseLogger.getMarker();
			newLoggerMap.put(marker, baseLogger);
			WriterOption option = configuration.getWriterOption(marker);
			if(option == null) option = configuration.getDefaultWriterOption();
			//noinspection DataFlowIssue
			baseLogger.change(absLineQueue, option);
		}
		initDefaultLogger(newLoggerMap);
		LinkedHashMap<String, BaseLogger> current = null;
		do {
			current = markerLoggerMapRef.get();
		} while(!markerLoggerMapRef.compareAndSet(current, newLoggerMap));
	}
	
	Configuration getConfiguration() {
		return configuration;
	}
	
	ArrayList<BaseLogger> getLoggerList() {
		Collection<BaseLogger> baseLoggers = markerLoggerMapRef.get().values();
		return new ArrayList<BaseLogger>(baseLoggers);
	}
	
	private void initDefaultLogger(HashMap<String, BaseLogger> map) {
		WriterOption option = configuration.getDefaultWriterOption();
		String defaultMarker = configuration.getDefaultMarker();
		BaseLogger baseLogger = map.get(defaultMarker);
		if(baseLogger == null) {
			//noinspection DataFlowIssue
			defaultBaseLogger = new BaseLogger(defaultMarker,option);
			defaultBaseLogger.setLineQueue(absLineQueue);
			map.put(defaultMarker, defaultBaseLogger);

		}  else {
			defaultBaseLogger = baseLogger;
			baseLogger.setLineQueue(absLineQueue);
		}
	}
	
	private void init() {
		absLineQueue = LineQueueFactory.create(configuration.isNonBlockingQueue() ? LineQueueFactory.LineQueueType.NonBlocking : LineQueueFactory.LineQueueType.Blocking,
				configuration.getQueueSize());
		logWriter = new WriteWorker(configuration);
		if(configuration.isAutoShutdown()) {
			logWriter.setOnRequestShutdown(new Runnable() {
				@Override
				public void run() {
					LogExpress.shutdown();
				}
			});
		}
		logWriter.setLineQueue(absLineQueue);
	}
	
	protected void startLogWriter() {
		if(logWriter != null) {
			logWriter.start();
		}
	}

	boolean isEnd() {
		return end.get();
	}
	
	protected ShutdownFuture end() {

		end.set(true);
		final long currentThreadID = Thread.currentThread().getId();
		final ShutdownFuture shutdownFuture = new ShutdownFuture() {
			final AtomicBoolean isEnd = new AtomicBoolean(false);
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
			public void addOnEndCallback(Runnable runnable) {
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


		if(!logWriter.isAliveWorker()) {
			shutdownFuture.onEnd();
			return shutdownFuture;
		}

		absLineQueue = null;
		WriteWorker worker = logWriter;

		worker.end(new OnTerminatedListener() {
			@Override
			public void onTerminated() {
				shutdownFuture.onEnd();
			}
		});
		logWriter = null;
		configuration = null;
		defaultBaseLogger = null;
		return shutdownFuture;
	}
	
	
	
	
	public BaseLogger defaultLogger() {
		return defaultBaseLogger;
	}
	
	/*public boolean available() {
		return mAvailable;
	}*/
	
	
	public BaseLogger logger(String marker) {
		WriterOption option = configuration.getWriterOption(marker);
		if(option == null) {
			option = configuration.getDefaultWriterOption();
		}

		LinkedHashMap<String, BaseLogger> current, next;
		BaseLogger newBaseLogger = null;
		do {
			current = markerLoggerMapRef.get();
			BaseLogger baseLogger = current.get(marker);
			if(baseLogger != null) {
				return baseLogger;
			}
			next = new LinkedHashMap<String, BaseLogger>(current);
			if(newBaseLogger == null) {
                //noinspection DataFlowIssue
                newBaseLogger = new BaseLogger(marker, option);
				newBaseLogger.setLineQueue(absLineQueue);
			}
			next.put(marker, newBaseLogger);
		} while(!markerLoggerMapRef.compareAndSet(current, next));
		
		return newBaseLogger;
	}

}
