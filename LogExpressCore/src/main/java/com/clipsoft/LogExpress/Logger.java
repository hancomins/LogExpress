


package com.clipsoft.LogExpress;

import com.clipsoft.LogExpress.configuration.WriterOption;

public interface Logger {


	public String getMarker();
	public Level getLevel();

	@Deprecated
	public boolean isInfo();

	public boolean isInfoEnabled();
	
	@Deprecated 
	public boolean isError();
	
	public boolean isErrorEnabled();
	@Deprecated 
	public boolean isDebug();
	
	public boolean isDebugEnabled();
	
	
	public boolean isTraceEnabled();
	
	@Deprecated 
	public boolean isTrace();
	
	public boolean isWarnEnabled();
	@Deprecated
	public boolean isWarn();

	public void setAddedStackTraceElementIndex(int addedStackTraceElementIndex);

	public boolean isLoggable(Level level);

	public void info(CharSequence messages);

	public void info(String messages, Object... args);

	public void info(CharSequence message, Throwable e);

	void error(CharSequence messages);

	public void error(String messages, Object ... args);

	public void error(CharSequence messageFormatter, Throwable e);


	public void debug(CharSequence messages);

	public void debug(String messages, Object ...args);
	
	public void debug(CharSequence messages, Throwable e);
	
	public void trace(CharSequence messages);

	public void trace(String messages, Object ...args);
	public void trace(CharSequence messages, Throwable e);
	
	public void warn(CharSequence messages);

	public void warn(String messages, Object ...args);
	public void warn(CharSequence messages, Throwable e);






}
