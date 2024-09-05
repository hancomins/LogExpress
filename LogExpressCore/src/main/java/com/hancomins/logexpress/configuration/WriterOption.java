package com.hancomins.logexpress.configuration;

import java.util.ArrayList;
import java.util.Arrays;

import com.hancomins.logexpress.Level;

/**
 * WriterOption은 로그를 기록할 때 사용하는 옵션을 설정합니다.<br>
 * 다른 로거 라이브러리의 Appender와 유사한 역할을 합니다.<br>
 * WriterOption sets the options used for logging.<br>
 * It functions similarly to Appenders in other logging libraries.
 *
 * @see com.hancomins.logexpress.configuration.Configuration
 * Author: Beom
 */
public class WriterOption implements Cloneable {

	public final static String FULL_PATTERN = "{time::HH:mm:ss.SSS} [{level}] {caller} &lt;{hostname}/PID:{pid}/{thread}:{tid}&gt;  {marker}  | ({file}) {class-name}.{method}():{line} | {message}";
	public final static String DEFAULT_FILE_PATTERN = "./log.{hostname}.{date::yyyy-MM-dd}.{number}.txt";

	public final static int DEFAULT_HISTORY = 60;
	public final static int DEFAULT_MAXSIZE = 512;
	public final static int DEFAULT_BUFFER_SIZE = 1024;
	public final static int DEFAULT_ADDED_INDEX_OF_STACKTRACE_ELEMENTS = 1;

	private Level level = null;
	private ArrayList<WriterType> writerTypes = new ArrayList<WriterType>(Arrays.asList(new WriterType[] { WriterType.Console }));
	private ArrayList<String> markers = new ArrayList<String>();
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private int maxSize = DEFAULT_MAXSIZE;
	private int history = DEFAULT_HISTORY;
	private volatile boolean isClosed = false;
	private int stackTraceDepth = DEFAULT_ADDED_INDEX_OF_STACKTRACE_ELEMENTS;
	private String encoding = null;
	private String file = DEFAULT_FILE_PATTERN;
	private String pattern = FULL_PATTERN;

	private String staticVariableReplacedEncoding = null;
	private String staticVariableReplacedFile = null;
	private String staticVariableReplacedPattern = null;
	private String[] staticVariableReplacedMarkers = null;

	private ColorOption colorOption = new ColorOption();

	@Override
	public WriterOption clone() {
		WriterOption option;
		try {
			option = (WriterOption) super.clone();
		} catch (CloneNotSupportedException e) {
			option = new WriterOption();
		}

		option.level = this.level;
		option.writerTypes = new ArrayList<WriterType>(this.writerTypes);
		option.markers = new ArrayList<String>(this.markers);
		option.bufferSize = this.bufferSize;
		option.maxSize = this.maxSize;
		option.history = this.history;
		option.isClosed = false;
		option.file = this.file;
		option.pattern = this.pattern;
		option.encoding = this.encoding;
		option.stackTraceDepth = this.stackTraceDepth;
		option.staticVariableReplacedFile = null;
		option.staticVariableReplacedPattern = null;
		option.staticVariableReplacedEncoding = null;
		option.staticVariableReplacedMarkers = null;
		colorOption = this.colorOption.clone();
		return option;
	}

	protected WriterOption() {}

	/**
	 * 로그 레벨을 반환합니다.<br>
	 * Returns the log level.
	 *
	 * @return 로그 레벨<br>
	 *         the log level
	 */
	public Level getLevel() {
		return this.level;
	}

	/**
	 * 로그 레벨을 설정합니다. 기본값은 INFO입니다.<br>
	 * Sets the log level. The default is INFO.
	 *
	 * @param level 로그 레벨<br>
	 *              the log level
	 */
	public void setLevel(Level level) {
		if(this.isClosed) return;
		this.level = level;
	}

	/**
	 * 설정된 WriterType을 가져옵니다.<br>
	 * Returns the configured WriterTypes.
	 *
	 * @return WriterType 배열<br>
	 *         array of WriterTypes
	 */
	public WriterType[] getWriterTypes() {
		return this.writerTypes.toArray(new WriterType[0]);
	}

	/**
	 * 설정된 WriterType들을 초기화합니다.<br>
	 * Clears the configured WriterTypes.
	 */
	public void clearWriterType() {
		if(this.isClosed) return;
		this.writerTypes.clear();
	}

	/**
	 * WriterType을 추가합니다.<br>
	 * 이미 추가된 WriterType은 중복 추가되지 않습니다.<br>
	 * 삭제하려면 clearWriterType()을 호출한 후 다시 추가해야 합니다.<br>
	 * Adds a WriterType.<br>
	 * Once added, a WriterType is not added again if it already exists.<br>
	 * To remove it, call clearWriterType() and then add it again.
	 *
	 * @param type WriterType
	 */
	public void addWriterType(WriterType type) {
		if(this.isClosed) return;
		int idx = this.writerTypes.indexOf(type);
		if(idx > -1) {
			this.writerTypes.remove(idx);
		}
		this.writerTypes.add(type);
	}

