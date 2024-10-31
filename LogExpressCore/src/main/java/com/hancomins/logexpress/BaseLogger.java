


package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.Configuration;
import com.hancomins.logexpress.configuration.StyleOption;
import com.hancomins.logexpress.configuration.WriterOption;
import com.hancomins.logexpress.queue.AbsLineQueue;


@SuppressWarnings({"NonAtomicOperationOnVolatileField", "unused"})
public class BaseLogger implements Logger {

	static final BaseLogger EMPTY = new BaseLogger("", Configuration.newConfiguration().newWriterOption("NONE").setLevel(Level.OFF));

	
	protected static final int DEF_ELEMENT_IDX = 2; 
	
	private final String marker;
	private volatile LineFormatter formatter = null;
	private volatile AbsLineQueue absLineQueue = null;
	private volatile Level level;
	private volatile int stackTraceElementsIndex = 0;
	
	private boolean isInfo = false;
	private boolean isError = false;
	private boolean isDebug = false;
	private boolean isTrace = false;
	private boolean isWarn = false;
	private boolean isFatal = false;

	static {
		EMPTY.parking();
	}
	

	protected BaseLogger(String marker, WriterOption option) {
		this.marker = marker;
		level =  option.getLevel();
		initFormatter(option.getPattern(), option.styleOption());
		setLevel(option.getLevel());
		stackTraceElementsIndex = DEF_ELEMENT_IDX + option.getStackTraceDepth();
	}
	
	
	protected void change(AbsLineQueue concurrentLineQueue, WriterOption option) {
		absLineQueue = concurrentLineQueue;
		level = option.getLevel();
		setLevel(level);
		initFormatter(option.getPattern(), option.styleOption());
		stackTraceElementsIndex = DEF_ELEMENT_IDX + option.getStackTraceDepth();
	}

	/**
	 * LogExpress 에서 shutdown 시 호출됩니다.
	 */
	void parking() {
		absLineQueue = new AbsLineQueue(0) {
			@Override
			public Line pop() {
				return null;
			}
		};
	}
	
	
	protected AbsLineQueue getLineQueue() {
		return absLineQueue;
	}
	
	private void initFormatter(String pattern, StyleOption styleOption) {
		formatter = LineFormatter.parse(pattern, styleOption);
	}
	
	protected void setLineQueue(AbsLineQueue queue) {
		absLineQueue = queue;
	}
	
	protected void setLevel(Level lv) {
		int lvValue = lv.getValue();
		isDebug = lvValue <= Level.DEBUG.getValue();
		isTrace = lvValue <= Level.TRACE.getValue();
		isInfo = lvValue <= Level.INFO.getValue();
		isWarn = lvValue <= Level.WARN.getValue();
		isError = lvValue <= Level.ERROR.getValue();
		isFatal = lvValue <= Level.FATAL.getValue();
	}
	
	public String getMarker() {
		return marker;
	}
	
	public Level getLevel() {
		return level;
	}
	
	@Deprecated 
	public boolean isInfo() {
		return isInfo;
	}
	
	public boolean isInfoEnabled() {
		return isInfo;
	}
	
	@Deprecated 
	public boolean isError() {
		return isError;
	}
	
	public boolean isErrorEnabled() {
		return isError;
	}

	@Override
	public boolean isFatalEnabled() {
		return isFatal;
	}

	@Deprecated 
	public boolean isDebug() {
		return isDebug;
	}
	
	public boolean isDebugEnabled() {
		return isDebug;
	}
	
	
	public boolean isTraceEnabled() {
		return isTrace;
	}
	
	@Deprecated 
	public boolean isTrace() {
		return isDebug;
	}
	
	public boolean isWarnEnabled() {
		return isWarn;
	}
	@Deprecated
	public boolean isWarn() {
		return isWarn;
	}



	@Override
	public void setAddedStackTraceElementIndex(int addedStackTraceElementIndex) {
		stackTraceElementsIndex = DEF_ELEMENT_IDX + addedStackTraceElementIndex + stackTraceElementsIndex;
	}


	@Override
	public boolean isLoggable(Level level) {
		return level.getValue() <= level.getValue();
	}



