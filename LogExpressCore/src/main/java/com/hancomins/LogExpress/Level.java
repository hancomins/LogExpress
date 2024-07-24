package com.hancomins.LogExpress;

public enum Level {
	TRACE(0),
	DEBUG(1),
	INFO(2),
	WARN(3),
	ERROR(4),
	FATAL(5),
	OFF(6);
	
	
	private int value = -1;
	
	Level(int value) {
		this.value = value;
	}
	
	public static Level stringValueOf(String value) {
		if(value == null || value.isEmpty()) return INFO;
		if(value.trim().equalsIgnoreCase("info")) {
			return INFO;
		}
		else if(value.trim().equalsIgnoreCase("trace")) {
			return TRACE;
		}
		else if(value.trim().equalsIgnoreCase("debug")) {
			return DEBUG;
		}
		else if(value.trim().equalsIgnoreCase("warn")) {
			return WARN;
		}
		else if(value.trim().equalsIgnoreCase("error")) {
			return ERROR;
		}
		else if(value.trim().equalsIgnoreCase("fatal")) {
			return FATAL;
		}
		else if(value.trim().equalsIgnoreCase("off")) {
			return OFF;
		}
		return INFO; 
		
	}
	
	public int getValue() {
		return value;
	}
	

}
