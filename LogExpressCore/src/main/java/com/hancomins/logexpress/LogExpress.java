package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.Configuration;
import com.hancomins.logexpress.util.ModulePathFinder;
import com.hancomins.logexpress.util.SysTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unused")
public class LogExpress {

	/**
	 * Property path for logexpress.<br>
	 * LogExpress의 속성 경로.
	 */
	public static final String PROPERTY_PATH = "logexpress.path";

	/**
	 * Property hostname for logexpress.<br>
	 * LogExpress의 호스트 이름 속성.
	 */
	public static final String PROPERTY_HOSTNAME = "logexpress.hostname";

	/**
	 * Property PID for logexpress.<br>
	 * LogExpress의 PID 속성.
	 */
	public static final String PROPERTY_PID = "logexpress.pid";

	private static final AtomicReference<ShutdownFuture> ShutdownFutureRef = new AtomicReference<ShutdownFuture>();

	private static final ReentrantLock lock = new ReentrantLock();
	private static final Object reInitMonitor = new Object();

	private static final List<BaseLogger> CachedBaseLoggers = new ArrayList<BaseLogger>();
	


	static AtomicReference<LoggerContext> LoggerContextRef = new AtomicReference<LoggerContext>();

	static {
		lock.lock();
		initLogger();
		lock.unlock();
	}

	/**
	 * Initializes the logger.<br>
	 * 로거를 초기화합니다.
	 */
	private synchronized static void initLogger() {
		LoggerContextRef.set(new LoggerContext(Configuration.fromDefaultConfigurationFile().close(), CachedBaseLoggers));
		CachedBaseLoggers.clear();
		System.setProperty(PROPERTY_PATH, ModulePathFinder.find(LogExpress.class).getAbsolutePath());
		System.setProperty(PROPERTY_HOSTNAME, SysTool.hostname());
		System.setProperty(PROPERTY_PID, SysTool.pid() + "");
		LoggerContextRef.get().startLogWriter();
	}

