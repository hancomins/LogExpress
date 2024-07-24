package com.hancomins.LogExpress;

import com.hancomins.LogExpress.configuration.Configuration;
import com.hancomins.LogExpress.util.ModulePathFinder;
import com.hancomins.LogExpress.util.SysTool;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unused")
public class LogExpress {

	public static final String PROPERTY_PATH = "LogExpress.path";
	public static final String PROPERTY_HOSTNAME = "LogExpress.hostname";
	public static final String PROPERTY_PID = "LogExpress.pid";

	private static final AtomicReference<ShutdownFuture> ShutdownFutureRef=  new AtomicReference<ShutdownFuture>();

	private final static ReentrantLock lock = new ReentrantLock();

	static AtomicReference<LoggerContext> LoggerContextRef =  new AtomicReference<LoggerContext>();
	static {
		lock.lock();
		initLogger();
		lock.unlock();
	}

	private static void initLogger() {
		LoggerContextRef.set(new LoggerContext(Configuration.fromDefaultConfigurationFile().close()));
		System.setProperty(PROPERTY_PATH, ModulePathFinder.find(LogExpress.class).getAbsolutePath());
		System.setProperty(PROPERTY_HOSTNAME, SysTool.hostname());
		System.setProperty(PROPERTY_PID, SysTool.pid() + "");
		LoggerContextRef.get().startLogWriter();
	}


	public static Configuration cloneConfiguration() {
		try {
			lock.lock();
			if (LoggerContextRef.get() == null) {
				initLogger();
			}
			LoggerContext loggerContext = LoggerContextRef.get();
			if(loggerContext == null) {
				 return Configuration.fromDefaultConfigurationFile();
			} else {
				return loggerContext.getConfiguration().clone();
			}
		} finally {
			if(lock.isLocked()) {
				lock.unlock();
			}
		}
	}
	