	/**
	 * 컬러 옵션의 인스턴스를 가져옵니다.<br>
	 * Returns the instance of the color option.
	 * {@link ColorOption}
	 * @return 컬러 옵션<br>
	 */
	public ColorOption colorOption() {
		return this.colorOption;
	}

	void setColorOption(ColorOption colorOption) {
		this.colorOption = colorOption;
	}

	/**
	 * 마커 목록을 가져옵니다.<br>
	 * Returns the list of markers.
	 *
	 * @return 마커 목록<br>
	 *         array of markers
	 */
	public String[] getMarkers() {
		if(!this.isClosed) return this.markers.toArray(new String[0]);
		if(this.staticVariableReplacedMarkers == null) {
			this.staticVariableReplacedMarkers = this.markers.toArray(new String[0]);
			for (int i = 0; i < this.staticVariableReplacedMarkers.length; i++) {
				this.staticVariableReplacedMarkers[i] = StaticVariableReplacer.replace(this.staticVariableReplacedMarkers[i]);
			}
		}
		return this.staticVariableReplacedMarkers;
	}

	/**
	 * 마커 이름이 포함되어 있는지 확인합니다.<br>
	 * Checks if the marker name is included.
	 *
	 * @param marker 마커 이름<br>
	 *               the marker name
	 * @return 포함 여부<br>
	 *         whether the marker is included
	 */
	@SuppressWarnings("unused")
	public boolean containsMarker(String marker) {
		return this.markers.contains(marker.trim());
	}

	/**
	 * 마커를 추가합니다.<br>
	 * Adds a marker.
	 *
	 * @param markerName 마커 이름<br>
	 *                   the marker name
	 */
	public void addMarker(String markerName) {
		if(this.isClosed) return;
		int idx = this.markers.indexOf(markerName);
		if(idx > -1) {
			return;
		}
		this.markers.add(markerName);
	}

	/**
	 * 설정된 로그 파일의 최대 크기를 반환합니다.<br>
	 * Returns the maximum size of the configured log file.
	 *
	 * @return 로그 파일의 최대 크기 (MiB)<br>
	 *         maximum log file size (MiB)
	 */
	public int getMaxSize() {
		return this.maxSize;
	}

	/**
	 * 로그 파일의 최대 크기를 설정합니다. MiB 단위.<br>
	 * 최대 크기를 초과하면 새 파일을 생성합니다. 기본값은 512 MiB입니다.<br>
	 * Sets the maximum size of the log file in MiB.<br>
	 * A new file is created if the maximum size is exceeded. The default is 512 MiB.
	 *
	 * @param maxSize 로그 파일의 최대 크기 (MiB)<br>
	 *                maximum log file size (MiB)
	 */
	public void setMaxSize(int maxSize) {
		if(this.isClosed) return;
		this.maxSize = maxSize <= 0 ? Integer.MAX_VALUE : maxSize;
	}

	/**
	 * 로그 파일에 기록하기 위한 버퍼 크기를 설정합니다.<br>
	 * Sets the buffer size for writing logs to the file.<br>
	 * 기본값은 1024 byte입니다.<br>
	 * The default is 1024 bytes.
	 *
	 * @param size 버퍼 크기 (byte)<br>
	 *             buffer size (bytes)
	 */
	public void setBufferSize(int size) {
		if(this.isClosed) return;
		this.bufferSize = size;
	}

	/**
	 * 로그 파일의 최대 보관 기간을 설정합니다.<br>
	 * Sets the maximum retention period for log files.<br>
	 * 기본값은 60일입니다.<br>
	 * The default is 60 days.
	 *
	 * @param history 보관 기간 (일)<br>
	 *                retention period (days)
	 */
	public void setHistory(int history) {
		if(this.isClosed) return;
		this.history = history <= 0 ? Integer.MAX_VALUE : history;
	}

	/**
	 * 설정된 로그 파일의 최대 보관 기간을 반환합니다.<br>
	 * Returns the configured maximum retention period for log files.
	 *
	 * @return 보관 기간 (일)<br>
	 *         retention period (days)
	 */
	public int getHistory() {
		return this.history;
	}

	/**
	 * 설정된 버퍼 크기를 반환합니다.<br>
	 * Returns the configured buffer size.
	 *
	 * @return 버퍼 크기 (byte)<br>
	 *         buffer size (bytes)
	 */
	public int getBufferSize() {
		return this.bufferSize;
	}

	/**
	 * 파일로 로그를 기록할 때 사용할 파일 경로와 패턴을 설정합니다.<br>
	 * Sets the file path and pattern to use when logging to a file.<br>
	 * 기본값은 "./log.{hostname}.{date::yyyy-MM-dd}.{number}.txt"입니다.<br>
	 * The default is "./log.{hostname}.{date::yyyy-MM-dd}.{number}.txt".
	 *
	 * @param file 파일 경로와 패턴<br>
	 *             file path and pattern
	 */
	public void setFile(String file) {
		if(this.isClosed) return;
		this.file = file;
	}