	/**
	 * Clones the current configuration.<br>
	 * 현재 구성을 복제합니다.
	 *
	 * @return the cloned configuration<br>
	 *         복제된 구성
	 */
	public static Configuration cloneConfiguration() {
		try {
			lock.lock();
			if (LoggerContextRef.get() == null) {
				initLogger();

			}
			LoggerContext loggerContext = LoggerContextRef.get();
			if (loggerContext == null) {
				return Configuration.fromDefaultConfigurationFile();
			} else {
				Configuration configuration = loggerContext.getConfiguration();
				if (configuration == null) {
					return Configuration.fromDefaultConfigurationFile();
				}
				return configuration.clone();
			}
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	/**
	 * Updates the logger configuration.<br>
	 * This method will shutdown the existing logger and start a new logger with the new configuration.<br>
	 * The configuration object passed as an argument cannot be reused.<br>
	 * 로거 구성을 업데이트 및 초기화 합니다.<br>
	 * 이 메서드를 호출하면 기존 구성으로 동작하던 로거는 로그 큐를 모두 비운 뒤에 종료됩니다.<br>
	 * 이 메서드 인자로 들어온 설정 객체는 재활용 할 수 없습니다.
	 *
	 * @param configure the new configuration<br>
	 *                  새로운 구성
	 */
	public static void updateConfig(Configuration configure) {
		if (configure == null) {
			configure = cloneConfiguration();
		} else if (configure.isClosed()) {
			throw new IllegalStateException("Configuration is closed. Please create a new Configuration or clone the existing one.");
		}

		lock.lock();
		if (configure.isDebug()) {
			InLogger.enable();
			InLogger.enableFile(configure.isDebugFileLogEnabled());
			InLogger.enableConsole(configure.isDebugConsoleLogEnabled());
		} else {
			InLogger.disable();
		}
		waitForShutdown();
		try {
			if (LoggerContextRef.get() == null) {
				initLogger();
			}
			final CountDownLatch latch = new CountDownLatch(1);
			if (InLogger.isEnabled()) {
				InLogger.DEBUG("Logger will be fully initialized.  (" + Thread.currentThread().getName() + ")");
			}
			configure.close();
			LoggerContext oldContext = LoggerContextRef.get();
			List<BaseLogger> oldLoggers = oldContext.getLoggerList();
			//BaseLogger baseLogger =  oldContext.defaultLogger();
			LoggerContext context = new LoggerContext(configure, oldLoggers);
			LoggerContextRef.set(context);
			ShutdownFuture shutdownFuture = oldContext.end();
			shutdownFuture.addOnEndCallback(new Runnable() {
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
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	/**
	 * Resets the logger configuration.<br>
	 * This method will shutdown the existing logger and start a new logger with the default configuration.<br>
	 * After calling this method, the existing logger will be shut down after emptying all log queues.<br>
	 * 로거 구성을 초기화합니다.<br>
	 * 이 메서드를 호출하면 기존 구성으로 동작하던 로거는 로그 큐를 모두 비운 뒤에 종료됩니다.<br>
	 * 이후 이전 설정으로 새로운 로거 객체가 생성되고 Writer 스레드가 시작됩니다.
	 */
	public static void reset() {
		LogExpress.updateConfig(cloneConfiguration());
	}

	/**
	 * Waits for the shutdown to complete.<br>
	 * 종료가 완료될 때까지 대기합니다.
	 */
	private static void waitForShutdown() {
		while (ShutdownFutureRef.get() != null) {
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
			public void addOnEndCallback(Runnable runnable) {
				runnable.run();
			}

			@Override
			public void await() {
			}
		};
	}


	/**
	 * Shuts down the logger.<br>
	 * You can check whether the logger is completely shut down through ShutdownFuture, or wait until the logger is shut down.<br>
	 * 로거를 종료합니다.<br>
	 * ShutdownFuture를 통하여 로거가 완전히 종료 되었는지 확인하거나, 로그거 종료되기 전까지 대기할 수 있습니다.
	 *
	 * @return ShutdownFuture
	 */
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
			final LoggerContext context = LoggerContextRef.get();
			ShutdownFuture shutdownFuture = context.end();
			ShutdownFutureRef.set(shutdownFuture);
			shutdownFuture.addOnEndCallback(new Runnable() {
				@Override
				public void run() {
					CachedBaseLoggers.clear();
					CachedBaseLoggers.addAll(context.getLoggerList());
					for(BaseLogger logger : CachedBaseLoggers) {
						logger.parking();
					}
					LoggerContextRef.set(null);
					ShutdownFutureRef.set(null);
					// 디버그 로그를 출력한다.
					resultFuture.onEnd();
				}
			});
			return new ShutdownFutureFacade(resultFuture);
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	/**
	 * Creates a new logger with a marker.<br>
	 * 마커와 함께 새로운 로거를 생성합니다.
	 *
	 * @param marker the marker for the logger<br>
	 *               새 로거의 마커
	 * @return the new logger<br>
	 *         새로운 로거
	 */
	public static Logger newLogger(String marker) {
		return new LoggerImpl(requireLoggerContext().logger(marker));
	}

	/**
	 * Creates a new logger.<br>
	 * 새로운 로거를 생성합니다.
	 *
	 * @return the new logger<br>
	 *         새로운 로거
	 */
	public static Logger newLogger() {
		return new LoggerImpl(requireLoggerContext().defaultLogger());
	}

	/**
	 * Creates a new logger with a caller class and marker.<br>
	 * 호출자 클래스와 마커와 함께 새로운 로거를 생성합니다.
	 *
	 * @param caller the caller class<br>
	 *               호출자 클래스
	 * @param marker the marker for the logger<br>
	 *               로거의 마커
	 * @return the new logger<br>
	 *         새로운 로거
	 */
	public static Logger newLogger(Class<?> caller, String marker) {

		return new LoggerWithCallerImpl(caller.getName(), requireLoggerContext().logger(marker));
	}

	/**
	 * Creates a new logger with a caller class.<br>
	 * 호출자 클래스와 함께 새로운 로거를 생성합니다.
	 *
	 * @param caller the caller class<br>
	 *               호출자 클래스
	 * @return the new logger<br>
	 *         새로운 로거
	 */
	public static Logger newLogger(Class<?> caller) {
		return new LoggerWithCallerImpl(caller.getName(), requireLoggerContext().defaultLogger());
	}

	/**
	 * Creates a new logger with a caller class and marker.<br>
	 * 호출자 클래스의 FQCN(Fully Qualified Class Name)와 마커 이름을 인자로 받아 새 로거를 생성합니다.
	 *
	 * @param callerFQCN the fully qualified caller name
	 *
	 * @param marker     the marker for the logger
	 *
	 * @return the new logger
	 *
	 */
	public static Logger newLogger(String callerFQCN, String marker) {
		LoggerContext context = requireLoggerContext();
		return new LoggerWithCallerImpl(callerFQCN, marker == null ? context.defaultLogger() : context.logger(marker));
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

	/**
	 * Gets the base logger.<br>
	 * 기본 로거를 가져옵니다.
	 *
	 * @return the base logger<br>
	 *         기본 로거
	 */
	public static BaseLogger baseLogger() {
		LoggerContext context = requireLoggerContext();

		return context.defaultLogger();
	}

	/**
	 * Gets the base logger with a marker.<br>
	 * 마커와 함께 기본 로거를 가져옵니다.
	 *
	 * @param marker the marker for the logger<br>
	 *               로거의 마커
	 * @return the base logger<br>
	 *         기본 로거
	 */
	public static BaseLogger baseLogger(String marker) {
		return requireLoggerContext().logger(marker);
	}

	/**
	 * Logs an info level message. Logs to the base logger.<br>
	 * 정보 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 */
	public static void info(CharSequence log) {
		baseLogger().info("", log, 1);
	}

	/**
	 * Logs an info level message with an exception. Logs to the base logger.<br>
	 * 예외와 함께 정보 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 * @param e   the exception to log<br>
	 *            로깅할 예외
	 */
	public static void info(CharSequence log, Throwable e) {
		baseLogger().info("", log, e, 1);
	}


	/**
	 * Logs an error level message. Logs to the base logger.<br>
	 * 오류 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 */
	public static void error(CharSequence log) {
		baseLogger().error("", log, 1);
	}

	/**
	 * Logs an error level message with an exception. Logs to the base logger.<br>
	 * 예외와 함께 오류 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 * @param e   the exception to log<br>
	 *            로깅할 예외
	 */
	public static void error(CharSequence log, Throwable e) {
		baseLogger().error("", log, e, 1);
	}

	/**
	 * Checks if info level logging is enabled.<br>
	 * 정보 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if info level logging is enabled, false otherwise<br>
	 *         정보 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isInfoEnabled()}
	 */
	@Deprecated
	public static boolean isInfo() {
		return baseLogger().isInfoEnabled();
	}

	/**
	 * Checks if info level logging is enabled.<br>
	 * 정보 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if info level logging is enabled, false otherwise<br>
	 *         정보 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isInfoEnabled() {
		return baseLogger().isInfoEnabled();
	}

	/**
	 * Checks if error level logging is enabled.<br>
	 * 오류 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if error level logging is enabled, false otherwise<br>
	 *         오류 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isErrorEnabled()}
	 */
	@Deprecated
	public static boolean isError() {
		return baseLogger().isErrorEnabled();
	}

	/**
	 * Checks if error level logging is enabled.<br>
	 * 오류 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if error level logging is enabled, false otherwise<br>
	 *         오류 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isErrorEnabled() {
		return baseLogger().isErrorEnabled();
	}

	/**
	 * Checks if debug level logging is enabled.<br>
	 * 디버그 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if debug level logging is enabled, false otherwise<br>
	 *         디버그 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isDebugEnabled()}
	 */
	@Deprecated
	public static boolean isDebug() {
		return baseLogger().isDebugEnabled();
	}

	/**
	 * Checks if debug level logging is enabled.<br>
	 * 디버그 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if debug level logging is enabled, false otherwise<br>
	 *         디버그 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isDebugEnabled() {
		return baseLogger().isDebugEnabled();
	}

	/**
	 * Checks if trace level logging is enabled.<br>
	 * 추적 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if trace level logging is enabled, false otherwise<br>
	 *         추적 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isTraceEnabled()}
	 */
	@Deprecated
	public static boolean isTrace() {
		return baseLogger().isTraceEnabled();
	}

	/**
	 * Checks if trace level logging is enabled.<br>
	 * 추적 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if trace level logging is enabled, false otherwise<br>
	 *         추적 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isTraceEnabled() {
		return baseLogger().isTraceEnabled();
	}

	/**
	 * Checks if warn level logging is enabled.<br>
	 * 경고 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if warn level logging is enabled, false otherwise<br>
	 *         경고 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isWarnEnabled()}
	 */
	@Deprecated
	public static boolean isWarn() {
		return baseLogger().isWarnEnabled();
	}

	/**
	 * Checks if warn level logging is enabled.<br>
	 * 경고 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if warn level logging is enabled, false otherwise<br>
	 *         경고 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isWarnEnabled() {
		return baseLogger().isWarnEnabled();
	}

	/**
	 * Logs a debug level message. Logs to the base logger.<br>
	 * 디버그 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 */
	public static void debug(CharSequence log) {
		baseLogger().debug("", log, 1);
	}

	/**
	 * Logs a debug level message with an exception. Logs to the base logger.<br>
	 * 예외와 함께 디버그 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 * @param e   the exception to log<br>
	 *            로깅할 예외
	 */
	public static void debug(CharSequence log, Throwable e) {
		baseLogger().debug("", log, e, 1);
	}

	/**
	 * Logs a trace level message. Logs to the base logger.<br>
	 * 추적 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 */
	public static void trace(CharSequence log) {
		baseLogger().trace("", log, 1);
	}

	/**
	 * Logs a trace level message with an exception. Logs to the base logger.<br>
	 * 예외와 함께 추적 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 * @param e   the exception to log<br>
	 *            로깅할 예외
	 */
	public static void trace(CharSequence log, Throwable e) {
		baseLogger().trace("", log, e, 1);
	}

	/**
	 * Logs a warn level message. Logs to the base logger.<br>
	 * 경고 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 */
	public static void warn(CharSequence log) {
		baseLogger().warn("", log, 1);
	}

	/**
	 * Logs a warn level message with an exception. Logs to the base logger.<br>
	 * 예외와 함께 경고 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log<br>
	 *            로깅할 메시지
	 * @param e   the exception to log<br>
	 *            로깅할 예외
	 */
	public static void warn(CharSequence log, Throwable e) {
		baseLogger().warn("", log, e, 1);
	}

	/**
	 * Checks if the logger is shutdown.<br>
	 * LogExpress 가 Shutdown 되었는지 확인합니다.
	 * @return true if LogExpress is shutdown, false otherwise <br>
	 * 	   LogExpress 가 Shutdown 되었으면 true, 그렇지 않으면 false
	 */
	public static boolean isShutdown() {
		return LoggerContextRef.get() == null;
	}


	private static LoggerContext requireLoggerContext() {
		LoggerContext loggerContext = LoggerContextRef.get();
		if(loggerContext == null) {
			InLogger.WARN("Logexpress is currently shut down. Please use the 'logexpress.updateConfig(Configuration)' method to reinitialize it.", false);
			return LoggerContext.EMPTY;
		}
		return loggerContext;
	}

}
