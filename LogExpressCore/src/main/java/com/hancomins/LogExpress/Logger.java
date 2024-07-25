


package com.hancomins.LogExpress;

/**
 * Interface representing a logger.
 * 로거의 인터페이스.
 *
 *
 */
public interface Logger {

	/**
	 * Gets the marker associated with the logger.
	 * 로거와 관련된 마커를 가져옵니다.
	 *
	 * @return the marker
	 *         마커
	 */
	public String getMarker();

	/**
	 * Gets the logging level of the logger.
	 * 로거의 로깅 레벨을 가져옵니다.
	 *
	 * @return the logging level
	 *         로깅 레벨
	 */
	public Level getLevel();

	/**
	 * Checks if info level logging is enabled.
	 * 정보 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if info level logging is enabled, false otherwise
	 *         정보 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isInfoEnabled()}
	 */
	@Deprecated
	public boolean isInfo();

	/**
	 * Checks if info level logging is enabled.
	 * 정보 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if info level logging is enabled, false otherwise
	 *         정보 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public boolean isInfoEnabled();

	/**
	 * Checks if error level logging is enabled.
	 * 오류 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if error level logging is enabled, false otherwise
	 *         오류 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isErrorEnabled()}
	 */
	@Deprecated
	public boolean isError();

	/**
	 * Checks if error level logging is enabled.
	 * 오류 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if error level logging is enabled, false otherwise
	 *         오류 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public boolean isErrorEnabled();

	/**
	 * Checks if debug level logging is enabled.
	 * 디버그 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if debug level logging is enabled, false otherwise
	 *         디버그 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isDebugEnabled()}
	 */
	@Deprecated
	public boolean isDebug();

	/**
	 * Checks if debug level logging is enabled.
	 * 디버그 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if debug level logging is enabled, false otherwise
	 *         디버그 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public boolean isDebugEnabled();

	/**
	 * Checks if trace level logging is enabled.
	 * 추적 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if trace level logging is enabled, false otherwise
	 *         추적 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public boolean isTraceEnabled();

	/**
	 * Checks if trace level logging is enabled.
	 * 추적 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if trace level logging is enabled, false otherwise
	 *         추적 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isTraceEnabled()}
	 */
	@Deprecated
	public boolean isTrace();

	/**
	 * Checks if warn level logging is enabled.
	 * 경고 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if warn level logging is enabled, false otherwise
	 *         경고 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public boolean isWarnEnabled();

	/**
	 * Checks if warn level logging is enabled.
	 * 경고 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if warn level logging is enabled, false otherwise
	 *         경고 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 * @deprecated use {@link #isWarnEnabled()}
	 */
	@Deprecated
	public boolean isWarn();

	/**
	 * Checks if fatal level logging is enabled.
	 * 치명적 레벨 로깅이 활성화되어 있는지 확인합니다.
	 *
	 * @return true if fatal level logging is enabled, false otherwise
	 *         치명적 레벨 로깅이 활성화되어 있으면 true, 그렇지 않으면 false
	 */
	public boolean isFatalEnabled();

	/**
	 * Sets the index of the added stack trace element.
	 * 추가된 스택 트레이스 요소의 인덱스를 설정합니다.
	 *
	 * @param addedStackTraceElementIndex the index to set
	 *                                    설정할 인덱스
	 */
	public void setAddedStackTraceElementIndex(int addedStackTraceElementIndex);

	/**
	 * Checks if the given logging level is loggable.
	 * 주어진 로깅 레벨이 로깅 가능한지 확인합니다.
	 *
	 * @param level the logging level to check
	 *              확인할 로깅 레벨
	 * @return true if the level is loggable, false otherwise
	 *         레벨이 로깅 가능하면 true, 그렇지 않으면 false
	 */
	public boolean isLoggable(Level level);

	/**
	 * Logs a trace level message.
	 * 추적 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 */
	public void trace(CharSequence messages);

	/**
	 * Logs a trace level message with arguments.
	 * 인수와 함께 추적 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param args     the arguments for the message
	 *                 메시지의 인수
	 */
	public void trace(String messages, Object... args);

	/**
	 * Logs a trace level message with an exception.
	 * 예외와 함께 추적 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param e        the exception to log
	 *                 로깅할 예외
	 */
	public void trace(CharSequence messages, Throwable e);

	/**
	 * Logs a debug level message.
	 * 디버그 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 */
	public void debug(CharSequence messages);

	/**
	 * Logs a debug level message with arguments.
	 * 인수와 함께 디버그 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param args     the arguments for the message
	 *                 메시지의 인수
	 */
	public void debug(String messages, Object... args);

	/**
	 * Logs a debug level message with an exception.
	 * 예외와 함께 디버그 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param e        the exception to log
	 *                 로깅할 예외
	 */
	public void debug(CharSequence messages, Throwable e);

	/**
	 * Logs an info level message.
	 * 정보 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 */
	public void info(CharSequence messages);

	/**
	 * Logs an info level message with arguments.
	 * 인수와 함께 정보 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param args     the arguments for the message
	 *                 메시지의 인수
	 */
	public void info(String messages, Object... args);

	/**
	 * Logs an info level message with an exception.
	 * 예외와 함께 정보 레벨 메시지를 로깅합니다.
	 *
	 * @param message the message to log
	 *                로깅할 메시지
	 * @param e       the exception to log
	 *                로깅할 예외
	 */
	public void info(CharSequence message, Throwable e);

	/**
	 * Logs a warn level message.
	 * 경고 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 */
	public void warn(CharSequence messages);

	/**
	 * Logs a warn level message with arguments.
	 * 인수와 함께 경고 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param args     the arguments for the message
	 *                 메시지의 인수
	 */
	public void warn(String messages, Object... args);

	/**
	 * Logs a warn level message with an exception.
	 * 예외와 함께 경고 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param e        the exception to log
	 *                 로깅할 예외
	 */
	public void warn(CharSequence messages, Throwable e);

	/**
	 * Logs an error level message.
	 * 오류 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 */
	public void error(CharSequence messages);

	/**
	 * Logs an error level message with arguments.
	 * 인수와 함께 오류 레벨 메시지를 로깅합니다.
	 *
	 * @param messages the message to log
	 *                 로깅할 메시지
	 * @param args     the arguments for the message
	 *                 메시지의 인수
	 */
	public void error(String messages, Object... args);

	/**
	 * Logs an error level message with an exception.
	 * 예외와 함께 오류 레벨 메시지를 로깅합니다.
	 *
	 * @param messageFormatter the message to log
	 *                         로깅할 메시지
	 * @param e                the exception to log
	 *                         로깅할 예외
	 */
	public void error(CharSequence messageFormatter, Throwable e);


	/**
	 * Logs a fatal level message.
	 * 치명적 레벨 메시지를 로깅합니다.
	 * @param messages the message to log
	 *                 로깅할 메시지
	 */
	public void fatal(CharSequence messages);

	/**
	 * Logs a fatal level message with arguments.
	 * 인수와 함께 치명적 레벨 메시지를 로깅합니다.
	 * @param messages  the message to log
	 *                  로깅할 메시지
	 * @param args 	the arguments for the message
	 *              메시지의 인수
	 */
	public void fatal(String messages, Object ... args);

	/**
	 * Logs a fatal level message with an exception.
	 * 예외와 함께 치명적 레벨 메시지를 로깅합니다.
	 * @param messageFormatter the message to log
	 *                         로깅할 메시지
 	 * @param e the exception to log
	 *          로깅할 예외
	 */
	public void fatal(CharSequence messageFormatter, Throwable e);


}