	public static void updateConfig(Configuration configure) {
		if(configure.isClosed()) {
			throw new IllegalStateException("Configuration is closed. Please create a new Configuration or clone the existing one.");
		}
		lock.lock();
		if(configure.isDebug()) {
			InLogger.enable();
			InLogger.enableFile(configure.isDebugFileLogEnabled());
			InLogger.enableConsole(configure.isDebugConsoleLogEnabled());
		} else {
			InLogger.disable();
		}

		waitForShutdown();
		try {
			if(LoggerContextRef.get() == null) {
				initLogger();
			}
			final CountDownLatch latch = new CountDownLatch(1);
			if (InLogger.isEnabled()) {
				InLogger.DEBUG("Logger will be fully initialized.  (" + Thread.currentThread().getName() + ")");
			}
			configure.close();
			LoggerContext oldContext = LoggerContextRef.get();
			ArrayList<BaseLogger> baseLoggers = oldContext.getLoggerList();
			LoggerContext context = new LoggerContext(configure, baseLoggers);
			LoggerContextRef.set(context);
			ShutdownFuture shutdownFuture = oldContext.end();
			shutdownFuture.setOnEndCallback(new Runnable() {
				@Override
				public void run() {
					// 디버그 로그를 출력한다.
					LoggerContext context = LoggerContextRef.get();
					if (context != null) {
						//Configuration configuration = context.getConfiguration();
						if (InLogger.isEnabled()) {
							InLogger.DEBUG("Restart log writer (" + Thread.currentThread().getName() + ")");
						}
					}
					LoggerContextRef.get().startLogWriter();
					latch.countDown();
				}
			});

			try {
				latch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} finally {
			if(lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	private static void waitForShutdown() {
		while(ShutdownFutureRef.get() != null) {
			try {
                //noinspection BusyWait
                Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static ShutdownFuture createEndShutdownFuture() {
		return new ShutdownFuture() {
			@Override
			public boolean isEnd() {
				return true;
			}

			@Override
			public void onEnd() {
			}

			@Override
			public void setOnEndCallback(Runnable runnable) {
				runnable.run();
			}

			@Override
			public void await() {}
		};
	}





	public static ShutdownFuture shutdown() {
		lock.lock();
		try {
			if (ShutdownFutureRef.get() != null) {
				return ShutdownFutureRef.get();
			}
			final UserShutdownFuture resultFuture = new UserShutdownFuture();
			if (LoggerContextRef.get() == null) return createEndShutdownFuture();
			info("LOGExpress shutdown called.");
			if (InLogger.isEnabled()) {
				InLogger.INFO("LOGExpress shutdown called.");
			}
			LoggerContext context = LoggerContextRef.get();
			ShutdownFuture shutdownFuture = context.end();
			ShutdownFutureRef.set(shutdownFuture);
			shutdownFuture.setOnEndCallback(new Runnable() {
				@Override
				public void run() {
				// 디버그 로그를 출력한다.
				resultFuture.onEnd();
				LoggerContextRef.set(null);
				ShutdownFutureRef.set(null);
				}
			});
			return resultFuture;
		} finally {
			if(lock.isLocked()) {
				lock.unlock();
			}
		}

	}
	
	
	public static Logger newLogger(String marker) {
		return new LoggerImpl(LoggerContextRef.get().logger(marker));
	}

	public static Logger newLogger() {
		return new LoggerImpl(LoggerContextRef.get().defaultLogger());
	}

	public static Logger newLogger(Class<?> caller, String marker) {
		return new LoggerWithCallerImpl(caller.getName(),LoggerContextRef.get().logger(marker));
	}

	public static Logger newLogger(Class<?> caller) {
		return new LoggerWithCallerImpl(caller.getName(), LoggerContextRef.get().defaultLogger());
	}

	public static Logger newLogger(String callerFQCN, String marker) {
		LoggerContext context = LoggerContextRef.get();
		return new LoggerWithCallerImpl(callerFQCN,marker == null ? context.defaultLogger() : context.logger(marker));
	}

	/**
	 * @deprecated use {@link #baseLogger()}
	 * @return CoreLogger
	 */
	@Deprecated
	public static BaseLogger defaultLogger() {
		return baseLogger();
	}

	/**
	 * @deprecated use {@link #baseLogger(String)}
	 * @return CoreLogger
	 */
	@Deprecated
	public static BaseLogger logger(String marker) {
		return baseLogger(marker);
	}

	public static BaseLogger baseLogger() {
		LoggerContext context =  LoggerContextRef.get();
		return context.defaultLogger();
	}

	public static BaseLogger baseLogger(String marker) {
		return LoggerContextRef.get().logger(marker);
	}
	
	public static void info(CharSequence log) {
		baseLogger().info("",log,1);
	}
	
	public static void info(CharSequence log,Throwable e) {
		baseLogger().info("",log,e, 1);
	}
	
	public static void error(CharSequence log) {
		baseLogger().error("",log,1);
	}
	
	public static void error(CharSequence log,Throwable e) {
		baseLogger().error("",log,e, 1);
	}
	

	@Deprecated
	public static boolean isInfo() {
		return baseLogger().isInfoEnabled();
	}
	
	public static boolean isInfoEnabled() {
		return baseLogger().isInfoEnabled();
	}
	
	@Deprecated
	public static boolean isError() {
		return baseLogger().isErrorEnabled();
	}
	
	public static boolean isErrorEnabled() {
		return baseLogger().isErrorEnabled();
	}
	
	@Deprecated
	public static boolean isDebug() {
		return baseLogger().isDebugEnabled();
	}
	
	public static boolean isDebugEnabled() {
		return baseLogger().isDebugEnabled();
	}
	
	@Deprecated
	public static boolean isTrace() {
		return baseLogger().isTraceEnabled();
	}
	
	public static boolean isTraceEnabled() {
		return baseLogger().isTraceEnabled();
	}
	
	@Deprecated
	public static boolean isWarn() {
		return baseLogger().isWarnEnabled();
	}
	
	public static boolean isWarnEnabled() {
		return baseLogger().isWarnEnabled();
	}
	
	
	public static void debug(CharSequence log) {
		baseLogger().debug("",log,1);
	}

	
	public static void debug(CharSequence log,Throwable e) {
		baseLogger().debug("",log,e, 1);
	}
	
	public static void trace(CharSequence log) {
		baseLogger().trace("",log,1);
	}
	
	public static void trace(CharSequence log,Throwable e) {
		baseLogger().trace("",log,e, 1);
	}
	
	public static void warn(CharSequence log) {
		baseLogger().warn("",log,1);
	}
	
	public static void warn(CharSequence log,Throwable e) {
		baseLogger().warn("",log,e, 1);
	}
	
}
