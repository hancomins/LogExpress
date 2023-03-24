package com.clipsoft.LogExpress.writer;

import com.clipsoft.LogExpress.util.Systool;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;




public class FileNamePattern {

	
	private PatternItem[] mPatternItems = null;
	private boolean mMarkerInPattern = false; 
	private boolean mNumberInPattern = false; 
	private boolean mAvailableDate = false; 
	
	
	/**
	 * marker 를 구분할 수 있는 패턴이 있는지 확인한다.
	 * @return true, false
	 */
	boolean isMarkerInPattern() {
		return mMarkerInPattern;
	}
	
	/**
	 * 파일 번호를 구분할 수 있는 패턴이 있는지 확인한다.
	 * @return true, false
	 */
	boolean isNumberInPattern() {
		return mNumberInPattern;
	}
	
	/**
	 * 날짜를 구분할 수 있는 패턴이 있는지 확인한다.
	 * @return true, false
	 */
	boolean isDateInPattern() {
		return mAvailableDate;
	}
	
	
	
	public File toFileOverMaxSize(String marker, int maxSize) throws IOException {
		long pid = Systool.pid();
		String hostName = Systool.hostname();
		long currentTime = CurrentTimeMillisGetter.currentTimeMillis();;
		File file = null;
		long maxSizeOfByte = (long)maxSize * 1024L * 1024L;
		int number = 0;
		do {
			file = toFile(pid, hostName, marker, currentTime, number);
			++number;
		} while(file.isDirectory() || (file.exists() && file.length() >= maxSizeOfByte));
		return file;
	}
	
	public File toFile(long pid, String hostName, String marker,long timestamp, int number) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0, n = mPatternItems.length; i < n; ++i) {
			PatternItem item = mPatternItems[i];
			switch(item.type) {
				case Path:
					stringBuilder.append(item.text.replace('/', File.separatorChar));
					break;
				case Date:
					stringBuilder.append(item.dateFormat.format(new Date(timestamp)));
					break;
				case Marker:
					stringBuilder.append(marker);
					break;
				case Hostname:
					stringBuilder.append(hostName);
					break;
				case Pid:
					stringBuilder.append(pid);
					break;
				case Number:
					stringBuilder.append(number);
					break;
			}
		}
		return new File(stringBuilder.toString()).getCanonicalFile();	
	}
	
	
	
	final static FileNamePattern parse(String format) {
		final int MODE_TEXT = 0;
		final int MODE_IN_TYPE = 1;
		
		
		StringBuffer itemBuffer = new StringBuffer();
		ArrayList<PatternItem> items = new ArrayList<PatternItem>();
		String[] typeNames = ItemType.getTypeNameArray();
		boolean markerInPattern = false;
		boolean numberInPattern = false;
		boolean availableDate = false; 
		int mode = 0;
		for(int i = 0, n = format.length(); i < n; ++i) {
			char ch = format.charAt(i);
			if(ch == '{') {
				if(itemBuffer.length() > 0) {
					items.add(PatternItem.newTextType(itemBuffer.toString()));
					itemBuffer.setLength(0);
				}
				mode = MODE_IN_TYPE;
				itemBuffer.append(ch);
			} 
			else if(ch == '}' && mode == MODE_IN_TYPE) {
				itemBuffer.append(ch);
				String text = itemBuffer.toString();
				boolean appended = false; 
				for(int ti = 0; ti < typeNames.length; ++ti) {
					if("date".equalsIgnoreCase(typeNames[ti]) && text.matches("[{][\\s]{0,}(date[:]{1,})(?i).+[\\s]{0,}[}]")) {
						String pattern = text.replaceAll("[{][\\s]{0,}(date[:]{1,})(?i)", "").replaceAll("[\\s]{0,}[}]", "").trim();
						if(text.contains("d") || text.contains("D")) {
							availableDate = true;
						}
						items.add(PatternItem.newDateType(pattern));
						appended = true;
						break;
					}
					else if(text.matches("[{][\\s]{0,}(" + typeNames[ti]  +  ")(?i)[\\s]{0,}[}]")) {
						ItemType type = ItemType.typeNameOf(text.replace("{", "").replace("}", "").trim());
						if(type == ItemType.Marker) markerInPattern = true;
						else if(type == ItemType.Number) numberInPattern = true;
						items.add(PatternItem.newByType(type));
						appended = true;
						break;
					}
				}
				if(!appended && text != null && !text.isEmpty()) {
					items.add(PatternItem.newTextType(text));
				}
				mode = MODE_TEXT; 
				itemBuffer.setLength(0);
			}
			else if(mode == MODE_IN_TYPE) {
				itemBuffer.append(ch);
			}
			else {
				itemBuffer.append(ch);
			}
		}
		
		if(itemBuffer.length() > 0) {
			items.add(PatternItem.newTextType(itemBuffer.toString()));
			itemBuffer.setLength(0);
		}
		
		FileNamePattern filePattern = new FileNamePattern();
		filePattern.mAvailableDate = availableDate;
		filePattern.mPatternItems = items.toArray(new PatternItem[items.size()]);
		filePattern.mMarkerInPattern = markerInPattern;
		filePattern.mNumberInPattern = numberInPattern;
		return filePattern;
	}
	
	
	static enum ItemType{
		Marker,
		Number,
		Date,
		Hostname,
		Pid,
		Path;
		
		private ItemType() {
			
		}
		
		
		
		
		public static ItemType typeNameOf(String type) {
			ItemType[] types = values();
			for(int i = 0; i < types.length; ++i) {
				if(types[i].name().equalsIgnoreCase(type)) {
					return types[i];
				}
			}
			
			
			return null;
		}
		
		private static String[] getTypeNameArray() {
			return new String[] {"number", "date","marker", "hostname", "pid","path"};
		}
		
		
		
	}
	
	
	final static class PatternItem {
		
		String text = "";
		ItemType type = ItemType.Path;
		SimpleDateFormat dateFormat = null;
		
		
		private static PatternItem newDateType(String pattern) {
			PatternItem item = new PatternItem();
			item.type = ItemType.Date;
			item.dateFormat = new SimpleDateFormat(pattern);
			return item;
		}
		
		private static PatternItem newByType(ItemType type) { 
			PatternItem item = new PatternItem();
			item.type = type;
			return item;
		}
		
		private static PatternItem newTextType(String text) {
			
			PatternItem item = new PatternItem();
			item.type = ItemType.Path;
			item.text = text == null ? "" : text;
			return item;
		}
		
		
	}
	
}
