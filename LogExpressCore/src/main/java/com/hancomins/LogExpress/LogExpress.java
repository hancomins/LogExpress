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

	/**
	 * Property path for LogExpress.
	 * LogExpress의 속성 경로.
	 */
	public static final String PROPERTY_PATH = "LogExpress.path";

	/**
	 * Property hostname for LogExpress.
	 * LogExpress의 호스트 이름 속성.
	 */
	public static final String PROPERTY_HOSTNAME = "LogExpress.hostname";

	/**
	 * Property PID for LogExpress.
	 * LogExpress의 PID 속성.
	 */
	public static final String PROPERTY_PID = "LogExpress.pid";

	private static final AtomicReference<ShutdownFuture> ShutdownFutureRef = new AtomicReference<>();

	private final static ReentrantLock lock = new ReentrantLock();

	static AtomicReference<LoggerContext> LoggerContextRef = new AtomicReference<>();

	static {
		lock.lock();
		initLogger();
		lock.unlock();
	}

	/**
	 * Initializes the logger.
	 * 로거를 초기화합니다.
	 */
	private static void initLogger() {
		LoggerContextRef.set(new LoggerContext(Configuration.fromDefaultConfigurationFile().close()));
		System.setProperty(PROPERTY_PATH, ModulePathFinder.find(LogExpress.class).getAbsolutePath());
		System.setProperty(PROPERTY_HOSTNAME, SysTool.hostname());
		System.setProperty(PROPERTY_PID, SysTool.pid() + "");
		LoggerContextRef.get().startLogWriter();
	}

	/**
	 * Clones the current configuration.
	 * 현재 구성을 복제합니다.
	 *
	 * @return the cloned configuration
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
	 * Updates the logger configuration.
	 * 로거 구성을 업데이트합니다.
	 *
	 * @param configure the new configuration
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
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	/**
	 * Waits for the shutdown to complete.
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

	/**
	 * Creates a shutdown future that indicates the end.
	 * 종료를 나타내는 종료 미래를 생성합니다.
	 *
	 * @return the shutdown future
	 *         종료 미래
	 */
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
			public void await() {
			}
		};
	}

	/**
	 * Shuts down the logger.
	 * 로거를 종료합니다.
	 *
	 * @return the shutdown future
	 *         종료 미래
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
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	/**
	 * Creates a new logger with a marker.
	 * 마커와 함께 새로운 로거를 생성합니다.
	 *
	 * @param marker the marker for the logger
	 *               로거의 마커
	 * @return the new logger
	 *         새로운 로거
	 */
	public static Logger newLogger(String marker) {
		return new LoggerImpl(requireLoggerContext().logger(marker));
	}

	/**
	 * Creates a new logger.
	 * 새로운 로거를 생성합니다.
	 *
	 * @return the new logger
	 *         새로운 로거
	 */
	public static Logger newLogger() {
		return new LoggerImpl(requireLoggerContext().defaultLogger());
	}

	/**
	 * Creates a new logger with a caller class and marker.
	 * 호출자 클래스와 마커와 함께 새로운 로거를 생성합니다.
	 *
	 * @param caller the caller class
	 *               호출자 클래스
	 * @param marker the marker for the logger
	 *               로거의 마커
	 * @return the new logger
	 *         새로운 로거
	 */
	public static Logger newLogger(Class<?> caller, String marker) {
		return new LoggerWithCallerImpl(caller.getName(), requireLoggerContext().logger(marker));
	}

	/**
	 * Creates a new logger with a caller class.
	 * 호출자 클래스와 함께 새로운 로거를 생성합니다.
	 *
	 * @param caller the caller class
	 *               호출자 클래스
	 * @return the new logger
	 *         새로운 로거
	 */
	public static Logger newLogger(Class<?> caller) {
		return new LoggerWithCallerImpl(caller.getName(), requireLoggerContext().defaultLogger());
	}

	/**
	 * Creates a new logger with a fully qualified caller name and marker.
	 * 완전히 자격을 갖춘 호출자 이름과 마커와 함께 새로운 로거를 생성합니다.
	 *
	 * @param callerFQCN the fully qualified caller name
	 *                   완전히 자격을 갖춘 호출자 이름
	 * @param marker     the marker for the logger
	 *                   로거의 마커
	 * @return the new logger
	 *         새로운 로거
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
	 * Gets the base logger.
	 * 기본 로거를 가져옵니다.
	 *
	 * @return the base logger
	 *         기본 로거
	 */
	public static BaseLogger baseLogger() {
		LoggerContext context = requireLoggerContext();
		return context.defaultLogger();
	}

	/**
	 * Gets the base logger with a marker.
	 * 마커와 함께 기본 로거를 가져옵니다.
	 *
	 * @param marker the marker for the logger
	 *               로거의 마커
	 * @return the base logger
	 *         기본 로거
	 */
	public static BaseLogger baseLogger(String marker) {
		return requireLoggerContext().logger(marker);
	}

	/**
	 * Logs an info level message. Logs to the base logger.
	 * 정보 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 */
	public static void info(CharSequence log) {
		baseLogger().info("", log, 1);
	}

	/**
	 * Logs an info level message with an exception. Logs to the base logger.
	 * 예외와 함께 정보 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 * @param e   the exception to log
	 *            로깅할 예외
	 */
	public static void info(CharSequence log, Throwable e) {
		baseLogger().info("", log, e, 1);
	}


	/**
	 * Logs an error level message. Logs to the base logger.
	 * 오류 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 */
	public static void error(CharSequence log) {
		baseLogger().error("", log, 1);
	}

	/**
	 * Logs an error level message with an exception. Logs to the base logger.
	 * 예외와 함께 오류 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 * @param e   the exception to log
	 *            로깅할 예외
	 */
	public static void error(CharSequence log, Throwable e) {
		baseLogger().error("", log, e, 1);
	}

	/**
	 * Checks if info level logging is enabled.
	 * 정보 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if info level logging is enabled, false otherwise
	 *         정보 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isInfoEnabled()}
	 */
	@Deprecated
	public static boolean isInfo() {
		return baseLogger().isInfoEnabled();
	}

	/**
	 * Checks if info level logging is enabled.
	 * 정보 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if info level logging is enabled, false otherwise
	 *         정보 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isInfoEnabled() {
		return baseLogger().isInfoEnabled();
	}

	/**
	 * Checks if error level logging is enabled.
	 * 오류 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if error level logging is enabled, false otherwise
	 *         오류 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isErrorEnabled()}
	 */
	@Deprecated
	public static boolean isError() {
		return baseLogger().isErrorEnabled();
	}

	/**
	 * Checks if error level logging is enabled.
	 * 오류 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if error level logging is enabled, false otherwise
	 *         오류 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isErrorEnabled() {
		return baseLogger().isErrorEnabled();
	}

	/**
	 * Checks if debug level logging is enabled.
	 * 디버그 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if debug level logging is enabled, false otherwise
	 *         디버그 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isDebugEnabled()}
	 */
	@Deprecated
	public static boolean isDebug() {
		return baseLogger().isDebugEnabled();
	}

	/**
	 * Checks if debug level logging is enabled.
	 * 디버그 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if debug level logging is enabled, false otherwise
	 *         디버그 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isDebugEnabled() {
		return baseLogger().isDebugEnabled();
	}

	/**
	 * Checks if trace level logging is enabled.
	 * 추적 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if trace level logging is enabled, false otherwise
	 *         추적 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isTraceEnabled()}
	 */
	@Deprecated
	public static boolean isTrace() {
		return baseLogger().isTraceEnabled();
	}

	/**
	 * Checks if trace level logging is enabled.
	 * 추적 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if trace level logging is enabled, false otherwise
	 *         추적 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isTraceEnabled() {
		return baseLogger().isTraceEnabled();
	}

	/**
	 * Checks if warn level logging is enabled.
	 * 경고 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if warn level logging is enabled, false otherwise
	 *         경고 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isWarnEnabled()}
	 */
	@Deprecated
	public static boolean isWarn() {
		return baseLogger().isWarnEnabled();
	}

	/**
	 * Checks if warn level logging is enabled.
	 * 경고 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if warn level logging is enabled, false otherwise
	 *         경고 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public static boolean isWarnEnabled() {
		return baseLogger().isWarnEnabled();
	}

	/**
	 * Logs a debug level message. Logs to the base logger.
	 * 디버그 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 */
	public static void debug(CharSequence log) {
		baseLogger().debug("", log, 1);
	}

	/**
	 * Logs a debug level message with an exception. Logs to the base logger.
	 * 예외와 함께 디버그 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 * @param e   the exception to log
	 *            로깅할 예외
	 */
	public static void debug(CharSequence log, Throwable e) {
		baseLogger().debug("", log, e, 1);
	}

	/**
	 * Logs a trace level message. Logs to the base logger.
	 * 추적 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 */
	public static void trace(CharSequence log) {
		baseLogger().trace("", log, 1);
	}

	/**
	 * Logs a trace level message with an exception. Logs to the base logger.
	 * 예외와 함께 추적 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 * @param e   the exception to log
	 *            로깅할 예외
	 */
	public static void trace(CharSequence log, Throwable e) {
		baseLogger().trace("", log, e, 1);
	}

	/**
	 * Logs a warn level message. Logs to the base logger. 
	 * 경고 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다. 
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 */
	public static void warn(CharSequence log) {
		baseLogger().warn("", log, 1);
	}

	/**
	 * Logs a warn level message with an exception. Logs to the base logger. Logs to the base logger.
	 * 예외와 함께 경고 레벨 메시지를 로깅합니다. 기본 로거에 로깅합니다.
	 *
	 * @param log the message to log
	 *            로깅할 메시지
	 * @param e   the exception to log
	 *            로깅할 예외
	 */
	public static void warn(CharSequence log, Throwable e) {
		baseLogger().warn("", log, e, 1);
	}

	private static LoggerContext requireLoggerContext() {
		LoggerContext loggerContext = LoggerContextRef.get();
		// LoggerContext가 상태 예외 발생. "shutdown 된 상태입니다. updateConfig 를 호출하여 다시 초기화 해야 합니다." 메시지 출력.
		if(loggerContext == null) {
			throw new IllegalStateException("LogExpress is currently shut down. Please use the 'LogExpress.updateConfig(Configuration)' method to reinitialize it.");
		}
		return loggerContext;
	}
	
}