	/**
	 * 파일로 로그를 기록할 때 사용할 파일 경로와 패턴을 반환합니다.<br>
	 * Returns the file path and pattern used for logging to a file.
	 *
	 * @return 파일 경로와 패턴<br>
	 *         file path and pattern
	 */
	public String getFile() {
		if(!this.isClosed) return this.file;
		if(this.staticVariableReplacedFile == null) {
			this.staticVariableReplacedFile = StaticVariableReplacer.replace(this.file);
		}
		return this.staticVariableReplacedFile;
	}

	/**
	 * 로그 라인 패턴을 설정합니다.<br>
	 * 기본값은 {time::HH:mm:ss.SSS} [{level}] {caller} &lt;{hostname}/PID:{pid}/{thread}:{tid}&gt;  {marker}  | ({file}) {class-name}.{method}():{line} | {message} 입니다.<br>
	 * Sets the log line pattern.<br>
	 * The default is {time::HH:mm:ss.SSS} [{level}] {caller} &lt;{hostname}/PID:{pid}/{thread}:{tid}&gt;  {marker}  | ({file}) {class-name}.{method}():{line} | {message}.
	 *
	 * @param linePattern 로그 라인 패턴<br>
	 *                    log line pattern
	 */
	public void setLinePattern(String linePattern) {
		if(this.isClosed) return;
		this.pattern = linePattern;
	}

	/**
	 * 로그 라인 패턴을 반환합니다.<br>
	 * Returns the log line pattern.
	 *
	 * @return 로그 라인 패턴<br>
	 *         log line pattern
	 */
	public String getPattern() {
		if(!this.isClosed) return this.pattern;
		if(this.staticVariableReplacedPattern == null) {
			this.staticVariableReplacedPattern = StaticVariableReplacer.replace(this.pattern);
		}
		return this.staticVariableReplacedPattern;
	}

	/**
	 * 파일로 로그를 기록할 때 사용할 인코딩을 설정합니다.<br>
	 * Sets the encoding to use when logging to a file.<br>
	 * null을 설정하면 시스템 기본 인코딩을 사용합니다. 기본값은 null입니다.<br>
	 * If null, the system default encoding is used. The default is null.
	 *
	 * @param encoding 인코딩 이름<br>
	 *                 encoding name
	 */
	public void setEncoding(String encoding) {
		if(this.isClosed) return;
		if(encoding != null && !encoding.isEmpty() && !ConfigurationParser.encodingTest(encoding)) {
			encoding = null;
		}
		this.encoding = encoding;
	}

	/**
	 * 설정된 인코딩을 반환합니다.<br>
	 * Returns the configured encoding.
	 *
	 * @return 인코딩<br>
	 *         encoding
	 */
	public String getEncoding() {
		if(!this.isClosed) return this.encoding;
		if(this.staticVariableReplacedEncoding == null) {
			this.staticVariableReplacedEncoding = StaticVariableReplacer.replace(this.encoding);
		}
		return this.staticVariableReplacedEncoding;
	}

	/**
	 * @deprecated {@link #setLinePattern(String)} 사용 권장<br>
	 *             Recommended to use {@link #setLinePattern(String)}
	 *
	 * @param linePattern 로그 라인 패턴<br>
	 *                    log line pattern
	 */
	@Deprecated
	public void setPattern(String linePattern) {
		this.setLinePattern(linePattern);
	}

	/**
	 * 스택 트레이스 깊이를 설정합니다.<br>
	 * Sets the stack trace depth.<br>
	 * 메서드 이름 혹은 라인 번호를 출력할 때, 로거가 Wrapper로 감싸져 있을 경우 StackTrace의 인덱스를 설정하여 정확한 정보를 출력할 수 있습니다.<br>
	 * 기본값은 1입니다.<br>
	 * When printing method names or line numbers, setting the index of the StackTrace can provide accurate information if the logger is wrapped.<br>
	 * The default is 1.
	 *
	 * @param depth 스택 트레이스 깊이<br>
	 *              stack trace depth
	 */
	public void setStackTraceDepth(int depth) {
		if(this.isClosed) return;
		this.stackTraceDepth = depth;
	}

	/**
	 * @deprecated since 0.10.3, use getStackTraceDepth instead.
	 */
	@Deprecated
	public int getAddedStackTraceElementsIndex() {
		return this.stackTraceDepth;
	}

	/**
	 * 설정된 스택 트레이스 깊이를 반환합니다.<br>
	 * Returns the configured stack trace depth.
	 *
	 * @return 스택 트레이스 깊이<br>
	 *         stack trace depth
	 */
	public int getStackTraceDepth() {
		return this.stackTraceDepth;
	}

	/**
	 * 설정을 종료합니다. 설정이 종료되면 더 이상 수정할 수 없습니다.<br>
	 * Closes the configuration. Once closed, it cannot be modified.
	 */
	protected void close() {
		this.isClosed = true;
	}
}
