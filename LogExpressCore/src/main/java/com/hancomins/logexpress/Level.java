package com.hancomins.logexpress;

/**
 * Enum representing different logging levels.<br>
 * 다양한 로깅 레벨을 나타내는 열거형.<br>
 * Trace &lt; Debug &lt; Info &lt; Warn &lt; Error &lt; Fatal &lt; Off<br>
 * @author beom
 */
public enum Level {

	TRACE(0),
	DEBUG(1),
	INFO(2),
	WARN(3),
	ERROR(4),
	FATAL(5),
	OFF(6);

	private int value = -1;

	/**
	 * Constructor for Level enum.<br>
	 * Level 열거형의 생성자.<br>
	 *
	 * @param value the integer value associated with the logging level<br>
	 *              로깅 레벨에 해당하는 정수 값
	 */
	Level(int value) {
		this.value = value;
	}

	/**
	 * Converts a string to the corresponding Level enum.<br>
	 * 문자열을 해당하는 Level 열거형으로 변환합니다.<br>
	 *
	 * @param value the string representation of the logging level<br>
	 *              로깅 레벨의 문자열 표현<br>
	 * @return the corresponding Level enum, or INFO if the string is null or empty<br>
	 *         해당하는 Level 열거형, 문자열이 null 또는 비어있으면 INFO 반환
	 */
	public static Level stringValueOf(String value) {
		Level level = stringValueOrNull(value);
		if (level == null) {
			return INFO;
		}
		return level;
	}


	public static Level stringValueOrNull(String value) {
		if (value == null || value.isEmpty()) return null;
		value = value.trim();
		if (value.equalsIgnoreCase("info")) {
			return INFO;
		} else if (value.equalsIgnoreCase("trace")) {
			return TRACE;
		} else if (value.equalsIgnoreCase("debug")) {
			return DEBUG;
		} else if (value.equalsIgnoreCase("warn")) {
			return WARN;
		} else if (value.equalsIgnoreCase("error")) {
			return ERROR;
		} else if (value.equalsIgnoreCase("fatal")) {
			return FATAL;
		} else if (value.equalsIgnoreCase("off")) {
			return OFF;
		}
		return null;
	}

	/**
	 * Gets the integer value associated with the logging level.<br>
	 * 로깅 레벨에 해당하는 정수 값을 가져옵니다.<br>
	 *
	 * @return the integer value of the logging level<br>
	 *         로깅 레벨의 정수 값
	 */
	public int getValue() {
		return value;
	}

	/**
	 * 인자로 받은 레벨이 현재 레밸보다 낮은지 확인합니다.
	 */
	public boolean isLowerThan(Level level) {
		// null이면 Trace보다 낮다고 판단합니다.
		if(level == null) return true;
		return this.value >= level.value;
	}
}
