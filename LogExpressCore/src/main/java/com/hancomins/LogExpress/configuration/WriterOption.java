package com.hancomins.LogExpress.configuration;

import java.util.ArrayList;
import java.util.Arrays;

import com.hancomins.LogExpress.Level;

public class WriterOption implements Cloneable {
	
	public final static String FULL_PATTERN = "{time::HH:mm:ss.SSS} [{level}] {caller} <{hostname}/PID:{pid}/{thread}:{tid}>  {marker}  | ({file}) {class-name}.{method}():{line} | {message}";
	
	public final static String DEFAULT_FILE_PATTERN = "./log.{hostname}.{date::yyyy-MM-dd}.{number}.txt";
	public final static Level DEFAULT_LEVEL = Level.INFO;
	public final static int DEFAULT_HISTORY = 60;
	public final static int DEFAULT_MAXSIZE = 512;
	public final static int DEFAULT_BUFFER_SIZE = 1024;
	//public final static int DEFAULT_CLOSER_BUFFER_SIZE = 1024 * 1024 * 16;
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

	public Level getLevel() {
		return level;
	}
	public void setLevel(Level mLevel) {
		if(isClosed) return;
		this.level = mLevel;
	}

	public WriterType[] getWriterTypes() {
		return writerTypes.toArray(new WriterType[0]);
	}

	public void clearWriterType() {
		if(isClosed) return;
		this.writerTypes.clear();
	}

	public void addWriterType(WriterType type) {
		if(isClosed) return;
		int idx = this.writerTypes.indexOf(type);
		if(idx > -1) {
			this.writerTypes.remove(idx);
		}
		this.writerTypes.add(type);
	}

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

	@SuppressWarnings("unused")
    public boolean containsMarker(String marker) {
		return markers.contains(marker.trim());
	}

	public void addMarker(String defaultName) {
		if(isClosed) return;
		int idx = this.markers.indexOf(defaultName);
		if(idx > -1) {
			//this.mMarkers.remove(idx);
			return;
		}
		this.markers.add(defaultName);
	}

	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		if(isClosed) return;
		this.maxSize = maxSize <= 0 ? Integer.MAX_VALUE : maxSize;
	}
	public void setBufferSize(int size) {
		if(isClosed) return;
		this.bufferSize = size;
	}
	public int getHistory() {
		return history;
	}
	public void setHistory(int history) {
		if(isClosed) return;
		this.history = history <= 0 ? Integer.MAX_VALUE : history;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	public void setFile(String mFile) {
		if(isClosed) return;
		this.file = mFile;
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

	public void setEncoding(String encoding) {
		if(isClosed) return;
		if(encoding != null && !encoding.isEmpty() && !ConfigurationParser.encodingTest(encoding)) {
			encoding = null;
		}
		encoding = encoding;
	}

	@Deprecated
	// @Deprecated since 0.10.3, use setStackTraceDepth instead.
	public void addStackTraceElementsIndex(int added) {
		if(isClosed) return;
		stackTraceDepth = added;
	}

	public void setStackTraceDepth(int depth) {
		if(isClosed) return;
		stackTraceDepth = depth;
	}

	@Deprecated
	//@Deprecated since 0.10.3, use getStackTraceDepth instead.
	public int getAddedStackTraceElementsIndex() {
		return stackTraceDepth;
	}

	public int getStackTraceDepth() {
		return stackTraceDepth;
	}


	public void setLinePattern(String linePattern) {
		if(isClosed) return;
		this.pattern = linePattern;
	}

	@Deprecated
	public void setPattern(String linePattern) {
		setLinePattern(linePattern);
	}
	
	protected void close() {
		isClosed = true;
	}






	
}
