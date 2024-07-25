package com.hancomins.LogExpress.configuration;

import java.util.ArrayList;
import java.util.Arrays;

import com.hancomins.LogExpress.Level;

/**
 * WriterOption은 로그를 기록할 때 사용하는 옵션을 설정합니다.
 * 다른 로거 라이브러리의 Appender와 유사한 역할을 합니다.
 * @see com.hancomins.LogExpress.configuration.Configuration
 * @author beom
 */
public class WriterOption implements Cloneable {
	
	public final static String FULL_PATTERN = "{time::HH:mm:ss.SSS} [{level}] {caller} <{hostname}/PID:{pid}/{thread}:{tid}>  {marker}  | ({file}) {class-name}.{method}():{line} | {message}";

	public final static String DEFAULT_FILE_PATTERN = "./log.{hostname}.{date::yyyy-MM-dd}.{number}.txt";
	public final static Level DEFAULT_LEVEL = Level.INFO;
	public final static int DEFAULT_HISTORY = 60;
	public final static int DEFAULT_MAXSIZE = 512;
	public final static int DEFAULT_BUFFER_SIZE = 1024;
	public final static int DEFAULT_ADDED_INDEX_OF_STACKTRACE_ELEMENTS = 1;


	private Level level = DEFAULT_LEVEL;
	private ArrayList<WriterType> writerTypes = new ArrayList<WriterType>(
			Arrays.asList(new WriterType[] {  WriterType.Console }));
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




	@Override
	public WriterOption clone() {
        WriterOption option;
        try {
			option =(WriterOption) super.clone();
        } catch (CloneNotSupportedException e) {
			option = new WriterOption();
        }

        option.level = level;
		option.writerTypes = new ArrayList<WriterType>(writerTypes);
		option.markers =  new ArrayList<String>(markers);
		option.bufferSize = bufferSize;
		option.maxSize = maxSize;
		option.history = history;
		option.isClosed = false;
		option.file = file;
		option.pattern = pattern;
		option.encoding = encoding;
		option.stackTraceDepth = stackTraceDepth;
		option.staticVariableReplacedFile = null;
		option.staticVariableReplacedPattern = null;
		option.staticVariableReplacedEncoding = null;
		option.staticVariableReplacedMarkers = null;
		return option;
	}


	protected WriterOption() {

	}

	/**
	 * 로그 레벨을 반환한다.
	 * @return 로그 레벨
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * 로그 레벨을 설정한다.
	 * 기본값 INFO
	 * @param Level 로그 레벨
	 */
	public void setLevel(Level Level) {
		if(isClosed) return;
		this.level = Level;
	}

	/**
	 * 설정된 WriterType을 가져옵니다.
	 * @return WriterType 배열
	 */
	public WriterType[] getWriterTypes() {
		return writerTypes.toArray(new WriterType[0]);
	}

	/**
	 * 설정된 WriterType 들을 초기화 합니다.
	 */
	public void clearWriterType() {
		if(isClosed) return;
		this.writerTypes.clear();
	}

	/**
	 * WriterType을 추가합니다.
	 * 한번 추가된 WriterType은 중복 추가되지 않습니다.
	 * 삭제를 원할 경우 clearWriterType()를 호출한 후 다시 추가해야 합니다.
	 * @param type WriterType
	 */
	public void addWriterType(WriterType type) {
		if(isClosed) return;
		int idx = this.writerTypes.indexOf(type);
		if(idx > -1) {
			this.writerTypes.remove(idx);
		}
		this.writerTypes.add(type);
	}

	/**
	 * 마커 목록을 가져옵니다.
	 * @return
	 */
	public String[] getMarkers() {
		if(!isClosed) markers.toArray(new String[0])	;
		if(staticVariableReplacedMarkers == null) {
			staticVariableReplacedMarkers = markers.toArray(new String[0]);
			for (int i = 0; i < staticVariableReplacedMarkers.length; i++) {
				staticVariableReplacedMarkers[i] = StaticVariableReplacer.replace(staticVariableReplacedMarkers[i]);
			}
		}
		return staticVariableReplacedMarkers;
	}

	/**
	 * 마커 이름이 포함되어 있는지 확인합니다.
	 * @param marker 마커 이름
	 * @return 포함 여부
	 */
	@SuppressWarnings("unused")
    public boolean containsMarker(String marker) {
		return markers.contains(marker.trim());
	}

	/**
	 * 마커를 추가합니다.
	 * @param markerName 마커 이름
	 */
	public void addMarker(String markerName) {
		if(isClosed) return;
		int idx = this.markers.indexOf(markerName);
		if(idx > -1) {
			//this.mMarkers.remove(idx);
			return;
		}
		this.markers.add(markerName);
	}

