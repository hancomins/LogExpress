


package com.clipsoft.LogExpress;

import com.clipsoft.LogExpress.configuration.WriterOption;
import com.clipsoft.LogExpress.queue.AbsLineQueue;


public class BaseLogger implements Logger {
	
	
	protected final static int DEF_ELEMENT_IDX = 2; 
	
	private final String mMarker;
	private volatile LineFormatter mFormatter = null;
	private volatile AbsLineQueue mAbsLineQueue = null;
	private volatile Level mLevel = Level.INFO;
	private volatile int mStackTraceElementsIndex = 0;
	
	private boolean mIsInfo = false;
	private boolean mIsError = false;
	private boolean mIsDebug = false;
	private boolean mIsTrace = false;
	private boolean mIsWarn = false;
	
	
	//protected Logger(String marker, Level level, String format, int added) {
	protected BaseLogger(String marker, WriterOption option) {
		mMarker = marker;
		mLevel =  option.getLevel();
		initFormatter(option.getPattern());
		setLevel(option.getLevel());
		mStackTraceElementsIndex = DEF_ELEMENT_IDX + option.getStackTraceDepth();
	}
	
	
	protected void change(AbsLineQueue concurrentLineQueue, WriterOption option) {
		
		mAbsLineQueue = concurrentLineQueue;
		mLevel = option.getLevel();
		initFormatter(option.getPattern());
		mStackTraceElementsIndex = DEF_ELEMENT_IDX + option.getStackTraceDepth();
	}
	
	
	
	protected AbsLineQueue getLineQueue() {
		return mAbsLineQueue;
	}
	
	private void initFormatter(String pattern) {
		mFormatter = LineFormatter.parse(pattern);
	}
	
	protected void setLineQueue(AbsLineQueue queue) {
		mAbsLineQueue = queue;
	}
	
	protected void setLevel(Level lv) {
		int lvValue = lv.getValue();
		mIsDebug = lvValue <= Level.DEBUG.getValue();
		mIsTrace = lvValue <= Level.TRACE.getValue();
		mIsInfo = lvValue <= Level.INFO.getValue();
		mIsWarn = lvValue <= Level.WARN.getValue();
		mIsError = lvValue <= Level.ERROR.getValue();
	}
	
	public String getMarker() {
		return mMarker;
	}
	
	public Level getLevel() {
		return mLevel;
	}
	
	@Deprecated 
	public boolean isInfo() {
		return mIsInfo;
	}
	
	public boolean isInfoEnabled() {
		return mIsInfo;
	}
	
	@Deprecated 
	public boolean isError() {
		return mIsError;
	}
	
	public boolean isErrorEnabled() {
		return mIsError;
	}
	@Deprecated 
	public boolean isDebug() {
		return mIsDebug;
	}
	
	public boolean isDebugEnabled() {
		return mIsDebug;
	}
	
	
	public boolean isTraceEnabled() {
		return mIsTrace;
	}
	
	@Deprecated 
	public boolean isTrace() {
		return mIsDebug;
	}
	
	public boolean isWarnEnabled() {
		return mIsWarn;
	}
	@Deprecated
	public boolean isWarn() {
		return mIsWarn;
	}

	@Override
	public void setAddedStackTraceElementIndex(int addedStackTraceElementIndex) {
		mStackTraceElementsIndex = DEF_ELEMENT_IDX + addedStackTraceElementIndex + mStackTraceElementsIndex;
	}


	public boolean isLoggable(Level level) {
		return level.getValue() <= mLevel.getValue();
	}
	

	
	


	public void info(CharSequence messages) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,Level.INFO,mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	@Override
	public void info(String messages, Object... args) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,Level.INFO, mMarker, MessageFormatter.newFormat(messages, args), null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void info(CharSequence messages, Throwable e) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,Level.INFO, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}
	
	public void error(CharSequence messages, Throwable e) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,Level.ERROR, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}
	
	public void error(CharSequence messages) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,Level.ERROR, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	@Override
	public void error(String messages, Object... args) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,Level.ERROR, mMarker, MessageFormatter.newFormat(messages, args), null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}


	public void debug(CharSequence messages) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,Level.DEBUG, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	@Override
	public void debug(String messages, Object... args) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,Level.DEBUG, mMarker, MessageFormatter.newFormat(messages, args), null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void debug(CharSequence messages, Throwable e) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,Level.DEBUG, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}
	
	public void trace(CharSequence messages) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,Level.TRACE, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	@Override
	public void trace(String messages, Object... args) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,Level.TRACE, mMarker, MessageFormatter.newFormat(messages, args), null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(CharSequence messages, Throwable e) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,Level.TRACE, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}
	
	public void warn(CharSequence messages) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,Level.WARN, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	@Override
	public void warn(String messages, Object... args) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,Level.WARN, mMarker, MessageFormatter.newFormat(messages,args), null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(CharSequence messages, Throwable e) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,Level.WARN, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}



	//////------------------//
	public void info(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerFQCN,Level.INFO,mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void info(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerFQCN,Level.INFO, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerFQCN,Level.ERROR, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerFQCN,Level.ERROR, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}



	public void debug(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerFQCN,Level.DEBUG, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void debug(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerFQCN,Level.DEBUG, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerFQCN,Level.TRACE, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerFQCN,Level.TRACE, mMarker, messages, e, addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerFQCN,Level.WARN, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerFQCN,Level.WARN, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}





	public void info(CharSequence callerFQCN,CharSequence messages) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerFQCN,Level.INFO,mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void info(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerFQCN,Level.INFO, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerFQCN,Level.ERROR, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerFQCN,Level.ERROR, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}



	public void debug(CharSequence callerFQCN,CharSequence messages) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerFQCN,Level.DEBUG, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void debug(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerFQCN,Level.DEBUG, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(CharSequence callerFQCN,CharSequence messages) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerFQCN,Level.TRACE, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}


	public void trace(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerFQCN,Level.TRACE, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerFQCN,Level.WARN, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerFQCN,Level.WARN, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}


	//////------------------//
	public void info(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerType,Level.INFO,mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void info(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerType,Level.INFO, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerType,Level.ERROR, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerType,Level.ERROR, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}



	public void debug(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerType,Level.DEBUG, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void debug(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerType,Level.DEBUG, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerType,Level.TRACE, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerType,Level.TRACE, mMarker, messages, e, addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerType,Level.WARN, mMarker, messages, null,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerType,Level.WARN, mMarker, messages, e,addedElementIndex + mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}







	public void info(Class<?> callerType,CharSequence messages) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerType,Level.INFO,mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void info(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!mIsInfo) return;
		Line line = new Line(mFormatter,callerType,Level.INFO, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerType,Level.ERROR, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void error(Class<?> callerType,CharSequence messages) {
		if(!mIsError) return;
		Line line = new Line(mFormatter,callerType,Level.ERROR, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}



	public void debug(Class<?> callerType,CharSequence messages) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerType,Level.DEBUG, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void debug(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!mIsDebug) return;
		Line line = new Line(mFormatter,callerType,Level.DEBUG, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerType,Level.TRACE, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!mIsTrace) return;
		Line line = new Line(mFormatter,callerType,Level.TRACE, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(Class<?> callerType,CharSequence messages) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerType,Level.WARN, mMarker, messages, null,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}

	public void warn(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!mIsWarn) return;
		Line line = new Line(mFormatter,callerType,Level.WARN, mMarker, messages, e,mStackTraceElementsIndex);
		mAbsLineQueue.push(line);
	}




}
