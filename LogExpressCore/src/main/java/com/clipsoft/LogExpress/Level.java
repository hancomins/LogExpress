package com.clipsoft.LogExpress;

public enum Level {
	INFO(2),
	TRACE(0),
	DEBUG(1),
	WARN(3),
	ERROR(4),
	OFF(5);
	
	
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
		else if(value.trim().equalsIgnoreCase("off")) {
			return OFF;
		}
		return INFO; 
		
	}
	
	public int getValue() {
		return value;
	}
	

}
