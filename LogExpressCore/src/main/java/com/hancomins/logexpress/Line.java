package com.hancomins.logexpress;

import com.hancomins.logexpress.writer.CurrentTimeMillisGetter;

public class Line {

	Line(LineFormatter formatter, Level level, String marker, CharSequence message, Throwable error, int elementIndex) {
		this(formatter,"",level,marker,message,error,elementIndex);
	}

	Line(LineFormatter formatter, Class<?> caller, Level level, String marker, CharSequence message, Throwable error, int elementIndex) {
		this(formatter, caller.getName(),level,marker,message,error,elementIndex);
	}


	 Line(LineFormatter formatter, CharSequence callerFQCN, Level level, String marker, CharSequence message, Throwable error, int elementIndex) {
		 this.marker = marker;
		 this.message = message;
		 this.error = error;
		 this.level = level;
		 this.callerFQCN = callerFQCN == null ? "" : callerFQCN;
		 this.time = CurrentTimeMillisGetter.currentTimeMillis();
		 this.elementIndex = elementIndex;
		 this.lineCombiner = formatter.getLineCombiner();
		 Thread thread = null;
		 
		 if(formatter.needThreadInfo()) {
			 thread = Thread.currentThread();
			 this.tid = thread.getId();
			 this.threadName = thread.getName();
		 }
		 if(formatter.needStacktrace()) {
			 this.throwable = new Throwable();
		 }
	 }
	 
	 public CharSequence release() {
		 CharSequence result = "";
		if (lineCombiner != null) {
		 result = this.lineCombiner.combine(this);
		}
		 threadName = null;
		 marker = null;
		 message = null;
		 level = null;
		 error = null;
		 throwable = null;
		 lineCombiner = null;
		 return result;
		 
	 }
	 
	 void setThreadInfo(Thread thread) {
		 threadName = thread.getName();
		 tid = thread.getId();
	 }
	 
	 



	public long getTID() {
		return tid;
	}

	public CharSequence getCallerFQCN() {
		return callerFQCN;
	}

	public String getThreadName() {
		return threadName;
	}

	public String getMarker() {
		return marker;
	}

	public CharSequence getMessage() {
		return message;
	}

	public Throwable getError() {
		 return error;
	}

	public StackTraceElement getStackTraceElement() {
		StackTraceElement[] stackTraceElements = throwable.getStackTrace();
		if(stackTraceElements.length <= elementIndex) {
			return stackTraceElements[stackTraceElements.length - 1];	
		} else if(elementIndex < 0) {
			return stackTraceElements[0];
		}
		return throwable.getStackTrace()[elementIndex];
	}
	
	public Level getLevel() {
		return level;
	}
	
	public long getTime() {
		return time;
		
	}

	
	private int elementIndex = 0;
	private long time = 0;
	private long tid = -1;
	private String threadName = null;
	private String marker = null;
	private CharSequence message = "";
	private CharSequence callerFQCN = "";
	private Level level = null;
	private Throwable throwable = null;
	private Throwable error = null;
	private LineCombiner lineCombiner = null;
}
