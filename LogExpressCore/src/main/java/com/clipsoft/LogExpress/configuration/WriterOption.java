package com.clipsoft.LogExpress.configuration;

import java.util.ArrayList;
import java.util.Arrays;

import com.clipsoft.LogExpress.Level;

public class WriterOption implements Cloneable {
	
	public final static String FULL_PATTERN = "{time::HH:mm:ss.SSS} [{level}] {caller} <{hostname}/PID:{pid}/{thread}:{tid}>  {marker}  | ({file}) {class-name}.{method}():{line} | {message}";
	
	public final static String DEFAULT_FILE_PATTERN = "./log.{hostname}.{date::yyyy-MM-dd}.{number}.txt";
	public final static Level DEFAULT_LEVEL = Level.INFO;
	public final static int DEFAULT_HISTORY = 60;
	public final static int DEFAULT_MAXSIZE = 512;
	public final static int DEFAULT_BUFFER_SIZE = 1024;
	public final static int DEFAULT_CLOSER_BUFFER_SIZE = 1024 * 1024 * 16;
	public final static int DEFAULT_ADDED_INDEX_OF_STACKTRACE_ELEMENTS = 1;
	
	
	private Level mLevel = DEFAULT_LEVEL;
	private ArrayList<WriterType> mWriterTypes = new ArrayList<WriterType>(
			Arrays.asList(new WriterType[] {  WriterType.Console }));
	private ArrayList<String> mMarkers = new ArrayList<String>();
	private int mBufferSize = DEFAULT_BUFFER_SIZE;
	private int mMaxSize = DEFAULT_MAXSIZE;
	private int mHistory = DEFAULT_HISTORY;
	private volatile boolean mIsClosed = false;
	private int mStackTraceDepth = DEFAULT_ADDED_INDEX_OF_STACKTRACE_ELEMENTS;
	private String mEncoding = null;
	private String mFile = DEFAULT_FILE_PATTERN;
	private String mPattern = FULL_PATTERN;


	private String mStaticVariableReplacedEncoding = null;
	private String mStaticVariableReplacedFile = null;
	private String mStaticVariableReplacedPattern = null;

	private String[] mStaticVariableReplacedMarkers = null;




	@Override
	protected WriterOption clone() {
		WriterOption option = new WriterOption();
		option.mLevel = mLevel;
		option.mWriterTypes = new ArrayList<WriterType>(mWriterTypes);
		option.mMarkers =  new ArrayList<String>(mMarkers);
		option.mBufferSize = mBufferSize;
		option.mMaxSize = mMaxSize;
		option.mHistory = mHistory;
		option.mIsClosed = false;
		option.mFile = mFile;
		option.mPattern = mPattern;
		option.mEncoding = mEncoding;
		option.mStackTraceDepth = mStackTraceDepth;
		option.mStaticVariableReplacedFile = null;
		option.mStaticVariableReplacedPattern = null;
		option.mStaticVariableReplacedEncoding = null;
		option.mStaticVariableReplacedMarkers = null;
		return option;
	}


	protected WriterOption() {

	}

	public Level getLevel() {
		return mLevel;
	}
	public void setLevel(Level mLevel) {
		if(mIsClosed) return;
		this.mLevel = mLevel;
	}

	public WriterType[] getWriterTypes() {
		return mWriterTypes.toArray(new WriterType[mWriterTypes.size()]);
	}

	public void clearWriterType() {
		if(mIsClosed) return;
		this.mWriterTypes.clear();
	}

	public void addWriterType(WriterType type) {
		if(mIsClosed) return;
		int idx = this.mWriterTypes.indexOf(type);
		if(idx > -1) {
			this.mWriterTypes.remove(idx);
		}
		this.mWriterTypes.add(type);
	}

	public String[] getMarkers() {
		if(!mIsClosed) mMarkers.toArray(new String[mMarkers.size()])	;
		if(mStaticVariableReplacedMarkers == null) {
			mStaticVariableReplacedMarkers = mMarkers.toArray(new String[mMarkers.size()]);
			for (int i = 0; i < mStaticVariableReplacedMarkers.length; i++) {
				mStaticVariableReplacedMarkers[i] = StaticVariableReplacer.replace(mStaticVariableReplacedMarkers[i]);
			}
		}
		return mStaticVariableReplacedMarkers;
	}

	public boolean containsMarker(String marker) {
		return mMarkers.contains(marker.trim());
	}

	public void addMarker(String defaultName) {
		if(mIsClosed) return;
		int idx = this.mMarkers.indexOf(defaultName);
		if(idx > -1) {
			//this.mMarkers.remove(idx);
			return;
		}
		this.mMarkers.add(defaultName);
	}

	public int getMaxSize() {
		return mMaxSize;
	}
	public void setMaxSize(int maxSize) {
		if(mIsClosed) return;
		this.mMaxSize = maxSize <= 0 ? Integer.MAX_VALUE : maxSize;
	}
	public void setBufferSize(int size) {
		if(mIsClosed) return;
		this.mBufferSize = size;
	}
	public int getHistory() {
		return mHistory;
	}
	public void setHistory(int history) {
		if(mIsClosed) return;
		this.mHistory = history <= 0 ? Integer.MAX_VALUE : history;
	}
	public int getBufferSize() {
		return mBufferSize;
	}
	public void setFile(String mFile) {
		if(mIsClosed) return;
		this.mFile = mFile;
	}
	public String getFile() {
		if(!mIsClosed) return mFile;
		if(mStaticVariableReplacedFile == null) {
			mStaticVariableReplacedFile = StaticVariableReplacer.replace(mFile);
		}
		return mStaticVariableReplacedFile;
	}
	public String getPattern() {
		if(!mIsClosed) return mPattern;
		if(mStaticVariableReplacedPattern == null) {
			mStaticVariableReplacedPattern = StaticVariableReplacer.replace(mPattern);
		}
		return mStaticVariableReplacedPattern;
	}

	public String getEncoding() {
		if(!mIsClosed) return mEncoding;
		if(mStaticVariableReplacedEncoding == null) {
			mStaticVariableReplacedEncoding = StaticVariableReplacer.replace(mEncoding);
		}
		return mStaticVariableReplacedEncoding;
	}

	public void setEncoding(String encoding) {
		if(mIsClosed) return;
		if(encoding != null && !encoding.isEmpty() && !ConfigurationParser.encodingTest(encoding)) {
			encoding = null;
		}
		mEncoding = encoding;
	}

	@Deprecated
	// @Deprecated since 0.10.3, use setStackTraceDepth instead.
	public void addStackTraceElementsIndex(int added) {
		if(mIsClosed) return;
		mStackTraceDepth = added;
	}

	public void setStackTraceDepth(int depth) {
		if(mIsClosed) return;
		mStackTraceDepth = depth;
	}

	@Deprecated
	//@Deprecated since 0.10.3, use getStackTraceDepth instead.
	public int getAddedStackTraceElementsIndex() {
		return mStackTraceDepth;
	}

	public int getStackTraceDepth() {
		return mStackTraceDepth;
	}


	public void setLinePattern(String linePattern) {
		if(mIsClosed) return;
		this.mPattern = linePattern;
	}

	@Deprecated
	public void setPattern(String linePattern) {
		setLinePattern(linePattern);
	}
	
	protected void close() {
		mIsClosed = true;
	}






	
}