	/**
	 * 설정된 로그 파일의 최대 크기를 반환합니다.
	 * @return 로그 파일의 최대 크기 Mibyte 단위
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * 로그 파일의 최대 크기를 설정합니다. Mibyte 단위.
	 * 최대 크기를 초과하면 파일을 새로 생성합니다.
	 * 기본값 512Mibyte
	 * @param maxSize 로그 파일의 최대 크기 (Mibyte)
	 */
	public void setMaxSize(int maxSize) {
		if(isClosed) return;
		this.maxSize = maxSize <= 0 ? Integer.MAX_VALUE : maxSize;
	}

	/**
	 * 로그 파일에 기록하기 위한 버퍼 크기를 설정합니다.
	 * 기본값 1024byte
	 * @param size 버퍼 크기 (byte)
	 */
	public void setBufferSize(int size) {
		if(isClosed) return;
		this.bufferSize = size;
	}
	public int getHistory() {
		return history;
	}

	/**
	 * 로그 파일의 최대 보관 기간을 설정합니다.
	 * 기본값 60일
	 * @param history 보관 기간 (일)
	 */
	public void setHistory(int history) {
		if(isClosed) return;
		this.history = history <= 0 ? Integer.MAX_VALUE : history;
	}
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * 파일로 로그를 기록할 때 사용할 파일 경로와 패턴을 설정합니다.
	 * 기본값 "./log.{hostname}.{date::yyyy-MM-dd}.{number}.txt"
	 * @param file 파일 경로와 패턴
	 */
	public void setFile(String file) {
		if(isClosed) return;
		this.file = file;
	}
	public String getFile() {
		if(!isClosed) return file;
		if(staticVariableReplacedFile == null) {
			staticVariableReplacedFile = StaticVariableReplacer.replace(file);
		}
		return staticVariableReplacedFile;
	}
	public String getPattern() {
		if(!isClosed) return pattern;
		if(staticVariableReplacedPattern == null) {
			staticVariableReplacedPattern = StaticVariableReplacer.replace(pattern);
		}
		return staticVariableReplacedPattern;
	}

	public String getEncoding() {
		if(!isClosed) return encoding;
		if(staticVariableReplacedEncoding == null) {
			staticVariableReplacedEncoding = StaticVariableReplacer.replace(encoding);
		}
		return staticVariableReplacedEncoding;
	}

	/**
	 * 파일로 로그를 기록할 때 사용할 인코딩을 설정합니다.
	 * null을 설정하면 시스템 기본 인코딩을 사용합니다.
	 * 기본값 null
	 * @param encoding 인코딩 이름
	 */
	public void setEncoding(String encoding) {
		if(isClosed) return;
		if(encoding != null && !encoding.isEmpty() && !ConfigurationParser.encodingTest(encoding)) {
			encoding = null;
		}
		this.encoding = encoding;
	}

	@Deprecated
	// @Deprecated since 0.10.3, use setStackTraceDepth instead.
	public void addStackTraceElementsIndex(int added) {
		if(isClosed) return;
		stackTraceDepth = added;
	}

	/**
	 * 스택 트레이스 깊이를 설정합니다.
	 * 메서드 이름 혹은 라인 번호를 출력할 때, 로거가 Wrapper로 감싸져 있을 경우 StackTrace의 인덱스를 설정하여 제대로된 정보를 출력할 수 있습니다.
	 * 기본값 1
	 * @param depth 스택 트레이스 깊이
	 */
	public void setStackTraceDepth(int depth) {
		if(isClosed) return;
		stackTraceDepth = depth;
	}

	@Deprecated
	//@Deprecated since 0.10.3, use getStackTraceDepth instead.
	public int getAddedStackTraceElementsIndex() {
		return stackTraceDepth;
	}

	/**
	 * 설정된 스택 트레이스 깊이를 반환합니다.
	 * @return 스택 트레이스 깊이
	 */
	public int getStackTraceDepth() {
		return stackTraceDepth;
	}


	/**
	 * 로그 라인 패턴을 설정합니다.
	 * @param linePattern 로그 라인 패턴
	 */
	public void setLinePattern(String linePattern) {
		if(isClosed) return;
		this.pattern = linePattern;
	}

	/**
	 * @deprecated {@link #setLinePattern(String)} 사용 권장
	 * @param linePattern 로그 라인 패턴
	 */
	@Deprecated
	public void setPattern(String linePattern) {
		setLinePattern(linePattern);
	}

	protected void close() {
		isClosed = true;
	}






	
}