	@Override
	public void info(CharSequence messages) {
		if(!isInfo) return;
		Line line = new Line(formatter,Level.INFO, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void info(String messages, Object... args) {
		if(!isInfo) return;
		Line line = new Line(formatter,Level.INFO, marker, MessageFormatter.newFormat(messages, args), null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void info(CharSequence messages, Throwable e) {
		if(!isInfo) return;
		Line line = new Line(formatter,Level.INFO, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void error(CharSequence messages, Throwable e) {
		if(!isError) return;
		Line line = new Line(formatter,Level.ERROR, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}


	@Override
	public void error(CharSequence messages) {
		if(!isError) return;
		Line line = new Line(formatter,Level.ERROR, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void error(String messages, Object... args) {
		if(!isError) return;
		Line line = new Line(formatter,Level.ERROR, marker, MessageFormatter.newFormat(messages, args), null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}


	@Override
	public void fatal(CharSequence messages) {
		if(!isFatal) return;
		Line line = new Line(formatter,Level.FATAL, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void fatal(String messages, Object... args) {
		if(!isFatal) return;
		Line line = new Line(formatter,Level.FATAL, marker,  MessageFormatter.newFormat(messages, args), null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void fatal(CharSequence messages, Throwable e) {
		if(!isFatal) return;
		Line line = new Line(formatter,Level.FATAL, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}




	@Override
	public void debug(CharSequence messages) {
		if(!isDebug) return;
		Line line = new Line(formatter,Level.DEBUG, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void debug(String messages, Object... args) {
		if(!isDebug) return;
		Line line = new Line(formatter,Level.DEBUG, marker, MessageFormatter.newFormat(messages, args), null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void debug(CharSequence messages, Throwable e) {
		if(!isDebug) return;
		Line line = new Line(formatter,Level.DEBUG, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void trace(CharSequence messages) {
		if(!isTrace) return;
		Line line = new Line(formatter,Level.TRACE, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void trace(String messages, Object... args) {
		if(!isTrace) return;
		Line line = new Line(formatter,Level.TRACE, marker, MessageFormatter.newFormat(messages, args), null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void trace(CharSequence messages, Throwable e) {
		if(!isTrace) return;
		Line line = new Line(formatter,Level.TRACE, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void warn(CharSequence messages) {
		if(!isWarn) return;
		Line line = new Line(formatter,Level.WARN, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void warn(String messages, Object... args) {
		if(!isWarn) return;
		Line line = new Line(formatter,Level.WARN, marker, MessageFormatter.newFormat(messages,args), null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	@Override
	public void warn(CharSequence messages, Throwable e) {
		if(!isWarn) return;
		Line line = new Line(formatter,Level.WARN, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}


	//////------------------//
	public void info(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerFQCN,Level.INFO, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void info(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerFQCN,Level.INFO, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isError) return;
		Line line = new Line(formatter,callerFQCN,Level.ERROR, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!isError) return;
		Line line = new Line(formatter,callerFQCN,Level.ERROR, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}



	public void debug(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerFQCN,Level.DEBUG, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void debug(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerFQCN,Level.DEBUG, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void trace(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerFQCN,Level.TRACE, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void trace(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerFQCN,Level.TRACE, marker, messages, e, addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerFQCN,Level.WARN, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerFQCN,Level.WARN, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(CharSequence callerFQCN,CharSequence messages, int addedElementIndex) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerFQCN,Level.FATAL, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(CharSequence callerFQCN,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerFQCN,Level.FATAL, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}






	public void info(CharSequence callerFQCN,CharSequence messages) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerFQCN,Level.INFO, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void info(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerFQCN,Level.INFO, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!isError) return;
		Line line = new Line(formatter,callerFQCN,Level.ERROR, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void error(CharSequence callerFQCN,CharSequence messages) {
		if(!isError) return;
		Line line = new Line(formatter,callerFQCN,Level.ERROR, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!isError) return;
		Line line = new Line(formatter,callerFQCN,Level.FATAL, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(CharSequence callerFQCN,CharSequence messages) {
		if(!isError) return;
		Line line = new Line(formatter,callerFQCN,Level.FATAL, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}



	public void debug(CharSequence callerFQCN,CharSequence messages) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerFQCN,Level.DEBUG, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void debug(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerFQCN,Level.DEBUG, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void trace(CharSequence callerFQCN,CharSequence messages) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerFQCN,Level.TRACE, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}


	public void trace(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerFQCN,Level.TRACE, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerFQCN,Level.WARN, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void warn(CharSequence callerFQCN,CharSequence messages, Throwable e) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerFQCN,Level.WARN, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}


	//////------------------//
	public void info(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerType,Level.INFO, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void info(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerType,Level.INFO, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void error(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isError) return;
		Line line = new Line(formatter,callerType,Level.ERROR, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void error(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!isError) return;
		Line line = new Line(formatter,callerType,Level.ERROR, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isError) return;
		Line line = new Line(formatter,callerType,Level.FATAL, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!isError) return;
		Line line = new Line(formatter,callerType,Level.FATAL, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}



	public void debug(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerType,Level.DEBUG, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void debug(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerType,Level.DEBUG, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerType,Level.TRACE, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerType,Level.TRACE, marker, messages, e, addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void warn(Class<?> callerType,CharSequence messages, int addedElementIndex) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerType,Level.WARN, marker, messages, null,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void warn(Class<?> callerType,CharSequence messages, Throwable e, int addedElementIndex) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerType,Level.WARN, marker, messages, e,addedElementIndex + stackTraceElementsIndex);
		absLineQueue.push(line);
	}







	public void info(Class<?> callerType,CharSequence messages) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerType,Level.INFO, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void info(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!isInfo) return;
		Line line = new Line(formatter,callerType,Level.INFO, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}



	public void debug(Class<?> callerType,CharSequence messages) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerType,Level.DEBUG, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void debug(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!isDebug) return;
		Line line = new Line(formatter,callerType,Level.DEBUG, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerType,Level.TRACE, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void trace(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!isTrace) return;
		Line line = new Line(formatter,callerType,Level.TRACE, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void warn(Class<?> callerType,CharSequence messages) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerType,Level.WARN, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}


	public void warn(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!isWarn) return;
		Line line = new Line(formatter,callerType,Level.WARN, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}


	public void error(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!isError) return;
		Line line = new Line(formatter,callerType,Level.ERROR, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void error(Class<?> callerType,CharSequence messages) {
		if(!isError) return;
		Line line = new Line(formatter,callerType,Level.ERROR, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(Class<?> callerType,CharSequence messages, Throwable e) {
		if(!isFatal) return;
		Line line = new Line(formatter,callerType,Level.FATAL, marker, messages, e, stackTraceElementsIndex);
		absLineQueue.push(line);
	}

	public void fatal(Class<?> callerType,CharSequence messages) {
		if(!isFatal) return;
		Line line = new Line(formatter,callerType,Level.FATAL, marker, messages, null, stackTraceElementsIndex);
		absLineQueue.push(line);
	}




}
